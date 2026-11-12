package com.erp.montfortuganda.student.service;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.school.entity.ErpALevelCombination;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import com.erp.montfortuganda.school.entity.SchoolClass;
import com.erp.montfortuganda.school.repository.AcademicYearRepository;
import com.erp.montfortuganda.school.repository.ErpALevelCombinationRepository;
import com.erp.montfortuganda.school.repository.SchoolClassRepository;
import com.erp.montfortuganda.student.dto.request.ErpStudentALevelSelectionRequest;
import com.erp.montfortuganda.student.dto.response.ErpStudentALevelSelectionResponse;
import com.erp.montfortuganda.student.entity.ErpStudent;
import com.erp.montfortuganda.student.entity.ErpStudentALevelSelection;
import com.erp.montfortuganda.student.entity.ErpStudentEnrollment;
import com.erp.montfortuganda.student.repository.ErpStudentALevelSelectionRepository;
import com.erp.montfortuganda.student.repository.ErpStudentEnrollmentRepository;
import com.erp.montfortuganda.student.repository.ErpStudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles branch-scoped A' Level combination selection for enrolled students.
 *
 * Application conversion and S5-to-S6 promotion are deliberately handled by
 * their respective workflow integration points, not by this direct student
 * assignment operation.
 */
@Service
@Transactional(readOnly = true)
public class ErpStudentALevelSelectionService {

    private static final int A_LEVEL_ID = 4;

    private final CurrentUserService currentUserService;
    private final ErpStudentRepository studentRepository;
    private final ErpStudentEnrollmentRepository enrollmentRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final ErpALevelCombinationRepository combinationRepository;
    private final ErpStudentALevelSelectionRepository selectionRepository;

    public ErpStudentALevelSelectionService(
            CurrentUserService currentUserService,
            ErpStudentRepository studentRepository,
            ErpStudentEnrollmentRepository enrollmentRepository,
            AcademicYearRepository academicYearRepository,
            SchoolClassRepository schoolClassRepository,
            ErpALevelCombinationRepository combinationRepository,
            ErpStudentALevelSelectionRepository selectionRepository
    ) {
        this.currentUserService = currentUserService;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.academicYearRepository = academicYearRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.combinationRepository = combinationRepository;
        this.selectionRepository = selectionRepository;
    }

