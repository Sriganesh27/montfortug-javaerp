package com.montfort.erp.modules.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DatabaseMigration {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void runMigrations() {
        try {
            jdbcTemplate.execute("ALTER TABLE erp_students ADD COLUMN StudentID VARCHAR(50) NULL AFTER AdmissionNo");
            System.out.println("DatabaseMigration: Successfully added StudentID column to erp_students.");
        } catch (Exception e) {
            // Column already exists or other error, ignore
            System.out.println("DatabaseMigration: StudentID column already exists or could not be added.");
        }
    }
}
