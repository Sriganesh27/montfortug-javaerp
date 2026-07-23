package com.erp.montfortuganda.employee.bulkimport.mapper;

import com.erp.montfortuganda.employee.bulkimport.dto.EmployeeBulkImportOptions;
import com.erp.montfortuganda.employee.bulkimport.dto.EmployeeBulkImportRow;
import com.erp.montfortuganda.employee.bulkimport.excel.EmployeeExcelValueParser;
import com.erp.montfortuganda.employee.bulkimport.service.EmployeeBulkReferenceService.EmployeeBulkReferenceData;
import com.erp.montfortuganda.employee.dto.request.EmployeeAccountRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeRegistrationRequest;
import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.employee.enums.EmployeeType;
import com.erp.montfortuganda.employee.enums.EmploymentMode;
import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import com.erp.montfortuganda.employee.enums.Gender;
import com.erp.montfortuganda.school.entity.Department;
import com.erp.montfortuganda.school.entity.Designation;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Converts one validated Employee Excel row into the existing
 * EmployeeRegistrationRequest.
 *
 * <p>This mapper does not save anything to the database.</p>
 */
@Component
@RequiredArgsConstructor
public class EmployeeBulkRequestMapper {

    private final EmployeeExcelValueParser valueParser;

    public EmployeeRegistrationRequest toRegistrationRequest(
            EmployeeBulkImportRow row,
            EmployeeBulkImportOptions options,
            EmployeeBulkReferenceData references
    ) {
        Objects.requireNonNull(
                row,
                "Employee bulk-import row is required."
        );

        Objects.requireNonNull(
                options,
                "Employee bulk-import options are required."
        );

        Objects.requireNonNull(
                references,
                "Employee bulk-import references are required."
        );

        options.validate();

        Department department =
                requireDepartment(
                        row,
                        references
                );

        Designation designation =
                requireDesignation(
                        row,
                        references
                );

        ErpEmployee reportingManager =
                resolveReportingManager(
                        row,
                        references
                );

        Gender gender =
                requireGender(row);

        LocalDate dateOfBirth =
                valueParser.requiredDate(
                        row.getDateOfBirth(),
                        "Date of Birth"
                );

        EmployeeCategory employeeCategory =
                requireEmployeeCategory(row);

        EmployeeType employeeType =
                valueParser.requiredEmployeeType(
                        row.getEmployeeType()
                );

        EmploymentMode employmentMode =
                valueParser.requiredEmploymentMode(
                        row.getEmploymentMode()
                );

        LocalDate joiningDate =
                valueParser.requiredDate(
                        row.getJoiningDate(),
                        "Joining Date"
                );

        boolean excelLoginEnabled =
                valueParser.requiredYesNo(
                        row.getLoginEnabled(),
                        "Login Enabled"
                );

        EmployeeAccountRequest accountRequest =
                buildAccountRequest(
                        row,
                        options,
                        excelLoginEnabled
                );


        return new EmployeeRegistrationRequest(
                valueParser.nullableText(row.getTitle()),
                valueParser.requiredText(
                        row.getFirstName(),
                        "First Name"
                ),
                valueParser.nullableText(row.getMiddleName()),
                valueParser.requiredText(
                        row.getLastName(),
                        "Last Name"
                ),

                gender,
                dateOfBirth,

                null,
                null,
                null,
                null,

                null,
                null,
                null,
                null,

                null,
                null,
                null,
                null,

                valueParser.nullableText(
                        row.getOfficialEmail()
                ),
                valueParser.nullableText(
                        row.getPersonalEmail()
                ),
                valueParser.requiredText(
                        row.getMobileNumber(),
                        "Mobile Number"
                ),
                valueParser.nullableText(
                        row.getAlternateMobile()
                ),

                department.getDepartmentId(),
                designation.getDesignationId(),
                reportingManager == null
                        ? null
                        : reportingManager.getEmployeeId(),

                employeeCategory,
                employeeType,
                employmentMode,
                EmploymentStatus.ACTIVE,

                joiningDate,
                null,
                null,
                null,

                valueParser.nullableText(
                        row.getNationality()
                ),
                valueParser.nullableText(
                        row.getNationalId()
                ),

                null,
                null,
                null,
                null,
                null,

                null,
                null,

                valueParser.nullableText(
                        row.getDistrict()
                ),
                valueParser.nullableText(
                        row.getCounty()
                ),
                valueParser.nullableText(
                        row.getSubCounty()
                ),
                valueParser.nullableText(
                        row.getParish()
                ),
                valueParser.nullableText(
                        row.getVillage()
                ),
                valueParser.nullableText(
                        row.getStreet()
                ),
                valueParser.nullableText(
                        row.getPostalCode()
                ),

                null,
                null,
                valueParser.nullableText(
                        row.getRemarks()
                ),

                List.of(),
                List.of(),
                List.of(),
                List.of(),

                accountRequest
        );
    }

