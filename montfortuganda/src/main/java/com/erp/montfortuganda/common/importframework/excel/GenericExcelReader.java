package com.erp.montfortuganda.common.importframework.excel;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.registry.ImportTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
@Component
public class GenericExcelReader {

    public static final String ROW_NUMBER_METADATA_KEY =
            "__ERP_IMPORT_ROW_NUMBER__";

    public void processFileInChunks(
            Path filePath,
            ImportContext context,
            ImportTemplate template,
            Consumer<List<Map<String, String>>> chunkConsumer
    ) throws Exception {

        validateInputs(filePath, context, template, chunkConsumer);

        String fileName = filePath.getFileName()
                .toString()
                .toLowerCase(Locale.ROOT);

        if (fileName.endsWith(".csv")) {
            processCsv(
                    filePath,
                    context,
                    template,
                    chunkConsumer
            );
            return;
        }

        if (fileName.endsWith(".xlsx")) {
            processXlsx(
                    filePath,
                    context,
                    template,
                    chunkConsumer
            );
            return;
        }

        throw new IllegalArgumentException(
                "Only .xlsx and .csv files are supported."
        );
    }

    private void validateInputs(
            Path filePath,
            ImportContext context,
            ImportTemplate template,
            Consumer<List<Map<String, String>>> chunkConsumer
    ) {
        Objects.requireNonNull(filePath, "Import file is required.");
        Objects.requireNonNull(context, "Import context is required.");
        Objects.requireNonNull(template, "Import template is required.");
        Objects.requireNonNull(chunkConsumer, "Chunk consumer is required.");

        if (context.getChunkSize() <= 0) {
            throw new IllegalArgumentException(
                    "Chunk size must be greater than zero."
            );
        }
    }

    // =====================================================================
    // CSV
    // =====================================================================

    private void processCsv(
            Path filePath,
            ImportContext context,
            ImportTemplate template,
            Consumer<List<Map<String, String>>> chunkConsumer
    ) throws Exception {

        int rowNumber = 0;
        int chunkSize = context.getChunkSize();
        Set<Integer> targetRows = context.getTargetRowNumbers();

        Map<Integer, String> headers = null;
        List<Map<String, String>> chunk = new ArrayList<>(chunkSize);

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;

            while ((line = reader.readLine()) != null) {
                rowNumber++;

                if (line.isBlank()) {
                    continue;
                }

                String[] values = parseCsvLine(line);

                if (headers == null) {
                    headers = canonicalizeHeaders(
                            createHeaderMap(values),
                            template
                    );
                    continue;
                }

                if (targetRows != null && !targetRows.contains(rowNumber)) {
                    continue;
                }

                Map<String, String> row = createRow(headers, values, rowNumber);

                chunk.add(row);
                flushIfFull(chunk, chunkSize, chunkConsumer);
            }
        }

        if (headers == null) {
            throw new IllegalArgumentException(
                    "CSV header row is missing."
            );
        }