    /**
     * Assigns a combination or replaces the student's current combination.
     * The previous current selection is retained as SUPERSEDED.
     */
    @Transactional
    public ErpStudentALevelSelectionResponse assignOrChange(
            Long studentId,
            ErpStudentALevelSelectionRequest request
    ) {
        CurrentUserContext user = currentUserService.getCurrentUserContext();
        Integer branchId = requireBranch(user);

        ErpStudent student = studentRepository
                .findByStudentIdAndBranch_BranchIdAndActiveTrue(studentId, branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active student not found in the current school."));

        ErpStudentEnrollment enrollment = enrollmentRepository
                .findByStudent_StudentIdAndBranch_BranchIdAndActiveTrue(studentId, branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active student enrollment not found."));

        if (!enrollment.getAcademicYearId().equals(request.academicYearId())
                || !enrollment.getClassId().equals(request.classId())) {
            throw new BadRequestException(
                    "The requested academic year and class must match the student's current enrollment.");
        }

        ErpAcademicYear academicYear = academicYearRepository
                .findByAcademicYearIdAndBranchBranchIdAndActiveTrue(
                        request.academicYearId(), branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active academic year not found in the current school."));

        SchoolClass schoolClass = schoolClassRepository
                .findByClassIdAndBranch_BranchIdAndAcademicYear_AcademicYearId(
                        request.classId(), branchId, request.academicYearId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Class not found for the selected school and academic year."));

        if (schoolClass.getLevel() == null
                || schoolClass.getLevel().getLevelId() != A_LEVEL_ID) {
            throw new BadRequestException(
                    "A' Level combinations can only be assigned to S5 or S6 classes.");
        }

        ErpALevelCombination combination = combinationRepository
                .findByCombinationIdAndBranch_BranchIdAndActiveTrue(
                        request.combinationId(), branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active A' Level combination not found in the current school."));

        ErpStudentALevelSelection current = selectionRepository
                .findCurrentStudentSelectionForUpdate(
                        studentId,
                        branchId,
                        request.academicYearId(),
                        request.classId(),
                        ErpStudentALevelSelectionRepository.CURRENT_STATUSES
                )
                .orElse(null);

        if (current != null
                && current.getCombination().getCombinationId()
                .equals(combination.getCombinationId())) {
            return toResponse(current);
        }

        if (current != null) {
            current.setStatus(ErpStudentALevelSelection.SelectionStatus.SUPERSEDED);
            current.setUpdatedBy(user.getUserId() == null
                    ? null : user.getUserId().longValue());
            // Flush releases the generated-column unique key before inserting
            // the replacement current selection.
            selectionRepository.saveAndFlush(current);
        }

        ErpStudentALevelSelection selection = new ErpStudentALevelSelection();
        selection.setStudent(student);
        selection.setEnrollment(enrollment);
        selection.setBranch(student.getBranch());
        selection.setAcademicYear(academicYear);
        selection.setSchoolClass(schoolClass);
        selection.setCombination(combination);
        selection.setStatus(ErpStudentALevelSelection.SelectionStatus.CONFIRMED);
        selection.setSelectedAt(LocalDateTime.now());
        selection.setSelectedBy(user.getUserId() == null
                ? null : user.getUserId().longValue());
        selection.setCreatedBy(user.getUserId() == null
                ? null : user.getUserId().longValue());
        selection.setUpdatedBy(user.getUserId() == null
                ? null : user.getUserId().longValue());

        return toResponse(selectionRepository.save(selection));
    }

    /** Returns the complete selection history for a branch-owned student. */
    public List<ErpStudentALevelSelectionResponse> getStudentHistory(Long studentId) {
        CurrentUserContext user = currentUserService.getCurrentUserContext();
        Integer branchId = requireBranch(user);

        if (!studentRepository.existsById(studentId)
                || studentRepository.findByStudentIdAndBranch_BranchId(
                        studentId, branchId).isEmpty()) {
            throw new ResourceNotFoundException(
                    "Student not found in the current school.");
        }

        return selectionRepository
                .findAllByStudent_StudentIdAndBranch_BranchIdOrderByAcademicYear_AcademicYearIdDescCreatedAtDesc(
                        studentId, branchId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Integer requireBranch(CurrentUserContext user) {
        if (user.getBranchId() == null) {
            throw new BadRequestException(
                    "The authenticated user is not assigned to a school branch.");
        }
        return user.getBranchId();
    }

    private ErpStudentALevelSelectionResponse toResponse(
            ErpStudentALevelSelection selection
    ) {
        ErpALevelCombination combination = selection.getCombination();

        return new ErpStudentALevelSelectionResponse(
                selection.getSelectionId(),
                selection.getApplication() == null
                        ? null : selection.getApplication().getApplicationId(),
                selection.getStudent() == null
                        ? null : selection.getStudent().getStudentId(),
                selection.getEnrollment() == null
                        ? null : selection.getEnrollment().getEnrollmentId(),
                selection.getBranch().getBranchId(),
                selection.getAcademicYear().getAcademicYearId(),
                selection.getAcademicYear().getAcademicYearName(),
                selection.getSchoolClass().getClassId(),
                selection.getSchoolClass().getClassName(),
                combination.getCombinationId(),
                combination.getCombinationCode(),
                combination.getCombinationName(),
                combination.getPrincipalSubject1().getSubjectId(),
                combination.getPrincipalSubject1().getSubjectName(),
                combination.getPrincipalSubject2().getSubjectId(),
                combination.getPrincipalSubject2().getSubjectName(),
                combination.getPrincipalSubject3().getSubjectId(),
                combination.getPrincipalSubject3().getSubjectName(),
                combination.getSubsidiarySubject().getSubjectId(),
                combination.getSubsidiarySubject().getSubjectName(),
                selection.getStatus().name(),
                selection.getSelectedAt(),
                selection.getSelectedBy(),
                selection.getVersion(),
                selection.getCreatedAt(),
                selection.getUpdatedAt()
        );
    }
}
