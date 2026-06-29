package com.erp.montfortuganda.admission.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "erp_application_status_history")
public class ErpApplicationStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private ErpApplication application;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 30)
    private ErpApplication.ApplicationStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 30, nullable = false)
    private ErpApplication.ApplicationStatus newStatus;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "changed_at", updatable = false)
    private LocalDateTime changedAt = LocalDateTime.now();

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}