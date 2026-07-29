package com.erp.montfortuganda.student.bulkimport.service;

import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.DuplicateResourceException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.student.dto.request.StudentCreateRequest;
import com.erp.montfortuganda.student.dto.request.StudentEnrollmentRequest;
import com.erp.montfortuganda.student.dto.request.StudentParentRequest;
import com.erp.montfortuganda.student.dto.request.StudentPersonalRequest;
import com.erp.montfortuganda.student.entity.ErpParent;
import com.erp.montfortuganda.student.entity.ErpStudent;
import com.erp.montfortuganda.student.entity.ErpStudentAcademicHistory;
import com.erp.montfortuganda.student.entity.ErpStudentEnrollment;
import com.erp.montfortuganda.student.entity.ErpStudentEnrollmentHistory;
import com.erp.montfortuganda.student.entity.ErpStudentMedical;
import com.erp.montfortuganda.student.dto.request.StudentParentRequest;
import com.erp.montfortuganda.student.dto.request.StudentPersonalRequest;
import com.erp.montfortuganda.student.entity.ErpParent;
import com.erp.montfortuganda.student.entity.ErpStudent;
import com.erp.montfortuganda.student.entity.ErpStudentAcademicHistory;
import com.erp.montfortuganda.student.entity.ErpStudentEnrollment;
import com.erp.montfortuganda.student.entity.ErpStudentEnrollmentHistory;
import com.erp.montfortuganda.student.entity.ErpStudentMedical;
import com.erp.montfortuganda.student.mapper.StudentMapper;
import com.erp.montfortuganda.student.service.StudentNumberService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates one Student from a validated Student bulk-import row.
 * <p>Every invocation runs in an independent transaction. A failure in one
 * Excel row does not roll back Students created from other valid rows.</p>
 * <p>Branch ownership, Admission Number, Student Code, status values,
 * audit fields and entity versions remain controlled by the backend.</p>
 */
@Service
@RequiredArgsConstructor
public class StudentBulkImportTransactionService {

    private final BranchRepository branchRepository;
    private final StudentNumberService numberService;
    private final StudentMapper studentMapper;
    private final EntityManager entityManager;
    private final Validator validator;

