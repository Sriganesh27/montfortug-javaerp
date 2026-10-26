package com.erp.montfortuganda.fees.dto;

import com.erp.montfortuganda.fees.entity.ErpFeeStructure.StructureStatus;

import java.time.LocalDateTime;
import java.util.List;

public record FeeStructureResponse(
        Long feeStructureId,
        Integer branchId,
        Long academicYearId,
        String academicYearCode,
        String academicYearName,
        Integer classId,
        String classCode,
        String className,
        Integer levelId,
        String levelName,
        Long termId,
        String termCode,
        String termName,
        Integer versionNo,
        StructureStatus structureStatus,
        LocalDateTime publishedAt,
        String remarks,
        Long rowVersion,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<FeeStructureItemResponse> items
) {
}
