package com.montfort.erp.controller;

import com.montfort.erp.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping("/admit")
    public ResponseEntity<?> admitStudent(HttpServletRequest request, @RequestParam Map<String, String> formData) {
        try {
            String message = studentService.admitStudent(request, formData);
            return ResponseEntity.ok(Map.of("success", true, "message", message));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }
}
