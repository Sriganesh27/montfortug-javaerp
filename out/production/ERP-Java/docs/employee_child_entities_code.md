# Employee Child Entities Codebase

Here is the Java code for the 4 child tables (`erp_employee_contacts`, `erp_employee_documents`, `erp_employee_experience`, `erp_employee_qualifications`).

Copy and paste these classes into your project under the `com.erp.montfortuganda.employee.entity` and `com.erp.montfortuganda.employee.enums` packages.

## 1. Enums

```java
// File: src/main/java/com/erp/montfortuganda/employee/enums/ContactRelationship.java
package com.erp.montfortuganda.employee.enums;
public enum ContactRelationship {
    FATHER, MOTHER, SPOUSE, SON, DAUGHTER, BROTHER, SISTER, GUARDIAN, RELATIVE, FRIEND, MANAGER, REFERENCE, OTHER
}
```

```java
// File: src/main/java/com/erp/montfortuganda/employee/enums/ContactType.java
package com.erp.montfortuganda.employee.enums;
public enum ContactType {
    EMERGENCY, NEXT_OF_KIN, REFERENCE, GUARDIAN, OTHER
}
```

```java
// File: src/main/java/com/erp/montfortuganda/employee/enums/EmployeeDocumentType.java
package com.erp.montfortuganda.employee.enums;
public enum EmployeeDocumentType {
    PASSPORT_PHOTO, SIGNATURE, RESUME, CURRICULUM_VITAE, NATIONAL_ID, PASSPORT, BIRTH_CERTIFICATE, 
    MARRIAGE_CERTIFICATE, MEDICAL_CERTIFICATE, POLICE_CLEARANCE, APPOINTMENT_LETTER, EMPLOYMENT_CONTRACT, 
    CONFIDENTIALITY_AGREEMENT, CODE_OF_CONDUCT_AGREEMENT, EXPERIENCE_CERTIFICATE, RELIEVING_LETTER, 
    SALARY_CERTIFICATE, BANK_DOCUMENT, NSSF_DOCUMENT, TIN_CERTIFICATE, WORK_PERMIT, VISA, 
    TEACHING_LICENSE, PROFESSIONAL_LICENSE, ACADEMIC_CERTIFICATE, OTHER
}
```

```java
// File: src/main/java/com/erp/montfortuganda/employee/enums/ExperienceEmploymentType.java
package com.erp.montfortuganda.employee.enums;
public enum ExperienceEmploymentType {
    FULL_TIME, PART_TIME, CONTRACT, TEMPORARY, INTERNSHIP, CONSULTANT, VOLUNTEER, OTHER
}
```

```java
// File: src/main/java/com/erp/montfortuganda/employee/enums/QualificationLevel.java
package com.erp.montfortuganda.employee.enums;
public enum QualificationLevel {
    PRIMARY, O_LEVEL, A_LEVEL, CERTIFICATE, DIPLOMA, ADVANCED_DIPLOMA, BACHELOR, POST_GRADUATE, 
    POST_GRADUATE_DIPLOMA, MASTER, M_PHIL, PHD, PROFESSIONAL, VOCATIONAL, SHORT_COURSE, TRAINING, 
    CERTIFICATION, OTHER
}
```

---

## 2. Entities

### Employee Contacts

```java
// File: src/main/java/com/erp/montfortuganda/employee/entity/ErpEmployeeContact.java
package com.erp.montfortuganda.employee.entity;

import com.erp.montfortuganda.employee.enums.ContactRelationship;
import com.erp.montfortuganda.employee.enums.ContactType;
import com.erp.montfortuganda.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "erp_employee_contacts")
public class ErpEmployeeContact extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_contact_id")
    private Long employeeContactId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private ErpEmployee employee;

    @Column(name = "employee_contact_name", nullable = false, length = 255)
    private String employeeContactName;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_contact_relationship", nullable = false, length = 30)
    private ContactRelationship employeeContactRelationship;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_contact_type", nullable = false, length = 30)
    private ContactType employeeContactType = ContactType.EMERGENCY;

    @Column(name = "employee_contact_mobile", nullable = false, length = 30)
    private String employeeContactMobile;

    @Column(name = "employee_contact_alternate_mobile", length = 30)
    private String employeeContactAlternateMobile;

    @Column(name = "employee_contact_email", length = 150)
    private String employeeContactEmail;

    @Column(name = "employee_contact_country", length = 100)
    private String employeeContactCountry;

    @Column(name = "employee_contact_state", length = 100)
    private String employeeContactState;

    @Column(name = "employee_contact_district", length = 100)
    private String employeeContactDistrict;

    @Column(name = "employee_contact_village", length = 150)
    private String employeeContactVillage;

    @Column(name = "employee_contact_street", length = 255)
    private String employeeContactStreet;

    @Column(name = "employee_contact_postal_code", length = 30)
    private String employeeContactPostalCode;

    @Column(name = "employee_contact_occupation", length = 150)
    private String employeeContactOccupation;

    @Column(name = "employee_contact_workplace", length = 255)
    private String employeeContactWorkplace;

    @Column(name = "employee_contact_is_primary", nullable = false)
    private Boolean employeeContactIsPrimary = false;

    @Column(name = "employee_contact_is_emergency", nullable = false)
    private Boolean employeeContactIsEmergency = true;

    @Column(name = "employee_contact_active", nullable = false)
    private Boolean employeeContactActive = true;

    @Column(name = "employee_contact_remarks", columnDefinition = "TEXT")
    private String employeeContactRemarks;

    @Version
    @Column(nullable = false)
    private Long version = 0L;
}
```

