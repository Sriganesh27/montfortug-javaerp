package com.erp.montfortuganda.admission.mapper;

import com.erp.montfortuganda.admission.dto.ApplicationDocumentRequestResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationDocumentResponseDTO;
import com.erp.montfortuganda.admission.dto.BranchApplicationDetailsResponseDTO;
import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.entity.ErpApplicationDocument;
import com.erp.montfortuganda.admission.entity.ErpApplicationDocumentRequest;
import com.erp.montfortuganda.admission.entity.ErpApplicationStatusHistory;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.ErpAcademicTerm;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import com.erp.montfortuganda.school.entity.SchoolClass;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the complete branch-facing admission-application response.
 *
 * <p>Reference entities and review collections are supplied by the service
 * after branch ownership has been validated. This mapper does not perform
 * database access and does not expose file-system paths, stored file names,
 * hashes, or upload tokens.</p>
 */
@Component
public class BranchApplicationDetailsMapper {

    private final ApplicationDocumentMapper applicationDocumentMapper;

    public BranchApplicationDetailsMapper(
            ApplicationDocumentMapper applicationDocumentMapper
    ) {
        this.applicationDocumentMapper = applicationDocumentMapper;
    }

    public BranchApplicationDetailsResponseDTO toDetailsResponse(
            ErpApplication application,
            ErpAcademicYear academicYear,
            SchoolClass schoolClass,
            ErpAcademicTerm joiningTerm,
            List<ErpApplicationDocument> documents,
            List<ErpApplicationDocumentRequest> documentRequests,
            List<ErpApplicationStatusHistory> statusHistory
    ) {
        if (application == null) {
            return null;
        }

        BranchApplicationDetailsResponseDTO response =
                new BranchApplicationDetailsResponseDTO();

        mapIdentity(
                response,
                application,
                academicYear,
                schoolClass,
                joiningTerm
        );

        mapStudentAndContactDetails(
                response,
                application
        );

        mapPreviousSchoolDetails(
                response,
                application
        );

        mapFamilyDetails(
                response,
                application
        );

        mapAddress(
                response,
                application
        );

        mapWorkflow(
                response,
                application
        );

        mapConversionAndAudit(
                response,
                application
        );

        List<ErpApplicationDocument> safeDocuments =
                documents == null
                        ? Collections.emptyList()
                        : documents;

        List<ErpApplicationDocumentRequest> safeRequests =
                documentRequests == null
                        ? Collections.emptyList()
                        : documentRequests;

        List<ErpApplicationStatusHistory> safeHistory =
                statusHistory == null
                        ? Collections.emptyList()
                        : statusHistory;

        response.setDocuments(
                safeDocuments.stream()
                        .map(
                                applicationDocumentMapper
                                        ::toDocumentResponse
                        )
                        .toList()
        );

        Map<Long, ErpApplicationDocument>
                documentByRequestId =
                indexDocumentsByRequestId(
                        safeDocuments
                );

        response.setDocumentRequests(
                safeRequests.stream()
                        .map(
                                request ->
                                        mapDocumentRequest(
                                                request,
                                                documentByRequestId
                                        )
                        )
                        .toList()
        );

        response.setStatusHistory(
                safeHistory.stream()
                        .map(this::toHistoryItem)
                        .toList()
        );

        return response;
    }

    private void mapIdentity(
            BranchApplicationDetailsResponseDTO response,
            ErpApplication application,
            ErpAcademicYear academicYear,
            SchoolClass schoolClass,
            ErpAcademicTerm joiningTerm
    ) {
        response.setApplicationId(
                application.getApplicationId()
        );
        response.setApplicationNo(
                application.getApplicationNo()
        );

        Branch branch =
                application.getBranch();

        if (branch != null) {
            response.setBranchId(
                    branch.getBranchId()
            );
            response.setBranchName(
                    branch.getBranchName()
            );
            response.setSchoolCode(
                    branch.getSchoolCode()
            );
        }

        response.setAcademicYearId(
                application.getAcademicYearId()
        );

        if (academicYear != null) {
            response.setAcademicYearCode(
                    academicYear.getAcademicYearCode()
            );
            response.setAcademicYearName(
                    academicYear.getAcademicYearName()
            );
        }

        response.setBranchClassId(
                application.getBranchClassId()
        );

        if (schoolClass != null) {
            response.setClassCode(
                    schoolClass.getClassCode()
            );
            response.setClassName(
                    schoolClass.getClassName()
            );
        }

        response.setTerm(
                application.getTerm()
        );
        response.setJoiningTermId(
                application.getJoiningTermId()
        );

        if (joiningTerm != null) {
            response.setJoiningTermCode(
                    joiningTerm.getTermCode()
            );
            response.setJoiningTermName(
                    joiningTerm.getTermName()
            );
        }

        response.setAdmissionType(
                application.getAdmissionType()
        );
    }

