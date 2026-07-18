package com.erp.montfortuganda.admission.entity;

import com.erp.montfortuganda.school.entity.Branch;
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

    public enum Gender { MALE, FEMALE, OTHER }
    public enum AdmissionType { NEW, TRANSFER }
    public enum ApplicationStatus { DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, WAITLISTED, ADMITTED }

    // ==========================================
    // 1. CORE IDENTITY & SYSTEM NOTIFICATIONS
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "application_no", unique = true, nullable = false, length = 50)
    private String applicationNo;

    @Column(name = "primary_email", length = 100)
    private String primaryEmail;

    @Column(name = "primary_mobile", length = 20)
    private String primaryMobile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "academic_year_id", nullable = false)
    private Long academicYearId;

    @Column(name = "branch_class_id", nullable = false)
    private Long branchClassId;

    @Column(name = "admission_type", length = 20)
    @Enumerated(EnumType.STRING)
    private AdmissionType admissionType = AdmissionType.NEW;

    // ==========================================
    // 2. STUDENT DETAILS
    // ==========================================
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "middle_name", length = 50)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "gender", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "nationality", length = 50)
    private String nationality = "Uganda";

    @Column(name = "photo_path", columnDefinition = "TEXT")
    private String photoPath = "";

    @Column(name = "more_info", columnDefinition = "TEXT")
    private String moreInfo = "";

    // ==========================================
    // 3. ACADEMIC & ENROLLMENT HISTORY
    // ==========================================
    @Column(name = "term", length = 20) private String term = "";
    @Column(name = "date_of_registration", length = 20) private String dateOfRegistration = "";
    @Column(name = "scholarship_status", length = 50) private String scholarshipStatus = "";

    @Column(name = "previous_school", length = 150) private String previousSchool;
    @Column(name = "former_school", columnDefinition = "TEXT") private String formerSchool = "";
    @Column(name = "former_school_code", length = 50) private String formerSchoolCode = "";
    @Column(name = "former_school_lin", length = 50) private String formerSchoolLin = "";

    @Column(name = "ple_ref", length = 50) private String pleRef = "";
    @Column(name = "ple_score") private Double pleScore;
    @Column(name = "uce_ref", length = 50) private String uceRef = "";
    @Column(name = "uce_score") private Double uceScore;

    @Column(name = "subject_marks", columnDefinition = "TEXT") private String subjectMarks = "";
    @Column(name = "prev_marks_doc", columnDefinition = "TEXT") private String prevMarksDoc = "";

    // ==========================================
    // 4. FATHER'S DETAILS
    // ==========================================
    @Column(name = "father_name", length = 50) private String fatherName = "";
    @Column(name = "father_contact", length = 20) private String fatherContact = "";
    @Column(name = "father_email", length = 100) private String fatherEmail = "";
    @Column(name = "father_occupation", columnDefinition = "TEXT") private String fatherOccupation = "";
    @Column(name = "father_education", length = 50) private String fatherEducation = "";
    @Column(name = "father_age") private Integer fatherAge = 0;

    // ==========================================
    // 5. MOTHER'S DETAILS
    // ==========================================
    @Column(name = "mother_name", length = 50) private String motherName = "";
    @Column(name = "mother_contact", length = 20) private String motherContact = "";
    @Column(name = "mother_email", length = 100) private String motherEmail = "";
    @Column(name = "mother_occupation", columnDefinition = "TEXT") private String motherOccupation = "";
    @Column(name = "mother_education", length = 50) private String motherEducation = "";
    @Column(name = "mother_age") private Integer motherAge = 0;

    // ==========================================
    // 6. GUARDIAN'S DETAILS
    // ==========================================
    @Column(name = "guardian_name", length = 50) private String guardianName = "";
    @Column(name = "guardian_mobile", length = 20) private String guardianMobile; // Legacy fallback
    @Column(name = "guardian_contact", length = 20) private String guardianContact = "";
    @Column(name = "guardian_email", length = 100) private String guardianEmail;
    @Column(name = "guardian_relation", length = 50) private String guardianRelation = "";
    @Column(name = "guardian_occupation", columnDefinition = "TEXT") private String guardianOccupation = "";
    @Column(name = "guardian_education", length = 50) private String guardianEducation = "";
    @Column(name = "guardian_location", columnDefinition = "TEXT") private String guardianLocation = "";
    @Column(name = "guardian_age") private Integer guardianAge = 0;

    // ==========================================
    // 7. PHYSICAL ADDRESS
    // ==========================================
    @Column(name = "address_region", length = 50) private String addressState = "";
    @Column(name = "address_district", length = 50) private String addressDistrict = "";
    @Column(name = "address_village", length = 50) private String addressVillage = "";
    @Column(name = "address_street", columnDefinition = "TEXT") private String addressStreet = "";
    @Column(name = "address_house", length = 50) private String addressHouse = "";
    @Column(name = "address_postal", length = 50) private String addressPostal = "";

    // ==========================================
    // 8. SYSTEM AUDIT & STATUS
    // ==========================================
    @Enumerated(EnumType.STRING)
    @Column(name = "application_status", length = 50)
    private ApplicationStatus applicationStatus = ApplicationStatus.DRAFT;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_by") private Long createdBy;
    @Column(name = "updated_by") private Long updatedBy;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at") private LocalDateTime updatedAt = LocalDateTime.now();
    @Column(name = "status") private Integer status = 1;
    // ==========================================
    // 8.5 STUDENT RECORD LINKAGE
    // ==========================================
    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "student_created", nullable = false)
    private Boolean studentCreated = false;
    // ==========================================
    // 9. RELATIONSHIPS
    // ==========================================
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ErpApplicationDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ErpApplicationStatusHistory> statusHistory = new ArrayList<>();

    // Utility methods
    public void addDocument(ErpApplicationDocument doc) {
        documents.add(doc);
        doc.setApplication(this);
    }

    public void addHistory(ErpApplicationStatusHistory history) {
        statusHistory.add(history);
        history.setApplication(this);
    }
}