package com.montfort.erp.modules.students.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StudentManagementService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long getCurrentUserBranchId() {
        try {
            Long branchId = jdbcTemplate.queryForObject("SELECT MIN(branch_id) FROM erp_branches", Long.class);
            return branchId != null ? branchId : 1L;
        } catch (Exception e) {
            return 1L;
        }
    }

    // 1. Fetch Students for the Quick Edit Data Table
    public Map<String, Object> fetchQuickEdit(Map<String, String> params) {
        Long branchId = getCurrentUserBranchId();
        
        int page = Integer.parseInt(params.getOrDefault("page", "1"));
        int limit = Integer.parseInt(params.getOrDefault("limit", "50"));
        int offset = (page - 1) * limit;

        StringBuilder sql = new StringBuilder(
            "SELECT s.AdmissionNo, s.StudentID, s.Name, s.Surname, s.Gender, s.PhotoPath, e.Class, e.Term, e.Stream, e.Level, e.Residence, e.EntryStatus " +
            "FROM erp_students s LEFT JOIN erp_enrollment e ON s.AdmissionNo = e.AdmissionNo " +
            "WHERE s.branch_id = ? "
        );
        StringBuilder countSql = new StringBuilder(
            "SELECT COUNT(*) FROM erp_students s LEFT JOIN erp_enrollment e ON s.AdmissionNo = e.AdmissionNo " +
            "WHERE s.branch_id = ? "
        );

        List<Object> args = new ArrayList<>();
        args.add(branchId);

        String classFilter = params.get("class");
        if (classFilter != null && !classFilter.trim().isEmpty() && !classFilter.equals("All")) {
            sql.append(" AND e.Class = ? ");
            countSql.append(" AND e.Class = ? ");
            args.add(classFilter);
        }
        
        String streamFilter = params.get("stream");
        if (streamFilter != null && !streamFilter.trim().isEmpty() && !streamFilter.equals("All")) {
            sql.append(" AND e.Stream = ? ");
            countSql.append(" AND e.Stream = ? ");
            args.add(streamFilter);
        }

        String yearFilter = params.get("admission_year");
        if (yearFilter != null && !yearFilter.trim().isEmpty() && !yearFilter.equals("All")) {
            sql.append(" AND s.AdmissionYear = ? ");
            countSql.append(" AND s.AdmissionYear = ? ");
            args.add(yearFilter);
        }
        
        String levelFilter = params.get("level");
        if (levelFilter != null && !levelFilter.trim().isEmpty() && !levelFilter.equals("All")) {
            sql.append(" AND e.Level = ? ");
            countSql.append(" AND e.Level = ? ");
            args.add(levelFilter);
        }
        
        String genderFilter = params.get("gender");
        if (genderFilter != null && !genderFilter.trim().isEmpty() && !genderFilter.equals("All")) {
            sql.append(" AND s.Gender = ? ");
            countSql.append(" AND s.Gender = ? ");
            args.add(genderFilter);
        }
        
        String residenceFilter = params.get("residence");
        if (residenceFilter != null && !residenceFilter.trim().isEmpty() && !residenceFilter.equals("All")) {
            sql.append(" AND e.Residence = ? ");
            countSql.append(" AND e.Residence = ? ");
            args.add(residenceFilter);
        }
        
        String termFilter = params.get("term");
        if (termFilter != null && !termFilter.trim().isEmpty() && !termFilter.equals("All")) {
            sql.append(" AND e.Term = ? ");
            countSql.append(" AND e.Term = ? ");
            args.add(termFilter);
        }
        
        String searchId = params.get("search_id");
        if (searchId != null && !searchId.trim().isEmpty()) {
            sql.append(" AND (CAST(s.AdmissionNo AS CHAR) LIKE ? OR s.StudentID LIKE ?) ");
            countSql.append(" AND (CAST(s.AdmissionNo AS CHAR) LIKE ? OR s.StudentID LIKE ?) ");
            String idQuery = "%" + searchId.trim() + "%";
            args.add(idQuery);
            args.add(idQuery);
        }

        String searchName = params.get("search_name");
        if (searchName != null && !searchName.trim().isEmpty()) {
            sql.append(" AND (s.Name LIKE ? OR s.Surname LIKE ?) ");
            countSql.append(" AND (s.Name LIKE ? OR s.Surname LIKE ?) ");
            String nameQuery = "%" + searchName.trim() + "%";
            args.add(nameQuery);
            args.add(nameQuery);
        }
        
        sql.append(" ORDER BY s.AdmissionNo DESC LIMIT ? OFFSET ?");
        
        List<Object> queryArgs = new ArrayList<>(args);
        queryArgs.add(limit);
        queryArgs.add(offset);

        List<Map<String, Object>> data = jdbcTemplate.queryForList(sql.toString(), queryArgs.toArray());
        Long total = jdbcTemplate.queryForObject(countSql.toString(), Long.class, args.toArray());
        
        int totalPages = (int) Math.ceil((double) total / limit);

        return Map.of("success", true, "data", data, "totalPages", totalPages, "currentPage", page);
    }

    // 2. Global Search for Students
    public Map<String, Object> searchGlobal(String query) {
        Long branchId = getCurrentUserBranchId();
        String q = "%" + (query != null ? query.trim() : "") + "%";
        
        String sql = "SELECT s.AdmissionNo, s.StudentID, s.Name, s.Surname, s.PhotoPath, e.Class, e.Term, e.Stream, p.father_name, p.mother_name, p.guardian_name, p.father_contact, p.mother_contact, p.guardian_contact, s.Village, s.Town, s.District " +
                     "FROM erp_students s " +
                     "LEFT JOIN erp_enrollment e ON s.AdmissionNo = e.AdmissionNo " +
                     "LEFT JOIN erp_parents p ON s.AdmissionNo = p.AdmissionNo " +
                     "WHERE s.branch_id = ? AND (s.Name LIKE ? OR s.Surname LIKE ? OR CAST(s.AdmissionNo AS CHAR) LIKE ? OR s.StudentID LIKE ?) " +
                     "LIMIT 20";
                     
        List<Map<String, Object>> data = jdbcTemplate.queryForList(sql, branchId, q, q, q, q);
        
        // Enhance data for frontend display
        for (Map<String, Object> row : data) {
            String parentDisplay = (String) row.get("father_name");
            if (parentDisplay == null || parentDisplay.trim().isEmpty()) parentDisplay = (String) row.get("mother_name");
            if (parentDisplay == null || parentDisplay.trim().isEmpty()) parentDisplay = (String) row.get("guardian_name");
            row.put("ParentDisplay", parentDisplay != null ? parentDisplay : "-");
            
            String contactDisplay = (String) row.get("father_contact");
            if (contactDisplay == null || contactDisplay.trim().isEmpty()) contactDisplay = (String) row.get("mother_contact");
            if (contactDisplay == null || contactDisplay.trim().isEmpty()) contactDisplay = (String) row.get("guardian_contact");
            row.put("ContactDisplay", contactDisplay != null ? contactDisplay : "-");
            
            String address = "";
            if (row.get("Village") != null && !row.get("Village").toString().isEmpty()) address += row.get("Village") + ", ";
            if (row.get("Town") != null && !row.get("Town").toString().isEmpty()) address += row.get("Town") + ", ";
            if (row.get("District") != null && !row.get("District").toString().isEmpty()) address += row.get("District");
            row.put("FormattedAddress", address.isEmpty() ? "-" : address);
        }
        
        return Map.of("success", true, "data", data);
    }

    // 3. Archive Student (Safe Archiving)
    public Map<String, Object> deleteStudent(Long admissionNo, String exitReason) {
        Long branchId = getCurrentUserBranchId();
        
        // Instead of hard-deleting, we update the EntryStatus
        int rows = jdbcTemplate.update("UPDATE erp_enrollment SET EntryStatus = ? WHERE AdmissionNo = ? AND branch_id = ?", exitReason, admissionNo, branchId);
        
        if (rows > 0) {
            return Map.of("success", true, "message", "Student securely archived as: " + exitReason);
        } else {
            return Map.of("success", false, "message", "Student not found or unauthorized.");
        }
    }
}