        flush(chunk, chunkConsumer);
    }

    private Map<Integer, String> createHeaderMap(String[] values) {
        Map<Integer, String> headers = new LinkedHashMap<>();

        for (int index = 0; index < values.length; index++) {
            headers.put(index, clean(values[index]));
        }

        return headers;
    }
    private Map<String, String> createRow(
            Map<Integer, String> headers,
            String[] values,
            int rowNumber
    ) {
        Map<String, String> row = new LinkedHashMap<>();

        headers.forEach((index, header) ->
                row.put(
                        header,
                        index < values.length ? values[index].trim() : ""
                )
        );

        row.put(ROW_NUMBER_METADATA_KEY, String.valueOf(rowNumber));
        return row;
    }
    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;

        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);

            if (character == '"') {
                if (
                        quoted
                                && index + 1 < line.length()
                                && line.charAt(index + 1) == '"'
                ) {
                    value.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                values.add(value.toString().trim());
                value.setLength(0);
            } else {
                value.append(character);
            }
        }

        values.add(value.toString().trim());
        return values.toArray(new String[0]);
    }

    // =====================================================================
    // XLSX
    // =====================================================================

    private void processXlsx(
            Path filePath,
            ImportContext context,
            ImportTemplate template,
            Consumer<List<Map<String, String>>> chunkConsumer
    ) throws Exception {

        try (OPCPackage workbook = OPCPackage.open(
                filePath.toFile(),
                PackageAccess.READ
        )) {
            XSSFReader reader = new XSSFReader(workbook);
            XSSFReader.SheetIterator sheets =
                    (XSSFReader.SheetIterator) reader.getSheetsData();

            if (!sheets.hasNext()) {
                throw new IllegalArgumentException(
                        "Excel worksheet is missing."
                );
            }

            StylesTable styles = reader.getStylesTable();
            ReadOnlySharedStringsTable strings =
                    new ReadOnlySharedStringsTable(workbook);

            RowAccumulator accumulator = new RowAccumulator(
                    context.getChunkSize(),
                    context.getTargetRowNumbers(),
                    template,
                    chunkConsumer
            );

            try (InputStream sheet = sheets.next()) {
                SAXParserFactory factory =
                        SAXParserFactory.newInstance();

                factory.setNamespaceAware(true);

                XMLReader parser = factory
                        .newSAXParser()
                        .getXMLReader();

                parser.setContentHandler(
                        new XSSFSheetXMLHandler(
                                styles,
                                null,
                                strings,
                                accumulator,
                                new DataFormatter(),
                                false
                        )
                );

                parser.parse(new InputSource(sheet));
            }

            accumulator.validateHeaders();
            accumulator.flush();
        }
    }

    // =====================================================================
    // HEADER VALIDATION
    // =====================================================================

    private static Map<Integer, String> canonicalizeHeaders(
            Map<Integer, String> uploadedHeaders,
            ImportTemplate template
    ) {
        HeaderConfiguration config =
                createHeaderConfiguration(template);

        if (config.legacyPassthrough) {
            return canonicalizeLegacyHeaders(uploadedHeaders);
        }

        Map<Integer, String> canonicalHeaders =
                new LinkedHashMap<>();

        Set<String> discovered = new LinkedHashSet<>();
        Set<String> unexpected = new LinkedHashSet<>();
        Set<String> duplicates = new LinkedHashSet<>();

        boolean invalidOrder = false;
        int previousPosition = -1;

        int maxColumn = uploadedHeaders.keySet()
                .stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(-1);

        for (int index = 0; index <= maxColumn; index++) {
            String uploaded = clean(uploadedHeaders.get(index));

            if (uploaded.isBlank()) {
                unexpected.add("Blank column " + (index + 1));
                continue;
            }

            String canonical =
                    config.acceptedHeaders.get(normalize(uploaded));

            if (canonical == null) {
                unexpected.add(uploaded);
                continue;
            }

            String normalizedCanonical = normalize(canonical);

            if (!discovered.add(normalizedCanonical)) {
                duplicates.add(canonical);
                continue;
            }

            canonicalHeaders.put(index, canonical);

            Integer currentPosition =
                    config.expectedPositions.get(normalizedCanonical);

            if (currentPosition != null) {
                if (currentPosition < previousPosition) {
                    invalidOrder = true;
                }

                previousPosition = Math.max(
                        previousPosition,
                        currentPosition
                );
            }
        }

        List<String> missing = config.mandatoryHeaders.stream()
                .filter(header -> !discovered.contains(normalize(header)))
                .toList();

        List<String> errors = new ArrayList<>();

        if (!missing.isEmpty()) {
            errors.add(
                    "Missing mandatory columns: "
                            + String.join(", ", missing)
            );
        }

        if (!unexpected.isEmpty()) {
            errors.add(
                    "Unexpected columns: "
                            + String.join(", ", unexpected)
            );
        }

        if (!duplicates.isEmpty()) {
            errors.add(
                    "Duplicate columns: "
                            + String.join(", ", duplicates)
            );
        }

        if (invalidOrder) {
            errors.add(
                    "Columns are not in the expected order: "
                            + String.join(
                            ", ",
                            safeList(template.getExpectedHeaders())
                    )
            );
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid import headers. "
                            + String.join(" ", errors)
            );
        }

        if (canonicalHeaders.isEmpty()) {
            throw new IllegalArgumentException(
                    "No recognised import columns were found."
            );
        }

        log.debug(
                "Import headers validated: {}",
                canonicalHeaders.values()
        );

        return canonicalHeaders;
    }

    private static Map<Integer, String> canonicalizeLegacyHeaders(
            Map<Integer, String> uploadedHeaders
    ) {
        Map<Integer, String> canonicalHeaders =
                new LinkedHashMap<>();

        Set<String> discovered = new LinkedHashSet<>();
        Set<String> duplicates = new LinkedHashSet<>();
        Set<String> blankColumns = new LinkedHashSet<>();

        int maxColumn = uploadedHeaders.keySet()
                .stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(-1);

        for (int index = 0; index <= maxColumn; index++) {
            String uploaded = clean(uploadedHeaders.get(index));

            if (uploaded.isBlank()) {
                blankColumns.add(
                        "Blank column " + (index + 1)
                );
                continue;
            }

            String normalized = normalize(uploaded);

            if (!discovered.add(normalized)) {
                duplicates.add(uploaded);
                continue;
            }

            /*
             * Backward-compatible mode for existing modules that do not yet
             * provide an ImportTemplate. Preserve the exact uploaded header
             * names because their row mappers already depend on them.
             */
            canonicalHeaders.put(index, uploaded);
        }

        List<String> errors = new ArrayList<>();

        if (!blankColumns.isEmpty()) {
            errors.add(String.join(", ", blankColumns));
        }

        if (!duplicates.isEmpty()) {
            errors.add(
                    "Duplicate columns: "
                            + String.join(", ", duplicates)
            );
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid import headers. "
                            + String.join(" ", errors)
            );
        }

        if (canonicalHeaders.isEmpty()) {
            throw new IllegalArgumentException(
                    "Excel header row does not contain any columns."
            );
        }

        log.debug(
                "Import headers accepted in legacy compatibility mode: {}",
                canonicalHeaders.values()
        );

        return canonicalHeaders;
    }

    private static HeaderConfiguration createHeaderConfiguration(
            ImportTemplate template
    ) {
        HeaderConfiguration config = new HeaderConfiguration();

        List<String> expected = safeList(template.getExpectedHeaders());
        Map<String, List<String>> aliases =
                safeMap(template.getAliases());
        List<String> mandatory = safeList(
                template.getMandatoryColumns()
        );

        if (
                expected.isEmpty()
                        && aliases.isEmpty()
                        && mandatory.isEmpty()
        ) {
            config.legacyPassthrough = true;
            return config;
        }

        for (int index = 0; index < expected.size(); index++) {
            String canonical = clean(expected.get(index));

            if (canonical.isBlank()) {
                throw new IllegalArgumentException(
                        "Expected import header cannot be blank."
                );
            }

            String normalized = normalize(canonical);

            if (config.acceptedHeaders.put(
                    normalized,
                    canonical
            ) != null) {
                throw new IllegalArgumentException(
                        "Duplicate expected header: " + canonical
                );
            }

            config.expectedPositions.put(normalized, index);
        }

        for (Map.Entry<String, List<String>> entry : aliases.entrySet()) {
            String configuredCanonical = clean(entry.getKey());
            String canonical = config.acceptedHeaders.get(
                    normalize(configuredCanonical)
            );

            if (canonical == null) {
                if (!expected.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Alias references unknown header: "
                                    + configuredCanonical
                    );
                }

                canonical = configuredCanonical;
                config.acceptedHeaders.put(
                        normalize(canonical),
                        canonical
                );
            }

            for (String alias : safeList(entry.getValue())) {
                addAlias(config, canonical, alias);
            }
        }

        for (String mandatoryColumn : mandatory) {
            String canonical = config.acceptedHeaders.get(
                    normalize(mandatoryColumn)
            );

            if (canonical == null) {
                throw new IllegalArgumentException(
                        "Mandatory column is not configured: "
                                + mandatoryColumn
                );
            }

            config.mandatoryHeaders.add(canonical);
        }

        return config;
    }

    private static void addAlias(
            HeaderConfiguration config,
            String canonical,
            String alias
    ) {
        String cleanedAlias = clean(alias);

        if (cleanedAlias.isBlank()) {
            return;
        }

        String existing = config.acceptedHeaders.putIfAbsent(
                normalize(cleanedAlias),
                canonical
        );

        if (existing != null && !existing.equals(canonical)) {
            throw new IllegalArgumentException(
                    "Alias is assigned to multiple columns: "
                            + cleanedAlias
            );
        }
    }

    private static String clean(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\uFEFF", "")
                .replace("\"", "")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private static String normalize(String value) {
        return clean(value).toLowerCase(Locale.ROOT);
    }

    private static <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private static <K, V> Map<K, V> safeMap(Map<K, V> values) {
        return values == null ? Map.of() : values;
    }

    private static void flushIfFull(
            List<Map<String, String>> chunk,
            int chunkSize,
            Consumer<List<Map<String, String>>> consumer
    ) {
        if (chunk.size() >= chunkSize) {
            flush(chunk, consumer);
        }
    }

    private static void flush(
            List<Map<String, String>> chunk,
            Consumer<List<Map<String, String>>> consumer
    ) {
        if (chunk.isEmpty()) {
            return;
        }

        consumer.accept(new ArrayList<>(chunk));
        chunk.clear();
    }

    private static class HeaderConfiguration {

        private boolean legacyPassthrough;

        private final Map<String, String> acceptedHeaders =
                new LinkedHashMap<>();

        private final Map<String, Integer> expectedPositions =
                new HashMap<>();

        private final List<String> mandatoryHeaders =
                new ArrayList<>();
    }

    // =====================================================================
    // XLSX SAX ROW HANDLER
    // =====================================================================

    private static class RowAccumulator
            implements XSSFSheetXMLHandler.SheetContentsHandler {

        private final int chunkSize;
        private final Set<Integer> targetRows;
        private final ImportTemplate template;
        private final Consumer<List<Map<String, String>>> consumer;

        private final Map<Integer, String> uploadedHeaders =
                new LinkedHashMap<>();

        private final List<Map<String, String>> chunk;

        private Map<Integer, String> canonicalHeaders;
        private Map<String, String> currentRow;
        private boolean headerProcessed;
        private boolean headerRow;

        private RowAccumulator(
                int chunkSize,
                Set<Integer> targetRows,
                ImportTemplate template,
                Consumer<List<Map<String, String>>> consumer
        ) {
            this.chunkSize = chunkSize;
            this.targetRows = targetRows;
            this.template = template;
            this.consumer = consumer;
            this.chunk = new ArrayList<>(chunkSize);
        }

        @Override
        public void startRow(int rowNum) {
            headerRow = rowNum == 0;

            if (headerRow) {
                currentRow = null;
                return;
            }

            if (!headerProcessed) {
                throw new IllegalArgumentException(
                        "Excel header row is invalid."
                );
            }

            int actualRowNumber = rowNum + 1;

            if (
                    targetRows != null
                            && !targetRows.contains(actualRowNumber)
            ) {
                currentRow = null;
                return;
            }

            currentRow = new LinkedHashMap<>();
            currentRow.put(
                    ROW_NUMBER_METADATA_KEY,
                    String.valueOf(actualRowNumber)
            );
        }

        @Override
        public void endRow(int rowNum) {
            if (rowNum == 0) {
                canonicalHeaders = canonicalizeHeaders(
                        uploadedHeaders,
                        template
                );

                headerProcessed = true;
                return;
            }

            if (currentRow == null || currentRow.size() == 1) {
                return;
            }

            canonicalHeaders.values().forEach(
                    header -> currentRow.putIfAbsent(header, "")
            );

            chunk.add(currentRow);
            flushIfFull(chunk, chunkSize, consumer);
        }

        @Override
        public void cell(
                String cellReference,
                String formattedValue,
                XSSFComment comment
        ) {
            if (cellReference == null) {
                return;
            }

            int columnIndex = getColumnIndex(cellReference);
            String value = formattedValue == null
                    ? ""
                    : formattedValue.trim();

            if (headerRow) {
                uploadedHeaders.put(
                        columnIndex,
                        clean(value)
                );
                return;
            }

            if (currentRow == null) {
                return;
            }

            String header = canonicalHeaders.get(columnIndex);

            if (header != null) {
                currentRow.put(header, value);
            }
        }

        private void validateHeaders() {
            if (!headerProcessed) {
                throw new IllegalArgumentException(
                        "Excel header row is missing."
                );
            }
        }

        private void flush() {
            GenericExcelReader.flush(chunk, consumer);
        }

        private int getColumnIndex(String cellReference) {
            int result = 0;

            for (char character : cellReference.toCharArray()) {
                if (!Character.isLetter(character)) {
                    break;
                }

                result = result * 26
                        + Character.toUpperCase(character)
                        - 'A'
                        + 1;
            }

            return result - 1;
        }
    }
}