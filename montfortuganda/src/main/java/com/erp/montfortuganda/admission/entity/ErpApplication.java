package com.erp.montfortuganda.admission.entity;

import com.erp.montfortuganda.school.entity.Branch;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@DynamicUpdate
@Table(name = "erp_applications")
@EqualsAndHashCode(exclude = {"documents", "statusHistory"})
@ToString(exclude = {"documents", "statusHistory"})
public class ErpApplication {

    public enum Gender {
        MALE,
        FEMALE,
        OTHER
    }

    public enum AdmissionType {
        NEW,
        READMISSION,
        TRANSFER
    }

    /**
     * Existing high-level application status retained for compatibility with
     * the public portal, dashboards and current repositories.
     */
    public enum ApplicationStatus {
        DRAFT,
        SUBMITTED,
        UNDER_REVIEW,
        APPROVED,
        REJECTED,
        WAITLISTED,
        ADMITTED
    }

    /**
     * Detailed stage of the admission workflow.
     */
    public enum CurrentStage {
        APPLICATION_DRAFT,
        APPLICATION_VERIFICATION,
        SCHOOL_VISIT,
        ENTRANCE_TEST,
        PARENT_FEE_DISCUSSION,
        PAYMENT,
        SCHOLARSHIP,
        FINAL_ADMISSION,
        ENROLLED,
        CLOSED
    }

    public enum VerificationStatus {
        NOT_STARTED,
        PENDING,
        ADDITIONAL_DOCUMENTS_REQUIRED,
        APPROVED,
        REJECTED
    }

    public enum DocumentStatus {
        NOT_STARTED,
        PENDING,
        REUPLOAD_REQUIRED,
        VERIFIED,
        REJECTED
    }

    public enum TestStatus {
        NOT_SCHEDULED,
        SCHEDULED,
        CONDUCTED,
        PASSED,
        FAILED,
        ABSENT,
        RETEST_REQUIRED,
        COMPLETED
    }

    public enum FeeDecisionStatus {
        NOT_STARTED,
        DECISION_PENDING,
        FEE_ACCEPTED,
        SCHOLARSHIP_REQUESTED,
        FEE_DECLINED,
        COMPLETED
    }

    public enum PaymentStatus {
        NOT_STARTED,
        PENDING,
        PARTIALLY_PAID,
        BALANCE_PENDING,
        PAID,
        REJECTED,
        REVERSED,
        DEFAULTED,
        COMPLETED
    }

    public enum AdmissionStatus {
        PENDING,
        APPROVED,
        REJECTED,
        ENROLLED,
        CLOSED
    }

