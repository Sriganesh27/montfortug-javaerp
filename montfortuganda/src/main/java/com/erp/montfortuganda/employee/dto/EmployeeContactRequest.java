// File: src/main/java/com/erp/montfortuganda/employee/dto/EmployeeContactRequest.java
package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.ContactRelationship;
import com.erp.montfortuganda.employee.enums.ContactType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeContactRequest {

    @Size(max = 50)
    private String employeeContactCode;

    @NotBlank(message = "Contact name is required")
    @Size(max = 255)
    @Pattern(regexp = "^[A-Za-z .'-]+$", message = "Invalid contact name format")
    private String employeeContactName;

    @NotNull(message = "Contact relationship is required")
    private ContactRelationship employeeContactRelationship;

    @NotNull(message = "Contact type is required")
    private ContactType employeeContactType;

    @NotBlank(message = "Contact mobile is required")
    @Pattern(regexp = "^[+0-9\\-\\s]{7,20}$", message = "Invalid contact mobile number")
    private String employeeContactMobile;

    @Pattern(regexp = "^[+0-9\\-\\s]{7,20}$", message = "Invalid alternate mobile number")
    private String employeeContactAlternateMobile;

    @Email(message = "Invalid contact email format")
    @Size(max = 150)
    private String employeeContactEmail;

    private LocalDate employeeContactDateOfBirth;

    @Size(max = 50)
    private String employeeContactNationalId;

    @Size(max = 100)
    private String employeeContactCountry;

    @Size(max = 100)
    private String employeeContactState;

    @Size(max = 100)
    private String employeeContactDistrict;

    @Size(max = 150)
    private String employeeContactVillage;

    @Size(max = 255)
    private String employeeContactStreet;

    @Size(max = 20)
    private String employeeContactPostalCode;

    @Size(max = 150)
    private String employeeContactOccupation;

    @Size(max = 255)
    private String employeeContactWorkplace;

    @NotNull(message = "Primary contact flag is required")
    private Boolean employeeContactIsPrimary;

    @NotNull(message = "Emergency contact flag is required")
    private Boolean employeeContactIsEmergency;

    @Size(max = 1000)
    private String employeeContactRemarks;

    private Integer displayOrder;

    private Boolean active = true;

}