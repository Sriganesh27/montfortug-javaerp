package com.erp.montfortuganda.school.enums;

import lombok.Getter;

@Getter
public enum DesignationSortField {
    NAME("designationName"),
    CODE("designationCode"),
    DEPARTMENT("department.departmentName"),
    CREATED("createdAt"),
    UPDATED("updatedAt"),
    DISPLAY_ORDER("displayOrder");

    private final String dbField;

    DesignationSortField(String dbField) { this.dbField = dbField; }
}