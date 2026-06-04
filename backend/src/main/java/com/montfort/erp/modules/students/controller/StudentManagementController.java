package com.montfort.erp.modules.students.controller;

import com.montfort.erp.modules.students.service.StudentManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/students")
public class StudentManagementController {

    private final StudentManagementService studentManagementService;

    public StudentManagementController(StudentManagementService studentManagementService) {
        this.studentManagementService = studentManagementService;
    }

    @GetMapping("/quick-edit")
    public ResponseEntity<?> fetchQuickEdit(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(studentManagementService.fetchQuickEdit(params));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchGlobal(@RequestParam("query") String query) {
        return ResponseEntity.ok(studentManagementService.searchGlobal(query));
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteStudent(@RequestParam("id") Long admissionNo) {
        return ResponseEntity.ok(studentManagementService.deleteStudent(admissionNo));
    }
}
