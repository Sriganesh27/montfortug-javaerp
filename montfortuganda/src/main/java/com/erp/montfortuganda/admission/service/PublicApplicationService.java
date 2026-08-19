package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.dto.ApplicationCreateDTO;
import com.erp.montfortuganda.admission.dto.ApplicationResponseDTO;
import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.entity.ErpApplicationStatusHistory;
import com.erp.montfortuganda.admission.entity.ErpApplicationDocument;
import com.erp.montfortuganda.admission.repository.ErpApplicationRepository;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import com.erp.montfortuganda.school.entity.ErpAcademicTerm;
import com.erp.montfortuganda.school.entity.BranchLevel;
import com.erp.montfortuganda.school.entity.Level;
import com.erp.montfortuganda.school.entity.SchoolClass;
import com.erp.montfortuganda.school.repository.AcademicYearRepository;
import com.erp.montfortuganda.school.repository.AcademicTermRepository;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.school.repository.LevelRepository;
import com.erp.montfortuganda.school.repository.SchoolClassRepository;
import com.erp.montfortuganda.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PublicApplicationService {

    private final ErpApplicationRepository applicationRepository;
    private final BranchRepository branchRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AcademicTermRepository academicTermRepository;
    private final SchoolClassRepository classRepository;
    private final LevelRepository levelRepository;

    @Value("${erp.storage.location:uploads}")
    private String publicStorageLocation;

    @Autowired
    private EmailService emailService;

    @Transactional
    public ApplicationResponseDTO submitApplication(
            ApplicationCreateDTO dto
    ) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "Application details are required."
            );
        }

        if (
                dto.getBranchId() == null
                        || dto.getBranchId() <= 0
        ) {
            throw new IllegalArgumentException(
                    "A valid Branch is required."
            );
        }

        if (
                dto.getAcademicYearId() == null
                        || dto.getAcademicYearId() <= 0
        ) {
            throw new IllegalArgumentException(
                    "A valid Academic Year is required."
            );
        }

        if (
                dto.getJoiningTermId() == null
                        || dto.getJoiningTermId() <= 0
        ) {
            throw new IllegalArgumentException(
                    "A valid Admission Term is required."
            );
        }

        Integer branchId =
                dto.getBranchId().intValue();

        Branch branch =
                branchRepository.findById(
                                branchId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Selected Branch was not found."
                                )
                        );

        ErpAcademicYear academicYear =
                academicYearRepository
                        .findByAcademicYearIdAndBranchBranchIdAndActiveTrue(
                                dto.getAcademicYearId(),
                                branchId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Selected Academic Year is inactive "
                                                + "or does not belong to the "
                                                + "selected Branch."
                                )
                        );

        LocalDate today = LocalDate.now();

        if (!isAdmissionOpenForAcademicYear(
                academicYear,
                today
        )) {
            throw new IllegalArgumentException(
                    "Admissions are not open for the selected Academic Year."
            );
        }

        ErpAcademicTerm academicTerm =
                academicTermRepository
                        .findActiveByTermIdAndBranchId(
                                dto.getJoiningTermId(),
                                branchId
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Selected Admission Term is inactive "
                                                + "or does not belong to the "
                                                + "selected Branch."
                                )
                        );

        if (
                academicTerm.getAcademicYear() == null
                        || !academicYear.getAcademicYearId().equals(
                        academicTerm
                                .getAcademicYear()
                                .getAcademicYearId()
                )
        ) {
            throw new IllegalArgumentException(
                    "Selected Admission Term does not belong to the "
                            + "selected Academic Year."
            );
        }

        if (
                academicTerm.getStatus()
                        == ErpAcademicTerm.Status.CLOSED
        ) {
            throw new IllegalArgumentException(
                    "The selected Admission Term is closed."
            );
        }

        /*
         * Public application number format:
         *
         *   SCHOOL_CODE-YEAR-SEQUENCE
         *   Example: U021-2026-0001
         *
         * Sequence is branch + academic-year scoped and zero padded
         * to four digits.
         */
        String yearString =
                String.valueOf(
                        academicYear
                                .getStartDate()
                                .getYear()
                );

        long currentCount =
                applicationRepository
                        .countApplicationsByBranchAndAcademicYear(
                                branch.getBranchId(),
                                academicYear.getAcademicYearId()
                        );

        String sequence =
                String.format(
                        "%04d",
                        currentCount + 1
                );

        String applicationNo =
                branch.getSchoolCode()
                        + "-"
                        + yearString
                        + "-"
                        + sequence;

        /*
         * Defensive uniqueness check in case imported/historic rows make
         * the branch/year count differ from the highest existing sequence.
         */
        while (
                applicationRepository
                        .findByApplicationNo(
                                applicationNo
                        )
                        .isPresent()
        ) {
            currentCount++;

            sequence =
                    String.format(
                            "%04d",
                            currentCount + 1
                    );

            applicationNo =
                    branch.getSchoolCode()
                            + "-"
                            + yearString
                            + "-"
                            + sequence;
        }

        ErpApplication app = new ErpApplication();
        app.setApplicationNo(applicationNo);
        app.setBranch(branch);
        app.setAcademicYearId(
                academicYear.getAcademicYearId()
        );
        app.setBranchClassId(dto.getBranchClassId());
        app.setJoiningTermId(
                academicTerm.getTermId()
        );
        app.setTerm(
                academicTerm.getTermName()
        );

        app.setFirstName(dto.getFirstName());
        app.setMiddleName(dto.getMiddleName());
        app.setLastName(dto.getLastName());
        app.setGender(dto.getGender());
        app.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getDateOfRegistration() != null) {
            app.setDateOfRegistration(dto.getDateOfRegistration().toString());
        } else {
            app.setDateOfRegistration("");
        }

        app.setNationality(dto.getNationality());
        app.setAdmissionType(dto.getAdmissionType());
        app.setPreviousSchool(dto.getPreviousSchool());

        app.setGuardianName(dto.getGuardianName());
        app.setGuardianMobile(dto.getGuardianMobile());
        app.setGuardianEmail(dto.getGuardianEmail());

        // Map Extended Data Fields
        app.setAddressHouse(dto.getAddressHouse());
        app.setAddressStreet(dto.getAddressStreet());
        app.setAddressVillage(dto.getAddressVillage());
        app.setAddressDistrict(dto.getAddressDistrict());
        app.setAddressState(dto.getAddressState());
        app.setAddressPostal(dto.getAddressPostal());

        app.setFatherName(dto.getFatherName());
        app.setFatherAge(dto.getFatherAge() != null ? dto.getFatherAge() : 0);
        app.setFatherContact(dto.getFatherContact());
        app.setFatherEducation(dto.getFatherEducation());
        app.setFatherOccupation(dto.getFatherOccupation());
        app.setFatherEmail(dto.getFatherEmail());

        app.setMotherName(dto.getMotherName());
        app.setMotherAge(dto.getMotherAge() != null ? dto.getMotherAge() : 0);
        app.setMotherContact(dto.getMotherContact());
        app.setMotherEducation(dto.getMotherEducation());
        app.setMotherOccupation(dto.getMotherOccupation());
        app.setMotherEmail(dto.getMotherEmail());

        app.setGuardianAge(dto.getGuardianAge() != null ? dto.getGuardianAge() : 0);
        app.setGuardianEducation(dto.getGuardianEducation());
        app.setGuardianOccupation(dto.getGuardianOccupation());
        app.setGuardianRelation(dto.getGuardianRelation());
        app.setGuardianLocation(dto.getGuardianLocation());

        app.setFormerSchool(dto.getPreviousSchool());
        app.setFormerSchoolCode(dto.getFormerSchoolCode());
        app.setFormerSchoolLin(dto.getFormerSchoolLin());
        app.setPleRef(dto.getPleRef());
        app.setPleScore(dto.getPleScore());
        app.setUceRef(dto.getUceRef());
        app.setUceScore(dto.getUceScore());
        app.setSubjectMarks(dto.getSubjectMarks());
        app.setScholarshipStatus(dto.getScholarshipStatus());
        app.setMoreInfo(dto.getMoreInfo());
        app.setApplicationStatus(ErpApplication.ApplicationStatus.SUBMITTED);

        ErpApplicationStatusHistory history = new ErpApplicationStatusHistory();
        history.setNewStatus(ErpApplication.ApplicationStatus.SUBMITTED);
        history.setRemarks("Application submitted by user");
        app.addHistory(history);
        app.setPrimaryEmail(dto.getPrimaryEmail());
        app.setPrimaryMobile(dto.getPrimaryMobile());

        ErpApplication savedApp = applicationRepository.save(app);
        // Fire and forget the background email task
        emailService.sendApplicationReceipt(savedApp);
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
    public void uploadApplicationFiles(
            String refNumber,
            org.springframework.web.multipart.MultipartFile photo,
            java.util.List<org.springframework.web.multipart.MultipartFile> documents
    ) {

        // SECURITY GUARD: Reject path traversal characters in the input
        if (refNumber == null
                || refNumber.contains("..")
                || refNumber.contains("/")
                || refNumber.contains("\\")) {
            throw new SecurityException(
                    "Invalid reference number: Path traversal detected"
            );
        }

        ErpApplication app =
                applicationRepository.findByApplicationNo(refNumber)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Application not found"
                                )
                        );

        /*
         * Keep the logical/database path in the existing compatible format:
         * uploads/applications/<branch>/<application>/
         *
         * The physical storage root comes only from erp.storage.location.
         */
        String uploadDir =
                generateUploadDirectory(
                        app,
                        app.getApplicationNo()
                );

        try {
            java.io.File storageRoot =
                    new java.io.File(
                            publicStorageLocation
                    ).getCanonicalFile();

            if (!storageRoot.exists()
                    && !storageRoot.mkdirs()) {
                throw new java.io.IOException(
                        "Failed to initialize application upload storage."
                );
            }

            /*
             * uploadDir starts with "uploads/" for existing DB compatibility.
             * Strip only that logical prefix before resolving it against the
             * configured physical storage root.
             */
            String relativeUploadDir =
                    uploadDir.startsWith("uploads/")
                            ? uploadDir.substring(
                            "uploads/".length()
                    )
                            : uploadDir;

            java.io.File baseDirFile =
                    new java.io.File(
                            storageRoot,
                            relativeUploadDir
                    ).getCanonicalFile();

            String storageRootPath =
                    storageRoot.getCanonicalPath();

            String baseDirPath =
                    baseDirFile.getCanonicalPath();

            if (!baseDirPath.equals(storageRootPath)
                    && !baseDirPath.startsWith(
                    storageRootPath
                            + java.io.File.separator
            )) {
                throw new SecurityException(
                        "Invalid application upload directory"
                );
            }

            if (!baseDirFile.exists()
                    && !baseDirFile.mkdirs()) {
                throw new java.io.IOException(
                        "Failed to create directory for uploads. "
                                + "Check folder permissions."
                );
            }

            // -------------------------------------------------------------
            // APPLICANT PHOTO
            // -------------------------------------------------------------
            if (photo != null
                    && !photo.isEmpty()) {

                String photoName =
                        sanitizeFileName(
                                "photo",
                                photo.getOriginalFilename()
                        );

                java.io.File targetFile =
                        new java.io.File(
                                baseDirFile,
                                photoName
                        ).getCanonicalFile();

                String targetPath =
                        targetFile.getCanonicalPath();

                if (!targetPath.startsWith(
                        baseDirPath
                                + java.io.File.separator
                )) {
                    throw new SecurityException(
                            "Invalid file path: Path Traversal detected"
                    );
                }

                photo.transferTo(targetFile);

                ErpApplicationDocument doc =
                        new ErpApplicationDocument();

                doc.setApplication(app);
                doc.setDocumentType(
                        ErpApplicationDocument.DocumentType.PHOTO
                );
                doc.setFileSize(photo.getSize());
                doc.setContentType(photo.getContentType());
                doc.setOriginalFileName(
                        photo.getOriginalFilename()
                );
                doc.setStoredFileName(photoName);

                String photoDatabasePath =
                        "/" + uploadDir + photoName;

                doc.setFilePath(photoDatabasePath);
                app.addDocument(doc);
                app.setPhotoPath(photoDatabasePath);
            }

            // -------------------------------------------------------------
            // APPLICATION DOCUMENTS
            // -------------------------------------------------------------
            if (documents != null) {
                for (org.springframework.web.multipart.MultipartFile file
                        : documents) {

                    if (file == null
                            || file.isEmpty()) {
                        continue;
                    }

                    String docName =
                            sanitizeFileName(
                                    "doc",
                                    file.getOriginalFilename()
                            );

                    java.io.File targetFile =
                            new java.io.File(
                                    baseDirFile,
                                    docName
                            ).getCanonicalFile();

                    String targetPath =
                            targetFile.getCanonicalPath();

                    if (!targetPath.startsWith(
                            baseDirPath
                                    + java.io.File.separator
                    )) {
                        throw new SecurityException(
                                "Invalid file path: Path Traversal detected"
                        );
                    }

                    file.transferTo(targetFile);

                    ErpApplicationDocument doc =
                            new ErpApplicationDocument();

                    doc.setApplication(app);
                    doc.setDocumentType(
                            ErpApplicationDocument.DocumentType.OTHER
                    );
                    doc.setFileSize(file.getSize());
                    doc.setContentType(file.getContentType());
                    doc.setOriginalFileName(
                            file.getOriginalFilename()
                    );
                    doc.setStoredFileName(docName);

                    String documentDatabasePath =
                            "/" + uploadDir + docName;

                    doc.setFilePath(documentDatabasePath);
                    app.addDocument(doc);

                    String previousMarks =
                            app.getPrevMarksDoc() == null
                                    ? ""
                                    : app.getPrevMarksDoc();

                    app.setPrevMarksDoc(
                            previousMarks
                                    + documentDatabasePath
                                    + ";"
                    );
                }
            }

            applicationRepository.save(app);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to upload files: "
                            + e.getMessage(),
                    e
            );
        }
    }

    private String generateUploadDirectory(ErpApplication app, String trustedRefNumber) {
        String schoolCode = app.getBranch() != null && app.getBranch().getSchoolCode() != null ? app.getBranch().getSchoolCode() : "UNKNOWN";
        String branchName = app.getBranch() != null && app.getBranch().getBranchName() != null ? app.getBranch().getBranchName() : "Branch";
        String branchLocation = app.getBranch() != null && app.getBranch().getBranchLocation() != null ? app.getBranch().getBranchLocation() : "Location";

        String folderPrefix = schoolCode + "-" + branchName + "," + branchLocation;
        folderPrefix = folderPrefix.replaceAll("[^a-zA-Z0-9.\\-, ]", "_");

        return "uploads/applications/" + folderPrefix + "/" + trustedRefNumber + "/";
    }

    // This absolutely breaks the IntelliJ data-flow taint tracker.
    // By returning hardcoded strings, the resulting file path has ZERO user input in it!
    private String sanitizeFileName(String prefix, String originalFilename) {
        String safeExt = getSafeExtension(originalFilename);

        // Example output: photo_17180000_123e4567.jpg
        return prefix + "_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().substring(0, 8) + safeExt;
    }

    // Only allow known, safe extensions via hardcoded strings.
    private String getSafeExtension(String originalFilename) {
        if (originalFilename == null) return ".bin";

        String lower = originalFilename.toLowerCase();
        if (lower.endsWith(".jpg")) return ".jpg";
        if (lower.endsWith(".jpeg")) return ".jpeg";
        if (lower.endsWith(".png")) return ".png";
        if (lower.endsWith(".pdf")) return ".pdf";
        if (lower.endsWith(".doc")) return ".doc";
        if (lower.endsWith(".docx")) return ".docx";

        // Fallback for any unknown file types
        return ".bin";
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

    @Transactional(readOnly = true)
    public Map<String, Object> verifyAndGetStatus(
            String refNumber,
            String dobString
    ) {
        Map<String, Object> response = new HashMap<>();

        Optional<ErpApplication> appOpt =
                applicationRepository.findByApplicationNo(refNumber);

        if (appOpt.isEmpty()) {
            response.put("success", false);
            response.put(
                    "message",
                    "Invalid Reference Number or Date of Birth."
            );
            return response;
        }

        ErpApplication app = appOpt.get();

        if (
                app.getDateOfBirth() == null ||
                        !app.getDateOfBirth()
                                .toString()
                                .equals(dobString)
        ) {
            response.put("success", false);
            response.put(
                    "message",
                    "Invalid Reference Number or Date of Birth."
            );
            return response;
        }

        Map<String, Object> data = new HashMap<>();

        StringBuilder fullName = new StringBuilder();

        if (
                app.getFirstName() != null &&
                        !app.getFirstName().isBlank()
        ) {
            fullName.append(app.getFirstName().trim());
        }

        if (
                app.getMiddleName() != null &&
                        !app.getMiddleName().isBlank()
        ) {
            if (!fullName.isEmpty()) {
                fullName.append(' ');
            }

            fullName.append(app.getMiddleName().trim());
        }

        if (
                app.getLastName() != null &&
                        !app.getLastName().isBlank()
        ) {
            if (!fullName.isEmpty()) {
                fullName.append(' ');
            }

            fullName.append(app.getLastName().trim());
        }

        data.put(
                "student_name",
                fullName.toString()
        );

        String appliedClass =
                app.getBranchClassId() != null
                        ? String.valueOf(app.getBranchClassId())
                        : "";

        if (app.getBranchClassId() != null) {
            Optional<SchoolClass> schoolClassOpt =
                    classRepository.findById(
                            app.getBranchClassId().intValue()
                    );

            if (schoolClassOpt.isPresent()) {
                appliedClass =
                        schoolClassOpt
                                .get()
                                .getClassName();
            }
        }

        data.put(
                "applied_class",
                appliedClass
        );

        data.put(
                "status",
                app.getApplicationStatus() != null
                        ? app.getApplicationStatus().name()
                        : ""
        );

        data.put(
                "ref_number",
                app.getApplicationNo()
        );

        response.put("success", true);
        response.put("data", data);

        // Used by the controller to store the verified application
        // in the secure HTTP session.
        response.put(
                "internal_id",
                app.getApplicationId()
        );

        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getApplicationDetails(
            Long applicationId
    ) {
        Map<String, Object> response = new HashMap<>();

        if (applicationId == null || applicationId <= 0) {
            response.put("success", false);
            response.put(
                    "message",
                    "Invalid application reference."
            );
            return response;
        }

        Optional<ErpApplication> appOpt =
                applicationRepository.findById(applicationId);

        if (appOpt.isEmpty()) {
            response.put("success", false);
            response.put(
                    "message",
                    "Application not found."
            );
            return response;
        }

        ErpApplication app = appOpt.get();
        Map<String, Object> data = new HashMap<>();

        data.put(
                "ref_number",
                app.getApplicationNo()
        );

        data.put(
                "status",
                app.getApplicationStatus() != null
                        ? app.getApplicationStatus().name()
                        : ""
        );

        Branch branch = app.getBranch();

        data.put(
                "branch_name",
                branch != null &&
                        branch.getBranchName() != null
                        ? branch.getBranchName()
                        : ""
        );

        data.put(
                "branch_location",
                branch != null &&
                        branch.getBranchLocation() != null
                        ? branch.getBranchLocation()
                        : ""
        );

        data.put(
                "date_of_registration",
                app.getDateOfRegistration()
        );

        data.put(
                "scholarship_status",
                app.getScholarshipStatus()
        );

        data.put(
                "student_name",
                app.getFirstName()
        );

        data.put(
                "middle_name",
                app.getMiddleName()
        );

        data.put(
                "student_surname",
                app.getLastName()
        );

        data.put(
                "gender",
                app.getGender() != null
                        ? app.getGender().name()
                        : ""
        );

        data.put(
                "dob",
                app.getDateOfBirth() != null
                        ? app.getDateOfBirth().toString()
                        : ""
        );

        data.put(
                "nationality",
                app.getNationality()
        );

        String academicYearDisplay = "";

        if (app.getAcademicYearId() != null) {

            Integer branchId =
                    app.getBranch() != null
                            ? app.getBranch().getBranchId()
                            : null;

            if (branchId != null) {

                Optional<ErpAcademicYear> academicYearOpt =
                        academicYearRepository
                                .findByAcademicYearIdAndBranchBranchId(
                                        app.getAcademicYearId(),
                                        branchId
                                );

                if (academicYearOpt.isPresent()) {

                    ErpAcademicYear academicYear =
                            academicYearOpt.get();

                    if (
                            academicYear.getAcademicYearCode() != null
                                    && !academicYear
                                    .getAcademicYearCode()
                                    .isBlank()
                    ) {
                        academicYearDisplay =
                                academicYear.getAcademicYearCode();
                    } else if (
                            academicYear.getAcademicYearName() != null
                                    && !academicYear
                                    .getAcademicYearName()
                                    .isBlank()
                    ) {
                        academicYearDisplay =
                                academicYear.getAcademicYearName();
                    } else if (
                            academicYear.getStartDate() != null
                    ) {
                        academicYearDisplay =
                                String.valueOf(
                                        academicYear
                                                .getStartDate()
                                                .getYear()
                                );
                    }
                }
            }
        }

        data.put(
                "academic_year",
                academicYearDisplay
        );

        data.put(
                "term",
                app.getTerm()
        );

        data.put("applied_class", "");
        data.put("class_code", "");
        data.put("level", "");

        if (app.getBranchClassId() != null) {
            Optional<SchoolClass> schoolClassOpt =
                    classRepository.findById(
                            app.getBranchClassId().intValue()
                    );

            if (schoolClassOpt.isPresent()) {
                SchoolClass schoolClass =
                        schoolClassOpt.get();

                data.put(
                        "applied_class",
                        schoolClass.getClassName() != null
                                ? schoolClass.getClassName()
                                : ""
                );

                data.put(
                        "class_code",
                        schoolClass.getClassCode() != null
                                ? schoolClass.getClassCode()
                                : ""
                );

                Level level = schoolClass.getLevel();

                data.put(
                        "level",
                        level != null &&
                                level.getLevelName() != null
                                ? level.getLevelName()
                                : ""
                );
            }
        }

        data.put(
                "photo_path",
                app.getPhotoPath()
        );

        data.put(
                "primary_email",
                app.getPrimaryEmail()
        );

        data.put(
                "primary_mobile",
                app.getPrimaryMobile()
        );

        data.put(
                "father_name",
                app.getFatherName()
        );

        data.put(
                "father_contact",
                app.getFatherContact()
        );

        data.put(
                "father_email",
                app.getFatherEmail()
        );

        data.put(
                "father_occupation",
                app.getFatherOccupation()
        );

        data.put(
                "father_education",
                app.getFatherEducation()
        );

        data.put(
                "father_age",
                app.getFatherAge()
        );

        data.put(
                "mother_name",
                app.getMotherName()
        );

        data.put(
                "mother_contact",
                app.getMotherContact()
        );

        data.put(
                "mother_email",
                app.getMotherEmail()
        );

        data.put(
                "mother_occupation",
                app.getMotherOccupation()
        );

        data.put(
                "mother_education",
                app.getMotherEducation()
        );

        data.put(
                "mother_age",
                app.getMotherAge()
        );

        data.put(
                "guardian_name",
                app.getGuardianName()
        );

        data.put(
                "guardian_relation",
                app.getGuardianRelation()
        );

        data.put(
                "guardian_contact",
                app.getGuardianContact()
        );

        data.put(
                "guardian_email",
                app.getGuardianEmail()
        );

        data.put(
                "guardian_occupation",
                app.getGuardianOccupation()
        );

        data.put(
                "guardian_education",
                app.getGuardianEducation()
        );

        data.put(
                "guardian_age",
                app.getGuardianAge()
        );

        data.put(
                "guardian_location",
                app.getGuardianLocation()
        );

        data.put(
                "address_house",
                app.getAddressHouse()
        );

        data.put(
                "address_street",
                app.getAddressStreet()
        );

        data.put(
                "address_village",
                app.getAddressVillage()
        );

        data.put(
                "address_district",
                app.getAddressDistrict()
        );

        data.put(
                "address_state",
                app.getAddressState()
        );

        data.put(
                "address_postal",
                app.getAddressPostal()
        );

        data.put(
                "former_school",
                app.getFormerSchool()
        );

        data.put(
                "former_school_code",
                app.getFormerSchoolCode()
        );

        data.put(
                "former_school_lin",
                app.getFormerSchoolLin()
        );

        data.put(
                "ple_ref",
                app.getPleRef()
        );

        data.put(
                "ple_score",
                app.getPleScore()
        );

        data.put(
                "uce_ref",
                app.getUceRef()
        );

        data.put(
                "uce_score",
                app.getUceScore()
        );

        data.put(
                "subject_marks",
                app.getSubjectMarks()
        );

        data.put(
                "more_info",
                app.getMoreInfo()
        );

        response.put("success", true);
        response.put("data", data);

        return response;
    }

    // -------------------------------------------------------------------------
    // CONTROLLER REFACTORING: Public Data Lookups
    // These methods securely fetch unauthenticated public data without exposing
    // your raw JPA Repositories to the Controller layer!
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPublicBranches() {
        List<Map<String, Object>> branchList = new ArrayList<>();

        for (Branch branch : branchRepository.findAll()) {
            Map<String, Object> branchMap = new HashMap<>();

            branchMap.put(
                    "branchId",
                    branch.getBranchId()
            );
            branchMap.put(
                    "branchName",
                    branch.getBranchName()
            );
            branchMap.put(
                    "branchLocation",
                    branch.getBranchLocation()
            );
            branchMap.put(
                    "schoolCode",
                    branch.getSchoolCode()
            );
            branchMap.put(
                    "branchLevels",
                    extractBranchLevelsList(
                            branch.getBranchLevels()
                    )
            );
            branchMap.put(
                    "academicYears",
                    extractActiveAcademicYears(
                            branch.getBranchId()
                    )
            );

            branchList.add(branchMap);
        }

        return branchList;
    }

    private List<Map<String, Object>> extractActiveAcademicYears(
            Integer branchId
    ) {
        List<Map<String, Object>> academicYears =
                new ArrayList<>();

        if (branchId == null || branchId <= 0) {
            return academicYears;
        }

        LocalDate today = LocalDate.now();

        for (ErpAcademicYear academicYear
                : academicYearRepository
                .findAllByBranchBranchIdAndActiveTrueOrderByStartDateDesc(
                        branchId
                )) {

            if (!isAdmissionOpenForAcademicYear(
                    academicYear,
                    today
            )) {
                continue;
            }

            Map<String, Object> academicYearMap =
                    new HashMap<>();

            academicYearMap.put(
                    "academicYearId",
                    academicYear.getAcademicYearId()
            );
            academicYearMap.put(
                    "academicYearCode",
                    academicYear.getAcademicYearCode()
            );
            academicYearMap.put(
                    "academicYearName",
                    academicYear.getAcademicYearName()
            );
            academicYearMap.put(
                    "startDate",
                    academicYear.getStartDate()
            );
            academicYearMap.put(
                    "endDate",
                    academicYear.getEndDate()
            );
            academicYearMap.put(
                    "admissionStartDate",
                    academicYear.getAdmissionStartDate()
            );
            academicYearMap.put(
                    "admissionEndDate",
                    academicYear.getAdmissionEndDate()
            );
            academicYearMap.put(
                    "currentYear",
                    academicYear.getCurrentYear()
            );
            academicYearMap.put(
                    "admissionYearType",
                    resolveAdmissionYearType(
                            academicYear,
                            today
                    )
            );
            academicYearMap.put(
                    "terms",
                    extractAdmissionTerms(
                            branchId,
                            academicYear.getAcademicYearId()
                    )
            );

            academicYears.add(academicYearMap);
        }

        return academicYears;
    }

    private boolean isAdmissionOpenForAcademicYear(
            ErpAcademicYear academicYear,
            LocalDate today
    ) {
        if (
                academicYear == null
                        || !Boolean.TRUE.equals(
                        academicYear.getActive()
                )
                        || academicYear.getStatus()
                        == ErpAcademicYear.Status.CLOSED
        ) {
            return false;
        }

        String admissionYearType =
                resolveAdmissionYearType(
                        academicYear,
                        today
                );

        if (admissionYearType == null) {
            return false;
        }

        LocalDate admissionStartDate =
                academicYear.getAdmissionStartDate();

        LocalDate admissionEndDate =
                academicYear.getAdmissionEndDate();

        boolean hasAdmissionWindow =
                admissionStartDate != null
                        || admissionEndDate != null;

        if (hasAdmissionWindow) {
            boolean started =
                    admissionStartDate == null
                            || !today.isBefore(
                            admissionStartDate
                    );

            boolean notEnded =
                    admissionEndDate == null
                            || !today.isAfter(
                            admissionEndDate
                    );

            return started && notEnded;
        }

        /*
         * Backward-compatible default:
         * the branch's current active Academic Year remains available when
         * admission dates have not yet been configured. Upcoming years are
         * exposed only after their admission window is configured and open.
         */
        return "CURRENT".equals(
                admissionYearType
        );
    }

    private String resolveAdmissionYearType(
            ErpAcademicYear academicYear,
            LocalDate today
    ) {
        if (academicYear == null || today == null) {
            return null;
        }

        if (Boolean.TRUE.equals(
                academicYear.getCurrentYear()
        )) {
            return "CURRENT";
        }

        LocalDate startDate =
                academicYear.getStartDate();

        LocalDate endDate =
                academicYear.getEndDate();

        if (
                startDate != null
                        && endDate != null
                        && !today.isBefore(startDate)
                        && !today.isAfter(endDate)
        ) {
            return "CURRENT";
        }

        if (
                startDate != null
                        && startDate.isAfter(today)
        ) {
            return "UPCOMING";
        }

        return null;
    }

    private List<Map<String, Object>> extractAdmissionTerms(
            Integer branchId,
            Long academicYearId
    ) {
        List<Map<String, Object>> terms =
                new ArrayList<>();

        if (
                branchId == null
                        || branchId <= 0
                        || academicYearId == null
                        || academicYearId <= 0
        ) {
            return terms;
        }

        for (ErpAcademicTerm academicTerm
                : academicTermRepository
                .findAllActiveByBranchAndAcademicYear(
                        branchId,
                        academicYearId
                )) {

            if (
                    academicTerm.getStatus()
                            == ErpAcademicTerm.Status.CLOSED
            ) {
                continue;
            }

            Map<String, Object> termMap =
                    new HashMap<>();

            termMap.put(
                    "termId",
                    academicTerm.getTermId()
            );
            termMap.put(
                    "termCode",
                    academicTerm.getTermCode()
            );
            termMap.put(
                    "termName",
                    academicTerm.getTermName()
            );
            termMap.put(
                    "displayOrder",
                    academicTerm.getDisplayOrder()
            );
            termMap.put(
                    "startDate",
                    academicTerm.getStartDate()
            );
            termMap.put(
                    "endDate",
                    academicTerm.getEndDate()
            );
            termMap.put(
                    "currentTerm",
                    academicTerm.getCurrentTerm()
            );
            termMap.put(
                    "status",
                    academicTerm.getStatus()
            );

            terms.add(termMap);
        }

        return terms;
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

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPublicLevels() {
        List<Map<String, Object>> levelList = new ArrayList<>();
        for (Level lvl : levelRepository.findAll()) {
            Map<String, Object> levelMap = new HashMap<>();
            levelMap.put("levelId", lvl.getLevelId());
            levelMap.put("levelName", lvl.getLevelName());
            levelList.add(levelMap);
        }
        return levelList;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPublicClasses() {
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
        return classList;
    }
}
