package com.erp.montfortuganda.common.importframework.plugin;

import com.erp.montfortuganda.common.importframework.model.ErpImportError;
import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.ArrayList;

@Getter
@Builder
public class ChunkProcessingResult {
    private final int processed;
    private final int succeeded;
    private final int validationFailed;
    private final int processingFailed;
    private final long processingTimeMs;

    @Builder.Default
    private final List<ErpImportError> processingErrors = new ArrayList<>();
}