package com.erp.montfortuganda.employee.bulkimport.plugin;

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
import com.erp.montfortuganda.employee.bulkimport.dto.EmployeeBulkImportRow;
import com.erp.montfortuganda.employee.bulkimport.excel.EmployeeExcelRowMapper;
import com.erp.montfortuganda.employee.bulkimport.processor.EmployeeBulkImportProcessor;
import com.erp.montfortuganda.employee.bulkimport.validation.EmployeeBulkImportValidator;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Registers Employee bulk import with the common import framework.
 *
 * <p>The plugin supports insert-only imports. Update and upsert are disabled
 * until explicit Employee update matching rules are designed and tested.</p>
 */
@Component
public class EmployeeImportPlugin
        implements ImportPlugin<EmployeeBulkImportRow> {

    public static final String MODULE_NAME =
            "EMPLOYEE";

    private static final int MAXIMUM_ROWS =
            5_000;

    private static final int DEFAULT_CHUNK_SIZE =
            100;

    private static final long MAXIMUM_FILE_SIZE_BYTES =
            10L * 1024L * 1024L;

    private final EmployeeExcelRowMapper rowMapper;
    private final EmployeeBulkImportValidator validator;
    private final EmployeeBulkImportProcessor processor;

    private final ModuleManifest manifest;
    private final ModuleCapabilities capabilities;
    private final ImportStrategyProvider strategies;

    public EmployeeImportPlugin(
            EmployeeExcelRowMapper rowMapper,
            EmployeeBulkImportValidator validator,
            EmployeeBulkImportProcessor processor
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
    public ExcelRowMapper<EmployeeBulkImportRow> getRowMapper() {
        return rowMapper;
    }

    @Override
    public ImportValidatorChain<EmployeeBulkImportRow> getValidator() {
        return validator;
    }

    @Override
    public PluginProcessor<EmployeeBulkImportRow> getProcessor() {
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
                                "EMPLOYEE_CREATE"
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