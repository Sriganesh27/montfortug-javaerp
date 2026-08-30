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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Data
@Entity
@DynamicUpdate
@Table(name = "erp_scholarship_applications")
public class ErpScholarshipApplication implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public enum ScholarshipType {
        MERIT,
        NEED_BASED,
        SPORTS,
        STAFF_CHILD,
        SIBLING,
        DONOR,
        OTHER
    }
    public enum ApplicationMethod {
        EMAIL_LINK,
        SCHOOL_ASSISTED
    }
    public enum SchoolReviewStatus {
        PENDING,
        UNDER_REVIEW,
        SHORTLISTED,
        WAITLISTED,
        REJECTED,
        NOT_SHORTLISTED
    }
    public enum SuperAdminReviewStatus {
        PENDING,
        UNDER_REVIEW,
        APPROVED,
        PARTIALLY_APPROVED,
        REJECTED
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scholarship_app_id")
    private Long scholarshipAppId;
    @Column(name = "branch_id", nullable = false)
    private Long branchId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "student_id",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private ErpStudent student;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "application_id",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private ErpApplication application;
    @Column(
            name = "academic_year",
            length = 20,
            nullable = false
    )
    private String academicYear;
    /*
     * Store only a one-way hash of the public scholarship token.
     * The raw token is sent to the parent and must not be persisted.
     */
    @Column(
            name = "public_token_hash",
            length = 255
    )
    private String publicTokenHash;
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;
    @Column(name = "token_used_at")
    private LocalDateTime tokenUsedAt;
    /*
     * School-assisted scholarship launch access.
     *
     * The browser receives only the raw opaque launch key once. Only its
     * SHA-256 hash is persisted here. It is intentionally separate from the
     * public parent token so a school key can never be accepted as a public
     * token, or vice versa.
     */
    @Column(
            name = "school_access_token_hash",
            length = 64
    )
    private String schoolAccessTokenHash;
    @Column(name = "school_access_expires_at")
    private LocalDateTime schoolAccessExpiresAt;
    @Column(name = "school_access_issued_at")
    private LocalDateTime schoolAccessIssuedAt;
    @Column(name = "school_access_issued_by")
    private Long schoolAccessIssuedBy;
    /*
     * Detailed housing information.
     *
     * housingStatus remains the primary ownership/occupancy value
     * (for example OWNED, RENTED, RELATIVE_HOME, EMPLOYER_HOUSING, OTHER).
     * The fields below are populated dynamically according to that choice.
     */
    /*
     * Family business / income-generating activity.
     */
    /*
     * Livestock / farming assets.
     */
    /*
     * Other material assets/investments not covered above.
     */
    /*
     * Keep the existing lifecycle status as String for backward compatibility
     * with the current scholarship service while the new workflow is added.
     */
    @Column(
            name = "status",
            length = 50,
            nullable = false
    )
    private String status = "Pending";
    @Column(
            name = "active",
            nullable = false
    )
    private Boolean active = true;
    @Version
    @Column(
            name = "version",
            nullable = false
    )
    private Long version = 0L;
    @Column(name = "created_by")
    private Long createdBy;
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;
    @Column(name = "updated_by")
    private Long updatedBy;
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;
    @OneToMany(
            mappedBy = "scholarshipApplication",
            cascade = CascadeType.ALL,
            orphanRemoval = false
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ErpScholarshipHistory> scholarshipHistories =
            new ArrayList<>();
    public void addScholarshipHistory(
            ErpScholarshipHistory history
    ) {
        scholarshipHistories.add(history);
        history.setScholarshipApplication(this);
    }
    public void removeScholarshipHistory(
            ErpScholarshipHistory history
    ) {
        scholarshipHistories.remove(history);
        history.setScholarshipApplication(null);
    }
    @OneToMany(
            mappedBy = "scholarshipApplication",
            cascade = CascadeType.ALL,
            orphanRemoval = false
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ErpScholarshipApplicationDoc> documents =
            new ArrayList<>();
    public void addDocument(
            ErpScholarshipApplicationDoc document
    ) {
        documents.add(document);
        document.setScholarshipApplication(this);
    }
    public void removeDocument(
            ErpScholarshipApplicationDoc document
    ) {
        documents.remove(document);
        document.setScholarshipApplication(null);
    }
    @PrePersist
    private void onCreate() {
        if (active == null) {
            active = true;
        }
        if (status == null
                || status.isBlank()) {
            status = "Pending";
        }
        LocalDateTime now =
                LocalDateTime.now();
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
