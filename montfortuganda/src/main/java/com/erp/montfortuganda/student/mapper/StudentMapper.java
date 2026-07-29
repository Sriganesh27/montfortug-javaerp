package com.erp.montfortuganda.student.mapper;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.student.dto.request.StudentAcademicHistoryRequest;
import com.erp.montfortuganda.student.dto.request.StudentDocumentMetadataRequest;
import com.erp.montfortuganda.student.dto.request.StudentDocumentVerificationRequest;
import com.erp.montfortuganda.student.dto.request.StudentEnrollmentRequest;
import com.erp.montfortuganda.student.dto.request.StudentEnrollmentUpdateRequest;
import com.erp.montfortuganda.student.dto.request.StudentHostelRequest;
import com.erp.montfortuganda.student.dto.request.StudentMedicalRequest;
import com.erp.montfortuganda.student.dto.request.StudentParentRequest;
import com.erp.montfortuganda.student.dto.request.StudentPersonalRequest;
import com.erp.montfortuganda.student.dto.request.StudentTransportRequest;
import com.erp.montfortuganda.student.dto.response.PagedStudentResponse;
import com.erp.montfortuganda.student.dto.response.StudentAcademicHistoryResponse;
import com.erp.montfortuganda.student.dto.response.StudentCreateResponse;
import com.erp.montfortuganda.student.dto.response.StudentDocumentResponse;
import com.erp.montfortuganda.student.dto.response.StudentEnrollmentHistoryResponse;
import com.erp.montfortuganda.student.dto.response.StudentEnrollmentResponse;
import com.erp.montfortuganda.student.dto.response.StudentHostelResponse;
import com.erp.montfortuganda.student.dto.response.StudentMedicalResponse;
import com.erp.montfortuganda.student.dto.response.StudentParentResponse;
import com.erp.montfortuganda.student.dto.response.StudentPersonalResponse;
import com.erp.montfortuganda.student.dto.response.StudentProfileResponse;
import com.erp.montfortuganda.student.dto.response.StudentSummaryResponse;
import com.erp.montfortuganda.student.dto.response.StudentTransportResponse;
import com.erp.montfortuganda.student.entity.ErpParent;
import com.erp.montfortuganda.student.entity.ErpStudent;
import com.erp.montfortuganda.student.entity.ErpStudentAcademicHistory;
import com.erp.montfortuganda.student.entity.ErpStudentDocument;
import com.erp.montfortuganda.student.entity.ErpStudentEnrollment;
import com.erp.montfortuganda.student.entity.ErpStudentEnrollmentHistory;
import com.erp.montfortuganda.student.entity.ErpStudentHostel;
import com.erp.montfortuganda.student.entity.ErpStudentMedical;
import com.erp.montfortuganda.student.entity.ErpStudentTransport;
import com.erp.montfortuganda.student.enums.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Maps Student request DTOs to persistence entities and persistence
 * entities to API response DTOs.
 *
 * Branch ownership, generated identifiers, audit values, physical file paths,
 * verification ownership and entity versions remain controlled by services
 * and JPA.
 */
@Component
public class StudentMapper {

    // =====================================================================
    // STUDENT REQUEST -> ENTITY
    // =====================================================================

    public ErpStudent toNewStudent(
            StudentPersonalRequest request,
            Branch branch,
            ErpApplication application,
            String admissionNo,
            String studentCode,
            Long authenticatedUserId
    ) {
        Objects.requireNonNull(
                request,
                "Student personal request is required."
        );
        Objects.requireNonNull(
                branch,
                "Student branch is required."
        );
        Objects.requireNonNull(
                admissionNo,
                "Generated admission number is required."
        );
        Objects.requireNonNull(
                studentCode,
                "Generated Student code is required."
        );

        ErpStudent student = new ErpStudent();

        student.setApplication(application);
        student.setBranch(branch);
        student.setAdmissionNo(trimRequired(admissionNo));
        student.setStudentCode(trimRequired(studentCode));

        mapStudentPersonalFields(
                request,
                student
        );

        student.setStudentStatus(
                StudentStatus.ACTIVE.name()
        );
        student.setActive(true);
        student.setCreatedBy(authenticatedUserId);

        return student;
    }

    public void updateStudent(
            StudentPersonalRequest request,
            ErpStudent student
    ) {
        Objects.requireNonNull(
                request,
                "Student personal request is required."
        );
        Objects.requireNonNull(
                student,
                "Student entity is required."
        );

        mapStudentPersonalFields(
                request,
                student
        );
    }

    private void mapStudentPersonalFields(
            StudentPersonalRequest request,
            ErpStudent student
    ) {
        student.setLearnerLin(
                uppercaseToNull(request.learnerLin())
        );
        student.setAdmissionYear(
                request.admissionYear()
        );

        student.setFirstName(
                trimRequired(request.firstName())
        );
        student.setMiddleName(
                trimToNull(request.middleName())
        );
        student.setLastName(
                trimToNull(request.lastName())
        );
        student.setFullName(
                buildFullName(
                        request.firstName(),
                        request.middleName(),
                        request.lastName()
                )
        );

        student.setGender(
                request.gender() != null
                        ? request.gender().name()
                        : null
        );
        student.setDateOfBirth(
                request.dateOfBirth()
        );
        student.setNationality(
                trimToNull(request.nationality())
        );

        student.setHouseNo(
                trimToNull(request.houseNo())
        );
        student.setStreet(
                trimToNull(request.street())
        );
        student.setVillage(
                trimToNull(request.village())
        );
        student.setTownCity(
                trimToNull(request.townCity())
        );
        student.setDistrict(
                trimToNull(request.district())
        );
        student.setState(
                trimToNull(request.state())
        );
        student.setCountry(
                trimToNull(request.country())
        );
        student.setPostalCode(
                trimToNull(request.postalCode())
        );
    }

    public void applyStudentStatus(
            ErpStudent student,
            StudentStatus status
    ) {
        Objects.requireNonNull(
                student,
                "Student entity is required."
        );
        Objects.requireNonNull(
                status,
                "Student status is required."
        );

        student.setStudentStatus(status.name());
        student.setActive(
                isActiveStudentStatus(status)
        );
    }

