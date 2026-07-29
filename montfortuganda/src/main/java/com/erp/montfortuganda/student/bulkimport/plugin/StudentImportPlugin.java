package com.erp.montfortuganda.student.bulkimport.plugin;

import com.erp.montfortuganda.common.importframework.lifecycle.ImportMode;
import com.erp.montfortuganda.common.importframework.plugin.DuplicateStrategy;
import com.erp.montfortuganda.common.importframework.plugin.ExcelRowMapper;
import com.erp.montfortuganda.common.importframework.plugin.ImportPlugin;
import com.erp.montfortuganda.common.importframework.plugin.ImportStrategyProvider;
import com.erp.montfortuganda.common.importframework.plugin.ImportValidatorChain;
import com.erp.montfortuganda.common.importframework.plugin.PluginProcessor;
import com.erp.montfortuganda.common.importframework.plugin.RetryStrategy;
import com.erp.montfortuganda.common.importframework.plugin.ValidationStrategy;
import com.erp.montfortuganda.common.importframework.registry.ModuleCapabilities;
import com.erp.montfortuganda.common.importframework.registry.ModuleManifest;
import com.erp.montfortuganda.student.bulkimport.dto.StudentBulkImportRow;
import com.erp.montfortuganda.student.bulkimport.excel.StudentExcelRowMapper;
import com.erp.montfortuganda.student.bulkimport.processor.StudentBulkImportProcessor;
import com.erp.montfortuganda.student.bulkimport.validation.StudentBulkImportValidator;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Registers Student bulk import with the common import framework.
 *
 * <p>The Student plugin supports insert-only imports. Existing Student
 * records are not updated or overwritten through bulk import.</p>
 *
 * <p>Invalid rows are rejected individually while valid rows continue
 * processing. Failed rows may be corrected and retried.</p>
 */
@Component
public class StudentImportPlugin
        implements ImportPlugin<StudentBulkImportRow> {

    public static final String MODULE_NAME =
            "STUDENT";

    private static final int MAXIMUM_ROWS =
            5_000;

    private static final int DEFAULT_CHUNK_SIZE =
            100;

    private static final long MAXIMUM_FILE_SIZE_BYTES =
            10L * 1024L * 1024L;

    private final StudentExcelRowMapper rowMapper;

    private final StudentBulkImportValidator validator;

    private final StudentBulkImportProcessor processor;

    private final ModuleManifest manifest;

    private final ModuleCapabilities capabilities;

    private final ImportStrategyProvider strategies;

    public StudentImportPlugin(
            StudentExcelRowMapper rowMapper,
            StudentBulkImportValidator validator,
            StudentBulkImportProcessor processor
    ) {
        this.rowMapper = rowMapper;
        this.validator = validator;
        this.processor = processor;

        this.manifest =
                buildManifest();

        this.capabilities =
                buildCapabilities();

        this.strategies =
                buildStrategies();
    }

    @Override
    public ModuleManifest getManifest() {
        return manifest;
    }

    @Override
    public ModuleCapabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public ImportStrategyProvider getStrategies() {
        return strategies;
    }

    @Override
    public ExcelRowMapper<StudentBulkImportRow> getRowMapper() {
        return rowMapper;
    }

    @Override
    public ImportValidatorChain<StudentBulkImportRow> getValidator() {
        return validator;
    }

    @Override
    public PluginProcessor<StudentBulkImportRow> getProcessor() {
        return processor;
    }

    private ModuleManifest buildManifest() {
        return ModuleManifest.builder()
                .moduleName(MODULE_NAME)
                .maximumRows(MAXIMUM_ROWS)
                .defaultChunkSize(DEFAULT_CHUNK_SIZE)
                .maximumFileSize(MAXIMUM_FILE_SIZE_BYTES)
                .supportedFileTypes(
                        List.of(
                                "xlsx"
                        )
                )
                .requiredPermissions(
                        List.of(
                                "STUDENT_CREATE"
                        )
                )
                .supportedImportModes(
                        List.of(
                                ImportMode.INSERT
                        )
                )
                .build();
    }

    private ModuleCapabilities buildCapabilities() {
        return ModuleCapabilities.builder()
                .supportsInsert(true)
                .supportsUpdate(false)
                .supportsUpsert(false)
                .supportsValidateOnly(false)
                .supportsRetry(true)
                .supportsTemplateExport(false)
                .supportsHistory(true)
                .build();
    }

    private ImportStrategyProvider buildStrategies() {
        return new ImportStrategyProvider() {

            @Override
            public DuplicateStrategy getDuplicateStrategy() {
                return DuplicateStrategy.REJECT_ROW;
            }

            @Override
            public RetryStrategy getRetryStrategy() {
                return RetryStrategy.RETRY_FAILED_ROWS;
            }

            @Override
            public ValidationStrategy getValidationStrategy() {
                return ValidationStrategy.CONTINUE_AND_REPORT;
            }
        };
    }
}