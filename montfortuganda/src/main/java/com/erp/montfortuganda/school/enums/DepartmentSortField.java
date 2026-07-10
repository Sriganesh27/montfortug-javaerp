package com.erp.montfortuganda.school.enums;

import lombok.Getter;

@Getter
public enum DepartmentSortField {
    NAME("departmentName"),
    CODE("departmentCode"),
    CREATED("createdAt"),
    UPDATED("updatedAt"),
    DISPLAY_ORDER("displayOrder");

    private final String dbField;

    DepartmentSortField(String dbField) { this.dbField = dbField; }
}