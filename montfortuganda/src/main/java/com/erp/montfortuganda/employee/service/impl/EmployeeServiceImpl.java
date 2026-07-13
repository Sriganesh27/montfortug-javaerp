package com.erp.montfortuganda.employee.service.impl;

import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.employee.dto.*;
import com.erp.montfortuganda.employee.entity.*;
import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import com.erp.montfortuganda.employee.generator.EmployeeCodeGenerator;
import com.erp.montfortuganda.employee.repository.*;
import com.erp.montfortuganda.employee.service.EmployeeService;
import com.erp.montfortuganda.employee.specification.EmployeeSpecificationBuilder;
import com.erp.montfortuganda.employee.validation.EmployeeValidator;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.school.Branch;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.infrastructure.service.StorageService;
import com.erp.montfortuganda.infrastructure.enums.DocumentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Base64;
import java.util.List;

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
    private final com.erp.montfortuganda.school.repository.DepartmentRepository departmentRepository;
    private final com.erp.montfortuganda.school.repository.DesignationRepository designationRepository;
    private final StorageService storageService;

    private final com.erp.montfortuganda.auth.service.UserService userService;
    private final com.erp.montfortuganda.infrastructure.service.PasswordService passwordService;
    private final com.erp.montfortuganda.notification.service.EmailService emailService;

    private final EmployeeContactRepository contactRepository;
    private final EmployeeQualificationRepository qualificationRepository;
    private final EmployeeDocumentRepository documentRepository;
    private final EmployeeExperienceRepository experienceRepository; // <-- Unused warning removed

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        Integer branchId = branchAccessService.getAccessibleBranchId(null);
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        validator.validateCreation(request, branchId);

        ErpEmployee employee = new ErpEmployee();
        BeanUtils.copyProperties(request, employee);

        if (request.getDepartmentId() != null) {
            employee.setDepartment(departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found")));
        }

        if (request.getDesignationId() != null) {
            employee.setDesignation(designationRepository.findById(request.getDesignationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation not found")));
        }

        employee.setOfficialEmail(request.getOfficialEmail());
        employee.setMobileNo(request.getMobileNo());
        employee.setEmployeeCategory(request.getEmployeeCategory());

        employee.setBranch(branch);
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employee.setActive(true);

        String generatedCode = codeGenerator.generateCode(branchId, request.getEmployeeCategory(), request.getJoiningDate());
        employee.setEmployeeNo(generatedCode);

        ErpEmployee saved = employeeRepository.save(employee);

        syncContacts(request.getContacts(), saved);
        syncQualifications(request.getQualifications(), saved);
        syncExperiences(request.getExperiences(), saved);
        syncDocuments(request.getDocuments(), saved);

        // --- ACCOUNT CREATION ---
        if (request.getAccountRequest() != null && Boolean.TRUE.equals(request.getAccountRequest().getGenerateLogin())) {
            String tempPassword = passwordService.generateSecureTemporaryPassword();
            String username = saved.getEmployeeNo(); // Standardize on employee code

            com.erp.montfortuganda.auth.dto.UserDTO userDTO = new com.erp.montfortuganda.auth.dto.UserDTO();
            userDTO.setUsername(username);
            userDTO.setPassword(tempPassword); // Raw password, UserService hashes it
            userDTO.setAssignedBranchId(branchId);
            
            if (request.getAccountRequest().getRoleId() != null) {
                userDTO.setRoleId(request.getAccountRequest().getRoleId());
            } else {
                // Default fallback based on category if roleId is not provided
                userDTO.setRole("ROLE_EMPLOYEE"); 
            }

            // Create user (this happens in the same transaction)
            userService.createUser(userDTO);

            // Trigger email if requested and if an official email exists
            if (Boolean.TRUE.equals(request.getAccountRequest().getSendEmail()) && saved.getOfficialEmail() != null) {
                emailService.sendEmployeeWelcomeEmail(saved, username, tempPassword);
            }
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long employeeId, EmployeeUpdateRequest request) {
        Integer branchId = branchAccessService.getAccessibleBranchId(null);
        ErpEmployee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (!employee.getBranch().getBranchId().equals(branchId)) {
            throw new ResourceNotFoundException("Employee not found in your branch");
        }

        validator.validateUpdate(employeeId, request, branchId);

        BeanUtils.copyProperties(request, employee, "employeeId", "employeeNo", "branch", "employmentStatus", "active");

        if (request.getDepartmentId() != null) {
            employee.setDepartment(departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found")));
        } else {
            employee.setDepartment(null);
        }

        if (request.getDesignationId() != null) {
            employee.setDesignation(designationRepository.findById(request.getDesignationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation not found")));
        } else {
            employee.setDesignation(null);
        }

        employee.setOfficialEmail(request.getOfficialEmail());
        employee.setMobileNo(request.getMobileNo());
        employee.setEmployeeCategory(request.getEmployeeCategory());

        ErpEmployee updated = employeeRepository.save(employee);

        syncContacts(request.getContacts(), updated);
        syncQualifications(request.getQualifications(), updated);
        syncExperiences(request.getExperiences(), updated);
        syncDocuments(request.getDocuments(), updated);

        return mapToResponse(updated);
    }

    // --- SYNCHRONIZATION METHODS ---

    private void syncContacts(List<EmployeeContactRequest> requests, ErpEmployee employee) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        for (EmployeeContactRequest req : requests) {
            ErpEmployeeContact contact = new ErpEmployeeContact();
            mapContact(req, employee, contact);
            contactRepository.save(contact);
        }
    }

    private void syncQualifications(List<EmployeeQualificationRequest> requests, ErpEmployee employee) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        for (EmployeeQualificationRequest req : requests) {
            ErpEmployeeQualification qual;
            if (req.getEmployeeQualificationId() == null) {
                qual = new ErpEmployeeQualification();
            } else {
                qual = qualificationRepository.findById(req.getEmployeeQualificationId())
                        .orElseThrow(() -> new BadRequestException("Qualification ID not found: " + req.getEmployeeQualificationId()));
            }

            mapQualification(req, employee, qual);
            qual = qualificationRepository.save(qual);

            if (req.getFileData() != null && req.getFileName() != null) {
                try {
                    String base64Image = req.getFileData();
                    if (base64Image.contains(",")) {
                        base64Image = base64Image.split(",")[1];
                    }
                    byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                    String levelName = req.getEmployeeQualificationLevel() != null
                            ? req.getEmployeeQualificationLevel().name().replaceAll("[^a-zA-Z0-9]", "_")
                            : "CERT";
                    String dynamicFileName = levelName + "_" + req.getFileName();

                    MockMultipartFile multipartFile = new MockMultipartFile("file", dynamicFileName, null, imageBytes);

                    String schoolPath = (employee.getBranch().getSchoolCode() != null ? employee.getBranch().getSchoolCode() : "N") + "-" +
                            (employee.getBranch().getBranchName() != null ? employee.getBranch().getBranchName() : "School") + "," +
                            (employee.getBranch().getBranchLocation() != null ? employee.getBranch().getBranchLocation() : "Location");
                    String entityPath = employee.getEmployeeNo() + "-" + employee.getFirstName() + (employee.getLastName() != null ? " " + employee.getLastName() : "");

                    String docPath = storageService.storeEntityDocument(
                            multipartFile,
                            schoolPath,
                            "staff",
                            entityPath,
                            DocumentType.CERTIFICATE
                    );

                    qual.setEmployeeQualificationRemarks((qual.getEmployeeQualificationRemarks() == null ? "" : qual.getEmployeeQualificationRemarks() + " ") + "[File: " + docPath + "]");
                    qualificationRepository.save(qual);
                } catch (Exception e) {
                    log.error("Failed to process qualification file upload for employee: {}", employee.getEmployeeId(), e);
                }
            }
        }
    }

    // --- NEW: EXPERIENCE SYNC LOGIC ---
    private void syncExperiences(List<EmployeeExperienceRequest> requests, ErpEmployee employee) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        for (EmployeeExperienceRequest req : requests) {
            ErpEmployeeExperience exp;

            if (req.getEmployeeExperienceId() == null) {
                exp = new ErpEmployeeExperience();
            } else {
                exp = experienceRepository.findById(req.getEmployeeExperienceId())
                        .orElseThrow(() -> new BadRequestException("Experience ID not found: " + req.getEmployeeExperienceId()));
            }

            mapExperience(req, employee, exp);
            exp = experienceRepository.save(exp);

            if (req.getFileData() != null && req.getFileName() != null) {
                try {
                    String base64Image = req.getFileData();
                    if (base64Image.contains(",")) {
                        base64Image = base64Image.split(",")[1];
                    }
                    byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                    String companyName = req.getCompanyName() != null
                            ? req.getCompanyName().replaceAll("[^a-zA-Z0-9]", "_")
                            : "EXP";
                    String dynamicFileName = companyName + "_" + req.getFileName();

                    MockMultipartFile multipartFile = new MockMultipartFile("file", dynamicFileName, null, imageBytes);

                    String schoolPath = (employee.getBranch().getSchoolCode() != null ? employee.getBranch().getSchoolCode() : "N") + "-" +
                            (employee.getBranch().getBranchName() != null ? employee.getBranch().getBranchName() : "School") + "," +
                            (employee.getBranch().getBranchLocation() != null ? employee.getBranch().getBranchLocation() : "Location");
                    String entityPath = employee.getEmployeeNo() + "-" + employee.getFirstName() + (employee.getLastName() != null ? " " + employee.getLastName() : "");

                    String docPath = storageService.storeEntityDocument(
                            multipartFile,
                            schoolPath,
                            "staff",
                            entityPath,
                            DocumentType.OTHER
                    );

                    // If your ErpEmployeeExperience doesn't have a remarks field,
                    // replace this with whatever field you use to store the file path!
                    exp.setEmployeeExperienceRemarks((exp.getEmployeeExperienceRemarks() == null ? "" : exp.getEmployeeExperienceRemarks() + " ") + "[File: " + docPath + "]");
                    experienceRepository.save(exp);
                } catch (Exception e) {
                    log.error("Failed to process experience file upload for employee: {}", employee.getEmployeeId(), e);
                }
            }
        }
    }

    private void syncDocuments(List<EmployeeDocumentRequest> requests, ErpEmployee employee) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        for (EmployeeDocumentRequest req : requests) {
            ErpEmployeeDocument doc;
            if (req.getEmployeeDocumentId() == null) {
                doc = new ErpEmployeeDocument();
            } else {
                doc = documentRepository.findById(req.getEmployeeDocumentId())
                        .orElseThrow(() -> new BadRequestException("Document ID not found: " + req.getEmployeeDocumentId()));
            }

            mapDocument(req, employee, doc);
            doc = documentRepository.save(doc);

            if (req.getFileData() != null && req.getFileName() != null) {
                try {
                    String base64Image = req.getFileData();
                    if (base64Image.contains(",")) {
                        base64Image = base64Image.split(",")[1];
                    }
                    byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                    String typeName = req.getDocumentType() != null ? req.getDocumentType().name() : "DOC";
                    String dynamicFileName = typeName + "_" + req.getFileName();

                    MockMultipartFile multipartFile = new MockMultipartFile("file", dynamicFileName, null, imageBytes);

                    String schoolPath = (employee.getBranch().getSchoolCode() != null ? employee.getBranch().getSchoolCode() : "N") + "-" +
                            (employee.getBranch().getBranchName() != null ? employee.getBranch().getBranchName() : "School") + "," +
                            (employee.getBranch().getBranchLocation() != null ? employee.getBranch().getBranchLocation() : "Location");
                    String entityPath = employee.getEmployeeNo() + "-" + employee.getFirstName() + (employee.getLastName() != null ? " " + employee.getLastName() : "");

                    String docPath = storageService.storeEntityDocument(
                            multipartFile,
                            schoolPath,
                            "staff",
                            entityPath,
                            DocumentType.OTHER
                    );

                    doc.setEmployeeDocumentFilePath(docPath);
                    documentRepository.save(doc);
                } catch (Exception e) {
                    log.error("Failed to process document file upload for employee: {}", employee.getEmployeeId(), e);
                }
            }
        }
    }

    // --- MAPPING METHODS ---

    private ErpEmployeeContact mapContact(EmployeeContactRequest request, ErpEmployee employee, ErpEmployeeContact entity) {
        entity.setEmployee(employee);
        entity.setEmployeeContactName(request.getEmployeeContactName());
        entity.setEmployeeContactRelationship(request.getEmployeeContactRelationship());

        if (request.getEmployeeContactType() != null) {
            entity.setEmployeeContactType(request.getEmployeeContactType());
        }

        entity.setEmployeeContactMobile(request.getEmployeeContactMobile());
        entity.setEmployeeContactAlternateMobile(request.getEmployeeContactAlternateMobile());
        entity.setEmployeeContactEmail(request.getEmployeeContactEmail());
        entity.setEmployeeContactCountry(request.getEmployeeContactCountry());
        entity.setEmployeeContactState(request.getEmployeeContactState());
        entity.setEmployeeContactDistrict(request.getEmployeeContactDistrict());
        entity.setEmployeeContactVillage(request.getEmployeeContactVillage());
        entity.setEmployeeContactStreet(request.getEmployeeContactStreet());
        entity.setEmployeeContactPostalCode(request.getEmployeeContactPostalCode());
        entity.setEmployeeContactOccupation(request.getEmployeeContactOccupation());
        entity.setEmployeeContactWorkplace(request.getEmployeeContactWorkplace());

        if (request.getEmployeeContactIsPrimary() != null) {
            entity.setEmployeeContactIsPrimary(request.getEmployeeContactIsPrimary());
        }
        if (request.getEmployeeContactIsEmergency() != null) {
            entity.setEmployeeContactIsEmergency(request.getEmployeeContactIsEmergency());
        }

        entity.setEmployeeContactRemarks(request.getEmployeeContactRemarks());
        return entity;
    }

    private ErpEmployeeQualification mapQualification(EmployeeQualificationRequest request, ErpEmployee employee, ErpEmployeeQualification entity) {
        entity.setEmployee(employee);
        entity.setEmployeeQualificationLevel(request.getEmployeeQualificationLevel());
        entity.setEmployeeQualificationName(request.getEmployeeQualificationName());
        entity.setEmployeeQualificationSpecialization(request.getEmployeeQualificationSpecialization());
        entity.setEmployeeQualificationInstitutionName(request.getEmployeeQualificationInstitutionName());
        entity.setEmployeeQualificationBoardUniversity(request.getEmployeeQualificationBoardUniversity());
        entity.setEmployeeQualificationCountry(request.getEmployeeQualificationCountry());
        entity.setEmployeeQualificationStartYear(request.getEmployeeQualificationStartYear());
        entity.setEmployeeQualificationCompletionYear(request.getEmployeeQualificationCompletionYear());
        entity.setEmployeeQualificationDurationMonths(request.getEmployeeQualificationDurationMonths());
        entity.setEmployeeQualificationGrade(request.getEmployeeQualificationGrade());
        entity.setEmployeeQualificationPercentage(request.getEmployeeQualificationPercentage());
        entity.setEmployeeQualificationCgpa(request.getEmployeeQualificationCgpa());
        entity.setEmployeeQualificationCertificateNumber(request.getEmployeeQualificationCertificateNumber());
        entity.setEmployeeQualificationRegistrationNumber(request.getEmployeeQualificationRegistrationNumber());
        entity.setEmployeeQualificationRemarks(request.getEmployeeQualificationRemarks());
        return entity;
    }

    // --- NEW: EXPERIENCE MAPPING ---
    private ErpEmployeeExperience mapExperience(EmployeeExperienceRequest request, ErpEmployee employee, ErpEmployeeExperience entity) {
        entity.setEmployee(employee);
        entity.setEmployeeExperienceCompanyName(request.getCompanyName());
        // Uncomment/Modify these lines if your entity has these fields:
        // entity.setEmployeeExperienceDesignation(request.getJobRole());
        // entity.setEmployeeExperienceStartDate(request.getStartDate());
        // entity.setEmployeeExperienceEndDate(request.getEndDate());
        return entity;
    }

    private ErpEmployeeDocument mapDocument(EmployeeDocumentRequest request, ErpEmployee employee, ErpEmployeeDocument entity) {
        entity.setEmployee(employee);

        if (request.getDocumentType() != null) {
            entity.setEmployeeDocumentType(request.getDocumentType());
        }

        entity.setEmployeeDocumentName(request.getDocumentFileName());
        entity.setEmployeeDocumentDescription(request.getRemarks());
        entity.setEmployeeDocumentFileName(request.getDocumentFileName());
        entity.setEmployeeDocumentFilePath(request.getDocumentStoragePath());
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long employeeId) {
        Integer branchId = branchAccessService.getAccessibleBranchId(null);
        ErpEmployee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (!employee.getBranch().getBranchId().equals(branchId)) {
            throw new ResourceNotFoundException("Employee not found in your branch");
        }

        return mapToResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> searchEmployees(EmployeeSearchCriteria criteria, Pageable pageable) {
        Integer branchId = branchAccessService.getAccessibleBranchId(null);
        Specification<ErpEmployee> spec = specificationBuilder.build(criteria, branchId);

        return employeeRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long employeeId) {
        Integer branchId = branchAccessService.getAccessibleBranchId(null);
        ErpEmployee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (!employee.getBranch().getBranchId().equals(branchId)) {
            throw new ResourceNotFoundException("Employee not found in your branch");
        }

        employee.setActive(false);
        employee.setEmploymentStatus(EmploymentStatus.TERMINATED);
        employeeRepository.save(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<com.erp.montfortuganda.employee.dto.response.EmployeeListResponse> getActiveTeachers() {
        Integer branchId = branchAccessService.getAccessibleBranchId(null);
        return employeeRepository.findActiveEmployeesByCategory(
                branchId,
                com.erp.montfortuganda.employee.enums.EmployeeCategory.TEACHING
        );
    }

    private EmployeeResponse mapToResponse(ErpEmployee entity) {
        EmployeeResponse dto = new EmployeeResponse();
        BeanUtils.copyProperties(entity, dto);

        dto.setEmail(entity.getOfficialEmail());
        dto.setPhone(entity.getMobileNo());
        dto.setCategory(entity.getEmployeeCategory());
        dto.setStatus(entity.getEmploymentStatus());

        if (entity.getDepartment() != null) dto.setDepartmentId(entity.getDepartment().getDepartmentId());
        if (entity.getDesignation() != null) dto.setDesignationId(entity.getDesignation().getDesignationId());
        if (entity.getReportingManager() != null) dto.setReportingManagerId(entity.getReportingManager().getEmployeeId());

        return dto;
    }
}