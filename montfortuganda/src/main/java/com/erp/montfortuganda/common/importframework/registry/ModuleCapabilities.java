package com.erp.montfortuganda.common.importframework.registry;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ModuleCapabilities {
    private final boolean supportsInsert;
    private final boolean supportsUpdate;
    private final boolean supportsUpsert;
    private final boolean supportsValidateOnly;
    private final boolean supportsRetry;
    private final boolean supportsTemplateExport;
    private final boolean supportsHistory;
}
