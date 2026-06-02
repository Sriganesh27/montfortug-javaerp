package com.montfort.erp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StudentService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long getCurrentUserBranchId() {
        try {
            Long branchId = jdbcTemplate.queryForObject("SELECT MIN(branch_id) FROM erp_branches", Long.class);
            return branchId != null ? branchId : 1L;
        } catch (Exception e) {
            return 1L;
        }
    }

    @Transactional
    public String admitStudent(HttpServletRequest request, Map<String, String> formData) throws Exception {
        Long branchId = getCurrentUserBranchId();
        if (branchId == null) throw new Exception("Unauthorized: Branch ID not found");

        String name = formData.get("name");
        String surname = formData.get("surname");
        String dob = formData.get("dob");
        String gender = formData.get("gender");
        String rawClass = formData.get("class");
        String level = formData.get("level");
        String term = formData.get("term");
        
        if (name == null || name.isEmpty() || surname == null || surname.isEmpty() || rawClass == null || rawClass.isEmpty()) {
            throw new Exception("Name, Surname, and Class are required.");
        }

        Long adNo = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(AdmissionNo), 0) + 1 FROM erp_students WHERE branch_id = ?", Long.class, branchId);
        
        String admissionYearStr = formData.getOrDefault("admission_year", String.valueOf(java.time.Year.now().getValue()));
        int admissionYear = Integer.parseInt(admissionYearStr);
        String academicYear = admissionYear + "/" + (admissionYear + 1);

        String classGrade = rawClass.replace(" ", "").replace(".", "").replace("PP", "N").toUpperCase();

        jdbcTemplate.update(
            "INSERT INTO erp_students (AdmissionNo, branch_id, AdmissionYear, Name, MiddleName, Surname, DateOfBirth, Gender, Nationality, HouseNo, Street, Village, Town, District, State, Country, PostalCode) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            adNo, branchId, admissionYear, name, formData.get("middle_name"), surname, dob, gender, formData.getOrDefault("nationality", "Ugandan"),
            formData.get("house_no"), formData.get("street"), formData.get("village"), formData.get("town"), formData.get("district"), formData.get("state"), formData.getOrDefault("country", "Uganda"), formData.get("postal_code")
        );

        String moreInfo = formData.get("more_info");
        if (moreInfo == null) moreInfo = "";
        
        String scholarshipStatus = formData.get("scholarship");
        String scholarshipDetails = formData.get("scholarship_details");
        if (scholarshipStatus != null && !scholarshipStatus.equals("None") && !scholarshipStatus.equals("No")) {
            moreInfo += "\n[Scholarship: " + scholarshipStatus + (scholarshipDetails != null && !scholarshipDetails.isEmpty() ? " - " + scholarshipDetails : "") + "]";
        }

        jdbcTemplate.update(
            "INSERT INTO erp_parents (AdmissionNo, branch_id, father_name, father_contact, father_email, father_age, father_occupation, father_education, mother_name, mother_contact, mother_email, mother_age, mother_occupation, mother_education, guardian_name, guardian_relation, guardian_contact, guardian_email, guardian_age, guardian_occupation, guardian_education, guardian_address, MoreInformation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            adNo, branchId, formData.get("father_name"), formData.get("father_contact"), formData.get("father_email"), formData.get("father_age"), formData.get("father_occupation"), formData.get("father_education"),
            formData.get("mother_name"), formData.get("mother_contact"), formData.get("mother_email"), formData.get("mother_age"), formData.get("mother_occupation"), formData.get("mother_education"),
            formData.get("guardian_name"), formData.get("guardian_relation"), formData.get("guardian_contact"), formData.get("guardian_email"), formData.get("guardian_age"), formData.get("guardian_occupation"), formData.get("guardian_education"), formData.get("guardian_address"),
            moreInfo.trim()
        );

        String[] subjectNames = request.getParameterValues("subject_name[]");
        String[] subjectMarks = request.getParameterValues("subject_mark[]");
        String[] subjectGrades = request.getParameterValues("subject_grade[]");
        List<Map<String, String>> subjects = new ArrayList<>();
        if (subjectNames != null) {
            for (int i = 0; i < subjectNames.length; i++) {
                String sName = subjectNames[i];
                if (sName == null || sName.trim().isEmpty()) continue;
                String sMark = (subjectMarks != null && i < subjectMarks.length) ? subjectMarks[i] : "";
                String sGrade = (subjectGrades != null && i < subjectGrades.length) ? subjectGrades[i] : "";
                subjects.add(Map.of("name", sName, "mark", sMark, "grade", sGrade));
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        String subjectMarksJson = mapper.writeValueAsString(subjects);

        jdbcTemplate.update(
            "INSERT INTO erp_academichistory (AdmissionNo, branch_id, FormerSchool, FormerSchoolCode, FormerSchoolLIN, PLEIndexNumber, PLEAggregate, UCEIndexNumber, UCEResult, SubjectMarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            adNo, branchId, formData.get("former_school"), formData.get("former_school_code"), formData.get("former_school_lin"), formData.get("ple_index"), formData.get("ple_agg"), formData.get("uce_index"), formData.get("uce_result"), subjectMarksJson
        );

        jdbcTemplate.update(
            "INSERT INTO erp_enrollment (AdmissionNo, branch_id, AcademicYear, Term, Class, Level, Stream, Residence, EntryStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            adNo, branchId, academicYear, term, classGrade, level, formData.getOrDefault("stream", "A"), formData.get("residence"), formData.get("entry_status")
        );

        String branchSchoolCode = jdbcTemplate.queryForObject("SELECT school_code FROM erp_branches WHERE branch_id = ?", String.class, branchId);
        if (branchSchoolCode == null) branchSchoolCode = "U011";
        String yearShort = admissionYearStr.length() >= 2 ? admissionYearStr.substring(admissionYearStr.length() - 2) : admissionYearStr;
        String username = branchSchoolCode + "-" + yearShort + "-" + classGrade + "-" + String.format("%04d", adNo);
        String pwdHash = passwordEncoder.encode(surname + admissionYearStr);

        jdbcTemplate.update(
            "INSERT INTO erp_student_accounts (AdmissionNo, branch_id, username, password, is_active) VALUES (?, ?, ?, ?, ?)",
            adNo, branchId, username, pwdHash, 1
        );

        String linkAppIdStr = formData.get("link_app_id");
        if (linkAppIdStr != null && !linkAppIdStr.isEmpty()) {
            Long linkAppId = Long.parseLong(linkAppIdStr);
            jdbcTemplate.update("UPDATE erp_applications SET status = 'Admitted' WHERE app_id = ? AND branch_id = ?", linkAppId, branchId);

            try {
                // Fetch existing application data
                Map<String, Object> appData = jdbcTemplate.queryForMap(
                        "SELECT photo_path, prev_marks_doc FROM erp_applications WHERE app_id = ? AND branch_id = ?", 
                        linkAppId, branchId);
                
                String oldPhotoPath = (String) appData.get("photo_path");
                String oldPrevMarksDoc = (String) appData.get("prev_marks_doc");
                
                String safeBranchName = branchSchoolCode.replaceAll("[^A-Za-z0-9]", "_");
                String safeStudentName = (name + "_" + surname).replaceAll("[^A-Za-z0-9]", "_");
                String adNoFormatted = String.format("%04d", adNo);
                
                String baseStudentDir = System.getProperty("user.dir") + "/public/assets/uploads/students/" + adNoFormatted + "_" + safeStudentName + "/";
                java.io.File dir = new java.io.File(baseStudentDir);
                if (!dir.exists()) dir.mkdirs();

                // Migrate Photo
                if (oldPhotoPath != null && !oldPhotoPath.trim().isEmpty()) {
                    String cleanOldPhoto = oldPhotoPath;
                    if (cleanOldPhoto.contains("/assets/uploads/")) {
                        cleanOldPhoto = cleanOldPhoto.substring(cleanOldPhoto.indexOf("/assets/uploads/"));
                    }
                    java.io.File sourcePhoto = new java.io.File(System.getProperty("user.dir") + "/public" + cleanOldPhoto);
                    if (!sourcePhoto.exists()) {
                        sourcePhoto = new java.io.File(System.getProperty("user.dir") + "/src/main/resources/static" + cleanOldPhoto);
                    }
                    
                    if (sourcePhoto.exists()) {
                        String ext = sourcePhoto.getName().contains(".") ? sourcePhoto.getName().substring(sourcePhoto.getName().lastIndexOf('.')) : ".jpg";
                        String newPhotoName = safeStudentName + "_" + adNo + ext;
                        java.nio.file.Files.copy(sourcePhoto.toPath(), java.nio.file.Paths.get(baseStudentDir, newPhotoName), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        
                        String newDbPhotoPath = "/assets/uploads/students/" + adNoFormatted + "_" + safeStudentName + "/" + newPhotoName;
                        jdbcTemplate.update("UPDATE erp_students SET PhotoPath = ? WHERE AdmissionNo = ? AND branch_id = ?", newDbPhotoPath, adNo, branchId);
                    }
                }

                // Migrate Previous Marks Document
                if (oldPrevMarksDoc != null && !oldPrevMarksDoc.trim().isEmpty()) {
                    String cleanOldDoc = oldPrevMarksDoc;
                    if (cleanOldDoc.contains("/assets/uploads/")) {
                        cleanOldDoc = cleanOldDoc.substring(cleanOldDoc.indexOf("/assets/uploads/"));
                    }
                    java.io.File sourceDoc = new java.io.File(System.getProperty("user.dir") + "/public" + cleanOldDoc);
                    if (!sourceDoc.exists()) {
                        sourceDoc = new java.io.File(System.getProperty("user.dir") + "/src/main/resources/static" + cleanOldDoc);
                    }
                    
                    if (sourceDoc.exists()) {
                        String ext = sourceDoc.getName().contains(".") ? sourceDoc.getName().substring(sourceDoc.getName().lastIndexOf('.')) : ".pdf";
                        String newDocName = safeStudentName + "_" + adNo + "_prev_marks" + ext;
                        java.nio.file.Files.copy(sourceDoc.toPath(), java.nio.file.Paths.get(baseStudentDir, newDocName), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        
                        String newDbDocPath = "/assets/uploads/students/" + adNoFormatted + "_" + safeStudentName + "/" + newDocName;
                        try {
                            jdbcTemplate.update("UPDATE erp_academichistory SET PreviousMarksDoc = ? WHERE AdmissionNo = ? AND branch_id = ?", newDbDocPath, adNo, branchId);
                        } catch (Exception e) {
                            try {
                                jdbcTemplate.execute("ALTER TABLE erp_academichistory ADD COLUMN PreviousMarksDoc VARCHAR(255)");
                                jdbcTemplate.update("UPDATE erp_academichistory SET PreviousMarksDoc = ? WHERE AdmissionNo = ? AND branch_id = ?", newDbDocPath, adNo, branchId);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "Student Admitted Successfully!<br><strong>Adm-no: " + String.format("%04d", adNo) + "</strong><br><strong> Student ID: " + username + "</strong>";
    }
}