    // =====================================================================
    // PARENT REQUEST -> ENTITY
    // =====================================================================

    public ErpParent toNewParent(
            StudentParentRequest request,
            ErpStudent student,
            Branch branch,
            Long authenticatedUserId
    ) {
        Objects.requireNonNull(
                request,
                "Student parent request is required."
        );
        Objects.requireNonNull(
                student,
                "Student entity is required."
        );
        Objects.requireNonNull(
                branch,
                "Student branch is required."
        );

        ErpParent parent = new ErpParent();

        parent.setStudent(student);
        parent.setBranch(branch);
        parent.setAdmissionNo(
                student.getAdmissionNo()
        );

        mapParentFields(
                request,
                parent
        );

        parent.setActive(true);
        parent.setCreatedBy(authenticatedUserId);
        parent.setUpdatedBy(authenticatedUserId);

        return parent;
    }

    public void updateParent(
            StudentParentRequest request,
            ErpParent parent,
            Long authenticatedUserId
    ) {
        Objects.requireNonNull(
                request,
                "Student parent request is required."
        );
        Objects.requireNonNull(
                parent,
                "Parent entity is required."
        );

        mapParentFields(
                request,
                parent
        );

        parent.setUpdatedBy(authenticatedUserId);
    }

    private void mapParentFields(
            StudentParentRequest request,
            ErpParent parent
    ) {
        parent.setFatherName(
                trimToNull(request.fatherName())
        );
        parent.setFatherUin(
                uppercaseToNull(request.fatherUin())
        );
        parent.setFatherPhone(
                trimToNull(request.fatherPhone())
        );
        parent.setFatherAlternatePhone(
                trimToNull(request.fatherAlternatePhone())
        );
        parent.setFatherEmail(
                normalizeEmail(request.fatherEmail())
        );
        parent.setFatherOccupation(
                trimToNull(request.fatherOccupation())
        );
        parent.setFatherEmployer(
                trimToNull(request.fatherEmployer())
        );
        parent.setFatherDesignation(
                trimToNull(request.fatherDesignation())
        );
        parent.setFatherAnnualIncome(
                request.fatherAnnualIncome()
        );

        parent.setMotherName(
                trimToNull(request.motherName())
        );
        parent.setMotherUin(
                uppercaseToNull(request.motherUin())
        );
        parent.setMotherPhone(
                trimToNull(request.motherPhone())
        );
        parent.setMotherAlternatePhone(
                trimToNull(request.motherAlternatePhone())
        );
        parent.setMotherEmail(
                normalizeEmail(request.motherEmail())
        );
        parent.setMotherOccupation(
                trimToNull(request.motherOccupation())
        );
        parent.setMotherEmployer(
                trimToNull(request.motherEmployer())
        );
        parent.setMotherDesignation(
                trimToNull(request.motherDesignation())
        );
        parent.setMotherAnnualIncome(
                request.motherAnnualIncome()
        );

        parent.setGuardianName(
                trimToNull(request.guardianName())
        );
        parent.setGuardianUin(
                uppercaseToNull(request.guardianUin())
        );
        parent.setGuardianRelationship(
                trimToNull(request.guardianRelationship())
        );
        parent.setGuardianPhone(
                trimToNull(request.guardianPhone())
        );
        parent.setGuardianAlternatePhone(
                trimToNull(request.guardianAlternatePhone())
        );
        parent.setGuardianEmail(
                normalizeEmail(request.guardianEmail())
        );
        parent.setGuardianOccupation(
                trimToNull(request.guardianOccupation())
        );

        parent.setPreferredContact(
                request.preferredContact()
        );
        parent.setFeeResponsibility(
                request.feeResponsibility()
        );
        parent.setParentsLivingTogether(
                request.parentsLivingTogether()
        );

        parent.setEmergencyContactName(
                trimToNull(request.emergencyContactName())
        );
        parent.setEmergencyContactPhone(
                trimToNull(request.emergencyContactPhone())
        );
        parent.setEmergencyContactRelationship(
                trimToNull(request.emergencyContactRelationship())
        );

        parent.setRemarks(
                trimToNull(request.remarks())
        );
    }

    // =====================================================================
    // ENROLLMENT REQUEST -> ENTITY
    // =====================================================================

    public ErpStudentEnrollment toNewEnrollment(
            StudentEnrollmentRequest request,
            ErpStudent student,
            Branch branch,
            Long authenticatedUserId
    ) {
        Objects.requireNonNull(
                request,
                "Student enrollment request is required."
        );
        Objects.requireNonNull(
                student,
                "Student entity is required."
        );
        Objects.requireNonNull(
                branch,
                "Student branch is required."
        );

        ErpStudentEnrollment enrollment =
                new ErpStudentEnrollment();

        enrollment.setStudent(student);
        enrollment.setBranch(branch);
        enrollment.setAdmissionNo(
                student.getAdmissionNo()
        );

        enrollment.setAcademicYearId(
                request.academicYearId()
        );
        enrollment.setClassId(
                request.classId()
        );
        enrollment.setSectionId(
                request.sectionId()
        );
        enrollment.setRollNo(
                trimToNull(request.rollNo())
        );

        enrollment.setAdmissionType(
                request.admissionType()
        );
        enrollment.setPromotionType(
                ErpStudentEnrollment.PromotionType.NEW
        );
        enrollment.setEnrollmentStatus(
                ErpStudentEnrollment.EnrollmentStatus.ACTIVE
        );

        enrollment.setJoiningDate(
                request.joiningDate()
        );
        enrollment.setLeavingDate(null);
        enrollment.setIsLocked(false);
        enrollment.setActive(true);
        enrollment.setRemarks(
                trimToNull(request.remarks())
        );
        enrollment.setCreatedBy(
                authenticatedUserId
        );

        student.setCurrentEnrollment(
                enrollment
        );

        return enrollment;
    }

    /**
     * Creates a history snapshot before modifying the current enrollment.
     */
    public ErpStudentEnrollmentHistory toEnrollmentHistory(
            ErpStudentEnrollment enrollment,
            StudentEnrollmentUpdateRequest request,
            Long authenticatedUserId
    ) {
        Objects.requireNonNull(
                enrollment,
                "Current enrollment is required."
        );
        Objects.requireNonNull(
                request,
                "Enrollment update request is required."
        );

        ErpStudentEnrollmentHistory history =
                copyEnrollmentToHistory(
                        enrollment,
                        authenticatedUserId
                );

        history.setEffectiveDate(
                request.effectiveDate()
        );
        history.setChangeReason(
                trimRequired(request.changeReason())
        );

        return history;
    }

