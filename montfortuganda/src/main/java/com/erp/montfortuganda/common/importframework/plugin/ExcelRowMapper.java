package com.erp.montfortuganda.common.importframework.plugin;

public interface ExcelRowMapper<DTO> {
    DTO mapRow(Object rowData, int rowNumber);
}
