package com.erp.montfortuganda.fees.dto;

import com.erp.montfortuganda.fees.entity.ErpFeeStructureItem.ChargingFrequency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FeeStructureItemResponse(
        Long feeStructureItemId,
        Long feeStructureId,
        String feeType,
        String feeName,
        BigDecimal amount,
        ChargingFrequency chargingFrequency,
        LocalDate dueDate,
        Boolean mandatory,
        Integer displayOrder,
        Boolean active,
        String remarks,
        Long rowVersion,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
