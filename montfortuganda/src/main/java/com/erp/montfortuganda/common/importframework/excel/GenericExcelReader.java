package com.erp.montfortuganda.common.importframework.excel;

import com.erp.montfortuganda.common.importframework.context.ImportContext;
import com.erp.montfortuganda.common.importframework.registry.ImportTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.StylesTable;
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

    public void processFileInChunks(
            Path filePath, // Changed from InputStream to Path to prevent POI memory leaks
            ImportContext context,
            ImportTemplate template,
            Consumer<List<Map<String, String>>> chunkConsumer) throws Exception {

        String fileName = filePath.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".csv")) {
            processCsv(filePath, context.getChunkSize(), chunkConsumer, context.getTargetRowNumbers());
        } else if (fileName.endsWith(".xlsx")) {
            processXlsx(filePath, context.getChunkSize(), chunkConsumer, context.getTargetRowNumbers());
        } else {
            throw new IllegalArgumentException("Unsupported file format. Please upload a .xlsx or .csv file.");
        }
    }

    // --- CSV PROCESSOR ---
    private void processCsv(Path filePath, int chunkSize, Consumer<List<Map<String, String>>> chunkConsumer, Set<Integer> targetRowNumbers) throws Exception {
        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            String[] headers = null;
            int rowNum = 0;
            List<Map<String, String>> currentChunk = new ArrayList<>(chunkSize);

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] values = parseCsvLine(line);

                if (headers == null) {
                    headers = values;
                    // Clean headers (remove BOM/quotes)
                    for(int i=0; i<headers.length; i++) headers[i] = headers[i].replace("\"", "").trim();
                } else {
                    rowNum++; // Data rows start at 1

                    if (targetRowNumbers != null && !targetRowNumbers.contains(rowNum)) {
                        continue; // Fast-forward skip for retries
                    }

                    Map<String, String> rowMap = new HashMap<>();
                    for (int i = 0; i < headers.length; i++) {
                        String val = (i < values.length) ? values[i] : "";
                        rowMap.put(headers[i], val);
                    }
                    currentChunk.add(rowMap);

                    if (currentChunk.size() >= chunkSize) {
                        chunkConsumer.accept(new ArrayList<>(currentChunk));
                        currentChunk.clear();
                    }
                }
            }
            if (!currentChunk.isEmpty()) {
                chunkConsumer.accept(currentChunk);
            }
        }
    }

    // Robust CSV line parser that respects commas inside quotes
    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '\"') inQuotes = !inQuotes;
            else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());
        return result.toArray(new String[0]);
    }

    // --- XLSX PROCESSOR ---
    private void processXlsx(Path filePath, int chunkSize, Consumer<List<Map<String, String>>> chunkConsumer, Set<Integer> targetRowNumbers) throws Exception {
        // Read directly from File to prevent OOM
        try (OPCPackage pkg = OPCPackage.open(filePath.toFile(), PackageAccess.READ)) {
            XSSFReader xssfReader = new XSSFReader(pkg);
            StylesTable styles = xssfReader.getStylesTable();
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);

            XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            if (iter.hasNext()) {
                try (InputStream sheetStream = iter.next()) {
                    SAXParserFactory saxFactory = SAXParserFactory.newInstance();
                    saxFactory.setNamespaceAware(true);
                    XMLReader sheetParser = saxFactory.newSAXParser().getXMLReader();

                    RowAccumulator handler = new RowAccumulator(chunkSize, chunkConsumer, targetRowNumbers);
                    sheetParser.setContentHandler(new XSSFSheetXMLHandler(styles, null, strings, handler, new org.apache.poi.ss.usermodel.DataFormatter(), false));
                    sheetParser.parse(new InputSource(sheetStream));
                    handler.flush();
                }
            }
        }
    }

    // SAX Handler for XLSX
    private static class RowAccumulator implements SheetContentsHandler {
        private final int chunkSize;
        private final Consumer<List<Map<String, String>>> chunkConsumer;
        private final Set<Integer> targetRowNumbers;

        private final List<Map<String, String>> currentChunk;
        private Map<String, String> currentRow;
        private final Map<Integer, String> headerMap;

        private boolean isHeaderRow = true;

        public RowAccumulator(int chunkSize, Consumer<List<Map<String, String>>> chunkConsumer, Set<Integer> targetRowNumbers) {
            this.chunkSize = chunkSize;
            this.chunkConsumer = chunkConsumer;
            this.targetRowNumbers = targetRowNumbers;
            this.currentChunk = new ArrayList<>(chunkSize);
            this.headerMap = new HashMap<>();
        }

        @Override
        public void startRow(int rowNum) {
            if (rowNum > 0) {
                isHeaderRow = false;
                int actualRowNum = rowNum + 1; // 1-indexed

                if (targetRowNumbers != null && !targetRowNumbers.contains(actualRowNum)) {
                    currentRow = null;
                    return;
                }
                currentRow = new HashMap<>();
            }
        }

        @Override
        public void endRow(int rowNum) {
            if (isHeaderRow || currentRow == null) return;
            if (!currentRow.isEmpty()) currentChunk.add(currentRow);
            if (currentChunk.size() >= chunkSize) flush();
        }

        @Override
        public void cell(String cellReference, String formattedValue, org.apache.poi.xssf.usermodel.XSSFComment comment) {
            if (currentRow == null && !isHeaderRow) return;
            int columnIndex = getColumnIndex(cellReference);
            if (formattedValue != null) formattedValue = formattedValue.trim();

            if (isHeaderRow) headerMap.put(columnIndex, formattedValue);
            else {
                String headerName = headerMap.get(columnIndex);
                if (headerName != null) currentRow.put(headerName, formattedValue);
            }
        }

        public void flush() {
            if (!currentChunk.isEmpty()) {
                chunkConsumer.accept(new ArrayList<>(currentChunk));
                currentChunk.clear();
            }
        }

        private int getColumnIndex(String cellReference) {
            String letters = cellReference.replaceAll("[0-9]", "");
            int sum = 0;
            for (int i = 0; i < letters.length(); i++) {
                sum *= 26;
                sum += (letters.charAt(i) - 'A' + 1);
            }
            return sum - 1;
        }
    }
}