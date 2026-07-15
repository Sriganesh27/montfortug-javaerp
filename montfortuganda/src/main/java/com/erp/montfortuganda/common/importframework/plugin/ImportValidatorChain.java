package com.erp.montfortuganda.common.importframework.plugin;

import com.erp.montfortuganda.common.importframework.context.ImportContext;

public interface ImportValidatorChain<DTO> {

    // Ensure the method exactly expects DTO, rowNum, and Context
    ValidationResult validate(DTO dto, int rowNum, ImportContext context);

}