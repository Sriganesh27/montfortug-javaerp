package com.erp.montfortuganda.fees.entity;

import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import com.erp.montfortuganda.school.entity.Level;
import com.erp.montfortuganda.school.entity.SchoolClass;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
@Entity @DynamicUpdate
@Table(name="erp_fee_structures",
       indexes={
         @Index(name="idx_fee_structure_branch_year", columnList="branch_id, academic_year_id"),
         @Index(name="idx_fee_structure_branch_year_status", columnList="branch_id, academic_year_id, structure_status, active"),
         @Index(name="idx_fee_structure_level_classes", columnList="level_id, start_class_id, end_class_id"),
         @Index(name="idx_fee_structure_frequency", columnList="charging_frequency")
       })
public class ErpFeeStructure implements Serializable {
 @Serial private static final long serialVersionUID=1L;
 public enum StructureStatus { DRAFT, PUBLISHED, ARCHIVED }
 public enum ChargingFrequency { PER_TERM, PER_ACADEMIC_YEAR, ONE_TIME, PER_ADMISSION, AS_REQUIRED }

 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="fee_structure_id")
 private Long feeStructureId;

 @NotNull @ManyToOne(fetch=FetchType.LAZY, optional=false)
 @JoinColumn(name="branch_id", nullable=false, foreignKey=@ForeignKey(name="fk_fee_structure_branch"))
 private Branch branch;

 @NotNull @ManyToOne(fetch=FetchType.LAZY, optional=false)
 @JoinColumn(name="academic_year_id", nullable=false, foreignKey=@ForeignKey(name="fk_fee_structure_year"))
 private ErpAcademicYear academicYear;

 @NotNull @ManyToOne(fetch=FetchType.LAZY, optional=false)
 @JoinColumn(name="level_id")
 private Level level;

 @NotNull @ManyToOne(fetch=FetchType.LAZY, optional=false)
 @JoinColumn(name="start_class_id")
 private SchoolClass startClass;

 @ManyToOne(fetch=FetchType.LAZY)
 @JoinColumn(name="end_class_id")
 private SchoolClass endClass;

 @NotBlank @Size(max=150) @Column(name="fee_name", length=150)
 private String feeName;

 @NotBlank @Size(max=50) @Column(name="fee_type", length=50)
 private String feeType;

 @Size(max=50) @Column(name="fee_code", length=50)
 private String feeCode;

 @NotNull @DecimalMin("0.00") @Digits(integer=10, fraction=2)
 @Column(name="amount", precision=12, scale=2)
 private BigDecimal amount;

 @NotNull @Enumerated(EnumType.STRING) @Column(name="charging_frequency", nullable=false, length=30)
 private ChargingFrequency chargingFrequency=ChargingFrequency.PER_TERM;

 @NotNull @Positive @Column(name="version_no", nullable=false)
 private Integer versionNo=1;

 @NotNull @Enumerated(EnumType.STRING) @Column(name="structure_status", nullable=false, length=20)
 private StructureStatus structureStatus=StructureStatus.DRAFT;

 @Column(name="active", nullable=false) private Boolean active=true;
 @Column(name="published_at") private LocalDateTime publishedAt;
 @Size(max=500) @Column(name="remarks", length=500) private String remarks;
 @Column(name="created_by") private Long createdBy;
 @Column(name="created_at", nullable=false, updatable=false) private LocalDateTime createdAt;
 @Column(name="updated_by") private Long updatedBy;
 @Column(name="updated_at", nullable=false) private LocalDateTime updatedAt;
 @Version @Column(name="row_version", nullable=false) private Long rowVersion=0L;

 @PrePersist void beforeInsert() {
   if(versionNo==null) versionNo=1;
   if(structureStatus==null) structureStatus=StructureStatus.DRAFT;
   if(chargingFrequency==null) chargingFrequency=ChargingFrequency.PER_TERM;
   if(active==null) active=true;
   if(rowVersion==null) rowVersion=0L;
   feeName=feeName==null?null:feeName.trim();
   feeType=feeType==null?null:feeType.trim().toUpperCase(java.util.Locale.ROOT);
   feeCode=feeCode==null||feeCode.isBlank()?null:feeCode.trim().toUpperCase(java.util.Locale.ROOT);
   remarks=remarks==null||remarks.isBlank()?null:remarks.trim();
   LocalDateTime now=LocalDateTime.now();
   if(createdAt==null) createdAt=now;
   if(updatedAt==null) updatedAt=now;
 }
 @PreUpdate void beforeUpdate() {
   if(feeName!=null) feeName=feeName.trim();
   if(feeType!=null) feeType=feeType.trim().toUpperCase(java.util.Locale.ROOT);
   if(feeCode!=null) feeCode=feeCode.isBlank()?null:feeCode.trim().toUpperCase(java.util.Locale.ROOT);
   if(remarks!=null) remarks=remarks.trim();
   updatedAt=LocalDateTime.now();
 }
}
