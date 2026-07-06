package com.erp.montfortuganda.school;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@DynamicUpdate
@Table(
        name = "erp_designations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_designation_code",
                        columnNames = "designation_code"
                ),
                @UniqueConstraint(
                        name = "uk_designation_name",
                        columnNames = "designation_name"
                )
        }
)
public class ErpDesignation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum Status {
        ACTIVE,
        INACTIVE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "designation_id")
    private Long designationId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "designation_code", nullable = false, unique = true, length = 20)
    private String designationCode;

    @NotBlank
    @Size(max = 100)
    @Column(name = "designation_name", nullable = false, unique = true, length = 100)
    private String designationName;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.ACTIVE;

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

        if (active == null)
            active = true;

        if (status == null)
            status = Status.ACTIVE;

        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null)
            createdAt = now;

        if (updatedAt == null)
            updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}