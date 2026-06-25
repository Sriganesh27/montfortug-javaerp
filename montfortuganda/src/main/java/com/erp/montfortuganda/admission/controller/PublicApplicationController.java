package com.erp.montfortuganda.admission.controller;

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
}