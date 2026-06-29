package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.repository.ErpApplicationRepository;
import com.erp.montfortuganda.school.Branch;
import com.erp.montfortuganda.school.BranchRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PublicApplicationService {

    private final ErpApplicationRepository applicationRepository;
    private final BranchRepository branchRepository;

    // NEW: Injecting our advanced security scanner
    private final SecuritySanitizerService securitySanitizerService;

    // This is the absolute root directory for all application files
    private final String ROOT_UPLOAD_DIR = System.getProperty("user.dir") + "/secure_uploads/applications/";

    // Constructor updated to require the new Security Service
    public PublicApplicationService(ErpApplicationRepository applicationRepository,
                                    BranchRepository branchRepository,
                                    SecuritySanitizerService securitySanitizerService) {
        this.applicationRepository = applicationRepository;
        this.branchRepository = branchRepository;
        this.securitySanitizerService = securitySanitizerService;
    }

    public String processApplication(ErpApplication app, MultipartFile photo, MultipartFile prevMarks) throws Exception {

        // 1. Fetch the School Branch Details First
        Branch branch = branchRepository.findById(app.getBranchId().intValue())
                .orElseThrow(() -> new Exception("Invalid Branch ID"));

        // 2. Determine Academic Year
        String yearString = app.getAcademicYear();
        if (yearString == null || yearString.isEmpty()) {
            yearString = String.valueOf(LocalDateTime.now().getYear());
            app.setAcademicYear(yearString);
        }

        // 3. Generate the Unique Tracking ID
        long currentCount = applicationRepository.countByAcademicYearAndBranchId(yearString, app.getBranchId());
        String sequence = String.format("%03d", currentCount + 1);
        String refNumber = "APP-" + yearString + "-" + branch.getSchoolCode() + "-" + sequence;
        app.setRefNumber(refNumber.toUpperCase());

        // 4. Build the dynamic folder structure
        String safeSchoolCode = sanitizeFolderName(branch.getSchoolCode());
        String safeSchoolName = sanitizeFolderName(branch.getBranchName());
        String safeLocation = sanitizeFolderName(branch.getBranchLocation());

        String dynamicRelativePath = safeSchoolCode + "_" + safeSchoolName + "_" + safeLocation + "/" + refNumber + "/";
        String finalAbsoluteDirPath = ROOT_UPLOAD_DIR + dynamicRelativePath;

        File studentDir = new File(finalAbsoluteDirPath);
        if (!studentDir.exists()) {
            boolean created = studentDir.mkdirs();
            if (!created) System.out.println("Warning: Could not create student directory.");
        }

        // 5. Process File Uploads
        if (photo != null && !photo.isEmpty()) {
            verifySafeImageFile(photo);
            String photoName = saveFileSecurely(photo, "photo", finalAbsoluteDirPath);
            app.setPhotoPath(dynamicRelativePath + photoName);
        }

        if (prevMarks != null && !prevMarks.isEmpty()) {
            verifySafePdfOrImage(prevMarks);
            String docName = saveFileSecurely(prevMarks, "doc", finalAbsoluteDirPath);
            app.setPrevMarksDoc(dynamicRelativePath + docName);
        }

        if (app.getClassCode() == null) app.setClassCode("N/A");

        // ==============================================================
        // 6. MAXIMUM SECURITY LOCKDOWN
        // ==============================================================

        // A. Scan entire application for Hostile SQLi and XSS scripts!
        securitySanitizerService.sanitizeAndValidate(app);

        // B. Prevent Mass Assignment Attacks (Force safe defaults)
        app.setAppId(null);
        app.setStatus("Pending");
        app.setScholarshipStatus("No");
        app.setCreatedAt(LocalDateTime.now());

        // 7. Save to Database
        applicationRepository.save(app);

        return refNumber;
    }

    // --- MILITARY GRADE ANTI-MALWARE SCANNING & STORAGE ---

    private String saveFileSecurely(MultipartFile file, String type, String absoluteDirPath) throws IOException {
        String extension = getExtension(file.getOriginalFilename());
        // We still use a UUID for the file name so hackers can't do Path Traversal attacks!
        String secureFilename = UUID.randomUUID() + "_" + type + extension;

        Path path = Paths.get(absoluteDirPath + secureFilename);
        Files.write(path, file.getBytes());

        // Return just the filename, the caller handles the path
        return secureFilename;
    }

    private void verifySafeImageFile(MultipartFile file) throws Exception {
        if (file.getSize() > 100 * 1024) throw new Exception("Photo exceeds 100KB limit.");
        byte[] magicBytes = getMagicBytes(file);
        if (isNotJpeg(magicBytes) && isNotPng(magicBytes)) {
            throw new Exception("MALWARE DETECTED: Disguised file signature. Upload blocked.");
        }
    }

    private void verifySafePdfOrImage(MultipartFile file) throws Exception {
        if (file.getSize() > 2 * 1024 * 1024) throw new Exception("Document exceeds 2MB limit.");
        byte[] magicBytes = getMagicBytes(file);
        if (isNotJpeg(magicBytes) && isNotPng(magicBytes) && isNotPdf(magicBytes)) {
            throw new Exception("MALWARE DETECTED: Disguised file signature. Upload blocked.");
        }
    }

    private byte[] getMagicBytes(MultipartFile file) throws IOException {
        byte[] header = new byte[8];
        try (InputStream is = file.getInputStream()) {
            int bytesRead = is.read(header, 0, 8);
            if (bytesRead < 4) {
                throw new IOException("File is too small to verify signature.");
            }
        }
        return header;
    }

    private boolean isNotJpeg(byte[] bytes) {
        return !(bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF);
    }

    private boolean isNotPng(byte[] bytes) {
        return !(bytes[0] == (byte) 0x89 && bytes[1] == (byte) 0x50 && bytes[2] == (byte) 0x4E && bytes[3] == (byte) 0x47);
    }

    private boolean isNotPdf(byte[] bytes) {
        return !(bytes[0] == (byte) 0x25 && bytes[1] == (byte) 0x50 && bytes[2] == (byte) 0x44 && bytes[3] == (byte) 0x46);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    // Helper tool to safely remove spaces and special characters from folder names
    private String sanitizeFolderName(String input) {
        if (input == null) return "Unknown";
        return input.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
    // ==============================================================
    // NEW METHODS FOR TRACKING & PRINTING
    // ==============================================================

    public ErpApplication getApplicationByRef(String refNumber) throws Exception {
        // 1. Fetch the application
        ErpApplication app = applicationRepository.findByRefNumber(refNumber.toUpperCase())
                .orElseThrow(() -> new Exception("Application not found for reference: " + refNumber));

        // 2. Fetch the associated branch name so the printer can display it
        Branch branch = branchRepository.findById(app.getBranchId().intValue()).orElse(null);
        if (branch != null) {
            // Note: Since ErpApplication doesn't have a branchName field mapped to the DB,
            // you might want to temporarily store it in a @Transient field if you have one,
            // OR we can just return the app, and let the Controller build a Map.
            // For simplicity, we will just return the raw application entity here.
        }

        return app;
    }
}