    /**
     * Creates the initial enrollment-history record after Student creation.
     */
    public ErpStudentEnrollmentHistory toInitialEnrollmentHistory(
            ErpStudentEnrollment enrollment,
            Long authenticatedUserId
    ) {
        Objects.requireNonNull(
                enrollment,
                "Initial enrollment is required."
        );

        ErpStudentEnrollmentHistory history =
                copyEnrollmentToHistory(
                        enrollment,
                        authenticatedUserId
                );

        history.setEffectiveDate(
                enrollment.getJoiningDate()
        );
        history.setChangeReason(
                "INITIAL_ENROLLMENT"
        );

        return history;
    }

    private ErpStudentEnrollmentHistory copyEnrollmentToHistory(
            ErpStudentEnrollment enrollment,
            Long authenticatedUserId
    ) {
        ErpStudentEnrollmentHistory history =
                new ErpStudentEnrollmentHistory();

        history.setStudent(
                enrollment.getStudent()
        );
        history.setEnrollment(
                enrollment
        );
        history.setBranch(
                enrollment.getBranch()
        );
        history.setAdmissionNo(
                enrollment.getAdmissionNo()
        );

        history.setAcademicYearId(
                enrollment.getAcademicYearId()
        );
        history.setClassId(
                enrollment.getClassId()
        );
        history.setSectionId(
                enrollment.getSectionId()
        );
        history.setStreamId(
                enrollment.getStreamId()
        );
        history.setHouseId(
                enrollment.getHouseId()
        );
        history.setHostelId(
                enrollment.getHostelId()
        );
        history.setBedId(
                enrollment.getBedId()
        );
        history.setRollNo(
                enrollment.getRollNo()
        );

        history.setAdmissionType(
                enumName(enrollment.getAdmissionType())
        );
        history.setPromotionType(
                enumName(enrollment.getPromotionType())
        );
        history.setEnrollmentStatus(
                enumName(enrollment.getEnrollmentStatus())
        );

        history.setJoiningDate(
                enrollment.getJoiningDate()
        );
        history.setLeavingDate(
                enrollment.getLeavingDate()
        );

        history.setRemarks(
                enrollment.getRemarks()
        );
        history.setApprovedBy(
                enrollment.getApprovedBy()
        );
        history.setApprovedAt(
                enrollment.getApprovedAt()
        );
        history.setCreatedBy(
                authenticatedUserId
        );

        return history;
    }

    public void updateEnrollment(
            StudentEnrollmentUpdateRequest request,
            ErpStudentEnrollment enrollment
    ) {
        Objects.requireNonNull(
                request,
                "Enrollment update request is required."
        );
        Objects.requireNonNull(
                enrollment,
                "Enrollment entity is required."
        );

        enrollment.setAcademicYearId(
                request.academicYearId()
        );
        enrollment.setClassId(
                request.classId()
        );
        enrollment.setSectionId(
                request.sectionId()
        );
        enrollment.setRollNo(
                trimToNull(request.rollNo())
        );
        enrollment.setPromotionType(
                request.promotionType()
        );
        enrollment.setEnrollmentStatus(
                request.enrollmentStatus()
        );
        enrollment.setLeavingDate(
                request.leavingDate()
        );
        enrollment.setRemarks(
                trimToNull(request.remarks())
        );

        enrollment.setActive(
                isActiveEnrollmentStatus(
                        request.enrollmentStatus()
                )
        );
    }

    // =====================================================================
    // MEDICAL REQUEST -> ENTITY
    // =====================================================================

    public ErpStudentMedical toNewMedical(
            StudentMedicalRequest request,
            ErpStudent student,
            Branch branch,
            Long authenticatedUserId
    ) {
        Objects.requireNonNull(
                request,
                "Student medical request is required."
        );
        Objects.requireNonNull(
                student,
                "Student entity is required."
        );
        Objects.requireNonNull(
                branch,
                "Student branch is required."
        );

        ErpStudentMedical medical =
                new ErpStudentMedical();

        medical.setStudent(student);
        medical.setBranch(branch);
        medical.setAdmissionNo(
                student.getAdmissionNo()
        );

        mapMedicalFields(
                request,
                medical
        );

        medical.setActive(true);
        medical.setCreatedBy(authenticatedUserId);
        medical.setUpdatedBy(authenticatedUserId);

        return medical;
    }

    public void updateMedical(
            StudentMedicalRequest request,
            ErpStudentMedical medical,
            Long authenticatedUserId
    ) {
        Objects.requireNonNull(
                request,
                "Student medical request is required."
        );
        Objects.requireNonNull(
                medical,
                "Student medical entity is required."
        );

        mapMedicalFields(
                request,
                medical
        );

        medical.setUpdatedBy(authenticatedUserId);
    }

    private void mapMedicalFields(
            StudentMedicalRequest request,
            ErpStudentMedical medical
    ) {
        medical.setBloodGroup(
                request.bloodGroup() != null
                        ? request.bloodGroup()
                        : ErpStudentMedical.BloodGroup.UNKNOWN
        );
        medical.setHeightCm(
                request.heightCm()
        );
        medical.setWeightKg(
                request.weightKg()
        );
        medical.setAllergies(
                trimToNull(request.allergies())
        );
        medical.setChronicConditions(
                trimToNull(request.chronicConditions())
        );
        medical.setOngoingMedication(
                trimToNull(request.ongoingMedication())
        );
        medical.setSpecialNeeds(
                trimToNull(request.specialNeeds())
        );
        medical.setFitForSports(
                request.fitForSports() != null
                        ? request.fitForSports()
                        : true
        );
        medical.setEmergencyDoctorName(
                trimToNull(request.emergencyDoctorName())
        );
        medical.setEmergencyDoctorMobile(
                trimToNull(request.emergencyDoctorMobile())
        );
        medical.setPreferredHospital(
                trimToNull(request.preferredHospital())
        );
        medical.setRemarks(
                trimToNull(request.remarks())
        );
    }

