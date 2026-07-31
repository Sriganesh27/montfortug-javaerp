package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.BranchNotAssignedException;
import com.erp.montfortuganda.exception.DuplicateResourceException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.school.dto.AcademicTermRequest;
import com.erp.montfortuganda.school.dto.AcademicTermResponse;
import com.erp.montfortuganda.school.entity.ErpAcademicTerm;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import com.erp.montfortuganda.school.repository.AcademicTermRepository;
import com.erp.montfortuganda.school.repository.AcademicYearRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Branch-safe Academic Term service implementation.
 *
 * <p>Academic Terms inherit branch ownership through their parent Academic
 * Year. Every read and write operation verifies that the parent Academic Year
 * belongs to the authenticated branch.</p>
 */
@Service
@Transactional(readOnly = true)
public class AcademicTermServiceImpl
        implements AcademicTermService {

    private final AcademicTermRepository academicTermRepository;
    private final AcademicYearRepository academicYearRepository;
    private final CurrentUserService currentUserService;

    public AcademicTermServiceImpl(
            AcademicTermRepository academicTermRepository,
            AcademicYearRepository academicYearRepository,
            CurrentUserService currentUserService
    ) {
        this.academicTermRepository = academicTermRepository;
        this.academicYearRepository = academicYearRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional
    public AcademicTermResponse createAcademicTerm(
            AcademicTermRequest request
    ) {
        requireRequest(request);

        UserContext context =
                requireAuthenticatedBranch();

        ErpAcademicYear academicYear =
                requireOwnedAcademicYear(
                        request.academicYearId(),
                        context.branchId()
                );

        validateAcademicYearAvailable(
                academicYear
        );

        String termCode =
                normalizeCode(
                        request.termCode()
                );

        validateDuplicateCode(
                academicYear.getAcademicYearId(),
                termCode,
                null
        );

        validateDisplayOrder(
                academicYear.getAcademicYearId(),
                request.displayOrder(),
                null
        );

        validateTermDates(
                academicYear,
                request
        );

        validateDateOverlap(
                academicYear.getAcademicYearId(),
                null,
                request
        );

        ErpAcademicTerm academicTerm =
                new ErpAcademicTerm();

        academicTerm.setAcademicYear(
                academicYear
        );

        applyRequest(
                academicTerm,
                request,
                termCode
        );

        academicTerm.setCreatedBy(
                context.userId()
        );

        academicTerm.setUpdatedBy(
                context.userId()
        );

        boolean makeCurrent =
                Boolean.TRUE.equals(
                        academicTerm.getCurrentTerm()
                );

        if (makeCurrent) {
            academicTermRepository
                    .findCurrentTermForUpdate(
                            academicYear.getAcademicYearId()
                    );

            academicTerm.setCurrentTerm(
                    false
            );
        }

        ErpAcademicTerm saved =
                academicTermRepository.saveAndFlush(
                        academicTerm
                );

        if (makeCurrent) {
            academicTermRepository
                    .clearCurrentTermForOtherRecords(
                            academicYear.getAcademicYearId(),
                            saved.getTermId(),
                            context.userId()
                    );

            saved.setCurrentTerm(
                    true
            );

            saved.setUpdatedBy(
                    context.userId()
            );

            saved =
                    academicTermRepository.saveAndFlush(
                            saved
                    );
        }

        return AcademicTermResponse.fromEntity(
                saved
        );
    }

    @Override
    @Transactional
    public AcademicTermResponse updateAcademicTerm(
            Long termId,
            AcademicTermRequest request
    ) {
        requireRequest(request);

        UserContext context =
                requireAuthenticatedBranch();

        ErpAcademicTerm academicTerm =
                requireOwnedAcademicTerm(
                        termId,
                        context.branchId()
                );

        validateVersion(
                academicTerm,
                request.version()
        );

        ErpAcademicYear academicYear =
                requireOwnedAcademicYear(
                        request.academicYearId(),
                        context.branchId()
                );

        validateAcademicYearAvailable(
                academicYear
        );

        String termCode =
                normalizeCode(
                        request.termCode()
                );

        validateDuplicateCode(
                academicYear.getAcademicYearId(),
                termCode,
                academicTerm.getTermId()
        );

        validateDisplayOrder(
                academicYear.getAcademicYearId(),
                request.displayOrder(),
                academicTerm.getTermId()
        );

        validateTermDates(
                academicYear,
                request
        );

        validateDateOverlap(
                academicYear.getAcademicYearId(),
                academicTerm.getTermId(),
                request
        );

        boolean makeCurrent =
                Boolean.TRUE.equals(
                        request.currentTerm()
                );

        if (makeCurrent) {
            academicTermRepository
                    .findCurrentTermForUpdate(
                            academicYear.getAcademicYearId()
                    );

            academicTermRepository
                    .clearCurrentTermForOtherRecords(
                            academicYear.getAcademicYearId(),
                            academicTerm.getTermId(),
                            context.userId()
                    );
        }

        academicTerm.setAcademicYear(
                academicYear
        );

        applyRequest(
                academicTerm,
                request,
                termCode
        );

        if (
                !Boolean.TRUE.equals(
                        academicTerm.getActive()
                )
        ) {
            academicTerm.setCurrentTerm(
                    false
            );
        }

        academicTerm.setUpdatedBy(
                context.userId()
        );

        try {
            return AcademicTermResponse.fromEntity(
                    academicTermRepository.saveAndFlush(
                            academicTerm
                    )
            );
        } catch (OptimisticLockException exception) {
            throw new BadRequestException(
                    "Academic Term was changed by another user. "
                            + "Refresh the page and try again."
            );
        }
    }

    @Override
    public AcademicTermResponse getAcademicTerm(
            Long termId
    ) {
        UserContext context =
                requireAuthenticatedBranch();

        return AcademicTermResponse.fromEntity(
                requireOwnedAcademicTerm(
                        termId,
                        context.branchId()
                )
        );
    }

    @Override
    public List<AcademicTermResponse> getAcademicTerms(
            Long academicYearId
    ) {
        UserContext context =
                requireAuthenticatedBranch();

        requireOwnedAcademicYear(
                academicYearId,
                context.branchId()
        );

        return academicTermRepository
                .findAllByBranchAndAcademicYear(
                        context.branchId(),
                        academicYearId
                )
                .stream()
                .map(
                        AcademicTermResponse::fromEntity
                )
                .toList();
    }

    @Override
    public List<AcademicTermResponse> getActiveAcademicTerms(
            Long academicYearId
    ) {
        UserContext context =
                requireAuthenticatedBranch();

        requireOwnedAcademicYear(
                academicYearId,
                context.branchId()
        );

        return academicTermRepository
                .findAllActiveByBranchAndAcademicYear(
                        context.branchId(),
                        academicYearId
                )
                .stream()
                .map(
                        AcademicTermResponse::fromEntity
                )
                .toList();
    }

    @Override
    public List<AcademicTermResponse> getAcademicTermsByStatus(
            Long academicYearId,
            ErpAcademicTerm.Status status
    ) {
        if (status == null) {
            throw new BadRequestException(
                    "Academic Term status is required."
            );
        }

        return getAcademicTerms(
                academicYearId
        )
                .stream()
                .filter(
                        academicTerm ->
                                status.equals(
                                        academicTerm.status()
                                )
                )
                .toList();
    }

    @Override
    public AcademicTermResponse getCurrentAcademicTerm() {
        UserContext context =
                requireAuthenticatedBranch();

        ErpAcademicTerm academicTerm =
                academicTermRepository
                        .findCurrentTermByBranchId(
                                context.branchId()
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "No current active Academic Term "
                                                + "is configured for this branch."
                                )
                        );

        return AcademicTermResponse.fromEntity(
                academicTerm
        );
    }

    @Override
    @Transactional
    public AcademicTermResponse makeCurrentAcademicTerm(
            Long termId
    ) {
        UserContext context =
                requireAuthenticatedBranch();

        ErpAcademicTerm academicTerm =
                requireOwnedAcademicTerm(
                        termId,
                        context.branchId()
                );

        if (
                !Boolean.TRUE.equals(
                        academicTerm.getActive()
                )
        ) {
            throw new BadRequestException(
                    "An inactive Academic Term cannot be made current."
            );
        }

        if (
                ErpAcademicTerm.Status.CLOSED.equals(
                        academicTerm.getStatus()
                )
        ) {
            throw new BadRequestException(
                    "A closed Academic Term cannot be made current."
            );
        }

        ErpAcademicYear academicYear =
                academicTerm.getAcademicYear();

        validateAcademicYearAvailable(
                academicYear
        );

        academicTermRepository
                .findCurrentTermForUpdate(
                        academicYear.getAcademicYearId()
                );

        academicTermRepository
                .clearCurrentTermForOtherRecords(
                        academicYear.getAcademicYearId(),
                        academicTerm.getTermId(),
                        context.userId()
                );

        academicTerm.setCurrentTerm(
                true
        );

        academicTerm.setUpdatedBy(
                context.userId()
        );

        return AcademicTermResponse.fromEntity(
                academicTermRepository.saveAndFlush(
                        academicTerm
                )
        );
    }

    @Override
    @Transactional
    public AcademicTermResponse changeAcademicTermActiveStatus(
            Long termId,
            boolean active,
            Long version
    ) {
        UserContext context =
                requireAuthenticatedBranch();

        ErpAcademicTerm academicTerm =
                requireOwnedAcademicTerm(
                        termId,
                        context.branchId()
                );

        validateVersion(
                academicTerm,
                version
        );

        academicTerm.setActive(
                active
        );

        if (!active) {
            academicTerm.setCurrentTerm(
                    false
            );
        }

        academicTerm.setUpdatedBy(
                context.userId()
        );

        return AcademicTermResponse.fromEntity(
                academicTermRepository.saveAndFlush(
                        academicTerm
                )
        );
    }

    private void applyRequest(
            ErpAcademicTerm academicTerm,
            AcademicTermRequest request,
            String normalizedCode
    ) {
        academicTerm.setTermCode(
                normalizedCode
        );

        academicTerm.setTermName(
                normalizeRequiredText(
                        request.termName(),
                        "Term name"
                )
        );

        academicTerm.setStartDate(
                request.startDate()
        );

        academicTerm.setEndDate(
                request.endDate()
        );

        academicTerm.setDisplayOrder(
                request.displayOrder()
        );

        academicTerm.setStatus(
                request.status()
        );

        academicTerm.setCurrentTerm(
                Boolean.TRUE.equals(
                        request.currentTerm()
                )
        );

        academicTerm.setDescription(
                normalizeOptionalText(
                        request.description()
                )
        );

        academicTerm.setActive(
                request.active() == null
                        || request.active()
        );

        if (
                !Boolean.TRUE.equals(
                        academicTerm.getActive()
                )
        ) {
            academicTerm.setCurrentTerm(
                    false
            );
        }
    }

    private void validateAcademicYearAvailable(
            ErpAcademicYear academicYear
    ) {
        if (
                !Boolean.TRUE.equals(
                        academicYear.getActive()
                )
        ) {
            throw new BadRequestException(
                    "An Academic Term cannot be managed under "
                            + "an inactive Academic Year."
            );
        }

        if (
                ErpAcademicYear.Status.CLOSED.equals(
                        academicYear.getStatus()
                )
        ) {
            throw new BadRequestException(
                    "An Academic Term cannot be managed under "
                            + "a closed Academic Year."
            );
        }
    }

    private void validateTermDates(
            ErpAcademicYear academicYear,
            AcademicTermRequest request
    ) {
        if (
                request.startDate() == null
                        || request.endDate() == null
        ) {
            return;
        }

        if (
                request.startDate().isBefore(
                        academicYear.getStartDate()
                )
                        || request.endDate().isAfter(
                        academicYear.getEndDate()
                )
        ) {
            throw new BadRequestException(
                    "Academic Term dates must fall within "
                            + "the selected Academic Year."
            );
        }
    }

    private void validateDuplicateCode(
            Long academicYearId,
            String termCode,
            Long excludedTermId
    ) {
        boolean duplicate =
                excludedTermId == null
                        ? academicTermRepository
                        .existsByAcademicYearAcademicYearIdAndTermCodeIgnoreCase(
                                academicYearId,
                                termCode
                        )
                        : academicTermRepository
                        .existsByAcademicYearAcademicYearIdAndTermCodeIgnoreCaseAndTermIdNot(
                                academicYearId,
                                termCode,
                                excludedTermId
                        );

        if (duplicate) {
            throw new DuplicateResourceException(
                    "Term code '" + termCode
                            + "' already exists in this Academic Year."
            );
        }
    }

    private void validateDisplayOrder(
            Long academicYearId,
            Integer displayOrder,
            Long excludedTermId
    ) {
        boolean duplicate =
                excludedTermId == null
                        ? academicTermRepository
                        .existsByAcademicYearAcademicYearIdAndDisplayOrder(
                                academicYearId,
                                displayOrder
                        )
                        : academicTermRepository
                        .existsByAcademicYearAcademicYearIdAndDisplayOrderAndTermIdNot(
                                academicYearId,
                                displayOrder,
                                excludedTermId
                        );

        if (duplicate) {
            throw new DuplicateResourceException(
                    "Display order " + displayOrder
                            + " is already used in this Academic Year."
            );
        }
    }

    private void validateDateOverlap(
            Long academicYearId,
            Long excludedTermId,
            AcademicTermRequest request
    ) {
        boolean active =
                request.active() == null
                        || request.active();

        if (!active) {
            return;
        }

        boolean overlaps =
                excludedTermId == null
                        ? academicTermRepository
                        .existsActiveDateOverlap(
                                academicYearId,
                                request.startDate(),
                                request.endDate()
                        )
                        : academicTermRepository
                        .existsActiveDateOverlapExcludingId(
                                academicYearId,
                                excludedTermId,
                                request.startDate(),
                                request.endDate()
                        );

        if (overlaps) {
            throw new BadRequestException(
                    "Academic Term dates overlap another active "
                            + "Term in this Academic Year."
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

    private ErpAcademicTerm requireOwnedAcademicTerm(
            Long termId,
            Integer branchId
    ) {
        if (
                termId == null
                        || termId <= 0
        ) {
            throw new BadRequestException(
                    "A valid Academic Term ID is required."
            );
        }

        return academicTermRepository
                .findByTermIdAndBranchId(
                        termId,
                        branchId
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Academic Term was not found in "
                                        + "the authenticated branch."
                        )
                );
    }

    private UserContext requireAuthenticatedBranch() {
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

        Long userId =
                currentUser.getUserId() == null
                        ? null
                        : currentUser.getUserId().longValue();

        return new UserContext(
                branchId,
                userId
        );
    }

    private void validateVersion(
            ErpAcademicTerm academicTerm,
            Long requestVersion
    ) {
        if (requestVersion == null) {
            throw new BadRequestException(
                    "Academic Term version is required. "
                            + "Refresh the page and try again."
            );
        }

        if (
                !Objects.equals(
                        academicTerm.getVersion(),
                        requestVersion
                )
        ) {
            throw new BadRequestException(
                    "Academic Term was changed by another user. "
                            + "Refresh the page and try again."
            );
        }
    }

    private void requireRequest(
            AcademicTermRequest request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Academic Term information is required."
            );
        }
    }

    private String normalizeCode(
            String value
    ) {
        return normalizeRequiredText(
                value,
                "Term code"
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

    private record UserContext(
            Integer branchId,
            Long userId
    ) {
    }
}