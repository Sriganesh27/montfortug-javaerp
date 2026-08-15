package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.BranchNotAssignedException;
import com.erp.montfortuganda.exception.DuplicateResourceException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.school.dto.SubjectRequestDTO;
import com.erp.montfortuganda.school.dto.SubjectResponseDTO;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.ErpSubject;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.school.repository.ErpSubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class SubjectServiceImpl implements SubjectService {

    private final ErpSubjectRepository subjectRepository;
    private final BranchRepository branchRepository;
    private final CurrentUserService currentUserService;

    public SubjectServiceImpl(
            ErpSubjectRepository subjectRepository,
            BranchRepository branchRepository,
            CurrentUserService currentUserService
    ) {
        this.subjectRepository = subjectRepository;
        this.branchRepository = branchRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional
    public SubjectResponseDTO createSubject(
            SubjectRequestDTO request
    ) {
        requireRequest(request);

        UserBranchContext context =
                requireAuthenticatedBranch();

        String subjectCode =
                normalizeCode(request.subjectCode());

        validateDuplicateCode(
                context.branchId(),
                subjectCode,
                null
        );

        ErpSubject subject =
                new ErpSubject();

        subject.setBranch(context.branch());

        applyRequest(
                subject,
                request,
                subjectCode
        );

        subject.setActive(true);
        subject.setCreatedBy(context.userId());
        subject.setUpdatedBy(context.userId());

        return toResponse(
                subjectRepository.saveAndFlush(subject)
        );
    }

    @Override
    @Transactional
    public SubjectResponseDTO updateSubject(
            Long subjectId,
            SubjectRequestDTO request
    ) {
        requireRequest(request);

        UserBranchContext context =
                requireAuthenticatedBranch();

        ErpSubject subject =
                requireOwnedSubject(
                        subjectId,
                        context.branchId()
                );

        String subjectCode =
                normalizeCode(request.subjectCode());

        validateDuplicateCode(
                context.branchId(),
                subjectCode,
                subject.getSubjectId()
        );

        applyRequest(
                subject,
                request,
                subjectCode
        );

        subject.setUpdatedBy(context.userId());

        return toResponse(
                subjectRepository.saveAndFlush(subject)
        );
    }

    @Override
    public SubjectResponseDTO getSubject(
            Long subjectId
    ) {
        UserBranchContext context =
                requireAuthenticatedBranch();

        return toResponse(
                requireOwnedSubject(
                        subjectId,
                        context.branchId()
                )
        );
    }

    @Override
    public List<SubjectResponseDTO> getSubjects() {
        UserBranchContext context =
                requireAuthenticatedBranch();

        return subjectRepository
                .findAllByBranch_BranchIdAndActiveTrueOrderByDisplayOrderAscSubjectNameAsc(
                        context.branchId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<SubjectResponseDTO> getActiveSubjects() {
        UserBranchContext context =
                requireAuthenticatedBranch();

        return subjectRepository
                .findAllByBranch_BranchIdAndActiveTrueAndStatusOrderByDisplayOrderAscSubjectNameAsc(
                        context.branchId(),
                        ErpSubject.Status.ACTIVE
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SubjectResponseDTO changeSubjectActiveStatus(
            Long subjectId,
            boolean active
    ) {
        UserBranchContext context =
                requireAuthenticatedBranch();

        ErpSubject subject =
                requireOwnedSubjectIncludingInactive(
                        subjectId,
                        context.branchId()
                );

        subject.setActive(active);

        if (!active) {
            subject.setStatus(
                    ErpSubject.Status.INACTIVE
            );
        }

        subject.setUpdatedBy(context.userId());

        return toResponse(
                subjectRepository.saveAndFlush(subject)
        );
    }

    private void applyRequest(
            ErpSubject subject,
            SubjectRequestDTO request,
            String normalizedCode
    ) {
        subject.setSubjectCode(normalizedCode);

        subject.setSubjectName(
                normalizeRequiredText(
                        request.subjectName(),
                        "Subject name"
                )
        );

        subject.setSubjectShortName(
                normalizeOptionalText(
                        request.subjectShortName()
                )
        );

        subject.setSubjectType(
                request.subjectType()
        );

        subject.setIsPractical(
                Boolean.TRUE.equals(
                        request.practical()
                )
        );

        subject.setDisplayOrder(
                request.displayOrder()
        );

        subject.setDescription(
                normalizeOptionalText(
                        request.description()
                )
        );

        subject.setStatus(
                request.status()
        );
    }

    private void validateDuplicateCode(
            Integer branchId,
            String subjectCode,
            Long excludedSubjectId
    ) {
        boolean duplicate =
                excludedSubjectId == null
                        ? subjectRepository
                        .existsByBranch_BranchIdAndSubjectCodeIgnoreCase(
                                branchId,
                                subjectCode
                        )
                        : subjectRepository
                        .existsByBranch_BranchIdAndSubjectCodeIgnoreCaseAndSubjectIdNot(
                                branchId,
                                subjectCode,
                                excludedSubjectId
                        );

        if (duplicate) {
            throw new DuplicateResourceException(
                    "Subject code '" + subjectCode
                            + "' already exists for this branch."
            );
        }
    }

    private ErpSubject requireOwnedSubject(
            Long subjectId,
            Integer branchId
    ) {
        if (
                subjectId == null
                        || subjectId <= 0
        ) {
            throw new BadRequestException(
                    "A valid Subject ID is required."
            );
        }

        return subjectRepository
                .findBySubjectIdAndBranch_BranchIdAndActiveTrue(
                        subjectId,
                        branchId
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Subject was not found in "
                                        + "the authenticated branch."
                        )
                );
    }

    private ErpSubject requireOwnedSubjectIncludingInactive(
            Long subjectId,
            Integer branchId
    ) {
        if (
                subjectId == null
                        || subjectId <= 0
        ) {
            throw new BadRequestException(
                    "A valid Subject ID is required."
            );
        }

        ErpSubject subject =
                subjectRepository
                        .findById(subjectId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Subject was not found."
                                )
                        );

        Integer subjectBranchId =
                subject.getBranch() == null
                        ? null
                        : subject.getBranch().getBranchId();

        if (!branchId.equals(subjectBranchId)) {
            throw new ResourceNotFoundException(
                    "Subject was not found."
            );
        }

        return subject;
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
                        .findById(branchId)
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

    private SubjectResponseDTO toResponse(
            ErpSubject subject
    ) {
        return new SubjectResponseDTO(
                subject.getSubjectId(),
                subject.getBranch() == null
                        ? null
                        : subject.getBranch().getBranchId(),
                subject.getSubjectCode(),
                subject.getSubjectName(),
                subject.getSubjectShortName(),
                subject.getSubjectType(),
                subject.getIsPractical(),
                subject.getDisplayOrder(),
                subject.getDescription(),
                subject.getStatus(),
                subject.getActive()
        );
    }

    private void requireRequest(
            SubjectRequestDTO request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Subject information is required."
            );
        }

        if (request.subjectType() == null) {
            throw new BadRequestException(
                    "Subject type is required."
            );
        }

        if (request.status() == null) {
            throw new BadRequestException(
                    "Subject status is required."
            );
        }

        if (
                request.displayOrder() == null
                        || request.displayOrder() < 1
        ) {
            throw new BadRequestException(
                    "Display order must be at least 1."
            );
        }
    }

    private String normalizeCode(
            String value
    ) {
        return normalizeRequiredText(
                value,
                "Subject code"
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
