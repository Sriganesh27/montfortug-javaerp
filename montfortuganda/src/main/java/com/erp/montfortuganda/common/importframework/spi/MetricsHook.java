package com.erp.montfortuganda.common.importframework.spi;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.plugin.ChunkProcessingResult;

public interface MetricsHook {
    void recordChunkMetrics(ImportContext context, ChunkProcessingResult chunkResult);
    void recordFinalMetrics(ImportContext context, ChunkProcessingResult finalResult);
}
