package com.erp.montfortuganda.employee.bulkimport.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Exact row/cell error used later to generate the correction workbook.
 */
@Getter
@Builder
public class EmployeeBulkImportError {

    private final int excelRowNumber;
    private final String columnName;
    private final String rejectedValue;
    private final String errorCode;
    private final String message;

    @Builder.Default
    private final String replacementValue = "ENTER VALID DATA";
}