    public boolean hasMedicalData(
            StudentMedicalRequest request
    ) {
        if (request == null) {
            return false;
        }

        return request.bloodGroup() != null
                || request.heightCm() != null
                || request.weightKg() != null
                || hasText(request.allergies())
                || hasText(request.chronicConditions())
                || hasText(request.ongoingMedication())
                || hasText(request.specialNeeds())
                || request.fitForSports() != null
                || hasText(request.emergencyDoctorName())
                || hasText(request.emergencyDoctorMobile())
                || hasText(request.preferredHospital())
                || hasText(request.remarks());
    }

    // =====================================================================
    // ACADEMIC HISTORY REQUEST -> ENTITY
    // =====================================================================

    public ErpStudentAcademicHistory toNewAcademicHistory(
            StudentAcademicHistoryRequest request,
            ErpStudent student,
            Branch branch,
            Long authenticatedUserId
    ) {
        Objects.requireNonNull(
                request,
                "Academic-history request is required."
        );
        Objects.requireNonNull(
                student,
                "Student entity is required."
        );
        Objects.requireNonNull(
                branch,
                "Student branch is required."
        );

        ErpStudentAcademicHistory history =
                new ErpStudentAcademicHistory();

        history.setStudent(student);
        history.setBranch(branch);
        history.setAdmissionNo(
                student.getAdmissionNo()
        );

        mapAcademicHistoryFields(
                request,
                history
        );

        history.setVerificationStatus(
                ErpStudentAcademicHistory.VerificationStatus.PENDING
        );
        history.setActive(true);
        history.setCreatedBy(authenticatedUserId);

        student.setAcademicHistory(
                history
        );

        return history;
    }

    public void updateAcademicHistory(
            StudentAcademicHistoryRequest request,
            ErpStudentAcademicHistory history
    ) {
        Objects.requireNonNull(
                request,
                "Academic-history request is required."
        );
        Objects.requireNonNull(
                history,
                "Academic-history entity is required."
        );

        mapAcademicHistoryFields(
                request,
                history
        );
    }

    private void mapAcademicHistoryFields(
            StudentAcademicHistoryRequest request,
            ErpStudentAcademicHistory history
    ) {
        history.setFormerSchoolName(
                trimToNull(request.formerSchoolName())
        );
        history.setFormerSchoolCode(
                uppercaseToNull(request.formerSchoolCode())
        );
        history.setFormerSchoolLin(
                uppercaseToNull(request.formerSchoolLin())
        );
        history.setFormerSchoolAddress(
                trimToNull(request.formerSchoolAddress())
        );

        if (request.schoolType() != null) {
            history.setSchoolType(
                    request.schoolType()
            );
        } else if (history.getSchoolType() == null) {
            history.setSchoolType(
                    ErpStudentAcademicHistory.SchoolType.PRIVATE
            );
        }

        history.setTransferReason(
                trimToNull(request.transferReason())
        );
        history.setPreviousAcademicYear(
                trimToNull(request.previousAcademicYear())
        );
        history.setPreviousClass(
                trimToNull(request.previousClass())
        );
        history.setPreviousSection(
                trimToNull(request.previousSection())
        );
        history.setPreviousStream(
                trimToNull(request.previousStream())
        );

        history.setPleIndexNumber(
                uppercaseToNull(request.pleIndexNumber())
        );
        history.setPleAggregate(
                trimToNull(request.pleAggregate())
        );
        history.setUceIndexNumber(
                uppercaseToNull(request.uceIndexNumber())
        );
        history.setUceResult(
                trimToNull(request.uceResult())
        );
        history.setUaceIndexNumber(
                uppercaseToNull(request.uaceIndexNumber())
        );
        history.setUaceResult(
                trimToNull(request.uaceResult())
        );
        history.setSubjectMarks(
                trimToNull(request.subjectMarks())
        );
        history.setRemarks(
                trimToNull(request.remarks())
        );
    }

    public boolean hasAcademicHistoryData(
            StudentAcademicHistoryRequest request
    ) {
        if (request == null) {
            return false;
        }

        return hasText(request.formerSchoolName())
                || hasText(request.formerSchoolCode())
                || hasText(request.formerSchoolLin())
                || hasText(request.formerSchoolAddress())
                || request.schoolType() != null
                || hasText(request.transferReason())
                || hasText(request.previousAcademicYear())
                || hasText(request.previousClass())
                || hasText(request.previousSection())
                || hasText(request.previousStream())
                || hasText(request.pleIndexNumber())
                || hasText(request.pleAggregate())
                || hasText(request.uceIndexNumber())
                || hasText(request.uceResult())
                || hasText(request.uaceIndexNumber())
                || hasText(request.uaceResult())
                || hasText(request.subjectMarks())
                || hasText(request.remarks());
    }


    // =====================================================================
    // HOSTEL REQUEST -> ENTITY
    // =====================================================================

    public ErpStudentHostel toNewHostel(
            StudentHostelRequest request,
            ErpStudent student,
            Branch branch,
            String academicYear,
            Long authenticatedUserId
    ) {
        Objects.requireNonNull(
                request,
                "Student hostel request is required."
        );
        Objects.requireNonNull(
                student,
                "Student entity is required."
        );
        Objects.requireNonNull(
                branch,
                "Student branch is required."
        );

        ErpStudentHostel hostel =
                new ErpStudentHostel();

        hostel.setStudent(student);
        hostel.setBranch(branch);
        hostel.setAdmissionNo(
                student.getAdmissionNo()
        );
        hostel.setAcademicYear(
                trimRequired(academicYear)
        );

        hostel.setHostelId(
                request.hostelId()
        );
        hostel.setRoomId(
                request.roomId()
        );
        hostel.setBedId(
                request.bedId()
        );

        hostel.setAllocationStartDate(
                request.allocationStartDate()
        );
        hostel.setAllocationEndDate(
                request.allocationEndDate()
        );

        hostel.setAllocationStatus(
                ErpStudentHostel.AllocationStatus.ACTIVE
        );
        hostel.setPaymentStatus(
                ErpStudentHostel.PaymentStatus.PENDING
        );

        hostel.setLocalGuardianName(
                trimToNull(request.localGuardianName())
        );
        hostel.setLocalGuardianMobile(
                trimToNull(request.localGuardianMobile())
        );
        hostel.setLocalGuardianRelation(
                trimToNull(request.localGuardianRelation())
        );
        hostel.setRemarks(
                trimToNull(request.remarks())
        );

        hostel.setActive(true);
        hostel.setCreatedBy(
                authenticatedUserId
        );
        hostel.setUpdatedBy(
                authenticatedUserId
        );

        return hostel;
    }

