package com.erp.montfortuganda.admission.dto;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Complete branch-facing view of one public admission application.
 *
 * <p>This DTO contains the applicant information required for school review
 * while deliberately excluding physical storage paths, stored file names,
 * file hashes, upload-token hashes, and other server-managed secrets.</p>
 */
@Data
public class BranchApplicationDetailsResponseDTO {

    // ---------------------------------------------------------------------
    // Application identity and resolved academic references
    // ---------------------------------------------------------------------

    private Long applicationId;
    private String applicationNo;

    private Integer branchId;
    private String branchName;
    private String schoolCode;

    private Long academicYearId;
    private String academicYearCode;
    private String academicYearName;

    private Integer branchClassId;
    private String classCode;
    private String className;

    /**
     * Legacy term text submitted by the existing public portal.
     */
    private String term;

    private Long joiningTermId;
    private String joiningTermCode;
    private String joiningTermName;

    private ErpApplication.AdmissionType admissionType;

    // ---------------------------------------------------------------------
    // Student and primary-contact details
    // ---------------------------------------------------------------------

    private String primaryEmail;
    private String primaryMobile;

    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;

    private ErpApplication.Gender gender;
    private LocalDate dateOfBirth;
    private String nationality;

    /**
     * The original database stores this value as text.
     */
    private String dateOfRegistration;

    /**
     * Indicates whether a student photo exists without exposing its path.
     */
    private Boolean photoAvailable;

    private String moreInfo;

    // ---------------------------------------------------------------------
    // Previous-school and academic details
    // ---------------------------------------------------------------------

    private String scholarshipStatus;

    private String previousSchool;
    private String formerSchool;
    private String formerSchoolCode;
    private String formerSchoolLin;

    private String pleRef;
    private Double pleScore;

    private String uceRef;
    private Double uceScore;

    private String subjectMarks;

    /**
     * Indicates whether a legacy previous-marks document reference exists.
     */
    private Boolean previousMarksDocumentAvailable;

    // ---------------------------------------------------------------------
    // Father details
    // ---------------------------------------------------------------------

    private String fatherName;
    private Integer fatherAge;
    private String fatherContact;
    private String fatherEmail;
    private String fatherOccupation;
    private String fatherEducation;

    // ---------------------------------------------------------------------
    // Mother details
    // ---------------------------------------------------------------------

    private String motherName;
    private Integer motherAge;
    private String motherContact;
    private String motherEmail;
    private String motherOccupation;
    private String motherEducation;

    // ---------------------------------------------------------------------
    // Guardian details
    // ---------------------------------------------------------------------

    private String guardianName;
    private Integer guardianAge;
    private String guardianMobile;
    private String guardianContact;
    private String guardianEmail;
    private String guardianRelation;
    private String guardianOccupation;
    private String guardianEducation;
    private String guardianLocation;

    // ---------------------------------------------------------------------
    // Address
    // ---------------------------------------------------------------------

    private String addressRegion;
    private String addressDistrict;
    private String addressVillage;
    private String addressStreet;
    private String addressHouse;
    private String addressPostal;

    // ---------------------------------------------------------------------
    // Admission workflow
    // ---------------------------------------------------------------------

    private ErpApplication.ApplicationStatus applicationStatus;
    private ErpApplication.CurrentStage currentStage;
    private ErpApplication.VerificationStatus verificationStatus;
    private ErpApplication.DocumentStatus documentStatus;
    private ErpApplication.TestStatus testStatus;
    private ErpApplication.FeeDecisionStatus feeDecisionStatus;
    private String scholarshipWorkflowStatus;
    private ErpApplication.PaymentStatus paymentStatus;
    private ErpApplication.AdmissionStatus admissionStatus;

    private LocalDateTime schoolVisitAt;
    private String schoolVisitRemarks;

    private Long verificationDecisionBy;
    private LocalDateTime verificationDecisionAt;
    private String rejectionReason;

    private String remarks;

    // ---------------------------------------------------------------------
    // Conversion and audit information
    // ---------------------------------------------------------------------

    private Long studentId;
    private Long enrollmentId;
    private Boolean studentCreated;
    private Long convertedBy;
    private LocalDateTime convertedAt;
    private Boolean workflowLocked;

    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private Integer status;

    // ---------------------------------------------------------------------
    // Review collections
    // ---------------------------------------------------------------------

    private List<ApplicationDocumentResponseDTO> documents =
            new ArrayList<>();

    private List<ApplicationDocumentRequestResponseDTO> documentRequests =
            new ArrayList<>();

    private List<StatusHistoryItem> statusHistory =
            new ArrayList<>();

    /**
     * Safe branch-facing status-history item.
     */
    @Data
    public static class StatusHistoryItem {

        private Long historyId;
        private String stage;
        private String oldStatus;
        private String newStatus;

        private String publicRemarks;
        private String internalRemarks;

        private String transitionSource;

        private Boolean emailRequired;
        private String emailStatus;
        private String emailType;
        private LocalDateTime emailSentAt;

        private Long changedBy;
        private LocalDateTime changedAt;

        private Boolean active;
        private Long version;
    }
}
