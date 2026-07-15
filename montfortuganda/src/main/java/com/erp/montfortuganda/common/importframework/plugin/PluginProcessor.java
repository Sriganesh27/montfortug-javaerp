package com.erp.montfortuganda.common.importframework.plugin;

import java.util.List;

public interface PluginProcessor<DTO> {
    ChunkProcessingResult processChunk(List<DTO> validDtos, com.erp.montfortuganda.common.importframework.context.ImportContext context);
}
