package com.erp.montfortuganda.fees.dto;
import com.erp.montfortuganda.fees.entity.ErpFeeStructure.StructureStatus;
import com.erp.montfortuganda.fees.entity.ErpFeeStructure.ChargingFrequency;
import java.math.BigDecimal; import java.time.LocalDateTime;
public record FeeStructureResponse(Long feeStructureId,Integer branchId,Long academicYearId,String academicYearCode,String academicYearName,
 Integer levelId,String levelName,Integer startClassId,String startClassName,Integer endClassId,String endClassName,
 String feeName,String feeType,String feeCode,BigDecimal amount,ChargingFrequency chargingFrequency,Integer versionNo,StructureStatus structureStatus,
 Boolean active,LocalDateTime publishedAt,String remarks,Long rowVersion,LocalDateTime createdAt,LocalDateTime updatedAt) {}
