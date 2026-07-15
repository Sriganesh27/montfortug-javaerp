package com.erp.montfortuganda.employee.importplugin;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.plugin.ValidationResult;
import com.erp.montfortuganda.common.importframework.validator.AbstractValidatorChain;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmployeeValidatorChain extends AbstractValidatorChain<EmployeeImportDTO> {

    public EmployeeValidatorChain() {

        // 1. Register the Intra-File Duplicate Email Validator
        addValidator((dto, rowNum, context) -> {
            ValidationResult.ValidationResultBuilder result = ValidationResult.builder();

            if (dto.getOfficialEmail() != null && !dto.getOfficialEmail().isBlank()) {
                @SuppressWarnings("unchecked")
                Set<String> seenEmails = (Set<String>) context.getJobStateCache()
                        .computeIfAbsent("SEEN_EMAILS", k -> ConcurrentHashMap.newKeySet());

                String normalizedEmail = dto.getOfficialEmail().trim().toLowerCase();

                if (!seenEmails.add(normalizedEmail)) {
                    result.errors(List.of(ValidationResult.ValidationError.builder()
                            .columnName("Official Email")
                            .cellValue(dto.getOfficialEmail())
                            .errorCode("DUPLICATE_IN_FILE")
                            .message("This email appears multiple times in the uploaded file.")
                            .build()));
                    return result.success(false).build();
                }
            }
            return result.success(true).build();
        });

        // 2. You can register future validators here sequentially
        // Example:
        // addValidator(new EmployeeAgeValidator());
        // addValidator((dto, rowNum, ctx) -> { ... });
    }

    @Override
    protected boolean shouldStopOnFirstError() {
        // Return false to collect all errors from all validators for this row
        return false;
    }
}