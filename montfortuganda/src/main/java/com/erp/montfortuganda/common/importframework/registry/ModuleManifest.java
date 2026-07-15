package com.erp.montfortuganda.common.importframework.registry;

import com.erp.montfortuganda.common.importframework.lifecycle.ImportMode;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ModuleManifest {
    private final String moduleName;
    private final int maximumRows;
    private final int defaultChunkSize;
    private final long maximumFileSize;
    private final List<String> supportedFileTypes;
    private final List<String> requiredPermissions;
    private final List<ImportMode> supportedImportModes;
}
