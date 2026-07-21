package com.erp.montfortuganda.employee.mapper;

import com.erp.montfortuganda.auth.entity.ErpRole;
import com.erp.montfortuganda.auth.entity.ErpUserRole;
import com.erp.montfortuganda.auth.entity.User;
import com.erp.montfortuganda.employee.dto.request.EmployeeContactRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeDocumentRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeExperienceRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeQualificationRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeRegistrationRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeUpdateRequest;
import com.erp.montfortuganda.employee.dto.response.EmployeeContactResponse;
import com.erp.montfortuganda.employee.dto.response.EmployeeDetailResponse;
import com.erp.montfortuganda.employee.dto.response.EmployeeDocumentResponse;
import com.erp.montfortuganda.employee.dto.response.EmployeeExperienceResponse;
import com.erp.montfortuganda.employee.dto.response.EmployeePageResponse;
import com.erp.montfortuganda.employee.dto.response.EmployeeQualificationResponse;
import com.erp.montfortuganda.employee.dto.response.EmployeeSummaryResponse;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import com.erp.montfortuganda.employee.entity.ErpEmployeeContact;
import com.erp.montfortuganda.employee.entity.ErpEmployeeDocument;
import com.erp.montfortuganda.employee.entity.ErpEmployeeExperience;
import com.erp.montfortuganda.employee.entity.ErpEmployeeQualification;
import com.erp.montfortuganda.employee.enums.EmployeeDocumentType;
import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.Department;
import com.erp.montfortuganda.school.entity.Designation;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Maps Employee request DTOs to persistence entities and persistence entities
 * to API response DTOs.
 *
 * Security-sensitive values are intentionally not mapped from requests:
 * branch ownership, Employee IDs/numbers, User IDs, private storage paths,
 * verification ownership/timestamps, audit fields, active master-record state
 * and entity versions are controlled by services and JPA.
 */
@Component
public class EmployeeMapper {

    // =====================================================================
    // EMPLOYEE REQUEST -> ENTITY
    // =====================================================================

    public ErpEmployee toNewEmployee(
            EmployeeRegistrationRequest request,
            Branch branch,
            Department department,
            Designation designation,
            ErpEmployee reportingManager,
            String employeeNo
    ) {
        Objects.requireNonNull(
                request,
                "Employee registration request is required."
        );
        Objects.requireNonNull(
                branch,
                "Employee branch is required."
        );
        Objects.requireNonNull(
                employeeNo,
                "Generated Employee number is required."
        );

        ErpEmployee employee =
                new ErpEmployee();

        employee.setEmployeeNo(
                trimRequired(employeeNo)
        );
        employee.setBranch(branch);
        employee.setDepartment(department);
        employee.setDesignation(designation);
        employee.setReportingManager(reportingManager);

        mapRegistrationFields(
                request,
                employee
        );

        employee.setLoginEnabled(false);
        employee.setActive(true);
        employee.setEmploymentEndDate(null);

        return employee;
    }

    public void updateEmployee(
            EmployeeUpdateRequest request,
            ErpEmployee employee,
            Department department,
            Designation designation,
            ErpEmployee reportingManager
    ) {
        Objects.requireNonNull(
                request,
                "Employee update request is required."
        );
        Objects.requireNonNull(
                employee,
                "Employee entity is required."
        );

        employee.setDepartment(department);
        employee.setDesignation(designation);
        employee.setReportingManager(reportingManager);

        employee.setTitle(
                trimToNull(request.title())
        );
        employee.setFirstName(
                trimRequired(request.firstName())
        );
        employee.setMiddleName(
                trimToNull(request.middleName())
        );
        employee.setLastName(
                trimRequired(request.lastName())
        );
        employee.setFullName(
                buildFullName(
                        request.firstName(),
                        request.middleName(),
                        request.lastName()
                )
        );

        employee.setGender(request.gender());
        employee.setDateOfBirth(request.dateOfBirth());
        employee.setMaritalStatus(
                trimToNull(request.maritalStatus())
        );
        employee.setBloodGroup(
                uppercaseToNull(request.bloodGroup())
        );
        employee.setReligion(
                trimToNull(request.religion())
        );
        employee.setSubReligion(
                trimToNull(request.subReligion())
        );

        employee.setOfficialEmail(
                normalizeEmail(request.officialEmail())
        );
        employee.setPersonalEmail(
                normalizeEmail(request.personalEmail())
        );
        employee.setMobileNo(
                trimRequired(request.mobileNo())
        );
        employee.setAlternateMobile(
                trimToNull(request.alternateMobile())
        );

        employee.setEmployeeCategory(
                request.employeeCategory()
        );
        employee.setEmployeeType(
                request.employeeType()
        );
        employee.setEmploymentMode(
                request.employmentMode()
        );
        employee.setEmploymentStatus(
                request.employmentStatus()
        );

        employee.setJoiningDate(
                request.joiningDate()
        );
        employee.setProbationEndDate(
                request.probationEndDate()
        );
        employee.setConfirmationDate(
                request.confirmationDate()
        );
        employee.setRetirementDate(
                request.retirementDate()
        );
        employee.setResignationDate(
                request.resignationDate()
        );
        employee.setTerminationDate(
                request.terminationDate()
        );
        employee.setEmploymentEndDate(
                resolveEmploymentEndDate(
                        request.employmentStatus(),
                        request.retirementDate(),
                        request.resignationDate(),
                        request.terminationDate()
                )
        );
        employee.setExitReason(
                trimToNull(request.exitReason())
        );

        employee.setNationality(
                trimToNull(request.nationality())
        );
        employee.setNationalId(
                trimToNull(request.nationalId())
        );
        employee.setTinNumber(
                trimToNull(request.tinNumber())
        );
        employee.setPassportNo(
                trimToNull(request.passportNo())
        );
        employee.setPassportExpiryDate(
                request.passportExpiryDate()
        );
        employee.setWorkPermitNumber(
                trimToNull(request.workPermitNumber())
        );
        employee.setWorkPermitExpiryDate(
                request.workPermitExpiryDate()
        );

        employee.setAddressCountry(
                trimToNull(request.addressCountry())
        );
        employee.setAddressState(
                trimToNull(request.addressState())
        );
        employee.setAddressDistrict(
                trimToNull(request.addressDistrict())
        );
        employee.setAddressCounty(
                trimToNull(request.addressCounty())
        );
        employee.setAddressSubCounty(
                trimToNull(request.addressSubCounty())
        );
        employee.setAddressParish(
                trimToNull(request.addressParish())
        );
        employee.setAddressVillage(
                trimToNull(request.addressVillage())
        );
        employee.setAddressStreet(
                trimToNull(request.addressStreet())
        );
        employee.setPostalCode(
                trimToNull(request.postalCode())
        );

        employee.setSkills(
                trimToNull(request.skills())
        );
        employee.setLanguagesSpoken(
                trimToNull(request.languagesSpoken())
        );
        employee.setEmployeeRemarks(
                trimToNull(request.employeeRemarks())
        );

        /*
         * Master active state is derived from employment status instead of
         * accepting an unrestricted active flag from the browser.
         */
        employee.setActive(
                !isFinalEmploymentStatus(
                        request.employmentStatus()
                )
        );
    }

