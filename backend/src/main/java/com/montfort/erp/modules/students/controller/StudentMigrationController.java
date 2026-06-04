package com.montfort.erp.modules.students.controller;

import com.montfort.erp.modules.students.service.StudentMigrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/students/migrate")
public class StudentMigrationController {

    private final StudentMigrationService studentMigrationService;

    public StudentMigrationController(StudentMigrationService studentMigrationService) {
        this.studentMigrationService = studentMigrationService;
    }

    @PostMapping("/execute")
    public ResponseEntity<?> executeMigration(@RequestBody Map<String, Object> payload) {
        try {
            List<Integer> studentIds = (List<Integer>) payload.get("studentIds");
            String targetYear = (String) payload.get("targetYear");
            String targetTerm = (String) payload.get("targetTerm");
            String targetLevel = (String) payload.get("targetLevel");
            String targetClass = (String) payload.get("targetClass");
            String targetStream = (String) payload.get("targetStream");

            if (studentIds == null || studentIds.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "No students selected for migration."));
            }

            int migratedCount = studentMigrationService.migrateStudents(studentIds, targetYear, targetTerm, targetLevel, targetClass, targetStream);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Successfully migrated " + migratedCount + " student(s).",
                    "migratedCount", migratedCount
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
