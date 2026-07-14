package com.erp.montfortuganda.core.task.entity;

import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.model.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "erp_tasks")
public class ErpTask extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "module", nullable = false, length = 30)
    private String module = "ADMISSION";

    @Column(name = "reference_type", nullable = false, length = 30)
    private String referenceType;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Column(name = "task_type", nullable = false, length = 100)
    private String taskType;

    @Column(name = "action_code", length = 100)
    private String actionCode;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_task_assigned_user"))
    private User assignedTo;

    @Column(name = "assigned_role", length = 50)
    private String assignedRole;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    public enum TaskStatus {
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    public enum TaskPriority {
        LOW, NORMAL, HIGH, URGENT
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private TaskPriority priority = TaskPriority.NORMAL;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_task_completed_user"))
    private User completedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id", referencedColumnName = "task_id", foreignKey = @ForeignKey(name = "fk_task_parent"))
    private ErpTask parentTask;

    @Column(name = "sequence_no", nullable = false)
    private Integer sequenceNo = 0;

    @Column(name = "branch_id")
    private Integer branchId;

    @Column(name = "school_id")
    private Integer schoolId;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}