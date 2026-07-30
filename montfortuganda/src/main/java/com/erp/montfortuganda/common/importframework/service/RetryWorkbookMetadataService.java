package com.erp.montfortuganda.common.importframework.service;

import com.erp.montfortuganda.common.importframework.report.CorrectedWorkbookService;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Reads and validates the secure metadata embedded in a failed-rows-only
 * correction workbook.
 *
 * <p>The browser never supplies retry row numbers. This service verifies the
 * workbook against the original backend job and converts compact workbook row
 * numbers into their original physical Excel row numbers.</p>
 */
@Service
public class RetryWorkbookMetadataService {

    public static final String RETRY_ROW_MAPPING_OPTION =
            "retryOriginalRowByWorkbookRow";

    private static final String ORIGINAL_JOB_ID_HEADER =
            "Original Job ID";

    private static final String FAILED_WORKBOOK_ROW_HEADER =
            "Failed Workbook Row";

    private static final String ORIGINAL_EXCEL_ROW_HEADER =
            "Original Excel Row";

    /**
     * Validates the uploaded failed-only workbook.
     *
     * @param workbookPath              uploaded corrected workbook
     * @param expectedOriginalJobId     original job from the retry endpoint
     * @param authorizedOriginalRows    failed rows resolved by the backend
     * @return verified compact-to-original row mapping
     */
    public RetryWorkbookMetadata readAndValidate(
            Path workbookPath,
            String expectedOriginalJobId,
            Set<Integer> authorizedOriginalRows
    ) {
        validateInput(
                workbookPath,
                expectedOriginalJobId,
                authorizedOriginalRows
        );

        try (
                InputStream input =
                        Files.newInputStream(
                                workbookPath
                        );

                XSSFWorkbook workbook =
                        new XSSFWorkbook(input)
        ) {
            Sheet dataSheet =
                    requireDataSheet(
                            workbook
                    );

            Sheet metadataSheet =
                    workbook.getSheet(
                            CorrectedWorkbookService
                                    .RETRY_METADATA_SHEET
                    );

            if (metadataSheet == null) {
                throw new IllegalArgumentException(
                        "This file is not a valid failed-row retry workbook. "
                                + "Secure retry metadata is missing."
                );
            }

            validateMetadataHeaders(
                    metadataSheet
            );

            DataFormatter formatter =
                    new DataFormatter(
                            Locale.ENGLISH
                    );

            FormulaEvaluator evaluator =
                    workbook
                            .getCreationHelper()
                            .createFormulaEvaluator();

            Map<Integer, Integer> originalRowByWorkbookRow =
                    new LinkedHashMap<>();

            Set<Integer> discoveredOriginalRows =
                    new LinkedHashSet<>();

            for (
                    int rowIndex = 1;
                    rowIndex <= metadataSheet.getLastRowNum();
                    rowIndex++
            ) {
                Row metadataRow =
                        metadataSheet.getRow(
                                rowIndex
                        );

                if (
                        metadataRow == null
                                || isBlankMetadataRow(
                                metadataRow,
                                formatter,
                                evaluator
                        )
                ) {
                    continue;
                }

                String originalJobId =
                        formattedCell(
                                metadataRow,
                                0,
                                formatter,
                                evaluator
                        );

                int workbookRowNumber =
                        positiveDataRowNumber(
                                formattedCell(
                                        metadataRow,
                                        1,
                                        formatter,
                                        evaluator
                                ),
                                FAILED_WORKBOOK_ROW_HEADER
                        );

                int originalExcelRowNumber =
                        positiveDataRowNumber(
                                formattedCell(
                                        metadataRow,
                                        2,
                                        formatter,
                                        evaluator
                                ),
                                ORIGINAL_EXCEL_ROW_HEADER
                        );

                if (
                        !expectedOriginalJobId.equals(
                                originalJobId
                        )
                ) {
                    throw new IllegalArgumentException(
                            "The retry workbook belongs to a different "
                                    + "original import job."
                    );
                }

                if (
                        !authorizedOriginalRows.contains(
                                originalExcelRowNumber
                        )
                ) {
                    throw new IllegalArgumentException(
                            "Retry workbook row "
                                    + workbookRowNumber
                                    + " references an unauthorized original "
                                    + "Excel row: "
                                    + originalExcelRowNumber
                                    + "."
                    );
                }

                if (
                        originalRowByWorkbookRow.putIfAbsent(
                                workbookRowNumber,
                                originalExcelRowNumber
                        ) != null
                ) {
                    throw new IllegalArgumentException(
                            "Retry workbook contains duplicate metadata for "
                                    + "workbook row "
                                    + workbookRowNumber
                                    + "."
                    );
                }

                if (
                        !discoveredOriginalRows.add(
                                originalExcelRowNumber
                        )
                ) {
                    throw new IllegalArgumentException(
                            "Retry workbook references original Excel row "
                                    + originalExcelRowNumber
                                    + " more than once."
                    );
                }

                validateDataRowExists(
                        dataSheet,
                        workbookRowNumber
                );
            }

            if (originalRowByWorkbookRow.isEmpty()) {
                throw new IllegalArgumentException(
                        "The retry workbook does not contain any failed rows."
                );
            }

            /*
             * The generated correction workbook contains every failed row.
             * Requiring an exact match prevents a user from inserting,
             * deleting or substituting retry rows before upload.
             */
            if (
                    !discoveredOriginalRows.equals(
                            authorizedOriginalRows
                    )
            ) {
                Set<Integer> missingRows =
                        new LinkedHashSet<>(
                                authorizedOriginalRows
                        );

                missingRows.removeAll(
                        discoveredOriginalRows
                );

                Set<Integer> unexpectedRows =
                        new LinkedHashSet<>(
                                discoveredOriginalRows
                        );

                unexpectedRows.removeAll(
                        authorizedOriginalRows
                );

                throw new IllegalArgumentException(
                        buildRowSetMismatchMessage(
                                missingRows,
                                unexpectedRows
                        )
                );
            }

            return new RetryWorkbookMetadata(
                    expectedOriginalJobId,
                    Collections.unmodifiableSet(
                            new LinkedHashSet<>(
                                    originalRowByWorkbookRow.keySet()
                            )
                    ),
                    Collections.unmodifiableMap(
                            new LinkedHashMap<>(
                                    originalRowByWorkbookRow
                            )
                    )
            );
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "The failed-row retry workbook could not be read.",
                    exception
            );
        }
    }

    private void validateInput(
            Path workbookPath,
            String expectedOriginalJobId,
            Set<Integer> authorizedOriginalRows
    ) {
        Objects.requireNonNull(
                workbookPath,
                "Retry workbook path is required."
        );

        if (
                !Files.isRegularFile(
                        workbookPath
                )
        ) {
            throw new IllegalArgumentException(
                    "The retry workbook was not found."
            );
        }

        String fileName =
                workbookPath
                        .getFileName()
                        .toString()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (!fileName.endsWith(".xlsx")) {
            throw new IllegalArgumentException(
                    "Retry Failed Rows requires the generated XLSX workbook."
            );
        }

        if (
                expectedOriginalJobId == null
                        || expectedOriginalJobId.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Original import job ID is required."
            );
        }

        if (
                authorizedOriginalRows == null
                        || authorizedOriginalRows.isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "No backend-authorized failed rows are available."
            );
        }

        for (
                Integer rowNumber
                : authorizedOriginalRows
        ) {
            if (
                    rowNumber == null
                            || rowNumber <= 1
            ) {
                throw new IllegalArgumentException(
                        "Backend retry rows contain an invalid Excel row."
                );
            }
        }
    }

    private Sheet requireDataSheet(
            XSSFWorkbook workbook
    ) {
        if (
                workbook == null
                        || workbook.getNumberOfSheets() == 0
        ) {
            throw new IllegalArgumentException(
                    "The retry workbook does not contain a data sheet."
            );
        }

        for (
                int index = 0;
                index < workbook.getNumberOfSheets();
                index++
        ) {
            Sheet sheet =
                    workbook.getSheetAt(index);

            String name =
                    sheet.getSheetName();

            if (
                    !CorrectedWorkbookService
                            .RETRY_METADATA_SHEET
                            .equals(name)
                            && !CorrectedWorkbookService
                            .ERROR_DETAILS_SHEET
                            .equals(name)
            ) {
                return sheet;
            }
        }

        throw new IllegalArgumentException(
                "The retry workbook does not contain failed Student rows."
        );
    }

    private void validateMetadataHeaders(
            Sheet metadataSheet
    ) {
        Row header =
                metadataSheet.getRow(0);

        if (header == null) {
            throw new IllegalArgumentException(
                    "Retry metadata header is missing."
            );
        }

        DataFormatter formatter =
                new DataFormatter(
                        Locale.ENGLISH
                );

        String originalJobHeader =
                formattedCell(
                        header,
                        0,
                        formatter,
                        null
                );

        String workbookRowHeader =
                formattedCell(
                        header,
                        1,
                        formatter,
                        null
                );

        String originalRowHeader =
                formattedCell(
                        header,
                        2,
                        formatter,
                        null
                );

        if (
                !ORIGINAL_JOB_ID_HEADER.equals(
                        originalJobHeader
                )
                        || !FAILED_WORKBOOK_ROW_HEADER.equals(
                        workbookRowHeader
                )
                        || !ORIGINAL_EXCEL_ROW_HEADER.equals(
                        originalRowHeader
                )
        ) {
            throw new IllegalArgumentException(
                    "Retry metadata header is invalid."
            );
        }
    }

    private boolean isBlankMetadataRow(
            Row row,
            DataFormatter formatter,
            FormulaEvaluator evaluator
    ) {
        return formattedCell(
                row,
                0,
                formatter,
                evaluator
        ).isBlank()
                && formattedCell(
                row,
                1,
                formatter,
                evaluator
        ).isBlank()
                && formattedCell(
                row,
                2,
                formatter,
                evaluator
        ).isBlank();
    }

    private String formattedCell(
            Row row,
            int columnIndex,
            DataFormatter formatter,
            FormulaEvaluator evaluator
    ) {
        if (
                row == null
                        || row.getCell(
                        columnIndex
                ) == null
        ) {
            return "";
        }

        String value =
                evaluator == null
                        ? formatter.formatCellValue(
                        row.getCell(
                                columnIndex
                        )
                )
                        : formatter.formatCellValue(
                        row.getCell(
                                columnIndex
                        ),
                        evaluator
                );

        return value == null
                ? ""
                : value.trim();
    }

    private int positiveDataRowNumber(
            String value,
            String label
    ) {
        try {
            int rowNumber =
                    Integer.parseInt(
                            value
                    );

            if (rowNumber <= 1) {
                throw new NumberFormatException();
            }

            return rowNumber;
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    label
                            + " must contain a valid physical Excel data row."
            );
        }
    }

    private void validateDataRowExists(
            Sheet dataSheet,
            int workbookRowNumber
    ) {
        Row row =
                dataSheet.getRow(
                        workbookRowNumber - 1
                );

        if (row == null) {
            throw new IllegalArgumentException(
                    "Retry workbook data row "
                            + workbookRowNumber
                            + " is missing."
            );
        }
    }

    private String buildRowSetMismatchMessage(
            Set<Integer> missingRows,
            Set<Integer> unexpectedRows
    ) {
        StringBuilder message =
                new StringBuilder(
                        "The retry workbook rows do not match the original "
                                + "failed rows."
                );

        if (!missingRows.isEmpty()) {
            message.append(" Missing original rows: ")
                    .append(missingRows)
                    .append('.');
        }

        if (!unexpectedRows.isEmpty()) {
            message.append(" Unauthorized original rows: ")
                    .append(unexpectedRows)
                    .append('.');
        }

        message.append(
                " Download the corrected workbook again and edit only "
                        + "the failed Student values."
        );

        return message.toString();
    }

    /**
     * Trusted metadata used by the retry submission pipeline.
     *
     * @param originalJobId              verified original job ID
     * @param workbookRowNumbers         compact physical rows to process
     * @param originalRowByWorkbookRow   compact row to original row mapping
     */
    public record RetryWorkbookMetadata(
            String originalJobId,
            Set<Integer> workbookRowNumbers,
            Map<Integer, Integer> originalRowByWorkbookRow
    ) {
    }
}