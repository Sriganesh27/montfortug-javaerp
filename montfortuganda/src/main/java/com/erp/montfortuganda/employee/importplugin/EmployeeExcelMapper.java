package com.erp.montfortuganda.employee.importplugin;

import com.erp.montfortuganda.common.importframework.plugin.ExcelRowMapper;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class EmployeeExcelMapper implements ExcelRowMapper<EmployeeImportDTO> {
    @Override
    public EmployeeImportDTO mapRow(Object rowData, int rowNumber) {
        @SuppressWarnings("unchecked")
        Map<String, String> row = (Map<String, String>) rowData;
        
        return EmployeeImportDTO.builder()
                // Organization
                .departmentName(row.get("Department Name"))
                .designationName(row.get("Designation Name"))
                .reportingManagerEmployeeNo(row.get("Reporting Manager Employee No"))
                
                // Personal Details
                .title(row.get("Title"))
                .firstName(row.get("First Name"))
                .middleName(row.get("Middle Name"))
                .lastName(row.get("Last Name"))
                .gender(row.get("Gender"))
                .dateOfBirth(row.get("Date of Birth (YYYY-MM-DD)"))
                .nationality(row.get("Nationality"))
                .nationalId(row.get("National ID"))
                
                // Contact Details
                .officialEmail(row.get("Official Email"))
                .personalEmail(row.get("Personal Email"))
                .mobileNumber(row.get("Mobile Number"))
                .alternateMobile(row.get("Alternate Mobile"))
                
                // Address
                .district(row.get("District"))
                .county(row.get("County"))
                .subCounty(row.get("Sub County"))
                .parish(row.get("Parish"))
                .village(row.get("Village (LC-I)"))
                .street(row.get("Street"))
                .postalCode(row.get("Postal Code"))
                
                // Employment
                .employeeCategory(row.get("Employee Category"))
                .employeeType(row.get("Employee Type"))
                .employmentMode(row.get("Employment Mode"))
                .joiningDate(row.get("Joining Date (YYYY-MM-DD)"))
                
                // Auth & Misc
                .loginEnabled(row.get("Login Enabled"))
                .remarks(row.get("Remarks"))
                .build();
    }
}
