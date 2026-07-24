package com.erp.montfortuganda.employee.bulkimport.service;

import com.erp.montfortuganda.employee.bulkimport.excel.EmployeeExcelValueParser;
import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.school.entity.Designation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves an Employee category for bulk import.
 *
 * <p>An explicitly supplied Excel category always wins. A category is inferred
 * only when the Excel cell is blank. Unknown designations remain unresolved so
 * the row can be reported instead of being silently misclassified.</p>
 */
@Component
@RequiredArgsConstructor
public class EmployeeBulkCategoryResolver {

    private final EmployeeExcelValueParser valueParser;

    public EmployeeCategory resolve(
            String rawCategory,
            Designation designation
    ) {
        EmployeeCategory supplied =
                valueParser.nullableEmployeeCategory(rawCategory);

        if (supplied != null) {
            return supplied;
        }

        if (designation == null) {
            return null;
        }

        EmployeeCategory byCode =
                infer(
                        valueParser.normalizeLookupKey(
                                designation.getDesignationCode()
                        )
                );

        if (byCode != null) {
            return byCode;
        }

        return infer(
                valueParser.normalizeLookupKey(
                        designation.getDesignationName()
                )
        );
    }

    private EmployeeCategory infer(String designationKey) {
        if (designationKey == null) {
            return null;
        }

        return switch (designationKey) {
            case "TEACHER" ->
                    EmployeeCategory.TEACHING;

            case "HEADTEACHER",
                 "HEADMASTER",
                 "PRINCIPAL",
                 "VICEPRINCIPAL" ->
                    EmployeeCategory.MANAGEMENT_TEACHING;

            case "RECEPTIONIST",
                 "ACCOUNTANT",
                 "CLERK",
                 "SECRETARY",
                 "LIBRARIAN",
                 "NURSE" ->
                    EmployeeCategory.NON_TEACHING;

            case "BURSAR",
                 "ADMINISTRATOR",
                 "SCHOOLADMIN",
                 "HRMANAGER" ->
                    EmployeeCategory.MANAGEMENT_NON_TEACHING;

            case "CLEANER",
                 "COOK",
                 "GUARD",
                 "SECURITYGUARD",
                 "GARDENER",
                 "DRIVER" ->
                    EmployeeCategory.SUPPORT_STAFF;

            default -> null;
        };
    }
}
