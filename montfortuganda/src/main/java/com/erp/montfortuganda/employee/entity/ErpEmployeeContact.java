package com.erp.montfortuganda.employee.entity;

import com.erp.montfortuganda.employee.enums.EmployeeContactRelationship;
import com.erp.montfortuganda.employee.enums.EmployeeContactType;
import com.erp.montfortuganda.model.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;

@Data
@Entity
@DynamicUpdate
@Table(
        name = "erp_employee_contacts",
        indexes = {
                @Index(name = "idx_empcontact_employee", columnList = "employee_id"),
                @Index(name = "idx_empcontact_type", columnList = "employee_contact_type")
        }
)
@EqualsAndHashCode(callSuper = true, exclude = {"employee"})
@ToString(callSuper = true, exclude = {"employee"})
public class ErpEmployeeContact extends AuditableEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_contact_id")
    private Long employeeContactId;

    //==================================================
    // RELATIONSHIPS
    //==================================================

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "employee_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_empcontact_employee")
    )
    private ErpEmployee employee;

    //==================================================
    // CONTACT DETAILS
    //==================================================

    @NotBlank
    @Size(max = 255)
    @Column(name = "employee_contact_name", nullable = false, length = 255)
    private String employeeContactName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_contact_relationship", nullable = false, length = 50)
    private EmployeeContactRelationship employeeContactRelationship;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_contact_type", nullable = false, length = 50)
    private EmployeeContactType employeeContactType = EmployeeContactType.EMERGENCY;

    @NotBlank
    @Size(max = 30)
    @Column(name = "employee_contact_mobile", nullable = false, length = 30)
    private String employeeContactMobile;

    @Size(max = 30)
    @Column(name = "employee_contact_alternate_mobile", length = 30)
    private String employeeContactAlternateMobile;

    @Size(max = 150)
    @Column(name = "employee_contact_email", length = 150)
    private String employeeContactEmail;

    //==================================================
    // ADDRESS DETAILS (UGANDA SPECIFIC)
    //==================================================

    @Size(max = 100)
    @Column(name = "employee_contact_country", length = 100)
    private String employeeContactCountry;

    @Size(max = 100)
    @Column(name = "employee_contact_district", length = 100)
    private String employeeContactDistrict;

    @Size(max = 100)
    @Column(name = "employee_contact_sub_county", length = 100)
    private String employeeContactSubCounty;

    @Size(max = 150)
    @Column(name = "employee_contact_parish", length = 150)
    private String employeeContactParish;

    @Size(max = 150)
    @Column(name = "employee_contact_village", length = 150)
    private String employeeContactVillage;

    @Size(max = 255)
    @Column(name = "employee_contact_landmark", length = 255)
    private String employeeContactLandmark;

    @Column(name = "employee_contact_physical_address", columnDefinition = "TEXT")
    private String employeeContactPhysicalAddress;

    @Size(max = 50)
    @Column(name = "employee_contact_po_box", length = 50)
    private String employeeContactPoBox;

    //==================================================
    // ADDITIONAL INFO
    //==================================================

    @Size(max = 150)
    @Column(name = "employee_contact_occupation", length = 150)
    private String employeeContactOccupation;

    @Size(max = 255)
    @Column(name = "employee_contact_workplace", length = 255)
    private String employeeContactWorkplace;

    @Column(name = "employee_contact_remarks", columnDefinition = "TEXT")
    private String employeeContactRemarks;

    //==================================================
    // STATUS FLAGS
    //==================================================

    @NotNull
    @Column(name = "employee_contact_preferred", nullable = false)
    private Boolean employeeContactPreferred = false;

    @NotNull
    @Column(name = "employee_contact_alive", nullable = false)
    private Boolean employeeContactAlive = true;

    @NotNull
    @Column(name = "employee_contact_active", nullable = false)
    private Boolean employeeContactActive = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @PrePersist
    public void prePersist() {
        if (employeeContactType == null)
            employeeContactType = EmployeeContactType.EMERGENCY;

        if (employeeContactPreferred == null)
            employeeContactPreferred = false;

        if (employeeContactAlive == null)
            employeeContactAlive = true;

        if (employeeContactActive == null)
            employeeContactActive = true;

        if (version == null)
            version = 0L;
    }
}