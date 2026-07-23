package com.erp.montfortuganda.employee.bulkimport.excel;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Exact header contract for Employee_Bulk_Import_ (1).xlsx.
 *
 * Do not rename or reorder these constants unless the approved Excel
 * template itself changes.
 */
public final class EmployeeExcelHeaders {

    public static final String EMPLOYEE_SHEET = "Employees";
    public static final String REFERENCE_LISTS_SHEET = "Reference Lists";

    public static final String DEPARTMENT_NAME =
            "Department Name";

    public static final String DESIGNATION_NAME =
            "Designation Name";

    public static final String REPORTING_MANAGER_EMPLOYEE_NO =
            "Reporting Manager Employee No";

    public static final String TITLE =
            "Title";

    public static final String FIRST_NAME =
            "First Name";

    public static final String MIDDLE_NAME =
            "Middle Name";

    public static final String LAST_NAME =
            "Last Name";

    public static final String GENDER =
            "Gender";

    public static final String DATE_OF_BIRTH =
            "Date of Birth (YYYY-MM-DD)";

    public static final String NATIONALITY =
            "Nationality";

    public static final String NATIONAL_ID =
            "National ID";

    public static final String OFFICIAL_EMAIL =
            "Official Email";

    public static final String PERSONAL_EMAIL =
            "Personal Email";

    public static final String MOBILE_NUMBER =
            "Mobile Number";

    public static final String ALTERNATE_MOBILE =
            "Alternate Mobile";

    public static final String DISTRICT =
            "District";

    public static final String COUNTY =
            "County";

    public static final String SUB_COUNTY =
            "Sub County";

    public static final String PARISH =
            "Parish";

    public static final String VILLAGE =
            "Village (LC-I)";

    public static final String STREET =
            "Street";

    public static final String POSTAL_CODE =
            "Postal Code";

    public static final String EMPLOYEE_CATEGORY =
            "Employee Category";

    public static final String EMPLOYEE_TYPE =
            "Employee Type";

    public static final String EMPLOYMENT_MODE =
            "Employment Mode";

    public static final String JOINING_DATE =
            "Joining Date (YYYY-MM-DD)";

    public static final String LOGIN_ENABLED =
            "Login Enabled";

    public static final String REMARKS =
            "Remarks";

    public static final String ENTER_VALID_DATA =
            "ENTER VALID DATA";

    /**
     * Exact approved order from the uploaded workbook.
     */
    public static final List<String> ALL_HEADERS = List.of(
            DEPARTMENT_NAME,
            DESIGNATION_NAME,
            REPORTING_MANAGER_EMPLOYEE_NO,
            TITLE,
            FIRST_NAME,
            MIDDLE_NAME,
            LAST_NAME,
            GENDER,
            DATE_OF_BIRTH,
            NATIONALITY,
            NATIONAL_ID,
            OFFICIAL_EMAIL,
            PERSONAL_EMAIL,
            MOBILE_NUMBER,
            ALTERNATE_MOBILE,
            DISTRICT,
            COUNTY,
            SUB_COUNTY,
            PARISH,
            VILLAGE,
            STREET,
            POSTAL_CODE,
            EMPLOYEE_CATEGORY,
            EMPLOYEE_TYPE,
            EMPLOYMENT_MODE,
            JOINING_DATE,
            LOGIN_ENABLED,
            REMARKS
    );

    /**
     * Required according to the current erp_employees database and
     * approved Employee import business rules.
     */
    public static final Set<String> REQUIRED_HEADERS = Set.of(
            DEPARTMENT_NAME,
            DESIGNATION_NAME,
            FIRST_NAME,
            LAST_NAME,
            EMPLOYEE_TYPE,
            EMPLOYMENT_MODE,
            LOGIN_ENABLED
    );

    /**
     * Date columns available in the current Excel template.
     */
    public static final Set<String> DATE_HEADERS = Set.of(
            DATE_OF_BIRTH,
            JOINING_DATE
    );

    /**
     * Columns that support fixed-value validation/dropdowns.
     */
    public static final Set<String> DROPDOWN_HEADERS = Set.of(
            GENDER,
            EMPLOYEE_CATEGORY,
            EMPLOYEE_TYPE,
            EMPLOYMENT_MODE,
            LOGIN_ENABLED
    );

    /**
     * Zero-based positions matching the uploaded workbook.
     */
    public static final Map<String, Integer> COLUMN_INDEX = Map.ofEntries(
            Map.entry(DEPARTMENT_NAME, 0),
            Map.entry(DESIGNATION_NAME, 1),
            Map.entry(REPORTING_MANAGER_EMPLOYEE_NO, 2),
            Map.entry(TITLE, 3),
            Map.entry(FIRST_NAME, 4),
            Map.entry(MIDDLE_NAME, 5),
            Map.entry(LAST_NAME, 6),
            Map.entry(GENDER, 7),
            Map.entry(DATE_OF_BIRTH, 8),
            Map.entry(NATIONALITY, 9),
            Map.entry(NATIONAL_ID, 10),
            Map.entry(OFFICIAL_EMAIL, 11),
            Map.entry(PERSONAL_EMAIL, 12),
            Map.entry(MOBILE_NUMBER, 13),
            Map.entry(ALTERNATE_MOBILE, 14),
            Map.entry(DISTRICT, 15),
            Map.entry(COUNTY, 16),
            Map.entry(SUB_COUNTY, 17),
            Map.entry(PARISH, 18),
            Map.entry(VILLAGE, 19),
            Map.entry(STREET, 20),
            Map.entry(POSTAL_CODE, 21),
            Map.entry(EMPLOYEE_CATEGORY, 22),
            Map.entry(EMPLOYEE_TYPE, 23),
            Map.entry(EMPLOYMENT_MODE, 24),
            Map.entry(JOINING_DATE, 25),
            Map.entry(LOGIN_ENABLED, 26),
            Map.entry(REMARKS, 27)
    );

    private EmployeeExcelHeaders() {
        throw new IllegalStateException(
                "EmployeeExcelHeaders is a utility class"
        );
    }
}