    private void mapStudentAndContactDetails(
            BranchApplicationDetailsResponseDTO response,
            ErpApplication application
    ) {
        response.setPrimaryEmail(
                application.getPrimaryEmail()
        );
        response.setPrimaryMobile(
                application.getPrimaryMobile()
        );

        response.setFirstName(
                application.getFirstName()
        );
        response.setMiddleName(
                application.getMiddleName()
        );
        response.setLastName(
                application.getLastName()
        );
        response.setFullName(
                buildFullName(application)
        );

        response.setGender(
                application.getGender()
        );
        response.setDateOfBirth(
                application.getDateOfBirth()
        );
        response.setNationality(
                application.getNationality()
        );
        response.setDateOfRegistration(
                application.getDateOfRegistration()
        );

        response.setPhotoAvailable(
                StringUtils.hasText(
                        application.getPhotoPath()
                )
        );
        response.setMoreInfo(
                application.getMoreInfo()
        );
    }

    private void mapPreviousSchoolDetails(
            BranchApplicationDetailsResponseDTO response,
            ErpApplication application
    ) {
        response.setScholarshipStatus(
                application.getScholarshipStatus()
        );

        response.setPreviousSchool(
                application.getPreviousSchool()
        );
        response.setFormerSchool(
                application.getFormerSchool()
        );
        response.setFormerSchoolCode(
                application.getFormerSchoolCode()
        );
        response.setFormerSchoolLin(
                application.getFormerSchoolLin()
        );

        response.setPleRef(
                application.getPleRef()
        );
        response.setPleScore(
                application.getPleScore()
        );

        response.setUceRef(
                application.getUceRef()
        );
        response.setUceScore(
                application.getUceScore()
        );

        response.setSubjectMarks(
                application.getSubjectMarks()
        );

        response.setPreviousMarksDocumentAvailable(
                StringUtils.hasText(
                        application.getPrevMarksDoc()
                )
        );
    }

    private void mapFamilyDetails(
            BranchApplicationDetailsResponseDTO response,
            ErpApplication application
    ) {
        response.setFatherName(
                application.getFatherName()
        );
        response.setFatherAge(
                application.getFatherAge()
        );
        response.setFatherContact(
                application.getFatherContact()
        );
        response.setFatherEmail(
                application.getFatherEmail()
        );
        response.setFatherOccupation(
                application.getFatherOccupation()
        );
        response.setFatherEducation(
                application.getFatherEducation()
        );

        response.setMotherName(
                application.getMotherName()
        );
        response.setMotherAge(
                application.getMotherAge()
        );
        response.setMotherContact(
                application.getMotherContact()
        );
        response.setMotherEmail(
                application.getMotherEmail()
        );
        response.setMotherOccupation(
                application.getMotherOccupation()
        );
        response.setMotherEducation(
                application.getMotherEducation()
        );

        response.setGuardianName(
                application.getGuardianName()
        );
        response.setGuardianAge(
                application.getGuardianAge()
        );
        response.setGuardianMobile(
                application.getGuardianMobile()
        );
        response.setGuardianContact(
                application.getGuardianContact()
        );
        response.setGuardianEmail(
                application.getGuardianEmail()
        );
        response.setGuardianRelation(
                application.getGuardianRelation()
        );
        response.setGuardianOccupation(
                application.getGuardianOccupation()
        );
        response.setGuardianEducation(
                application.getGuardianEducation()
        );
        response.setGuardianLocation(
                application.getGuardianLocation()
        );
    }

    private void mapAddress(
            BranchApplicationDetailsResponseDTO response,
            ErpApplication application
    ) {
        response.setAddressRegion(
                application.getAddressState()
        );
        response.setAddressDistrict(
                application.getAddressDistrict()
        );
        response.setAddressVillage(
                application.getAddressVillage()
        );
        response.setAddressStreet(
                application.getAddressStreet()
        );
        response.setAddressHouse(
                application.getAddressHouse()
        );
        response.setAddressPostal(
                application.getAddressPostal()
        );
    }

    private void mapWorkflow(
            BranchApplicationDetailsResponseDTO response,
            ErpApplication application
    ) {
        response.setApplicationStatus(
                application.getApplicationStatus()
        );
        response.setCurrentStage(
                application.getCurrentStage()
        );
        response.setVerificationStatus(
                application.getVerificationStatus()
        );
        response.setDocumentStatus(
                application.getDocumentStatus()
        );
        response.setTestStatus(
                application.getTestStatus()
        );
        response.setFeeDecisionStatus(
                application.getFeeDecisionStatus()
        );

        response.setScholarshipWorkflowStatus(
                application.getScholarshipStatus()
        );

        response.setPaymentStatus(
                application.getPaymentStatus()
        );
        response.setAdmissionStatus(
                application.getAdmissionStatus()
        );

        response.setSchoolVisitAt(
                application.getSchoolVisitAt()
        );
        response.setSchoolVisitRemarks(
                application.getSchoolVisitRemarks()
        );
        response.setVerificationDecisionBy(
                application.getVerificationDecisionBy()
        );
        response.setVerificationDecisionAt(
                application.getVerificationDecisionAt()
        );
        response.setRejectionReason(
                application.getRejectionReason()
        );
        response.setRemarks(
                application.getRemarks()
        );
    }

