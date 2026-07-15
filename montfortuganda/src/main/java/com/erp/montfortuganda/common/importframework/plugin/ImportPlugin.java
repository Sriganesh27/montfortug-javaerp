package com.erp.montfortuganda.common.importframework.plugin;

import com.erp.montfortuganda.common.importframework.registry.ModuleCapabilities;
import com.erp.montfortuganda.common.importframework.registry.ModuleManifest;
import com.erp.montfortuganda.common.importframework.spi.BeforeImportHook;
import com.erp.montfortuganda.common.importframework.spi.AfterImportHook;

public interface ImportPlugin<DTO> {

    // RESTORED: These were accidentally removed in the previous snippet
    ModuleManifest getManifest();
    ModuleCapabilities getCapabilities();
    ImportStrategyProvider getStrategies();

    ExcelRowMapper<DTO> getRowMapper();
    ImportValidatorChain<DTO> getValidator();
    PluginProcessor<DTO> getProcessor();

    // Default implementations so plugins that don't need hooks won't break
    default BeforeImportHook getBeforeImportHook() {
        return null;
    }

    default AfterImportHook getAfterImportHook() {
        return null;
    }
}