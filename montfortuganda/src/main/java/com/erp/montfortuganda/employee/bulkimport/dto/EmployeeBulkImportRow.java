package com.erp.montfortuganda.employee.bulkimport.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Raw values read from one row of the Employee Excel workbook.

 * Dates remain Strings at this stage. They are converted to LocalDate
 * only after validation so invalid text can never reach the database.
 */
@Getter
@Builder
@ToString
public class EmployeeBulkImportRow {

    /**
     * Actual one-based Excel row number.
     * Header is row 1, therefore employee data normally starts at row 2.
     */
    private final int excelRowNumber;

    private final String departmentName;
    private final String designationName;
    private final String reportingManagerEmployeeNo;

    private final String title;
    private final String firstName;
    private final String middleName;
    private final String lastName;
    private final String gender;
    private final String dateOfBirth;

    private final String nationality;
    private final String nationalId;

    private final String officialEmail;
    private final String personalEmail;
    private final String mobileNumber;
    private final String alternateMobile;

    private final String district;
    private final String county;
    private final String subCounty;
    private final String parish;
    private final String village;
    private final String street;
    private final String postalCode;

    private final String employeeCategory;
    private final String employeeType;
    private final String employmentMode;
    private final String joiningDate;

    private final String loginEnabled;
    private final String remarks;

    /**
     * Used to avoid attempting to process completely blank Excel rows.
     */
    public boolean isBlank() {
        return isBlankValue(departmentName)
                && isBlankValue(designationName)
                && isBlankValue(reportingManagerEmployeeNo)
                && isBlankValue(title)
                && isBlankValue(firstName)
                && isBlankValue(middleName)
                && isBlankValue(lastName)
                && isBlankValue(gender)
                && isBlankValue(dateOfBirth)
                && isBlankValue(nationality)
                && isBlankValue(nationalId)
                && isBlankValue(officialEmail)
                && isBlankValue(personalEmail)
                && isBlankValue(mobileNumber)
                && isBlankValue(alternateMobile)
                && isBlankValue(district)
                && isBlankValue(county)
                && isBlankValue(subCounty)
                && isBlankValue(parish)
                && isBlankValue(village)
                && isBlankValue(street)
                && isBlankValue(postalCode)
                && isBlankValue(employeeCategory)
                && isBlankValue(employeeType)
                && isBlankValue(employmentMode)
                && isBlankValue(joiningDate)
                && isBlankValue(loginEnabled)
                && isBlankValue(remarks);
    }

    private boolean isBlankValue(String value) {
        return value == null || value.isBlank();
    }
}