    // =====================================================================
    // TRANSPORT REQUEST -> ENTITY
    // =====================================================================

    public ErpStudentTransport toNewTransport(
            StudentTransportRequest request,
            ErpStudent student,
            Branch branch,
            String academicYear,
            Long authenticatedUserId
    ) {
        Objects.requireNonNull(
                request,
                "Student transport request is required."
        );
        Objects.requireNonNull(
                student,
                "Student entity is required."
        );
        Objects.requireNonNull(
                branch,
                "Student branch is required."
        );

        ErpStudentTransport transport =
                new ErpStudentTransport();

        transport.setStudent(student);
        transport.setBranch(branch);
        transport.setAdmissionNo(
                student.getAdmissionNo()
        );
        transport.setAcademicYear(
                trimRequired(academicYear)
        );

        transport.setRouteId(
                request.routeId()
        );
        transport.setVehicleId(
                request.vehicleId()
        );
        transport.setPickupPointId(
                request.pickupPointId()
        );

        transport.setTransportStartDate(
                request.transportStartDate()
        );
        transport.setTransportEndDate(
                request.transportEndDate()
        );

        transport.setSeatNumber(
                trimToNull(request.seatNumber())
        );
        transport.setEmergencyContact(
                trimToNull(request.emergencyContact())
        );
        transport.setEmergencyMobile(
                trimToNull(request.emergencyMobile())
        );
        transport.setRemarks(
                trimToNull(request.remarks())
        );

        transport.setTransportStatus(
                ErpStudentTransport.TransportStatus.ACTIVE
        );
        transport.setPaymentStatus(
                ErpStudentTransport.PaymentStatus.PENDING
        );

        transport.setActive(true);
        transport.setCreatedBy(
                authenticatedUserId
        );
        transport.setUpdatedBy(
                authenticatedUserId
        );

        return transport;
    }

    // =====================================================================
    // DOCUMENT REQUEST -> ENTITY
    // =====================================================================

    public ErpStudentDocument toNewDocument(
            StudentDocumentMetadataRequest request,
            ErpStudent student,
            Branch branch,
            String storedFileName,
            String originalFileName,
            String storedFilePath,
            String fileExtension,
            String mimeType,
            Long fileSize,
            Long authenticatedUserId
    ) {
        Objects.requireNonNull(
                request,
                "Document metadata request is required."
        );
        Objects.requireNonNull(
                student,
                "Student entity is required."
        );
        Objects.requireNonNull(
                branch,
                "Student branch is required."
        );

        ErpStudentDocument document =
                new ErpStudentDocument();

        document.setStudent(student);
        document.setBranch(branch);
        document.setAdmissionNo(
                student.getAdmissionNo()
        );

        document.setDocumentType(
                request.documentType().name()
        );
        document.setDocumentName(
                trimRequired(request.documentName())
        );
        document.setDocumentNumber(
                trimToNull(request.documentNumber())
        );

        document.setFileName(
                trimRequired(storedFileName)
        );
        document.setOriginalFileName(
                trimToNull(originalFileName)
        );
        document.setFilePath(
                trimRequired(storedFilePath)
        );
        document.setFileExtension(
                lowercaseToNull(fileExtension)
        );
        document.setMimeType(
                lowercaseToNull(mimeType)
        );
        document.setFileSize(fileSize);

        document.setDocumentStatus(
                ErpStudentDocument.DocumentStatus.PENDING
        );
        document.setRemarks(
                trimToNull(request.remarks())
        );
        document.setActive(true);
        document.setUploadedBy(authenticatedUserId);
        document.setCreatedBy(authenticatedUserId);

        student.addDocument(document);

        return document;
    }

    public void applyDocumentVerification(
            StudentDocumentVerificationRequest request,
            ErpStudentDocument document,
            Long authenticatedUserId
    ) {
        Objects.requireNonNull(
                request,
                "Document verification request is required."
        );
        Objects.requireNonNull(
                document,
                "Student document is required."
        );

        document.setDocumentStatus(
                request.documentStatus()
        );
        document.setRemarks(
                trimToNull(request.remarks())
        );
        document.setVerifiedBy(
                authenticatedUserId
        );
        document.setVerifiedAt(
                LocalDateTime.now()
        );
    }

    // =====================================================================
    // ENTITY -> RESPONSE
    // =====================================================================

    public StudentCreateResponse toCreateResponse(
            ErpStudent student,
            ErpStudentEnrollment enrollment
    ) {
        Objects.requireNonNull(
                student,
                "Student entity is required."
        );
        Objects.requireNonNull(
                enrollment,
                "Enrollment entity is required."
        );

        return new StudentCreateResponse(
                student.getStudentId(),
                enrollment.getEnrollmentId(),
                student.getAdmissionNo(),
                student.getStudentCode(),
                student.getFullName(),
                branchId(student.getBranch()),
                enrollment.getAcademicYearId(),
                enrollment.getClassId(),
                enrollment.getSectionId(),
                enrollment.getRollNo(),
                student.getStudentStatus(),
                enumName(enrollment.getEnrollmentStatus()),
                student.getVersion(),
                student.getCreatedAt()
        );
    }