    private void mapConversionAndAudit(
            BranchApplicationDetailsResponseDTO response,
            ErpApplication application
    ) {
        response.setStudentId(
                application.getStudentId()
        );
        response.setEnrollmentId(
                application.getEnrollmentId()
        );
        response.setStudentCreated(
                application.getStudentCreated()
        );
        response.setConvertedBy(
                application.getConvertedBy()
        );
        response.setConvertedAt(
                application.getConvertedAt()
        );
        response.setWorkflowLocked(
                application.getWorkflowLocked()
        );

        response.setCreatedBy(
                application.getCreatedBy()
        );
        response.setCreatedAt(
                application.getCreatedAt()
        );
        response.setUpdatedBy(
                application.getUpdatedBy()
        );
        response.setUpdatedAt(
                application.getUpdatedAt()
        );
        response.setStatus(
                application.getStatus()
        );
    }

    private ApplicationDocumentRequestResponseDTO
    mapDocumentRequest(
            ErpApplicationDocumentRequest request,
            Map<Long, ErpApplicationDocument>
                    documentByRequestId
    ) {
        ErpApplicationDocument uploadedDocument =
                request == null
                        || request.getRequestId() == null
                        ? null
                        : documentByRequestId.get(
                                request.getRequestId()
                        );

        return applicationDocumentMapper
                .toDocumentRequestResponse(
                        request,
                        uploadedDocument
                );
    }

    private Map<Long, ErpApplicationDocument>
    indexDocumentsByRequestId(
            List<ErpApplicationDocument> documents
    ) {
        Map<Long, ErpApplicationDocument> result =
                new HashMap<>();

        for (ErpApplicationDocument document
                : documents) {

            if (document == null
                    || document.getDocumentRequest()
                    == null
                    || document.getDocumentRequest()
                    .getRequestId() == null) {
                continue;
            }

            Long requestId =
                    document.getDocumentRequest()
                            .getRequestId();

            ErpApplicationDocument existing =
                    result.get(requestId);

            if (existing == null
                    || isNewer(
                            document,
                            existing
                    )) {
                result.put(
                        requestId,
                        document
                );
            }
        }

        return result;
    }

    private boolean isNewer(
            ErpApplicationDocument candidate,
            ErpApplicationDocument existing
    ) {
        if (candidate.getUploadedAt() == null) {
            return false;
        }

        return existing.getUploadedAt() == null
                || candidate.getUploadedAt()
                .isAfter(existing.getUploadedAt());
    }

    private BranchApplicationDetailsResponseDTO
    .StatusHistoryItem toHistoryItem(
            ErpApplicationStatusHistory history
    ) {
        BranchApplicationDetailsResponseDTO
                .StatusHistoryItem item =
                new BranchApplicationDetailsResponseDTO
                        .StatusHistoryItem();

        item.setHistoryId(
                history.getHistoryId()
        );
        item.setStage(
                history.getStage()
        );
        item.setOldStatus(
                history.getOldStatus()
        );
        item.setNewStatus(
                history.getNewStatus()
        );
        item.setPublicRemarks(
                history.getPublicRemarks()
        );
        item.setInternalRemarks(
                firstNonBlank(
                        history.getInternalRemarks(),
                        history.getRemarks()
                )
        );
        item.setTransitionSource(
                history.getTransitionSource()
        );
        item.setEmailRequired(
                history.getEmailRequired()
        );
        item.setEmailStatus(
                history.getEmailStatus()
        );
        item.setEmailType(
                history.getEmailType()
        );
        item.setEmailSentAt(
                history.getEmailSentAt()
        );
        item.setChangedBy(
                history.getChangedBy()
        );
        item.setChangedAt(
                history.getChangedAt()
        );
        item.setActive(
                history.getActive()
        );
        item.setVersion(
                history.getVersion()
        );

        return item;
    }

    private String buildFullName(
            ErpApplication application
    ) {
        StringBuilder fullName =
                new StringBuilder();

        appendNamePart(
                fullName,
                application.getFirstName()
        );
        appendNamePart(
                fullName,
                application.getMiddleName()
        );
        appendNamePart(
                fullName,
                application.getLastName()
        );

        return fullName.toString();
    }

    private void appendNamePart(
            StringBuilder target,
            String value
    ) {
        if (!StringUtils.hasText(value)) {
            return;
        }

        if (!target.isEmpty()) {
            target.append(' ');
        }

        target.append(
                value.trim()
        );
    }

    private String firstNonBlank(
            String preferred,
            String fallback
    ) {
        if (StringUtils.hasText(preferred)) {
            return preferred.trim();
        }

        return StringUtils.hasText(fallback)
                ? fallback.trim()
                : null;
    }
}
