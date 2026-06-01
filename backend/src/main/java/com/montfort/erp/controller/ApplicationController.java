package com.montfort.erp.controller;

import com.montfort.erp.entity.Application;
import com.montfort.erp.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/application")
public class ApplicationController {

    @Autowired
    private ApplicationRepository applicationRepository;

    @PostMapping("/submit")
    public ResponseEntity<?> submitApplication(
            @ModelAttribute Application application,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "prev_marks_doc", required = false) MultipartFile prevMarksDoc
    ) {
        try {
            // Generate Reference Number
            long count = applicationRepository.countByBranchIdAndAcademicYear(
                application.getBranchId(), 
                application.getAcademicYear()
            );
            String refNumber = String.format("APP-%s-%03d", application.getAcademicYear(), count + 1);
            application.setRefNumber(refNumber);
            application.setDateOfRegistration(LocalDate.now());

            // Handle file uploads (Mocked path for now)
            if (photo != null && !photo.isEmpty()) {
                application.setPhotoPath("/uploads/" + refNumber + "_photo.jpg");
            }
            if (prevMarksDoc != null && !prevMarksDoc.isEmpty()) {
                application.setPrevMarksDoc("/uploads/" + refNumber + "_marks.pdf");
            }

            applicationRepository.save(application);
            
            return ResponseEntity.ok().body("{\"success\": true, \"ref_number\": \"" + refNumber + "\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
        }
    }
}
