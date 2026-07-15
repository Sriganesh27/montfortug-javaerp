package com.erp.montfortuganda.common.importframework.validator;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.plugin.ValidationResult;

public interface RowValidator<DTO> {

    // FIX: Added ImportContext so individual validators can access the job cache
    ValidationResult validate(DTO dto, int rowNum, ImportContext context);

}