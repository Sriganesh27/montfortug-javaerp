package com.montfort.erp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "erp_applications")
@Data
@NoArgsConstructor
public class Application {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ref_number", nullable = false, unique = true)
    private String refNumber;
    
    @Column(name = "branch_id", nullable = false)
    private Long branchId;
    
    @Column(name = "academic_year")
    private String academicYear;
    
    private String term;
    
    @Column(name = "date_of_registration")
    private LocalDate dateOfRegistration;
    
    @Column(name = "student_name")
    private String studentName;
    
    @Column(name = "middle_name")
    private String middleName;
    
    @Column(name = "student_surname")
    private String studentSurname;
    
    private String gender;
    private String dob;
    private String nationality;
    
    @Column(name = "address_postal") private String addressPostal;
    @Column(name = "address_house") private String addressHouse;
    @Column(name = "address_street") private String addressStreet;
    @Column(name = "address_village") private String addressVillage;
    @Column(name = "address_district") private String addressDistrict;
    @Column(name = "address_state") private String addressState;
    @Column(name = "address_country") private String addressCountry;
    
    @Column(name = "father_name") private String fatherName;
    @Column(name = "father_age") private String fatherAge;
    @Column(name = "father_contact") private String fatherContact;
    @Column(name = "father_email") private String fatherEmail;
    @Column(name = "father_occupation") private String fatherOccupation;
    @Column(name = "father_education") private String fatherEducation;
    
    @Column(name = "mother_name") private String motherName;
    @Column(name = "mother_age") private String motherAge;
    @Column(name = "mother_contact") private String motherContact;
    @Column(name = "mother_email") private String motherEmail;
    @Column(name = "mother_occupation") private String motherOccupation;
    @Column(name = "mother_education") private String motherEducation;
    
    @Column(name = "guardian_name") private String guardianName;
    @Column(name = "guardian_relation") private String guardianRelation;
    @Column(name = "guardian_age") private String guardianAge;
    @Column(name = "guardian_contact") private String guardianContact;
    @Column(name = "guardian_email") private String guardianEmail;
    @Column(name = "guardian_occupation") private String guardianOccupation;
    @Column(name = "guardian_education") private String guardianEducation;
    
    private String level;
    
    @Column(name = "applied_class") private String appliedClass;
    @Column(name = "class_code") private String classCode;
    
    @Column(name = "former_school") private String formerSchool;
    @Column(name = "former_school_code") private String formerSchoolCode;
    @Column(name = "former_school_lin") private String formerSchoolLin;
    @Column(name = "prev_marks_doc") private String prevMarksDoc;
    
    @Column(name = "ple_score") private String pleScore;
    @Column(name = "ple_ref") private String pleRef;
    @Column(name = "uce_score") private String uceScore;
    @Column(name = "uce_ref") private String uceRef;
    
    @Column(name = "subject_marks", columnDefinition = "TEXT") 
    private String subjectMarks;
    
    @Column(name = "more_info", columnDefinition = "TEXT") 
    private String moreInfo;
    
    @Column(name = "photo_path") 
    private String photoPath;
}
