package com.erp.montfortuganda.employee.bulkimport.excel;

import com.erp.montfortuganda.common.importframework.plugin.ExcelRowMapper;
import com.erp.montfortuganda.employee.bulkimport.dto.EmployeeBulkImportRow;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Maps one generic Excel row into an EmployeeBulkImportRow.
 *
 * GenericExcelReader supplies each row as Map<header, formatted value>.
 */
@Component
public class EmployeeExcelRowMapper
        implements ExcelRowMapper<EmployeeBulkImportRow> {

    @Override
    public EmployeeBulkImportRow mapRow(
            Object rowData,
            int rowNumber
    ) {
        if (!(rowData instanceof Map<?, ?> rawMap)) {
            throw new IllegalArgumentException(
                    "Employee import row must be a header-value map"
            );
        }

        return EmployeeBulkImportRow.builder()
                .excelRowNumber(rowNumber)
                .departmentName(
                        value(rawMap, EmployeeExcelHeaders.DEPARTMENT_NAME)
                )
                .designationName(
                        value(rawMap, EmployeeExcelHeaders.DESIGNATION_NAME)
                )
                .reportingManagerEmployeeNo(
                        value(
                                rawMap,
                                EmployeeExcelHeaders
                                        .REPORTING_MANAGER_EMPLOYEE_NO
                        )
                )
                .title(
                        value(rawMap, EmployeeExcelHeaders.TITLE)
                )
                .firstName(
                        value(rawMap, EmployeeExcelHeaders.FIRST_NAME)
                )
                .middleName(
                        value(rawMap, EmployeeExcelHeaders.MIDDLE_NAME)
                )
                .lastName(
                        value(rawMap, EmployeeExcelHeaders.LAST_NAME)
                )
                .gender(
                        value(rawMap, EmployeeExcelHeaders.GENDER)
                )
                .dateOfBirth(
                        value(rawMap, EmployeeExcelHeaders.DATE_OF_BIRTH)
                )
                .nationality(
                        value(rawMap, EmployeeExcelHeaders.NATIONALITY)
                )
                .nationalId(
                        value(rawMap, EmployeeExcelHeaders.NATIONAL_ID)
                )
                .officialEmail(
                        value(rawMap, EmployeeExcelHeaders.OFFICIAL_EMAIL)
                )
                .personalEmail(
                        value(rawMap, EmployeeExcelHeaders.PERSONAL_EMAIL)
                )
                .mobileNumber(
                        value(rawMap, EmployeeExcelHeaders.MOBILE_NUMBER)
                )
                .alternateMobile(
                        value(rawMap, EmployeeExcelHeaders.ALTERNATE_MOBILE)
                )
                .district(
                        value(rawMap, EmployeeExcelHeaders.DISTRICT)
                )
                .county(
                        value(rawMap, EmployeeExcelHeaders.COUNTY)
                )
                .subCounty(
                        value(rawMap, EmployeeExcelHeaders.SUB_COUNTY)
                )
                .parish(
                        value(rawMap, EmployeeExcelHeaders.PARISH)
                )
                .village(
                        value(rawMap, EmployeeExcelHeaders.VILLAGE)
                )
                .street(
                        value(rawMap, EmployeeExcelHeaders.STREET)
                )
                .postalCode(
                        value(rawMap, EmployeeExcelHeaders.POSTAL_CODE)
                )
                .employeeCategory(
                        value(
                                rawMap,
                                EmployeeExcelHeaders.EMPLOYEE_CATEGORY
                        )
                )
                .employeeType(
                        value(rawMap, EmployeeExcelHeaders.EMPLOYEE_TYPE)
                )
                .employmentMode(
                        value(rawMap, EmployeeExcelHeaders.EMPLOYMENT_MODE)
                )
                .joiningDate(
                        value(rawMap, EmployeeExcelHeaders.JOINING_DATE)
                )
                .loginEnabled(
                        value(rawMap, EmployeeExcelHeaders.LOGIN_ENABLED)
                )
                .remarks(
                        value(rawMap, EmployeeExcelHeaders.REMARKS)
                )
                .build();
    }

    private String value(
            Map<?, ?> row,
            String header
    ) {
        Object rawValue = row.get(header);

        if (rawValue == null) {
            return null;
        }

        String value = rawValue.toString().trim();

        return value.isEmpty() ? null : value;
    }
}