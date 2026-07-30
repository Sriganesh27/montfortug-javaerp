package com.erp.montfortuganda.common.importframework.plugin;

import com.erp.montfortuganda.common.importframework.registry.ImportTemplate;
import com.erp.montfortuganda.common.importframework.registry.ModuleCapabilities;
import com.erp.montfortuganda.common.importframework.registry.ModuleManifest;
import com.erp.montfortuganda.common.importframework.spi.AfterImportHook;
import com.erp.montfortuganda.common.importframework.spi.BeforeImportHook;

public interface ImportPlugin<DTO> {

    ModuleManifest getManifest();

    ModuleCapabilities getCapabilities();

    ImportStrategyProvider getStrategies();

    ExcelRowMapper<DTO> getRowMapper();

    ImportValidatorChain<DTO> getValidator();

    PluginProcessor<DTO> getProcessor();

    /**
     * Returns the module-specific Excel template definition.
     *
     * <p>The default empty template preserves compatibility with existing
     * plugins until their header definitions are added.</p>
     */
    default ImportTemplate getTemplate() {
        return ImportTemplate.builder()
                .build();
    }

    default BeforeImportHook getBeforeImportHook() {
        return null;
    }

    default AfterImportHook getAfterImportHook() {
        return null;
    }
}