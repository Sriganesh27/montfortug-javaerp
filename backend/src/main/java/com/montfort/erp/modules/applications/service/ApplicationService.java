package com.montfort.erp.modules.applications.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ApplicationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Helper to get branch ID (mocked for now)
    public Long getCurrentUserBranchId() {
        try {
            Long branchId = jdbcTemplate.queryForObject("SELECT MIN(branch_id) FROM erp_branches", Long.class);
            return branchId != null ? branchId : 1L;
        } catch (Exception e) {
            return 1L;
        }
    }

    public List<Map<String, Object>> getBranchInfo() {
        Long branchId = getCurrentUserBranchId();
        String sql = "SELECT branch_id, branch_name, branch_type FROM erp_branches WHERE branch_id = ?";
        return jdbcTemplate.queryForList(sql, branchId);
    }

    public List<Map<String, Object>> getAllBranches() {
        String sql = "SELECT branch_id, branch_name, branch_type FROM erp_branches WHERE status = 'Active' OR status IS NULL";
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            // Fallback if status column doesn't exist
            return jdbcTemplate.queryForList("SELECT branch_id, branch_name, branch_type FROM erp_branches");
        }
    }

    public List<Map<String, Object>> fetchApplications(String status, String search, String appliedLevel, String appliedClass, String scholarship, String academicYear) {
        Long branchId = getCurrentUserBranchId();
        if (branchId == null) return List.of();

        StringBuilder sql = new StringBuilder("SELECT app_id, ref_number, student_name, student_surname, applied_class, status, scholarship_status, created_at FROM erp_applications WHERE 1=1 ");
        List<Object> args = new ArrayList<>();
        
        if (status != null && !"All".equalsIgnoreCase(status)) {
            sql.append(" AND status = ? ");
            args.add(status);
        }
        
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (student_name LIKE ? OR student_surname LIKE ? OR ref_number LIKE ?) ");
            String searchParam = "%" + search.trim() + "%";
            args.add(searchParam);
            args.add(searchParam);
            args.add(searchParam);
        }
        
        if (appliedClass != null && !"All".equalsIgnoreCase(appliedClass) && !appliedClass.trim().isEmpty()) {
            sql.append(" AND class_code = ? ");
            args.add(appliedClass);
        } else if (appliedLevel != null && !appliedLevel.trim().isEmpty()) {
            if ("Nursery".equalsIgnoreCase(appliedLevel)) {
                sql.append(" AND class_code IN ('N1', 'N2', 'N3') ");
            } else if ("Primary".equalsIgnoreCase(appliedLevel)) {
                sql.append(" AND class_code IN ('P1', 'P2', 'P3', 'P4', 'P5', 'P6', 'P7') ");
            } else if ("Secondary".equalsIgnoreCase(appliedLevel)) {
                sql.append(" AND class_code IN ('S1', 'S2', 'S3', 'S4', 'S5', 'S6') ");
            }
        }
        
        if (scholarship != null && !"All".equalsIgnoreCase(scholarship) && !scholarship.trim().isEmpty()) {
            sql.append(" AND scholarship_status = ? ");
            args.add(scholarship);
        }
        
        if (academicYear != null && !"All".equalsIgnoreCase(academicYear) && !academicYear.trim().isEmpty()) {
            sql.append(" AND academic_year = ? ");
            args.add(academicYear);
        }
        
        sql.append(" ORDER BY created_at DESC");

        return jdbcTemplate.queryForList(sql.toString(), args.toArray());
    }

    public List<Map<String, Object>> fetchSingleApplication(Long id) {
        Long branchId = getCurrentUserBranchId();
        if (branchId == null) return List.of();

        String sql = "SELECT * FROM erp_applications WHERE app_id = ? AND branch_id = ?";
        return jdbcTemplate.queryForList(sql, id, branchId);
    }

    public Map<String, Object> fetchApplicationStatusByRef(String refNumber) {
        String sql = "SELECT ref_number, student_name, applied_class, scholarship_status, status FROM erp_applications WHERE ref_number = ?";
        try {
            return jdbcTemplate.queryForMap(sql, refNumber);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Map<String, Object> fetchApplicationDetailsByRef(String refNumber) {
        String sql = "SELECT a.*, b.branch_name FROM erp_applications a LEFT JOIN erp_branches b ON a.branch_id = b.branch_id WHERE a.ref_number = ?";
        try {
            return jdbcTemplate.queryForMap(sql, refNumber);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    public boolean updateStatus(Long appId, String status) {
        Long branchId = getCurrentUserBranchId();
        if (branchId == null) return false;

        String sql = "UPDATE erp_applications SET status = ? WHERE app_id = ? AND branch_id = ?";
        return jdbcTemplate.update(sql, status, appId, branchId) > 0;
    }

    public boolean updateScholarship(Long appId, String scholarship) {
        Long branchId = getCurrentUserBranchId();
        if (branchId == null) return false;

        String sql = "UPDATE erp_applications SET scholarship_status = ? WHERE app_id = ? AND branch_id = ?";
        return jdbcTemplate.update(sql, scholarship, appId, branchId) > 0;
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

    public String submitApplication(Map<String, String> formData, MultipartFile photo, MultipartFile prevMarksDoc, HttpServletRequest request) throws Exception {
        int branchId = Integer.parseInt(formData.getOrDefault("branch_id", "0"));
        String year = formData.getOrDefault("admission_year", String.valueOf(java.time.Year.now().getValue()));
        
        String classSelection = formData.getOrDefault("class_selection", "");
        String[] classParts = classSelection.split("\\|");
        String classCode = classParts.length > 0 ? classParts[0] : "";
        String className = classParts.length > 1 ? classParts[1] : "";

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
                throw new Exception("The student photograph must be 50KB or smaller.");
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
            if (prevMarksDoc.getSize() > 5 * 1024 * 1024) {
                throw new Exception("The previous marks document must be 5MB or smaller.");
            }
            String originalFilename = prevMarksDoc.getOriginalFilename();
            String ext = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf('.')) : ".pdf";
            String fileName = refNumber + "_prev_marks" + ext;
            Path path = Paths.get(baseDir, fileName);
            Files.copy(prevMarksDoc.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            prevMarksPath = "/assets/uploads/applications/" + safeBranchName + "/" + classCode + "/" + refNumber + "/" + fileName;
        }

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

        String sql = "INSERT INTO erp_applications (" +
                "ref_number, branch_id, academic_year, term, date_of_registration, " +
                "student_name, middle_name, student_surname, gender, dob, nationality, " +
                "address_postal, address_house, address_street, address_village, address_district, address_state, address_country, " +
                "father_name, father_age, father_contact, father_email, father_occupation, father_education, " +
                "mother_name, mother_age, mother_contact, mother_email, mother_occupation, mother_education, " +
                "guardian_name, guardian_relation, guardian_age, guardian_contact, guardian_email, guardian_occupation, guardian_education, " +
                "level, applied_class, class_code, " +
                "former_school, former_school_code, former_school_lin, prev_marks_doc, " +
                "ple_score, ple_ref, uce_score, uce_ref, subject_marks, more_info, photo_path, scholarship_status, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                refNumber, branchId, year, formData.get("term"), formData.get("reg_date"),
                formData.get("name"), formData.get("middle_name"), formData.get("surname"), formData.get("gender"), formData.get("dob"), formData.get("nationality"),
                formData.get("postal"), formData.get("house"), formData.get("street"), formData.get("village"), formData.get("district"), formData.get("state"), "Uganda",
                formData.get("f_name"), parseInteger(formData.get("f_age")), formData.get("f_con"), formData.get("f_email"), formData.get("f_occ"), formData.get("f_edu"),
                formData.get("m_name"), parseInteger(formData.get("m_age")), formData.get("m_con"), formData.get("m_email"), formData.get("m_occ"), formData.get("m_edu"),
                formData.get("g_name"), formData.get("g_rel"), parseInteger(formData.get("g_age")), formData.get("g_con"), formData.get("g_email"), formData.get("g_occ"), formData.get("g_edu"),
                formData.get("level"), className, classCode,
                formData.get("former_school"), formData.get("former_school_code"), formData.get("former_school_lin"), prevMarksPath,
                formData.get("ple_score"), formData.get("ple_ref"), formData.get("uce_score"), formData.get("uce_ref"), subjectMarksJson, formData.get("more_info"), photoPath, formData.getOrDefault("scholarship_status", "No"), "Pending"
        );

        return refNumber;
    }
}

