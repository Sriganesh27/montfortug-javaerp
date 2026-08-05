package com.erp.montfortuganda.employee.bulkimport.validation;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.plugin.ImportValidatorChain;
import com.erp.montfortuganda.common.importframework.plugin.ValidationResult;
import com.erp.montfortuganda.employee.bulkimport.dto.EmployeeBulkImportRow;
import com.erp.montfortuganda.employee.bulkimport.excel.EmployeeExcelHeaders;
import com.erp.montfortuganda.employee.bulkimport.excel.EmployeeExcelValueParser;
import com.erp.montfortuganda.employee.bulkimport.service.EmployeeBulkCategoryResolver;
import com.erp.montfortuganda.employee.bulkimport.service.EmployeeBulkReferenceService;
import com.erp.montfortuganda.employee.bulkimport.service.EmployeeBulkReferenceService.EmployeeBulkReferenceData;
import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.school.entity.Designation;
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
    private final EmployeeBulkCategoryResolver categoryResolver;

    @Override
    public ValidationResult validate(
            EmployeeBulkImportRow row,
            int rowNum,
            ImportContext context
    ) {
        List<ValidationResult.ValidationError> errors =
                new ArrayList<>();

        List<ValidationResult.ValidationWarning> warnings =
                new ArrayList<>();

        if (row == null || row.isBlank()) {
            return ValidationResult.builder()
                    .success(true)
                    .skipRow(true)
                    .errors(List.of())
                    .warnings(List.copyOf(warnings))
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

        validateOptionalName(
                row.getLastName(),
                EmployeeExcelHeaders.LAST_NAME,
                warnings
        );

        validateDepartment(
                row,
                references,
                warnings
        );

        validateDesignation(
                row,
                references,
                warnings
        );

        validateReportingManager(
                row,
                references,
                warnings
        );

        validateGender(
                row,
                warnings
        );

        validateDateOfBirth(
                row,
                warnings
        );

        validateJoiningDate(
                row,
                errors
        );

        validateDateRelationship(
                row,
                warnings
        );

        validateEmployeeCategory(
                row,
                references,
                errors
        );

        validateEmployeeType(
                row,
                warnings
        );

        validateEmploymentMode(
                row,
                warnings
        );

        validateLoginEnabled(
                row,
                warnings
        );

        validateOptionalEmail(
                row.getOfficialEmail(),
                EmployeeExcelHeaders.OFFICIAL_EMAIL,
                warnings
        );

        validateOptionalEmail(
                row.getPersonalEmail(),
                EmployeeExcelHeaders.PERSONAL_EMAIL,
                warnings
        );

        validateOptionalMobile(
                row.getMobileNumber(),
                warnings
        );

        validateOptionalAlternateMobile(
                row.getAlternateMobile(),
                warnings
        );

        validateMaximumLengths(
                row,
                warnings
        );

        validateInFileDuplicates(
                row,
                context,
                warnings
        );

        return ValidationResult.builder()
                .success(errors.isEmpty())
                .skipRow(false)
                .errors(List.copyOf(errors))
                .warnings(List.copyOf(warnings))
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
            List<ValidationResult.ValidationWarning> warnings
    ) {
        String value =
                valueParser.nullableText(
                        row.getDepartmentName()
                );

        if (value == null) {
            return;
        }

        String key =
                valueParser.normalizeLookupKey(
                        value
                );

        if (references.findDepartment(key) == null) {
            addWarning(
                    warnings,
                    EmployeeExcelHeaders.DEPARTMENT_NAME,
                    row.getDepartmentName(),
                    "Department does not exist or is inactive for this branch. "
                            + "It will be saved as empty for bulk import."
            );
        }
    }

    private void validateDesignation(
            EmployeeBulkImportRow row,
            EmployeeBulkReferenceData references,
            List<ValidationResult.ValidationWarning> warnings
    ) {
        String value =
                valueParser.nullableText(
                        row.getDesignationName()
                );

        if (value == null) {
            return;
        }

        String key =
                valueParser.normalizeLookupKey(
                        value
                );

        if (references.findDesignation(key) == null) {
            addWarning(
                    warnings,
                    EmployeeExcelHeaders.DESIGNATION_NAME,
                    row.getDesignationName(),
                    "Designation does not exist or is inactive. "
                            + "It will be saved as empty for bulk import."
            );
        }
    }

    private void validateReportingManager(
            EmployeeBulkImportRow row,
            EmployeeBulkReferenceData references,
            List<ValidationResult.ValidationWarning> warnings
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
            addWarning(
                    warnings,
                    EmployeeExcelHeaders
                            .REPORTING_MANAGER_EMPLOYEE_NO,
                    row.getReportingManagerEmployeeNo(),
                    "Reporting Manager Employee No does not exist in this branch. "
                            + "It will be saved as empty for bulk import."
            );
        }
    }

    private void validateGender(
            EmployeeBulkImportRow row,
            List<ValidationResult.ValidationWarning> warnings
    ) {
        try {
            valueParser.nullableGender(
                    row.getGender()
            );
        } catch (IllegalArgumentException exception) {
            addWarning(
                    warnings,
                    EmployeeExcelHeaders.GENDER,
                    row.getGender(),
                    exception.getMessage()
                            + ". It will be saved as empty for bulk import."
            );
        }
    }

    private void validateDateOfBirth(
            EmployeeBulkImportRow row,
            List<ValidationResult.ValidationWarning> warnings
    ) {
        LocalDate dateOfBirth;

        try {
            dateOfBirth =
                    valueParser.nullableDate(
                            row.getDateOfBirth(),
                            EmployeeExcelHeaders.DATE_OF_BIRTH
                    );
        } catch (IllegalArgumentException exception) {
            addWarning(
                    warnings,
                    EmployeeExcelHeaders.DATE_OF_BIRTH,
                    row.getDateOfBirth(),
                    exception.getMessage()
                            + " It will be saved as empty for bulk import."
            );
            return;
        }

        if (dateOfBirth == null) {
            return;
        }

        LocalDate today =
                LocalDate.now();

        if (!dateOfBirth.isBefore(today)) {
            addWarning(
                    warnings,
                    EmployeeExcelHeaders.DATE_OF_BIRTH,
                    row.getDateOfBirth(),
                    "Date of Birth must be before today. "
                            + "It will be saved as empty for bulk import."
            );
            return;
        }

        if (
                Period.between(
                        dateOfBirth,
                        today
                ).getYears() < 18
        ) {
            addWarning(
                    warnings,
                    EmployeeExcelHeaders.DATE_OF_BIRTH,
                    row.getDateOfBirth(),
                    "Employee must be at least 18 years old. "
                            + "Date of Birth will be saved as empty for bulk import."
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
                    valueParser.nullableJoiningDate(
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
            List<ValidationResult.ValidationWarning> warnings
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
                    valueParser.nullableJoiningDate(
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
            addWarning(
                    warnings,
                    EmployeeExcelHeaders.DATE_OF_BIRTH,
                    row.getDateOfBirth(),
                    "Date of Birth must be before Joining Date. "
                            + "Date of Birth will be saved as empty for bulk import."
            );
        }
    }

    private void validateEmployeeCategory(
            EmployeeBulkImportRow row,
            EmployeeBulkReferenceData references,
            List<ValidationResult.ValidationError> errors
    ) {
        Designation designation =
                references.findDesignation(
                        valueParser.normalizeLookupKey(
                                row.getDesignationName()
                        )
                );

        try {
            EmployeeCategory category =
                    categoryResolver.resolve(
                            row.getEmployeeCategory(),
                            designation
                    );

            if (category == null) {
                addError(
                        errors,
                        EmployeeExcelHeaders.EMPLOYEE_CATEGORY,
                        row.getEmployeeCategory(),
                        "EMPLOYEE_CATEGORY_REQUIRED",
                        "Employee Category is required when it cannot be inferred from Designation"
                );
            }
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
            List<ValidationResult.ValidationWarning> warnings
    ) {
        String value =
                valueParser.nullableText(
                        row.getEmployeeType()
                );

        if (value == null) {
            return;
        }

        try {
            valueParser.requiredEmployeeType(value);
        } catch (IllegalArgumentException exception) {
            addWarning(
                    warnings,
                    EmployeeExcelHeaders.EMPLOYEE_TYPE,
                    row.getEmployeeType(),
                    exception.getMessage()
                            + ". It will be saved as empty for bulk import."
            );
        }
    }

    private void validateEmploymentMode(
            EmployeeBulkImportRow row,
            List<ValidationResult.ValidationWarning> warnings
    ) {
        String value =
                valueParser.nullableText(
                        row.getEmploymentMode()
                );

        if (value == null) {
            return;
        }

        try {
            valueParser.requiredEmploymentMode(value);
        } catch (IllegalArgumentException exception) {
            addWarning(
                    warnings,
                    EmployeeExcelHeaders.EMPLOYMENT_MODE,
                    row.getEmploymentMode(),
                    exception.getMessage()
                            + ". It will be saved as empty for bulk import."
            );
        }
    }

    private void validateLoginEnabled(
            EmployeeBulkImportRow row,
            List<ValidationResult.ValidationWarning> warnings
    ) {
        try {
            valueParser.nullableYesNo(
                    row.getLoginEnabled(),
                    EmployeeExcelHeaders.LOGIN_ENABLED
            );
        } catch (IllegalArgumentException exception) {
            addWarning(
                    warnings,
                    EmployeeExcelHeaders.LOGIN_ENABLED,
                    row.getLoginEnabled(),
                    exception.getMessage()
                            + ". Login creation will default to disabled."
            );
        }
    }

    private void validateOptionalEmail(
            String value,
            String column,
            List<ValidationResult.ValidationWarning> warnings
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
            addWarning(
                    warnings,
                    column,
                    value,
                    column
                            + " must contain a valid email address. "
                            + "It will be saved as empty for bulk import."
            );
        }
    }

    private void validateOptionalMobile(
            String value,
            List<ValidationResult.ValidationWarning> warnings
    ) {
        String mobile =
                valueParser.nullableText(value);

        if (mobile == null) {
            return;
        }

        if (!MOBILE_PATTERN.matcher(mobile).matches()) {
            addWarning(
                    warnings,
                    EmployeeExcelHeaders.MOBILE_NUMBER,
                    value,
                    "Mobile Number contains an invalid mobile number. "
                            + "It will be saved as empty for bulk import."
            );
        }
    }

    private void validateOptionalAlternateMobile(
            String value,
            List<ValidationResult.ValidationWarning> warnings
    ) {
        String mobile =
                valueParser.nullableText(value);

        if (mobile == null) {
            return;
        }

        if (!MOBILE_PATTERN.matcher(mobile).matches()) {
            addWarning(
                    warnings,
                    EmployeeExcelHeaders.ALTERNATE_MOBILE,
                    value,
                    "Alternate Mobile contains an invalid mobile number. "
                            + "It will be saved as empty for bulk import."
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

    private void validateOptionalName(
            String value,
            String column,
            List<ValidationResult.ValidationWarning> warnings
    ) {
        String normalized =
                valueParser.nullableText(
                        value
                );

        if (
                normalized != null
                        && normalized.length() > 100
        ) {
            addWarning(
                    warnings,
                    column,
                    value,
                    column
                            + " cannot exceed 100 characters. "
                            + "It will be saved as empty for bulk import."
            );
        }
    }

    private void validateMaximumLengths(
            EmployeeBulkImportRow row,
            List<ValidationResult.ValidationWarning> warnings
    ) {
        validateOptionalLength(
                row.getTitle(),
                EmployeeExcelHeaders.TITLE,
                20,
                warnings
        );

        validateOptionalLength(
                row.getMiddleName(),
                EmployeeExcelHeaders.MIDDLE_NAME,
                100,
                warnings
        );

        validateOptionalLength(
                row.getNationality(),
                EmployeeExcelHeaders.NATIONALITY,
                100,
                warnings
        );

        validateOptionalLength(
                row.getNationalId(),
                EmployeeExcelHeaders.NATIONAL_ID,
                100,
                warnings
        );

        validateOptionalLength(
                row.getDistrict(),
                EmployeeExcelHeaders.DISTRICT,
                100,
                warnings
        );

        validateOptionalLength(
                row.getCounty(),
                EmployeeExcelHeaders.COUNTY,
                100,
                warnings
        );

        validateOptionalLength(
                row.getSubCounty(),
                EmployeeExcelHeaders.SUB_COUNTY,
                100,
                warnings
        );

        validateOptionalLength(
                row.getParish(),
                EmployeeExcelHeaders.PARISH,
                100,
                warnings
        );

        validateOptionalLength(
                row.getVillage(),
                EmployeeExcelHeaders.VILLAGE,
                150,
                warnings
        );

        validateOptionalLength(
                row.getStreet(),
                EmployeeExcelHeaders.STREET,
                255,
                warnings
        );

        validateOptionalLength(
                row.getPostalCode(),
                EmployeeExcelHeaders.POSTAL_CODE,
                30,
                warnings
        );

        validateOptionalLength(
                row.getRemarks(),
                EmployeeExcelHeaders.REMARKS,
                10000,
                warnings
        );
    }

    private void validateOptionalLength(
            String value,
            String column,
            int maximumLength,
            List<ValidationResult.ValidationWarning> warnings
    ) {
        String normalized =
                valueParser.nullableText(
                        value
                );

        if (
                normalized != null
                        && normalized.length() > maximumLength
        ) {
            addWarning(
                    warnings,
                    column,
                    value,
                    column
                            + " cannot exceed "
                            + maximumLength
                            + " characters. It will be saved as empty "
                            + "for bulk import."
            );
        }
    }

    private void validateInFileDuplicates(
            EmployeeBulkImportRow row,
            ImportContext context,
            List<ValidationResult.ValidationWarning> warnings
    ) {
        validateDuplicateValue(
                context,
                OFFICIAL_EMAIL_CACHE_KEY,
                row.getOfficialEmail(),
                EmployeeExcelHeaders.OFFICIAL_EMAIL,
                warnings
        );

        validateDuplicateValue(
                context,
                NATIONAL_ID_CACHE_KEY,
                row.getNationalId(),
                EmployeeExcelHeaders.NATIONAL_ID,
                warnings
        );

        validateDuplicateValue(
                context,
                MOBILE_CACHE_KEY,
                row.getMobileNumber(),
                EmployeeExcelHeaders.MOBILE_NUMBER,
                warnings
        );
    }

    @SuppressWarnings("unchecked")
    private void validateDuplicateValue(
            ImportContext context,
            String cacheKey,
            String rawValue,
            String column,
            List<ValidationResult.ValidationWarning> warnings
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
            addWarning(
                    warnings,
                    column,
                    rawValue,
                    column
                            + " is duplicated inside the workbook. "
                            + "Review this value before creating login credentials."
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

    private void addWarning(
            List<ValidationResult.ValidationWarning> warnings,
            String columnName,
            String cellValue,
            String message
    ) {
        warnings.add(
                ValidationResult.ValidationWarning
                        .builder()
                        .columnName(columnName)
                        .cellValue(cellValue)
                        .message(message)
                        .build()
        );
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

