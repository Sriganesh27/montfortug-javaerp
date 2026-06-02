package com.montfort.erp.controller;

import com.montfort.erp.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/public/applications")
public class PublicApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitApplication(
            @RequestParam Map<String, String> formData,
            HttpServletRequest request,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "prev_marks_doc", required = false) MultipartFile prevMarksDoc) {
        
        try {
            String refNumber = applicationService.submitApplication(formData, photo, prevMarksDoc, request);
            return ResponseEntity.ok(Map.of("success", true, "ref_number", refNumber));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("success", false, "message", "Server Error: " + e.getMessage()));
        }
    }

    @PostMapping("/status")
    public ResponseEntity<?> checkStatus(@RequestParam("ref_number") String refNumber) {
        try {
            Map<String, Object> appData = applicationService.fetchApplicationStatusByRef(refNumber);
            
            if (appData != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", appData
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Application not found. Please check your Reference Number."
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Server Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/details")
    public ResponseEntity<?> getApplicationDetails(@RequestParam("ref") String refNumber) {
        try {
            Map<String, Object> appData = applicationService.fetchApplicationDetailsByRef(refNumber);
            if (appData != null) {
                return ResponseEntity.ok(Map.of("success", true, "data", appData));
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "Application not found."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("success", false, "message", "Server Error: " + e.getMessage()));
        }
    }
}
