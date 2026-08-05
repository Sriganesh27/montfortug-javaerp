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
public class StudentImportPlugin
        implements ImportPlugin<StudentBulkImportRow> {

    public static final String MODULE_NAME = "STUDENT";

    private static final int MAXIMUM_ROWS = 5_000;
    private static final int DEFAULT_CHUNK_SIZE = 100;
    private static final long MAXIMUM_FILE_SIZE_BYTES =
            10L * 1024 * 1024;

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
                .supportedImportModes(
                        List.of(
                                ImportMode.INSERT,
                                ImportMode.RETRY_FAILED_ROWS
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

    private ImportTemplate buildTemplate() {
        return ImportTemplate.builder()
                .templateVersion("2.0")
                .expectedHeaders(StudentExcelHeaders.ALL_HEADERS)
                .mandatoryColumns(
                        List.copyOf(
                                StudentExcelHeaders.REQUIRED_HEADERS
                        )
                )
                .optionalColumns(buildOptionalHeaders())
                .aliases(buildAliases())
                .validationHints(buildValidationHints())
                .downloadUrl(
                        "/templates/Student_Import_Template.xlsx"
                )
                .sampleData(Map.of())
                .build();
    }

    private List<String> buildOptionalHeaders() {
        return StudentExcelHeaders.ALL_HEADERS.stream()
                .filter(
                        header ->
                                !StudentExcelHeaders.REQUIRED_HEADERS
                                        .contains(header)
                )
                .toList();
    }

    /**
     * Keeps the already-distributed 39-column workbook usable.
     *
     * <p>The former combined "Joining Date / Year" header is canonicalized
     * as Admission Year. {@link StudentExcelRowMapper} then separates a full
     * legacy date from a four-digit year. Missing Admission Date is allowed
     * because all Student row cells are optional.</p>
     */
    private Map<String, List<String>> buildAliases() {
        return Map.ofEntries(
                alias(
                        StudentExcelHeaders.ADMISSION_YEAR,
                        "Joining Date / Year",
                        "Joining Year",
                        "Year Joined"
                ),
                alias(
                        StudentExcelHeaders.ADMISSION_DATE,
                        "Joining Date",
                        "Date of Joining",
                        "Date of Admission"
                ),
                alias(
                        StudentExcelHeaders.JOINING_CLASS,
                        "Joined Class",
                        "Admission Class",
                        "Class Joined"
                ),
                alias(
                        StudentExcelHeaders.JOINED_TERM,
                        "Joining Term",
                        "Admission Term",
                        "Term Joined"
                ),
                alias(
                        StudentExcelHeaders.FIRST_NAME,
                        "Student First Name",
                        "Given Name"
                ),
                alias(
                        StudentExcelHeaders.MIDDLE_NAME,
                        "Student Middle Name"
                ),
                alias(
                        StudentExcelHeaders.LAST_NAME,
                        "Student Last Name",
                        "Surname",
                        "Family Name"
                ),
                alias(
                        StudentExcelHeaders.DATE_OF_BIRTH,
                        "DOB",
                        "Birth Date"
                ),
                alias(
                        StudentExcelHeaders.PRESENT_EDUCATION_LEVEL,
                        "Current Education Level",
                        "Education Level"
                ),
                alias(
                        StudentExcelHeaders.PRESENT_CLASS,
                        "Current Class",
                        "Class"
                ),
                alias(
                        StudentExcelHeaders.PRESENT_TERM,
                        "Current Term",
                        "Term"
                ),
                alias(
                        StudentExcelHeaders.FATHER,
                        "Father Name",
                        "Father's Name",
                        "Father/Guardian Name"
                ),
                alias(
                        StudentExcelHeaders.MOTHER,
                        "Mother Name",
                        "Mother's Name",
                        "Mother/Guardian Name"
                ),
                alias(
                        StudentExcelHeaders.GUARDIAN_NAME,
                        "Guardian",
                        "Guardian name"
                ),
                alias(
                        StudentExcelHeaders.GUARDIAN_RELATION,
                        "Guardian Relationship",
                        "Relationship to Guardian"
                ),
                alias(
                        StudentExcelHeaders.PRESENT_RESPONSIBLE_PERSON,
                        "Responsible Person",
                        "Preferred Contact",
                        "Present Guardian"
                ),
                alias(
                        StudentExcelHeaders.MOBILE_NUMBER,
                        "Mobile",
                        "Phone",
                        "Phone Number",
                        "Primary Mobile"
                ),
                alias(
                        StudentExcelHeaders.ALTERNATE_MOBILE,
                        "Alternate Phone",
                        "Secondary Mobile",
                        "Other Mobile"
                ),
                alias(
                        StudentExcelHeaders.NATIONAL_ID_OR_PASSPORT,
                        "National ID / Passport",
                        "National ID",
                        "Passport Number",
                        "National ID or Passport"
                ),
                alias(
                        StudentExcelHeaders.ADDRESS_COUNTRY,
                        "Country",
                        "Residence Country"
                ),
                alias(
                        StudentExcelHeaders.SUB_COUNTY,
                        "Sub-County",
                        "Subcounty"
                ),
                alias(
                        StudentExcelHeaders.PREVIOUS_SCHOOL,
                        "Former School",
                        "Previous School Name"
                ),
                alias(
                        StudentExcelHeaders.TRANSPORT_REQUIRED,
                        "Transport Required",
                        "Requires Transport",
                        "Transport"
                ),
                alias(
                        StudentExcelHeaders.HOSTEL_REQUIRED,
                        "Hostel Required",
                        "Requires Hostel",
                        "Hostel"
                ),
                alias(
                        StudentExcelHeaders.SCHOLARSHIP,
                        "Scholarship Required",
                        "Requires Scholarship",
                        "Scholarship"
                ),
                alias(
                        StudentExcelHeaders.MEDICAL_CONDITIONS,
                        "Medical Condition",
                        "Health Conditions",
                        "Special Medical Conditions"
                )
        );
    }

    private Map<String, String> buildValidationHints() {
        return Map.ofEntries(
                hint(
                        StudentExcelHeaders.ADMISSION_YEAR,
                        "Enter a four-digit year, for example 2026."
                ),
                hint(
                        StudentExcelHeaders.ADMISSION_DATE,
                        "Enter the actual admission date, for example 2026-02-05."
                ),
                hint(
                        StudentExcelHeaders.JOINING_CLASS,
                        "Enter the original class joined, for example P4."
                ),
                hint(
                        StudentExcelHeaders.JOINED_TERM,
                        "Enter Term 1, Term 2 or Term 3."
                ),
                hint(
                        StudentExcelHeaders.DATE_OF_BIRTH,
                        "Use YYYY-MM-DD, DD/MM/YYYY, MM/DD/YYYY or an Excel date."
                ),
                hint(
                        StudentExcelHeaders.GENDER,
                        "Use MALE, FEMALE, OTHER, M or F."
                ),
                hint(
                        StudentExcelHeaders.PRESENT_EDUCATION_LEVEL,
                        "Use NURSERY, PRIMARY, SECONDARY or SENIOR SECONDARY."
                ),
                hint(
                        StudentExcelHeaders.PRESENT_CLASS,
                        "Enter N1-N3, P1-P7, S1-S6 or a supported class label."
                ),
                hint(
                        StudentExcelHeaders.PRESENT_RESPONSIBLE_PERSON,
                        "Use FATHER, MOTHER or GUARDIAN."
                ),
                hint(
                        StudentExcelHeaders.TRANSPORT_REQUIRED,
                        "Use YES or NO."
                ),
                hint(
                        StudentExcelHeaders.HOSTEL_REQUIRED,
                        "Use YES or NO."
                ),
                hint(
                        StudentExcelHeaders.SCHOLARSHIP,
                        "Use YES or NO."
                )
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
