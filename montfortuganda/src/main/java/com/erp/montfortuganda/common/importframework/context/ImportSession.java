package com.erp.montfortuganda.common.importframework.context;

import com.erp.montfortuganda.common.importframework.lifecycle.ImportStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImportSession {
    private String jobId;
    private int currentChunk;
    private int processedRows;
    private int successRows;
    private int failedRows;
    private long startTime;
    private String lastCheckpoint;
    private ImportStatus currentLifecycle;
}
