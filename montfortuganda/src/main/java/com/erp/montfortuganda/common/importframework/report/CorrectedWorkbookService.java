package com.erp.montfortuganda.common.importframework.report;

import com.erp.montfortuganda.common.importframework.model.ErpImportError;
import com.erp.montfortuganda.common.importframework.model.ErpImportErrorRepository;
import com.erp.montfortuganda.common.importframework.registry.ImportTemplate;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Generates a compact Student/Employee correction workbook containing only
 * rows that failed during the original import.
 *
 * <p>The first visible sheet retains the original import headers, but
 * successful rows are excluded. A second visible sheet contains exact error
 * details. A very-hidden metadata sheet preserves the relationship between
 * each compact output row and its physical row in the original workbook.</p>
 */
@Component
public class CorrectedWorkbookService {

    public static final String RETRY_METADATA_SHEET =
            "__ERP_RETRY_METADATA";

    public static final String ERROR_DETAILS_SHEET =
            "Import Errors";

    private static final String CORRECTION_MARKER =
            "ENTER VALID DATA";

    private static final String FAILED_ROWS_SHEET_FALLBACK =
            "Failed Import Rows";

    private static final int ERROR_PAGE_SIZE = 1000;
    private static final int COMMENT_MAX_LENGTH = 3000;
    private static final int MAX_EXCEL_COLUMN_WIDTH = 255 * 256;

    private final ErpImportErrorRepository errorRepository;
    private final Path correctedDirectory;

    public CorrectedWorkbookService(
            ErpImportErrorRepository errorRepository,
            @Value(
                    "${app.import.corrected-directory:"
                            + "uploads/imports/corrected}"
            )
            String correctedDirectory
    ) {
        this.errorRepository = errorRepository;
        this.correctedDirectory = Path.of(correctedDirectory)
                .toAbsolutePath()
                .normalize();
    }

