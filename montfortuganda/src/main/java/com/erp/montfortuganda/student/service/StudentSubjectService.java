package com.erp.montfortuganda.student.service;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.BranchNotAssignedException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import com.erp.montfortuganda.school.entity.ErpClassSubject;
import com.erp.montfortuganda.school.entity.ErpSubject;
import com.erp.montfortuganda.school.entity.SchoolClass;
import com.erp.montfortuganda.school.repository.AcademicYearRepository;
import com.erp.montfortuganda.school.repository.ErpClassSubjectRepository;
import com.erp.montfortuganda.school.repository.SchoolClassRepository;
import com.erp.montfortuganda.student.dto.request.StudentSubjectSelectionRequest;
import com.erp.montfortuganda.student.dto.response.StudentSubjectOptionResponse;
import com.erp.montfortuganda.student.dto.response.StudentSubjectSelectionResponse;
import com.erp.montfortuganda.student.entity.ErpStudent;
import com.erp.montfortuganda.student.entity.ErpStudentEnrollment;
import com.erp.montfortuganda.student.entity.ErpStudentSubject;
import com.erp.montfortuganda.student.repository.ErpStudentEnrollmentRepository;
import com.erp.montfortuganda.student.repository.ErpStudentSubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class StudentSubjectService {

    private final ErpStudentEnrollmentRepository enrollmentRepository;
    private final ErpStudentSubjectRepository studentSubjectRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SchoolClassRepository classRepository;
    private final ErpClassSubjectRepository classSubjectRepository;
    private final CurrentUserService currentUserService;

    public StudentSubjectService(
            ErpStudentEnrollmentRepository enrollmentRepository,
            ErpStudentSubjectRepository studentSubjectRepository,
            AcademicYearRepository academicYearRepository,
            SchoolClassRepository classRepository,
            ErpClassSubjectRepository classSubjectRepository,
            CurrentUserService currentUserService) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentSubjectRepository = studentSubjectRepository;
        this.academicYearRepository = academicYearRepository;
        this.classRepository = classRepository;
        this.classSubjectRepository = classSubjectRepository;
        this.currentUserService = currentUserService;
    }

    public StudentSubjectSelectionResponse getSelection(Long enrollmentId) {
        Integer branchId = requireBranchId();
        ErpStudentEnrollment enrollment = requireEnrollment(enrollmentId, branchId, false);
        Placement placement = validatePlacement(enrollment, branchId);
        return buildResponse(enrollment, placement, branchId);
    }

    @Transactional
    public StudentSubjectSelectionResponse saveSelection(Long enrollmentId, StudentSubjectSelectionRequest request) {
        if (request == null || request.subjectIds() == null) {
            throw new BadRequestException("Optional subject selections are required.");
        }

        Integer branchId = requireBranchId();
        CurrentUserContext user = currentUserService.getCurrentUserContext();
        ErpStudentEnrollment enrollment = requireEnrollment(enrollmentId, branchId, true);
        Placement placement = validatePlacement(enrollment, branchId);

        List<Long> requestedIds = request.subjectIds();
        Set<Long> selectedIds = new HashSet<>(requestedIds);
        if (selectedIds.size() != requestedIds.size()) {
            throw new BadRequestException("A subject cannot be selected more than once.");
        }
        if (selectedIds.size() != placement.requiredOptionalCount()) {
            throw new BadRequestException("Select exactly " + placement.requiredOptionalCount()
                    + " optional subject(s) for class " + placement.schoolClass().getClassCode() + ".");
        }

        Map<Long, ErpSubject> allowedOptionalSubjects = placement.rules().stream()
                .filter(rule -> rule.getSubjectRequirement() == ErpClassSubject.Requirement.OPTIONAL)
                .map(ErpClassSubject::getSubject)
                .filter(this::isSelectableSubject)
                .collect(Collectors.toMap(ErpSubject::getSubjectId, Function.identity(), (first, second) -> first,
                        LinkedHashMap::new));

        for (Long subjectId : selectedIds) {
            if (!allowedOptionalSubjects.containsKey(subjectId)) {
                throw new BadRequestException("One or more selected subjects are not valid optional subjects for this class and academic year.");
            }
        }

        // Serialize updates for the same enrollment, then lock existing placement rows.
        List<ErpStudentSubject> existingRecords = studentSubjectRepository.lockPlacementRecords(
                enrollment.getStudent().getStudentId(), branchId,
                enrollment.getAcademicYearId(), enrollment.getClassId());
        Map<Long, ErpStudentSubject> existingBySubjectId = existingRecords.stream()
                .collect(Collectors.toMap(record -> record.getSubject().getSubjectId(), Function.identity(), (first, second) -> first));

        Long userId = user.getUserId() == null ? null : user.getUserId().longValue();
        for (ErpStudentSubject existing : existingRecords) {
            boolean shouldBeActive = selectedIds.contains(existing.getSubject().getSubjectId());
            if (Boolean.TRUE.equals(existing.getActive()) != shouldBeActive) {
                existing.setActive(shouldBeActive);
                existing.setUpdatedBy(userId);
            }
        }

        for (Long subjectId : selectedIds) {
            ErpStudentSubject existing = existingBySubjectId.get(subjectId);
            if (existing != null) {
                // Re-activate the same row instead of violating the placement unique key.
                boolean changed = !Boolean.TRUE.equals(existing.getActive())
                        || !enrollment.getEnrollmentId().equals(existing.getEnrollment().getEnrollmentId());
                existing.setActive(true);
                existing.setEnrollment(enrollment);
                existing.setStudent(enrollment.getStudent());
                existing.setBranch(enrollment.getBranch());
                existing.setAcademicYear(placement.academicYear());
                existing.setSchoolClass(placement.schoolClass());
                if (changed) existing.setUpdatedBy(userId);
                continue;
            }

            ErpStudentSubject created = new ErpStudentSubject();
            created.setStudent(enrollment.getStudent());
            created.setEnrollment(enrollment);
            created.setBranch(enrollment.getBranch());
            created.setAcademicYear(placement.academicYear());
            created.setSchoolClass(placement.schoolClass());
            created.setSubject(allowedOptionalSubjects.get(subjectId));
            created.setActive(true);
            created.setCreatedBy(userId);
            created.setUpdatedBy(userId);
            studentSubjectRepository.save(created);
        }

        studentSubjectRepository.flush();
        return buildResponse(enrollment, placement, branchId);
    }

    private StudentSubjectSelectionResponse buildResponse(
            ErpStudentEnrollment enrollment, Placement placement, Integer branchId) {
        Map<Long, ErpStudentSubject> recordsBySubjectId = studentSubjectRepository.findPlacementRecords(
                        enrollment.getStudent().getStudentId(), branchId,
                        enrollment.getAcademicYearId(), enrollment.getClassId())
                .stream().collect(Collectors.toMap(
                        record -> record.getSubject().getSubjectId(), Function.identity(), (first, second) -> first));

        List<StudentSubjectOptionResponse> subjects = placement.rules().stream()
                .filter(rule -> isSelectableSubject(rule.getSubject()))
                .sorted(Comparator.comparing((ErpClassSubject rule) -> rule.getSubject().getDisplayOrder())
                        .thenComparing(rule -> rule.getSubject().getSubjectName(), String.CASE_INSENSITIVE_ORDER))
                .map(rule -> {
                    ErpSubject subject = rule.getSubject();
                    ErpStudentSubject selection = recordsBySubjectId.get(subject.getSubjectId());
                    boolean selected = selection != null && Boolean.TRUE.equals(selection.getActive());
                    return new StudentSubjectOptionResponse(
                            subject.getSubjectId(), subject.getSubjectCode(), subject.getSubjectName(),
                            subject.getSubjectType().name(), rule.getSubjectRequirement().name(), selected,
                            selection == null ? null : selection.getStudentSubjectId());
                }).toList();

        ErpStudent student = enrollment.getStudent();
        String studentName = Stream.of(student.getFirstName(), student.getMiddleName(), student.getLastName())
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim).collect(Collectors.joining(" "));

        return new StudentSubjectSelectionResponse(
                student.getStudentId(), student.getAdmissionNo(), studentName,
                enrollment.getEnrollmentId(), enrollment.getAcademicYearId(), enrollment.getClassId(),
                placement.schoolClass().getClassCode(), placement.requiredOptionalCount(), subjects);
    }

    private Placement validatePlacement(ErpStudentEnrollment enrollment, Integer branchId) {
        if (!Boolean.TRUE.equals(enrollment.getActive())
                || enrollment.getEnrollmentStatus() != ErpStudentEnrollment.EnrollmentStatus.ACTIVE) {
            throw new BadRequestException("Subjects can only be managed for an active student enrollment.");
        }
        ErpStudent student = enrollment.getStudent();
        if (student == null || !Boolean.TRUE.equals(student.getActive())
                || student.getBranch() == null || !branchId.equals(student.getBranch().getBranchId())) {
            throw new ResourceNotFoundException("Active student was not found in your school.");
        }

        ErpAcademicYear year = academicYearRepository
                .findByAcademicYearIdAndBranchBranchIdAndActiveTrue(enrollment.getAcademicYearId(), branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year was not found in your school."));

        if (!classRepository.existsAvailableForBranchAndAcademicYear(
                enrollment.getClassId(), branchId, enrollment.getAcademicYearId(), 1)) {
            throw new ResourceNotFoundException("Enrollment class is not available for its academic year.");
        }
        SchoolClass schoolClass = classRepository.findByClassIdAndBranch_BranchId(
                        enrollment.getClassId(), branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment class was not found in your school."));
        String classCode = schoolClass.getClassCode() == null
                ? "" : schoolClass.getClassCode().trim().toUpperCase(Locale.ROOT);
        if (!classCode.matches("S[1-4]")) {
            throw new BadRequestException("Student subject selection is available only for O-Level classes S1 to S4.");
        }

        int requiredCount = (classCode.equals("S1") || classCode.equals("S2")) ? 1 : 2;
        List<ErpClassSubject> applicableRules = classSubjectRepository
                .findAllByBranch_BranchIdAndAcademicYear_AcademicYearIdAndActiveTrue(
                        branchId, enrollment.getAcademicYearId())
                .stream()
                .filter(rule -> isApplicableToClass(rule, schoolClass))
                .filter(rule -> rule.getSubject().getBranch() != null
                        && branchId.equals(rule.getSubject().getBranch().getBranchId()))
                .toList();

        long optionalCount = applicableRules.stream()
                .filter(rule -> rule.getSubjectRequirement() == ErpClassSubject.Requirement.OPTIONAL)
                .filter(rule -> isSelectableSubject(rule.getSubject()))
                .map(rule -> rule.getSubject().getSubjectId()).distinct().count();
        if (optionalCount < requiredCount) {
            throw new BadRequestException("Subject configuration is incomplete for class " + classCode
                    + ". Configure at least " + requiredCount + " active optional subject(s) first.");
        }
        return new Placement(year, schoolClass, applicableRules, requiredCount);
    }

    private boolean isApplicableToClass(ErpClassSubject rule, SchoolClass schoolClass) {
        if (rule.getStartClass() == null || rule.getStartClass().getDisplayOrder() == null
                || schoolClass.getDisplayOrder() == null) return false;
        int start = rule.getStartClass().getDisplayOrder();
        int end = rule.getEndClass() == null || rule.getEndClass().getDisplayOrder() == null
                ? start : rule.getEndClass().getDisplayOrder();
        int current = schoolClass.getDisplayOrder();
        return start <= current && current <= end;
    }

    private boolean isSelectableSubject(ErpSubject subject) {
        return subject != null && Boolean.TRUE.equals(subject.getActive())
                && subject.getStatus() == ErpSubject.Status.ACTIVE;
    }

    private ErpStudentEnrollment requireEnrollment(Long enrollmentId, Integer branchId, boolean lock) {
        if (enrollmentId == null || enrollmentId <= 0) {
            throw new BadRequestException("Enrollment ID must be greater than zero.");
        }
        return (lock
                ? enrollmentRepository.findForSubjectSelectionUpdate(enrollmentId, branchId)
                : enrollmentRepository.findByEnrollmentIdAndBranch_BranchId(enrollmentId, branchId))
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment was not found in your school."));
    }

    private Integer requireBranchId() {
        Integer branchId = currentUserService.getCurrentUserContext().getBranchId();
        if (branchId == null || branchId <= 0) {
            throw new BranchNotAssignedException("Your account is not assigned to a school branch.");
        }
        return branchId;
    }

    private record Placement(
            ErpAcademicYear academicYear,
            SchoolClass schoolClass,
            List<ErpClassSubject> rules,
            int requiredOptionalCount) {}
}
