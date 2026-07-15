package com.erp.montfortuganda.common.importframework.validator;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.plugin.ImportValidatorChain;
import com.erp.montfortuganda.common.importframework.plugin.ValidationResult;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractValidatorChain<DTO> implements ImportValidatorChain<DTO> {

    private final List<RowValidator<DTO>> validators = new ArrayList<>();

    public void addValidator(RowValidator<DTO> validator) {
        validators.add(validator);
    }

    // FIX: Added ImportContext to signature to match the interface
    @Override
    public ValidationResult validate(DTO dto, int rowNumber, ImportContext context) {
        List<ValidationResult.ValidationError> errors = new ArrayList<>();
        List<ValidationResult.ValidationWarning> warnings = new ArrayList<>();
        boolean skipRow = false;

        for (RowValidator<DTO> validator : validators) {
            // FIX: Pass context down to the individual row validators
            ValidationResult result = validator.validate(dto, rowNumber, context);

            if (result.getErrors() != null) {
                errors.addAll(result.getErrors());
            }
            if (result.getWarnings() != null) {
                warnings.addAll(result.getWarnings());
            }
            if (result.isSkipRow()) {
                skipRow = true;
            }

            if (!result.isSuccess() && shouldStopOnFirstError()) {
                break;
            }
        }

        return ValidationResult.builder()
                .success(errors.isEmpty())
                .skipRow(skipRow)
                .errors(errors)
                .warnings(warnings)
                .build();
    }

    protected abstract boolean shouldStopOnFirstError();
}