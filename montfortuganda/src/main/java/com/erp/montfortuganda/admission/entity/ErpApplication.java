package com.erp.montfortuganda.admission.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "erp_applications")
public class ErpApplication {

    // ==========================================
    // 1. PRIMARY & SYSTEM IDENTIFIERS
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_id")
    private Long appId;

    @Column(name = "ref_number", length = 50, unique = true)
    private String refNumber;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "status", length = 50)
    private String status = "Pending";

    @Column(name = "scholarship_status", length = 50)
    private String scholarshipStatus = "No";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ==========================================
    // 2. ENROLLMENT DETAILS
    // ==========================================
    @Column(name = "academic_year", length = 10)
    private String academicYear;

    @Column(name = "term", length = 20)
    private String term;

    @Column(name = "date_of_registration", length = 50)
    private String dateOfRegistration;

    @Column(name = "level", length = 50, nullable = false)
    private String level;

    @Column(name = "applied_class", length = 50, nullable = false)
    private String appliedClass;

    @Column(name = "class_code", length = 10, nullable = false)
    private String classCode;

    // ==========================================
    // 3. STUDENT PERSONAL DETAILS
    // ==========================================
    @Column(name = "student_name", length = 100, nullable = false)
    private String studentName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "student_surname", length = 100, nullable = false)
    private String studentSurname;

    @Column(name = "gender", length = 10, nullable = false)
    private String gender;

    @Column(name = "dob", length = 20)
    private String dob;

    @Column(name = "nationality", length = 100)
    private String nationality = "Uganda";

    @Column(name = "photo_path", length = 255)
    private String photoPath;

    // ==========================================
    // 4. STUDENT ADDRESS
    // ==========================================
    @Column(name = "address_country", length = 100)
    private String addressCountry = "Uganda";

    @Column(name = "address_state", length = 100)
    private String addressState;

    @Column(name = "address_district", length = 100)
    private String addressDistrict;

    @Column(name = "address_village", length = 100)
    private String addressVillage;

    @Column(name = "address_street", length = 100)
    private String addressStreet;

    @Column(name = "address_house", length = 100)
    private String addressHouse;

    @Column(name = "address_postal", length = 20)
    private String addressPostal;

    // ==========================================
    // 5. FATHER'S DETAILS
    // ==========================================
    @Column(name = "father_name", length = 100)
    private String fatherName;

    @Column(name = "father_age")
    private Integer fatherAge;

    @Column(name = "father_contact", length = 50)
    private String fatherContact;

    @Column(name = "father_email", length = 100)
    private String fatherEmail;

    @Column(name = "father_occupation", length = 100)
    private String fatherOccupation;

    @Column(name = "father_education", length = 100)
    private String fatherEducation;

    // ==========================================
    // 6. MOTHER'S DETAILS
    // ==========================================
    @Column(name = "mother_name", length = 100)
    private String motherName;

    @Column(name = "mother_age")
    private Integer motherAge;

    @Column(name = "mother_contact", length = 50)
    private String motherContact;

    @Column(name = "mother_email", length = 100)
    private String motherEmail;

    @Column(name = "mother_occupation", length = 100)
    private String motherOccupation;

    @Column(name = "mother_education", length = 100)
    private String motherEducation;

    // ==========================================
    // 7. GUARDIAN'S DETAILS
    // ==========================================
    @Column(name = "guardian_name", length = 100)
    private String guardianName;

    @Column(name = "guardian_relation", length = 50)
    private String guardianRelation;

    @Column(name = "guardian_age")
    private Integer guardianAge;

    @Column(name = "guardian_contact", length = 50)
    private String guardianContact;

    @Column(name = "guardian_email", length = 100)
    private String guardianEmail;

    @Column(name = "guardian_occupation", length = 100)
    private String guardianOccupation;

    @Column(name = "guardian_education", length = 100)
    private String guardianEducation;

    @Column(name = "guardian_location", length = 255)
    private String guardianLocation;
    // ==========================================
    // 8. ACADEMIC HISTORY
    // ==========================================
    @Column(name = "former_school", length = 255)
    private String formerSchool;

    @Column(name = "former_school_code", length = 50)
    private String formerSchoolCode;

    @Column(name = "former_school_lin", length = 50)
    private String formerSchoolLin;

    @Column(name = "ple_score")
    private Integer pleScore;

    @Column(name = "ple_ref", length = 50)
    private String pleRef;

    @Column(name = "uce_score")
    private Integer uceScore;

    @Column(name = "uce_ref", length = 50)
    private String uceRef;

    @Column(name = "subject_marks", columnDefinition = "LONGTEXT")
    private String subjectMarks;

    @Column(name = "prev_marks_doc", length = 255)
    private String prevMarksDoc;

    // ==========================================
    // 9. MISCELLANEOUS
    // ==========================================
    @Column(name = "more_info", columnDefinition = "TEXT")
    private String moreInfo;
}