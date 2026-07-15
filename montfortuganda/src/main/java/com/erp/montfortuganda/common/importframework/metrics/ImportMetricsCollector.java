package com.erp.montfortuganda.common.importframework.metrics;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.plugin.ChunkProcessingResult;
import com.erp.montfortuganda.common.importframework.plugin.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ImportMetricsCollector {

    public void recordChunkMetrics(ImportContext context, ChunkProcessingResult result) {
        log.info("Job {} - Chunk Complete | Processed: {} | Succeeded: {} | Validation Failed: {} | Processing Failed: {} | Time: {}ms",
                context.getJobId(),
                result.getProcessed(),
                result.getSucceeded(),
                result.getValidationFailed(),
                result.getProcessingFailed(),
                result.getProcessingTimeMs());
    }

    public void recordFinalMetrics(ImportContext context, ChunkProcessingResult result) {
        log.info("Job {} FINAL SUMMARY | Total Processed: {} | Total Succeeded: {} | Total Validation Failed: {} | Total Processing Failed: {} | Total Time: {}ms",
                context.getJobId(),
                result.getProcessed(),
                result.getSucceeded(),
                result.getValidationFailed(),
                result.getProcessingFailed(),
                result.getProcessingTimeMs());
    }

    public void recordValidationMetrics(ImportContext context, ValidationResult result) {
        if (result.getErrors() != null && !result.getErrors().isEmpty()) {
            log.warn("Job {} - Encountered {} validation errors in current row.",
                    context.getJobId(), result.getErrors().size());
        }

        if (result.getWarnings() != null && !result.getWarnings().isEmpty()) {
            log.warn("Job {} - Encountered {} validation warnings in current row.",
                    context.getJobId(), result.getWarnings().size());
        }
    }
}