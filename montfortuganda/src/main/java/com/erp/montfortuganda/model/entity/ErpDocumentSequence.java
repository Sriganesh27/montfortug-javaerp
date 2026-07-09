package com.erp.montfortuganda.model.entity;

import com.erp.montfortuganda.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "erp_document_sequences",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"branch_id", "module_code", "running_year"})})
@SQLDelete(sql = "UPDATE erp_document_sequences SET deleted = true WHERE id=?")
@SQLRestriction("deleted=false")
public class ErpDocumentSequence extends BaseEntity {
    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "module_code", nullable = false)
    private String moduleCode; // e.g. EMP, ADM, STD

    @Column(name = "running_year", nullable = false)
    private Integer runningYear; // e.g. 2026. If year changes, sequence resets.

    @Column(name = "current_sequence", nullable = false)
    private Long currentSequence = 0L;
}