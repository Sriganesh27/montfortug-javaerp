package com.erp.montfortuganda;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelHeaderReader {
    public static void main(String[] args) {
        String path = "D:\\Java\\montforterp\\ERP-Java\\Exel formate\\Employee_Bulk_Import_.xlsx";
        try (FileInputStream fis = new FileInputStream(path);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) headerRow = sheet.getRow(1);
            if (headerRow == null) headerRow = sheet.getRow(2); // check first few rows
            
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }
            System.out.println("EXCEL_HEADERS: " + headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
