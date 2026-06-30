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

    @Column(name = "application_no", unique = true, nullable = false)
    private String applicationNo;

    // Use LAZY fetch to prevent N+1 queries. We map to the Branch entity.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "academic_year_id", nullable = false)
    private Long academicYearId;

    @Column(name = "branch_class_id", nullable = false)
    private Long branchClassId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name", nullable = false)
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

    @Column(name = "nationality")
    private String nationality = "Uganda";

    @Enumerated(EnumType.STRING)
    @Column(name = "admission_type")
    private AdmissionType admissionType = AdmissionType.NEW;

    @Column(name = "previous_school")
    private String previousSchool;

    @Column(name = "guardian_name")
    private String guardianName;

    @Column(name = "guardian_mobile")
    private String guardianMobile;

    @Column(name = "guardian_email")
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
    // --- LEGACY FIELDS TO SATISFY EXISTING MYSQL NOT NULL CONSTRAINTS ---
    @Column(name = "applied_class") private String appliedClass = "";
    @Column(name = "class_code") private String classCode = "";
    @Column(name = "level") private String level = "";
    @Column(name = "student_name") private String studentName = "";
    @Column(name = "student_surname") private String studentSurname = "";
    @Column(name = "dob") private String dobLegacy = "";
    @Column(name = "ref_number") private String refNumberLegacy = java.util.UUID.randomUUID().toString();
    @Column(name = "academic_year") private String academicYearLegacy = "";
    @Column(name = "term") private String term = "";
    @Column(name = "photo_path", columnDefinition = "TEXT") private String photoPath = "";

    @Column(name = "address_country") private String addressCountry = "";
    @Column(name = "address_district") private String addressDistrict = "";
    @Column(name = "address_house") private String addressHouse = "";
    @Column(name = "address_postal") private String addressPostal = "";
    @Column(name = "address_state") private String addressState = "";
    @Column(name = "address_street") private String addressStreet = "";
    @Column(name = "address_village") private String addressVillage = "";
    @Column(name = "date_of_registration") private String dateOfRegistration = "";

    @Column(name = "father_age") private Integer fatherAge = 0;
    @Column(name = "father_contact") private String fatherContact = "";
    @Column(name = "father_education") private String fatherEducation = "";
    @Column(name = "father_email") private String fatherEmail = "";
    @Column(name = "father_name") private String fatherName = "";
    @Column(name = "father_occupation") private String fatherOccupation = "";

    @Column(name = "former_school") private String formerSchool = "";
    @Column(name = "former_school_code") private String formerSchoolCode = "";
    @Column(name = "former_school_lin") private String formerSchoolLin = "";

    @Column(name = "guardian_age") private Integer guardianAge = 0;
    @Column(name = "guardian_contact") private String guardianContact = "";
    @Column(name = "guardian_education") private String guardianEducation = "";
    @Column(name = "guardian_location") private String guardianLocation = "";
    @Column(name = "guardian_occupation") private String guardianOccupation = "";
    @Column(name = "guardian_relation") private String guardianRelation = "";

    @Column(name = "more_info", columnDefinition = "TEXT") private String moreInfo = "";

    @Column(name = "mother_age") private Integer motherAge = 0;
    @Column(name = "mother_contact") private String motherContact = "";
    @Column(name = "mother_education") private String motherEducation = "";
    @Column(name = "mother_email") private String motherEmail = "";
    @Column(name = "mother_name") private String motherName = "";
    @Column(name = "mother_occupation") private String motherOccupation = "";

    @Column(name = "ple_ref") private String pleRef = "";
    @Column(name = "ple_score") private Double pleScore;
    @Column(name = "prev_marks_doc", columnDefinition = "TEXT") private String prevMarksDoc = "";
    @Column(name = "scholarship_status") private String scholarshipStatus = "";
    @Column(name = "subject_marks", columnDefinition = "TEXT") private String subjectMarks = "";
    @Column(name = "uce_ref") private String uceRef = "";
    @Column(name = "uce_score") private Double uceScore;
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
