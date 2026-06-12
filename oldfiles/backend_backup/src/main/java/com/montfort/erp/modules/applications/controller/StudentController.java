package com.montfort.erp.modules.applications.controller;

import com.montfort.erp.modules.applications.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/students")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/admit")
    public ResponseEntity<?> admitStudent(HttpServletRequest request, @RequestParam Map<String, String> formData) {
        try {
            String message = studentService.admitStudent(request, formData);
            return ResponseEntity.ok(Map.of("success", true, "message", message));
            
        } catch (Exception e) {
            logger.error("Error admitting student", e);
            return ResponseEntity.ok(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }
}

