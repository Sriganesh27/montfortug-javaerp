package com.erp.montfortuganda.employee.service;

import com.erp.montfortuganda.employee.dto.request.EmployeeContactRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeDeactivationRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeLoginAccountRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeTemporaryPasswordRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeDocumentRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeExperienceRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeQualificationRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeRegistrationRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeSearchRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeUpdateRequest;
import com.erp.montfortuganda.employee.dto.response.EmployeeDetailResponse;
import com.erp.montfortuganda.employee.dto.response.EmployeeLoginRoleOptionResponse;
import com.erp.montfortuganda.employee.dto.response.EmployeeOptionResponse;
import com.erp.montfortuganda.employee.dto.response.EmployeePageResponse;
import com.erp.montfortuganda.employee.dto.response.EmployeeRegistrationResponse;
import com.erp.montfortuganda.employee.dto.response.EmployeeRegistrationResponse.RegistrationStage;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import com.erp.montfortuganda.employee.entity.ErpEmployeeContact;
import com.erp.montfortuganda.employee.entity.ErpEmployeeDocument;
import com.erp.montfortuganda.employee.entity.ErpEmployeeExperience;
import com.erp.montfortuganda.employee.entity.ErpEmployeeQualification;
import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import com.erp.montfortuganda.employee.mapper.EmployeeMapper;
import com.erp.montfortuganda.employee.repository.EmployeeSpecification;
import com.erp.montfortuganda.employee.repository.ErpEmployeeContactRepository;
import com.erp.montfortuganda.employee.repository.ErpEmployeeDocumentRepository;
import com.erp.montfortuganda.employee.repository.ErpEmployeeExperienceRepository;
import com.erp.montfortuganda.employee.repository.ErpEmployeeQualificationRepository;
import com.erp.montfortuganda.employee.repository.ErpEmployeeRepository;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.DuplicateResourceException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Transactional Employee-module implementation.
 *
 * <p>Every browser-supplied Employee or nested-record ID is resolved together
 * with the authenticated branch. Permanent Employee data is stored in MySQL;
 * the registration-progress service contains only short-lived UI status.</p>
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final int DEFAULT_PAGE =
            0;

    private static final int DEFAULT_PAGE_SIZE =
            10;

    private static final int MAX_PAGE_SIZE =
            100;

    private static final String DEFAULT_SORT_FIELD =
            "employeeId";

    private static final Sort.Direction DEFAULT_SORT_DIRECTION =
            Sort.Direction.DESC;

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of(
                    "employeeId",
                    "employeeNo",
                    "fullName",
                    "employeeCategory",
                    "employeeType",
                    "employmentMode",
                    "employmentStatus",
                    "joiningDate",
                    "officialEmail",
                    "mobileNo",
                    "active",
                    "createdAt",
                    "updatedAt"
            );

    private static final String EMPLOYEE_API_BASE =
            "/api/branchadmin/employees/";

    private final EmployeeValidationService validationService;
    private final EmployeeNumberService numberService;
    private final EmployeeFileService fileService;
    private final EmployeeAccountService accountService;
    private final EmployeeRegistrationProgressService progressService;
    private final EmployeeMapper employeeMapper;

    private final ErpEmployeeRepository employeeRepository;
    private final ErpEmployeeContactRepository contactRepository;
    private final ErpEmployeeQualificationRepository qualificationRepository;
    private final ErpEmployeeExperienceRepository experienceRepository;
    private final ErpEmployeeDocumentRepository documentRepository;

    private final ApplicationEventPublisher eventPublisher;

    public EmployeeServiceImpl(
            EmployeeValidationService validationService,
            EmployeeNumberService numberService,
            EmployeeFileService fileService,
            EmployeeAccountService accountService,
            EmployeeRegistrationProgressService progressService,
            EmployeeMapper employeeMapper,
            ErpEmployeeRepository employeeRepository,
            ErpEmployeeContactRepository contactRepository,
            ErpEmployeeQualificationRepository qualificationRepository,
            ErpEmployeeExperienceRepository experienceRepository,
            ErpEmployeeDocumentRepository documentRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.validationService = validationService;
        this.numberService = numberService;
        this.fileService = fileService;
        this.accountService = accountService;
        this.progressService = progressService;
        this.employeeMapper = employeeMapper;
        this.employeeRepository = employeeRepository;
        this.contactRepository = contactRepository;
        this.qualificationRepository = qualificationRepository;
        this.experienceRepository = experienceRepository;
        this.documentRepository = documentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public EmployeeRegistrationResponse registerEmployee(
            EmployeeRegistrationRequest request
    ) {
        Objects.requireNonNull(
                request,
                "Employee registration request is required."
        );

        EmployeeValidationService.BranchContext initialBranchContext =
                validationService.requireAuthenticatedBranch();

        Integer branchId =
                initialBranchContext.branch().getBranchId();

        Integer userId =
                initialBranchContext.userId();

        String operationId =
                progressService
                        .createOperation(
                                branchId,
                                userId,
                                buildRequestedEmployeeName(request),
                                countRelatedItems(request)
                        )
                        .operationId();

        RegistrationStage currentStage =
                RegistrationStage.SERVER_VALIDATION;

        try {
            progressService.updateStage(
                    operationId,
                    branchId,
                    userId,
                    currentStage,
                    10,
                    "Validating Employee information."
            );

            EmployeeValidationService.RegistrationReferences references =
                    validationService.validateForRegistration(request);

            currentStage =
                    RegistrationStage.EMPLOYEE_CREATION;

            progressService.updateStage(
                    operationId,
                    branchId,
                    userId,
                    currentStage,
                    25,
                    "Generating Employee number and creating the Employee record."
            );

            String employeeNo =
                    numberService.generateEmployeeNumber(
                            references.branchContext().branch(),
                            request.employeeCategory(),
                            request.joiningDate()
                    );

            ErpEmployee employee =
                    employeeMapper.toNewEmployee(
                            request,
                            references.branchContext().branch(),
                            references.department(),
                            references.designation(),
                            references.reportingManager(),
                            employeeNo
                    );

            fileService.storeNewEmployeeFiles(
                    request,
                    employee
            );

            employee =
                    employeeRepository.saveAndFlush(
                            employee
                    );

            currentStage =
                    RegistrationStage.RELATED_RECORDS;

            progressService.updateStage(
                    operationId,
                    branchId,
                    userId,
                    currentStage,
                    40,
                    "Saving Employee contacts, qualifications, experience and documents."
            );

            RelatedRecordProgress relatedProgress =
                    createRelatedRecords(
                            request,
                            employee,
                            operationId,
                            branchId,
                            userId
                    );

            currentStage =
                    RegistrationStage.REPORTING_MANAGER;

            progressService.updateStage(
                    operationId,
                    branchId,
                    userId,
                    currentStage,
                    75,
                    references.reportingManager() == null
                            ? "No reporting manager was selected."
                            : "Reporting manager assigned successfully."
            );

            currentStage =
                    RegistrationStage.LOGIN_ACCOUNT;

            progressService.updateStage(
                    operationId,
                    branchId,
                    userId,
                    currentStage,
                    85,
                    request.accountRequest().generateLogin()
                            ? "Creating the Employee login account."
                            : "Employee login account was not requested."
            );

            EmployeeAccountService.AccountCreationResult accountResult =
                    accountService.createEmployeeAccount(
                            request.accountRequest(),
                            employee,
                            references.branchContext().userId()
                    );

            employee =
                    employeeRepository.saveAndFlush(
                            employee
                    );

            publishWelcomeEmailAfterCommit(
                    employee,
                    accountResult
            );

            currentStage =
                    RegistrationStage.FINALIZATION;

            progressService.updateStage(
                    operationId,
                    branchId,
                    userId,
                    currentStage,
                    95,
                    "Finalizing Employee registration."
            );

            EmployeeRegistrationProgressService.RegistrationCompletion completion =
                    new EmployeeRegistrationProgressService
                            .RegistrationCompletion(
                            employee.getEmployeeId(),
                            employee.getEmployeeNo(),
                            employee.getFullName(),
                            references.department().getDepartmentName(),
                            references.designation().getDesignationName(),
                            references.reportingManager() == null
                                    ? null
                                    : references.reportingManager().getFullName(),
                            accountResult.created(),
                            resolveLoginAccountStatus(accountResult),
                            accountResult.userId(),
                            accountResult.username(),
                            accountResult.roleCode(),
                            accountResult.credentialDeliveryStatus(),
                            relatedProgress.completedItems(),
                            relatedProgress.totalItems(),
                            "Employee registered successfully."
                    );

            return progressService.complete(
                    operationId,
                    branchId,
                    userId,
                    completion
            );
        } catch (RuntimeException exception) {
            progressService.fail(
                    operationId,
                    branchId,
                    userId,
                    currentStage,
                    "Employee registration failed.",
                    safeFailureMessage(exception)
            );

            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeRegistrationResponse getRegistrationStatus(
            String operationId
    ) {
        EmployeeValidationService.BranchContext branchContext =
                validationService.requireAuthenticatedBranch();

        return progressService.getStatus(
                operationId,
                branchContext.branch().getBranchId(),
                branchContext.userId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeePageResponse searchEmployees(
            EmployeeSearchRequest request,
            Integer page,
            Integer size,
            String sort
    ) {
        EmployeeValidationService.BranchContext branchContext =
                validationService.requireAuthenticatedBranch();

        PageRequestDetails pageRequestDetails =
                resolvePageRequest(
                        page,
                        size,
                        sort
                );

        Pageable pageable =
                PageRequest.of(
                        pageRequestDetails.page(),
                        pageRequestDetails.size(),
                        Sort.by(
                                pageRequestDetails.direction(),
                                pageRequestDetails.sortField()
                        )
                );

        Page<ErpEmployee> employeePage =
                employeeRepository.findAll(
                        EmployeeSpecification.getSearchSpecification(
                                branchContext.branch().getBranchId(),
                                request
                        ),
                        pageable
                );

        return employeeMapper.toPageResponse(
                employeePage,
                this::profilePhotoUrl,
                pageRequestDetails.sortField(),
                pageRequestDetails.direction()
                        .name()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeLoginRoleOptionResponse> getLoginRoleOptions() {
        validationService.requireAuthenticatedBranch();

        return accountService.getAssignableLoginRoles();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeOptionResponse> getReportingManagers(
            Long excludeEmployeeId
    ) {
        EmployeeValidationService.BranchContext branchContext =
                validationService.requireAuthenticatedBranch();

        Integer branchId =
                branchContext.branch().getBranchId();

        return employeeRepository
                .findAllByBranch_BranchIdAndActiveTrueOrderByFullNameAsc(
                        branchId
                )
                .stream()
                .filter(employee ->
                        !Objects.equals(
                                employee.getEmployeeId(),
                                excludeEmployeeId
                        )
                )
                .filter(employee ->
                        !isFinalEmploymentStatus(
                                employee
                                        .getEmploymentStatus()
                        )
                )
                .map(this::toEmployeeOptionResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDetailResponse getEmployee(
            Long employeeId
    ) {
        EmployeeValidationService.BranchContext branchContext =
                validationService.requireAuthenticatedBranch();

        Integer branchId =
                branchContext.branch().getBranchId();

        ErpEmployee employee =
                validationService.requireEmployee(
                        employeeId,
                        branchId
                );

        return buildDetailResponse(
                employee,
                branchId
        );
    }

    @Override
    @Transactional
    public EmployeeDetailResponse updateEmployee(
            Long employeeId,
            EmployeeUpdateRequest request
    ) {
        EmployeeValidationService.UpdateReferences references =
                validationService.validateForUpdate(
                        employeeId,
                        request
                );

        Integer branchId =
                references.branchContext()
                        .branch()
                        .getBranchId();

        ErpEmployee employee =
                references.employee();

        employeeMapper.updateEmployee(
                request,
                employee,
                references.department(),
                references.designation(),
                references.reportingManager()
        );

        fileService.replaceEmployeeMasterFiles(
                request,
                employee
        );

        synchronizeContacts(
                employee,
                branchId,
                request.contacts()
        );

        synchronizeQualifications(
                employee,
                branchId,
                request.qualifications()
        );

        synchronizeExperiences(
                employee,
                branchId,
                request.experiences()
        );

        synchronizeDocuments(
                employee,
                branchId,
                request.documents()
        );

        if (!Boolean.TRUE.equals(employee.getActive())) {
            accountService.deactivateEmployeeAccount(
                    employee
            );
        }

        employee =
                employeeRepository.saveAndFlush(
                        employee
                );

        return buildDetailResponse(
                employee,
                branchId
        );
    }

    @Override
    @Transactional
    public EmployeeDetailResponse createLoginAccount(
            Long employeeId,
            EmployeeLoginAccountRequest request
    ) {
        Objects.requireNonNull(
                request,
                "Employee login account request is required."
        );

        EmployeeValidationService.BranchContext branchContext =
                validationService.requireAuthenticatedBranch();

        ErpEmployee employee =
                validationService.requireBranchEmployee(
                        employeeId
                );

        EmployeeAccountService.AccountCreationResult accountResult =
                accountService.createEmployeeAccountForExistingEmployee(
                        request,
                        employee,
                        branchContext.userId()
                );

        employee =
                employeeRepository.saveAndFlush(
                        employee
                );

        publishWelcomeEmailAfterCommit(
                employee,
                accountResult
        );

        return buildDetailResponse(
                employee,
                branchContext.branch().getBranchId()
        );
    }

    @Override
    @Transactional
    public EmployeeDetailResponse resetAndSendTemporaryPassword(
            Long employeeId,
            EmployeeTemporaryPasswordRequest request
    ) {
        Objects.requireNonNull(
                request,
                "Employee temporary-password request is required."
        );

        if (!request.sendEmail()) {
            throw new BadRequestException(
                    "The new temporary password must be sent to the Employee email."
            );
        }

        EmployeeValidationService.BranchContext branchContext =
                validationService.requireAuthenticatedBranch();

        ErpEmployee employee =
                validationService.requireBranchEmployee(
                        employeeId
                );

        EmployeeAccountService.TemporaryPasswordResetResult resetResult =
                accountService.resetTemporaryPassword(
                        employee
                );

        employee =
                employeeRepository.saveAndFlush(
                        employee
                );

        publishTemporaryPasswordResetEmailAfterCommit(
                employee,
                resetResult
        );

        return buildDetailResponse(
                employee,
                branchContext.branch().getBranchId()
        );
    }

    @Override
    @Transactional
    public void deactivateEmployee(
            Long employeeId,
            EmployeeDeactivationRequest request
    ) {
        Objects.requireNonNull(
                request,
                "Employee deactivation request is required."
        );

        ErpEmployee employee =
                validationService.validateForDeactivation(
                        employeeId
                );

        if (!Boolean.TRUE.equals(employee.getActive())) {
            throw new BadRequestException(
                    "Employee is already inactive."
            );
        }

        EmploymentStatus finalStatus =
                request.employmentStatus();

        employee.setEmploymentStatus(
                finalStatus
        );
        employee.setEmploymentEndDate(
                request.effectiveDate()
        );
        employee.setExitReason(
                request.exitReason().trim()
        );
        employee.setActive(false);

        switch (finalStatus) {
            case RESIGNED -> {
                employee.setResignationDate(
                        request.effectiveDate()
                );
                employee.setTerminationDate(null);
                employee.setRetirementDate(null);
            }

            case RETIRED -> {
                employee.setRetirementDate(
                        request.effectiveDate()
                );
                employee.setResignationDate(null);
                employee.setTerminationDate(null);
            }

            case TERMINATED -> {
                employee.setTerminationDate(
                        request.effectiveDate()
                );
                employee.setResignationDate(null);
                employee.setRetirementDate(null);
            }

            default -> throw new BadRequestException(
                    "Final employment status must be RESIGNED, RETIRED or TERMINATED."
            );
        }

        accountService.deactivateEmployeeAccount(
                employee
        );

        employeeRepository.saveAndFlush(
                employee
        );
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeePrivateFile getProfilePhoto(
            Long employeeId
    ) {
        ErpEmployee employee =
                validationService.requireBranchEmployee(
                        employeeId
                );

        return privateFile(
                employee.getProfilePhoto(),
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeePrivateFile getSignature(
            Long employeeId
    ) {
        ErpEmployee employee =
                validationService.requireBranchEmployee(
                        employeeId
                );

        return privateFile(
                employee.getSignatureFile(),
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeePrivateFile getQualificationFile(
            Long employeeId,
            Long qualificationId
    ) {
        EmployeeValidationService.BranchContext branchContext =
                validationService.requireAuthenticatedBranch();

        validationService.requireEmployee(
                employeeId,
                branchContext.branch().getBranchId()
        );

        ErpEmployeeQualification qualification =
                qualificationRepository
                        .findByEmployeeQualificationIdAndEmployee_EmployeeIdAndEmployee_Branch_BranchId(
                                qualificationId,
                                employeeId,
                                branchContext.branch().getBranchId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Employee qualification was not found."
                                )
                        );

        return privateFile(
                qualification.getEmployeeQualificationDocumentFile(),
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeePrivateFile getExperienceCertificate(
            Long employeeId,
            Long experienceId
    ) {
        ErpEmployeeExperience experience =
                requireExperience(
                        employeeId,
                        experienceId
                );

        return privateFile(
                experience
                        .getEmployeeExperienceExperienceCertificateFile(),
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeePrivateFile getRelievingLetter(
            Long employeeId,
            Long experienceId
    ) {
        ErpEmployeeExperience experience =
                requireExperience(
                        employeeId,
                        experienceId
                );

        return privateFile(
                experience
                        .getEmployeeExperienceRelievingLetterFile(),
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeePrivateFile getDocumentFile(
            Long employeeId,
            Long documentId
    ) {
        EmployeeValidationService.BranchContext branchContext =
                validationService.requireAuthenticatedBranch();

        Integer branchId =
                branchContext.branch().getBranchId();

        validationService.requireEmployee(
                employeeId,
                branchId
        );

        ErpEmployeeDocument document =
                documentRepository
                        .findByEmployeeDocumentIdAndEmployee_EmployeeIdAndEmployee_Branch_BranchId(
                                documentId,
                                employeeId,
                                branchId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Employee document was not found."
                                )
                        );

        return privateFile(
                document.getEmployeeDocumentFilePath(),
                document.getEmployeeDocumentOriginalFileName()
        );
    }

    private RelatedRecordProgress createRelatedRecords(
            EmployeeRegistrationRequest request,
            ErpEmployee employee,
            String operationId,
            Integer branchId,
            Integer userId
    ) {
        int totalItems =
                countRelatedItems(request);

        int completedItems =
                0;

        List<ErpEmployeeContact> contacts =
                employeeMapper.toNewContacts(
                        request.contacts(),
                        employee
                );

        if (!contacts.isEmpty()) {
            contactRepository.saveAllAndFlush(
                    contacts
            );

            completedItems += contacts.size();

            updateRelatedProgress(
                    operationId,
                    branchId,
                    userId,
                    completedItems,
                    totalItems,
                    "Employee contacts saved."
            );
        }

        List<ErpEmployeeQualification> qualifications =
                new ArrayList<>();

        for (
                EmployeeQualificationRequest qualificationRequest
                : request.qualifications()
        ) {
            ErpEmployeeQualification qualification =
                    employeeMapper.toNewQualification(
                            qualificationRequest,
                            employee
                    );

            fileService.storeNewQualificationFile(
                    qualificationRequest,
                    qualification
            );

            qualifications.add(qualification);

            completedItems++;

            updateRelatedProgress(
                    operationId,
                    branchId,
                    userId,
                    completedItems,
                    totalItems,
                    "Employee qualifications are being saved."
            );
        }

        if (!qualifications.isEmpty()) {
            qualificationRepository.saveAllAndFlush(
                    qualifications
            );
        }

        List<ErpEmployeeExperience> experiences =
                new ArrayList<>();

        for (
                EmployeeExperienceRequest experienceRequest
                : request.experiences()
        ) {
            ErpEmployeeExperience experience =
                    employeeMapper.toNewExperience(
                            experienceRequest,
                            employee
                    );

            fileService.storeNewExperienceFiles(
                    experienceRequest,
                    experience
            );

            experiences.add(experience);

            completedItems++;

            updateRelatedProgress(
                    operationId,
                    branchId,
                    userId,
                    completedItems,
                    totalItems,
                    "Employee experience records are being saved."
            );
        }

        if (!experiences.isEmpty()) {
            experienceRepository.saveAllAndFlush(
                    experiences
            );
        }

        List<ErpEmployeeDocument> documents =
                new ArrayList<>();

        for (
                EmployeeDocumentRequest documentRequest
                : request.documents()
        ) {
            ErpEmployeeDocument document =
                    employeeMapper.toNewDocument(
                            documentRequest,
                            employee
                    );

            fileService.storeNewDocumentFile(
                    documentRequest,
                    document
            );

            documents.add(document);

            completedItems++;

            updateRelatedProgress(
                    operationId,
                    branchId,
                    userId,
                    completedItems,
                    totalItems,
                    "Employee documents are being saved."
            );
        }

        if (!documents.isEmpty()) {
            documentRepository.saveAllAndFlush(
                    documents
            );
        }

        if (totalItems == 0) {
            updateRelatedProgress(
                    operationId,
                    branchId,
                    userId,
                    0,
                    0,
                    "No related Employee records were supplied."
            );
        }

        return new RelatedRecordProgress(
                completedItems,
                totalItems
        );
    }

    private void synchronizeContacts(
            ErpEmployee employee,
            Integer branchId,
            List<EmployeeContactRequest> requests
    ) {
        List<ErpEmployeeContact> existing =
                contactRepository
                        .findAllByEmployee_EmployeeIdAndEmployee_Branch_BranchIdOrderByEmployeeContactIsPrimaryDescEmployeeContactNameAsc(
                                employee.getEmployeeId(),
                                branchId
                        );

        Map<Long, ErpEmployeeContact> remaining =
                mapById(
                        existing,
                        ErpEmployeeContact::getEmployeeContactId
                );

        List<ErpEmployeeContact> recordsToSave =
                new ArrayList<>();

        for (EmployeeContactRequest request : requests) {
            ErpEmployeeContact contact;

            if (request.employeeContactId() == null) {
                contact =
                        employeeMapper.toNewContact(
                                request,
                                employee
                        );
            } else {
                contact =
                        takeExisting(
                                remaining,
                                request.employeeContactId(),
                                "Employee contact"
                        );

                employeeMapper.updateContact(
                        request,
                        contact
                );
            }

            recordsToSave.add(contact);
        }

        if (!recordsToSave.isEmpty()) {
            contactRepository.saveAllAndFlush(
                    recordsToSave
            );
        }

        if (!remaining.isEmpty()) {
            contactRepository.deleteAll(
                    remaining.values()
            );

            contactRepository.flush();
        }
    }

    private void synchronizeQualifications(
            ErpEmployee employee,
            Integer branchId,
            List<EmployeeQualificationRequest> requests
    ) {
        List<ErpEmployeeQualification> existing =
                qualificationRepository
                        .findAllByEmployee_EmployeeIdAndEmployee_Branch_BranchIdOrderByEmployeeQualificationCompletionYearDescEmployeeQualificationIdDesc(
                                employee.getEmployeeId(),
                                branchId
                        );

        Map<Long, ErpEmployeeQualification> remaining =
                mapById(
                        existing,
                        ErpEmployeeQualification::getEmployeeQualificationId
                );

        List<ErpEmployeeQualification> recordsToSave =
                new ArrayList<>();

        for (EmployeeQualificationRequest request : requests) {
            ErpEmployeeQualification qualification;

            if (request.employeeQualificationId() == null) {
                qualification =
                        employeeMapper.toNewQualification(
                                request,
                                employee
                        );

                fileService.storeNewQualificationFile(
                        request,
                        qualification
                );
            } else {
                qualification =
                        takeExisting(
                                remaining,
                                request.employeeQualificationId(),
                                "Employee qualification"
                        );

                employeeMapper.updateQualification(
                        request,
                        qualification
                );

                fileService.replaceQualificationFile(
                        request,
                        qualification
                );
            }

            recordsToSave.add(qualification);
        }

        if (!recordsToSave.isEmpty()) {
            qualificationRepository.saveAllAndFlush(
                    recordsToSave
            );
        }

        if (!remaining.isEmpty()) {
            scheduleQualificationFilesForDeletion(
                    remaining.values()
            );

            qualificationRepository.deleteAll(
                    remaining.values()
            );

            qualificationRepository.flush();
        }
    }

    private void synchronizeExperiences(
            ErpEmployee employee,
            Integer branchId,
            List<EmployeeExperienceRequest> requests
    ) {
        List<ErpEmployeeExperience> existing =
                experienceRepository
                        .findAllByEmployee_EmployeeIdAndEmployee_Branch_BranchIdOrderByEmployeeExperienceStartDateDescEmployeeExperienceIdDesc(
                                employee.getEmployeeId(),
                                branchId
                        );

        Map<Long, ErpEmployeeExperience> remaining =
                mapById(
                        existing,
                        ErpEmployeeExperience::getEmployeeExperienceId
                );

        List<ErpEmployeeExperience> recordsToSave =
                new ArrayList<>();

        for (EmployeeExperienceRequest request : requests) {
            ErpEmployeeExperience experience;

            if (request.employeeExperienceId() == null) {
                experience =
                        employeeMapper.toNewExperience(
                                request,
                                employee
                        );

                fileService.storeNewExperienceFiles(
                        request,
                        experience
                );
            } else {
                experience =
                        takeExisting(
                                remaining,
                                request.employeeExperienceId(),
                                "Employee experience"
                        );

                employeeMapper.updateExperience(
                        request,
                        experience
                );

                fileService.replaceExperienceFiles(
                        request,
                        experience
                );
            }

            recordsToSave.add(experience);
        }

        if (!recordsToSave.isEmpty()) {
            experienceRepository.saveAllAndFlush(
                    recordsToSave
            );
        }

        if (!remaining.isEmpty()) {
            scheduleExperienceFilesForDeletion(
                    remaining.values()
            );

            experienceRepository.deleteAll(
                    remaining.values()
            );

            experienceRepository.flush();
        }
    }

    private void synchronizeDocuments(
            ErpEmployee employee,
            Integer branchId,
            List<EmployeeDocumentRequest> requests
    ) {
        List<ErpEmployeeDocument> existing =
                documentRepository
                        .findAllByEmployee_EmployeeIdAndEmployee_Branch_BranchIdOrderByEmployeeDocumentTypeAscEmployeeDocumentNameAsc(
                                employee.getEmployeeId(),
                                branchId
                        );

        Map<Long, ErpEmployeeDocument> remaining =
                mapById(
                        existing,
                        ErpEmployeeDocument::getEmployeeDocumentId
                );

        List<ErpEmployeeDocument> recordsToSave =
                new ArrayList<>();

        for (EmployeeDocumentRequest request : requests) {
            ErpEmployeeDocument document;

            if (request.employeeDocumentId() == null) {
                document =
                        employeeMapper.toNewDocument(
                                request,
                                employee
                        );

                fileService.storeNewDocumentFile(
                        request,
                        document
                );
            } else {
                document =
                        takeExisting(
                                remaining,
                                request.employeeDocumentId(),
                                "Employee document"
                        );

                employeeMapper.updateDocument(
                        request,
                        document
                );

                fileService.replaceDocumentFile(
                        request,
                        document
                );
            }

            recordsToSave.add(document);
        }

        if (!recordsToSave.isEmpty()) {
            documentRepository.saveAllAndFlush(
                    recordsToSave
            );
        }

        if (!remaining.isEmpty()) {
            fileService.scheduleDeleteAfterCommit(
                    remaining.values()
                            .stream()
                            .map(
                                    ErpEmployeeDocument
                                            ::getEmployeeDocumentFilePath
                            )
                            .filter(StringUtils::hasText)
                            .toList()
            );

            documentRepository.deleteAll(
                    remaining.values()
            );

            documentRepository.flush();
        }
    }

    private EmployeeDetailResponse buildDetailResponse(
            ErpEmployee employee,
            Integer branchId
    ) {
        Long employeeId =
                employee.getEmployeeId();

        List<ErpEmployeeContact> contacts =
                contactRepository
                        .findAllByEmployee_EmployeeIdAndEmployee_Branch_BranchIdOrderByEmployeeContactIsPrimaryDescEmployeeContactNameAsc(
                                employeeId,
                                branchId
                        );

        List<ErpEmployeeQualification> qualifications =
                qualificationRepository
                        .findAllByEmployee_EmployeeIdAndEmployee_Branch_BranchIdOrderByEmployeeQualificationCompletionYearDescEmployeeQualificationIdDesc(
                                employeeId,
                                branchId
                        );

        List<ErpEmployeeExperience> experiences =
                experienceRepository
                        .findAllByEmployee_EmployeeIdAndEmployee_Branch_BranchIdOrderByEmployeeExperienceStartDateDescEmployeeExperienceIdDesc(
                                employeeId,
                                branchId
                        );

        List<ErpEmployeeDocument> documents =
                documentRepository
                        .findAllByEmployee_EmployeeIdAndEmployee_Branch_BranchIdOrderByEmployeeDocumentTypeAscEmployeeDocumentNameAsc(
                                employeeId,
                                branchId
                        );

        return employeeMapper.toDetailResponse(
                employee,
                contacts,
                qualifications,
                experiences,
                documents,
                profilePhotoUrl(employee),
                signatureUrl(employee)
        );
    }

    private EmployeeOptionResponse toEmployeeOptionResponse(
            ErpEmployee employee
    ) {
        return new EmployeeOptionResponse(
                employee.getEmployeeId(),
                employee.getEmployeeNo(),
                employee.getFullName(),
                employee.getDepartment() == null
                        ? null
                        : employee
                        .getDepartment()
                        .getDepartmentId(),
                employee.getDepartment() == null
                        ? null
                        : employee
                        .getDepartment()
                        .getDepartmentName(),
                employee.getDesignation() == null
                        ? null
                        : employee
                        .getDesignation()
                        .getDesignationId(),
                employee.getDesignation() == null
                        ? null
                        : employee
                        .getDesignation()
                        .getDesignationName()
        );
    }

    private boolean isFinalEmploymentStatus(
            EmploymentStatus status
    ) {
        return status == EmploymentStatus.RESIGNED
                || status == EmploymentStatus.RETIRED
                || status == EmploymentStatus.TERMINATED;
    }

    private ErpEmployeeExperience requireExperience(
            Long employeeId,
            Long experienceId
    ) {
        EmployeeValidationService.BranchContext branchContext =
                validationService.requireAuthenticatedBranch();

        Integer branchId =
                branchContext.branch().getBranchId();

        validationService.requireEmployee(
                employeeId,
                branchId
        );

        return experienceRepository
                .findByEmployeeExperienceIdAndEmployee_EmployeeIdAndEmployee_Branch_BranchId(
                        experienceId,
                        employeeId,
                        branchId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee experience record was not found."
                        )
                );
    }

    private EmployeePrivateFile privateFile(
            String relativePath,
            String preferredFilename
    ) {
        if (!StringUtils.hasText(relativePath)) {
            throw new ResourceNotFoundException(
                    "Employee file was not found."
            );
        }

        Resource resource =
                fileService.loadPrivateFile(
                        relativePath
                );

        String filename =
                StringUtils.hasText(preferredFilename)
                        ? preferredFilename.trim()
                        : fileService.getStoredFilename(
                        relativePath
                );

        return new EmployeePrivateFile(
                resource,
                filename,
                fileService.detectContentType(
                        relativePath
                ),
                fileService.getStoredFileSize(
                        relativePath
                )
        );
    }

    private void publishWelcomeEmailAfterCommit(
            ErpEmployee employee,
            EmployeeAccountService.AccountCreationResult accountResult
    ) {
        if (
                !accountResult.created()
                        || !accountResult.sendEmail()
        ) {
            return;
        }

        eventPublisher.publishEvent(
                new EmployeeWelcomeEmailRequestedEvent(
                        employee.getEmployeeId(),
                        accountResult.userId(),
                        accountResult.username(),
                        accountResult.temporaryPassword(),
                        false
                )
        );
    }

    private void publishTemporaryPasswordResetEmailAfterCommit(
            ErpEmployee employee,
            EmployeeAccountService.TemporaryPasswordResetResult resetResult
    ) {
        eventPublisher.publishEvent(
                new EmployeeWelcomeEmailRequestedEvent(
                        employee.getEmployeeId(),
                        resetResult.userId(),
                        resetResult.username(),
                        resetResult.temporaryPassword(),
                        true
                )
        );
    }

    private void updateRelatedProgress(
            String operationId,
            Integer branchId,
            Integer userId,
            Integer completedItems,
            Integer totalItems,
            String message
    ) {
        progressService.updateItemProgress(
                operationId,
                branchId,
                userId,
                completedItems,
                totalItems,
                message
        );
    }

    private int countRelatedItems(
            EmployeeRegistrationRequest request
    ) {
        return safeSize(request.contacts())
                + safeSize(request.qualifications())
                + safeSize(request.experiences())
                + safeSize(request.documents());
    }

    private int safeSize(
            Collection<?> values
    ) {
        return values == null
                ? 0
                : values.size();
    }

    private String buildRequestedEmployeeName(
            EmployeeRegistrationRequest request
    ) {
        return java.util.stream.Stream.of(
                        request.firstName(),
                        request.middleName(),
                        request.lastName()
                )
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(
                        Collectors.joining(" ")
                );
    }

    private String resolveLoginAccountStatus(
            EmployeeAccountService.AccountCreationResult result
    ) {
        if (!result.created()) {
            return "NOT_CREATED";
        }

        if (result.sendEmail()) {
            return "CREATED_CREDENTIAL_EMAIL_PENDING";
        }

        return "CREATED_EMAIL_NOT_REQUESTED";
    }

    private String profilePhotoUrl(
            ErpEmployee employee
    ) {
        if (
                employee == null
                        || employee.getEmployeeId() == null
                        || !StringUtils.hasText(
                        employee.getProfilePhoto()
                )
        ) {
            return null;
        }

        return EMPLOYEE_API_BASE
                + employee.getEmployeeId()
                + "/profile-photo";
    }

    private String signatureUrl(
            ErpEmployee employee
    ) {
        if (
                employee == null
                        || employee.getEmployeeId() == null
                        || !StringUtils.hasText(
                        employee.getSignatureFile()
                )
        ) {
            return null;
        }

        return EMPLOYEE_API_BASE
                + employee.getEmployeeId()
                + "/signature";
    }

    private PageRequestDetails resolvePageRequest(
            Integer page,
            Integer size,
            String sort
    ) {
        int resolvedPage =
                page == null
                        ? DEFAULT_PAGE
                        : page;

        if (resolvedPage < 0) {
            throw new BadRequestException(
                    "Employee page number cannot be negative."
            );
        }

        int resolvedSize =
                size == null
                        ? DEFAULT_PAGE_SIZE
                        : size;

        if (
                resolvedSize <= 0
                        || resolvedSize > MAX_PAGE_SIZE
        ) {
            throw new BadRequestException(
                    "Employee page size must be between 1 and "
                            + MAX_PAGE_SIZE
                            + "."
            );
        }

        String sortField =
                DEFAULT_SORT_FIELD;

        Sort.Direction direction =
                DEFAULT_SORT_DIRECTION;

        if (StringUtils.hasText(sort)) {
            String[] parts =
                    sort.trim()
                            .split(
                                    ",",
                                    2
                            );

            if (StringUtils.hasText(parts[0])) {
                sortField =
                        parts[0].trim();
            }

            if (parts.length == 2) {
                try {
                    direction =
                            Sort.Direction.fromString(
                                    parts[1].trim()
                            );
                } catch (IllegalArgumentException exception) {
                    throw new BadRequestException(
                            "Employee sort direction must be ASC or DESC."
                    );
                }
            }
        }

        if (!ALLOWED_SORT_FIELDS.contains(sortField)) {
            throw new BadRequestException(
                    "Unsupported Employee sort field: "
                            + sortField
                            + "."
            );
        }

        return new PageRequestDetails(
                resolvedPage,
                resolvedSize,
                sortField,
                direction
        );
    }

    private <T> Map<Long, T> mapById(
            List<T> records,
            Function<T, Long> idResolver
    ) {
        Map<Long, T> mappedRecords =
                new LinkedHashMap<>();

        for (T record : records) {
            Long recordId =
                    idResolver.apply(record);

            if (recordId != null) {
                mappedRecords.put(
                        recordId,
                        record
                );
            }
        }

        return mappedRecords;
    }

    private <T> T takeExisting(
            Map<Long, T> remaining,
            Long recordId,
            String label
    ) {
        T existing =
                remaining.remove(
                        recordId
                );

        if (existing == null) {
            throw new BadRequestException(
                    label
                            + " ID "
                            + recordId
                            + " was supplied more than once or does not belong "
                            + "to this Employee."
            );
        }

        return existing;
    }

    private void scheduleQualificationFilesForDeletion(
            Collection<ErpEmployeeQualification> qualifications
    ) {
        fileService.scheduleDeleteAfterCommit(
                qualifications.stream()
                        .map(
                                ErpEmployeeQualification
                                        ::getEmployeeQualificationDocumentFile
                        )
                        .filter(StringUtils::hasText)
                        .toList()
        );
    }

    private void scheduleExperienceFilesForDeletion(
            Collection<ErpEmployeeExperience> experiences
    ) {
        List<String> paths =
                new ArrayList<>();

        for (ErpEmployeeExperience experience : experiences) {
            addPath(
                    paths,
                    experience
                            .getEmployeeExperienceExperienceCertificateFile()
            );

            addPath(
                    paths,
                    experience
                            .getEmployeeExperienceRelievingLetterFile()
            );
        }

        fileService.scheduleDeleteAfterCommit(
                paths
        );
    }

    private void addPath(
            List<String> paths,
            String path
    ) {
        if (StringUtils.hasText(path)) {
            paths.add(
                    path
            );
        }
    }

    private String safeFailureMessage(
            RuntimeException exception
    ) {
        if (
                exception instanceof BadRequestException
                        || exception instanceof DuplicateResourceException
                        || exception instanceof ResourceNotFoundException
        ) {
            return StringUtils.hasText(
                    exception.getMessage()
            )
                    ? exception.getMessage()
                    : "Employee registration information is invalid.";
        }

        return "The Employee could not be registered. No Employee data was committed.";
    }

    private record RelatedRecordProgress(
            Integer completedItems,
            Integer totalItems
    ) {
    }

    private record PageRequestDetails(
            Integer page,
            Integer size,
            String sortField,
            Sort.Direction direction
    ) {
    }

    /**
     * Published inside the Employee transaction and consumed after commit by
     * the welcome-email listener. The password is intentionally protected
     * from generated log output.
     */
    public record EmployeeWelcomeEmailRequestedEvent(
            Long employeeId,
            Integer userId,
            String username,
            String temporaryPassword,
            boolean resent
    ) {

        @SuppressWarnings("NullableProblems")
        @Override
        public String toString() {
            return "EmployeeWelcomeEmailRequestedEvent{"
                    + "employeeId="
                    + employeeId
                    + ", userId="
                    + userId
                    + ", username='"
                    + username
                    + '\''
                    + ", temporaryPassword='[PROTECTED]'"
                    + ", resent="
                    + resent
                    + '}';
        }
    }
}
