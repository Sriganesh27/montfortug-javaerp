package com.erp.montfortuganda.common.importframework.context;

import com.erp.montfortuganda.common.importframework.lifecycle.ImportMode;
import lombok.Builder;
import lombok.Getter;

import java.time.ZoneId;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Builder
public class ImportContext {
    private final String jobId;
    private final String branchId;
    private final String userId;
    private final String locale;
    private final ZoneId timeZone;
    private final ImportMode importMode;
    private final int chunkSize;
    private final String fileHash;
    private final String uploadedFileName;
    private final long startTime;

    // NEW: Fast-forward target rows for efficient retries (Null means process all)
    private final Set<Integer> targetRowNumbers;

    // NEW: Thread-safe job cache for cross-row validation state (e.g. intra-file duplicates)
    @Builder.Default
    private final Map<String, Object> jobStateCache = new ConcurrentHashMap<>();
}