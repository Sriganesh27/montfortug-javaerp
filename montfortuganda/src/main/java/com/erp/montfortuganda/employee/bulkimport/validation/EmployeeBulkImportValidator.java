package com.erp.montfortuganda.employee.bulkimport.validation;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.plugin.ImportValidatorChain;
import com.erp.montfortuganda.common.importframework.plugin.ValidationResult;
import com.erp.montfortuganda.employee.bulkimport.dto.EmployeeBulkImportRow;
import com.erp.montfortuganda.employee.bulkimport.excel.EmployeeExcelHeaders;
import com.erp.montfortuganda.employee.bulkimport.excel.EmployeeExcelValueParser;
import com.erp.montfortuganda.employee.bulkimport.service.EmployeeBulkReferenceService;
import com.erp.montfortuganda.employee.bulkimport.service.EmployeeBulkReferenceService.EmployeeBulkReferenceData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Employee-specific row validator.
 *
 * <p>This validator checks one Employee Excel row and reports exact
 * field-level errors. It does not create, update or save any Employee
 * record.</p>
 */
@Component
@RequiredArgsConstructor
public class EmployeeBulkImportValidator
        implements ImportValidatorChain<EmployeeBulkImportRow> {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern MOBILE_PATTERN =
            Pattern.compile("^[+0-9][0-9()\\-\\s]{6,29}$");

    private static final String REFERENCE_CACHE_KEY =
            "employee.bulk.references";

    private static final String OFFICIAL_EMAIL_CACHE_KEY =
            "employee.bulk.officialEmails";

    private static final String NATIONAL_ID_CACHE_KEY =
            "employee.bulk.nationalIds";

    private static final String MOBILE_CACHE_KEY =
            "employee.bulk.mobileNumbers";

    private final EmployeeExcelValueParser valueParser;
    private final EmployeeBulkReferenceService referenceService;

    @Override
    public ValidationResult validate(
            EmployeeBulkImportRow row,
            int rowNum,
            ImportContext context
    ) {
        List<ValidationResult.ValidationError> errors =
                new ArrayList<>();

        if (row == null || row.isBlank()) {
            return ValidationResult.builder()
                    .success(true)
                    .skipRow(true)
                    .errors(List.of())
                    .warnings(List.of())
                    .build();
        }

        Integer branchId =
                parseBranchId(context);

        EmployeeBulkReferenceData references =
                getReferences(
                        context,
                        branchId
                );

        validateRequiredName(
                row.getFirstName(),
                EmployeeExcelHeaders.FIRST_NAME,
                errors
        );

        validateRequiredName(
                row.getLastName(),
                EmployeeExcelHeaders.LAST_NAME,
                errors
        );

        validateDepartment(
                row,
                references,
                errors
        );

        validateDesignation(
                row,
                references,
                errors
        );

        validateReportingManager(
                row,
                references,
                errors
        );

        validateGender(
                row,
                errors
        );

        validateDateOfBirth(
                row,
                errors
        );

        validateJoiningDate(
                row,
                errors
        );

        validateDateRelationship(
                row,
                errors
        );

        validateEmployeeCategory(
                row,
                errors
        );

        validateEmployeeType(
                row,
                errors
        );

        validateEmploymentMode(
                row,
                errors
        );

        validateLoginEnabled(
                row,
                errors
        );

        validateOptionalEmail(
                row.getOfficialEmail(),
                EmployeeExcelHeaders.OFFICIAL_EMAIL,
                errors
        );

        validateOptionalEmail(
                row.getPersonalEmail(),
                EmployeeExcelHeaders.PERSONAL_EMAIL,
                errors
        );

        validateRequiredMobile(
                row.getMobileNumber(),
                errors
        );

        validateOptionalAlternateMobile(
                row.getAlternateMobile(),
                errors
        );

        validateMaximumLengths(
                row,
                errors
        );

        validateInFileDuplicates(
                row,
                context,
                errors
        );

        return ValidationResult.builder()
                .success(errors.isEmpty())
                .skipRow(false)
                .errors(List.copyOf(errors))
                .warnings(List.of())
                .build();
    }

    private Integer parseBranchId(
            ImportContext context
    ) {
        if (
                context == null
                        || context.getBranchId() == null
                        || context.getBranchId().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Employee import branch context is missing"
            );
        }

        try {
            int branchId =
                    Integer.parseInt(
                            context.getBranchId().trim()
                    );

            if (branchId <= 0) {
                throw new NumberFormatException(
                        "Branch ID must be positive"
                );
            }

            return branchId;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Employee import branch context is invalid"
            );
        }
    }

    private EmployeeBulkReferenceData getReferences(
            ImportContext context,
            Integer branchId
    ) {
        Object cached =
                context
                        .getJobStateCache()
                        .computeIfAbsent(
                                REFERENCE_CACHE_KEY,
                                ignored ->
                                        referenceService
                                                .loadReferences(
                                                        branchId
                                                )
                        );

        if (!(cached instanceof EmployeeBulkReferenceData data)) {
            throw new IllegalStateException(
                    "Employee import reference cache is invalid"
            );
        }

        if (!branchId.equals(data.getBranchId())) {
            throw new IllegalStateException(
                    "Employee import branch reference mismatch"
            );
        }

        return data;
    }

    private void validateDepartment(
            EmployeeBulkImportRow row,
            EmployeeBulkReferenceData references,
            List<ValidationResult.ValidationError> errors
    ) {
        String value =
                valueParser.nullableText(
                        row.getDepartmentName()
                );

        if (value == null) {
            addError(
                    errors,
                    EmployeeExcelHeaders.DEPARTMENT_NAME,
                    row.getDepartmentName(),
                    "EMPLOYEE_DEPARTMENT_REQUIRED",
                    "Department Name is required"
            );
            return;
        }

        String key =
                valueParser.normalizeLookupKey(
                        value
                );

        if (references.findDepartment(key) == null) {
            addError(
                    errors,
                    EmployeeExcelHeaders.DEPARTMENT_NAME,
                    row.getDepartmentName(),
                    "EMPLOYEE_DEPARTMENT_NOT_FOUND",
                    "Department does not exist or is inactive for this branch"
            );
        }
    }

    private void validateDesignation(
            EmployeeBulkImportRow row,
            EmployeeBulkReferenceData references,
            List<ValidationResult.ValidationError> errors
    ) {
        String value =
                valueParser.nullableText(
                        row.getDesignationName()
                );

        if (value == null) {
            addError(
                    errors,
                    EmployeeExcelHeaders.DESIGNATION_NAME,
                    row.getDesignationName(),
                    "EMPLOYEE_DESIGNATION_REQUIRED",
                    "Designation Name is required"
            );
            return;
        }

        String key =
                valueParser.normalizeLookupKey(
                        value
                );

        if (references.findDesignation(key) == null) {
            addError(
                    errors,
                    EmployeeExcelHeaders.DESIGNATION_NAME,
                    row.getDesignationName(),
                    "EMPLOYEE_DESIGNATION_NOT_FOUND",
                    "Designation does not exist or is inactive"
            );
        }
    }

    private void validateReportingManager(
            EmployeeBulkImportRow row,
            EmployeeBulkReferenceData references,
            List<ValidationResult.ValidationError> errors
    ) {
        String employeeNo =
                valueParser.nullableText(
                        row.getReportingManagerEmployeeNo()
                );

        if (employeeNo == null) {
            return;
        }

        String key =
                valueParser.normalizeLookupKey(
                        employeeNo
                );

        if (references.findReportingManager(key) == null) {
            addError(
                    errors,
                    EmployeeExcelHeaders
                            .REPORTING_MANAGER_EMPLOYEE_NO,
                    row.getReportingManagerEmployeeNo(),
                    "EMPLOYEE_REPORTING_MANAGER_NOT_FOUND",
                    "Reporting Manager Employee No does not exist in this branch"
            );
        }
    }

    private void validateGender(
            EmployeeBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        String value =
                valueParser.nullableText(
                        row.getGender()
                );

        if (value == null) {
            addError(
                    errors,
                    EmployeeExcelHeaders.GENDER,
                    row.getGender(),
                    "EMPLOYEE_GENDER_REQUIRED",
                    "Gender is required"
            );
            return;
        }

        try {
            valueParser.nullableGender(
                    value
            );
        } catch (IllegalArgumentException exception) {
            addError(
                    errors,
                    EmployeeExcelHeaders.GENDER,
                    row.getGender(),
                    "EMPLOYEE_GENDER_INVALID",
                    exception.getMessage()
            );
        }
    }

    private void validateDateOfBirth(
            EmployeeBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        LocalDate dateOfBirth;

        try {
            dateOfBirth =
                    valueParser.nullableDate(
                            row.getDateOfBirth(),
                            EmployeeExcelHeaders.DATE_OF_BIRTH
                    );
        } catch (IllegalArgumentException exception) {
            addError(
                    errors,
                    EmployeeExcelHeaders.DATE_OF_BIRTH,
                    row.getDateOfBirth(),
                    "EMPLOYEE_DATE_OF_BIRTH_INVALID",
                    exception.getMessage()
            );
            return;
        }

        if (dateOfBirth == null) {
            addError(
                    errors,
                    EmployeeExcelHeaders.DATE_OF_BIRTH,
                    row.getDateOfBirth(),
                    "EMPLOYEE_DATE_OF_BIRTH_REQUIRED",
                    "Date of Birth is required"
            );
            return;
        }

        LocalDate today =
                LocalDate.now();

        if (!dateOfBirth.isBefore(today)) {
            addError(
                    errors,
                    EmployeeExcelHeaders.DATE_OF_BIRTH,
                    row.getDateOfBirth(),
                    "EMPLOYEE_DATE_OF_BIRTH_FUTURE",
                    "Date of Birth must be before today"
            );
            return;
        }

        if (
                Period.between(
                        dateOfBirth,
                        today
                ).getYears() < 18
        ) {
            addError(
                    errors,
                    EmployeeExcelHeaders.DATE_OF_BIRTH,
                    row.getDateOfBirth(),
                    "EMPLOYEE_MINIMUM_AGE",
                    "Employee must be at least 18 years old"
            );
        }
    }

    private void validateJoiningDate(
            EmployeeBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        LocalDate joiningDate;

        try {
            joiningDate =
                    valueParser.nullableDate(
                            row.getJoiningDate(),
                            EmployeeExcelHeaders.JOINING_DATE
                    );
        } catch (IllegalArgumentException exception) {
            addError(
                    errors,
                    EmployeeExcelHeaders.JOINING_DATE,
                    row.getJoiningDate(),
                    "EMPLOYEE_JOINING_DATE_INVALID",
                    exception.getMessage()
            );
            return;
        }

        if (joiningDate == null) {
            addError(
                    errors,
                    EmployeeExcelHeaders.JOINING_DATE,
                    row.getJoiningDate(),
                    "EMPLOYEE_JOINING_DATE_REQUIRED",
                    "Joining Date is required"
            );
        }
    }

    private void validateDateRelationship(
            EmployeeBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        LocalDate dateOfBirth;
        LocalDate joiningDate;

        try {
            dateOfBirth =
                    valueParser.nullableDate(
                            row.getDateOfBirth(),
                            EmployeeExcelHeaders.DATE_OF_BIRTH
                    );

            joiningDate =
                    valueParser.nullableDate(
                            row.getJoiningDate(),
                            EmployeeExcelHeaders.JOINING_DATE
                    );
        } catch (IllegalArgumentException exception) {
            return;
        }

        if (
                dateOfBirth != null
                        && joiningDate != null
                        && !dateOfBirth.isBefore(joiningDate)
        ) {
            addError(
                    errors,
                    EmployeeExcelHeaders.JOINING_DATE,
                    row.getJoiningDate(),
                    "EMPLOYEE_DATE_ORDER_INVALID",
                    "Joining Date must be after Date of Birth"
            );
        }
    }

    private void validateEmployeeCategory(
            EmployeeBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        String value =
                valueParser.nullableText(
                        row.getEmployeeCategory()
                );

        if (value == null) {
            addError(
                    errors,
                    EmployeeExcelHeaders.EMPLOYEE_CATEGORY,
                    row.getEmployeeCategory(),
                    "EMPLOYEE_CATEGORY_REQUIRED",
                    "Employee Category is required"
            );
            return;
        }

        try {
            valueParser.nullableEmployeeCategory(
                    value
            );
        } catch (IllegalArgumentException exception) {
            addError(
                    errors,
                    EmployeeExcelHeaders.EMPLOYEE_CATEGORY,
                    row.getEmployeeCategory(),
                    "EMPLOYEE_CATEGORY_INVALID",
                    exception.getMessage()
            );
        }
    }

    private void validateEmployeeType(
            EmployeeBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        try {
            valueParser.requiredEmployeeType(
                    row.getEmployeeType()
            );
        } catch (IllegalArgumentException exception) {
            addError(
                    errors,
                    EmployeeExcelHeaders.EMPLOYEE_TYPE,
                    row.getEmployeeType(),
                    "EMPLOYEE_TYPE_INVALID",
                    exception.getMessage()
            );
        }
    }

    private void validateEmploymentMode(
            EmployeeBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        try {
            valueParser.requiredEmploymentMode(
                    row.getEmploymentMode()
            );
        } catch (IllegalArgumentException exception) {
            addError(
                    errors,
                    EmployeeExcelHeaders.EMPLOYMENT_MODE,
                    row.getEmploymentMode(),
                    "EMPLOYMENT_MODE_INVALID",
                    exception.getMessage()
            );
        }
    }

    private void validateLoginEnabled(
            EmployeeBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        try {
            valueParser.requiredYesNo(
                    row.getLoginEnabled(),
                    EmployeeExcelHeaders.LOGIN_ENABLED
            );
        } catch (IllegalArgumentException exception) {
            addError(
                    errors,
                    EmployeeExcelHeaders.LOGIN_ENABLED,
                    row.getLoginEnabled(),
                    "EMPLOYEE_LOGIN_ENABLED_INVALID",
                    exception.getMessage()
            );
        }
    }

    private void validateOptionalEmail(
            String value,
            String column,
            List<ValidationResult.ValidationError> errors
    ) {
        String email =
                valueParser.nullableText(
                        value
                );

        if (email == null) {
            return;
        }

        if (
                email.length() > 150
                        || !EMAIL_PATTERN
                        .matcher(email)
                        .matches()
        ) {
            addError(
                    errors,
                    column,
                    value,
                    "EMPLOYEE_EMAIL_INVALID",
                    column
                            + " must contain a valid email address"
            );
        }
    }

    private void validateRequiredMobile(
            String value,
            List<ValidationResult.ValidationError> errors
    ) {
        String mobile =
                valueParser.nullableText(value);

        if (mobile == null) {
            addError(
                    errors,
                    EmployeeExcelHeaders.MOBILE_NUMBER,
                    value,
                    "EMPLOYEE_MOBILE_REQUIRED",
                    "Mobile Number is required"
            );
            return;
        }

        if (!MOBILE_PATTERN.matcher(mobile).matches()) {
            addError(
                    errors,
                    EmployeeExcelHeaders.MOBILE_NUMBER,
                    value,
                    "EMPLOYEE_MOBILE_INVALID",
                    "Mobile Number contains an invalid mobile number"
            );
        }
    }

    private void validateOptionalAlternateMobile(
            String value,
            List<ValidationResult.ValidationError> errors
    ) {
        String mobile =
                valueParser.nullableText(value);

        if (mobile == null) {
            return;
        }

        if (!MOBILE_PATTERN.matcher(mobile).matches()) {
            addError(
                    errors,
                    EmployeeExcelHeaders.ALTERNATE_MOBILE,
                    value,
                    "EMPLOYEE_MOBILE_INVALID",
                    "Alternate Mobile contains an invalid mobile number"
            );
        }
    }

    private void validateRequiredName(
            String value,
            String column,
            List<ValidationResult.ValidationError> errors
    ) {
        String normalized =
                valueParser.nullableText(
                        value
                );

        if (normalized == null) {
            addError(
                    errors,
                    column,
                    value,
                    "EMPLOYEE_REQUIRED_VALUE_MISSING",
                    column + " is required"
            );
            return;
        }

        if (normalized.length() > 100) {
            addError(
                    errors,
                    column,
                    value,
                    "EMPLOYEE_VALUE_TOO_LONG",
                    column
                            + " cannot exceed 100 characters"
            );
        }
    }

    private void validateMaximumLengths(
            EmployeeBulkImportRow row,
            List<ValidationResult.ValidationError> errors
    ) {
        validateOptionalLength(
                row.getTitle(),
                EmployeeExcelHeaders.TITLE,
                20,
                errors
        );

        validateOptionalLength(
                row.getMiddleName(),
                EmployeeExcelHeaders.MIDDLE_NAME,
                100,
                errors
        );

        validateOptionalLength(
                row.getNationality(),
                EmployeeExcelHeaders.NATIONALITY,
                100,
                errors
        );

        validateOptionalLength(
                row.getNationalId(),
                EmployeeExcelHeaders.NATIONAL_ID,
                100,
                errors
        );

        validateOptionalLength(
                row.getDistrict(),
                EmployeeExcelHeaders.DISTRICT,
                100,
                errors
        );

        validateOptionalLength(
                row.getCounty(),
                EmployeeExcelHeaders.COUNTY,
                100,
                errors
        );

        validateOptionalLength(
                row.getSubCounty(),
                EmployeeExcelHeaders.SUB_COUNTY,
                100,
                errors
        );

        validateOptionalLength(
                row.getParish(),
                EmployeeExcelHeaders.PARISH,
                100,
                errors
        );

        validateOptionalLength(
                row.getVillage(),
                EmployeeExcelHeaders.VILLAGE,
                150,
                errors
        );

        validateOptionalLength(
                row.getStreet(),
                EmployeeExcelHeaders.STREET,
                255,
                errors
        );

        validateOptionalLength(
                row.getPostalCode(),
                EmployeeExcelHeaders.POSTAL_CODE,
                30,
                errors
        );

        validateOptionalLength(
                row.getRemarks(),
                EmployeeExcelHeaders.REMARKS,
                10000,
                errors
        );
    }

    private void validateOptionalLength(
            String value,
            String column,
            int maximumLength,
            List<ValidationResult.ValidationError> errors
    ) {
        String normalized =
                valueParser.nullableText(
                        value
                );

        if (
                normalized != null
                        && normalized.length() > maximumLength
        ) {
            addError(
                    errors,
                    column,
                    value,
                    "EMPLOYEE_VALUE_TOO_LONG",
                    column
                            + " cannot exceed "
                            + maximumLength
                            + " characters"
            );
        }
    }

    private void validateInFileDuplicates(
            EmployeeBulkImportRow row,
            ImportContext context,
            List<ValidationResult.ValidationError> errors
    ) {
        validateDuplicateValue(
                context,
                OFFICIAL_EMAIL_CACHE_KEY,
                row.getOfficialEmail(),
                EmployeeExcelHeaders.OFFICIAL_EMAIL,
                "EMPLOYEE_DUPLICATE_OFFICIAL_EMAIL",
                errors
        );

        validateDuplicateValue(
                context,
                NATIONAL_ID_CACHE_KEY,
                row.getNationalId(),
                EmployeeExcelHeaders.NATIONAL_ID,
                "EMPLOYEE_DUPLICATE_NATIONAL_ID",
                errors
        );

        validateDuplicateValue(
                context,
                MOBILE_CACHE_KEY,
                row.getMobileNumber(),
                EmployeeExcelHeaders.MOBILE_NUMBER,
                "EMPLOYEE_DUPLICATE_MOBILE",
                errors
        );
    }

    @SuppressWarnings("unchecked")
    private void validateDuplicateValue(
            ImportContext context,
            String cacheKey,
            String rawValue,
            String column,
            String errorCode,
            List<ValidationResult.ValidationError> errors
    ) {
        String value =
                normalizeDuplicateValue(
                        rawValue
                );

        if (value == null) {
            return;
        }

        Object cached =
                context
                        .getJobStateCache()
                        .computeIfAbsent(
                                cacheKey,
                                ignored ->
                                        ConcurrentHashMap
                                                .newKeySet()
                        );

        if (!(cached instanceof Set<?> rawSet)) {
            throw new IllegalStateException(
                    "Employee duplicate cache is invalid"
            );
        }

        Set<String> values =
                (Set<String>) rawSet;

        if (!values.add(value)) {
            addError(
                    errors,
                    column,
                    rawValue,
                    errorCode,
                    column
                            + " is duplicated inside the workbook"
            );
        }
    }

    private String normalizeDuplicateValue(
            String value
    ) {
        String normalized =
                valueParser.nullableText(
                        value
                );

        if (normalized == null) {
            return null;
        }

        return normalized
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private void addError(
            List<ValidationResult.ValidationError> errors,
            String columnName,
            String cellValue,
            String errorCode,
            String message
    ) {
        errors.add(
                ValidationResult.ValidationError
                        .builder()
                        .columnName(columnName)
                        .cellValue(cellValue)
                        .errorCode(errorCode)
                        .message(message)
                        .suggestedFix(
                                EmployeeExcelHeaders
                                        .ENTER_VALID_DATA
                        )
                        .build()
        );
    }
}

