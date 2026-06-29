package com.erp.montfortuganda.admission.entity;

import com.erp.montfortuganda.school.Branch;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "erp_applications")
@EqualsAndHashCode(exclude = {"documents", "statusHistory"})
@ToString(exclude = {"documents", "statusHistory"})
public class ErpApplication {

    // ==========================================
    // NESTED ENUMS
    // ==========================================
    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public enum AdmissionType {
        NEW, TRANSFER
    }

    public enum ApplicationStatus {
        DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, WAITLISTED, ADMITTED
    }

    // ==========================================
    // FIELDS
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "application_no", length = 30, unique = true, nullable = false)
    private String applicationNo;

    // Use LAZY fetch to prevent N+1 queries. We map to the Branch entity.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "academic_year_id", nullable = false)
    private Long academicYearId;

    @Column(name = "branch_class_id", nullable = false)
    private Long branchClassId;

    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", length = 100, nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "religion_id")
    private Long religionId;

    @Column(name = "blood_group_id")
    private Long bloodGroupId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "nationality", length = 100)
    private String nationality = "Uganda";

    @Enumerated(EnumType.STRING)
    @Column(name = "admission_type")
    private AdmissionType admissionType = AdmissionType.NEW;

    @Column(name = "previous_school", length = 255)
    private String previousSchool;

    @Column(name = "guardian_name", length = 150)
    private String guardianName;

    @Column(name = "guardian_mobile", length = 20)
    private String guardianMobile;

    @Column(name = "guardian_email", length = 150)
    private String guardianEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_status")
    private ApplicationStatus applicationStatus = ApplicationStatus.DRAFT;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "status")
    private Integer status = 1;

    // One-To-Many Relationships with strict CASCADE ALL
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ErpApplicationDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ErpApplicationStatusHistory> statusHistory = new ArrayList<>();

    // Utility methods for bidirectional consistency
    public void addDocument(ErpApplicationDocument doc) {
        documents.add(doc);
        doc.setApplication(this);
    }

    public void addHistory(ErpApplicationStatusHistory history) {
        statusHistory.add(history);
        history.setApplication(this);
    }
}