    /**
     * Creates a compact correction workbook containing only failed rows.
     *
     * <p>Invalid nonblank values remain visible so the user can understand
     * what failed. Blank invalid cells are filled with ENTER VALID DATA.
     * Every invalid cell is highlighted and receives an exact comment.</p>
     *
     * @return generated workbook path, or {@code null} when no failed rows
     * exist
     */
    public Path generateCorrectedWorkbook(
            String jobId,
            Path sourceFile,
            ImportTemplate template
    ) {
        validateInput(
                jobId,
                sourceFile,
                template
        );

        Set<Integer> failedRowNumbers =
                new LinkedHashSet<>(
                        errorRepository
                                .findDistinctFailedRowNumbers(
                                        jobId
                                )
                );

        if (failedRowNumbers.isEmpty()) {
            return null;
        }

        List<ErpImportError> errors =
                loadErrors(jobId);

        Map<Integer, List<ErpImportError>> errorsByRow =
                groupErrorsByRow(
                        errors,
                        failedRowNumbers
                );

        Path temporaryOutput = null;

        try {
            Files.createDirectories(
                    correctedDirectory
            );

            Path correctedFile =
                    correctedWorkbookPath(jobId);

            temporaryOutput =
                    Files.createTempFile(
                            correctedDirectory,
                            "failed-rows-",
                            ".xlsx"
                    );

            try (
                    InputStream input =
                            Files.newInputStream(
                                    sourceFile
                            );

                    XSSFWorkbook sourceWorkbook =
                            new XSSFWorkbook(input);

                    XSSFWorkbook outputWorkbook =
                            new XSSFWorkbook();

                    OutputStream output =
                            Files.newOutputStream(
                                    temporaryOutput
                            )
            ) {
                if (
                        sourceWorkbook
                                .getNumberOfSheets()
                                == 0
                ) {
                    throw new IllegalArgumentException(
                            "The uploaded workbook does not contain a sheet."
                    );
                }

                Sheet sourceSheet =
                        sourceWorkbook.getSheetAt(0);

                Row sourceHeader =
                        sourceSheet.getRow(0);

                if (sourceHeader == null) {
                    throw new IllegalArgumentException(
                            "The uploaded workbook header row is missing."
                    );
                }

                String dataSheetName =
                        safeSheetName(
                                sourceSheet.getSheetName()
                        );

                Sheet outputSheet =
                        outputWorkbook.createSheet(
                                dataSheetName
                        );

                outputSheet.createFreezePane(
                        0,
                        1
                );

                CellStyle headerStyle =
                        createHeaderStyle(
                                outputWorkbook
                        );

                CellStyle correctionStyle =
                        createCorrectionStyle(
                                outputWorkbook
                        );

                Map<String, Integer> sourceColumnIndexes =
                        buildColumnIndexes(
                                sourceHeader,
                                template
                        );

                int lastColumnIndex =
                        Math.max(
                                sourceHeader.getLastCellNum(),
                                shortValue(
                                        template
                                                .getExpectedHeaders()
                                )
                        );

                copyHeader(
                        sourceSheet,
                        sourceHeader,
                        outputSheet,
                        lastColumnIndex,
                        headerStyle
                );

                Sheet errorSheet =
                        createErrorSheet(
                                outputWorkbook
                        );

                CellStyle errorBodyStyle =
                        createErrorBodyStyle(
                                outputWorkbook
                        );

                Sheet metadataSheet =
                        createMetadataSheet(
                                outputWorkbook
                        );

                DataFormatter formatter =
                        new DataFormatter(
                                Locale.ENGLISH
                        );

                FormulaEvaluator evaluator =
                        sourceWorkbook
                                .getCreationHelper()
                                .createFormulaEvaluator();

                Drawing<?> drawing =
                        outputSheet
                                .createDrawingPatriarch();

                CreationHelper helper =
                        outputWorkbook
                                .getCreationHelper();

                List<Integer> orderedFailedRows =
                        failedRowNumbers.stream()
                                .filter(
                                        rowNumber ->
                                                rowNumber != null
                                                        && rowNumber > 1
                                )
                                .sorted()
                                .toList();

                int outputRowIndex = 1;
                int errorRowIndex = 1;
                int metadataRowIndex = 1;

                for (
                        Integer originalRowNumber
                        : orderedFailedRows
                ) {
                    Row sourceRow =
                            sourceSheet.getRow(
                                    originalRowNumber - 1
                            );

                    Row outputRow =
                            outputSheet.createRow(
                                    outputRowIndex
                            );

                    copyDataRow(
                            sourceRow,
                            outputRow,
                            formatter,
                            evaluator,
                            lastColumnIndex
                    );

                    List<ErpImportError> rowErrors =
                            errorsByRow.getOrDefault(
                                    originalRowNumber,
                                    List.of()
                            );

                    for (
                            ErpImportError error
                            : rowErrors
                    ) {
                        applyError(
                                outputRow,
                                outputRowIndex + 1,
                                originalRowNumber,
                                sourceColumnIndexes,
                                correctionStyle,
                                drawing,
                                helper,
                                error
                        );

                        errorRowIndex =
                                appendErrorDetail(
                                        errorSheet,
                                        errorRowIndex,
                                        outputRowIndex + 1,
                                        originalRowNumber,
                                        error,
                                        errorBodyStyle
                                );
                    }

                    metadataRowIndex =
                            appendRetryMetadata(
                                    metadataSheet,
                                    metadataRowIndex,
                                    jobId,
                                    outputRowIndex + 1,
                                    originalRowNumber
                            );

                    outputRowIndex++;
                }

                finalizeErrorSheet(
                        errorSheet
                );

                finalizeMetadataSheet(
                        outputWorkbook,
                        metadataSheet
                );

                outputWorkbook
                        .setForceFormulaRecalculation(
                                true
                        );

                outputWorkbook.write(output);
            }

            moveSafely(
                    temporaryOutput,
                    correctedFile
            );

            return correctedFile;
        } catch (Exception exception) {
            deleteQuietly(
                    temporaryOutput
            );

            throw new RuntimeException(
                    "Failed to generate failed-row workbook for job "
                            + jobId
                            + ".",
                    exception
            );
        }
    }

    public Path correctedWorkbookPath(
            String jobId
    ) {
        String safeJobId =
                sanitizeJobId(jobId);

        Path resolvedPath =
                correctedDirectory
                        .resolve(
                                "Corrected_Import_"
                                        + safeJobId
                                        + ".xlsx"
                        )
                        .normalize();

        if (
                !resolvedPath.startsWith(
                        correctedDirectory
                )
        ) {
            throw new SecurityException(
                    "Corrected workbook path is invalid."
            );
        }

        return resolvedPath;
    }

    public Path findCorrectedWorkbook(
            String jobId
    ) {
        Path file =
                correctedWorkbookPath(jobId);

        return Files.isRegularFile(file)
                ? file
                : null;
    }

