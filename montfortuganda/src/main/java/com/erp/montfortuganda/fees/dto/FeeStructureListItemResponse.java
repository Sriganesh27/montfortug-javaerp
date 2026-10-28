package com.erp.montfortuganda.fees.dto;

import com.erp.montfortuganda.fees.entity.ErpFeeStructure.ChargingFrequency;
import com.erp.montfortuganda.fees.entity.ErpFeeStructure.StructureStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**

 * Represents a paginated fee-structure list entry.
 *
 * @param feeStructureId   fee structure identifier
 * @param academicYearId   associated academic year identifier
 * @param academicYearCode academic year code
 * @param academicYearName academic year name
 * @param levelId          associated education level identifier
 * @param levelName        education level name
 * @param startClassId     starting class identifier
 * @param startClassName   starting class name
 * @param endClassId       optional ending class identifier
 * @param endClassName     optional ending class name
 * @param feeName          fee structure name
 * @param feeType          fee category
 * @param feeCode          fee code
 * @param amount           configured fee amount
 * @param chargingFrequency fee charging frequency
 * @param versionNo        structure version number
 * @param structureStatus  current structure status
 * @param active           whether the structure is active
 * @param publishedAt      publication timestamp
 * @param createdAt        creation timestamp
 * @param updatedAt        last update timestamp
 */
public record FeeStructureListItemResponse(
        Long feeStructureId,
        Long academicYearId,
        String academicYearCode,
        String academicYearName,
        Integer levelId,
        String levelName,
        Integer startClassId,
        String startClassName,
        Integer endClassId,
        String endClassName,
        String feeName,
        String feeType,
        String feeCode,
        BigDecimal amount,
        ChargingFrequency chargingFrequency,
        Integer versionNo,
        StructureStatus structureStatus,
        Boolean active,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
