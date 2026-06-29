package com.erp.montfortuganda.admission.controller;
import com.erp.montfortuganda.school.Branch;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.service.PublicApplicationService;
import com.erp.montfortuganda.admission.service.RateLimitingService;
import com.erp.montfortuganda.school.BranchRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

// MAXIMUM CORS LOCKDOWN: Only accept form submissions originating from these specific websites!
@CrossOrigin(origins = {"https://montfort.ug", "http://localhost:8080"})
@RestController
@RequestMapping("/api/public")
public class PublicApplicationController {

    private final PublicApplicationService applicationService;
    private final BranchRepository branchRepository;
    private final RateLimitingService rateLimitingService;

    public PublicApplicationController(PublicApplicationService applicationService,
                                       BranchRepository branchRepository,
                                       RateLimitingService rateLimitingService) {
        this.applicationService = applicationService;
        this.branchRepository = branchRepository;
        this.rateLimitingService = rateLimitingService;
    }

    @GetMapping("/branches")
    public ResponseEntity<Map<String, Object>> getPublicBranches() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", branchRepository.findAll());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/applications/submit")
    public ResponseEntity<Map<String, Object>> submitApplication(
            HttpServletRequest request,
            @ModelAttribute ErpApplication app,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "prevMarks", required = false) MultipartFile prevMarks,
            // THE INVISIBLE HONEYPOT TRAP!
            @RequestParam(value = "fax_number", required = false) String honeypot) {

        Map<String, Object> response = new HashMap<>();

        try {
            // SECURITY CHECK 1: The Honeypot Trap
            // Real parents will never see 'fax_number'. If it's filled out, it's 100% a spam bot!
            if (honeypot != null && !honeypot.trim().isEmpty()) {
                System.err.println("SECURITY ALERT: Bot detected via honeypot. IP: " + request.getRemoteAddr());
                throw new Exception("Automated bot submission detected and blocked.");
            }

            // SECURITY CHECK 2: Rate Limiting (1 application per IP every 10 minutes)
            String clientIp = request.getRemoteAddr();
            rateLimitingService.checkRateLimit(clientIp);

            // SECURITY CHECK 3: Process securely with XSS and Mass Assignment protections
            String refNumber = applicationService.processApplication(app, photo, prevMarks);

            response.put("success", true);
            response.put("message", "Application submitted successfully.");
            response.put("ref_number", refNumber);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Application Submission Failed: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    @PostMapping("/applications/status")
    public ResponseEntity<Map<String, Object>> trackApplicationStatus(
            @RequestParam("ref_number") String refNumber) {

        Map<String, Object> response = new HashMap<>();

        try {
            ErpApplication app = applicationService.getApplicationByRef(refNumber);

            // Map the exact fields expected by status.js
            Map<String, Object> statusData = new HashMap<>();
            statusData.put("status", app.getStatus());
            statusData.put("ref_number", app.getRefNumber());
            statusData.put("student_name", app.getStudentName() + " " + (app.getMiddleName() != null ? app.getMiddleName() : "") + " " + app.getStudentSurname());
            statusData.put("applied_class", app.getAppliedClass());
            statusData.put("scholarship_status", app.getScholarshipStatus());

            response.put("success", true);
            response.put("data", statusData);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    // --- THIS REMAINS UNCHANGED (Uses GET and expects 'ref') ---
    @GetMapping("/applications/details")
    public ResponseEntity<Map<String, Object>> getApplicationDetails(@RequestParam("ref") String refNumber) {

        Map<String, Object> response = new HashMap<>();

        try {
            ErpApplication app = applicationService.getApplicationByRef(refNumber);

            // Map the entity to a dictionary to inject the Branch Name dynamically
            Map<String, Object> details = new HashMap<>();
            details.put("ref_number", app.getRefNumber());
            details.put("date_of_registration", app.getDateOfRegistration());
            details.put("status", app.getStatus());
            details.put("scholarship_status", app.getScholarshipStatus());

            // Student Info
            details.put("student_name", app.getStudentName());
            details.put("middle_name", app.getMiddleName());
            details.put("student_surname", app.getStudentSurname());
            details.put("gender", app.getGender());
            details.put("dob", app.getDob());
            details.put("nationality", app.getNationality());
            details.put("photo_path", app.getPhotoPath());

            // Enrollment
            details.put("academic_year", app.getAcademicYear());
            details.put("term", app.getTerm());
            details.put("applied_class", app.getAppliedClass());
            details.put("class_code", app.getClassCode());
            details.put("level", app.getLevel());

            // Parents
            details.put("father_name", app.getFatherName());
            details.put("father_contact", app.getFatherContact());
            details.put("father_email", app.getFatherEmail());
            details.put("father_occupation", app.getFatherOccupation());
            details.put("father_education", app.getFatherEducation());
            details.put("father_age", app.getFatherAge());

            details.put("mother_name", app.getMotherName());
            details.put("mother_contact", app.getMotherContact());
            details.put("mother_email", app.getMotherEmail());
            details.put("mother_occupation", app.getMotherOccupation());
            details.put("mother_education", app.getMotherEducation());
            details.put("mother_age", app.getMotherAge());

            // Guardian
            details.put("guardian_name", app.getGuardianName());
            details.put("guardian_relation", app.getGuardianRelation());
            details.put("guardian_contact", app.getGuardianContact());
            details.put("guardian_email", app.getGuardianEmail());
            details.put("guardian_occupation", app.getGuardianOccupation());
            details.put("guardian_education", app.getGuardianEducation());
            details.put("guardian_age", app.getGuardianAge());
            details.put("guardian_location", app.getGuardianLocation());

            // Address
            details.put("address_country", app.getAddressCountry());
            details.put("address_state", app.getAddressState());
            details.put("address_district", app.getAddressDistrict());
            details.put("address_village", app.getAddressVillage());
            details.put("address_street", app.getAddressStreet());
            details.put("address_house", app.getAddressHouse());
            details.put("address_postal", app.getAddressPostal());

            // Academics
            details.put("former_school", app.getFormerSchool());
            details.put("former_school_code", app.getFormerSchoolCode());
            details.put("former_school_lin", app.getFormerSchoolLin());
            details.put("ple_score", app.getPleScore());
            details.put("ple_ref", app.getPleRef());
            details.put("uce_score", app.getUceScore());
            details.put("uce_ref", app.getUceRef());
            details.put("subject_marks", app.getSubjectMarks());

            details.put("more_info", app.getMoreInfo());

            // Fetch Branch Name manually!
            branchRepository.findById(app.getBranchId().intValue()).ifPresent(branch -> {
                details.put("branch_name", branch.getBranchName());
                details.put("branch_location", branch.getBranchLocation());
            });
            response.put("success", true);
            response.put("data", details);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}