### Employee Documents

```java
// File: src/main/java/com/erp/montfortuganda/employee/entity/ErpEmployeeDocument.java
package com.erp.montfortuganda.employee.entity;

import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.employee.enums.EmployeeDocumentType;
import com.erp.montfortuganda.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "erp_employee_documents")
public class ErpEmployeeDocument extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_document_id")
    private Long employeeDocumentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private ErpEmployee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_document_type", nullable = false, length = 50)
    private EmployeeDocumentType employeeDocumentType;

    @Column(name = "employee_document_name", nullable = false, length = 255)
    private String employeeDocumentName;

    @Column(name = "employee_document_description", columnDefinition = "TEXT")
    private String employeeDocumentDescription;

    @Column(name = "employee_document_file_name", nullable = false, length = 255)
    private String employeeDocumentFileName;

    @Column(name = "employee_document_original_file_name", length = 255)
    private String employeeDocumentOriginalFileName;

    @Column(name = "employee_document_file_path", nullable = false, length = 500)
    private String employeeDocumentFilePath;

    @Column(name = "employee_document_file_extension", length = 20)
    private String employeeDocumentFileExtension;

    @Column(name = "employee_document_mime_type", length = 100)
    private String employeeDocumentMimeType;

    @Column(name = "employee_document_file_size")
    private Long employeeDocumentFileSize;

    @Column(name = "employee_document_issue_date")
    private LocalDate employeeDocumentIssueDate;

    @Column(name = "employee_document_expiry_date")
    private LocalDate employeeDocumentExpiryDate;

    @Column(name = "employee_document_verified", nullable = false)
    private Boolean employeeDocumentVerified = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_document_verified_by")
    private User employeeDocumentVerifiedBy;

    @Column(name = "employee_document_verified_at")
    private LocalDateTime employeeDocumentVerifiedAt;

    @Column(name = "employee_document_is_mandatory", nullable = false)
    private Boolean employeeDocumentIsMandatory = false;

    @Column(name = "employee_document_active", nullable = false)
    private Boolean employeeDocumentActive = true;

    @Column(name = "employee_document_remarks", columnDefinition = "TEXT")
    private String employeeDocumentRemarks;

    @Version
    @Column(nullable = false)
    private Long version = 0L;
}
```

### Employee Experience

