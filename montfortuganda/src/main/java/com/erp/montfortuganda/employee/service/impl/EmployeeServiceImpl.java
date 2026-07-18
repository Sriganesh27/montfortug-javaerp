package com.erp.montfortuganda.employee.service.impl;

import com.erp.montfortuganda.auth.dto.UserDTO;
import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.auth.service.UserService;
import com.erp.montfortuganda.employee.dto.EmployeeContactRequest;
import com.erp.montfortuganda.employee.dto.EmployeeCreateRequest;
import com.erp.montfortuganda.employee.dto.EmployeeDocumentRequest;
import com.erp.montfortuganda.employee.dto.EmployeeExperienceRequest;
import com.erp.montfortuganda.employee.dto.EmployeeQualificationRequest;
import com.erp.montfortuganda.employee.dto.EmployeeResponse;
import com.erp.montfortuganda.employee.dto.EmployeeSearchCriteria;
import com.erp.montfortuganda.employee.dto.EmployeeUpdateRequest;
import com.erp.montfortuganda.employee.dto.response.EmployeeListResponse;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import com.erp.montfortuganda.employee.entity.ErpEmployeeContact;
import com.erp.montfortuganda.employee.entity.ErpEmployeeDocument;
import com.erp.montfortuganda.employee.entity.ErpEmployeeExperience;
import com.erp.montfortuganda.employee.entity.ErpEmployeeQualification;
import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import com.erp.montfortuganda.employee.generator.EmployeeCodeGenerator;
import com.erp.montfortuganda.employee.repository.EmployeeContactRepository;
import com.erp.montfortuganda.employee.repository.EmployeeDocumentRepository;
import com.erp.montfortuganda.employee.repository.EmployeeExperienceRepository;
import com.erp.montfortuganda.employee.repository.EmployeeQualificationRepository;
import com.erp.montfortuganda.employee.repository.EmployeeRepository;
import com.erp.montfortuganda.employee.service.EmployeeService;
import com.erp.montfortuganda.employee.specification.EmployeeSpecificationBuilder;
import com.erp.montfortuganda.employee.validation.EmployeeValidator;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.infrastructure.enums.DocumentType;
import com.erp.montfortuganda.infrastructure.service.PasswordService;
import com.erp.montfortuganda.infrastructure.service.StorageService;
import com.erp.montfortuganda.notification.service.EmailService;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.Department;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.school.repository.DepartmentRepository;
import com.erp.montfortuganda.school.repository.DesignationRepository;
import com.erp.montfortuganda.employee.dto.EmployeeContactDTO;
import com.erp.montfortuganda.employee.dto.EmployeeQualificationDTO;
import com.erp.montfortuganda.employee.dto.EmployeeExperienceDTO;
import com.erp.montfortuganda.employee.dto.EmployeeDocumentDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;
    private final BranchAccessService branchAccessService;
    private final EmployeeCodeGenerator codeGenerator;
    private final EmployeeValidator validator;
    private final EmployeeSpecificationBuilder specificationBuilder;

    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;

    private final StorageService storageService;
    private final PasswordService passwordService;
    private final EmailService emailService;
    private final UserService userService;

    private final EmployeeContactRepository contactRepository;
    private final EmployeeQualificationRepository qualificationRepository;
    private final EmployeeExperienceRepository experienceRepository;
    private final EmployeeDocumentRepository documentRepository;

    @Override
    @Transactional
    public EmployeeResponse createEmployee(
            EmployeeCreateRequest request
    ) {
        Integer branchId =
                branchAccessService.getAccessibleBranchId(null);

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Branch not found"
                        )
                );

        validator.validateCreation(request, branchId);

        ErpEmployee employee = new ErpEmployee();

        BeanUtils.copyProperties(
                request,
                employee,
                "departmentId",
                "designationId",
                "reportingManagerId",
                "contacts",
                "qualifications",
                "experiences",
                "documents",
                "accountRequest",
                "officialEmail",
                "branch",
                "department",
                "designation",
                "reportingManager",
                "employeeId",
                "employeeNo",
                "active",
                "employmentStatus"
        );

        if (request.getDepartmentId() != null) {
            employee.setDepartment(
                    getDepartmentForBranch(
                            request.getDepartmentId(),
                            branchId
                    )
            );
        }

        if (request.getDesignationId() != null) {
            employee.setDesignation(
                    designationRepository
                            .findById(request.getDesignationId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Designation not found"
                                    )
                            )
            );
        }
        if (request.getReportingManagerId() != null) {
            employee.setReportingManager(
                    getReportingManagerForBranch(
                            request.getReportingManagerId(),
                            branchId,
                            null
                    )
            );
        }
        employee.setOfficialEmail(
                normalizeEmail(request.getOfficialEmail())
        );

        employee.setMobileNo(request.getMobileNo());
        employee.setEmployeeCategory(
                request.getEmployeeCategory()
        );

        employee.setBranch(branch);
        employee.setEmploymentStatus(
                EmploymentStatus.ACTIVE
        );
        employee.setActive(true);

        String generatedCode = codeGenerator.generateCode(
                branchId,
                request.getEmployeeCategory(),
                request.getJoiningDate()
        );

        employee.setEmployeeNo(generatedCode);

        ErpEmployee saved =
                employeeRepository.save(employee);

        syncContacts(
                request.getContacts(),
                saved
        );

        syncQualifications(
                request.getQualifications(),
                saved
        );

        syncExperiences(
                request.getExperiences(),
                saved
        );

        syncDocuments(
                request.getDocuments(),
                saved
        );

        createEmployeeAccountIfRequested(
                request,
                saved,
                branchId
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(
            Long employeeId,
            EmployeeUpdateRequest request
    ) {
        Integer branchId =
                branchAccessService.getAccessibleBranchId(null);

        ErpEmployee employee = employeeRepository
                .findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found"
                        )
                );

        validateEmployeeBranch(employee, branchId);

        validator.validateUpdate(
                employeeId,
                request,
                branchId
        );

        BeanUtils.copyProperties(
                request,
                employee,
                "employeeId",
                "employeeNo",
                "branch",
                "department",
                "designation",
                "reportingManager",
                "employmentStatus",
                "active",
                "officialEmail",
                "contacts",
                "qualifications",
                "experiences",
                "documents"
        );

        if (request.getDepartmentId() != null) {
            employee.setDepartment(
                    getDepartmentForBranch(
                            request.getDepartmentId(),
                            branchId
                    )
            );
        } else {
            employee.setDepartment(null);
        }

        if (request.getDesignationId() != null) {
            employee.setDesignation(
                    designationRepository
                            .findById(request.getDesignationId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Designation not found"
                                    )
                            )
            );
        } else {
            employee.setDesignation(null);
        }

        if (request.getReportingManagerId() != null) {
            employee.setReportingManager(
                    getReportingManagerForBranch(
                            request.getReportingManagerId(),
                            branchId,
                            employeeId
                    )
            );
        } else {
            employee.setReportingManager(null);
        }

        employee.setOfficialEmail(
                normalizeEmail(request.getOfficialEmail())
        );

        employee.setMobileNo(request.getMobileNo());
        employee.setEmployeeCategory(
                request.getEmployeeCategory()
        );

        ErpEmployee updated =
                employeeRepository.save(employee);

        syncContacts(
                request.getContacts(),
                updated
        );

        syncQualifications(
                request.getQualifications(),
                updated
        );

        syncExperiences(
                request.getExperiences(),
                updated
        );

        syncDocuments(
                request.getDocuments(),
                updated
        );

        return mapToResponse(updated);
    }

    private void createEmployeeAccountIfRequested(
            EmployeeCreateRequest request,
            ErpEmployee employee,
            Integer branchId
    ) {
        if (request.getAccountRequest() == null
                || !Boolean.TRUE.equals(
                request.getAccountRequest()
                        .getGenerateLogin()
        )) {
            return;
        }

        String temporaryPassword =
                passwordService
                        .generateSecureTemporaryPassword();

        String username = employee.getEmployeeNo();

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);
        userDTO.setPassword(temporaryPassword);
        userDTO.setAssignedBranchId(branchId);

        if (request.getAccountRequest().getRoleId() != null) {
            userDTO.setRoleId(
                    request.getAccountRequest().getRoleId()
            );
        } else {
            userDTO.setRole("EMPLOYEE");
        }

        userService.createUser(userDTO);

        if (Boolean.TRUE.equals(
                request.getAccountRequest().getSendEmail()
        ) && employee.getOfficialEmail() != null) {

            emailService.sendEmployeeWelcomeEmail(
                    employee,
                    username,
                    temporaryPassword
            );
        }
    }

    private void syncContacts(
            List<EmployeeContactRequest> requests,
            ErpEmployee employee
    ) {
        /*
         * Null means the frontend did not send contact changes.
         * Keep existing records unchanged.
         */
        if (requests == null) {
            return;
        }

        List<ErpEmployeeContact> existingContacts =
                contactRepository
                        .findByEmployee_EmployeeIdAndEmployeeContactActiveTrue(
                                employee.getEmployeeId()
                        );

        java.util.Map<Long, ErpEmployeeContact> existingById =
                existingContacts.stream()
                        .collect(java.util.stream.Collectors.toMap(
                                ErpEmployeeContact::getEmployeeContactId,
                                contact -> contact
                        ));

        java.util.Set<Long> receivedIds =
                new java.util.HashSet<>();

        for (EmployeeContactRequest request : requests) {
            ErpEmployeeContact contact;

            if (request.getEmployeeContactId() == null) {
                contact = new ErpEmployeeContact();
            } else {
                contact = existingById.get(
                        request.getEmployeeContactId()
                );

                if (contact == null) {
                    throw new BadRequestException(
                            "Contact does not belong to this employee: "
                                    + request.getEmployeeContactId()
                    );
                }

                receivedIds.add(
                        request.getEmployeeContactId()
                );
            }

            mapContact(
                    request,
                    employee,
                    contact
            );

            contact.setEmployeeContactActive(
                    request.getActive() == null
                            || request.getActive()
            );

            contactRepository.save(contact);
        }

        /*
         * Contacts removed from the submitted list are soft-deleted.
         */
        for (ErpEmployeeContact existing : existingContacts) {
            if (!receivedIds.contains(
                    existing.getEmployeeContactId()
            )) {
                existing.setEmployeeContactActive(false);
                contactRepository.save(existing);
            }
        }
    }

    private void syncQualifications(
            List<EmployeeQualificationRequest> requests,
            ErpEmployee employee
    ) {
        /*
         * Null means qualification data was not included in this request.
         * Existing qualifications remain unchanged.
         *
         * An empty list means the user removed all qualifications.
         */
        if (requests == null) {
            return;
        }

        List<ErpEmployeeQualification> existingQualifications =
                qualificationRepository
                        .findByEmployee_EmployeeIdAndEmployeeQualificationActiveTrue(
                                employee.getEmployeeId()
                        );

        java.util.Map<Long, ErpEmployeeQualification> existingById =
                existingQualifications.stream()
                        .collect(java.util.stream.Collectors.toMap(
                                ErpEmployeeQualification::getEmployeeQualificationId,
                                qualification -> qualification
                        ));

        java.util.Set<Long> receivedIds =
                new java.util.HashSet<>();

        for (EmployeeQualificationRequest request : requests) {
            ErpEmployeeQualification qualification;

            if (request.getEmployeeQualificationId() == null) {
                qualification = new ErpEmployeeQualification();
            } else {
                qualification = existingById.get(
                        request.getEmployeeQualificationId()
                );

                if (qualification == null) {
                    throw new BadRequestException(
                            "Qualification does not belong to this employee: "
                                    + request.getEmployeeQualificationId()
                    );
                }

                receivedIds.add(
                        request.getEmployeeQualificationId()
                );
            }

            mapQualification(
                    request,
                    employee,
                    qualification
            );

            qualification.setEmployeeQualificationActive(
                    request.getActive() == null
                            || request.getActive()
            );

            qualification =
                    qualificationRepository.save(qualification);

            uploadQualificationFile(
                    request,
                    employee,
                    qualification
            );
        }

        /*
         * Qualifications removed from the submitted list
         * are soft-deleted.
         */
        for (ErpEmployeeQualification existing
                : existingQualifications) {

            if (!receivedIds.contains(
                    existing.getEmployeeQualificationId()
            )) {
                existing.setEmployeeQualificationActive(false);
                qualificationRepository.save(existing);
            }
        }
    }

    private void syncExperiences(
            List<EmployeeExperienceRequest> requests,
            ErpEmployee employee
    ) {
        if (requests == null) {
            return;
        }

        List<ErpEmployeeExperience> existingExperiences =
                experienceRepository
                        .findByEmployee_EmployeeIdAndEmployeeExperienceActiveTrue(
                                employee.getEmployeeId()
                        );

        java.util.Map<Long, ErpEmployeeExperience> existingById =
                existingExperiences.stream()
                        .collect(java.util.stream.Collectors.toMap(
                                ErpEmployeeExperience::getEmployeeExperienceId,
                                experience -> experience
                        ));

        java.util.Set<Long> receivedIds =
                new java.util.HashSet<>();

        for (EmployeeExperienceRequest request : requests) {
            ErpEmployeeExperience experience;

            if (request.getEmployeeExperienceId() == null) {
                experience = new ErpEmployeeExperience();
            } else {
                experience = existingById.get(
                        request.getEmployeeExperienceId()
                );

                if (experience == null) {
                    throw new BadRequestException(
                            "Experience does not belong to this employee: "
                                    + request.getEmployeeExperienceId()
                    );
                }

                receivedIds.add(
                        request.getEmployeeExperienceId()
                );
            }

            mapExperience(
                    request,
                    employee,
                    experience
            );

            experience.setEmployeeExperienceActive(true);

            experience = experienceRepository.save(experience);

            uploadExperienceFile(
                    request,
                    employee,
                    experience
            );
        }

        for (ErpEmployeeExperience existing : existingExperiences) {
            if (!receivedIds.contains(
                    existing.getEmployeeExperienceId()
            )) {
                existing.setEmployeeExperienceActive(false);
                experienceRepository.save(existing);
            }
        }
    }

    private void syncDocuments(
            List<EmployeeDocumentRequest> requests,
            ErpEmployee employee
    ) {
        if (requests == null) {
            return;
        }

        List<ErpEmployeeDocument> existingDocuments =
                documentRepository
                        .findByEmployee_EmployeeIdAndEmployeeDocumentActiveTrue(
                                employee.getEmployeeId()
                        );

        java.util.Map<Long, ErpEmployeeDocument> existingById =
                existingDocuments.stream()
                        .collect(java.util.stream.Collectors.toMap(
                                ErpEmployeeDocument::getEmployeeDocumentId,
                                document -> document
                        ));

        java.util.Set<Long> receivedIds =
                new java.util.HashSet<>();

        for (EmployeeDocumentRequest request : requests) {
            ErpEmployeeDocument document;

            if (request.getEmployeeDocumentId() == null) {
                document = new ErpEmployeeDocument();
            } else {
                document = existingById.get(
                        request.getEmployeeDocumentId()
                );

                if (document == null) {
                    throw new BadRequestException(
                            "Document does not belong to this employee: "
                                    + request.getEmployeeDocumentId()
                    );
                }

                receivedIds.add(
                        request.getEmployeeDocumentId()
                );
            }

            mapDocument(
                    request,
                    employee,
                    document
            );

            document.setEmployeeDocumentActive(true);

            document = documentRepository.save(document);

            uploadDocumentFile(
                    request,
                    employee,
                    document
            );
        }

        for (ErpEmployeeDocument existing : existingDocuments) {
            if (!receivedIds.contains(
                    existing.getEmployeeDocumentId()
            )) {
                existing.setEmployeeDocumentActive(false);
                documentRepository.save(existing);
            }
        }
    }

    private void uploadQualificationFile(
            EmployeeQualificationRequest request,
            ErpEmployee employee,
            ErpEmployeeQualification qualification
    ) {
        if (request.getFileData() == null
                || request.getFileData().isBlank()
                || request.getFileName() == null
                || request.getFileName().isBlank()) {
            return;
        }

        try {
            byte[] fileBytes =
                    decodeBase64(request.getFileData());

            String levelName =
                    request.getEmployeeQualificationLevel()
                            != null
                            ? request
                            .getEmployeeQualificationLevel()
                            .name()
                            .replaceAll(
                                    "[^a-zA-Z0-9]",
                                    "_"
                            )
                            : "CERT";

            String dynamicFileName =
                    levelName + "_"
                            + request.getFileName();

            MockMultipartFile multipartFile =
                    new MockMultipartFile(
                            "file",
                            dynamicFileName,
                            null,
                            fileBytes
                    );

            String documentPath =
                    storageService.storeEntityDocument(
                            multipartFile,
                            buildSchoolPath(employee),
                            "staff",
                            buildEmployeePath(employee),
                            DocumentType.CERTIFICATE
                    );

            String currentRemarks =
                    qualification
                            .getEmployeeQualificationRemarks();

            qualification.setEmployeeQualificationRemarks(
                    appendFilePath(
                            currentRemarks,
                            documentPath
                    )
            );

            qualificationRepository.save(qualification);

        } catch (Exception exception) {
            log.error(
                    "Failed to upload qualification file "
                            + "for employee {}",
                    employee.getEmployeeId(),
                    exception
            );

            throw new BadRequestException(
                    "Failed to upload qualification document"
            );
        }
    }

    private void uploadExperienceFile(
            EmployeeExperienceRequest request,
            ErpEmployee employee,
            ErpEmployeeExperience experience
    ) {
        if (request.getFileData() == null
                || request.getFileData().isBlank()
                || request.getFileName() == null
                || request.getFileName().isBlank()) {
            return;
        }

        try {
            byte[] fileBytes =
                    decodeBase64(request.getFileData());

            String companyName =
                    request.getCompanyName() != null
                            ? request.getCompanyName()
                            .replaceAll(
                                    "[^a-zA-Z0-9]",
                                    "_"
                            )
                            : "EXP";

            String dynamicFileName =
                    companyName + "_"
                            + request.getFileName();

            MockMultipartFile multipartFile =
                    new MockMultipartFile(
                            "file",
                            dynamicFileName,
                            null,
                            fileBytes
                    );

            String documentPath =
                    storageService.storeEntityDocument(
                            multipartFile,
                            buildSchoolPath(employee),
                            "staff",
                            buildEmployeePath(employee),
                            DocumentType.OTHER
                    );

            String currentRemarks =
                    experience.getEmployeeExperienceRemarks();

            experience.setEmployeeExperienceRemarks(
                    appendFilePath(
                            currentRemarks,
                            documentPath
                    )
            );

            experienceRepository.save(experience);

        } catch (Exception exception) {
            log.error(
                    "Failed to upload experience file "
                            + "for employee {}",
                    employee.getEmployeeId(),
                    exception
            );

            throw new BadRequestException(
                    "Failed to upload experience document"
            );
        }
    }

    private void uploadDocumentFile(
            EmployeeDocumentRequest request,
            ErpEmployee employee,
            ErpEmployeeDocument document
    ) {
        if (request.getFileData() == null
                || request.getFileData().isBlank()
                || request.getFileName() == null
                || request.getFileName().isBlank()) {
            return;
        }

        try {
            byte[] fileBytes =
                    decodeBase64(request.getFileData());

            String typeName =
                    request.getDocumentType() != null
                            ? request
                            .getDocumentType()
                            .name()
                            : "DOC";

            String dynamicFileName =
                    typeName + "_"
                            + request.getFileName();

            MockMultipartFile multipartFile =
                    new MockMultipartFile(
                            "file",
                            dynamicFileName,
                            null,
                            fileBytes
                    );

            String documentPath =
                    storageService.storeEntityDocument(
                            multipartFile,
                            buildSchoolPath(employee),
                            "staff",
                            buildEmployeePath(employee),
                            DocumentType.OTHER
                    );

            document.setEmployeeDocumentFilePath(
                    documentPath
            );

            documentRepository.save(document);

        } catch (Exception exception) {
            log.error(
                    "Failed to upload employee document "
                            + "for employee {}",
                    employee.getEmployeeId(),
                    exception
            );

            throw new BadRequestException(
                    "Failed to upload employee document"
            );
        }
    }

    private ErpEmployeeContact mapContact(
            EmployeeContactRequest request,
            ErpEmployee employee,
            ErpEmployeeContact entity

    ) {
        entity.setEmployee(employee);

        entity.setEmployeeContactName(
                request.getEmployeeContactName()
        );

        entity.setEmployeeContactRelationship(
                request.getEmployeeContactRelationship()
        );

        if (request.getEmployeeContactType() != null) {
            entity.setEmployeeContactType(
                    request.getEmployeeContactType()
            );
        }

        entity.setEmployeeContactMobile(
                request.getEmployeeContactMobile()
        );

        entity.setEmployeeContactAlternateMobile(
                request.getEmployeeContactAlternateMobile()
        );

        entity.setEmployeeContactEmail(
                normalizeEmail(
                        request.getEmployeeContactEmail()
                )
        );

        entity.setEmployeeContactCountry(
                request.getEmployeeContactCountry()
        );

        entity.setEmployeeContactState(
                request.getEmployeeContactState()
        );

        entity.setEmployeeContactDistrict(
                request.getEmployeeContactDistrict()
        );

        entity.setEmployeeContactVillage(
                request.getEmployeeContactVillage()
        );

        entity.setEmployeeContactStreet(
                request.getEmployeeContactStreet()
        );

        entity.setEmployeeContactPostalCode(
                request.getEmployeeContactPostalCode()
        );

        entity.setEmployeeContactOccupation(
                request.getEmployeeContactOccupation()
        );

        entity.setEmployeeContactWorkplace(
                request.getEmployeeContactWorkplace()
        );

        if (request.getEmployeeContactIsPrimary() != null) {
            entity.setEmployeeContactIsPrimary(
                    request.getEmployeeContactIsPrimary()
            );
        }

        if (request.getEmployeeContactIsEmergency()
                != null) {
            entity.setEmployeeContactIsEmergency(
                    request.getEmployeeContactIsEmergency()
            );
        }

        entity.setEmployeeContactRemarks(
                request.getEmployeeContactRemarks()
        );

        return entity;
    }

    private ErpEmployeeQualification mapQualification(
            EmployeeQualificationRequest request,
            ErpEmployee employee,
            ErpEmployeeQualification entity
    ) {
        entity.setEmployee(employee);

        entity.setEmployeeQualificationLevel(
                request.getEmployeeQualificationLevel()
        );

        entity.setEmployeeQualificationName(
                request.getEmployeeQualificationName()
        );

        entity.setEmployeeQualificationSpecialization(
                request
                        .getEmployeeQualificationSpecialization()
        );

        entity.setEmployeeQualificationInstitutionName(
                request
                        .getEmployeeQualificationInstitutionName()
        );

        entity.setEmployeeQualificationBoardUniversity(
                request
                        .getEmployeeQualificationBoardUniversity()
        );

        entity.setEmployeeQualificationCountry(
                request.getEmployeeQualificationCountry()
        );

        entity.setEmployeeQualificationStartYear(
                request.getEmployeeQualificationStartYear()
        );

        entity.setEmployeeQualificationCompletionYear(
                request
                        .getEmployeeQualificationCompletionYear()
        );

        entity.setEmployeeQualificationDurationMonths(
                request
                        .getEmployeeQualificationDurationMonths()
        );

        entity.setEmployeeQualificationGrade(
                request.getEmployeeQualificationGrade()
        );

        entity.setEmployeeQualificationPercentage(
                request.getEmployeeQualificationPercentage()
        );

        entity.setEmployeeQualificationCgpa(
                request.getEmployeeQualificationCgpa()
        );

        entity.setEmployeeQualificationCertificateNumber(
                request
                        .getEmployeeQualificationCertificateNumber()
        );

        entity.setEmployeeQualificationRegistrationNumber(
                request
                        .getEmployeeQualificationRegistrationNumber()
        );

        entity.setEmployeeQualificationRemarks(
                request.getEmployeeQualificationRemarks()
        );

        return entity;
    }

    private ErpEmployeeExperience mapExperience(
            EmployeeExperienceRequest request,
            ErpEmployee employee,
            ErpEmployeeExperience entity
    ) {
        entity.setEmployee(employee);

        entity.setEmployeeExperienceCompanyName(
                request.getCompanyName()
        );

        entity.setEmployeeExperienceDesignation(
                request.getJobRole()
        );

        entity.setEmployeeExperienceEmploymentType(
                request.getEmployeeExperienceEmploymentType()
        );

        entity.setEmployeeExperienceStartDate(
                request.getStartDate()
        );

        entity.setEmployeeExperienceEndDate(
                request.getEndDate()
        );

        entity.setEmployeeExperienceCurrentJob(
                request.getEndDate() == null
        );

        entity.setEmployeeExperienceVerified(false);
        entity.setEmployeeExperienceActive(true);

        return entity;
    }

    private ErpEmployeeDocument mapDocument(
            EmployeeDocumentRequest request,
            ErpEmployee employee,
            ErpEmployeeDocument entity
    ) {
        entity.setEmployee(employee);

        if (request.getDocumentType() != null) {
            entity.setEmployeeDocumentType(
                    request.getDocumentType()
            );
        }

        entity.setEmployeeDocumentName(
                request.getDocumentFileName()
        );

        entity.setEmployeeDocumentDescription(
                request.getRemarks()
        );

        entity.setEmployeeDocumentFileName(
                request.getDocumentFileName()
        );

        if (request.getDocumentStoragePath() != null
                && !request.getDocumentStoragePath()
                .isBlank()) {

            entity.setEmployeeDocumentFilePath(
                    request.getDocumentStoragePath()
            );
        }

        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(
            Long employeeId
    ) {
        Integer branchId =
                branchAccessService.getAccessibleBranchId(null);

        ErpEmployee employee = employeeRepository
                .findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found"
                        )
                );

        validateEmployeeBranch(employee, branchId);

        return mapToResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> searchEmployees(
            EmployeeSearchCriteria criteria,
            Pageable pageable
    ) {
        Integer branchId =
                branchAccessService.getAccessibleBranchId(null);

        Specification<ErpEmployee> specification =
                specificationBuilder.build(
                        criteria,
                        branchId
                );

        return employeeRepository
                .findAll(specification, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long employeeId) {
        Integer branchId =
                branchAccessService.getAccessibleBranchId(null);

        ErpEmployee employee = employeeRepository
                .findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found"
                        )
                );

        validateEmployeeBranch(employee, branchId);

        employee.setActive(false);
        employee.setEmploymentStatus(
                EmploymentStatus.TERMINATED
        );

        employeeRepository.save(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeListResponse> getActiveTeachers() {
        Integer branchId =
                branchAccessService.getAccessibleBranchId(null);

        return employeeRepository
                .findActiveEmployeesByCategory(
                        branchId,
                        EmployeeCategory.TEACHING
                );
    }

    private EmployeeResponse mapToResponse(
            ErpEmployee entity
    ) {
        EmployeeResponse response =
                new EmployeeResponse();

        BeanUtils.copyProperties(entity, response);

        StringBuilder fullName = new StringBuilder();

        if (entity.getFirstName() != null) {
            fullName.append(entity.getFirstName());
        }

        if (entity.getMiddleName() != null
                && !entity.getMiddleName().isBlank()) {
            fullName.append(" ")
                    .append(entity.getMiddleName());
        }

        if (entity.getLastName() != null
                && !entity.getLastName().isBlank()) {
            fullName.append(" ")
                    .append(entity.getLastName());
        }

        response.setFullName(fullName.toString().trim());
        response.setEmail(entity.getOfficialEmail());
        response.setPhone(entity.getMobileNo());
        response.setCategory(entity.getEmployeeCategory());
        response.setStatus(entity.getEmploymentStatus());

        if (entity.getDepartment() != null) {
            response.setDepartmentId(
                    entity.getDepartment()
                            .getDepartmentId()
            );
        }

        if (entity.getDesignation() != null) {
            response.setDesignationId(
                    entity.getDesignation()
                            .getDesignationId()
            );
        }

        if (entity.getReportingManager() != null) {
            response.setReportingManagerId(
                    entity.getReportingManager()
                            .getEmployeeId()
            );
        }

        response.setContacts(
                contactRepository
                        .findByEmployee_EmployeeIdAndEmployeeContactActiveTrue(
                                entity.getEmployeeId()
                        )
                        .stream()
                        .map(this::mapContactToDTO)
                        .toList()
        );

        response.setQualifications(
                qualificationRepository
                        .findByEmployee_EmployeeIdAndEmployeeQualificationActiveTrue(
                                entity.getEmployeeId()
                        )
                        .stream()
                        .map(this::mapQualificationToDTO)
                        .toList()
        );

        response.setExperiences(
                experienceRepository
                        .findByEmployee_EmployeeIdAndEmployeeExperienceActiveTrue(
                                entity.getEmployeeId()
                        )
                        .stream()
                        .map(this::mapExperienceToDTO)
                        .toList()
        );

        response.setDocuments(
                documentRepository
                        .findByEmployee_EmployeeIdAndEmployeeDocumentActiveTrue(
                                entity.getEmployeeId()
                        )
                        .stream()
                        .map(this::mapDocumentToDTO)
                        .toList()
        );

        return response;
    }
    private EmployeeContactDTO mapContactToDTO(
            ErpEmployeeContact entity
    ) {
        EmployeeContactDTO dto = new EmployeeContactDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
    private EmployeeQualificationDTO mapQualificationToDTO(
            ErpEmployeeQualification entity
    ) {
        EmployeeQualificationDTO dto =
                new EmployeeQualificationDTO();

        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
    private EmployeeExperienceDTO mapExperienceToDTO(
            ErpEmployeeExperience entity
    ) {
        EmployeeExperienceDTO dto =
                new EmployeeExperienceDTO();

        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
    private EmployeeDocumentDTO mapDocumentToDTO(
            ErpEmployeeDocument entity
    ) {
        EmployeeDocumentDTO dto =
                new EmployeeDocumentDTO();

        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
    private Department getDepartmentForBranch(
            Long departmentId,
            Integer branchId
    ) {
        Department department =
                departmentRepository.findById(departmentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Department not found"
                                )
                        );

        if (department.getBranch() == null
                || !department.getBranch()
                .getBranchId()
                .equals(branchId)) {

            throw new BadRequestException(
                    "Selected department does not "
                            + "belong to your branch."
            );
        }

        if (!Boolean.TRUE.equals(
                department.getActive()
        )) {
            throw new BadRequestException(
                    "Selected department is inactive."
            );
        }

        return department;
    }

    private void validateEmployeeBranch(
            ErpEmployee employee,
            Integer branchId
    ) {
        if (employee.getBranch() == null
                || !employee.getBranch()
                .getBranchId()
                .equals(branchId)) {

            throw new ResourceNotFoundException(
                    "Employee not found in your branch"
            );
        }
    }


    private byte[] decodeBase64(String fileData) {
        String cleanBase64 = fileData;

        if (cleanBase64.contains(",")) {
            cleanBase64 =
                    cleanBase64.substring(
                            cleanBase64.indexOf(',') + 1
                    );
        }

        try {
            return Base64.getDecoder().decode(cleanBase64);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException(
                    "Invalid Base64 file data"
            );
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim()
                .toLowerCase(Locale.ROOT);
    }

    private String buildSchoolPath(
            ErpEmployee employee
    ) {
        Branch branch = employee.getBranch();

        String schoolCode =
                branch.getSchoolCode() != null
                        ? branch.getSchoolCode()
                        : "N";

        String branchName =
                branch.getBranchName() != null
                        ? branch.getBranchName()
                        : "School";

        String branchLocation =
                branch.getBranchLocation() != null
                        ? branch.getBranchLocation()
                        : "Location";

        return schoolCode
                + "-"
                + branchName
                + ","
                + branchLocation;
    }

    private String buildEmployeePath(
            ErpEmployee employee
    ) {
        StringBuilder path = new StringBuilder();

        path.append(employee.getEmployeeNo())
                .append("-")
                .append(employee.getFirstName());

        if (employee.getLastName() != null
                && !employee.getLastName().isBlank()) {
            path.append(" ")
                    .append(employee.getLastName());
        }

        return path.toString();
    }

    private String appendFilePath(
            String remarks,
            String documentPath
    ) {
        String prefix =
                remarks == null || remarks.isBlank()
                        ? ""
                        : remarks.trim() + " ";

        return prefix
                + "[File: "
                + documentPath
                + "]";
    }
    private ErpEmployee getReportingManagerForBranch(
            Long managerId,
            Integer branchId,
            Long currentEmployeeId
    ) {
        if (currentEmployeeId != null
                && currentEmployeeId.equals(managerId)) {

            throw new BadRequestException(
                    "An employee cannot report to themselves."
            );
        }

        ErpEmployee manager = employeeRepository.findById(managerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reporting manager not found"
                        )
                );

        if (manager.getBranch() == null
                || !manager.getBranch()
                .getBranchId()
                .equals(branchId)) {

            throw new BadRequestException(
                    "Reporting manager must belong to the same branch."
            );
        }

        if (!Boolean.TRUE.equals(manager.getActive())
                || manager.getEmploymentStatus()
                == EmploymentStatus.TERMINATED) {

            throw new BadRequestException(
                    "Selected reporting manager is inactive."
            );
        }

        return manager;

    }
}