    public StudentPersonalResponse toPersonalResponse(
            ErpStudent student
    ) {
        Objects.requireNonNull(
                student,
                "Student entity is required."
        );

        return new StudentPersonalResponse(
                student.getStudentId(),
                student.getApplication() != null
                        ? student.getApplication().getApplicationId()
                        : null,
                branchId(student.getBranch()),
                branchCode(student.getBranch()),
                branchName(student.getBranch()),
                student.getAdmissionNo(),
                student.getStudentCode(),
                student.getLearnerLin(),
                student.getAdmissionYear(),
                student.getFirstName(),
                student.getMiddleName(),
                student.getLastName(),
                student.getFullName(),
                student.getGender(),
                student.getDateOfBirth(),
                student.getNationality(),
                student.getHouseNo(),
                student.getStreet(),
                student.getVillage(),
                student.getTownCity(),
                student.getDistrict(),
                student.getState(),
                student.getCountry(),
                student.getPostalCode(),
                studentPhotoUrl(student),
                student.getStudentStatus(),
                student.getActive(),
                student.getVersion(),
                student.getCreatedAt(),
                student.getUpdatedAt()
        );
    }

    public StudentParentResponse toParentResponse(
            ErpParent parent
    ) {
        if (parent == null) {
            return null;
        }

        return new StudentParentResponse(
                parent.getParentId(),
                studentId(parent.getStudent()),
                branchId(parent.getBranch()),
                parent.getAdmissionNo(),

                parent.getFatherName(),
                parent.getFatherUin(),
                parent.getFatherPhone(),
                parent.getFatherAlternatePhone(),
                parent.getFatherEmail(),
                parent.getFatherOccupation(),
                parent.getFatherEmployer(),
                parent.getFatherDesignation(),
                parent.getFatherAnnualIncome(),

                parent.getMotherName(),
                parent.getMotherUin(),
                parent.getMotherPhone(),
                parent.getMotherAlternatePhone(),
                parent.getMotherEmail(),
                parent.getMotherOccupation(),
                parent.getMotherEmployer(),
                parent.getMotherDesignation(),
                parent.getMotherAnnualIncome(),

                parent.getGuardianName(),
                parent.getGuardianUin(),
                parent.getGuardianRelationship(),
                parent.getGuardianPhone(),
                parent.getGuardianAlternatePhone(),
                parent.getGuardianEmail(),
                parent.getGuardianOccupation(),

                enumName(parent.getPreferredContact()),
                enumName(parent.getFeeResponsibility()),
                parent.getParentsLivingTogether(),

                parent.getEmergencyContactName(),
                parent.getEmergencyContactPhone(),
                parent.getEmergencyContactRelationship(),

                parent.getRemarks(),
                parent.getActive(),
                parent.getVersion(),
                parent.getCreatedAt(),
                parent.getUpdatedAt()
        );
    }

    public StudentEnrollmentResponse toEnrollmentResponse(
            ErpStudentEnrollment enrollment,
            String academicYearName,
            String className,
            String sectionName,
            String streamName,
            String classTeacherName
    ) {
        if (enrollment == null) {
            return null;
        }

        return new StudentEnrollmentResponse(
                enrollment.getEnrollmentId(),
                studentId(enrollment.getStudent()),
                enrollment.getAdmissionNo(),
                branchId(enrollment.getBranch()),
                branchName(enrollment.getBranch()),
                enrollment.getAcademicYearId(),
                trimToNull(academicYearName),
                enrollment.getClassId(),
                trimToNull(className),
                enrollment.getSectionId(),
                trimToNull(sectionName),
                enrollment.getStreamId(),
                trimToNull(streamName),
                enrollment.getHouseId(),
                enrollment.getHostelId(),
                enrollment.getBedId(),
                enrollment.getRollNo(),
                enumName(enrollment.getAdmissionType()),
                enumName(enrollment.getPromotionType()),
                enumName(enrollment.getEnrollmentStatus()),
                enrollment.getJoiningDate(),
                enrollment.getLeavingDate(),
                enrollment.getClassTeacherId(),
                trimToNull(classTeacherName),
                enrollment.getFeeStructureId(),
                enrollment.getScholarshipId(),
                enrollment.getApprovedBy(),
                enrollment.getApprovedAt(),
                enrollment.getIsLocked(),
                enrollment.getActive(),
                enrollment.getRemarks(),
                enrollment.getVersion(),
                enrollment.getCreatedAt(),
                enrollment.getUpdatedAt()
        );
    }

    public StudentEnrollmentHistoryResponse toEnrollmentHistoryResponse(
            ErpStudentEnrollmentHistory history,
            String academicYearName,
            String className,
            String sectionName,
            String streamName,
            String approvedByName,
            String createdByName
    ) {
        Objects.requireNonNull(
                history,
                "Enrollment history entity is required."
        );

        return new StudentEnrollmentHistoryResponse(
                history.getEnrollmentHistoryId(),
                studentId(history.getStudent()),
                history.getEnrollment() != null
                        ? history.getEnrollment().getEnrollmentId()
                        : null,
                branchId(history.getBranch()),
                branchName(history.getBranch()),
                history.getAdmissionNo(),
                history.getAcademicYearId(),
                trimToNull(academicYearName),
                history.getClassId(),
                trimToNull(className),
                history.getSectionId(),
                trimToNull(sectionName),
                history.getStreamId(),
                trimToNull(streamName),
                history.getHouseId(),
                history.getHostelId(),
                history.getBedId(),
                history.getRollNo(),
                history.getAdmissionType(),
                history.getPromotionType(),
                history.getEnrollmentStatus(),
                history.getJoiningDate(),
                history.getLeavingDate(),
                history.getEffectiveDate(),
                history.getChangeReason(),
                history.getRemarks(),
                history.getApprovedBy(),
                trimToNull(approvedByName),
                history.getApprovedAt(),
                history.getCreatedBy(),
                trimToNull(createdByName),
                history.getCreatedAt()
        );
    }

    public StudentMedicalResponse toMedicalResponse(
            ErpStudentMedical medical
    ) {
        if (medical == null) {
            return null;
        }

        return new StudentMedicalResponse(
                medical.getMedicalId(),
                studentId(medical.getStudent()),
                branchId(medical.getBranch()),
                medical.getAdmissionNo(),
                enumName(medical.getBloodGroup()),
                medical.getBloodGroup() != null
                        ? medical.getBloodGroup().getCode()
                        : null,
                medical.getHeightCm(),
                medical.getWeightKg(),
                medical.getAllergies(),
                medical.getChronicConditions(),
                medical.getOngoingMedication(),
                medical.getSpecialNeeds(),
                medical.getFitForSports(),
                medical.getEmergencyDoctorName(),
                medical.getEmergencyDoctorMobile(),
                medical.getPreferredHospital(),
                medical.getRemarks(),
                medical.getActive(),
                medical.getVersion(),
                medical.getCreatedAt(),
                medical.getUpdatedAt()
        );
    }