    /**
     * Creates one Student in an independent transaction.
     *
     * @param request         validated Student registration request
     * @param branchId        trusted authenticated branch ID
     * @param createdByUserId trusted authenticated user ID
     * @return safe Student creation result
     */
    @SuppressWarnings("unused")
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            rollbackFor = Exception.class
    )
    public StudentBulkCreationResult createStudent(
            StudentCreateRequest request,
            Integer branchId,
            Integer createdByUserId
    ) {
        Objects.requireNonNull(
                request,
                "Student registration request is required."
        );

        validateContext(
                branchId,
                createdByUserId
        );

        validateRequest(request);
        validateBulkRequestScope(request);

        Branch branch =
                requireBranch(branchId);

        validateAcademicPlacement(
                request.enrollment(),
                branchId
        );

        validateLearnerLinUniqueness(
                request.personal()
        );

        validateRollNumberUniqueness(
                request.enrollment(),
                branchId
        );

        validateExistingStudentDuplicate(
                request,
                branchId
        );

        Long authenticatedUserId =
                createdByUserId.longValue();

        String studentCode =
                numberService.generateStudentCode(
                        branch,
                        request.enrollment()
                                .academicYearId(),
                        createdByUserId
                );

        String admissionNo =
                numberService.generateAdmissionNumber(
                        branch,
                        createdByUserId
                );

        ErpStudent student =
                studentMapper.toNewStudent(
                        request.personal(),
                        branch,
                        null,
                        admissionNo,
                        studentCode,
                        authenticatedUserId
                );

        entityManager.persist(student);
        entityManager.flush();

        ErpParent parent =
                studentMapper.toNewParent(
                        request.parent(),
                        student,
                        branch,
                        authenticatedUserId
                );

        entityManager.persist(parent);

        ErpStudentEnrollment enrollment =
                studentMapper.toNewEnrollment(
                        request.enrollment(),
                        student,
                        branch,
                        authenticatedUserId
                );

        entityManager.persist(enrollment);
        entityManager.flush();

        ErpStudentEnrollmentHistory enrollmentHistory =
                studentMapper.toInitialEnrollmentHistory(
                        enrollment,
                        authenticatedUserId
                );

        entityManager.persist(enrollmentHistory);

        persistMedicalInformation(
                request,
                student,
                branch,
                authenticatedUserId
        );

        persistAcademicHistory(
                request,
                student,
                branch,
                authenticatedUserId
        );

        entityManager.flush();

        entityManager.refresh(student);
        entityManager.refresh(enrollment);

        return new StudentBulkCreationResult(
                student.getStudentId(),
                enrollment.getEnrollmentId(),
                student.getAdmissionNo(),
                student.getStudentCode(),
                student.getFullName(),
                branch.getBranchId(),
                enrollment.getAcademicYearId(),
                enrollment.getClassId(),
                enrollment.getSectionId(),
                student.getStudentStatus(),
                enrollment.getEnrollmentStatus() == null
                        ? null
                        : enrollment.getEnrollmentStatus()
                        .name(),
                student.getVersion()
        );
    }

    // =====================================================================
    // CONTEXT VALIDATION
    // =====================================================================

    private void validateContext(
            Integer branchId,
            Integer createdByUserId
    ) {
        if (branchId == null || branchId <= 0) {
            throw new BadRequestException(
                    "A valid Student import branch ID is required."
            );
        }

        if (
                createdByUserId == null
                || createdByUserId <= 0
        ) {
            throw new BadRequestException(
                    "A valid Student import user ID is required."
            );
        }
    }

    private Branch requireBranch(
            Integer branchId
    ) {
        Branch branch =
                branchRepository.findById(branchId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "The Student import branch was not found."
                                )
                        );

        if (
                !Integer.valueOf(1)
                        .equals(branch.getIsActive())
        ) {
            throw new BadRequestException(
                    "The Student import branch is inactive."
            );
        }

        return branch;
    }

    // =====================================================================
    // REQUEST VALIDATION
    // =====================================================================

    private void validateRequest(
            StudentCreateRequest request
    ) {
        Set<ConstraintViolation<StudentCreateRequest>> violations =
                validator.validate(request);

        if (violations.isEmpty()) {
            return;
        }

        String validationMessage =
                violations.stream()
                        .sorted(
                                Comparator.comparing(
                                        this::violationPropertyPath
                                )
                        )
                        .map(this::formatViolation)
                        .distinct()
                        .collect(
                                Collectors.joining(" ")
                        );

        throw new BadRequestException(
                validationMessage.isBlank()
                        ? "Student import row contains invalid data."
                        : validationMessage
        );
    }

    private String violationPropertyPath(
            ConstraintViolation<StudentCreateRequest> violation
    ) {
        return violation.getPropertyPath()
                .toString();
    }

    private String formatViolation(
            ConstraintViolation<StudentCreateRequest> violation
    ) {
        String property =
                violationPropertyPath(violation);

        String message =
                violation.getMessage();

        if (!StringUtils.hasText(property)) {
            return message;
        }

        return property
               + ": "
               + message;
    }

    /**
     * Bulk import does not convert admission applications or create
     * hostel and transport allocations directly.
     */
    private void validateBulkRequestScope(
            StudentCreateRequest request
    ) {
        if (request.applicationId() != null) {
            throw new BadRequestException(
                    "Student bulk import cannot convert an admission application."
            );
        }

        if (request.hostel() != null) {
            throw new BadRequestException(
                    "Hostel allocation must be managed through the Hostel module."
            );
        }

        if (request.transport() != null) {
            throw new BadRequestException(
                    "Transport allocation must be managed through the Transport module."
            );
        }
    }

    // =====================================================================
    // ACADEMIC PLACEMENT
    // =====================================================================

    private void validateAcademicPlacement(
            StudentEnrollmentRequest enrollment,
            Integer branchId
    ) {
        if (enrollment == null) {
            throw new BadRequestException(
                    "Student enrollment information is required."
            );
        }

        if (
                enrollment.academicYearId() == null
                || enrollment.academicYearId() <= 0
        ) {
            throw new BadRequestException(
                    "A valid Academic Year is required."
            );
        }

        if (
                enrollment.classId() == null
                || enrollment.classId() <= 0
        ) {
            throw new BadRequestException(
                    "A valid Class is required."
            );
        }

        if (
                !existsActiveAcademicYear(
                        enrollment.academicYearId()
                )
        ) {
            throw new ResourceNotFoundException(
                    "Selected Academic Year was not found or is inactive."
            );
        }

        if (
                !existsBranchClass(
                        branchId,
                        enrollment.classId()
                )
        ) {
            throw new ResourceNotFoundException(
                    "Selected Class is not available for the authenticated branch."
            );
        }

        if (
                enrollment.sectionId() != null
                && !existsMatchingSection(
                        enrollment.sectionId(),
                        branchId,
                        enrollment.academicYearId(),
                        enrollment.classId()
                )
        ) {
            throw new ResourceNotFoundException(
                    "Selected Section does not belong to the selected "
                    + "branch, Academic Year and Class."
            );
        }
    }

    private boolean existsActiveAcademicYear(
            Long academicYearId
    ) {
        Number count =
                (Number) entityManager
                        .createNativeQuery(
                                """
                                select count(*)
                                from erp_academic_years
                                where academic_year_id = :academicYearId
                                  and active = 1
                                  and upper(status)
                                      in ('PLANNED', 'ACTIVE')
                                """
                        )
                        .setParameter(
                                "academicYearId",
                                academicYearId
                        )
                        .getSingleResult();

        return count.longValue() > 0;
    }

    private boolean existsBranchClass(
            Integer branchId,
            Integer classId
    ) {
        Number count =
                (Number) entityManager
                        .createNativeQuery(
                                """
                                select count(*)
                                from erp_classes school_class
                                join erp_branch_levels branch_level
                                  on branch_level.level_id =
                                     school_class.level_id
                                join erp_levels level
                                  on level.level_id =
                                     school_class.level_id
                                where branch_level.branch_id = :branchId
                                  and school_class.class_id = :classId
                                  and school_class.status = 1
                                  and level.status = 1
                                """
                        )
                        .setParameter(
                                "branchId",
                                branchId
                        )
                        .setParameter(
                                "classId",
                                classId
                        )
                        .getSingleResult();

        return count.longValue() > 0;
    }

    private boolean existsMatchingSection(
            Long sectionId,
            Integer branchId,
            Long academicYearId,
            Integer classId
    ) {
        Number count =
                (Number) entityManager
                        .createNativeQuery(
                                """
                                select count(*)
                                from erp_sections
                                where section_id = :sectionId
                                  and branch_id = :branchId
                                  and academic_year_id = :academicYearId
                                  and class_id = :classId
                                  and active = 1
                                  and upper(status) = 'ACTIVE'
                                """
                        )
                        .setParameter(
                                "sectionId",
                                sectionId
                        )
                        .setParameter(
                                "branchId",
                                branchId
                        )
                        .setParameter(
                                "academicYearId",
                                academicYearId
                        )
                        .setParameter(
                                "classId",
                                classId
                        )
                        .getSingleResult();

        return count.longValue() > 0;
    }

    // =====================================================================
    // UNIQUENESS VALIDATION
    // =====================================================================

    private void validateLearnerLinUniqueness(
            StudentPersonalRequest personal
    ) {
        if (
                personal == null
                || !StringUtils.hasText(
                        personal.learnerLin()
                )
        ) {
            return;
        }

        Number count =
                (Number) entityManager
                        .createNativeQuery(
                                """
                                select count(*)
                                from erp_students
                                where upper(trim(learner_lin)) =
                                      upper(trim(:learnerLin))
                                """
                        )
                        .setParameter(
                                "learnerLin",
                                personal.learnerLin()
                                        .trim()
                        )
                        .getSingleResult();

        if (count.longValue() > 0) {
            throw new DuplicateResourceException(
                    "Another Student already uses Learner Identification Number "
                    + personal.learnerLin()
                            .trim()
                            .toUpperCase(Locale.ROOT)
                    + "."
            );
        }
    }

    private void validateRollNumberUniqueness(
            StudentEnrollmentRequest enrollment,
            Integer branchId
    ) {
        if (
                enrollment == null
                || !StringUtils.hasText(
                        enrollment.rollNo()
                )
        ) {
            return;
        }

        Query query =
                entityManager.createNativeQuery(
                        rollNumberUniquenessSql(
                                enrollment.sectionId()
                        )
                );

        query.setParameter(
                "branchId",
                branchId
        );

        query.setParameter(
                "academicYearId",
                enrollment.academicYearId()
        );

        query.setParameter(
                "classId",
                enrollment.classId()
        );

        query.setParameter(
                "rollNo",
                enrollment.rollNo()
                        .trim()
        );

        if (enrollment.sectionId() != null) {
            query.setParameter(
                    "sectionId",
                    enrollment.sectionId()
            );
        }

        Number count =
                (Number) query.getSingleResult();

        if (count.longValue() > 0) {
            throw new DuplicateResourceException(
                    "Another Student already uses roll number "
                    + enrollment.rollNo()
                            .trim()
                    + " in the selected Class and Section."
            );
        }
    }

    private String rollNumberUniquenessSql(
            Long sectionId
    ) {
        String baseSql =
                """
                select count(*)
                from erp_student_enrollment
                where branch_id = :branchId
                  and academic_year_id = :academicYearId
                  and class_id = :classId
                  and upper(trim(roll_no)) =
                      upper(trim(:rollNo))
                """;

        if (sectionId == null) {
            return baseSql
                   + " and section_id is null";
        }

        return baseSql
               + " and section_id = :sectionId";
    }

    /**
     * Prevents the same Student from being created again when a corrected
     * workbook is uploaded after a partial import.
     */
    private void validateExistingStudentDuplicate(
            StudentCreateRequest request,
            Integer branchId
    ) {
        StudentPersonalRequest personal =
                request.personal();

        StudentParentRequest parent =
                request.parent();

        if (
                personal == null
                || parent == null
                || personal.dateOfBirth() == null
        ) {
            return;
        }

        String preferredPhone =
                resolvePreferredContactPhone(parent);

        if (!StringUtils.hasText(preferredPhone)) {
            return;
        }

        Number count =
                (Number) entityManager
                        .createNativeQuery(
                                existingStudentDuplicateSql()
                        )
                        .setParameter(
                                "branchId",
                                branchId
                        )
                        .setParameter(
                                "firstName",
                                normalizedName(
                                        personal.firstName()
                                )
                        )
                        .setParameter(
                                "middleName",
                                normalizedName(
                                        personal.middleName()
                                )
                        )
                        .setParameter(
                                "lastName",
                                normalizedName(
                                        personal.lastName()
                                )
                        )
                        .setParameter(
                                "dateOfBirth",
                                personal.dateOfBirth()
                        )
                        .setParameter(
                                "preferredPhone",
                                normalizedPhone(
                                        preferredPhone
                                )
                        )
                        .getSingleResult();

        if (count.longValue() > 0) {
            throw new DuplicateResourceException(
                    "A Student with the same name, Date of Birth and "
                    + "preferred-contact mobile number already exists."
            );
        }
    }

    private String existingStudentDuplicateSql() {
        String sql =
                """
                select count(*)
                from erp_students student
                join erp_parents parent
                  on parent.student_id = student.student_id
                where student.branch_id = :branchId
                  and student.active = 1
                  and parent.active = 1
                  and lower(trim(student.first_name)) = :firstName
                  and lower(
                          trim(
                              coalesce(
                                  student.middle_name,
                                  ''
                              )
                          )
                      ) = :middleName
                  and lower(
                          trim(
                              coalesce(
                                  student.last_name,
                                  ''
                              )
                          )
                      ) = :lastName
                  and student.date_of_birth = :dateOfBirth
                  and (
                      NORMALIZED_FATHER_PHONE = :preferredPhone
                      or NORMALIZED_MOTHER_PHONE = :preferredPhone
                      or NORMALIZED_GUARDIAN_PHONE = :preferredPhone
                  )
                """;

        return sql
                .replace(
                        "NORMALIZED_FATHER_PHONE",
                        normalizedPhoneSql(
                                "parent.father_phone"
                        )
                )
                .replace(
                        "NORMALIZED_MOTHER_PHONE",
                        normalizedPhoneSql(
                                "parent.mother_phone"
                        )
                )
                .replace(
                        "NORMALIZED_GUARDIAN_PHONE",
                        normalizedPhoneSql(
                                "parent.guardian_phone"
                        )
                );
    }

    private String resolvePreferredContactPhone(
            StudentParentRequest parent
    ) {
        if (parent.preferredContact() == null) {
            return null;
        }

        return switch (parent.preferredContact()) {
            case FATHER ->
                    parent.fatherPhone();

            case MOTHER ->
                    parent.motherPhone();

            case GUARDIAN ->
                    parent.guardianPhone();
        };
    }

    private String normalizedName(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return value.trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizedPhone(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return value.trim()
                .replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "");
    }

    private String normalizedPhoneSql(
            String column
    ) {
        return "replace("
               + "replace("
               + "replace("
               + "replace("
               + "trim(coalesce("
               + column
               + ", '')), "
               + "' ', ''), "
               + "'-', ''), "
               + "'(', ''), "
               + "')', '')";
    }

    // =====================================================================
    // OPTIONAL RECORDS
    // =====================================================================

    private void persistMedicalInformation(
            StudentCreateRequest request,
            ErpStudent student,
            Branch branch,
            Long authenticatedUserId
    ) {
        if (
                request.medical() == null
                || !studentMapper.hasMedicalData(
                        request.medical()
                )
        ) {
            return;
        }

        ErpStudentMedical medical =
                studentMapper.toNewMedical(
                        request.medical(),
                        student,
                        branch,
                        authenticatedUserId
                );

        entityManager.persist(medical);
    }

    private void persistAcademicHistory(
            StudentCreateRequest request,
            ErpStudent student,
            Branch branch,
            Long authenticatedUserId
    ) {
        if (
                request.academicHistory() == null
                || !studentMapper
                        .hasAcademicHistoryData(
                                request.academicHistory()
                        )
        ) {
            return;
        }

        ErpStudentAcademicHistory academicHistory =
                studentMapper.toNewAcademicHistory(
                        request.academicHistory(),
                        student,
                        branch,
                        authenticatedUserId
                );

        entityManager.persist(academicHistory);
    }

    // =====================================================================
    // RESULT
    // =====================================================================

    /**
     * Safe result returned to the Student bulk-import processor.
     */
    public record StudentBulkCreationResult(
            Long studentId,
            Long enrollmentId,
            String admissionNo,
            String studentCode,
            String fullName,
            Integer branchId,
            Long academicYearId,
            Integer classId,
            Long sectionId,
            String studentStatus,
            String enrollmentStatus,
            Long version
    ) {
    }
}