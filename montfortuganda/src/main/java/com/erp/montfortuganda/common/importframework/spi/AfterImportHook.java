package com.erp.montfortuganda.common.importframework.spi;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.plugin.ChunkProcessingResult;

public interface AfterImportHook {
    void onAfterImport(ImportContext context, ChunkProcessingResult finalResult);
}
