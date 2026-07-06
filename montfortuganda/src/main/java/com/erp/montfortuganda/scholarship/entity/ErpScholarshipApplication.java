package com.erp.montfortuganda.scholarship.entity;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.student.entity.ErpStudent;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@DynamicUpdate
@Table(name = "erp_scholarship_applications")
@EqualsAndHashCode(exclude = {"application", "student", "documents"})
@ToString(exclude = {"application", "student", "documents"})
public class ErpScholarshipApplication implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scholarship_app_id") // We successfully changed this in the DB today!
    private Long scholarshipAppId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ErpStudent student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ErpApplication application;

    @Column(name = "amount_requested_ugx", precision = 38, scale = 2, nullable = false)
    private BigDecimal amountRequestedUgx = BigDecimal.ZERO;

    @Column(name = "term_requested", length = 50, nullable = false)
    private String termRequested;

    @Column(name = "category", length = 100, nullable = false)
    private String category;

    @Column(name = "status", length = 50)
    private String status = "Pending";

    @Column(name = "academic_year", length = 20, nullable = false)
    private String academicYear;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Link the docs here!
    @OneToMany(mappedBy = "scholarshipApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ErpScholarshipApplicationDoc> documents = new ArrayList<>();

    public void addDocument(ErpScholarshipApplicationDoc doc) {
        documents.add(doc);
        doc.setScholarshipApplication(this);
    }

    @PrePersist
    private void onCreate() {
        if (active == null) active = true;
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}