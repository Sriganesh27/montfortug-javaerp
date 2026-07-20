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
    @Column(name = "version", nullable = false)
    private Long version = 0L;
}