    public StudentAcademicHistoryResponse toAcademicHistoryResponse(
            ErpStudentAcademicHistory history,
            String verifiedByName
    ) {
        if (history == null) {
            return null;
        }

        Long studentId =
                studentId(history.getStudent());

        return new StudentAcademicHistoryResponse(
                history.getAcademicHistoryId(),
                studentId,
                branchId(history.getBranch()),
                history.getAdmissionNo(),

                history.getFormerSchoolName(),
                history.getFormerSchoolCode(),
                history.getFormerSchoolLin(),
                history.getFormerSchoolAddress(),
                enumName(history.getSchoolType()),
                history.getTransferReason(),

                history.getPreviousAcademicYear(),
                history.getPreviousClass(),
                history.getPreviousSection(),
                history.getPreviousStream(),

                history.getPleIndexNumber(),
                history.getPleAggregate(),

                history.getUceIndexNumber(),
                history.getUceResult(),

                history.getUaceIndexNumber(),
                history.getUaceResult(),

                history.getSubjectMarks(),

                protectedAcademicDocumentUrl(
                        studentId,
                        history.getPreviousReportCard(),
                        "report-card"
                ),
                protectedAcademicDocumentUrl(
                        studentId,
                        history.getTransferCertificate(),
                        "transfer-certificate"
                ),
                protectedAcademicDocumentUrl(
                        studentId,
                        history.getLeavingCertificate(),
                        "leaving-certificate"
                ),

                enumName(history.getVerificationStatus()),
                history.getVerifiedBy(),
                trimToNull(verifiedByName),
                history.getVerifiedAt(),

                history.getActive(),
                history.getRemarks(),
                history.getVersion(),
                history.getCreatedAt(),
                history.getUpdatedAt()
        );
    }


    public StudentHostelResponse toHostelResponse(
            ErpStudentHostel hostel,
            String hostelName,
            String roomName,
            String bedName
    ) {
        if (hostel == null) {
            return null;
        }

        return new StudentHostelResponse(
                hostel.getHostelAllocationId(),
                studentId(hostel.getStudent()),
                branchId(hostel.getBranch()),
                branchName(hostel.getBranch()),
                hostel.getAdmissionNo(),
                hostel.getAcademicYear(),
                hostel.getHostelId(),
                trimToNull(hostelName),
                hostel.getRoomId(),
                trimToNull(roomName),
                hostel.getBedId(),
                trimToNull(bedName),
                hostel.getAllocationStartDate(),
                hostel.getAllocationEndDate(),
                hostel.getMonthlyFee(),
                hostel.getAnnualFee(),
                hostel.getDiscountAmount(),
                hostel.getPayableAmount(),
                enumName(hostel.getAllocationStatus()),
                enumName(hostel.getPaymentStatus()),
                hostel.getLocalGuardianName(),
                hostel.getLocalGuardianMobile(),
                hostel.getLocalGuardianRelation(),
                hostel.getRemarks(),
                hostel.getActive(),
                hostel.getVersion(),
                hostel.getCreatedAt(),
                hostel.getUpdatedAt()
        );
    }

    public StudentTransportResponse toTransportResponse(
            ErpStudentTransport transport,
            String routeName,
            String vehicleNumber,
            String pickupPointName
    ) {
        if (transport == null) {
            return null;
        }

        return new StudentTransportResponse(
                transport.getTransportId(),
                studentId(transport.getStudent()),
                branchId(transport.getBranch()),
                branchName(transport.getBranch()),
                transport.getAdmissionNo(),
                transport.getAcademicYear(),
                transport.getRouteId(),
                trimToNull(routeName),
                transport.getVehicleId(),
                trimToNull(vehicleNumber),
                transport.getPickupPointId(),
                trimToNull(pickupPointName),
                transport.getTransportStartDate(),
                transport.getTransportEndDate(),
                transport.getSeatNumber(),
                transport.getMonthlyFee(),
                transport.getAnnualFee(),
                transport.getDiscountAmount(),
                transport.getPayableAmount(),
                enumName(transport.getTransportStatus()),
                enumName(transport.getPaymentStatus()),
                transport.getEmergencyContact(),
                transport.getEmergencyMobile(),
                transport.getRemarks(),
                transport.getActive(),
                transport.getVersion(),
                transport.getCreatedAt(),
                transport.getUpdatedAt()
        );
    }

