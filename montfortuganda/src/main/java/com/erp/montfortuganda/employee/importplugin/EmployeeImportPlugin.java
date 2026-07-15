package com.erp.montfortuganda.employee.importplugin;

import com.erp.montfortuganda.common.importframework.lifecycle.ImportMode;
import com.erp.montfortuganda.common.importframework.plugin.*;
import com.erp.montfortuganda.common.importframework.registry.ModuleCapabilities;
import com.erp.montfortuganda.common.importframework.registry.ModuleManifest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EmployeeImportPlugin implements ImportPlugin<EmployeeImportDTO> {

    private final EmployeeExcelMapper rowMapper;
    private final EmployeeValidatorChain validatorChain;
    private final EmployeePluginProcessor processor;

    @Override
    public ModuleManifest getManifest() {
        return ModuleManifest.builder()
                .moduleName("EMPLOYEE")
                .maximumRows(10000)
                .defaultChunkSize(500)
                .maximumFileSize(10_000_000L) // 10MB
                .supportedFileTypes(List.of(".xlsx", ".csv"))
                .requiredPermissions(List.of("IMPORT_EMPLOYEES"))
                .supportedImportModes(List.of(ImportMode.INSERT, ImportMode.UPDATE))
                .build();
    }

    @Override
    public ModuleCapabilities getCapabilities() {
        return ModuleCapabilities.builder()
                .supportsInsert(true)
                .supportsUpdate(true)
                .supportsUpsert(false)
                .supportsValidateOnly(true)
                .supportsRetry(true)
                .supportsTemplateExport(true)
                .supportsHistory(true)
                .build();
    }

    @Override
    public ImportStrategyProvider getStrategies() {
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

    @Override
    public ExcelRowMapper<EmployeeImportDTO> getRowMapper() {
        return rowMapper;
    }

    @Override
    public ImportValidatorChain<EmployeeImportDTO> getValidator() {
        return validatorChain;
    }

    @Override
    public PluginProcessor<EmployeeImportDTO> getProcessor() {
        return processor;
    }
}
