// File: src/main/java/com/erp/montfortuganda/employee/entity/ErpEmployeeExperience.java
package com.erp.montfortuganda.employee.entity;

import com.erp.montfortuganda.auth.entity.User;
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