```java
// File: src/main/java/com/erp/montfortuganda/employee/entity/ErpEmployeeExperience.java
package com.erp.montfortuganda.employee.entity;

import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.employee.enums.ExperienceEmploymentType;
import com.erp.montfortuganda.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "erp_employee_experience")
public class ErpEmployeeExperience extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_experience_id")
    private Long employeeExperienceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private ErpEmployee employee;

    @Column(name = "employee_experience_company_name", nullable = false, length = 255)
    private String employeeExperienceCompanyName;

    @Column(name = "employee_experience_company_address", length = 255)
    private String employeeExperienceCompanyAddress;

    @Column(name = "employee_experience_company_country", length = 100)
    private String employeeExperienceCompanyCountry;

    @Column(name = "employee_experience_company_state", length = 100)
    private String employeeExperienceCompanyState;

    @Column(name = "employee_experience_company_district", length = 100)
    private String employeeExperienceCompanyDistrict;

    @Column(name = "employee_experience_designation", length = 150)
    private String employeeExperienceDesignation;

    @Column(name = "employee_experience_department", length = 150)
    private String employeeExperienceDepartment;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_experience_employment_type", nullable = false, length = 30)
    private ExperienceEmploymentType employeeExperienceEmploymentType;

    @Column(name = "employee_experience_start_date", nullable = false)
    private LocalDate employeeExperienceStartDate;

    @Column(name = "employee_experience_end_date")
    private LocalDate employeeExperienceEndDate;

    @Column(name = "employee_experience_current_job", nullable = false)
    private Boolean employeeExperienceCurrentJob = false;

    @Column(name = "employee_experience_total_months")
    private Integer employeeExperienceTotalMonths;

    @Column(name = "employee_experience_salary", precision = 15, scale = 2)
    private BigDecimal employeeExperienceSalary;

    @Column(name = "employee_experience_currency", length = 10)
    private String employeeExperienceCurrency;

    @Column(name = "employee_experience_supervisor_name", length = 255)
    private String employeeExperienceSupervisorName;

    @Column(name = "employee_experience_supervisor_contact", length = 100)
    private String employeeExperienceSupervisorContact;

    @Column(name = "employee_experience_reason_for_leaving", length = 255)
    private String employeeExperienceReasonForLeaving;

    @Column(name = "employee_experience_responsibilities", columnDefinition = "TEXT")
    private String employeeExperienceResponsibilities;

    @Column(name = "employee_experience_achievements", columnDefinition = "TEXT")
    private String employeeExperienceAchievements;

    @Column(name = "employee_experience_experience_certificate_file", length = 500)
    private String employeeExperienceExperienceCertificateFile;

    @Column(name = "employee_experience_relieving_letter_file", length = 500)
    private String employeeExperienceRelievingLetterFile;

    @Column(name = "employee_experience_verified", nullable = false)
    private Boolean employeeExperienceVerified = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_experience_verified_by")
    private User employeeExperienceVerifiedBy;

    @Column(name = "employee_experience_verified_at")
    private LocalDateTime employeeExperienceVerifiedAt;

    @Column(name = "employee_experience_active", nullable = false)
    private Boolean employeeExperienceActive = true;

    @Column(name = "employee_experience_remarks", columnDefinition = "TEXT")
    private String employeeExperienceRemarks;

    @Version
    @Column(nullable = false)
    private Long version = 0L;
}
```

### Employee Qualifications

```java
// File: src/main/java/com/erp/montfortuganda/employee/entity/ErpEmployeeQualification.java
package com.erp.montfortuganda.employee.entity;

import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.employee.enums.QualificationLevel;
import com.erp.montfortuganda.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "erp_employee_qualifications")
public class ErpEmployeeQualification extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_qualification_id")
    private Long employeeQualificationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private ErpEmployee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_qualification_level", nullable = false, length = 30)
    private QualificationLevel employeeQualificationLevel;

    @Column(name = "employee_qualification_name", nullable = false, length = 255)
    private String employeeQualificationName;

    @Column(name = "employee_qualification_specialization", length = 255)
    private String employeeQualificationSpecialization;

    @Column(name = "employee_qualification_institution_name", nullable = false, length = 255)
    private String employeeQualificationInstitutionName;

    @Column(name = "employee_qualification_board_university", length = 255)
    private String employeeQualificationBoardUniversity;

    @Column(name = "employee_qualification_country", length = 100)
    private String employeeQualificationCountry;

    @Column(name = "employee_qualification_start_year")
    private Integer employeeQualificationStartYear;

    @Column(name = "employee_qualification_completion_year")
    private Integer employeeQualificationCompletionYear;

    @Column(name = "employee_qualification_duration_months")
    private Integer employeeQualificationDurationMonths;

    @Column(name = "employee_qualification_grade", length = 50)
    private String employeeQualificationGrade;

    @Column(name = "employee_qualification_percentage", precision = 5, scale = 2)
    private BigDecimal employeeQualificationPercentage;

    @Column(name = "employee_qualification_cgpa", precision = 4, scale = 2)
    private BigDecimal employeeQualificationCgpa;

    @Column(name = "employee_qualification_certificate_number", length = 100)
    private String employeeQualificationCertificateNumber;

    @Column(name = "employee_qualification_registration_number", length = 100)
    private String employeeQualificationRegistrationNumber;

    @Column(name = "employee_qualification_document_file", length = 500)
    private String employeeQualificationDocumentFile;

    @Column(name = "employee_qualification_verified", nullable = false)
    private Boolean employeeQualificationVerified = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_qualification_verified_by")
    private User employeeQualificationVerifiedBy;

    @Column(name = "employee_qualification_verified_at")
    private LocalDateTime employeeQualificationVerifiedAt;

    @Column(name = "employee_qualification_remarks", columnDefinition = "TEXT")
    private String employeeQualificationRemarks;

    @Column(name = "employee_qualification_active", nullable = false)
    private Boolean employeeQualificationActive = true;

    @Version
    @Column(nullable = false)
    private Long version = 0L;
}
```
