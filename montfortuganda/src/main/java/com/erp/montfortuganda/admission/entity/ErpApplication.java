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
    // --- LEGACY FIELDS TO SATISFY EXISTING MYSQL NOT NULL CONSTRAINTS ---
    @Column(name = "applied_class", length = 100) private String appliedClass = "";
    @Column(name = "class_code", length = 50) private String classCode = "";
    @Column(name = "level", length = 50) private String level = "";
    @Column(name = "student_name", length = 100) private String studentName = "";
    @Column(name = "student_surname", length = 100) private String studentSurname = "";
    @Column(name = "dob", length = 50) private String dobLegacy = "";
    @Column(name = "ref_number", length = 50) private String refNumberLegacy = "";
    @Column(name = "academic_year", length = 50) private String academicYearLegacy = "";
    @Column(name = "term", length = 50) private String term = "";
    @Column(name = "photo_path", columnDefinition = "TEXT") private String photoPath = "";

    @Column(name = "address_country", length = 100) private String addressCountry = "";
    @Column(name = "address_district", length = 100) private String addressDistrict = "";
    @Column(name = "address_house", length = 100) private String addressHouse = "";
    @Column(name = "address_postal", length = 50) private String addressPostal = "";
    @Column(name = "address_state", length = 100) private String addressState = "";
    @Column(name = "address_street", length = 150) private String addressStreet = "";
    @Column(name = "address_village", length = 100) private String addressVillage = "";
    @Column(name = "date_of_registration", length = 50) private String dateOfRegistration = "";

    @Column(name = "father_age") private Integer fatherAge = 0;
    @Column(name = "father_contact", length = 50) private String fatherContact = "";
    @Column(name = "father_education", length = 100) private String fatherEducation = "";
    @Column(name = "father_email", length = 100) private String fatherEmail = "";
    @Column(name = "father_name", length = 100) private String fatherName = "";
    @Column(name = "father_occupation", length = 100) private String fatherOccupation = "";

    @Column(name = "former_school", length = 150) private String formerSchool = "";
    @Column(name = "former_school_code", length = 50) private String formerSchoolCode = "";
    @Column(name = "former_school_lin", length = 50) private String formerSchoolLin = "";

    @Column(name = "guardian_age") private Integer guardianAge = 0;
    @Column(name = "guardian_contact", length = 50) private String guardianContact = "";
    @Column(name = "guardian_education", length = 100) private String guardianEducation = "";
    @Column(name = "guardian_location", length = 100) private String guardianLocation = "";
    @Column(name = "guardian_occupation", length = 100) private String guardianOccupation = "";
    @Column(name = "guardian_relation", length = 50) private String guardianRelation = "";

    @Column(name = "more_info", columnDefinition = "TEXT") private String moreInfo = "";

    @Column(name = "mother_age") private Integer motherAge = 0;
    @Column(name = "mother_contact", length = 50) private String motherContact = "";
    @Column(name = "mother_education", length = 100) private String motherEducation = "";
    @Column(name = "mother_email", length = 100) private String motherEmail = "";
    @Column(name = "mother_name", length = 100) private String motherName = "";
    @Column(name = "mother_occupation", length = 100) private String motherOccupation = "";

    @Column(name = "ple_ref", length = 50) private String pleRef = "";
    @Column(name = "ple_score", length = 20) private Double pleScore;
    @Column(name = "prev_marks_doc", columnDefinition = "TEXT") private String prevMarksDoc = "";
    @Column(name = "scholarship_status", length = 50) private String scholarshipStatus = "";
    @Column(name = "subject_marks", columnDefinition = "TEXT") private String subjectMarks = "";
    @Column(name = "uce_ref", length = 50) private String uceRef = "";
    @Column(name = "uce_score", length = 20) private Double uceScore;
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