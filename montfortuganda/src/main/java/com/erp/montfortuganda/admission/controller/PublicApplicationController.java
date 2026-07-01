package com.erp.montfortuganda.admission.controller;

import com.erp.montfortuganda.admission.dto.ApplicationCreateDTO;
import com.erp.montfortuganda.admission.dto.ApplicationResponseDTO;
import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.repository.ErpApplicationRepository;
import com.erp.montfortuganda.admission.service.PublicApplicationService;

import com.erp.montfortuganda.school.Branch;
import com.erp.montfortuganda.school.BranchLevel;
import com.erp.montfortuganda.school.Level;
import com.erp.montfortuganda.school.SchoolClass;
import com.erp.montfortuganda.school.BranchRepository;
import com.erp.montfortuganda.school.LevelRepository;
import com.erp.montfortuganda.school.SchoolClassRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PublicApplicationController {

    private final PublicApplicationService applicationService;
    private final ErpApplicationRepository applicationRepository;
    private final BranchRepository branchRepository;
    private final LevelRepository levelRepository;
    private final SchoolClassRepository classRepository;

    @GetMapping("/public/branches")
    public ResponseEntity<Map<String, Object>> getPublicBranches() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> branchList = new ArrayList<>();
            for (Branch b : branchRepository.findAll()) {
                Map<String, Object> branchMap = new HashMap<>();
                branchMap.put("branchId", b.getBranchId());
                branchMap.put("branchName", b.getBranchName());
                branchMap.put("branchLocation", b.getBranchLocation());
                branchMap.put("schoolCode", b.getSchoolCode());
                branchMap.put("branchLevels", extractBranchLevelsList(b.getBranchLevels()));
                branchList.add(branchMap);
            }
            response.put("success", true);
            response.put("data", branchList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private List<Map<String, Object>> extractBranchLevelsList(List<BranchLevel> branchLevels) {
        List<Map<String, Object>> branchLevelsList = new ArrayList<>();
        if (branchLevels != null) {
            for (BranchLevel bl : branchLevels) {
                Map<String, Object> blMap = new HashMap<>();
                Map<String, Object> levelMap = new HashMap<>();
                if (bl.getLevel() != null) {
                    levelMap.put("levelId", bl.getLevel().getLevelId());
                    levelMap.put("levelName", bl.getLevel().getLevelName());
                }
                blMap.put("level", levelMap);
                branchLevelsList.add(blMap);
            }
        }
        return branchLevelsList;
    }

    @GetMapping("/public/levels")
    public ResponseEntity<Map<String, Object>> getPublicLevels() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> levelList = new ArrayList<>();
            for (Level lvl : levelRepository.findAll()) {
                Map<String, Object> levelMap = new HashMap<>();
                levelMap.put("levelId", lvl.getLevelId());
                levelMap.put("levelName", lvl.getLevelName());
                levelList.add(levelMap);
            }
            response.put("success", true);
            response.put("data", levelList);
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
            List<Map<String, Object>> classList = new ArrayList<>();
            for (SchoolClass sc : classRepository.findAll()) {
                Map<String, Object> classMap = new HashMap<>();
                classMap.put("classId", sc.getClassId());
                classMap.put("classCode", sc.getClassCode());
                classMap.put("className", sc.getClassName());
                if (sc.getLevel() != null) {
                    classMap.put("levelId", sc.getLevel().getLevelId());
                }
                classList.add(classMap);
            }
            response.put("success", true);
            response.put("data", classList);
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
    public ResponseEntity<Map<String, Object>> checkApplicationStatus(@RequestParam("ref_number") String refNumber) {
        Map<String, Object> response = new HashMap<>();
        Optional<ErpApplication> appOpt = applicationRepository.findByApplicationNo(refNumber);

        if (appOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Application not found with reference number: " + refNumber);
            return ResponseEntity.ok(response);
        }

        ErpApplication app = appOpt.get();
        Map<String, Object> data = new HashMap<>();

        String fullName = app.getFirstName();
        if (app.getMiddleName() != null && !app.getMiddleName().trim().isEmpty()) fullName += " " + app.getMiddleName();
        if (app.getLastName() != null) fullName += " " + app.getLastName();
        data.put("student_name", fullName.trim());

        String appliedClass = String.valueOf(app.getBranchClassId());
        if (app.getBranchClassId() != null) {
            Optional<SchoolClass> sc = classRepository.findById(app.getBranchClassId().intValue());
            if (sc.isPresent()) {
                appliedClass = sc.get().getClassName();
            }
        }
        data.put("applied_class", appliedClass);
        data.put("status", app.getApplicationStatus().name());
        data.put("ref_number", app.getApplicationNo());
        data.put("scholarship_status", "None");

        response.put("success", true);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    // Public Endpoint: Details (For the Print Page)
    @GetMapping("/public/applications/details")
    public ResponseEntity<Map<String, Object>> getApplicationDetails(@RequestParam("ref") String refNumber) {
        Map<String, Object> response = new HashMap<>();
        Optional<ErpApplication> appOpt = applicationRepository.findByApplicationNo(refNumber);

        if (appOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Application not found.");
            return ResponseEntity.ok(response);
        }

        ErpApplication app = appOpt.get();
        Map<String, Object> data = new HashMap<>();

        // Campus & System info
        data.put("branch_name", app.getBranch() != null ? app.getBranch().getBranchName() : "");
        data.put("branch_location", app.getBranch() != null && app.getBranch().getBranchLocation() != null
                ? app.getBranch().getBranchLocation() : "Uganda");
        data.put("ref_number", app.getApplicationNo());
        data.put("date_of_registration", app.getCreatedAt() != null ? app.getCreatedAt().toLocalDate().toString() : "");
        data.put("status", app.getApplicationStatus().name());
        data.put("scholarship_status", "None");

        // Student Info
        data.put("student_name", app.getFirstName());
        data.put("middle_name", app.getMiddleName() != null ? app.getMiddleName() : "");
        data.put("student_surname", app.getLastName());
        data.put("gender", app.getGender() != null ? app.getGender().name() : "");
        data.put("dob", app.getDateOfBirth() != null ? app.getDateOfBirth().toString() : "");
        data.put("nationality", app.getNationality());

        // Class Info
        data.put("academic_year", String.valueOf(app.getAcademicYearId()));
        data.put("term", "Term I"); // Placeholder

        String appliedClass = "";
        String classCode = "";
        String level = "";
        if (app.getBranchClassId() != null) {
            Optional<SchoolClass> sc = classRepository.findById(app.getBranchClassId().intValue());
            if (sc.isPresent()) {
                appliedClass = sc.get().getClassName();
                classCode = sc.get().getClassCode();
                if (sc.get().getLevel() != null) {
                    level = sc.get().getLevel().getLevelName();
                }
            }
        }
        data.put("applied_class", appliedClass);
        data.put("class_code", classCode);
        data.put("level", level);
        data.put("primary_email", app.getPrimaryEmail());
        data.put("primary_mobile", app.getPrimaryMobile());

        // Family Info
        data.put("father_name", app.getFatherName());
        data.put("father_contact", app.getFatherContact());
        data.put("father_email", app.getFatherEmail());
        data.put("father_occupation", app.getFatherOccupation());
        data.put("father_education", app.getFatherEducation());
        data.put("father_age", app.getFatherAge());

        data.put("mother_name", app.getMotherName());
        data.put("mother_contact", app.getMotherContact());
        data.put("mother_email", app.getMotherEmail());
        data.put("mother_occupation", app.getMotherOccupation());
        data.put("mother_education", app.getMotherEducation());
        data.put("mother_age", app.getMotherAge());

        data.put("guardian_name", app.getGuardianName());
        data.put("guardian_relation", app.getGuardianRelation());
        data.put("guardian_contact", app.getGuardianMobile() != null ? app.getGuardianMobile() : app.getGuardianContact());
        data.put("guardian_email", app.getGuardianEmail());
        data.put("guardian_occupation", app.getGuardianOccupation());
        data.put("guardian_education", app.getGuardianEducation());
        data.put("guardian_age", app.getGuardianAge());
        data.put("guardian_location", app.getGuardianLocation());

        // Address
        data.put("address_house", app.getAddressHouse());
        data.put("address_street", app.getAddressStreet());
        data.put("address_village", app.getAddressVillage());
        data.put("address_district", app.getAddressDistrict());
        data.put("address_state", app.getAddressState());
        data.put("address_postal", app.getAddressPostal());

        // Academic Info
        data.put("former_school", app.getPreviousSchool() != null ? app.getPreviousSchool() : app.getFormerSchool());
        data.put("former_school_code", app.getFormerSchoolCode());
        data.put("former_school_lin", app.getFormerSchoolLin());
        data.put("ple_ref", app.getPleRef());
        data.put("ple_score", app.getPleScore());
        data.put("uce_ref", app.getUceRef());
        data.put("uce_score", app.getUceScore());
        data.put("subject_marks", app.getSubjectMarks());
        data.put("more_info", app.getMoreInfo());

        data.put("photo_path", app.getPhotoPath());

        response.put("success", true);
        response.put("data", data);
        return ResponseEntity.ok(response);
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