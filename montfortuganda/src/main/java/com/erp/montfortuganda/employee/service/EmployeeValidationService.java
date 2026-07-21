package com.erp.montfortuganda.employee.service;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.employee.dto.request.EmployeeContactRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeDocumentRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeExperienceRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeQualificationRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeRegistrationRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeUpdateRequest;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import com.erp.montfortuganda.employee.enums.EmployeeDocumentType;
import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import com.erp.montfortuganda.employee.repository.ErpEmployeeContactRepository;
import com.erp.montfortuganda.employee.repository.ErpEmployeeDocumentRepository;
import com.erp.montfortuganda.employee.repository.ErpEmployeeExperienceRepository;
import com.erp.montfortuganda.employee.repository.ErpEmployeeQualificationRepository;
import com.erp.montfortuganda.employee.repository.ErpEmployeeRepository;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.BranchNotAssignedException;
import com.erp.montfortuganda.exception.DuplicateResourceException;
import com.erp.montfortuganda.exception.EntityInUseException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.Department;
import com.erp.montfortuganda.school.entity.Designation;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.school.repository.DepartmentRepository;
import com.erp.montfortuganda.school.repository.DesignationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Performs Employee business validation and resolves all branch-owned
 * references before persistence.
  * <p>
 * Bean Validation annotations protect the HTTP contract. This service adds
 * database-aware and cross-record rules such as:
  * <p>
 * - authenticated branch ownership;
 * - active branch, department, designation and reporting manager;
 * - Employee identifier uniqueness;
 * - reporting hierarchy safety;
 * - nested-record ownership during updates;
 * - duplicate records inside nested request collections;
 * - collection size and primary/emergency contact rules.
 */
@SuppressWarnings("unused")
@Service
@Transactional(readOnly = true)
public class EmployeeValidationService {

    private static final int MAX_CONTACTS = 10;
    private static final int MAX_QUALIFICATIONS = 25;
    private static final int MAX_EXPERIENCES = 25;
    private static final int MAX_DOCUMENTS = 50;
    private static final int MAX_REPORTING_HIERARCHY_DEPTH = 100;

    private final CurrentUserService currentUserService;
    private final BranchRepository branchRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final ErpEmployeeRepository employeeRepository;
    private final ErpEmployeeContactRepository contactRepository;
    private final ErpEmployeeQualificationRepository qualificationRepository;
    private final ErpEmployeeExperienceRepository experienceRepository;
    private final ErpEmployeeDocumentRepository documentRepository;

    public EmployeeValidationService(
            CurrentUserService currentUserService,
            BranchRepository branchRepository,
            DepartmentRepository departmentRepository,
            DesignationRepository designationRepository,
            ErpEmployeeRepository employeeRepository,
            ErpEmployeeContactRepository contactRepository,
            ErpEmployeeQualificationRepository qualificationRepository,
            ErpEmployeeExperienceRepository experienceRepository,
            ErpEmployeeDocumentRepository documentRepository
    ) {
        this.currentUserService = currentUserService;
        this.branchRepository = branchRepository;
        this.departmentRepository = departmentRepository;
        this.designationRepository = designationRepository;
        this.employeeRepository = employeeRepository;
        this.contactRepository = contactRepository;
        this.qualificationRepository = qualificationRepository;
        this.experienceRepository = experienceRepository;
        this.documentRepository = documentRepository;
    }

