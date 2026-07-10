package com.erp.montfortuganda.infrastructure.enums;

import lombok.Getter;

@Getter
public enum ModuleCode {
    EMPLOYEE("EMP", "Employee", 5, true),
    STUDENT("STD", "Student", 6, true),
    ADMISSION("ADM", "Admission", 5, true),
    INVOICE("INV", "Invoice", 8, true),
    RECEIPT("REC", "Receipt", 8, true),
    DEPARTMENT("DEP", "Department", 3, false),
    DESIGNATION("DSG", "Designation", 3, false),
    LIBRARY("LIB", "Library", 6, true);

    private final String code;
    private final String description;
    private final int padding;
    private final boolean yearlyReset;

    ModuleCode(String code, String description, int padding, boolean yearlyReset) {
        this.code = code;
        this.description = description;
        this.padding = padding;
        this.yearlyReset = yearlyReset;
    }
}