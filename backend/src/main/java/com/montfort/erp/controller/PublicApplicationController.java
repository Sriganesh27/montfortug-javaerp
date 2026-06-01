package com.montfort.erp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/api/public")
public class PublicApplicationController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${erp.uploads.applications.dir:uploads/applications/}")
    private String uploadDir;

    @GetMapping("/branches")
    public ResponseEntity<?> getActiveBranches() {
        try {
            List<Map<String, Object>> branches = jdbcTemplate.queryForList(
                    "SELECT branch_id, branch_name, school_code, branch_type FROM erp_branches");
            return ResponseEntity.ok(Map.of("success", true, "data", branches));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Database error: " + e.getMessage()));
        }
    }

    @PostMapping("/applications/status")
    public ResponseEntity<?> checkStatus(@RequestParam("ref_number") String refNumber) {
        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    "SELECT a.student_name, a.applied_class, a.scholarship_status, a.status, a.ref_number " +
                    "FROM erp_applications a WHERE a.ref_number = ?", refNumber);
            
            if (!results.isEmpty()) {
                return ResponseEntity.ok(Map.of("success", true, "data", results.get(0)));
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "Application not found for the given reference number."));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Server Error: " + e.getMessage()));
        }
    }

    @GetMapping("/applications/details")
    public ResponseEntity<?> getApplicationDetails(@RequestParam("ref") String refNumber) {
        try {
            String sql = "SELECT a.*, b.branch_name FROM erp_applications a " +
                         "LEFT JOIN erp_branches b ON a.branch_id = b.branch_id " +
                         "WHERE a.ref_number = ?";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, refNumber);
            
            if (!results.isEmpty()) {
                return ResponseEntity.ok(Map.of("success", true, "data", results.get(0)));
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "Application not found."));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Server Error: " + e.getMessage()));
        }
    }

    @PostMapping("/applications/submit")
    public ResponseEntity<?> submitApplication(
            HttpServletRequest request,
            @RequestParam Map<String, String> formData,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "prev_marks_doc", required = false) MultipartFile prevMarksDoc) {
        
        try {
            int branchId = Integer.parseInt(formData.getOrDefault("branch_id", "0"));
            String year = formData.getOrDefault("admission_year", String.valueOf(java.time.Year.now().getValue()));
            
            // Parse class_selection which is in format "code|name"
            String classSelection = formData.getOrDefault("class_selection", "");
            String[] classParts = classSelection.split("\\|");
            String classCode = classParts.length > 0 ? classParts[0] : "";
            String className = classParts.length > 1 ? classParts[1] : "";

            // Generate Reference Number
            String schoolCode = jdbcTemplate.queryForObject(
                    "SELECT school_code FROM erp_branches WHERE branch_id = ?", String.class, branchId);
            if (schoolCode == null) schoolCode = "U000";
            
            String prefix = schoolCode + "-" + year.substring(Math.max(0, year.length() - 2)) + "-";
            Integer total = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) as total FROM erp_applications WHERE branch_id = ? AND academic_year = ?", 
                    Integer.class, branchId, year);
            if (total == null) total = 0;
            String sequence = String.format("%03d", total + 1);
            String refNumber = prefix + sequence;

            // Handle File Uploads
            String safeBranchName = jdbcTemplate.queryForObject(
                    "SELECT branch_name FROM erp_branches WHERE branch_id = ?", String.class, branchId);
            if (safeBranchName == null) safeBranchName = "General_School";
            safeBranchName = safeBranchName.replaceAll("[^A-Za-z0-9]", "_");

            String baseDir = System.getProperty("user.dir") + "/public/assets/uploads/applications/" + safeBranchName + "/" + classCode + "/" + refNumber;
            File dir = new File(baseDir);
            if (!dir.exists()) dir.mkdirs();

            String photoPath = null;
            if (photo != null && !photo.isEmpty()) {
                if (photo.getSize() > 51200) {
                    return ResponseEntity.ok(Map.of("success", false, "message", "The student photograph must be 50KB or smaller."));
                }
                String originalFilename = photo.getOriginalFilename();
                String ext = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf('.')) : ".jpg";
                String fileName = refNumber + ext;
                Path path = Paths.get(baseDir, fileName);
                Files.copy(photo.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                photoPath = "/assets/uploads/applications/" + safeBranchName + "/" + classCode + "/" + refNumber + "/" + fileName;
            }

            String prevMarksPath = null;
            if (prevMarksDoc != null && !prevMarksDoc.isEmpty()) {
                String originalFilename = prevMarksDoc.getOriginalFilename();
                String ext = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf('.')) : ".pdf";
                String fileName = refNumber + "_prev_marks" + ext;
                Path path = Paths.get(baseDir, fileName);
                Files.copy(prevMarksDoc.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                prevMarksPath = "/assets/uploads/applications/" + safeBranchName + "/" + classCode + "/" + refNumber + "/" + fileName;
            }

            // Extract subject arrays from HttpServletRequest
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

            // Insert into Database
            String sql = "INSERT INTO erp_applications (" +
                    "ref_number, branch_id, academic_year, term, date_of_registration, " +
                    "student_name, middle_name, student_surname, gender, dob, nationality, " +
                    "address_postal, address_house, address_street, address_village, address_district, address_state, address_country, " +
                    "father_name, father_age, father_contact, father_email, father_occupation, father_education, " +
                    "mother_name, mother_age, mother_contact, mother_email, mother_occupation, mother_education, " +
                    "guardian_name, guardian_relation, guardian_age, guardian_contact, guardian_email, guardian_occupation, guardian_education, " +
                    "level, applied_class, class_code, " +
                    "former_school, former_school_code, former_school_lin, prev_marks_doc, " +
                    "ple_score, ple_ref, uce_score, uce_ref, subject_marks, more_info, photo_path) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            jdbcTemplate.update(sql,
                    refNumber, branchId, year, formData.get("term"), formData.get("reg_date"),
                    formData.get("name"), formData.get("middle_name"), formData.get("surname"), formData.get("gender"), formData.get("dob"), formData.get("nationality"),
                    formData.get("postal"), formData.get("house"), formData.get("street"), formData.get("village"), formData.get("district"), formData.get("state"), "Uganda",
                    formData.get("f_name"), parseInteger(formData.get("f_age")), formData.get("f_con"), formData.get("f_email"), formData.get("f_occ"), formData.get("f_edu"),
                    formData.get("m_name"), parseInteger(formData.get("m_age")), formData.get("m_con"), formData.get("m_email"), formData.get("m_occ"), formData.get("m_edu"),
                    formData.get("g_name"), formData.get("g_rel"), parseInteger(formData.get("g_age")), formData.get("g_con"), formData.get("g_email"), formData.get("g_occ"), formData.get("g_edu"),
                    formData.get("level"), className, classCode,
                    formData.get("former_school"), formData.get("former_school_code"), formData.get("former_school_lin"), prevMarksPath,
                    formData.get("ple_score"), formData.get("ple_ref"), formData.get("uce_score"), formData.get("uce_ref"), subjectMarksJson, formData.get("more_info"), photoPath
            );

            return ResponseEntity.ok(Map.of("success", true, "ref_number", refNumber));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("success", false, "message", "Server Error: " + e.getMessage()));
        }
    }

    private Integer parseInteger(String val) {
        try {
            if (val != null && !val.trim().isEmpty()) {
                return Integer.parseInt(val.trim());
            }
        } catch (NumberFormatException e) {
            // ignore
        }
        return null;
    }
}
