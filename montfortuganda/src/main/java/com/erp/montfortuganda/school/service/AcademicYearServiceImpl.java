package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.BranchNotAssignedException;
import com.erp.montfortuganda.exception.DuplicateResourceException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.school.dto.AcademicYearRequest;
import com.erp.montfortuganda.school.dto.AcademicYearResponse;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import com.erp.montfortuganda.school.repository.AcademicYearRepository;
import com.erp.montfortuganda.school.repository.BranchRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Branch-scoped Academic Year service.
 *
 * <p>The authenticated user's assigned branch is the only source of branch
 * ownership. No browser-supplied branch value is accepted.</p>
 */
@Service
@Transactional(readOnly = true)
public class AcademicYearServiceImpl
        implements AcademicYearService {

    private final AcademicYearRepository academicYearRepository;
    private final BranchRepository branchRepository;
    private final CurrentUserService currentUserService;

    public AcademicYearServiceImpl(
            AcademicYearRepository academicYearRepository,
            BranchRepository branchRepository,
            CurrentUserService currentUserService
    ) {
        this.academicYearRepository = academicYearRepository;
        this.branchRepository = branchRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional
    public AcademicYearResponse createAcademicYear(
            AcademicYearRequest request
    ) {
        requireRequest(request);

        UserBranchContext context =
                requireAuthenticatedBranch();

        String code =
                normalizeCode(
                        request.academicYearCode()
                );

        validateDuplicateCode(
                context.branchId(),
                code,
                null
        );

        validateDateOverlap(
                context.branchId(),
                null,
                request
        );

        ErpAcademicYear academicYear =
                new ErpAcademicYear();

        academicYear.setBranch(
                context.branch()
        );

        applyRequest(
                academicYear,
                request,
                code
        );

        academicYear.setCreatedBy(
                context.userId()
        );

        academicYear.setUpdatedBy(
                context.userId()
        );

        boolean makeCurrent =
                Boolean.TRUE.equals(
                        academicYear.getCurrentYear()
                );

        if (makeCurrent) {
            academicYearRepository
                    .findCurrentYearForUpdate(
                            context.branchId()
                    );

            academicYear.setCurrentYear(
                    false
            );
        }

        ErpAcademicYear saved =
                academicYearRepository.saveAndFlush(
                        academicYear
                );

        if (makeCurrent) {
            academicYearRepository
                    .clearCurrentYearForOtherRecords(
                            context.branchId(),
                            saved.getAcademicYearId(),
                            context.userId()
                    );

            saved.setCurrentYear(
                    true
            );

            saved.setUpdatedBy(
                    context.userId()
            );

            saved =
                    academicYearRepository.saveAndFlush(
                            saved
                    );
        }

        return AcademicYearResponse.fromEntity(
                saved
        );
    }

    @Override
    @Transactional
    public AcademicYearResponse updateAcademicYear(
            Long academicYearId,
            AcademicYearRequest request
    ) {
        requireRequest(request);

        UserBranchContext context =
                requireAuthenticatedBranch();

        ErpAcademicYear academicYear =
                requireOwnedAcademicYear(
                        academicYearId,
                        context.branchId()
                );

        validateVersion(
                academicYear,
                request.version()
        );

        String code =
                normalizeCode(
                        request.academicYearCode()
                );

        validateDuplicateCode(
                context.branchId(),
                code,
                academicYear.getAcademicYearId()
        );

        validateDateOverlap(
                context.branchId(),
                academicYear.getAcademicYearId(),
                request
        );

        boolean makeCurrent =
                Boolean.TRUE.equals(
                        request.currentYear()
                );

        if (makeCurrent) {
            academicYearRepository
                    .findCurrentYearForUpdate(
                            context.branchId()
                    );

            academicYearRepository
                    .clearCurrentYearForOtherRecords(
                            context.branchId(),
                            academicYear.getAcademicYearId(),
                            context.userId()
                    );
        }

        applyRequest(
                academicYear,
                request,
                code
        );

        if (
                !Boolean.TRUE.equals(
                        academicYear.getActive()
                )
        ) {
            academicYear.setCurrentYear(
                    false
            );
        }

        academicYear.setUpdatedBy(
                context.userId()
        );

        try {
            return AcademicYearResponse.fromEntity(
                    academicYearRepository.saveAndFlush(
                            academicYear
                    )
            );
        } catch (OptimisticLockException exception) {
            throw new BadRequestException(
                    "Academic Year was changed by another user. "
                            + "Refresh the page and try again."
            );
        }
    }

    @Override
    public AcademicYearResponse getAcademicYear(
            Long academicYearId
    ) {
        UserBranchContext context =
                requireAuthenticatedBranch();

        return AcademicYearResponse.fromEntity(
                requireOwnedAcademicYear(
                        academicYearId,
                        context.branchId()
                )
        );
    }

    @Override
    public List<AcademicYearResponse> getAcademicYears() {
        UserBranchContext context =
                requireAuthenticatedBranch();

        return academicYearRepository
                .findAllByBranchBranchIdOrderByStartDateDesc(
                        context.branchId()
                )
                .stream()
                .map(
                        AcademicYearResponse::fromEntity
                )
                .toList();
    }

    @Override
    public List<AcademicYearResponse> getActiveAcademicYears() {
        UserBranchContext context =
                requireAuthenticatedBranch();

        return academicYearRepository
                .findAllByBranchBranchIdAndActiveTrueOrderByStartDateDesc(
                        context.branchId()
                )
                .stream()
                .map(
                        AcademicYearResponse::fromEntity
                )
                .toList();
    }

    @Override
    public List<AcademicYearResponse> getAcademicYearsByStatus(
            ErpAcademicYear.Status status
    ) {
        if (status == null) {
            throw new BadRequestException(
                    "Academic Year status is required."
            );
        }

        UserBranchContext context =
                requireAuthenticatedBranch();

        return academicYearRepository
                .findAllByBranchBranchIdOrderByStartDateDesc(
                        context.branchId()
                )
                .stream()
                .filter(
                        academicYear ->
                                status.equals(
                                        academicYear.getStatus()
                                )
                )
                .map(
                        AcademicYearResponse::fromEntity
                )
                .toList();
    }

    @Override
    public AcademicYearResponse getCurrentAcademicYear() {
        UserBranchContext context =
                requireAuthenticatedBranch();

        ErpAcademicYear academicYear =
                academicYearRepository
                        .findByBranchBranchIdAndCurrentYearTrueAndActiveTrue(
                                context.branchId()
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "No current active Academic Year "
                                                + "is configured for this branch."
                                )
                        );

        return AcademicYearResponse.fromEntity(
                academicYear
        );
    }

    @Override
    @Transactional
    public AcademicYearResponse makeCurrentAcademicYear(
            Long academicYearId
    ) {
        UserBranchContext context =
                requireAuthenticatedBranch();

        ErpAcademicYear academicYear =
                requireOwnedAcademicYear(
                        academicYearId,
                        context.branchId()
                );

        if (
                !Boolean.TRUE.equals(
                        academicYear.getActive()
                )
        ) {
            throw new BadRequestException(
                    "An inactive Academic Year cannot be made current."
            );
        }

        if (
                ErpAcademicYear.Status.CLOSED.equals(
                        academicYear.getStatus()
                )
        ) {
            throw new BadRequestException(
                    "A closed Academic Year cannot be made current."
            );
        }

        academicYearRepository
                .findCurrentYearForUpdate(
                        context.branchId()
                );

        academicYearRepository
                .clearCurrentYearForOtherRecords(
                        context.branchId(),
                        academicYear.getAcademicYearId(),
                        context.userId()
                );

        academicYear.setCurrentYear(
                true
        );

        academicYear.setUpdatedBy(
                context.userId()
        );

        return AcademicYearResponse.fromEntity(
                academicYearRepository.saveAndFlush(
                        academicYear
                )
        );
    }

    @Override
    @Transactional
    public AcademicYearResponse changeAcademicYearActiveStatus(
            Long academicYearId,
            boolean active,
            Long version
    ) {
        UserBranchContext context =
                requireAuthenticatedBranch();

        ErpAcademicYear academicYear =
                requireOwnedAcademicYear(
                        academicYearId,
                        context.branchId()
                );

        validateVersion(
                academicYear,
                version
        );

        academicYear.setActive(
                active
        );

        if (!active) {
            academicYear.setCurrentYear(
                    false
            );
        }

        academicYear.setUpdatedBy(
                context.userId()
        );

        return AcademicYearResponse.fromEntity(
                academicYearRepository.saveAndFlush(
                        academicYear
                )
        );
    }

    private void applyRequest(
            ErpAcademicYear academicYear,
            AcademicYearRequest request,
            String normalizedCode
    ) {
        academicYear.setAcademicYearCode(
                normalizedCode
        );

        academicYear.setAcademicYearName(
                normalizeRequiredText(
                        request.academicYearName(),
                        "Academic Year name"
                )
        );

        academicYear.setStartDate(
                request.startDate()
        );

        academicYear.setEndDate(
                request.endDate()
        );

        academicYear.setAdmissionStartDate(
                request.admissionStartDate()
        );

        academicYear.setAdmissionEndDate(
                request.admissionEndDate()
        );

        academicYear.setStatus(
                request.status()
        );

        academicYear.setCurrentYear(
                Boolean.TRUE.equals(
                        request.currentYear()
                )
        );

        academicYear.setDescription(
                normalizeOptionalText(
                        request.description()
                )
        );

        academicYear.setActive(
                request.active() == null
                        || request.active()
        );

        if (
                !Boolean.TRUE.equals(
                        academicYear.getActive()
                )
        ) {
            academicYear.setCurrentYear(
                    false
            );
        }
    }

    private void validateDuplicateCode(
            Integer branchId,
            String code,
            Long excludedAcademicYearId
    ) {
        boolean duplicate =
                excludedAcademicYearId == null
                        ? academicYearRepository
                        .existsByBranchBranchIdAndAcademicYearCodeIgnoreCase(
                                branchId,
                                code
                        )
                        : academicYearRepository
                        .existsByBranchBranchIdAndAcademicYearCodeIgnoreCaseAndAcademicYearIdNot(
                                branchId,
                                code,
                                excludedAcademicYearId
                        );

        if (duplicate) {
            throw new DuplicateResourceException(
                    "Academic Year code '" + code
                            + "' already exists for this branch."
            );
        }
    }

    private void validateDateOverlap(
            Integer branchId,
            Long excludedAcademicYearId,
            AcademicYearRequest request
    ) {
        boolean overlaps =
                excludedAcademicYearId == null
                        ? academicYearRepository
                        .existsActiveDateOverlap(
                                branchId,
                                request.startDate(),
                                request.endDate()
                        )
                        : academicYearRepository
                        .existsActiveDateOverlapExcludingId(
                                branchId,
                                excludedAcademicYearId,
                                request.startDate(),
                                request.endDate()
                        );

        if (
                overlaps
                        && (
                        request.active() == null
                                || request.active()
                )
        ) {
            throw new BadRequestException(
                    "Academic Year dates overlap another active "
                            + "Academic Year in this branch."
            );
        }
    }

    private ErpAcademicYear requireOwnedAcademicYear(
            Long academicYearId,
            Integer branchId
    ) {
        if (
                academicYearId == null
                        || academicYearId <= 0
        ) {
            throw new BadRequestException(
                    "A valid Academic Year ID is required."
            );
        }

        return academicYearRepository
                .findByAcademicYearIdAndBranchBranchId(
                        academicYearId,
                        branchId
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Academic Year was not found in "
                                        + "the authenticated branch."
                        )
                );
    }

    private UserBranchContext requireAuthenticatedBranch() {
        CurrentUserContext currentUser =
                currentUserService
                        .getCurrentUserContext();

        Integer branchId =
                currentUser.getBranchId();

        if (
                branchId == null
                        || branchId <= 0
        ) {
            throw new BranchNotAssignedException(
                    "Authenticated user is not assigned to a branch."
            );
        }

        Branch branch =
                branchRepository
                        .findById(
                                branchId
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Authenticated branch was not found."
                                )
                        );

        if (
                branch.getIsActive() == null
                        || branch.getIsActive() != 1
        ) {
            throw new BadRequestException(
                    "Authenticated branch is inactive."
            );
        }

        Long userId =
                currentUser.getUserId() == null
                        ? null
                        : currentUser.getUserId().longValue();

        return new UserBranchContext(
                branchId,
                branch,
                userId
        );
    }

    private void validateVersion(
            ErpAcademicYear academicYear,
            Long requestVersion
    ) {
        if (requestVersion == null) {
            throw new BadRequestException(
                    "Academic Year version is required. "
                            + "Refresh the page and try again."
            );
        }

        if (
                !Objects.equals(
                        academicYear.getVersion(),
                        requestVersion
                )
        ) {
            throw new BadRequestException(
                    "Academic Year was changed by another user. "
                            + "Refresh the page and try again."
            );
        }
    }

    private void requireRequest(
            AcademicYearRequest request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Academic Year information is required."
            );
        }
    }

    private String normalizeCode(
            String value
    ) {
        return normalizeRequiredText(
                value,
                "Academic Year code"
        ).toUpperCase(
                Locale.ROOT
        );
    }

    private String normalizeRequiredText(
            String value,
            String fieldName
    ) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(
                    fieldName + " is required."
            );
        }

        return value.trim();
    }

    private String normalizeOptionalText(
            String value
    ) {
        return StringUtils.hasText(value)
                ? value.trim()
                : null;
    }

    private record UserBranchContext(
            Integer branchId,
            Branch branch,
            Long userId
    ) {
    }
}