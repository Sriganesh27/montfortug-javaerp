// File: src/main/java/com/erp/montfortuganda/employee/dto/EmployeeContactDTO.java
package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.ContactRelationship;
import com.erp.montfortuganda.employee.enums.ContactType;
import lombok.Data;

@Data
public class EmployeeContactDTO {
    private Long employeeContactId;
    private Long employeeId;
    private String employeeContactName;
    private ContactRelationship employeeContactRelationship;
    private ContactType employeeContactType;
    private String employeeContactMobile;
    private String employeeContactEmail;
    private Boolean employeeContactIsPrimary;
    private Boolean employeeContactIsEmergency;
    private Boolean employeeContactActive;
}