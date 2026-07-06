package com.erp.montfortuganda.admission.controller;

import com.erp.montfortuganda.admission.dto.ApplicationCreateDTO;
import com.erp.montfortuganda.admission.dto.ApplicationResponseDTO;
import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.service.PublicApplicationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PublicApplicationController {

    // -------------------------------------------------------------------------
    // ARCHITECTURE FIX: Notice how clean this is now!
    // ZERO direct database Repositories are injected here.
    // The Controller only talks to the Service.
    // -------------------------------------------------------------------------
    private final PublicApplicationService applicationService;

    @GetMapping("/public/branches")
    public ResponseEntity<Map<String, Object>> getPublicBranches() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("success", true);
            response.put("data", applicationService.getPublicBranches());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/public/levels")
    public ResponseEntity<Map<String, Object>> getPublicLevels() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("success", true);
            response.put("data", applicationService.getPublicLevels());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/public/classes")
    public ResponseEntity<Map<String, Object>> getPublicClasses() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("success", true);
            response.put("data", applicationService.getPublicClasses());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Public Endpoint: Submission
    @PostMapping("/public/applications/submit")
    public ResponseEntity<ApplicationResponseDTO> submitApplication(@Valid @RequestBody ApplicationCreateDTO dto) {
        ApplicationResponseDTO response = applicationService.submitApplication(dto);
        return ResponseEntity.ok(response);
    }

    // Public Endpoint: Document Upload
    @PostMapping("/public/applications/{refNumber}/upload")
    public ResponseEntity<Map<String, Object>> uploadApplicationFiles(
            @PathVariable String refNumber,
            @RequestParam(value = "photo", required = false) org.springframework.web.multipart.MultipartFile photo,
            @RequestParam(value = "documents", required = false) List<org.springframework.web.multipart.MultipartFile> documents) {
        Map<String, Object> response = new HashMap<>();
        try {
            applicationService.uploadApplicationFiles(refNumber, photo, documents);
            response.put("success", true);
            response.put("message", "Files uploaded successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "File upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Public Endpoint: Check Status (Compact for the Status Page)
    @PostMapping("/public/applications/status")
    public ResponseEntity<Map<String, Object>> checkApplicationStatus(
            @RequestParam("ref_number") String refNumber,
            @RequestParam("dob") String dob,
            jakarta.servlet.http.HttpServletRequest request) {

        // 1. Fetch data from Service Layer
        Map<String, Object> response = applicationService.verifyAndGetStatus(refNumber, dob);

        if ((Boolean) response.get("success")) {
            Long appId = (Long) response.remove("internal_id"); // Strip internal ID

            // 2. Session Fixation Protection (Modern Servlet 3.1+ Approach)
            jakarta.servlet.http.HttpSession session = request.getSession(true);
            request.changeSessionId();

            // 3. Set the secure VerifiedApplicationSession object
            com.erp.montfortuganda.admission.dto.VerifiedApplicationSession verifiedSession =
                    new com.erp.montfortuganda.admission.dto.VerifiedApplicationSession(refNumber, appId, 10);

            session.setAttribute("VERIFIED_APPLICATION", verifiedSession);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/applications/details")
    public ResponseEntity<Map<String, Object>> getApplicationDetails(jakarta.servlet.http.HttpServletRequest request) {

        // 1. Check Session Authorization
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("VERIFIED_APPLICATION") == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Session Expired. Please track again."));
        }

        com.erp.montfortuganda.admission.dto.VerifiedApplicationSession verifiedSession =
                (com.erp.montfortuganda.admission.dto.VerifiedApplicationSession) session.getAttribute("VERIFIED_APPLICATION");

        if (!verifiedSession.isValid()) {
            session.invalidate();
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Session Expired. Please track again."));
        }

        // 2. Add Cache-Control Headers to protect PII
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setCacheControl("no-store, no-cache, must-revalidate, max-age=0");
        headers.setPragma("no-cache");
        headers.setExpires(0);

        Map<String, Object> response = applicationService.getApplicationDetails(verifiedSession.getApplicationId());

        return ResponseEntity.ok().headers(headers).body(response);
    }

    // Secured Endpoint: Approve/Reject workflows
    @PostMapping("/superadmin/applications/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long id,
            @RequestParam ErpApplication.ApplicationStatus status,
            @RequestParam(required = false) String remarks) {

        // Use the authenticated user's ID from SecurityContext in production
        Long currentUserId = 1L;
        applicationService.updateApplicationStatus(id, status, currentUserId, remarks);
        return ResponseEntity.ok("Status successfully updated to " + status);
    }
}