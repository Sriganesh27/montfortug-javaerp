package com.erp.montfortuganda.employee.importplugin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeImportDTO {
    // Organization
    private String departmentName;
    private String designationName;
    private String reportingManagerEmployeeNo;

    // Personal Details
    private String title;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private String dateOfBirth; // (YYYY-MM-DD)
    private String nationality;
    private String nationalId;

    // Contact Details
    private String officialEmail;
    private String personalEmail;
    private String mobileNumber;
    private String alternateMobile;

    // Address
    private String district;
    private String county;
    private String subCounty;
    private String parish;
    private String village;
    private String street;
    private String postalCode;

    // Employment
    private String employeeCategory;
    private String employeeType;
    private String employmentMode;
    private String joiningDate; // (YYYY-MM-DD)
    
    // Auth & Misc
    private String loginEnabled;
    private String remarks;
}
