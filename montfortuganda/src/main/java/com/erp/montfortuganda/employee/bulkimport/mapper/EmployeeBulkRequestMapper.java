package com.erp.montfortuganda.employee.bulkimport.mapper;

import com.erp.montfortuganda.employee.bulkimport.dto.EmployeeBulkImportOptions;
import com.erp.montfortuganda.employee.bulkimport.dto.EmployeeBulkImportRow;
import com.erp.montfortuganda.employee.bulkimport.excel.EmployeeExcelHeaders;
import com.erp.montfortuganda.employee.bulkimport.excel.EmployeeExcelValueParser;
import com.erp.montfortuganda.employee.bulkimport.service.EmployeeBulkCategoryResolver;
import com.erp.montfortuganda.employee.bulkimport.service.EmployeeBulkReferenceService.EmployeeBulkReferenceData;
import com.erp.montfortuganda.employee.dto.request.EmployeeAccountRequest;
import com.erp.montfortuganda.employee.dto.request.EmployeeRegistrationRequest;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.employee.enums.EmployeeType;
import com.erp.montfortuganda.employee.enums.EmploymentMode;
import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import com.erp.montfortuganda.employee.enums.Gender;
import com.erp.montfortuganda.school.entity.Department;
import com.erp.montfortuganda.school.entity.Designation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Converts one validated Employee Excel row into the existing
 * EmployeeRegistrationRequest.
 *
 * <p>This mapper is used only by Employee bulk import. It does not save
 * anything to the database.</p>
 *
 * <p>Bulk-import policy:</p>
 * <ul>
 *     <li>First Name, Joining Date/Year and Employee Category are required.</li>
 *     <li>Optional blank or invalid values are mapped to {@code null}.</li>
 *     <li>The corrected-workbook placeholder is never stored.</li>
 *     <li>A four-digit Joining Year is parsed by the bulk parser as
 *     January 1 of that year.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class EmployeeBulkRequestMapper {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern MOBILE_PATTERN =
            Pattern.compile("^[+0-9][0-9()\\-\\s]{6,29}$");

    private final EmployeeExcelValueParser valueParser;
    private final EmployeeBulkCategoryResolver categoryResolver;

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
                resolveDepartment(
                        row,
                        references
                );

        Designation designation =
                resolveDesignation(
                        row,
                        references
                );

        ErpEmployee reportingManager =
                resolveReportingManager(
                        row,
                        references
                );

        Gender gender =
                resolveOptionalGender(
                        row.getGender()
                );

        LocalDate dateOfBirth =
                resolveOptionalDate(
                        row.getDateOfBirth(),
                        EmployeeExcelHeaders.DATE_OF_BIRTH
                );

        EmployeeCategory employeeCategory =
                categoryResolver.resolve(
                        nullableBulkText(
                                row.getEmployeeCategory(),
                                100
                        ),
                        designation
                );

        if (employeeCategory == null) {
            throw new IllegalArgumentException(
                    "Employee Category is required when it cannot "
                            + "be inferred from Designation."
            );
        }

        EmployeeType employeeType =
                resolveOptionalEmployeeType(
                        row.getEmployeeType()
                );

        EmploymentMode employmentMode =
                resolveOptionalEmploymentMode(
                        row.getEmploymentMode()
                );

        LocalDate joiningDate =
                valueParser.requiredJoiningDate(
                        row.getJoiningDate(),
                        EmployeeExcelHeaders.JOINING_DATE
                );

        Boolean parsedLoginEnabled =
                resolveOptionalYesNo(
                        row.getLoginEnabled(),
                        EmployeeExcelHeaders.LOGIN_ENABLED
                );

        boolean excelLoginEnabled =
                Boolean.TRUE.equals(parsedLoginEnabled);

        String officialEmail =
                resolveOptionalEmail(
                        row.getOfficialEmail()
                );

        EmployeeAccountRequest accountRequest =
                buildAccountRequest(
                        options,
                        excelLoginEnabled,
                        officialEmail
                );

        return new EmployeeRegistrationRequest(
                nullableBulkText(
                        row.getTitle(),
                        20
                ),
                requiredBulkText(
                        row.getFirstName(),
                        EmployeeExcelHeaders.FIRST_NAME,
                        100
                ),
                nullableBulkText(
                        row.getMiddleName(),
                        100
                ),
                nullableBulkText(
                        row.getLastName(),
                        100
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

                officialEmail,
                resolveOptionalEmail(
                        row.getPersonalEmail()
                ),
                resolveOptionalMobile(
                        row.getMobileNumber()
                ),
                resolveOptionalMobile(
                        row.getAlternateMobile()
                ),

                department == null
                        ? null
                        : department.getDepartmentId(),
                designation == null
                        ? null
                        : designation.getDesignationId(),
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

                nullableBulkText(
                        row.getNationality(),
                        100
                ),
                nullableBulkText(
                        row.getNationalId(),
                        100
                ),

                null,
                null,
                null,
                null,
                null,

                null,
                null,

                nullableBulkText(
                        row.getDistrict(),
                        100
                ),
                nullableBulkText(
                        row.getCounty(),
                        100
                ),
                nullableBulkText(
                        row.getSubCounty(),
                        100
                ),
                nullableBulkText(
                        row.getParish(),
                        100
                ),
                nullableBulkText(
                        row.getVillage(),
                        150
                ),
                nullableBulkText(
                        row.getStreet(),
                        255
                ),
                nullableBulkText(
                        row.getPostalCode(),
                        30
                ),

                null,
                null,
                nullableBulkText(
                        row.getRemarks(),
                        10000
                ),

                List.of(),
                List.of(),
                List.of(),
                List.of(),

                accountRequest
        );
    }

    /**
     * Department is optional for Employee bulk import.
     * Blank or unknown values are stored as null.
     */
    private Department resolveDepartment(
            EmployeeBulkImportRow row,
            EmployeeBulkReferenceData references
    ) {
        String value =
                nullableBulkText(
                        row.getDepartmentName(),
                        255
                );

        if (value == null) {
            return null;
        }

        String key =
                valueParser.normalizeLookupKey(
                        value
                );

        return references.findDepartment(key);
    }

    /**
     * Designation is optional for Employee bulk import.
     * Blank or unknown values are stored as null.
     */
    private Designation resolveDesignation(
            EmployeeBulkImportRow row,
            EmployeeBulkReferenceData references
    ) {
        String value =
                nullableBulkText(
                        row.getDesignationName(),
                        255
                );

        if (value == null) {
            return null;
        }

        String key =
                valueParser.normalizeLookupKey(
                        value
                );

        return references.findDesignation(key);
    }

    /**
     * Reporting Manager is optional for Employee bulk import.
     * Blank or unknown employee numbers are stored as null.
     */
    private ErpEmployee resolveReportingManager(
            EmployeeBulkImportRow row,
            EmployeeBulkReferenceData references
    ) {
        String employeeNo =
                nullableBulkText(
                        row.getReportingManagerEmployeeNo(),
                        50
                );

        if (employeeNo == null) {
            return null;
        }

        String key =
                valueParser.normalizeLookupKey(
                        employeeNo
                );

        return references.findReportingManager(key);
    }

    private Gender resolveOptionalGender(
            String rawValue
    ) {
        if (nullableBulkText(rawValue, 50) == null) {
            return null;
        }

        try {
            return valueParser.nullableGender(
                    rawValue
            );
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private LocalDate resolveOptionalDate(
            String rawValue,
            String columnName
    ) {
        if (nullableBulkText(rawValue, 100) == null) {
            return null;
        }

        try {
            return valueParser.nullableDate(
                    rawValue,
                    columnName
            );
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private EmployeeType resolveOptionalEmployeeType(
            String rawValue
    ) {
        if (nullableBulkText(rawValue, 100) == null) {
            return null;
        }

        try {
            return valueParser.requiredEmployeeType(
                    rawValue
            );
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private EmploymentMode resolveOptionalEmploymentMode(
            String rawValue
    ) {
        if (nullableBulkText(rawValue, 100) == null) {
            return null;
        }

        try {
            return valueParser.requiredEmploymentMode(
                    rawValue
            );
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private Boolean resolveOptionalYesNo(
            String rawValue,
            String columnName
    ) {
        if (nullableBulkText(rawValue, 50) == null) {
            return null;
        }

        try {
            return valueParser.nullableYesNo(
                    rawValue,
                    columnName
            );
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String resolveOptionalEmail(
            String rawValue
    ) {
        String email =
                nullableBulkText(
                        rawValue,
                        150
                );

        if (
                email == null
                        || !EMAIL_PATTERN
                        .matcher(email)
                        .matches()
        ) {
            return null;
        }

        return email;
    }

    private String resolveOptionalMobile(
            String rawValue
    ) {
        String mobile =
                nullableBulkText(
                        rawValue,
                        30
                );

        if (
                mobile == null
                        || !MOBILE_PATTERN
                        .matcher(mobile)
                        .matches()
        ) {
            return null;
        }

        return mobile;
    }

    private String requiredBulkText(
            String rawValue,
            String fieldName,
            int maximumLength
    ) {
        String value =
                nullableBulkText(
                        rawValue,
                        maximumLength
                );

        if (value == null) {
            throw new IllegalArgumentException(
                    fieldName + " is required."
            );
        }

        return value;
    }

    /**
     * Returns null for blank values, the correction-workbook placeholder,
     * or values exceeding the database field length.
     */
    private String nullableBulkText(
            String rawValue,
            int maximumLength
    ) {
        String value =
                valueParser.nullableText(
                        rawValue
                );

        if (
                value == null
                        || isCorrectionPlaceholder(value)
                        || value.length() > maximumLength
        ) {
            return null;
        }

        return value;
    }

    private boolean isCorrectionPlaceholder(
            String value
    ) {
        return value.equalsIgnoreCase(
                EmployeeExcelHeaders.ENTER_VALID_DATA
        );
    }

    /**
     * Login creation is skipped for an incomplete bulk row instead of
     * rejecting the Employee record.
     */
    private EmployeeAccountRequest buildAccountRequest(
            EmployeeBulkImportOptions options,
            boolean excelLoginEnabled,
            String officialEmail
    ) {
        boolean generateLogin =
                options.isCreateCredentials()
                        && excelLoginEnabled
                        && officialEmail != null;

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

        return new EmployeeAccountRequest(
                generateLogin,
                roleId,
                sendEmail
        );
    }
}