    private List<ErpImportError> loadErrors(
            String jobId
    ) {
        List<ErpImportError> errors =
                new ArrayList<>();

        int pageNumber = 0;
        Page<ErpImportError> page;

        do {
            page = errorRepository.findByJobId(
                    jobId,
                    PageRequest.of(
                            pageNumber,
                            ERROR_PAGE_SIZE
                    )
            );

            errors.addAll(
                    page.getContent()
            );

            pageNumber++;
        } while (page.hasNext());

        errors.sort(
                Comparator
                        .comparingInt(
                                ErpImportError::getRowNumber
                        )
                        .thenComparing(
                                error ->
                                        clean(
                                                error.getColumnName()
                                        ),
                                String.CASE_INSENSITIVE_ORDER
                        )
                        .thenComparing(
                                error ->
                                        clean(
                                                error.getErrorCode()
                                        ),
                                String.CASE_INSENSITIVE_ORDER
                        )
        );

        return errors;
    }

    private Map<Integer, List<ErpImportError>>
    groupErrorsByRow(
            List<ErpImportError> errors,
            Set<Integer> failedRowNumbers
    ) {
        Map<Integer, List<ErpImportError>> grouped =
                new LinkedHashMap<>();

        for (ErpImportError error : errors) {
            if (
                    error == null
                            || error.getRowNumber() <= 1
                            || !failedRowNumbers.contains(
                            error.getRowNumber()
                    )
            ) {
                continue;
            }

            grouped.computeIfAbsent(
                    error.getRowNumber(),
                    ignored -> new ArrayList<>()
            ).add(error);
        }

        return grouped;
    }

    private void copyHeader(
            Sheet sourceSheet,
            Row sourceHeader,
            Sheet outputSheet,
            int lastColumnIndex,
            CellStyle headerStyle
    ) {
        Row outputHeader =
                outputSheet.createRow(0);

        DataFormatter formatter =
                new DataFormatter(
                        Locale.ENGLISH
                );

        for (
                int columnIndex = 0;
                columnIndex < lastColumnIndex;
                columnIndex++
        ) {
            Cell sourceCell =
                    sourceHeader.getCell(
                            columnIndex
                    );

            Cell outputCell =
                    outputHeader.createCell(
                            columnIndex
                    );

            String header =
                    sourceCell == null
                            ? ""
                            : formatter
                            .formatCellValue(
                                    sourceCell
                            )
                            .trim();

            outputCell.setCellValue(
                    header
            );

            outputCell.setCellStyle(
                    headerStyle
            );

            int sourceWidth =
                    sourceSheet.getColumnWidth(
                            columnIndex
                    );

            outputSheet.setColumnWidth(
                    columnIndex,
                    Math.clamp(
                            sourceWidth,
                            12 * 256,
                            MAX_EXCEL_COLUMN_WIDTH
                    )
            );
        }

        outputHeader.setHeightInPoints(
                24
        );
    }

    private void copyDataRow(
            Row sourceRow,
            Row outputRow,
            DataFormatter formatter,
            FormulaEvaluator evaluator,
            int lastColumnIndex
    ) {
        for (
                int columnIndex = 0;
                columnIndex < lastColumnIndex;
                columnIndex++
        ) {
            Cell outputCell =
                    outputRow.createCell(
                            columnIndex
                    );

            if (sourceRow == null) {
                outputCell.setBlank();
                continue;
            }

            Cell sourceCell =
                    sourceRow.getCell(
                            columnIndex
                    );

            if (sourceCell == null) {
                outputCell.setBlank();
                continue;
            }

            String displayedValue =
                    formatter.formatCellValue(
                            sourceCell,
                            evaluator
                    );

            outputCell.setCellValue(
                    displayedValue == null
                            ? ""
                            : displayedValue.trim()
            );
        }
    }

    private void applyError(
            Row outputRow,
            int outputExcelRowNumber,
            int originalExcelRowNumber,
            Map<String, Integer> sourceColumnIndexes,
            CellStyle correctionStyle,
            Drawing<?> drawing,
            CreationHelper helper,
            ErpImportError error
    ) {
        Integer columnIndex =
                sourceColumnIndexes.get(
                        normalize(
                                error.getColumnName()
                        )
                );

        Cell cell;

        if (columnIndex == null) {
            cell = outputRow.getCell(
                    0,
                    Row.MissingCellPolicy
                            .CREATE_NULL_AS_BLANK
            );
        } else {
            cell = outputRow.getCell(
                    columnIndex,
                    Row.MissingCellPolicy
                            .CREATE_NULL_AS_BLANK
            );

            if (
                    cell.getStringCellValue() == null
                            || cell.getStringCellValue()
                            .isBlank()
            ) {
                cell.setCellValue(
                        CORRECTION_MARKER
                );
            }
        }

        cell.setCellStyle(
                correctionStyle
        );

        appendComment(
                cell,
                drawing,
                helper,
                buildComment(
                        outputExcelRowNumber,
                        originalExcelRowNumber,
                        error
                )
        );
    }

