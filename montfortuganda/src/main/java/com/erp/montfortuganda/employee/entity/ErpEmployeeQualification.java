// File: src/main/java/com/erp/montfortuganda/employee/entity/ErpEmployeeQualification.java
package com.erp.montfortuganda.employee.entity;

import com.erp.montfortuganda.auth.entity.User;
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

    @Column(name = "qualification_grade", length = 100)
    private String qualificationGrade;

    @Column(name = "custom_level", length = 255)
    private String customLevel;

    @Version
    @Column(nullable = false)
    private Long version = 0L;
}