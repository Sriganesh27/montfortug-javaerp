package com.erp.montfortuganda.common.importframework.spi;

import com.erp.montfortuganda.common.importframework.context.ImportContext;

public interface BeforeImportHook {
    void onBeforeImport(ImportContext context);
}