    private Sheet createErrorSheet(
            Workbook workbook
    ) {
        Sheet sheet =
                workbook.createSheet(
                        ERROR_DETAILS_SHEET
                );

        Row header =
                sheet.createRow(0);

        String[] headers = {
                "Failed Workbook Row",
                "Original Excel Row",
                "Column",
                "Original Value",
                "Error Code",
                "Severity",
                "Exact Error",
                "Suggested Correction"
        };

        CellStyle style =
                createHeaderStyle(
                        workbook
                );

        for (
                int index = 0;
                index < headers.length;
                index++
        ) {
            Cell cell =
                    header.createCell(index);

            cell.setCellValue(
                    headers[index]
            );

            cell.setCellStyle(style);
        }

        sheet.createFreezePane(
                0,
                1
        );

        return sheet;
    }

    private int appendErrorDetail(
            Sheet errorSheet,
            int errorRowIndex,
            int outputExcelRowNumber,
            int originalExcelRowNumber,
            ErpImportError error,
            CellStyle errorBodyStyle
    ) {
        Row row =
                errorSheet.createRow(
                        errorRowIndex
                );

        setCell(
                row,
                0,
                String.valueOf(
                        outputExcelRowNumber
                )
        );

        setCell(
                row,
                1,
                String.valueOf(
                        originalExcelRowNumber
                )
        );

        setCell(
                row,
                2,
                error.getColumnName()
        );

        setCell(
                row,
                3,
                error.getCellValue()
        );

        setCell(
                row,
                4,
                error.getErrorCode()
        );

        setCell(
                row,
                5,
                error.getSeverity()
        );

        setCell(
                row,
                6,
                error.getMessage()
        );

        setCell(
                row,
                7,
                error.getSuggestedFix()
        );

        for (Cell cell : row) {
            cell.setCellStyle(
                    errorBodyStyle
            );
        }

        return errorRowIndex + 1;
    }

    private void finalizeErrorSheet(
            Sheet errorSheet
    ) {
        int[] widths = {
                20,
                20,
                24,
                28,
                34,
                14,
                55,
                55
        };

        for (
                int index = 0;
                index < widths.length;
                index++
        ) {
            errorSheet.setColumnWidth(
                    index,
                    Math.min(
                            widths[index] * 256,
                            MAX_EXCEL_COLUMN_WIDTH
                    )
            );
        }

    }

    private CellStyle createErrorBodyStyle(
            Workbook workbook
    ) {
        CellStyle style =
                workbook.createCellStyle();

        style.setWrapText(true);
        style.setVerticalAlignment(
                VerticalAlignment.TOP
        );

        return style;
    }

    private Sheet createMetadataSheet(
            Workbook workbook
    ) {
        Sheet sheet =
                workbook.createSheet(
                        RETRY_METADATA_SHEET
                );

        Row header =
                sheet.createRow(0);

        setCell(
                header,
                0,
                "Original Job ID"
        );

        setCell(
                header,
                1,
                "Failed Workbook Row"
        );

        setCell(
                header,
                2,
                "Original Excel Row"
        );

        return sheet;
    }

    private int appendRetryMetadata(
            Sheet metadataSheet,
            int metadataRowIndex,
            String jobId,
            int outputExcelRowNumber,
            int originalExcelRowNumber
    ) {
        Row row =
                metadataSheet.createRow(
                        metadataRowIndex
                );

        setCell(
                row,
                0,
                jobId
        );

        setCell(
                row,
                1,
                String.valueOf(
                        outputExcelRowNumber
                )
        );

        setCell(
                row,
                2,
                String.valueOf(
                        originalExcelRowNumber
                )
        );

        return metadataRowIndex + 1;
    }

    private void finalizeMetadataSheet(
            XSSFWorkbook workbook,
            Sheet metadataSheet
    ) {
        metadataSheet.protectSheet(
                "ERP_RETRY_METADATA"
        );

        int sheetIndex =
                workbook.getSheetIndex(
                        metadataSheet
                );

        workbook.setSheetVisibility(
                sheetIndex,
                SheetVisibility.VERY_HIDDEN
        );
    }

