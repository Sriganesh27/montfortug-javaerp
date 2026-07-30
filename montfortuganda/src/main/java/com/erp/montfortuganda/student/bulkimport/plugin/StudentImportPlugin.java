package com.erp.montfortuganda.student.bulkimport.plugin;

import com.erp.montfortuganda.common.importframework.lifecycle.ImportMode;
import com.erp.montfortuganda.common.importframework.plugin.*;
import com.erp.montfortuganda.common.importframework.registry.ImportTemplate;
import com.erp.montfortuganda.common.importframework.registry.ModuleCapabilities;
import com.erp.montfortuganda.common.importframework.registry.ModuleManifest;
import com.erp.montfortuganda.student.bulkimport.dto.StudentBulkImportRow;
import com.erp.montfortuganda.student.bulkimport.excel.StudentExcelHeaders;
import com.erp.montfortuganda.student.bulkimport.excel.StudentExcelRowMapper;
import com.erp.montfortuganda.student.bulkimport.processor.StudentBulkImportProcessor;
import com.erp.montfortuganda.student.bulkimport.validation.StudentBulkImportValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StudentImportPlugin implements ImportPlugin<StudentBulkImportRow> {

    public static final String MODULE_NAME = "STUDENT";

    private static final int MAXIMUM_ROWS = 5_000;
    private static final int DEFAULT_CHUNK_SIZE = 100;
    private static final long MAXIMUM_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    private final StudentExcelRowMapper rowMapper;
    private final StudentBulkImportValidator validator;
    private final StudentBulkImportProcessor processor;

    private final ModuleManifest manifest = buildManifest();
    private final ModuleCapabilities capabilities = buildCapabilities();
    private final ImportStrategyProvider strategies = buildStrategies();
    private final ImportTemplate template = buildTemplate();

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

    @Override
    public ImportTemplate getTemplate() {
        return template;
    }

    private ModuleManifest buildManifest() {
        return ModuleManifest.builder()
                .moduleName(MODULE_NAME)
                .maximumRows(MAXIMUM_ROWS)
                .defaultChunkSize(DEFAULT_CHUNK_SIZE)
                .maximumFileSize(MAXIMUM_FILE_SIZE_BYTES)
                .supportedFileTypes(List.of("xlsx"))
                .requiredPermissions(List.of("STUDENT_CREATE"))
                .supportedImportModes(List.of(ImportMode.INSERT, ImportMode.RETRY_FAILED_ROWS))
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

    private ImportTemplate buildTemplate() {
        return ImportTemplate.builder()
                .templateVersion("1.0")
                .expectedHeaders(StudentExcelHeaders.ALL_HEADERS)
                .mandatoryColumns(List.copyOf(StudentExcelHeaders.REQUIRED_HEADERS))
                .optionalColumns(buildOptionalHeaders())
                .aliases(buildAliases())
                .validationHints(buildValidationHints())
                .downloadUrl("/templates/Student_Import_Template.xlsx")
                .sampleData(Map.of())
                .build();
    }

    private List<String> buildOptionalHeaders() {
        return StudentExcelHeaders.ALL_HEADERS.stream()
                .filter(header -> !StudentExcelHeaders.REQUIRED_HEADERS.contains(header))
                .toList();
    }

    private Map<String, List<String>> buildAliases() {
        return Map.ofEntries(
                alias(StudentExcelHeaders.ADMISSION_NO,
                        "Admission Number", "Admission No"),
                alias(StudentExcelHeaders.DATE_OF_BIRTH,
                        "DOB", "Birth Date"),
                alias(StudentExcelHeaders.CLASS_NAME,
                        "Class Name"),
                alias(StudentExcelHeaders.MOBILE_NUMBER,
                        "Mobile", "Phone", "Phone Number"),
                alias(StudentExcelHeaders.ALTERNATE_MOBILE,
                        "Alternate Phone", "Secondary Mobile"),
                alias(StudentExcelHeaders.FATHER_OR_GUARDIAN_NAME,
                        "Father Name", "Guardian Name"),
                alias(StudentExcelHeaders.MOTHER_OR_GUARDIAN_NAME,
                        "Mother Name"),
                alias(StudentExcelHeaders.NATIONAL_ID_OR_PASSPORT,
                        "National ID", "Passport Number"),
                alias(StudentExcelHeaders.TRANSPORT_REQUIRED,
                        "Transport Required"),
                alias(StudentExcelHeaders.HOSTEL_REQUIRED,
                        "Hostel Required")
        );
    }

    private Map<String, String> buildValidationHints() {
        return Map.ofEntries(
                hint(StudentExcelHeaders.ADMISSION_YEAR,
                        "Enter a four-digit year."),
                hint(StudentExcelHeaders.DATE_OF_BIRTH,
                        "Use YYYY-MM-DD format."),
                hint(StudentExcelHeaders.GENDER,
                        "Use MALE, FEMALE or OTHER."),
                hint(StudentExcelHeaders.TRANSPORT_REQUIRED,
                        "Use YES or NO."),
                hint(StudentExcelHeaders.HOSTEL_REQUIRED,
                        "Use YES or NO."),
                hint(StudentExcelHeaders.SCHOLARSHIP,
                        "Use YES or NO.")
        );
    }

    private Map.Entry<String, List<String>> alias(
            String header,
            String... aliases
    ) {
        return Map.entry(header, List.of(aliases));
    }

    private Map.Entry<String, String> hint(
            String header,
            String message
    ) {
        return Map.entry(header, message);
    }
}