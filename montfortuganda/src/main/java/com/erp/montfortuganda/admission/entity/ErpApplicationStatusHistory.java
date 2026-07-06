package com.erp.montfortuganda.admission.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "erp_application_status_history")
@EqualsAndHashCode(exclude = "application")
@ToString(exclude = "application")
public class ErpApplicationStatusHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "application_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_application_status_history_application")
    )
    private ErpApplication application;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 30)
    private ErpApplication.ApplicationStatus oldStatus;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private ErpApplication.ApplicationStatus newStatus;

    // TODO: Replace with ErpUser entity when User module is ready (@ManyToOne)
    @Column(name = "changed_by")
    private Long changedBy;

    // Unbounded TEXT block allows admission officers to write comprehensive evaluations
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "changed_at", updatable = false)
    private LocalDateTime changedAt;

    @PrePersist
    private void onCreate() {
        if (active == null) {
            active = true;
        }
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }
}