    /**
     * Validates a new Employee request and returns all resolved references
     * needed by the registration service.
     */
    public RegistrationReferences validateForRegistration(
            EmployeeRegistrationRequest request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Employee registration information is required."
            );
        }

        BranchContext branchContext =
                requireAuthenticatedBranch();

        Branch branch =
                branchContext.branch();

        Department department =
                requireActiveDepartment(
                        request.departmentId(),
                        branch.getBranchId()
                );

        Designation designation =
                requireActiveDesignation(
                        request.designationId()
                );

        ErpEmployee reportingManager =
                requireOptionalActiveReportingManager(
                        request.reportingManagerId(),
                        branch.getBranchId()
                );

        validateMinimumJoiningAge(
                request.dateOfBirth(),
                request.joiningDate()
        );

        validateRegistrationStatus(
                request.employmentStatus()
        );

        validateEmployeeIdentifierUniquenessForCreate(
                branch.getBranchId(),
                request
        );

        validateNestedCollections(
                request.contacts(),
                request.qualifications(),
                request.experiences(),
                request.documents()
        );

        return new RegistrationReferences(
                branchContext,
                department,
                designation,
                reportingManager
        );
    }

    /**
     * Validates an Employee update and returns the existing branch-owned
     * Employee together with all resolved references.
     */
    public UpdateReferences validateForUpdate(
            Long employeeId,
            EmployeeUpdateRequest request
    ) {
        if (employeeId == null || employeeId <= 0) {
            throw new BadRequestException(
                    "A valid Employee ID is required."
            );
        }

        if (request == null) {
            throw new BadRequestException(
                    "Employee update information is required."
            );
        }

        BranchContext branchContext =
                requireAuthenticatedBranch();

        Integer branchId =
                branchContext.branch().getBranchId();

        ErpEmployee employee =
                requireEmployee(
                        employeeId,
                        branchId
                );

        validateVersion(
                employee,
                request.version()
        );

        Department department =
                requireActiveDepartment(
                        request.departmentId(),
                        branchId
                );

        Designation designation =
                requireActiveDesignation(
                        request.designationId()
                );

        ErpEmployee reportingManager =
                requireOptionalActiveReportingManager(
                        request.reportingManagerId(),
                        branchId
                );

        validateReportingManagerForUpdate(
                employee,
                reportingManager
        );

        validateMinimumJoiningAge(
                request.dateOfBirth(),
                request.joiningDate()
        );

        validateEmployeeIdentifierUniquenessForUpdate(
                branchId,
                employeeId,
                request
        );

        validateNestedCollections(
                request.contacts(),
                request.qualifications(),
                request.experiences(),
                request.documents()
        );

        validateNestedRecordOwnership(
                employeeId,
                branchId,
                request
        );

        return new UpdateReferences(
                branchContext,
                employee,
                department,
                designation,
                reportingManager
        );
    }

    /**
     * Resolves one Employee using both Employee ID and authenticated branch.
     */
    public ErpEmployee requireBranchEmployee(
            Long employeeId
    ) {
        BranchContext branchContext =
                requireAuthenticatedBranch();

        return requireEmployee(
                employeeId,
                branchContext.branch().getBranchId()
        );
    }

    /**
     * Validates that an Employee can be deactivated or removed.
     */
    public ErpEmployee validateForDeactivation(
            Long employeeId
    ) {
        BranchContext branchContext =
                requireAuthenticatedBranch();

        Integer branchId =
                branchContext.branch().getBranchId();

        ErpEmployee employee =
                requireEmployee(
                        employeeId,
                        branchId
                );

        if (
                employeeRepository
                        .existsByBranch_BranchIdAndReportingManager_EmployeeId(
                                branchId,
                                employeeId
                        )
        ) {
            throw new EntityInUseException(
                    "This Employee is assigned as a reporting manager. "
                            + "Reassign the direct reports before deactivation."
            );
        }

        return employee;
    }

    /**
     * Returns the current authenticated branch and username.
     */
    public BranchContext requireAuthenticatedBranch() {
        CurrentUserContext currentUser =
                currentUserService
                        .getCurrentUserContext();

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

    /**
     * Branch-safe lookup used by file and nested-record services.
     */
    public ErpEmployee requireEmployee(
            Long employeeId,
            Integer branchId
    ) {
        if (employeeId == null || employeeId <= 0) {
            throw new BadRequestException(
                    "A valid Employee ID is required."
            );
        }

        return employeeRepository
                .findByEmployeeIdAndBranch_BranchId(
                        employeeId,
                        branchId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee was not found."
                        )
                );
    }

    private Department requireActiveDepartment(
            Long departmentId,
            Integer branchId
    ) {
        if (departmentId == null || departmentId <= 0) {
            throw new BadRequestException(
                    "A valid department is required."
            );
        }

        Department department =
                departmentRepository
                        .findById(departmentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Selected department was not found."
                                )
                        );

        Integer departmentBranchId =
                department.getBranch() == null
                        ? null
                        : department.getBranch().getBranchId();

        if (!Objects.equals(branchId, departmentBranchId)) {
            /*
             * Use a not-found response rather than revealing that another
             * branch owns this department.
             */
            throw new ResourceNotFoundException(
                    "Selected department was not found."
            );
        }

        if (
                !Boolean.TRUE.equals(department.getActive())
                        || department.getStatus() != RecordStatus.ACTIVE
        ) {
            throw new BadRequestException(
                    "The selected department is inactive."
            );
        }

        return department;
    }

    private Designation requireActiveDesignation(
            Long designationId
    ) {
        if (designationId == null || designationId <= 0) {
            throw new BadRequestException(
                    "A valid designation is required."
            );
        }

        Designation designation =
                designationRepository
                        .findById(designationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Selected designation was not found."
                                )
                        );

        if (
                !Boolean.TRUE.equals(designation.getActive())
                        || !"ACTIVE".equalsIgnoreCase(
                        designation.getStatus()
                )
        ) {
            throw new BadRequestException(
                    "The selected designation is inactive."
            );
        }

        return designation;
    }

    private ErpEmployee requireOptionalActiveReportingManager(
            Long reportingManagerId,
            Integer branchId
    ) {
        if (reportingManagerId == null) {
            return null;
        }

        if (reportingManagerId <= 0) {
            throw new BadRequestException(
                    "Reporting-manager Employee ID must be greater than zero."
            );
        }

        ErpEmployee reportingManager =
                employeeRepository
                        .findByEmployeeIdAndBranch_BranchIdAndActiveTrue(
                                reportingManagerId,
                                branchId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Selected reporting manager was not found."
                                )
                        );

        if (isFinalEmploymentStatus(
                reportingManager.getEmploymentStatus()
        )) {
            throw new BadRequestException(
                    "A resigned, retired or terminated Employee cannot be "
                            + "selected as reporting manager."
            );
        }

        return reportingManager;
    }

    private void validateVersion(
            ErpEmployee employee,
            Long submittedVersion
    ) {
        if (submittedVersion == null) {
            throw new BadRequestException(
                    "Employee version is required for update."
            );
        }

        if (!Objects.equals(
                employee.getVersion(),
                submittedVersion
        )) {
            throw new BadRequestException(
                    "This Employee record was changed by another user. "
                            + "Reload the Employee and try again."
            );
        }
    }

    private void validateReportingManagerForUpdate(
            ErpEmployee employee,
            ErpEmployee reportingManager
    ) {
        if (reportingManager == null) {
            return;
        }

        if (Objects.equals(
                employee.getEmployeeId(),
                reportingManager.getEmployeeId()
        )) {
            throw new BadRequestException(
                    "An Employee cannot be their own reporting manager."
            );
        }

        validateNoReportingCycle(
                employee.getEmployeeId(),
                reportingManager
        );
    }

    private void validateNoReportingCycle(
            Long employeeId,
            ErpEmployee selectedManager
    ) {
        Set<Long> visitedEmployeeIds =
                new HashSet<>();

        ErpEmployee current =
                selectedManager;

        int depth = 0;

        while (current != null) {
            Long currentEmployeeId =
                    current.getEmployeeId();

            if (Objects.equals(
                    employeeId,
                    currentEmployeeId
            )) {
                throw new BadRequestException(
                        "The selected reporting manager would create a "
                                + "circular reporting hierarchy."
                );
            }

            if (
                    currentEmployeeId != null
                            && !visitedEmployeeIds.add(
                            currentEmployeeId
                    )
            ) {
                throw new BadRequestException(
                        "The existing reporting hierarchy contains a cycle."
                );
            }

            depth++;

            if (depth > MAX_REPORTING_HIERARCHY_DEPTH) {
                throw new BadRequestException(
                        "The reporting hierarchy exceeds the supported depth."
                );
            }

            current =
                    current.getReportingManager();
        }
    }

    private void validateMinimumJoiningAge(
            LocalDate dateOfBirth,
            LocalDate joiningDate
    ) {
        if (dateOfBirth == null || joiningDate == null) {
            return;
        }

        if (dateOfBirth.plusYears(18).isAfter(joiningDate)) {
            throw new BadRequestException(
                    "Employee must be at least 18 years old on the joining date."
            );
        }
    }

    private void validateRegistrationStatus(
            EmploymentStatus employmentStatus
    ) {
        if (
                employmentStatus != EmploymentStatus.ACTIVE
                        && employmentStatus != EmploymentStatus.PROBATION
        ) {
            throw new BadRequestException(
                    "A new Employee can be registered only with ACTIVE or "
                            + "PROBATION employment status."
            );
        }
    }

    private void validateEmployeeIdentifierUniquenessForCreate(
            Integer branchId,
            EmployeeRegistrationRequest request
    ) {
        validateOfficialEmailForCreate(
                branchId,
                request.officialEmail()
        );

        validateNationalIdForCreate(
                branchId,
                request.nationalId()
        );

        validatePassportForCreate(
                branchId,
                request.passportNo()
        );

        validateTinForCreate(
                branchId,
                request.tinNumber()
        );

        validateWorkPermitForCreate(
                branchId,
                request.workPermitNumber()
        );
    }

    private void validateEmployeeIdentifierUniquenessForUpdate(
            Integer branchId,
            Long employeeId,
            EmployeeUpdateRequest request
    ) {
        if (
                hasText(request.officialEmail())
                        && employeeRepository
                        .existsByBranch_BranchIdAndOfficialEmailIgnoreCaseAndEmployeeIdNot(
                                branchId,
                                request.officialEmail().trim(),
                                employeeId
                        )
        ) {
            throw duplicate(
                    "official email",
                    request.officialEmail()
            );
        }

        if (
                hasText(request.nationalId())
                        && employeeRepository
                        .existsByBranch_BranchIdAndNationalIdIgnoreCaseAndEmployeeIdNot(
                                branchId,
                                request.nationalId().trim(),
                                employeeId
                        )
        ) {
            throw duplicate(
                    "National ID",
                    request.nationalId()
            );
        }

        if (
                hasText(request.passportNo())
                        && employeeRepository
                        .existsByBranch_BranchIdAndPassportNoIgnoreCaseAndEmployeeIdNot(
                                branchId,
                                request.passportNo().trim(),
                                employeeId
                        )
        ) {
            throw duplicate(
                    "passport number",
                    request.passportNo()
            );
        }

        if (
                hasText(request.tinNumber())
                        && employeeRepository
                        .existsByBranch_BranchIdAndTinNumberIgnoreCaseAndEmployeeIdNot(
                                branchId,
                                request.tinNumber().trim(),
                                employeeId
                        )
        ) {
            throw duplicate(
                    "TIN number",
                    request.tinNumber()
            );
        }

        if (
                hasText(request.workPermitNumber())
                        && employeeRepository
                        .existsByBranch_BranchIdAndWorkPermitNumberIgnoreCaseAndEmployeeIdNot(
                                branchId,
                                request.workPermitNumber().trim(),
                                employeeId
                        )
        ) {
            throw duplicate(
                    "work-permit number",
                    request.workPermitNumber()
            );
        }
    }

    private void validateOfficialEmailForCreate(
            Integer branchId,
            String officialEmail
    ) {
        if (
                hasText(officialEmail)
                        && employeeRepository
                        .existsByBranch_BranchIdAndOfficialEmailIgnoreCase(
                                branchId,
                                officialEmail.trim()
                        )
        ) {
            throw duplicate(
                    "official email",
                    officialEmail
            );
        }
    }

    private void validateNationalIdForCreate(
            Integer branchId,
            String nationalId
    ) {
        if (
                hasText(nationalId)
                        && employeeRepository
                        .existsByBranch_BranchIdAndNationalIdIgnoreCase(
                                branchId,
                                nationalId.trim()
                        )
        ) {
            throw duplicate(
                    "National ID",
                    nationalId
            );
        }
    }

    private void validatePassportForCreate(
            Integer branchId,
            String passportNo
    ) {
        if (
                hasText(passportNo)
                        && employeeRepository
                        .existsByBranch_BranchIdAndPassportNoIgnoreCase(
                                branchId,
                                passportNo.trim()
                        )
        ) {
            throw duplicate(
                    "passport number",
                    passportNo
            );
        }
    }

    private void validateTinForCreate(
            Integer branchId,
            String tinNumber
    ) {
        if (
                hasText(tinNumber)
                        && employeeRepository
                        .existsByBranch_BranchIdAndTinNumberIgnoreCase(
                                branchId,
                                tinNumber.trim()
                        )
        ) {
            throw duplicate(
                    "TIN number",
                    tinNumber
            );
        }
    }

    private void validateWorkPermitForCreate(
            Integer branchId,
            String workPermitNumber
    ) {
        if (
                hasText(workPermitNumber)
                        && employeeRepository
                        .existsByBranch_BranchIdAndWorkPermitNumberIgnoreCase(
                                branchId,
                                workPermitNumber.trim()
                        )
        ) {
            throw duplicate(
                    "work-permit number",
                    workPermitNumber
            );
        }
    }

    private DuplicateResourceException duplicate(
            String fieldName,
            String value
    ) {
        return new DuplicateResourceException(
                "Another Employee already uses "
                        + fieldName
                        + " "
                        + value.trim()
                        + "."
        );
    }

    private void validateNestedCollections(
            List<EmployeeContactRequest> contacts,
            List<EmployeeQualificationRequest> qualifications,
            List<EmployeeExperienceRequest> experiences,
            List<EmployeeDocumentRequest> documents
    ) {
        validateCollectionSize(
                contacts,
                MAX_CONTACTS,
                "contacts"
        );

        validateCollectionSize(
                qualifications,
                MAX_QUALIFICATIONS,
                "qualifications"
        );

        validateCollectionSize(
                experiences,
                MAX_EXPERIENCES,
                "experience records"
        );

        validateCollectionSize(
                documents,
                MAX_DOCUMENTS,
                "documents"
        );

        validateContacts(contacts);
        validateQualifications(qualifications);
        validateExperiences(experiences);
        validateDocuments(documents);
    }

    private void validateCollectionSize(
            List<?> values,
            int maximumSize,
            String label
    ) {
        if (values == null) {
            throw new BadRequestException(
                    "Employee " + label + " collection is required."
            );
        }

        if (values.size() > maximumSize) {
            throw new BadRequestException(
                    "An Employee cannot have more than "
                            + maximumSize
                            + " "
                            + label
                            + "."
            );
        }

        if (values.stream().anyMatch(Objects::isNull)) {
            throw new BadRequestException(
                    "Employee " + label + " cannot contain an empty record."
            );
        }
    }

    private void validateContacts(
            List<EmployeeContactRequest> contacts
    ) {
        if (contacts.isEmpty()) {
            throw new BadRequestException(
                    "At least one Employee contact is required."
            );
        }

        long activePrimaryContacts =
                contacts.stream()
                        .filter(this::isActiveContact)
                        .filter(contact ->
                                Boolean.TRUE.equals(
                                        contact.employeeContactIsPrimary()
                                )
                        )
                        .count();

        if (activePrimaryContacts != 1) {
            throw new BadRequestException(
                    "Exactly one active primary Employee contact is required."
            );
        }

        boolean activeEmergencyContactExists =
                contacts.stream()
                        .filter(this::isActiveContact)
                        .anyMatch(contact ->
                                Boolean.TRUE.equals(
                                        contact.employeeContactIsEmergency()
                                )
                        );

        if (!activeEmergencyContactExists) {
            throw new BadRequestException(
                    "At least one active emergency contact is required."
            );
        }

        Set<String> mobileKeys =
                new HashSet<>();

        for (EmployeeContactRequest contact : contacts) {
            if (!isActiveContact(contact)) {
                continue;
            }

            String mobileKey =
                    normalizePhoneKey(
                            contact.employeeContactMobile()
                    );

            if (
                    hasText(mobileKey)
                            && !mobileKeys.add(mobileKey)
            ) {
                throw new DuplicateResourceException(
                        "Duplicate active Employee contact mobile number: "
                                + contact.employeeContactMobile().trim()
                                + "."
                );
            }
        }
    }

    private boolean isActiveContact(
            EmployeeContactRequest contact
    ) {
        return Boolean.TRUE.equals(
                contact.employeeContactActive()
        );
    }

    private void validateQualifications(
            List<EmployeeQualificationRequest> qualifications
    ) {
        Set<String> qualificationKeys =
                new HashSet<>();
        Set<String> certificateNumbers =
                new HashSet<>();
        Set<String> registrationNumbers =
                new HashSet<>();

        for (
                EmployeeQualificationRequest qualification
                : qualifications
        ) {
            if (
                    !Boolean.TRUE.equals(
                            qualification.employeeQualificationActive()
                    )
            ) {
                continue;
            }

            String qualificationKey =
                    normalizeKey(
                            qualification.employeeQualificationLevel(),
                            qualification.employeeQualificationName(),
                            qualification.employeeQualificationInstitutionName(),
                            qualification.employeeQualificationCompletionYear()
                    );

            if (!qualificationKeys.add(qualificationKey)) {
                throw new DuplicateResourceException(
                        "Duplicate active Employee qualification: "
                                + qualification.employeeQualificationName()
                                + "."
                );
            }

            validateUniqueOptionalValue(
                    certificateNumbers,
                    qualification.employeeQualificationCertificateNumber(),
                    "qualification certificate number"
            );

            validateUniqueOptionalValue(
                    registrationNumbers,
                    qualification.employeeQualificationRegistrationNumber(),
                    "qualification registration number"
            );
        }
    }

    private void validateExperiences(
            List<EmployeeExperienceRequest> experiences
    ) {
        Set<String> experienceKeys =
                new HashSet<>();

        for (EmployeeExperienceRequest experience : experiences) {
            boolean active =
                    !Boolean.FALSE.equals(
                            experience.employeeExperienceActive()
                    );

            if (!active) {
                continue;
            }

            String experienceKey =
                    normalizeKey(
                            experience.employeeExperienceCompanyName(),
                            experience.employeeExperienceDesignation(),
                            experience.employeeExperienceStartDate()
                    );

            if (!experienceKeys.add(experienceKey)) {
                throw new DuplicateResourceException(
                        "Duplicate active Employee experience record for "
                                + experience.employeeExperienceCompanyName()
                                + "."
                );
            }
        }
    }

    private void validateDocuments(
            List<EmployeeDocumentRequest> documents
    ) {
        Set<String> documentKeys =
                new HashSet<>();

        for (EmployeeDocumentRequest document : documents) {
            boolean active =
                    !Boolean.FALSE.equals(
                            document.employeeDocumentActive()
                    );

            if (!active) {
                continue;
            }

            String displayName =
                    document.employeeDocumentType()
                                    == EmployeeDocumentType.OTHER
                            ? document.employeeDocumentName()
                            : document.employeeDocumentType().name();

            String documentKey =
                    normalizeKey(
                            document.employeeDocumentType(),
                            displayName
                    );

            if (!documentKeys.add(documentKey)) {
                throw new DuplicateResourceException(
                        "Duplicate active Employee document: "
                                + toDocumentLabel(
                                document.employeeDocumentType(),
                                displayName
                        )
                                + "."
                );
            }
        }
    }

    private void validateNestedRecordOwnership(
            Long employeeId,
            Integer branchId,
            EmployeeUpdateRequest request
    ) {
        for (EmployeeContactRequest contact : request.contacts()) {
            Long contactId =
                    contact.employeeContactId();

            if (
                    contactId != null
                            && contactRepository
                            .findByEmployeeContactIdAndEmployee_EmployeeIdAndEmployee_Branch_BranchId(
                                    contactId,
                                    employeeId,
                                    branchId
                            )
                            .isEmpty()
            ) {
                throw new ResourceNotFoundException(
                        "Employee contact was not found."
                );
            }
        }

        for (
                EmployeeQualificationRequest qualification
                : request.qualifications()
        ) {
            Long qualificationId =
                    qualification.employeeQualificationId();

            if (
                    qualificationId != null
                            && qualificationRepository
                            .findByEmployeeQualificationIdAndEmployee_EmployeeIdAndEmployee_Branch_BranchId(
                                    qualificationId,
                                    employeeId,
                                    branchId
                            )
                            .isEmpty()
            ) {
                throw new ResourceNotFoundException(
                        "Employee qualification was not found."
                );
            }
        }

        for (EmployeeExperienceRequest experience : request.experiences()) {
            Long experienceId =
                    experience.employeeExperienceId();

            if (
                    experienceId != null
                            && experienceRepository
                            .findByEmployeeExperienceIdAndEmployee_EmployeeIdAndEmployee_Branch_BranchId(
                                    experienceId,
                                    employeeId,
                                    branchId
                            )
                            .isEmpty()
            ) {
                throw new ResourceNotFoundException(
                        "Employee experience record was not found."
                );
            }
        }

        for (EmployeeDocumentRequest document : request.documents()) {
            Long documentId =
                    document.employeeDocumentId();

            if (
                    documentId != null
                            && documentRepository
                            .findByEmployeeDocumentIdAndEmployee_EmployeeIdAndEmployee_Branch_BranchId(
                                    documentId,
                                    employeeId,
                                    branchId
                            )
                            .isEmpty()
            ) {
                throw new ResourceNotFoundException(
                        "Employee document was not found."
                );
            }
        }
    }

    private void validateUniqueOptionalValue(
            Set<String> existingValues,
            String value,
            String label
    ) {
        if (!hasText(value)) {
            return;
        }

        String normalized =
                value.trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (!existingValues.add(normalized)) {
            throw new DuplicateResourceException(
                    "Duplicate " + label + ": " + value.trim() + "."
            );
        }
    }

    private String normalizePhoneKey(
            String phone
    ) {
        if (!hasText(phone)) {
            return null;
        }

        return phone.trim()
                .replaceAll(
                        "[\\s()\\-]",
                        ""
                )
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private String normalizeKey(
            Object... values
    ) {
        StringBuilder key =
                new StringBuilder();

        for (Object value : values) {
            if (!key.isEmpty()) {
                key.append('|');
            }

            if (value == null) {
                continue;
            }

            String normalized =
                    value.toString()
                            .trim()
                            .toLowerCase(
                                    Locale.ROOT
                            );

            key.append(normalized);
        }

        return key.toString();
    }

    private String toDocumentLabel(
            EmployeeDocumentType documentType,
            String displayName
    ) {
        if (documentType == EmployeeDocumentType.OTHER) {
            return StringUtils.hasText(displayName)
                    ? displayName.trim()
                    : "Other";
        }

        return documentType.name()
                .replace('_', ' ')
                .toLowerCase(Locale.ROOT);
    }

    private boolean isFinalEmploymentStatus(
            EmploymentStatus status
    ) {
        return status == EmploymentStatus.RESIGNED
                || status == EmploymentStatus.RETIRED
                || status == EmploymentStatus.TERMINATED;
    }

    private boolean hasText(
            String value
    ) {
        return StringUtils.hasText(value);
    }

    /**
     * Authenticated branch information used by Employee services.
     */
    public record BranchContext(
            Branch branch,
            Integer userId,
            String username
    ) {
    }

    /**
     * Validated references required to create an Employee.
     */
    public record RegistrationReferences(
            BranchContext branchContext,
            Department department,
            Designation designation,
            ErpEmployee reportingManager
    ) {
    }

    /**
     * Validated references required to update an Employee.
     */
    public record UpdateReferences(
            BranchContext branchContext,
            ErpEmployee employee,
            Department department,
            Designation designation,
            ErpEmployee reportingManager
    ) {
    }
}