    private Map<String, Integer> buildColumnIndexes(
            Row headerRow,
            ImportTemplate template
    ) {
        Map<String, String> acceptedHeaders =
                buildAcceptedHeaders(
                        template
                );

        Map<String, Integer> indexes =
                new HashMap<>();

        DataFormatter formatter =
                new DataFormatter(
                        Locale.ENGLISH
                );

        short firstCell =
                headerRow.getFirstCellNum();

        short lastCell =
                headerRow.getLastCellNum();

        if (
                firstCell < 0
                        || lastCell < 0
        ) {
            return indexes;
        }

        for (
                int index = firstCell;
                index < lastCell;
                index++
        ) {
            Cell cell =
                    headerRow.getCell(index);

            if (cell == null) {
                continue;
            }

            String uploadedHeader =
                    clean(
                            formatter
                                    .formatCellValue(
                                            cell
                                    )
                    );

            String canonicalHeader =
                    acceptedHeaders.get(
                            normalize(
                                    uploadedHeader
                            )
                    );

            if (
                    canonicalHeader == null
                            && acceptedHeaders.isEmpty()
            ) {
                canonicalHeader =
                        uploadedHeader;
            }

            if (canonicalHeader != null) {
                indexes.put(
                        normalize(
                                canonicalHeader
                        ),
                        index
                );
            }
        }

        return indexes;
    }

    private Map<String, String> buildAcceptedHeaders(
            ImportTemplate template
    ) {
        Map<String, String> accepted =
                new HashMap<>();

        for (
                String header
                : safeList(
                template.getExpectedHeaders()
        )
        ) {
            accepted.put(
                    normalize(header),
                    clean(header)
            );
        }

        Map<String, List<String>> aliases =
                template.getAliases() == null
                        ? Map.of()
                        : template.getAliases();

        aliases.forEach(
                (canonical, aliasValues) -> {
                    String resolvedCanonical =
                            accepted.getOrDefault(
                                    normalize(
                                            canonical
                                    ),
                                    clean(canonical)
                            );

                    accepted.put(
                            normalize(canonical),
                            resolvedCanonical
                    );

                    for (
                            String alias
                            : safeList(aliasValues)
                    ) {
                        accepted.put(
                                normalize(alias),
                                resolvedCanonical
                        );
                    }
                }
        );

        return accepted;
    }

