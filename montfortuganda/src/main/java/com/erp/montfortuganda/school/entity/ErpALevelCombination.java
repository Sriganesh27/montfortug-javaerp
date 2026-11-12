package com.erp.montfortuganda.school.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@DynamicUpdate
@Table(
        name = "erp_a_level_combinations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_a_level_combination_branch_code",
                        columnNames = {"branch_id", "combination_code"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_a_level_combination_branch_active",
                        columnList = "branch_id, active"
                ),
                @Index(
                        name = "idx_a_level_combination_principal_1",
                        columnList = "principal_subject_1_id"
                ),
                @Index(
                        name = "idx_a_level_combination_principal_2",
                        columnList = "principal_subject_2_id"
                ),
                @Index(
                        name = "idx_a_level_combination_principal_3",
                        columnList = "principal_subject_3_id"
                ),
                @Index(
                        name = "idx_a_level_combination_subsidiary",
                        columnList = "subsidiary_subject_id"
                )
        }
)
@EqualsAndHashCode(exclude = {
        "branch", "principalSubject1", "principalSubject2",
        "principalSubject3", "subsidiarySubject"
})
@ToString(exclude = {
        "branch", "principalSubject1", "principalSubject2",
        "principalSubject3", "subsidiarySubject"
})
public class ErpALevelCombination implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "combination_id")
    private Long combinationId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "branch_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_a_level_combination_branch"
            )
    )
    private Branch branch;

    @NotBlank
    @Size(max = 30)
    @Column(name = "combination_code", nullable = false, length = 30)
    private String combinationCode;

    @NotBlank
    @Size(max = 150)
    @Column(name = "combination_name", nullable = false, length = 150)
    private String combinationName;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "principal_subject_1_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_a_level_combination_subject_1"
            )
    )
    private ErpSubject principalSubject1;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "principal_subject_2_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_a_level_combination_subject_2"
            )
    )
    private ErpSubject principalSubject2;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "principal_subject_3_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_a_level_combination_subject_3"
            )
    )
    private ErpSubject principalSubject3;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "subsidiary_subject_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_a_level_combination_subsidiary"
            )
    )
    private ErpSubject subsidiarySubject;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void onCreate() {
        if (active == null) {
            active = true;
        }
        if (version == null) {
            version = 0L;
        }

        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}