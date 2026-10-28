package com.erp.montfortuganda.fees.entity;

import com.erp.montfortuganda.school.entity.Branch;
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
@Table(name="erp_fee_structure_items",
 uniqueConstraints=@UniqueConstraint(name="uk_fee_item_branch_type_name", columnNames={"branch_id","fee_type","fee_name"}),
 indexes={@Index(name="idx_fee_structure_item_type", columnList="fee_type"),
          @Index(name="idx_fee_item_branch_active", columnList="branch_id, active")})
public class ErpFeeStructureItem implements Serializable {
 @Serial private static final long serialVersionUID=1L;
 public enum ChargingFrequency { PER_TERM, PER_ACADEMIC_YEAR, ONE_TIME, PER_ADMISSION, AS_REQUIRED }
 public enum StructureStatus { DRAFT, PUBLISHED, ARCHIVED }
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="fee_structure_item_id")
 private Long feeStructureItemId;
 @NotNull @ManyToOne(fetch=FetchType.LAZY,optional=false)
 @JoinColumn(name="branch_id",nullable=false,foreignKey=@ForeignKey(name="fk_fee_item_branch"))
 private Branch branch;
 @NotBlank @Size(max=50) @Column(name="fee_type",nullable=false,length=50) private String feeType;
 @Size(max=50) @Column(name="fee_code",length=50) private String feeCode;
 @NotBlank @Size(max=150) @Column(name="fee_name",nullable=false,length=150) private String feeName;
 @NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2) @Column(name="amount",nullable=false,precision=12,scale=2) private BigDecimal amount;
 @NotNull @Enumerated(EnumType.STRING) @Column(name="charging_frequency",nullable=false,length=30) private ChargingFrequency chargingFrequency=ChargingFrequency.PER_TERM;
 @NotNull @Enumerated(EnumType.STRING) @Column(name="structure_status",nullable=false,length=30) private StructureStatus structureStatus=StructureStatus.DRAFT;
 @NotNull @Column(name="active",nullable=false) private Boolean active=true;
 @Size(max=500) @Column(name="remarks",length=500) private String remarks;
 @Column(name="created_by") private Long createdBy;
 @Column(name="created_at",nullable=false,updatable=false) private LocalDateTime createdAt;
 @Column(name="updated_by") private Long updatedBy;
 @Column(name="updated_at",nullable=false) private LocalDateTime updatedAt;
 @Version @Column(name="row_version",nullable=false) private Long rowVersion=0L;
 @PrePersist void beforeInsert(){normalize(); if(chargingFrequency==null)chargingFrequency=ChargingFrequency.PER_TERM;if(structureStatus==null)structureStatus=StructureStatus.DRAFT;if(active==null)active=true;if(rowVersion==null)rowVersion=0L;LocalDateTime n=LocalDateTime.now();if(createdAt==null)createdAt=n;if(updatedAt==null)updatedAt=n;}
 @PreUpdate void beforeUpdate(){normalize();updatedAt=LocalDateTime.now();}
 private void normalize(){if(feeType!=null)feeType=feeType.trim().toUpperCase(java.util.Locale.ROOT);if(feeCode!=null)feeCode=feeCode.isBlank()?null:feeCode.trim().toUpperCase(java.util.Locale.ROOT);if(feeName!=null)feeName=feeName.trim();if(remarks!=null)remarks=remarks.trim();}
}