    private CellStyle createHeaderStyle(
            Workbook workbook
    ) {
        CellStyle style =
                workbook.createCellStyle();

        style.setFillForegroundColor(
                IndexedColors
                        .DARK_BLUE
                        .getIndex()
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        style.setAlignment(
                HorizontalAlignment.CENTER
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setWrapText(true);

        Font font =
                workbook.createFont();

        font.setBold(true);

        font.setColor(
                IndexedColors
                        .WHITE
                        .getIndex()
        );

        style.setFont(font);

        return style;
    }

    private CellStyle createCorrectionStyle(
            Workbook workbook
    ) {
        CellStyle style =
                workbook.createCellStyle();

        style.setFillForegroundColor(
                IndexedColors
                        .LIGHT_YELLOW
                        .getIndex()
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        style.setWrapText(true);

        style.setVerticalAlignment(
                VerticalAlignment.TOP
        );

        Font font =
                workbook.createFont();

        font.setBold(true);

        font.setColor(
                IndexedColors
                        .RED
                        .getIndex()
        );

        style.setFont(font);

        return style;
    }

    private void appendComment(
            Cell cell,
            Drawing<?> drawing,
            CreationHelper helper,
            String message
    ) {
        Comment existingComment =
                cell.getCellComment();

        String existingText =
                existingComment == null
                        ? null
                        : existingComment
                        .getString()
                        .getString();

        String combined =
                existingText == null
                        || existingText.isBlank()
                        ? message
                        : existingText
                          + "\n\n"
                          + message;

        Comment comment =
                existingComment;

        if (comment == null) {
            ClientAnchor anchor =
                    helper.createClientAnchor();

            anchor.setCol1(
                    cell.getColumnIndex()
            );

            anchor.setCol2(
                    Math.min(
                            cell.getColumnIndex() + 5,
                            16383
                    )
            );

            anchor.setRow1(
                    cell.getRowIndex()
            );

            anchor.setRow2(
                    cell.getRowIndex() + 7
            );

            comment =
                    drawing.createCellComment(
                            anchor
                    );

            comment.setAuthor(
                    "ERP Import Validator"
            );
        }

        comment.setString(
                helper.createRichTextString(
                        limitComment(
                                combined
                        )
                )
        );

        cell.setCellComment(comment);
    }

    private String buildComment(
            int outputExcelRowNumber,
            int originalExcelRowNumber,
            ErpImportError error
    ) {
        StringBuilder message =
                new StringBuilder();

        append(
                message,
                "Failed workbook row",
                String.valueOf(
                        outputExcelRowNumber
                )
        );

        append(
                message,
                "Original Excel row",
                String.valueOf(
                        originalExcelRowNumber
                )
        );

        append(
                message,
                "Column",
                error.getColumnName()
        );

        append(
                message,
                "Original value",
                error.getCellValue()
        );

        append(
                message,
                "Exact error",
                error.getMessage()
        );

        append(
                message,
                "Suggested correction",
                error.getSuggestedFix()
        );

        append(
                message,
                "Error code",
                error.getErrorCode()
        );

        return message.toString();
    }

    private void append(
            StringBuilder target,
            String label,
            String value
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            return;
        }

        if (!target.isEmpty()) {
            target.append('\n');
        }

        target.append(label)
                .append(": ")
                .append(
                        value.trim()
                );
    }

    private void setCell(
            Row row,
            int columnIndex,
            String value
    ) {
        Cell cell =
                row.createCell(
                        columnIndex
                );

        cell.setCellValue(
                value == null
                        ? ""
                        : value
        );
    }

    private void validateInput(
            String jobId,
            Path sourceFile,
            ImportTemplate template
    ) {
        if (
                jobId == null
                        || jobId.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Import job ID is required."
            );
        }

        Objects.requireNonNull(
                sourceFile,
                "Original import workbook is required."
        );

        Objects.requireNonNull(
                template,
                "Import template is required."
        );

        if (
                !Files.isRegularFile(
                        sourceFile
                )
        ) {
            throw new IllegalArgumentException(
                    "Original import workbook was not found."
            );
        }

        String fileName =
                sourceFile
                        .getFileName()
                        .toString()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (!fileName.endsWith(".xlsx")) {
            throw new IllegalArgumentException(
                    "Failed-row workbook generation supports XLSX files only."
            );
        }
    }

    private void moveSafely(
            Path source,
            Path target
    ) throws Exception {
        try {
            Files.move(
                    source,
                    target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (
                AtomicMoveNotSupportedException exception
        ) {
            Files.move(
                    source,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private void deleteQuietly(
            Path file
    ) {
        if (file == null) {
            return;
        }

        try {
            Files.deleteIfExists(file);
        } catch (Exception ignored) {
            // Keep the original generation error as the main failure.
        }
    }

    private String sanitizeJobId(
            String jobId
    ) {
        String value =
                jobId == null
                        ? ""
                        : jobId.trim()
                        .replaceAll(
                                "[^A-Za-z0-9_-]",
                                "_"
                        );

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    "Import job ID is invalid."
            );
        }

        return value.substring(
                0,
                Math.min(
                        value.length(),
                        100
                )
        );
    }

    private String safeSheetName(
            String preferred
    ) {
        String candidate =
                clean(preferred);

        if (candidate.isBlank()) {
            candidate =
                    FAILED_ROWS_SHEET_FALLBACK;
        }

        candidate =
                candidate.replaceAll(
                        "[\\\\/?*\\[\\]:]",
                        "_"
                );

        if (candidate.length() > 31) {
            candidate =
                    candidate.substring(
                            0,
                            31
                    );
        }

        if (
                ERROR_DETAILS_SHEET
                        .equalsIgnoreCase(candidate)
                        || RETRY_METADATA_SHEET
                        .equalsIgnoreCase(candidate)
        ) {
            return FAILED_ROWS_SHEET_FALLBACK;
        }

        return candidate;
    }

    private int shortValue(
            List<String> values
    ) {
        return values == null
                ? 0
                : values.size();
    }

    private String clean(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\uFEFF", "")
                .replace("\"", "")
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }

    private String normalize(
            String value
    ) {
        return clean(value)
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private String limitComment(
            String value
    ) {
        if (
                value == null
                        || value.length()
                        <= COMMENT_MAX_LENGTH
        ) {
            return value;
        }

        return value.substring(
                0,
                COMMENT_MAX_LENGTH
        );
    }

    private <T> List<T> safeList(
            List<T> values
    ) {
        return values == null
                ? List.of()
                : values;
    }
}