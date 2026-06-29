package com.erp.montfortuganda.school;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "erp_branch_levels", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"branch_id", "level_id"})
})
@Data
public class BranchLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_level_id")
    private Integer branchLevelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)

    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    private Level level;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "created_by")
    private String createdBy;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BranchLevel)) return false;
        return branchLevelId != null && branchLevelId.equals(((BranchLevel) o).getBranchLevelId());
    }
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}