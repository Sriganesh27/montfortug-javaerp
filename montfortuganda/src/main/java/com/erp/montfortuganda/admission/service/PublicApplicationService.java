package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.dto.ApplicationCreateDTO;
import com.erp.montfortuganda.admission.dto.ApplicationResponseDTO;
import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.entity.ErpApplicationStatusHistory;
import com.erp.montfortuganda.admission.repository.ErpApplicationRepository;
import com.erp.montfortuganda.school.Branch;
import com.erp.montfortuganda.school.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PublicApplicationService {

    private final ErpApplicationRepository applicationRepository;
    private final BranchRepository branchRepository;

    @Transactional
    public ApplicationResponseDTO submitApplication(ApplicationCreateDTO dto) {

        Branch branch = branchRepository.findById(dto.getBranchId().intValue())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Branch ID"));

        String yearString = String.valueOf(java.time.LocalDateTime.now().getYear());
        long currentCount = applicationRepository.countByBranchAndYear(branch.getBranchId(), dto.getAcademicYearId());
        String sequence = String.format("%03d", currentCount + 1); // 3 digits as requested
        String applicationNo = "APP-" + yearString + "-" + branch.getSchoolCode() + "-" + sequence;

        ErpApplication app = new ErpApplication();
        app.setApplicationNo(applicationNo);
        app.setBranch(branch);
        app.setAcademicYearId(dto.getAcademicYearId());
        app.setBranchClassId(dto.getBranchClassId());

        app.setFirstName(dto.getFirstName());
        app.setMiddleName(dto.getMiddleName());
        app.setLastName(dto.getLastName());
        app.setGender(dto.getGender());
        app.setDateOfBirth(dto.getDateOfBirth());

        app.setReligionId(dto.getReligionId());
        app.setBloodGroupId(dto.getBloodGroupId());
        app.setCategoryId(dto.getCategoryId());

        app.setNationality(dto.getNationality());
        app.setAdmissionType(dto.getAdmissionType());
        app.setPreviousSchool(dto.getPreviousSchool());

        app.setGuardianName(dto.getGuardianName());
        app.setGuardianMobile(dto.getGuardianMobile());
        app.setGuardianEmail(dto.getGuardianEmail());

        app.setApplicationStatus(ErpApplication.ApplicationStatus.SUBMITTED);

        ErpApplicationStatusHistory history = new ErpApplicationStatusHistory();
        history.setNewStatus(ErpApplication.ApplicationStatus.SUBMITTED);
        history.setRemarks("Application submitted by user");
        app.addHistory(history);

        ErpApplication savedApp = applicationRepository.save(app);

        return mapToResponseDTO(savedApp);
    }

    @Transactional
    public void updateApplicationStatus(Long applicationId, ErpApplication.ApplicationStatus newStatus, Long userId, String remarks) {
        ErpApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        ErpApplication.ApplicationStatus oldStatus = app.getApplicationStatus();
        if (oldStatus == newStatus) return;

        app.setApplicationStatus(newStatus);
        app.setUpdatedBy(userId);
        app.setUpdatedAt(LocalDateTime.now());

        ErpApplicationStatusHistory history = new ErpApplicationStatusHistory();
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(userId);
        history.setRemarks(remarks);
        app.addHistory(history);

        applicationRepository.save(app);
    }

    @Transactional
    public void uploadApplicationFiles(String refNumber, org.springframework.web.multipart.MultipartFile photo, java.util.List<org.springframework.web.multipart.MultipartFile> documents) {
        ErpApplication app = applicationRepository.findByApplicationNo(refNumber)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        
        String uploadDir = "uploads/applications/" + refNumber + "/";
        try {
            java.nio.file.Path basePath = java.nio.file.Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!java.nio.file.Files.exists(basePath)) java.nio.file.Files.createDirectories(basePath);

            if (photo != null && !photo.isEmpty()) {
                String photoName = "photo_" + System.currentTimeMillis() + "_" + photo.getOriginalFilename().replaceAll("[^a-zA-Z0-9.\\-]", "_");
                java.nio.file.Path filePath = basePath.resolve(photoName).normalize();
                java.nio.file.Files.copy(photo.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                com.erp.montfortuganda.admission.entity.ErpApplicationDocument doc = new com.erp.montfortuganda.admission.entity.ErpApplicationDocument();
                doc.setApplication(app);
                doc.setDocumentType("PHOTO");
                doc.setOriginalFileName(photo.getOriginalFilename());
                doc.setStoredFileName(photoName);
                doc.setFilePath("/" + uploadDir + photoName);
                app.addDocument(doc);
            }
            
            if (documents != null) {
                for (org.springframework.web.multipart.MultipartFile file : documents) {
                    if (file != null && !file.isEmpty()) {
                        String docName = "doc_" + System.currentTimeMillis() + "_" + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.\\-]", "_");
                        java.nio.file.Path filePath = basePath.resolve(docName).normalize();
                        java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        
                        com.erp.montfortuganda.admission.entity.ErpApplicationDocument doc = new com.erp.montfortuganda.admission.entity.ErpApplicationDocument();
                        doc.setApplication(app);
                        doc.setDocumentType("DOCUMENT");
                        doc.setOriginalFileName(file.getOriginalFilename());
                        doc.setStoredFileName(docName);
                        doc.setFilePath("/" + uploadDir + docName);
                        app.addDocument(doc);
                    }
                }
            }
            
            applicationRepository.save(app);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload files: " + e.getMessage(), e);
        }
    }

    private ApplicationResponseDTO mapToResponseDTO(ErpApplication app) {
        ApplicationResponseDTO dto = new ApplicationResponseDTO();
        dto.setApplicationId(app.getApplicationId());
        dto.setApplicationNo(app.getApplicationNo());
        dto.setApplicationStatus(app.getApplicationStatus());
        dto.setBranchName(app.getBranch().getBranchName());
        dto.setFirstName(app.getFirstName());
        dto.setMiddleName(app.getMiddleName());
        dto.setLastName(app.getLastName());
        dto.setGender(app.getGender());
        dto.setCreatedAt(app.getCreatedAt());
        return dto;
    }
}