    private void mapRegistrationFields(
            EmployeeRegistrationRequest request,
            ErpEmployee employee
    ) {
        employee.setTitle(
                trimToNull(request.title())
        );
        employee.setFirstName(
                trimRequired(request.firstName())
        );
        employee.setMiddleName(
                trimToNull(request.middleName())
        );
        employee.setLastName(
                trimRequired(request.lastName())
        );
        employee.setFullName(
                buildFullName(
                        request.firstName(),
                        request.middleName(),
                        request.lastName()
                )
        );

        employee.setGender(request.gender());
        employee.setDateOfBirth(request.dateOfBirth());
        employee.setMaritalStatus(
                trimToNull(request.maritalStatus())
        );
        employee.setBloodGroup(
                uppercaseToNull(request.bloodGroup())
        );
        employee.setReligion(
                trimToNull(request.religion())
        );
        employee.setSubReligion(
                trimToNull(request.subReligion())
        );

        employee.setOfficialEmail(
                normalizeEmail(request.officialEmail())
        );
        employee.setPersonalEmail(
                normalizeEmail(request.personalEmail())
        );
        employee.setMobileNo(
                trimRequired(request.mobileNo())
        );
        employee.setAlternateMobile(
                trimToNull(request.alternateMobile())
        );

        employee.setEmployeeCategory(
                request.employeeCategory()
        );
        employee.setEmployeeType(
                request.employeeType()
        );
        employee.setEmploymentMode(
                request.employmentMode()
        );
        employee.setEmploymentStatus(
                request.employmentStatus()
        );

        employee.setJoiningDate(
                request.joiningDate()
        );
        employee.setProbationEndDate(
                request.probationEndDate()
        );
        employee.setConfirmationDate(
                request.confirmationDate()
        );
        employee.setRetirementDate(
                request.retirementDate()
        );

        employee.setNationality(
                trimToNull(request.nationality())
        );
        employee.setNationalId(
                trimToNull(request.nationalId())
        );
        employee.setTinNumber(
                trimToNull(request.tinNumber())
        );
        employee.setPassportNo(
                trimToNull(request.passportNo())
        );
        employee.setPassportExpiryDate(
                request.passportExpiryDate()
        );
        employee.setWorkPermitNumber(
                trimToNull(request.workPermitNumber())
        );
        employee.setWorkPermitExpiryDate(
                request.workPermitExpiryDate()
        );

        employee.setAddressCountry(
                trimToNull(request.addressCountry())
        );
        employee.setAddressState(
                trimToNull(request.addressState())
        );
        employee.setAddressDistrict(
                trimToNull(request.addressDistrict())
        );
        employee.setAddressCounty(
                trimToNull(request.addressCounty())
        );
        employee.setAddressSubCounty(
                trimToNull(request.addressSubCounty())
        );
        employee.setAddressParish(
                trimToNull(request.addressParish())
        );
        employee.setAddressVillage(
                trimToNull(request.addressVillage())
        );
        employee.setAddressStreet(
                trimToNull(request.addressStreet())
        );
        employee.setPostalCode(
                trimToNull(request.postalCode())
        );

        employee.setSkills(
                trimToNull(request.skills())
        );
        employee.setLanguagesSpoken(
                trimToNull(request.languagesSpoken())
        );
        employee.setEmployeeRemarks(
                trimToNull(request.employeeRemarks())
        );
    }

    // =====================================================================
    // CONTACT REQUEST -> ENTITY
    // =====================================================================

    public List<ErpEmployeeContact> toNewContacts(
            List<EmployeeContactRequest> requests,
            ErpEmployee employee
    ) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        List<ErpEmployeeContact> contacts =
                new ArrayList<>();

        for (EmployeeContactRequest request : requests) {
            if (request != null) {
                contacts.add(
                        toNewContact(
                                request,
                                employee
                        )
                );
            }
        }

