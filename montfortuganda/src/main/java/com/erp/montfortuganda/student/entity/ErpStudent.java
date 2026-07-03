package com.erp.montfortuganda.student.entity;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.school.Branch;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Master Identity record for a Student.
 */
@Data
@Entity
@DynamicUpdate
@Table(name = "erp_students")
@EqualsAndHashCode(exclude = {"documents", "currentEnrollment", "application", "branch"})
@ToString(exclude = {"documents", "currentEnrollment", "application", "branch"})
public class ErpStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long studentId;

    // Enterprise Optimistic Locking
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private ErpApplication application;

    @NotNull(message = "Branch is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @NotNull(message = "Admission number is required")
    @Size(max = 50, message = "Admission number cannot exceed 50 characters")
    @Column(name = "admission_no", nullable = false, unique = true, length = 50)
    private String admissionNo;

    @Size(max = 50, message = "Learner LIN cannot exceed 50 characters")
    @Column(name = "learner_lin", length = 50)
    private String learnerLin;

    @NotNull(message = "Admission year is required")
    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;

    @NotNull(message = "Student code is required")
    @Size(max = 50, message = "Student code cannot exceed 50 characters")
    @Column(name = "student_code", nullable = false, unique = true, length = 50)
    private String studentCode;

    @NotNull(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Size(max = 100, message = "Middle name cannot exceed 100 characters")
    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    @Column(name = "last_name", length = 100)
    private String lastName;

    @Size(max = 255)
    @Column(name = "full_name")
    private String fullName;

    @Size(max = 20)
    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Size(max = 100)
    @Column(name = "nationality", length = 100)
    private String nationality;

    // Demographic Identifiers
    @Column(name = "blood_group_id")
    private Long bloodGroupId;

    @Column(name = "religion_id")
    private Long religionId;

    @Column(name = "category_id")
    private Long categoryId;

    @Size(max = 50)
    @Column(name = "house_no", length = 50)
    private String houseNo;

    @Size(max = 150)
    @Column(name = "street", length = 150)
    private String street;

    @Size(max = 100)
    @Column(name = "village", length = 100)
    private String village;

    @Size(max = 100)
    @Column(name = "town_city", length = 100)
    private String townCity;

    @Size(max = 100)
    @Column(name = "district", length = 100)
    private String district;

    @Size(max = 100)
    @Column(name = "state", length = 100)
    private String state;

    @Size(max = 100)
    @Column(name = "country", length = 100)
    private String country;

    @Size(max = 20)
    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Size(max = 255)
    @Column(name = "photo_path", length = 255)
    private String photoPath;

    @NotNull(message = "Student status is required")
    @Size(max = 30)
    @Column(name = "student_status", nullable = false, length = 30)
    private String studentStatus;

    @NotNull(message = "Active status is required")
    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==========================================
    // RELATIONSHIPS
    // ==========================================

    // Inverse relationship for Current Enrollment
    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL)
    private ErpStudentEnrollment currentEnrollment;

    // Inverse relationship for Academic History
    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL)
    private ErpStudentAcademicHistory academicHistory;

    // Fixed Cascade to prevent dangerous hard deletes
    @OneToMany(mappedBy = "student", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<ErpStudentDocument> documents = new ArrayList<>();

    public void addDocument(ErpStudentDocument doc) {
        documents.add(doc);
        doc.setStudent(this);
    }

    // ==========================================
    // JPA LIFECYCLE CALLBACKS
    // ==========================================
    @PrePersist
    protected void onCreate() {
        if (this.active == null) {
            this.active = true;
        }
        if (this.studentStatus == null) {
            this.studentStatus = "ACTIVE";
        }
        if (this.version == null) {
            this.version = 0L;
        }
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}