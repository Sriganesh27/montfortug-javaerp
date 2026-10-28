package com.erp.montfortuganda.fees.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A row in the unified fee list. Fields not applicable to a source are null.
 * feeSource is either MANDATORY or OPTIONAL.
 */
public record CombinedFeeListItemResponse(
        Long id,
        String feeSource,
        Long academicYearId,
        String academicYearCode,
        String academicYearName,
        Integer levelId,
        String levelName,
        Integer startClassId,
        String startClassName,
        String startClassCode,
        Integer endClassId,
        String endClassName,
        String endClassCode,
        String feeName,
        String feeType,
        String feeCode,
        BigDecimal amount,
        String chargingFrequency,
        Integer versionNo,
        String status,
        Boolean active,
        LocalDateTime publishedAt,
        String remarks,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