        return contacts;
    }

    public ErpEmployeeContact toNewContact(
            EmployeeContactRequest request,
            ErpEmployee employee
    ) {
        Objects.requireNonNull(
                request,
                "Employee contact request is required."
        );
        Objects.requireNonNull(
                employee,
                "Employee entity is required."
        );

        ErpEmployeeContact contact =
                new ErpEmployeeContact();

        contact.setEmployee(employee);
        updateContact(
                request,
                contact
        );

        return contact;
    }

    public void updateContact(
            EmployeeContactRequest request,
            ErpEmployeeContact contact
    ) {
        Objects.requireNonNull(
                request,
                "Employee contact request is required."
        );
        Objects.requireNonNull(
                contact,
                "Employee contact entity is required."
        );

        contact.setEmployeeContactName(
                trimRequired(
                        request.employeeContactName()
                )
        );
        contact.setEmployeeContactRelationship(
                request.employeeContactRelationship()
        );
        contact.setEmployeeContactType(
                request.employeeContactType()
        );
        contact.setEmployeeContactMobile(
                trimRequired(
                        request.employeeContactMobile()
                )
        );
        contact.setEmployeeContactAlternateMobile(
                trimToNull(
                        request.employeeContactAlternateMobile()
                )
        );
        contact.setEmployeeContactEmail(
                normalizeEmail(
                        request.employeeContactEmail()
                )
        );
        contact.setEmployeeContactCountry(
                trimToNull(
                        request.employeeContactCountry()
                )
        );
        contact.setEmployeeContactState(
                trimToNull(
                        request.employeeContactState()
                )
        );
        contact.setEmployeeContactDistrict(
                trimToNull(
                        request.employeeContactDistrict()
                )
        );
        contact.setEmployeeContactVillage(
                trimToNull(
                        request.employeeContactVillage()
                )
        );
        contact.setEmployeeContactStreet(
                trimToNull(
                        request.employeeContactStreet()
                )
        );
        contact.setEmployeeContactPostalCode(
                trimToNull(
                        request.employeeContactPostalCode()
                )
        );
        contact.setEmployeeContactOccupation(
                trimToNull(
                        request.employeeContactOccupation()
                )
        );
        contact.setEmployeeContactWorkplace(
                trimToNull(
                        request.employeeContactWorkplace()
                )
        );
        contact.setEmployeeContactIsPrimary(
                Boolean.TRUE.equals(
                        request.employeeContactIsPrimary()
                )
        );
        contact.setEmployeeContactIsEmergency(
                Boolean.TRUE.equals(
                        request.employeeContactIsEmergency()
                )
        );
        contact.setEmployeeContactActive(
                Boolean.TRUE.equals(
                        request.employeeContactActive()
                )
        );
        contact.setEmployeeContactRemarks(
                trimToNull(
                        request.employeeContactRemarks()
                )
        );
    }

    // =====================================================================
    // QUALIFICATION REQUEST -> ENTITY
    // =====================================================================

    public List<ErpEmployeeQualification> toNewQualifications(
            List<EmployeeQualificationRequest> requests,
            ErpEmployee employee
    ) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        List<ErpEmployeeQualification> qualifications =
                new ArrayList<>();

        for (EmployeeQualificationRequest request : requests) {
            if (request != null) {
                qualifications.add(
                        toNewQualification(
                                request,
                                employee
                        )
                );
            }
        }

        return qualifications;
    }

    public ErpEmployeeQualification toNewQualification(
            EmployeeQualificationRequest request,
            ErpEmployee employee
    ) {
        Objects.requireNonNull(
                request,
                "Employee qualification request is required."
        );
        Objects.requireNonNull(
                employee,
                "Employee entity is required."
        );

        ErpEmployeeQualification qualification =
                new ErpEmployeeQualification();

        qualification.setEmployee(employee);
        qualification.setEmployeeQualificationVerified(false);
        qualification.setEmployeeQualificationVerifiedBy(null);
        qualification.setEmployeeQualificationVerifiedAt(null);

        updateQualification(
                request,
                qualification
        );

        return qualification;
    }

    public void updateQualification(
            EmployeeQualificationRequest request,
            ErpEmployeeQualification qualification
    ) {
        Objects.requireNonNull(
                request,
                "Employee qualification request is required."
        );
        Objects.requireNonNull(
                qualification,
                "Employee qualification entity is required."
        );

        qualification.setEmployeeQualificationLevel(
                request.employeeQualificationLevel()
        );
        qualification.setCustomLevel(
                trimToNull(request.customLevel())
        );
        qualification.setEmployeeQualificationName(
                trimRequired(
                        request.employeeQualificationName()
                )
        );
        qualification.setEmployeeQualificationSpecialization(
                trimToNull(
                        request.employeeQualificationSpecialization()
                )
        );
        qualification.setEmployeeQualificationInstitutionName(
                trimRequired(
                        request.employeeQualificationInstitutionName()
                )
        );
        qualification.setQualificationGrade(
                trimToNull(request.qualificationGrade())
        );
        qualification.setEmployeeQualificationBoardUniversity(
                trimToNull(
                        request.employeeQualificationBoardUniversity()
                )
        );
        qualification.setEmployeeQualificationCountry(
                trimToNull(
                        request.employeeQualificationCountry()
                )
        );
        qualification.setEmployeeQualificationStartYear(
                request.employeeQualificationStartYear()
        );
        qualification.setEmployeeQualificationCompletionYear(
                request.employeeQualificationCompletionYear()
        );
        qualification.setEmployeeQualificationDurationMonths(
                resolveQualificationDurationMonths(
                        request
                )
        );
        qualification.setEmployeeQualificationGrade(
                trimToNull(
                        request.employeeQualificationGrade()
                )
        );
        qualification.setEmployeeQualificationPercentage(
                request.employeeQualificationPercentage()
        );
        qualification.setEmployeeQualificationCgpa(
                request.employeeQualificationCgpa()
        );
        qualification.setEmployeeQualificationCertificateNumber(
                trimToNull(
                        request.employeeQualificationCertificateNumber()
                )
        );
        qualification.setEmployeeQualificationRegistrationNumber(
                trimToNull(
                        request.employeeQualificationRegistrationNumber()
                )
        );
        qualification.setEmployeeQualificationRemarks(
                trimToNull(
                        request.employeeQualificationRemarks()
                )
        );
        qualification.setEmployeeQualificationActive(
                Boolean.TRUE.equals(
                        request.employeeQualificationActive()
                )
        );
    }

    // =====================================================================
    // EXPERIENCE REQUEST -> ENTITY
    // =====================================================================

    public List<ErpEmployeeExperience> toNewExperiences(
            List<EmployeeExperienceRequest> requests,
            ErpEmployee employee
    ) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        List<ErpEmployeeExperience> experiences =
                new ArrayList<>();

        for (EmployeeExperienceRequest request : requests) {
            if (request != null) {
                experiences.add(
                        toNewExperience(
                                request,
                                employee
                        )
                );
            }
        }

        return experiences;
    }

    public ErpEmployeeExperience toNewExperience(
            EmployeeExperienceRequest request,
            ErpEmployee employee
    ) {
        Objects.requireNonNull(
                request,
                "Employee experience request is required."
        );
        Objects.requireNonNull(
                employee,
                "Employee entity is required."
        );

        ErpEmployeeExperience experience =
                new ErpEmployeeExperience();

        experience.setEmployee(employee);
        experience.setEmployeeExperienceVerified(false);
        experience.setEmployeeExperienceVerifiedBy(null);
        experience.setEmployeeExperienceVerifiedAt(null);

        updateExperience(
                request,
                experience
        );

        return experience;
    }

    public void updateExperience(
            EmployeeExperienceRequest request,
            ErpEmployeeExperience experience
    ) {
        Objects.requireNonNull(
                request,
                "Employee experience request is required."
        );
        Objects.requireNonNull(
                experience,
                "Employee experience entity is required."
        );

        /*
         * The schema currently stores the same concept in two columns. The
         * browser supplies one field; both entity fields receive that value.
         */
        experience.setEmployeeExperienceType(
                request.employeeExperienceEmploymentType()
        );
        experience.setEmployeeExperienceEmploymentType(
                request.employeeExperienceEmploymentType()
        );

        experience.setEmployeeExperienceCompanyName(
                trimRequired(
                        request.employeeExperienceCompanyName()
                )
        );
        experience.setEmployeeExperienceCompanyAddress(
                trimToNull(
                        request.employeeExperienceCompanyAddress()
                )
        );
        experience.setEmployeeExperienceCompanyCountry(
                trimToNull(
                        request.employeeExperienceCompanyCountry()
                )
        );
        experience.setEmployeeExperienceCompanyState(
                trimToNull(
                        request.employeeExperienceCompanyState()
                )
        );
        experience.setEmployeeExperienceCompanyDistrict(
                trimToNull(
                        request.employeeExperienceCompanyDistrict()
                )
        );
        experience.setEmployeeExperienceDesignation(
                trimToNull(
                        request.employeeExperienceDesignation()
                )
        );
        experience.setEmployeeExperienceDepartment(
                trimToNull(
                        request.employeeExperienceDepartment()
                )
        );
        experience.setEmployeeExperienceStartDate(
                request.employeeExperienceStartDate()
        );
        experience.setEmployeeExperienceEndDate(
                request.employeeExperienceEndDate()
        );
        experience.setEmployeeExperienceCurrentJob(
                Boolean.TRUE.equals(
                        request.employeeExperienceCurrentJob()
                )
        );
        experience.setEmployeeExperienceTotalMonths(
                calculateExperienceMonths(
                        request.employeeExperienceStartDate(),
                        request.employeeExperienceEndDate(),
                        request.employeeExperienceCurrentJob()
                )
        );
        experience.setEmployeeExperienceSalary(
                request.employeeExperienceSalary()
        );
        experience.setEmployeeExperienceCurrency(
                uppercaseToNull(
                        request.employeeExperienceCurrency()
                )
        );
        experience.setEmployeeExperienceSupervisorName(
                trimToNull(
                        request.employeeExperienceSupervisorName()
                )
        );
        experience.setEmployeeExperienceSupervisorContact(
                trimToNull(
                        request.employeeExperienceSupervisorContact()
                )
        );
        experience.setEmployeeExperienceReasonForLeaving(
                trimToNull(
                        request.employeeExperienceReasonForLeaving()
                )
        );
        experience.setEmployeeExperienceResponsibilities(
                trimToNull(
                        request.employeeExperienceResponsibilities()
                )
        );
        experience.setEmployeeExperienceAchievements(
                trimToNull(
                        request.employeeExperienceAchievements()
                )
        );
        experience.setEmployeeExperienceActive(
                request.employeeExperienceActive() == null
                        || Boolean.TRUE.equals(
                        request.employeeExperienceActive()
                )
        );
        experience.setEmployeeExperienceRemarks(
                trimToNull(
                        request.employeeExperienceRemarks()
                )
        );
    }

    // =====================================================================
    // DOCUMENT REQUEST -> ENTITY
    // =====================================================================

    public List<ErpEmployeeDocument> toNewDocuments(
            List<EmployeeDocumentRequest> requests,
            ErpEmployee employee
    ) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        List<ErpEmployeeDocument> documents =
                new ArrayList<>();

        for (EmployeeDocumentRequest request : requests) {
            if (request != null) {
                documents.add(
                        toNewDocument(
                                request,
                                employee
                        )
                );
            }
        }

        return documents;
    }

    public ErpEmployeeDocument toNewDocument(
            EmployeeDocumentRequest request,
            ErpEmployee employee
    ) {
        Objects.requireNonNull(
                request,
                "Employee document request is required."
        );
        Objects.requireNonNull(
                employee,
                "Employee entity is required."
        );

        ErpEmployeeDocument document =
                new ErpEmployeeDocument();

        document.setEmployee(employee);
        document.setEmployeeDocumentVerified(false);
        document.setEmployeeDocumentVerifiedBy(null);
        document.setEmployeeDocumentVerifiedAt(null);

        updateDocument(
                request,
                document
        );

        return document;
    }

    public void updateDocument(
            EmployeeDocumentRequest request,
            ErpEmployeeDocument document
    ) {
        Objects.requireNonNull(
                request,
                "Employee document request is required."
        );
        Objects.requireNonNull(
                document,
                "Employee document entity is required."
        );

        document.setEmployeeDocumentType(
                request.employeeDocumentType()
        );
        document.setEmployeeDocumentName(
                resolveDocumentName(request)
        );
        document.setEmployeeDocumentDescription(
                trimToNull(
                        request.employeeDocumentDescription()
                )
        );
        document.setEmployeeDocumentIssueDate(
                request.employeeDocumentIssueDate()
        );
        document.setEmployeeDocumentExpiryDate(
                request.employeeDocumentExpiryDate()
        );
        document.setEmployeeDocumentIsMandatory(
                Boolean.TRUE.equals(
                        request.employeeDocumentIsMandatory()
                )
        );
        document.setEmployeeDocumentActive(
                request.employeeDocumentActive() == null
                        || Boolean.TRUE.equals(
                        request.employeeDocumentActive()
                )
        );
        document.setEmployeeDocumentRemarks(
                trimToNull(
                        request.employeeDocumentRemarks()
                )
        );
    }

    // =====================================================================
    // ENTITY -> SUMMARY / PAGE RESPONSE
    // =====================================================================

    public EmployeeSummaryResponse toSummaryResponse(
            ErpEmployee employee,
            String profilePhotoUrl
    ) {
        Objects.requireNonNull(
                employee,
                "Employee entity is required."
        );

        Branch branch =
                employee.getBranch();
        Department department =
                employee.getDepartment();
        Designation designation =
                employee.getDesignation();
        ErpEmployee reportingManager =
                employee.getReportingManager();
        User user =
                employee.getUser();

        boolean profilePhotoAvailable =
                hasText(employee.getProfilePhoto());

        return new EmployeeSummaryResponse(
                employee.getEmployeeId(),
                employee.getEmployeeNo(),
                employee.getTitle(),
                employee.getFullName(),
                employee.getGender(),

                branch == null
                        ? null
                        : branch.getBranchId(),
                branch == null
                        ? null
                        : branch.getBranchName(),
                branch == null
                        ? null
                        : branch.getSchoolCode(),

                department == null
                        ? null
                        : department.getDepartmentId(),
                department == null
                        ? null
                        : department.getDepartmentCode(),
                department == null
                        ? null
                        : department.getDepartmentName(),

                designation == null
                        ? null
                        : designation.getDesignationId(),
                designation == null
                        ? null
                        : designation.getDesignationCode(),
                designation == null
                        ? null
                        : designation.getDesignationName(),

                reportingManager == null
                        ? null
                        : reportingManager.getEmployeeId(),
                reportingManager == null
                        ? null
                        : reportingManager.getEmployeeNo(),
                reportingManager == null
                        ? null
                        : reportingManager.getFullName(),

                employee.getEmployeeCategory(),
                employee.getEmployeeType(),
                employee.getEmploymentMode(),
                employee.getEmploymentStatus(),
                employee.getJoiningDate(),
                employee.getOfficialEmail(),
                employee.getMobileNo(),
                Boolean.TRUE.equals(
                        employee.getLoginEnabled()
                ),

                user == null
                        ? null
                        : user.getId(),
                user == null
                        ? null
                        : user.getUsername(),
                resolvePrimaryRole(user),

                profilePhotoAvailable,
                profilePhotoAvailable
                        ? trimToNull(profilePhotoUrl)
                        : null,

                Boolean.TRUE.equals(
                        employee.getActive()
                ),
                employee.getVersion(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }

    public EmployeePageResponse toPageResponse(
            Page<ErpEmployee> page,
            Function<ErpEmployee, String> profilePhotoUrlResolver,
            String sortBy,
            String sortDirection
    ) {
        Objects.requireNonNull(
                page,
                "Employee page is required."
        );

        List<EmployeeSummaryResponse> content =
                page.getContent()
                        .stream()
                        .map(
                                employee -> toSummaryResponse(
                                        employee,
                                        profilePhotoUrlResolver == null
                                                ? null
                                                : profilePhotoUrlResolver.apply(
                                                employee
                                        )
                                )
                        )
                        .toList();

        return new EmployeePageResponse(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumberOfElements(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty(),
                trimToNull(sortBy),
                normalizeSortDirection(
                        sortDirection
                )
        );
    }

    // =====================================================================
    // ENTITY -> DETAIL RESPONSE
    // =====================================================================

    public EmployeeDetailResponse toDetailResponse(
            ErpEmployee employee,
            List<ErpEmployeeContact> contacts,
            List<ErpEmployeeQualification> qualifications,
            List<ErpEmployeeExperience> experiences,
            List<ErpEmployeeDocument> documents,
            String profilePhotoUrl,
            String signatureFileUrl
    ) {
        Objects.requireNonNull(
                employee,
                "Employee entity is required."
        );

        Branch branch =
                employee.getBranch();
        Department department =
                employee.getDepartment();
        Designation designation =
                employee.getDesignation();
        ErpEmployee reportingManager =
                employee.getReportingManager();
        User user =
                employee.getUser();

        boolean profilePhotoAvailable =
                hasText(employee.getProfilePhoto());
        boolean signatureFileAvailable =
                hasText(employee.getSignatureFile());

        List<String> loginRoles =
                resolveRoleCodes(user);

        return new EmployeeDetailResponse(
                employee.getEmployeeId(),
                employee.getEmployeeNo(),
                employee.getTitle(),
                employee.getFirstName(),
                employee.getMiddleName(),
                employee.getLastName(),
                employee.getFullName(),
                employee.getGender(),
                employee.getDateOfBirth(),
                employee.getMaritalStatus(),
                employee.getBloodGroup(),
                employee.getReligion(),
                employee.getSubReligion(),

                profilePhotoAvailable,
                profilePhotoAvailable
                        ? trimToNull(profilePhotoUrl)
                        : null,
                signatureFileAvailable,
                signatureFileAvailable
                        ? trimToNull(signatureFileUrl)
                        : null,

                employee.getNationality(),
                employee.getNationalId(),
                employee.getPassportNo(),
                employee.getPassportExpiryDate(),
                employee.getTinNumber(),
                employee.getWorkPermitNumber(),
                employee.getWorkPermitExpiryDate(),

                employee.getOfficialEmail(),
                employee.getPersonalEmail(),
                employee.getMobileNo(),
                employee.getAlternateMobile(),

                employee.getAddressCountry(),
                employee.getAddressState(),
                employee.getAddressDistrict(),
                employee.getAddressCounty(),
                employee.getAddressSubCounty(),
                employee.getAddressParish(),
                employee.getAddressVillage(),
                employee.getAddressStreet(),
                employee.getPostalCode(),

                branch == null
                        ? null
                        : branch.getBranchId(),
                branch == null
                        ? null
                        : branch.getBranchName(),
                branch == null
                        ? null
                        : branch.getSchoolCode(),

                department == null
                        ? null
                        : department.getDepartmentId(),
                department == null
                        ? null
                        : department.getDepartmentCode(),
                department == null
                        ? null
                        : department.getDepartmentName(),

                designation == null
                        ? null
                        : designation.getDesignationId(),
                designation == null
                        ? null
                        : designation.getDesignationCode(),
                designation == null
                        ? null
                        : designation.getDesignationName(),

                reportingManager == null
                        ? null
                        : reportingManager.getEmployeeId(),
                reportingManager == null
                        ? null
                        : reportingManager.getEmployeeNo(),
                reportingManager == null
                        ? null
                        : reportingManager.getFullName(),

                employee.getEmployeeCategory(),
                employee.getEmployeeType(),
                employee.getEmploymentMode(),
                employee.getEmploymentStatus(),
                employee.getJoiningDate(),
                employee.getProbationEndDate(),
                employee.getConfirmationDate(),
                employee.getRetirementDate(),
                employee.getResignationDate(),
                employee.getTerminationDate(),
                employee.getEmploymentEndDate(),
                employee.getExitReason(),
                employee.getSkills(),
                employee.getLanguagesSpoken(),
                employee.getEmployeeRemarks(),

                Boolean.TRUE.equals(
                        employee.getLoginEnabled()
                ),
                user == null
                        ? null
                        : user.getId(),
                user == null
                        ? null
                        : user.getUsername(),
                resolvePrimaryRole(user),
                loginRoles,
                resolveLoginStatus(user),
                user == null
                        ? null
                        : user.getMustChangePassword(),
                user == null
                        ? null
                        : user.getTemporaryPasswordExpiresAt(),
                user == null
                        ? null
                        : user.getCredentialDeliveryStatus(),
                user == null
                        ? null
                        : user.getCredentialsSentAt(),
                user == null
                        ? null
                        : user.getCredentialDeliveryAttempts(),

                toContactResponses(contacts),
                toQualificationResponses(qualifications),
                toExperienceResponses(experiences),
                toDocumentResponses(documents),

                Boolean.TRUE.equals(
                        employee.getActive()
                ),
                employee.getVersion(),
                employee.getCreatedBy(),
                employee.getCreatedAt(),
                employee.getUpdatedBy(),
                employee.getUpdatedAt()
        );
    }

    // =====================================================================
    // NESTED ENTITY -> RESPONSE
    // =====================================================================

    public List<EmployeeContactResponse> toContactResponses(
            List<ErpEmployeeContact> contacts
    ) {
        if (contacts == null || contacts.isEmpty()) {
            return List.of();
        }

        return contacts.stream()
                .filter(Objects::nonNull)
                .map(this::toContactResponse)
                .toList();
    }

    public EmployeeContactResponse toContactResponse(
            ErpEmployeeContact contact
    ) {
        Objects.requireNonNull(
                contact,
                "Employee contact entity is required."
        );

        ErpEmployee employee =
                contact.getEmployee();

        return new EmployeeContactResponse(
                contact.getEmployeeContactId(),
                employee == null
                        ? null
                        : employee.getEmployeeId(),
                contact.getEmployeeContactName(),
                contact.getEmployeeContactRelationship(),
                contact.getEmployeeContactType(),
                contact.getEmployeeContactMobile(),
                contact.getEmployeeContactAlternateMobile(),
                contact.getEmployeeContactEmail(),
                contact.getEmployeeContactCountry(),
                contact.getEmployeeContactState(),
                contact.getEmployeeContactDistrict(),
                contact.getEmployeeContactVillage(),
                contact.getEmployeeContactStreet(),
                contact.getEmployeeContactPostalCode(),
                contact.getEmployeeContactOccupation(),
                contact.getEmployeeContactWorkplace(),
                contact.getEmployeeContactIsPrimary(),
                contact.getEmployeeContactIsEmergency(),
                contact.getEmployeeContactActive(),
                contact.getEmployeeContactRemarks(),
                contact.getVersion(),
                contact.getCreatedBy(),
                contact.getCreatedAt(),
                contact.getUpdatedBy(),
                contact.getUpdatedAt()
        );
    }

    public List<EmployeeQualificationResponse> toQualificationResponses(
            List<ErpEmployeeQualification> qualifications
    ) {
        if (qualifications == null || qualifications.isEmpty()) {
            return List.of();
        }

        return qualifications.stream()
                .filter(Objects::nonNull)
                .map(this::toQualificationResponse)
                .toList();
    }

    public EmployeeQualificationResponse toQualificationResponse(
            ErpEmployeeQualification qualification
    ) {
        Objects.requireNonNull(
                qualification,
                "Employee qualification entity is required."
        );

        ErpEmployee employee =
                qualification.getEmployee();
        User verifiedBy =
                qualification.getEmployeeQualificationVerifiedBy();

        return new EmployeeQualificationResponse(
                qualification.getEmployeeQualificationId(),
                employee == null
                        ? null
                        : employee.getEmployeeId(),
                qualification.getEmployeeQualificationLevel(),
                qualification.getCustomLevel(),
                qualification.getEmployeeQualificationName(),
                qualification.getEmployeeQualificationSpecialization(),
                qualification.getEmployeeQualificationInstitutionName(),
                qualification.getQualificationGrade(),
                qualification.getEmployeeQualificationBoardUniversity(),
                qualification.getEmployeeQualificationCountry(),
                qualification.getEmployeeQualificationStartYear(),
                qualification.getEmployeeQualificationCompletionYear(),
                qualification.getEmployeeQualificationDurationMonths(),
                qualification.getEmployeeQualificationGrade(),
                qualification.getEmployeeQualificationPercentage(),
                qualification.getEmployeeQualificationCgpa(),
                qualification.getEmployeeQualificationCertificateNumber(),
                qualification.getEmployeeQualificationRegistrationNumber(),
                hasText(
                        qualification.getEmployeeQualificationDocumentFile()
                ),
                qualification.getEmployeeQualificationVerified(),
                verifiedBy == null
                        ? null
                        : verifiedBy.getId(),
                verifiedBy == null
                        ? null
                        : verifiedBy.getUsername(),
                qualification.getEmployeeQualificationVerifiedAt(),
                qualification.getEmployeeQualificationRemarks(),
                qualification.getEmployeeQualificationActive(),
                qualification.getVersion(),
                qualification.getCreatedBy(),
                qualification.getCreatedAt(),
                qualification.getUpdatedBy(),
                qualification.getUpdatedAt()
        );
    }

    public List<EmployeeExperienceResponse> toExperienceResponses(
            List<ErpEmployeeExperience> experiences
    ) {
        if (experiences == null || experiences.isEmpty()) {
            return List.of();
        }

        return experiences.stream()
                .filter(Objects::nonNull)
                .map(this::toExperienceResponse)
                .toList();
    }

    public EmployeeExperienceResponse toExperienceResponse(
            ErpEmployeeExperience experience
    ) {
        Objects.requireNonNull(
                experience,
                "Employee experience entity is required."
        );

        ErpEmployee employee =
                experience.getEmployee();
        User verifiedBy =
                experience.getEmployeeExperienceVerifiedBy();

        return new EmployeeExperienceResponse(
                experience.getEmployeeExperienceId(),
                employee == null
                        ? null
                        : employee.getEmployeeId(),
                experience.getEmployeeExperienceType(),
                experience.getEmployeeExperienceCompanyName(),
                experience.getEmployeeExperienceCompanyAddress(),
                experience.getEmployeeExperienceCompanyCountry(),
                experience.getEmployeeExperienceCompanyState(),
                experience.getEmployeeExperienceCompanyDistrict(),
                experience.getEmployeeExperienceDesignation(),
                experience.getEmployeeExperienceDepartment(),
                experience.getEmployeeExperienceEmploymentType(),
                experience.getEmployeeExperienceStartDate(),
                experience.getEmployeeExperienceEndDate(),
                experience.getEmployeeExperienceCurrentJob(),
                experience.getEmployeeExperienceTotalMonths(),
                experience.getEmployeeExperienceSalary(),
                experience.getEmployeeExperienceCurrency(),
                experience.getEmployeeExperienceSupervisorName(),
                experience.getEmployeeExperienceSupervisorContact(),
                experience.getEmployeeExperienceReasonForLeaving(),
                experience.getEmployeeExperienceResponsibilities(),
                experience.getEmployeeExperienceAchievements(),
                hasText(
                        experience.getEmployeeExperienceExperienceCertificateFile()
                ),
                hasText(
                        experience.getEmployeeExperienceRelievingLetterFile()
                ),
                experience.getEmployeeExperienceVerified(),
                verifiedBy == null
                        ? null
                        : verifiedBy.getId(),
                verifiedBy == null
                        ? null
                        : verifiedBy.getUsername(),
                experience.getEmployeeExperienceVerifiedAt(),
                experience.getEmployeeExperienceActive(),
                experience.getEmployeeExperienceRemarks(),
                experience.getVersion(),
                experience.getCreatedBy(),
                experience.getCreatedAt(),
                experience.getUpdatedBy(),
                experience.getUpdatedAt()
        );
    }

    public List<EmployeeDocumentResponse> toDocumentResponses(
            List<ErpEmployeeDocument> documents
    ) {
        if (documents == null || documents.isEmpty()) {
            return List.of();
        }

        return documents.stream()
                .filter(Objects::nonNull)
                .map(this::toDocumentResponse)
                .toList();
    }

    public EmployeeDocumentResponse toDocumentResponse(
            ErpEmployeeDocument document
    ) {
        Objects.requireNonNull(
                document,
                "Employee document entity is required."
        );

        ErpEmployee employee =
                document.getEmployee();
        User verifiedBy =
                document.getEmployeeDocumentVerifiedBy();

        return new EmployeeDocumentResponse(
                document.getEmployeeDocumentId(),
                employee == null
                        ? null
                        : employee.getEmployeeId(),
                document.getEmployeeDocumentType(),
                document.getEmployeeDocumentName(),
                document.getEmployeeDocumentDescription(),
                document.getEmployeeDocumentOriginalFileName(),
                document.getEmployeeDocumentFileExtension(),
                document.getEmployeeDocumentMimeType(),
                document.getEmployeeDocumentFileSize(),
                hasText(
                        document.getEmployeeDocumentFilePath()
                ),
                document.getEmployeeDocumentIssueDate(),
                document.getEmployeeDocumentExpiryDate(),
                document.getEmployeeDocumentVerified(),
                verifiedBy == null
                        ? null
                        : verifiedBy.getId(),
                verifiedBy == null
                        ? null
                        : verifiedBy.getUsername(),
                document.getEmployeeDocumentVerifiedAt(),
                document.getEmployeeDocumentIsMandatory(),
                document.getEmployeeDocumentActive(),
                document.getEmployeeDocumentRemarks(),
                document.getVersion(),
                document.getCreatedBy(),
                document.getCreatedAt(),
                document.getUpdatedBy(),
                document.getUpdatedAt()
        );
    }

    // =====================================================================
    // INTERNAL HELPERS
    // =====================================================================

    private LocalDate resolveEmploymentEndDate(
            EmploymentStatus status,
            LocalDate retirementDate,
            LocalDate resignationDate,
            LocalDate terminationDate
    ) {
        if (status == null) {
            return null;
        }

        return switch (status) {
            case RESIGNED -> resignationDate;
            case RETIRED -> retirementDate;
            case TERMINATED -> terminationDate;
            default -> null;
        };
    }

    private boolean isFinalEmploymentStatus(
            EmploymentStatus status
    ) {
        return status == EmploymentStatus.RESIGNED
                || status == EmploymentStatus.RETIRED
                || status == EmploymentStatus.TERMINATED;
    }

    private Integer resolveQualificationDurationMonths(
            EmployeeQualificationRequest request
    ) {
        if (request.employeeQualificationDurationMonths() != null) {
            return request.employeeQualificationDurationMonths();
        }

        Integer startYear =
                request.employeeQualificationStartYear();
        Integer completionYear =
                request.employeeQualificationCompletionYear();

        if (startYear == null || completionYear == null) {
            return null;
        }

        return (
                completionYear
                        - startYear
                        + 1
        ) * 12;
    }

    private Integer calculateExperienceMonths(
            LocalDate startDate,
            LocalDate endDate,
            Boolean currentJob
    ) {
        if (startDate == null) {
            return null;
        }

        LocalDate effectiveEndDate =
                Boolean.TRUE.equals(currentJob)
                        ? LocalDate.now()
                        : endDate;

        if (
                effectiveEndDate == null
                        || effectiveEndDate.isBefore(startDate)
        ) {
            return null;
        }

        Period period =
                Period.between(
                        startDate,
                        effectiveEndDate
                );

        int months =
                period.getYears() * 12
                        + period.getMonths();

        if (period.getDays() > 0) {
            months++;
        }

        return Math.max(
                months,
                0
        );
    }

    private String resolveDocumentName(
            EmployeeDocumentRequest request
    ) {
        if (
                request.employeeDocumentType()
                        == EmployeeDocumentType.OTHER
        ) {
            return trimRequired(
                    request.employeeDocumentName()
            );
        }

        return toDisplayName(
                request.employeeDocumentType()
        );
    }

    private String toDisplayName(
            EmployeeDocumentType documentType
    ) {
        if (documentType == null) {
            return null;
        }

        String[] words =
                documentType.name()
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .split("_");

        StringBuilder displayName =
                new StringBuilder();

        for (String word : words) {
            if (!displayName.isEmpty()) {
                displayName.append(' ');
            }

            displayName.append(
                    Character.toUpperCase(
                            word.charAt(0)
                    )
            );

            if (word.length() > 1) {
                displayName.append(
                        word.substring(1)
                );
            }
        }

        return displayName.toString();
    }

    private List<String> resolveRoleCodes(
            User user
    ) {
        if (user == null) {
            return List.of();
        }

        Set<String> roleCodes =
                new LinkedHashSet<>();

        if (hasText(user.getRole())) {
            roleCodes.add(
                    user.getRole()
                            .trim()
                            .toUpperCase(
                                    Locale.ROOT
                            )
            );
        }

        List<ErpUserRole> userRoles =
                user.getUserRoles();

        if (userRoles != null) {
            for (ErpUserRole userRole : userRoles) {
                if (
                        userRole == null
                                || !Boolean.TRUE.equals(
                                userRole.getActive()
                        )
                ) {
                    continue;
                }

                ErpRole role =
                        userRole.getRole();

                if (
                        role != null
                                && Boolean.TRUE.equals(
                                role.getActive()
                        )
                                && hasText(
                                role.getRoleCode()
                        )
                ) {
                    roleCodes.add(
                            role.getRoleCode()
                                    .trim()
                                    .toUpperCase(
                                            Locale.ROOT
                                    )
                    );
                }
            }
        }

        return List.copyOf(roleCodes);
    }

    private String resolvePrimaryRole(
            User user
    ) {
        List<String> roles =
                resolveRoleCodes(user);

        return roles.isEmpty()
                ? null
                : roles.getFirst();
    }

    private String resolveLoginStatus(
            User user
    ) {
        if (user == null) {
            return "NOT_CREATED";
        }

        return Integer.valueOf(1)
                .equals(user.getIsActive())
                ? "ACTIVE"
                : "INACTIVE";
    }

    private String normalizeSortDirection(
            String sortDirection
    ) {
        if (!hasText(sortDirection)) {
            return "ASC";
        }

        return "DESC".equalsIgnoreCase(
                sortDirection.trim()
        )
                ? "DESC"
                : "ASC";
    }

    private String normalizeEmail(
            String email
    ) {
        String normalized =
                trimToNull(email);

        return normalized == null
                ? null
                : normalized.toLowerCase(
                        Locale.ROOT
                );
    }

    private String uppercaseToNull(
            String value
    ) {
        String normalized =
                trimToNull(value);

        return normalized == null
                ? null
                : normalized.toUpperCase(
                        Locale.ROOT
                );
    }

    private String buildFullName(
            String firstName,
            String middleName,
            String lastName
    ) {
        StringBuilder fullName =
                new StringBuilder();

        appendNamePart(
                fullName,
                firstName
        );
        appendNamePart(
                fullName,
                middleName
        );
        appendNamePart(
                fullName,
                lastName
        );

        return fullName.toString();
    }

    private void appendNamePart(
            StringBuilder fullName,
            String value
    ) {
        String normalized =
                trimToNull(value);

        if (normalized == null) {
            return;
        }

        if (!fullName.isEmpty()) {
            fullName.append(' ');
        }

        fullName.append(normalized);
    }

    private String trimRequired(
            String value
    ) {
        return value == null
                ? null
                : value.trim();
    }

    private String trimToNull(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    private boolean hasText(
            String value
    ) {
        return value != null
                && !value.isBlank();
    }
}
