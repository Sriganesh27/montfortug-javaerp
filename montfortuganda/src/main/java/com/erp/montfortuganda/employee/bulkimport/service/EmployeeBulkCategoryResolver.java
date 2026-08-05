package com.erp.montfortuganda.employee.bulkimport.service;

import com.erp.montfortuganda.employee.bulkimport.excel.EmployeeExcelValueParser;
import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.school.entity.Designation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves an Employee Category for Employee bulk import.
 *
 * <p>An explicitly supplied Excel category always wins. A category is inferred
 * from the configured Designation only when the Excel category cell is blank.
 * Unknown Designations remain unresolved so the row is not silently assigned
 * to an incorrect category.</p>
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
                valueParser.nullableEmployeeCategory(
                        rawCategory
                );

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

    private EmployeeCategory infer(
            String designationKey
    ) {
        if (designationKey == null) {
            return null;
        }

        return switch (designationKey) {

            // =============================================================
            // TEACHING
            // =============================================================
            case "TEACHER" ->
                    EmployeeCategory.TEACHING;

            // =============================================================
            // MANAGEMENT + TEACHING
            // =============================================================
            case "HEADTEACHER",
                 "HEADMASTER",
                 "PRINCIPAL",
                 "VICEPRINCIPAL",
                 "DOS",
                 "DIRECTOROFSTUDIESDOS",
                 "DEANOFSTUDIESDOS",
                 "HOD",
                 "HEADOFDEPARTMENT" ->
                    EmployeeCategory.MANAGEMENT_TEACHING;

            // =============================================================
            // NON-TEACHING
            // =============================================================
            case "ADMISSIONSOFFICER",
                 "ACCOUNTANT",
                 "ACCOUNTANTS",
                 "ICTOFFICER",
                 "RECEPTIONIST",
                 "CLERK",
                 "SECRETARY",
                 "LIBRARIAN",
                 "NURSE",
                 "SCHOOLNURSE",
                 "COUNSELOR",
                 "COUNSELLOR" ->
                    EmployeeCategory.NON_TEACHING;

            // =============================================================
            // MANAGEMENT + NON-TEACHING
            // =============================================================
            case "DIRECTOR",
                 "BURSAR",
                 "ADMINISTRATOR",
                 "SCHOOLADMIN",
                 "SCHOOLADMINISTRATOR",
                 "HRMANAGER",
                 "FINANCE" ->
                    EmployeeCategory.MANAGEMENT_NON_TEACHING;

            // =============================================================
            // SUPPORT STAFF
            // =============================================================
            case "CLEANER",
                 "COOK",
                 "STAFFCOOK",
                 "GUARD",
                 "SECURITY",
                 "SECURITYGUARD",
                 "GARDENER",
                 "DRIVER",
                 "HYGIENE",
                 "FOOD" ->
                    EmployeeCategory.SUPPORT_STAFF;

            default -> null;
        };
    }
}
