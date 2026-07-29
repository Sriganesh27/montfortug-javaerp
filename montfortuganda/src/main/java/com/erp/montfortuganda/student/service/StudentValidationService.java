package com.erp.montfortuganda.student.service;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.BranchNotAssignedException;
import com.erp.montfortuganda.exception.DuplicateResourceException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.student.dto.request.StudentCreateRequest;
import com.erp.montfortuganda.student.dto.request.StudentEnrollmentRequest;
import com.erp.montfortuganda.student.dto.request.StudentEnrollmentUpdateRequest;
import com.erp.montfortuganda.student.dto.request.StudentHostelRequest;
import com.erp.montfortuganda.student.dto.request.StudentParentRequest;
import com.erp.montfortuganda.student.dto.request.StudentPersonalRequest;
import com.erp.montfortuganda.student.dto.request.StudentStatusChangeRequest;
import com.erp.montfortuganda.student.dto.request.StudentTransportRequest;
import com.erp.montfortuganda.student.dto.request.StudentUpdateRequest;
import com.erp.montfortuganda.student.entity.ErpStudent;
import com.erp.montfortuganda.student.entity.ErpStudentEnrollment;
import com.erp.montfortuganda.student.enums.StudentStatus;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

/**
 * Performs database-aware and cross-record validation for the Student module.
 *
 * <p>Bean Validation protects the request format. This service protects:</p>
 *
 * <ul>
 *     <li>authenticated branch ownership;</li>
 *     <li>active branch ownership;</li>
 *     <li>Student and enrollment optimistic versions;</li>
 *     <li>academic-year, class and section references;</li>
 *     <li>application ownership and duplicate conversion;</li>
 *     <li>Learner Identification Number uniqueness;</li>
 *     <li>roll-number uniqueness;</li>
 *     <li>parent and preferred-contact consistency;</li>
 *     <li>Student and enrollment status transitions.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class StudentValidationService {

    private final CurrentUserService currentUserService;
    private final BranchRepository branchRepository;
    private final EntityManager entityManager;

    public StudentValidationService(
            CurrentUserService currentUserService,
            BranchRepository branchRepository,
            EntityManager entityManager
    ) {
        this.currentUserService = currentUserService;
        this.branchRepository = branchRepository;
        this.entityManager = entityManager;
    }

    // =====================================================================
    // CREATE
    // =====================================================================

    public CreateReferences validateForCreate(
            StudentCreateRequest request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Student registration information is required."
            );
        }

        BranchContext branchContext =
                requireAuthenticatedBranch();

        validatePersonal(
                request.personal()
        );

        validateParent(
                request.parent()
        );

        validateEnrollmentForCreate(
                request.enrollment(),
                branchContext.branch().getBranchId()
        );

        AcademicYearWindow academicYear =
                requireAcademicYearWindow(
                        request.enrollment().academicYearId()
                );

        validateHostelForCreate(
                request.hostel(),
                request.enrollment(),
                academicYear
        );

        validateTransportForCreate(
                request.transport(),
                request.enrollment(),
                academicYear
        );

        validateLearnerLinUniqueness(
                request.personal().learnerLin(),
                null
        );

        validateApplicationReference(
                request.applicationId(),
                branchContext.branch().getBranchId()
        );

        return new CreateReferences(
                branchContext,
                academicYear.academicYearCode()
        );
    }

    // =====================================================================
    // UPDATE
    // =====================================================================

    public UpdateReferences validateForUpdate(
            Long studentId,
            StudentUpdateRequest request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Student update information is required."
            );
        }

        BranchContext branchContext =
                requireAuthenticatedBranch();

        ErpStudent student =
                requireStudent(
                        studentId,
                        branchContext.branch().getBranchId()
                );

        validateStudentVersion(
                student,
                request.version()
        );

        validatePersonal(
                request.personal()
        );

        validateParent(
                request.parent()
        );

        validateLearnerLinUniqueness(
                request.personal().learnerLin(),
                student.getStudentId()
        );

        return new UpdateReferences(
                branchContext,
                student
        );
    }

    // =====================================================================
    // STATUS CHANGE
    // =====================================================================

    public StatusChangeReferences validateForStatusChange(
            Long studentId,
            StudentStatusChangeRequest request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Student status-change information is required."
            );
        }

        BranchContext branchContext =
                requireAuthenticatedBranch();

        ErpStudent student =
                requireStudent(
                        studentId,
                        branchContext.branch().getBranchId()
                );

        validateStudentVersion(
                student,
                request.version()
        );

        StudentStatus currentStatus =
                parseStudentStatus(
                        student.getStudentStatus()
                );

        validateStudentStatusTransition(
                currentStatus,
                request.newStatus()
        );

        if (
                request.effectiveDate() != null
                        && request.effectiveDate().isAfter(
                        LocalDate.now()
                )
        ) {
            throw new BadRequestException(
                    "Student status effective date cannot be in the future."
            );
        }

        if (!StringUtils.hasText(request.reason())) {
            throw new BadRequestException(
                    "Student status-change reason is required."
            );
        }

        return new StatusChangeReferences(
                branchContext,
                student,
                currentStatus
        );
    }

    // =====================================================================
    // ENROLLMENT UPDATE
    // =====================================================================

    public EnrollmentUpdateReferences validateForEnrollmentUpdate(
            Long studentId,
            StudentEnrollmentUpdateRequest request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Student enrollment-update information is required."
            );
        }

        BranchContext branchContext =
                requireAuthenticatedBranch();

        Integer branchId =
                branchContext.branch().getBranchId();

        ErpStudent student =
                requireStudent(
                        studentId,
                        branchId
                );

        ErpStudentEnrollment enrollment =
                requireEnrollment(
                        studentId,
                        branchId
                );

        validateEnrollmentVersion(
                enrollment,
                request.version()
        );

        if (Boolean.TRUE.equals(enrollment.getIsLocked())) {
            throw new BadRequestException(
                    "This Student enrollment is locked and cannot be changed."
            );
        }

        validateAcademicPlacement(
                branchId,
                request.academicYearId(),
                request.classId(),
                request.sectionId()
        );

        validateRollNumberUniqueness(
                branchId,
                request.academicYearId(),
                request.classId(),
                request.sectionId(),
                request.rollNo(),
                enrollment.getEnrollmentId()
        );

        validateEnrollmentDates(
                enrollment,
                request
        );

        validateEnrollmentStatusTransition(
                enrollment.getEnrollmentStatus(),
                request.enrollmentStatus()
        );

        if (!StringUtils.hasText(request.changeReason())) {
            throw new BadRequestException(
                    "Enrollment change reason is required."
            );
        }

        return new EnrollmentUpdateReferences(
                branchContext,
                student,
                enrollment
        );
    }

    // =====================================================================
    // BRANCH-SAFE LOOKUPS
    // =====================================================================

    public BranchContext requireAuthenticatedBranch() {
        CurrentUserContext currentUser =
                currentUserService.getCurrentUserContext();

        Integer branchId =
                currentUser.getBranchId();

        if (branchId == null || branchId <= 0) {
            throw new BranchNotAssignedException(
                    "The authenticated user is not assigned to a branch."
            );
        }

        Branch branch =
                branchRepository
                        .findById(branchId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Assigned branch was not found."
                                )
                        );

        if (!Integer.valueOf(1).equals(branch.getIsActive())) {
            throw new BadRequestException(
                    "The assigned branch is inactive."
            );
        }

        return new BranchContext(
                branch,
                currentUser.getUserId(),
                currentUser.getUsername()
        );
    }

    public ErpStudent requireStudent(
            Long studentId,
            Integer branchId
    ) {
        if (studentId == null || studentId <= 0) {
            throw new BadRequestException(
                    "A valid Student ID is required."
            );
        }

        return entityManager
                .createQuery(
                        """
                        select student
                        from ErpStudent student
                        where student.studentId = :studentId
                          and student.branch.branchId = :branchId
                        """,
                        ErpStudent.class
                )
                .setParameter(
                        "studentId",
                        studentId
                )
                .setParameter(
                        "branchId",
                        branchId
                )
                .getResultStream()
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Student was not found."
                        )
                );
    }

    public ErpStudentEnrollment requireEnrollment(
            Long studentId,
            Integer branchId
    ) {
        if (studentId == null || studentId <= 0) {
            throw new BadRequestException(
                    "A valid Student ID is required."
            );
        }

        return entityManager
                .createQuery(
                        """
                        select enrollment
                        from ErpStudentEnrollment enrollment
                        where enrollment.student.studentId = :studentId
                          and enrollment.branch.branchId = :branchId
                        """,
                        ErpStudentEnrollment.class
                )
                .setParameter(
                        "studentId",
                        studentId
                )
                .setParameter(
                        "branchId",
                        branchId
                )
                .getResultStream()
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Current Student enrollment was not found."
                        )
                );
    }

    // =====================================================================
    // PERSONAL VALIDATION
    // =====================================================================

    private void validatePersonal(
            StudentPersonalRequest request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Student personal information is required."
            );
        }

        if (!StringUtils.hasText(request.firstName())) {
            throw new BadRequestException(
                    "Student first name is required."
            );
        }

        if (request.gender() == null) {
            throw new BadRequestException(
                    "Student gender is required."
            );
        }

        if (request.dateOfBirth() == null) {
            throw new BadRequestException(
                    "Student date of birth is required."
            );
        }

        if (request.dateOfBirth().isAfter(LocalDate.now())) {
            throw new BadRequestException(
                    "Student date of birth cannot be in the future."
            );
        }

        if (request.admissionYear() == null) {
            throw new BadRequestException(
                    "Student admission year is required."
            );
        }

        if (
                request.admissionYear() < 1900
                        || request.admissionYear() > 2100
        ) {
            throw new BadRequestException(
                    "Student admission year must be between 1900 and 2100."
            );
        }
    }

    // =====================================================================
    // PARENT VALIDATION
    // =====================================================================

    private void validateParent(
            StudentParentRequest request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Student parent information is required."
            );
        }

        boolean fatherAvailable =
                hasNameAndPhone(
                        request.fatherName(),
                        request.fatherPhone()
                );

        boolean motherAvailable =
                hasNameAndPhone(
                        request.motherName(),
                        request.motherPhone()
                );

        boolean guardianAvailable =
                hasNameAndPhone(
                        request.guardianName(),
                        request.guardianPhone()
                );

        if (
                !fatherAvailable
                        && !motherAvailable
                        && !guardianAvailable
        ) {
            throw new BadRequestException(
                    "At least one parent or guardian name and phone number is required."
            );
        }

        if (request.preferredContact() == null) {
            throw new BadRequestException(
                    "Preferred parent contact is required."
            );
        }

        switch (request.preferredContact()) {
            case FATHER -> {
                if (!fatherAvailable) {
                    throw new BadRequestException(
                            "Father name and phone are required when Father is the preferred contact."
                    );
                }
            }

            case MOTHER -> {
                if (!motherAvailable) {
                    throw new BadRequestException(
                            "Mother name and phone are required when Mother is the preferred contact."
                    );
                }
            }

            case GUARDIAN -> {
                if (!guardianAvailable) {
                    throw new BadRequestException(
                            "Guardian name and phone are required when Guardian is the preferred contact."
                    );
                }

                if (!StringUtils.hasText(
                        request.guardianRelationship()
                )) {
                    throw new BadRequestException(
                            "Guardian relationship is required."
                    );
                }
            }
        }

        if (
                guardianAvailable
                        && !StringUtils.hasText(
                        request.guardianRelationship()
                )
        ) {
            throw new BadRequestException(
                    "Guardian relationship is required when guardian information is entered."
            );
        }

        validateEmergencyContact(
                request
        );
    }

    private void validateEmergencyContact(
            StudentParentRequest request
    ) {
        boolean hasName =
                StringUtils.hasText(
                        request.emergencyContactName()
                );

        boolean hasPhone =
                StringUtils.hasText(
                        request.emergencyContactPhone()
                );

        boolean hasRelationship =
                StringUtils.hasText(
                        request.emergencyContactRelationship()
                );

        boolean anyEntered =
                hasName || hasPhone || hasRelationship;

        boolean allEntered =
                hasName && hasPhone && hasRelationship;

        if (anyEntered && !allEntered) {
            throw new BadRequestException(
                    "Emergency-contact name, phone and relationship must be entered together."
            );
        }
    }

    // =====================================================================
    // ENROLLMENT VALIDATION
    // =====================================================================

    private void validateEnrollmentForCreate(
            StudentEnrollmentRequest request,
            Integer branchId
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Student enrollment information is required."
            );
        }

        validateAcademicPlacement(
                branchId,
                request.academicYearId(),
                request.classId(),
                request.sectionId()
        );

        if (request.admissionType() == null) {
            throw new BadRequestException(
                    "Student admission type is required."
            );
        }

        if (request.joiningDate() == null) {
            throw new BadRequestException(
                    "Student joining date is required."
            );
        }

        if (request.joiningDate().isAfter(LocalDate.now())) {
            throw new BadRequestException(
                    "Student joining date cannot be in the future."
            );
        }

        validateRollNumberUniqueness(
                branchId,
                request.academicYearId(),
                request.classId(),
                request.sectionId(),
                request.rollNo(),
                null
        );
    }

    private void validateAcademicPlacement(
            Integer branchId,
            Long academicYearId,
            Integer classId,
            Long sectionId
    ) {
        if (academicYearId == null || academicYearId <= 0) {
            throw new BadRequestException(
                    "A valid academic year is required."
            );
        }

        if (classId == null || classId <= 0) {
            throw new BadRequestException(
                    "A valid class is required."
            );
        }

        if (!existsActiveAcademicYear(academicYearId)) {
            throw new ResourceNotFoundException(
                    "Selected academic year was not found or is inactive."
            );
        }

        if (!existsActiveClass(classId)) {
            throw new ResourceNotFoundException(
                    "Selected class was not found or is inactive."
            );
        }

        if (
                sectionId != null
                        && !existsMatchingSection(
                        sectionId,
                        branchId,
                        academicYearId,
                        classId
                )
        ) {
            throw new ResourceNotFoundException(
                    "Selected section does not belong to the selected branch, academic year and class."
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
                                """
                        )
                        .setParameter(
                                "academicYearId",
                                academicYearId
                        )
                        .getSingleResult();

        return count.longValue() > 0;
    }

    private boolean existsActiveClass(
            Integer classId
    ) {
        Number count =
                (Number) entityManager
                        .createNativeQuery(
                                """
                                select count(*)
                                from erp_classes
                                where class_id = :classId
                                  and status = 1
                                """
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

    private void validateRollNumberUniqueness(
            Integer branchId,
            Long academicYearId,
            Integer classId,
            Long sectionId,
            String rollNo,
            Long excludedEnrollmentId
    ) {
        if (!StringUtils.hasText(rollNo)) {
            return;
        }

        String sql =
                buildRollNumberUniquenessSql(
                        sectionId,
                        excludedEnrollmentId
                );

        var query =
                entityManager.createNativeQuery(sql);

        query.setParameter(
                "branchId",
                branchId
        );
        query.setParameter(
                "academicYearId",
                academicYearId
        );
        query.setParameter(
                "classId",
                classId
        );
        query.setParameter(
                "rollNo",
                rollNo.trim()
        );

        if (sectionId != null) {
            query.setParameter(
                    "sectionId",
                    sectionId
            );
        }

        if (excludedEnrollmentId != null) {
            query.setParameter(
                    "excludedEnrollmentId",
                    excludedEnrollmentId
            );
        }

        Number count =
                (Number) query.getSingleResult();

        if (count.longValue() > 0) {
            throw new DuplicateResourceException(
                    "Another Student already uses roll number "
                            + rollNo.trim()
                            + " in the selected class and section."
            );
        }
    }

    private String buildRollNumberUniquenessSql(
            Long sectionId,
            Long excludedEnrollmentId
    ) {
        StringBuilder sql =
                new StringBuilder(
                        """
                        select count(*)
                        from erp_student_enrollment
                        where branch_id = :branchId
                          and academic_year_id = :academicYearId
                          and class_id = :classId
                          and upper(trim(roll_no)) = upper(trim(:rollNo))
                        """
                );

        sql.append(
                sectionId == null
                        ? " and section_id is null"
                        : " and section_id = :sectionId"
        );

        if (excludedEnrollmentId != null) {
            sql.append(
                    " and enrollment_id <> :excludedEnrollmentId"
            );
        }

        return sql.toString();
    }

    private void validateEnrollmentDates(
            ErpStudentEnrollment enrollment,
            StudentEnrollmentUpdateRequest request
    ) {
        if (
                request.effectiveDate() != null
                        && request.effectiveDate().isBefore(
                        enrollment.getJoiningDate()
                )
        ) {
            throw new BadRequestException(
                    "Enrollment effective date cannot be earlier than the joining date."
            );
        }

        if (
                request.leavingDate() != null
                        && request.leavingDate().isBefore(
                        enrollment.getJoiningDate()
                )
        ) {
            throw new BadRequestException(
                    "Student leaving date cannot be earlier than the joining date."
            );
        }

        if (
                request.leavingDate() != null
                        && request.effectiveDate() != null
                        && request.leavingDate().isBefore(
                        request.effectiveDate()
                )
        ) {
            throw new BadRequestException(
                    "Student leaving date cannot be earlier than the enrollment effective date."
            );
        }
    }

    // =====================================================================
    // OPTIONAL HOSTEL AND TRANSPORT VALIDATION
    // =====================================================================

    private void validateHostelForCreate(
            StudentHostelRequest request,
            StudentEnrollmentRequest enrollment,
            AcademicYearWindow academicYear
    ) {
        if (request == null) {
            return;
        }

        if (request.hostelId() == null || request.hostelId() <= 0) {
            throw new BadRequestException(
                    "A valid Hostel ID is required."
            );
        }

        if (request.bedId() != null && request.roomId() == null) {
            throw new BadRequestException(
                    "Room is required when a Hostel bed is selected."
            );
        }

        if (request.allocationStartDate() == null) {
            throw new BadRequestException(
                    "Hostel allocation start date is required."
            );
        }

        validateAllocationDateRange(
                "Hostel allocation",
                request.allocationStartDate(),
                request.allocationEndDate(),
                enrollment.joiningDate(),
                academicYear
        );

        validateLocalGuardian(
                request.localGuardianName(),
                request.localGuardianMobile(),
                request.localGuardianRelation()
        );
    }

    private void validateTransportForCreate(
            StudentTransportRequest request,
            StudentEnrollmentRequest enrollment,
            AcademicYearWindow academicYear
    ) {
        if (request == null) {
            return;
        }

        if (request.routeId() == null || request.routeId() <= 0) {
            throw new BadRequestException(
                    "A valid transport route ID is required."
            );
        }

        if (request.transportStartDate() == null) {
            throw new BadRequestException(
                    "Transport start date is required."
            );
        }

        validateAllocationDateRange(
                "Transport allocation",
                request.transportStartDate(),
                request.transportEndDate(),
                enrollment.joiningDate(),
                academicYear
        );

        boolean hasEmergencyContact =
                StringUtils.hasText(
                        request.emergencyContact()
                );

        boolean hasEmergencyMobile =
                StringUtils.hasText(
                        request.emergencyMobile()
                );

        if (hasEmergencyContact != hasEmergencyMobile) {
            throw new BadRequestException(
                    "Transport emergency contact and mobile must be entered together."
            );
        }
    }

    private void validateAllocationDateRange(
            String allocationName,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate joiningDate,
            AcademicYearWindow academicYear
    ) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException(
                    allocationName
                            + " end date cannot be earlier than its start date."
            );
        }

        if (joiningDate != null && startDate.isBefore(joiningDate)) {
            throw new BadRequestException(
                    allocationName
                            + " start date cannot be earlier than the Student joining date."
            );
        }

        if (
                startDate.isBefore(academicYear.startDate())
                        || startDate.isAfter(academicYear.endDate())
        ) {
            throw new BadRequestException(
                    allocationName
                            + " start date must fall within the selected academic year."
            );
        }

        if (
                endDate != null
                        && (
                        endDate.isBefore(academicYear.startDate())
                                || endDate.isAfter(academicYear.endDate())
                )
        ) {
            throw new BadRequestException(
                    allocationName
                            + " end date must fall within the selected academic year."
            );
        }
    }

    private void validateLocalGuardian(
            String name,
            String mobile,
            String relationship
    ) {
        boolean hasName =
                StringUtils.hasText(name);

        boolean hasMobile =
                StringUtils.hasText(mobile);

        boolean hasRelationship =
                StringUtils.hasText(relationship);

        boolean noneEntered =
                !hasName
                        && !hasMobile
                        && !hasRelationship;

        boolean allEntered =
                hasName
                        && hasMobile
                        && hasRelationship;

        if (!noneEntered && !allEntered) {
            throw new BadRequestException(
                    "Local guardian name, mobile and relationship must be entered together."
            );
        }
    }

    private AcademicYearWindow requireAcademicYearWindow(
            Long academicYearId
    ) {
        if (academicYearId == null || academicYearId <= 0) {
            throw new BadRequestException(
                    "A valid academic year is required."
            );
        }

        @SuppressWarnings("unchecked")
        java.util.List<Object[]> rows =
                entityManager
                        .createNativeQuery(
                                """
                                select academic_year_code,
                                       start_date,
                                       end_date
                                from erp_academic_years
                                where academic_year_id = :academicYearId
                                  and active = 1
                                """
                        )
                        .setParameter(
                                "academicYearId",
                                academicYearId
                        )
                        .setMaxResults(1)
                        .getResultList();

        if (rows.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Selected academic year was not found or is inactive."
            );
        }

        Object[] row =
                rows.getFirst();

        String academicYearCode =
                Objects.toString(
                        row[0],
                        ""
                ).trim();

        LocalDate startDate =
                toLocalDate(
                        row[1]
                );

        LocalDate endDate =
                toLocalDate(
                        row[2]
                );

        if (!StringUtils.hasText(academicYearCode)) {
            throw new IllegalStateException(
                    "Selected academic year does not contain a valid code."
            );
        }

        if (startDate == null || endDate == null) {
            throw new IllegalStateException(
                    "Selected academic year does not contain a valid date range."
            );
        }

        return new AcademicYearWindow(
                academicYearCode,
                startDate,
                endDate
        );
    }

    private LocalDate toLocalDate(
            Object value
    ) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }

        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }

        return null;
    }

    // =====================================================================
    // APPLICATION AND IDENTIFIER VALIDATION
    // =====================================================================

    private void validateApplicationReference(
            Long applicationId,
            Integer branchId
    ) {
        if (applicationId == null) {
            return;
        }

        if (applicationId <= 0) {
            throw new BadRequestException(
                    "Application ID must be greater than zero."
            );
        }

        Number applicationCount =
                (Number) entityManager
                        .createNativeQuery(
                                """
                                select count(*)
                                from erp_applications
                                where application_id = :applicationId
                                  and branch_id = :branchId
                                  and status = 1
                                """
                        )
                        .setParameter(
                                "applicationId",
                                applicationId
                        )
                        .setParameter(
                                "branchId",
                                branchId
                        )
                        .getSingleResult();

        if (applicationCount.longValue() == 0) {
            throw new ResourceNotFoundException(
                    "Selected admission application was not found."
            );
        }

        Number studentCount =
                (Number) entityManager
                        .createNativeQuery(
                                """
                                select count(*)
                                from erp_students
                                where application_id = :applicationId
                                """
                        )
                        .setParameter(
                                "applicationId",
                                applicationId
                        )
                        .getSingleResult();

        if (studentCount.longValue() > 0) {
            throw new DuplicateResourceException(
                    "A Student has already been created from this admission application."
            );
        }
    }

    private void validateLearnerLinUniqueness(
            String learnerLin,
            Long excludedStudentId
    ) {
        if (!StringUtils.hasText(learnerLin)) {
            return;
        }

        String sql =
                buildLearnerLinUniquenessSql(
                        excludedStudentId
                );

        var query =
                entityManager.createNativeQuery(sql);

        query.setParameter(
                "learnerLin",
                learnerLin.trim()
        );

        if (excludedStudentId != null) {
            query.setParameter(
                    "excludedStudentId",
                    excludedStudentId
            );
        }

        Number count =
                (Number) query.getSingleResult();

        if (count.longValue() > 0) {
            throw new DuplicateResourceException(
                    "Another Student already uses Learner Identification Number "
                            + learnerLin.trim().toUpperCase(Locale.ROOT)
                            + "."
            );
        }
    }

    private String buildLearnerLinUniquenessSql(
            Long excludedStudentId
    ) {
        StringBuilder sql =
                new StringBuilder(
                        """
                        select count(*)
                        from erp_students
                        where upper(trim(learner_lin)) =
                              upper(trim(:learnerLin))
                        """
                );

        if (excludedStudentId != null) {
            sql.append(
                    " and student_id <> :excludedStudentId"
            );
        }

        return sql.toString();
    }

    // =====================================================================
    // VERSION VALIDATION
    // =====================================================================

    private void validateStudentVersion(
            ErpStudent student,
            Long submittedVersion
    ) {
        if (submittedVersion == null) {
            throw new BadRequestException(
                    "Student version is required."
            );
        }

        if (!Objects.equals(
                student.getVersion(),
                submittedVersion
        )) {
            throw new BadRequestException(
                    "This Student record was changed by another user. Reload the Student and try again."
            );
        }
    }

    private void validateEnrollmentVersion(
            ErpStudentEnrollment enrollment,
            Long submittedVersion
    ) {
        if (submittedVersion == null) {
            throw new BadRequestException(
                    "Student enrollment version is required."
            );
        }

        if (!Objects.equals(
                enrollment.getVersion(),
                submittedVersion
        )) {
            throw new BadRequestException(
                    "This Student enrollment was changed by another user. Reload the Student and try again."
            );
        }
    }

    // =====================================================================
    // STATUS TRANSITIONS
    // =====================================================================

    private void validateStudentStatusTransition(
            StudentStatus currentStatus,
            StudentStatus newStatus
    ) {
        if (newStatus == null) {
            throw new BadRequestException(
                    "New Student status is required."
            );
        }

        if (currentStatus == newStatus) {
            throw new BadRequestException(
                    "Student is already in status "
                            + newStatus.name()
                            + "."
            );
        }

        if (currentStatus == StudentStatus.DECEASED) {
            throw new BadRequestException(
                    "A deceased Student record cannot be changed through a normal status change."
            );
        }

        if (
                currentStatus == StudentStatus.ALUMNI
                        && newStatus == StudentStatus.ACTIVE
        ) {
            throw new BadRequestException(
                    "An alumni record cannot be directly changed to ACTIVE."
            );
        }
    }

    private void validateEnrollmentStatusTransition(
            ErpStudentEnrollment.EnrollmentStatus currentStatus,
            ErpStudentEnrollment.EnrollmentStatus newStatus
    ) {
        if (newStatus == null) {
            throw new BadRequestException(
                    "New enrollment status is required."
            );
        }

        if (currentStatus == newStatus) {
            return;
        }

        if (
                currentStatus
                        == ErpStudentEnrollment.EnrollmentStatus.GRADUATED
                        && newStatus
                        == ErpStudentEnrollment.EnrollmentStatus.ACTIVE
        ) {
            throw new BadRequestException(
                    "A graduated enrollment cannot be directly changed to ACTIVE."
            );
        }

        if (
                currentStatus
                        == ErpStudentEnrollment.EnrollmentStatus.EXPELLED
                        && newStatus
                        == ErpStudentEnrollment.EnrollmentStatus.ACTIVE
        ) {
            throw new BadRequestException(
                    "An expelled enrollment cannot be directly changed to ACTIVE."
            );
        }
    }

    private StudentStatus parseStudentStatus(
            String status
    ) {
        if (!StringUtils.hasText(status)) {
            throw new BadRequestException(
                    "Student currently has no valid status."
            );
        }

        try {
            return StudentStatus.valueOf(
                    status.trim()
                            .toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException(
                    "Student currently has an unsupported status: "
                            + status
                            + "."
            );
        }
    }

    // =====================================================================
    // HELPERS
    // =====================================================================

    private boolean hasNameAndPhone(
            String name,
            String phone
    ) {
        return StringUtils.hasText(name)
                && StringUtils.hasText(phone);
    }

    // =====================================================================
    // VALIDATED CONTEXT RECORDS
    // =====================================================================

    public record BranchContext(
            Branch branch,
            Integer userId,
            String username
    ) {
    }

    private record AcademicYearWindow(
            String academicYearCode,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }

    public record CreateReferences(
            BranchContext branchContext,
            String academicYearCode
    ) {
    }

    public record UpdateReferences(
            BranchContext branchContext,
            ErpStudent student
    ) {
    }

    public record StatusChangeReferences(
            BranchContext branchContext,
            ErpStudent student,
            StudentStatus currentStatus
    ) {
    }

    public record EnrollmentUpdateReferences(
            BranchContext branchContext,
            ErpStudent student,
            ErpStudentEnrollment enrollment
    ) {
    }
}