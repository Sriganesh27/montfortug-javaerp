package com.montfort.erp.modules.students.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class StudentMigrationService {

    private final JdbcTemplate jdbcTemplate;

    public StudentMigrationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public int migrateStudents(List<Integer> studentIds, String targetYear, String targetTerm, String targetLevel, String targetClass, String targetStream) {
        if (studentIds == null || studentIds.isEmpty()) return 0;
        
        // Use IN clause to update all selected student IDs
        String inSql = String.join(",", Collections.nCopies(studentIds.size(), "?"));
        
        // Base SQL string
        StringBuilder sql = new StringBuilder("UPDATE erp_enrollment SET ");
        sql.append("AcademicYear = ?, ");
        sql.append("Term = ?, ");
        
        if (targetLevel != null && !targetLevel.trim().isEmpty()) {
            sql.append("Level = '").append(targetLevel).append("', ");
        }
        if (targetClass != null && !targetClass.trim().isEmpty()) {
            sql.append("Class = '").append(targetClass).append("', ");
        }
        if (targetStream != null && !targetStream.trim().isEmpty()) {
            sql.append("Stream = '").append(targetStream).append("', ");
        }
        
        // Check if archiving
        if ("alumni".equalsIgnoreCase(targetLevel) || "alumni".equalsIgnoreCase(targetClass)) {
            sql.append("EntryStatus = 'Completed', ");
        }
        
        // Remove trailing comma
        sql.setLength(sql.length() - 2); 
        
        sql.append(" WHERE AdmissionNo IN (").append(inSql).append(")");

        // Prepare arguments
        Object[] args = new Object[2 + studentIds.size()];
        args[0] = targetYear;
        args[1] = targetTerm;
        
        for (int i = 0; i < studentIds.size(); i++) {
            args[2 + i] = studentIds.get(i);
        }

        return jdbcTemplate.update(sql.toString(), args);
    }
}