    // =====================================================================
    // 1. CORE IDENTITY AND APPLICATION REFERENCE
    // =====================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "application_no", nullable = false, unique = true, length = 50)
    private String applicationNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "academic_year_id", nullable = false)
    private Long academicYearId;

    /**
     * References erp_classes.class_id, whose database type is INT.
     */
    @Column(name = "branch_class_id", nullable = false)
    private Integer branchClassId;

    /**
     * Legacy public-portal term value retained during the transition to the
     * branch-scoped academic-term foreign key.
     */
    @Column(name = "term", nullable = false, length = 20)
    private String term = "";

    @Column(name = "joining_term_id")
    private Long joiningTermId;

    @Enumerated(EnumType.STRING)
    @Column(name = "admission_type", length = 20)
    private AdmissionType admissionType = AdmissionType.NEW;

    // =====================================================================
    // 2. STUDENT CONVERSION LINKAGE
    // =====================================================================

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "enrollment_id")
    private Long enrollmentId;

    @Column(name = "student_created", nullable = false)
    private Boolean studentCreated = false;

    @Column(name = "converted_by")
    private Long convertedBy;

    @Column(name = "converted_at")
    private LocalDateTime convertedAt;

    @Column(name = "workflow_locked", nullable = false)
    private Boolean workflowLocked = false;

    // =====================================================================
    // 3. PRIMARY CONTACT AND STUDENT DETAILS
    // =====================================================================

    @Column(name = "primary_email", length = 100)
    private String primaryEmail;

    @Column(name = "primary_mobile", length = 20)
    private String primaryMobile;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "middle_name", length = 50)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 20)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "nationality", length = 50)
    private String nationality = "Uganda";

    @Column(name = "photo_path", columnDefinition = "TEXT")
    private String photoPath = "";

    @Column(name = "more_info", columnDefinition = "TEXT")
    private String moreInfo = "";

    // =====================================================================
    // 4. ACADEMIC AND PREVIOUS-SCHOOL DETAILS
    // =====================================================================

    /**
     * Kept as VARCHAR because the existing public form and database column
     * currently store this value as text.
     */
    @Column(name = "date_of_registration", nullable = false, length = 20)
    private String dateOfRegistration = "";

    /**
     * Kept as String until the scholarship workflow is aligned because the
     * existing database may contain legacy status values.
     */
    @Column(name = "scholarship_status", nullable = false, length = 50)
    private String scholarshipStatus = "NOT_APPLIED";

    @Column(name = "previous_school", length = 150)
    private String previousSchool;

    @Column(name = "former_school", columnDefinition = "TEXT")
    private String formerSchool = "";

    @Column(name = "former_school_code", nullable = false, length = 50)
    private String formerSchoolCode = "";

    @Column(name = "former_school_lin", nullable = false, length = 50)
    private String formerSchoolLin = "";

    @Column(name = "ple_ref", nullable = false, length = 50)
    private String pleRef = "";

    @Column(name = "ple_score")
    private Double pleScore;

    @Column(name = "uce_ref", nullable = false, length = 50)
    private String uceRef = "";

    @Column(name = "uce_score")
    private Double uceScore;

    @Column(name = "subject_marks", columnDefinition = "TEXT")
    private String subjectMarks = "";

    @Column(name = "prev_marks_doc", columnDefinition = "TEXT")
    private String prevMarksDoc = "";

    // =====================================================================
    // 5. FATHER DETAILS
    // =====================================================================

    @Column(name = "father_name", nullable = false, length = 50)
    private String fatherName = "";

    @Column(name = "father_contact", nullable = false, length = 20)
    private String fatherContact = "";

    @Column(name = "father_email", nullable = false, length = 100)
    private String fatherEmail = "";

    @Column(name = "father_occupation", columnDefinition = "TEXT")
    private String fatherOccupation = "";

    @Column(name = "father_education", nullable = false, length = 50)
    private String fatherEducation = "";

    @Column(name = "father_age")
    private Integer fatherAge = 0;

    // =====================================================================
    // 6. MOTHER DETAILS
    // =====================================================================

    @Column(name = "mother_name", nullable = false, length = 50)
    private String motherName = "";

    @Column(name = "mother_contact", nullable = false, length = 20)
    private String motherContact = "";

    @Column(name = "mother_email", nullable = false, length = 100)
    private String motherEmail = "";

    @Column(name = "mother_occupation", columnDefinition = "TEXT")
    private String motherOccupation = "";

    @Column(name = "mother_education", nullable = false, length = 50)
    private String motherEducation = "";

    @Column(name = "mother_age")
    private Integer motherAge = 0;

    // =====================================================================
    // 7. GUARDIAN DETAILS
    // =====================================================================

    @Column(name = "guardian_name", nullable = false, length = 50)
    private String guardianName = "";

    /**
     * Legacy field retained because the current public application DTO and
     * service still populate guardian_mobile.
     */
    @Column(name = "guardian_mobile", length = 20)
    private String guardianMobile;

    @Column(name = "guardian_contact", nullable = false, length = 20)
    private String guardianContact = "";

    @Column(name = "guardian_email", length = 100)
    private String guardianEmail;

    @Column(name = "guardian_relation", nullable = false, length = 50)
    private String guardianRelation = "";

    @Column(name = "guardian_occupation", columnDefinition = "TEXT")
    private String guardianOccupation = "";

    @Column(name = "guardian_education", nullable = false, length = 50)
    private String guardianEducation = "";

    @Column(name = "guardian_location", columnDefinition = "TEXT")
    private String guardianLocation = "";

    @Column(name = "guardian_age")
    private Integer guardianAge = 0;

    // =====================================================================
    // 8. PHYSICAL ADDRESS
    // =====================================================================

    @Column(name = "address_region", nullable = false, length = 50)
    private String addressState = "";

    @Column(name = "address_district", nullable = false, length = 50)
    private String addressDistrict = "";

    @Column(name = "address_village", nullable = false, length = 50)
    private String addressVillage = "";

    @Column(name = "address_street", columnDefinition = "TEXT")
    private String addressStreet = "";

    @Column(name = "address_house", nullable = false, length = 50)
    private String addressHouse = "";

    @Column(name = "address_postal", nullable = false, length = 50)
    private String addressPostal = "";

    // =====================================================================
    // 9. HIGH-LEVEL AND DETAILED WORKFLOW STATUS
    // =====================================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "application_status", length = 50)
    private ApplicationStatus applicationStatus = ApplicationStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage", nullable = false, length = 50)
    private CurrentStage currentStage = CurrentStage.APPLICATION_VERIFICATION;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 50)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_status", nullable = false, length = 50)
    private DocumentStatus documentStatus = DocumentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_status", nullable = false, length = 50)
    private TestStatus testStatus = TestStatus.NOT_SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_decision_status", nullable = false, length = 50)
    private FeeDecisionStatus feeDecisionStatus = FeeDecisionStatus.NOT_STARTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 50)
    private PaymentStatus paymentStatus = PaymentStatus.NOT_STARTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "admission_status", nullable = false, length = 50)
    private AdmissionStatus admissionStatus = AdmissionStatus.PENDING;

    @Column(name = "school_visit_at")
    private LocalDateTime schoolVisitAt;

    @Column(name = "school_visit_remarks", length = 1000)
    private String schoolVisitRemarks;

    @Column(name = "verification_decision_by")
    private Long verificationDecisionBy;

    @Column(name = "verification_decision_at")
    private LocalDateTime verificationDecisionAt;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    // =====================================================================
    // 10. SYSTEM AUDIT
    // =====================================================================

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "status")
    private Integer status = 1;

    // =====================================================================
    // 11. RELATIONSHIPS
    // =====================================================================

    @OneToMany(mappedBy = "application", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<ErpApplicationDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<ErpApplicationStatusHistory> statusHistory = new ArrayList<>();

    // =====================================================================
    // 12. LIFECYCLE AND COMPATIBILITY HELPERS
    // =====================================================================

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (studentCreated == null) {
            studentCreated = false;
        }
        if (workflowLocked == null) {
            workflowLocked = false;
        }
        if (status == null) {
            status = 1;
        }
        if (scholarshipStatus == null || scholarshipStatus.isBlank()) {
            scholarshipStatus = "NOT_APPLIED";
        }
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Compatibility overload for the existing ApplicationCreateDTO, which
     * currently exposes branchClassId as Long.
     */
    public void setBranchClassId(Long branchClassId) {
        this.branchClassId = branchClassId == null
                ? null
                : Math.toIntExact(branchClassId);
    }

    public void setBranchClassId(Integer branchClassId) {
        this.branchClassId = branchClassId;
    }

    public void addDocument(ErpApplicationDocument document) {
        if (document == null) {
            return;
        }
        documents.add(document);
        document.setApplication(this);
    }

    public void addHistory(ErpApplicationStatusHistory history) {
        if (history == null) {
            return;
        }
        statusHistory.add(history);
        history.setApplication(this);
    }
}