    private Department requireDepartment(
            EmployeeBulkImportRow row,
            EmployeeBulkReferenceData references
    ) {
        String key =
                valueParser.normalizeLookupKey(
                        row.getDepartmentName()
                );

        Department department =
                references.findDepartment(key);

        if (department == null) {
            throw new IllegalArgumentException(
                    "Department does not exist for this branch."
            );
        }

        return department;
    }

    private Designation requireDesignation(
            EmployeeBulkImportRow row,
            EmployeeBulkReferenceData references
    ) {
        String key =
                valueParser.normalizeLookupKey(
                        row.getDesignationName()
                );

        Designation designation =
                references.findDesignation(key);

        if (designation == null) {
            throw new IllegalArgumentException(
                    "Designation does not exist or is inactive."
            );
        }

        return designation;
    }

    private ErpEmployee resolveReportingManager(
            EmployeeBulkImportRow row,
            EmployeeBulkReferenceData references
    ) {
        String employeeNo =
                valueParser.nullableText(
                        row.getReportingManagerEmployeeNo()
                );

        if (employeeNo == null) {
            return null;
        }

        String key =
                valueParser.normalizeLookupKey(
                        employeeNo
                );

        ErpEmployee reportingManager =
                references.findReportingManager(key);

        if (reportingManager == null) {
            throw new IllegalArgumentException(
                    "Reporting Manager Employee No does not exist "
                            + "in this branch."
            );
        }

        return reportingManager;
    }

    private Gender requireGender(
            EmployeeBulkImportRow row
    ) {
        Gender gender =
                valueParser.nullableGender(
                        row.getGender()
                );

        if (gender == null) {
            throw new IllegalArgumentException(
                    "Gender is required."
            );
        }

        return gender;
    }

    private EmployeeCategory requireEmployeeCategory(
            EmployeeBulkImportRow row
    ) {
        EmployeeCategory category =
                valueParser.nullableEmployeeCategory(
                        row.getEmployeeCategory()
                );

        if (category == null) {
            throw new IllegalArgumentException(
                    "Employee Category is required."
            );
        }

        return category;
    }

    private EmployeeAccountRequest buildAccountRequest(
            EmployeeBulkImportRow row,
            EmployeeBulkImportOptions options,
            boolean excelLoginEnabled
    ) {
        boolean generateLogin =
                options.isCreateCredentials()
                        && excelLoginEnabled;

        boolean sendEmail =
                generateLogin
                        && options.isSendEmail();

        Long roleId =
                generateLogin
                        ? options.getRoleId()
                        : null;

        if (generateLogin && roleId == null) {
            throw new IllegalArgumentException(
                    "A login role must be selected when "
                            + "Create Credentials is enabled."
            );
        }

        String officialEmail =
                valueParser.nullableText(
                        row.getOfficialEmail()
                );

        if (generateLogin && officialEmail == null) {
            throw new IllegalArgumentException(
                    "Official Email is required when login "
                            + "credentials are created."
            );
        }

        return new EmployeeAccountRequest(
                generateLogin,
                roleId,
                sendEmail
        );
    }
}