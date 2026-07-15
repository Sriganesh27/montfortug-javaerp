package com.erp.montfortuganda;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

public class DataGenerator {
    public static void main(String[] args) {
        String[] headers = {
            "departmentName", "designationName", "reportingManagerEmployeeNo", "title",
            "firstName", "middleName", "lastName", "gender", "dateOfBirth", "nationality",
            "nationalId", "officialEmail", "personalEmail", "mobileNumber", "alternateMobile",
            "district", "county", "subCounty", "parish", "village", "street", "postalCode",
            "employeeCategory", "employeeType", "employmentMode", "joiningDate", "loginEnabled", "remarks"
        };

        List<String[]> data = new java.util.ArrayList<>();
        String[] firstNames = {"John", "Jane", "Alice", "Bob", "Charlie", "Diana", "Edward", "Fiona", "George", "Hannah"};
        String[] lastNames = {"Smith", "Doe", "Brown", "Wilson", "Taylor", "Anderson", "Thomas", "Jackson", "White", "Harris"};
        String[] departments = {"Academics", "Administration", "Finance", "HR", "Transport", "Security"};
        String[] designations = {"Teacher", "Principal", "Accountant", "HR Manager", "Driver", "Guard"};
        
        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < 50; i++) {
            String fn = firstNames[rand.nextInt(firstNames.length)];
            String ln = lastNames[rand.nextInt(lastNames.length)];
            String dept = departments[rand.nextInt(departments.length)];
            String desig = designations[rand.nextInt(designations.length)];
            String gender = rand.nextBoolean() ? "Male" : "Female";
            String title = gender.equals("Male") ? "Mr" : "Ms";
            String dob = "19" + (70 + rand.nextInt(30)) + "-0" + (1 + rand.nextInt(9)) + "-1" + rand.nextInt(9);
            String mobile = "077" + (1000000 + rand.nextInt(8999999));
            String email = fn.toLowerCase() + "." + ln.toLowerCase() + i + "@montfort.ug";
            String nid = "NID" + (100000 + rand.nextInt(899999));
            
            data.add(new String[]{
                dept, desig, "", title, fn, "X", ln, gender, dob, "Ugandan",
                nid, email, "", mobile, "", "Kampala", "Central", "Makindye",
                "Kibuye", "Zone 1", "Main St", "10101", "TEACHING", "FULL_TIME",
                "REGULAR", "2023-01-01", "TRUE", "Generated User"
            });
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Employees");

            // Header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Data rows
            int rowNum = 1;
            for (String[] rowData : data) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < rowData.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(rowData[i]);
                }
            }

            String path = "D:\\Java\\montforterp\\ERP-Java\\Exel formate\\Employee_Bulk_Import_.xlsx";
            try (FileOutputStream out = new FileOutputStream(path)) {
                workbook.write(out);
            }
            System.out.println("Excel file successfully generated at " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