    public StudentDocumentResponse toDocumentResponse(
            ErpStudentDocument document,
            String uploadedByName,
            String verifiedByName
    ) {
        Objects.requireNonNull(
                document,
                "Student document entity is required."
        );

        Long studentId =
                studentId(document.getStudent());

        return new StudentDocumentResponse(
                document.getDocumentId(),
                studentId,
                document.getAdmissionNo(),
                branchId(document.getBranch()),
                document.getDocumentType(),
                document.getDocumentName(),
                document.getDocumentNumber(),
                document.getOriginalFileName(),
                document.getFileExtension(),
                document.getMimeType(),
                document.getFileSize(),
                enumName(document.getDocumentStatus()),
                document.getRemarks(),
                studentDocumentUrl(
                        studentId,
                        document.getDocumentId()
                ),
                document.getUploadedBy(),
                trimToNull(uploadedByName),
                document.getUploadedAt(),
                document.getVerifiedBy(),
                trimToNull(verifiedByName),
                document.getVerifiedAt(),
                document.getActive(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    public StudentSummaryResponse toSummaryResponse(
            ErpStudent student,
            ErpStudentEnrollment enrollment,
            ErpParent parent,
            String academicYearName,
            String className,
            String sectionName
    ) {
        Objects.requireNonNull(
                student,
                "Student entity is required."
        );

        PreferredContactView preferredContact =
                resolvePreferredContact(parent);

        return new StudentSummaryResponse(
                student.getStudentId(),
                student.getStudentCode(),
                student.getAdmissionNo(),
                student.getLearnerLin(),
                student.getFullName(),
                student.getGender(),
                student.getAdmissionYear(),
                studentPhotoUrl(student),
                branchId(student.getBranch()),
                branchName(student.getBranch()),
                enrollment != null
                        ? enrollment.getAcademicYearId()
                        : null,
                trimToNull(academicYearName),
                enrollment != null
                        ? enrollment.getClassId()
                        : null,
                trimToNull(className),
                enrollment != null
                        ? enrollment.getSectionId()
                        : null,
                trimToNull(sectionName),
                enrollment != null
                        ? enrollment.getRollNo()
                        : null,
                preferredContact.type(),
                preferredContact.name(),
                preferredContact.phone(),
                student.getStudentStatus(),
                enrollment != null
                        ? enumName(enrollment.getEnrollmentStatus())
                        : null,
                student.getActive(),
                student.getVersion()
        );
    }

    public StudentProfileResponse toProfileResponse(
            StudentPersonalResponse personal,
            StudentParentResponse parent,
            StudentEnrollmentResponse currentEnrollment,
            List<StudentEnrollmentHistoryResponse> enrollmentHistory,
            StudentMedicalResponse medical,
            StudentAcademicHistoryResponse academicHistory,
            StudentHostelResponse hostel,
            StudentTransportResponse transport,
            List<StudentDocumentResponse> documents
    ) {
        Objects.requireNonNull(
                personal,
                "Student personal response is required."
        );

        return new StudentProfileResponse(
                personal,
                parent,
                currentEnrollment,
                enrollmentHistory,
                medical,
                academicHistory,
                hostel,
                transport,
                documents
        );
    }

    public PagedStudentResponse toPagedResponse(
            Page<StudentSummaryResponse> page
    ) {
        Objects.requireNonNull(
                page,
                "Student page is required."
        );

        return new PagedStudentResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );
    }

    // =====================================================================
    // PRIVATE RESPONSE HELPERS
    // =====================================================================

    private PreferredContactView resolvePreferredContact(
            ErpParent parent
    ) {
        if (parent == null
                || parent.getPreferredContact() == null) {
            return PreferredContactView.empty();
        }

        return switch (parent.getPreferredContact()) {
            case FATHER -> new PreferredContactView(
                    ErpParent.PreferredContact.FATHER.name(),
                    parent.getFatherName(),
                    parent.getFatherPhone()
            );
            case MOTHER -> new PreferredContactView(
                    ErpParent.PreferredContact.MOTHER.name(),
                    parent.getMotherName(),
                    parent.getMotherPhone()
            );
            case GUARDIAN -> new PreferredContactView(
                    ErpParent.PreferredContact.GUARDIAN.name(),
                    parent.getGuardianName(),
                    parent.getGuardianPhone()
            );
        };
    }

    private String studentPhotoUrl(
            ErpStudent student
    ) {
        if (student == null
                || student.getStudentId() == null
                || !hasText(student.getPhotoPath())) {
            return null;
        }

        return "/api/students/"
                + student.getStudentId()
                + "/photo";
    }

    private String studentDocumentUrl(
            Long studentId,
            Long documentId
    ) {
        if (studentId == null || documentId == null) {
            return null;
        }

        return "/api/students/"
                + studentId
                + "/documents/"
                + documentId
                + "/download";
    }

    private String protectedAcademicDocumentUrl(
            Long studentId,
            String storedPath,
            String documentType
    ) {
        if (studentId == null
                || !hasText(storedPath)
                || !hasText(documentType)) {
            return null;
        }

        return "/api/students/"
                + studentId
                + "/academic-history/documents/"
                + documentType;
    }

    private static boolean isActiveStudentStatus(
            StudentStatus status
    ) {
        return status == StudentStatus.ACTIVE
                || status == StudentStatus.SUSPENDED;
    }

    private static boolean isActiveEnrollmentStatus(
            ErpStudentEnrollment.EnrollmentStatus status
    ) {
        return status == ErpStudentEnrollment.EnrollmentStatus.ACTIVE
                || status == ErpStudentEnrollment.EnrollmentStatus.SUSPENDED;
    }

    private static Integer branchId(
            Branch branch
    ) {
        return branch != null
                ? branch.getBranchId()
                : null;
    }

    private static String branchName(
            Branch branch
    ) {
        return branch != null
                ? branch.getBranchName()
                : null;
    }

    private static String branchCode(
            Branch branch
    ) {
        return branch != null
                ? branch.getSchoolCode()
                : null;
    }

    private static Long studentId(
            ErpStudent student
    ) {
        return student != null
                ? student.getStudentId()
                : null;
    }

    private static String enumName(
            Enum<?> value
    ) {
        return value != null
                ? value.name()
                : null;
    }

    // =====================================================================
    // STRING NORMALIZATION
    // =====================================================================

    private static String trimRequired(
            String value
    ) {
        String normalized =
                trimToNull(value);

        if (normalized == null) {
            throw new IllegalArgumentException(
                    "Required text value cannot be blank."
            );
        }

        return normalized;
    }

    private static String trimToNull(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String trimmed =
                value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }

    private static String normalizeEmail(
            String value
    ) {
        String normalized =
                trimToNull(value);

        return normalized != null
                ? normalized.toLowerCase(Locale.ROOT)
                : null;
    }

    private static String uppercaseToNull(
            String value
    ) {
        String normalized =
                trimToNull(value);

        return normalized != null
                ? normalized.toUpperCase(Locale.ROOT)
                : null;
    }

    private static String lowercaseToNull(
            String value
    ) {
        String normalized =
                trimToNull(value);

        return normalized != null
                ? normalized.toLowerCase(Locale.ROOT)
                : null;
    }

    private static String buildFullName(
            String firstName,
            String middleName,
            String lastName
    ) {
        return java.util.stream.Stream.of(
                        trimToNull(firstName),
                        trimToNull(middleName),
                        trimToNull(lastName)
                )
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.joining(" "));
    }

    private static boolean hasText(
            String value
    ) {
        return value != null
                && !value.isBlank();
    }

    private record PreferredContactView(
            String type,
            String name,
            String phone
    ) {

        private static PreferredContactView empty() {
            return new PreferredContactView(
                    null,
                    null,
                    null
            );
        }
    }
}