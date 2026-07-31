package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.BranchNotAssignedException;
import com.erp.montfortuganda.exception.DuplicateResourceException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.school.dto.SectionRequest;
import com.erp.montfortuganda.school.dto.SectionResponse;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import com.erp.montfortuganda.school.entity.ErpSection;
import com.erp.montfortuganda.school.entity.SchoolClass;
import com.erp.montfortuganda.school.repository.AcademicYearRepository;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.school.repository.SchoolClassRepository;
import com.erp.montfortuganda.school.repository.SectionRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Branch-safe Section service implementation.
 *
 * <p>The authenticated user's branch is the only source of Section ownership.
 * The selected Academic Year must belong to that same branch. Classes remain
 * global master data and must be active before they can be assigned.</p>
 */
@Service
@Transactional(readOnly = true)
public class SectionServiceImpl
        implements SectionService {

    private final SectionRepository sectionRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final BranchRepository branchRepository;
    private final CurrentUserService currentUserService;

    public SectionServiceImpl(
            SectionRepository sectionRepository,
            AcademicYearRepository academicYearRepository,
            SchoolClassRepository schoolClassRepository,
            BranchRepository branchRepository,
            CurrentUserService currentUserService
    ) {
        this.sectionRepository = sectionRepository;
        this.academicYearRepository = academicYearRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.branchRepository = branchRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional
    public SectionResponse createSection(
            SectionRequest request
    ) {
        requireRequest(request);

        UserBranchContext context =
                requireAuthenticatedBranch();

        ErpAcademicYear academicYear =
                requireOwnedAcademicYear(
                        request.academicYearId(),
                        context.branchId()
                );

        validateAcademicYearAvailable(
                academicYear
        );

        SchoolClass schoolClass =
                requireActiveClass(
                        request.classId()
                );

        String sectionCode =
                normalizeCode(
                        request.sectionCode()
                );

        validateDuplicateSectionCode(
                context.branchId(),
                academicYear.getAcademicYearId(),
                schoolClass.getClassId(),
                sectionCode,
                null
        );

        ErpSection section =
                new ErpSection();

        section.setBranch(
                context.branch()
        );

        section.setAcademicYear(
                academicYear
        );

        section.setSchoolClass(
                schoolClass
        );

        applyRequest(
                section,
                request,
                sectionCode
        );

        section.setCreatedBy(
                context.userId()
        );

        section.setUpdatedBy(
                context.userId()
        );

        return SectionResponse.fromEntity(
                sectionRepository.saveAndFlush(
                        section
                )
        );
    }

    @Override
    @Transactional
    public SectionResponse updateSection(
            Long sectionId,
            SectionRequest request
    ) {
        requireRequest(request);

        UserBranchContext context =
                requireAuthenticatedBranch();

        ErpSection section =
                requireOwnedSection(
                        sectionId,
                        context.branchId()
                );

        validateVersion(
                section,
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

        SchoolClass schoolClass =
                requireActiveClass(
                        request.classId()
                );

        String sectionCode =
                normalizeCode(
                        request.sectionCode()
                );

        validateDuplicateSectionCode(
                context.branchId(),
                academicYear.getAcademicYearId(),
                schoolClass.getClassId(),
                sectionCode,
                section.getSectionId()
        );

        section.setBranch(
                context.branch()
        );

        section.setAcademicYear(
                academicYear
        );

        section.setSchoolClass(
                schoolClass
        );

        applyRequest(
                section,
                request,
                sectionCode
        );

        section.setUpdatedBy(
                context.userId()
        );

        try {
            return SectionResponse.fromEntity(
                    sectionRepository.saveAndFlush(
                            section
                    )
            );
        } catch (OptimisticLockException exception) {
            throw new BadRequestException(
                    "Section was changed by another user. "
                            + "Refresh the page and try again."
            );
        }
    }

    @Override
    public SectionResponse getSection(
            Long sectionId
    ) {
        UserBranchContext context =
                requireAuthenticatedBranch();

        return SectionResponse.fromEntity(
                requireOwnedSection(
                        sectionId,
                        context.branchId()
                )
        );
    }

    @Override
    public List<SectionResponse> getSections() {
        UserBranchContext context =
                requireAuthenticatedBranch();

        return sectionRepository
                .findAllByBranchBranchIdOrderBySchoolClassDisplayOrderAscSectionCodeAsc(
                        context.branchId()
                )
                .stream()
                .map(
                        SectionResponse::fromEntity
                )
                .toList();
    }

    @Override
    public List<SectionResponse> getActiveSections() {
        UserBranchContext context =
                requireAuthenticatedBranch();

        return sectionRepository
                .findAllByBranchBranchIdAndActiveTrueOrderBySchoolClassDisplayOrderAscSectionCodeAsc(
                        context.branchId()
                )
                .stream()
                .map(
                        SectionResponse::fromEntity
                )
                .toList();
    }

    @Override
    public List<SectionResponse> getSectionsByAcademicYear(
            Long academicYearId
    ) {
        UserBranchContext context =
                requireAuthenticatedBranch();

        requireOwnedAcademicYear(
                academicYearId,
                context.branchId()
        );

        return sectionRepository
                .findAllByBranchBranchIdAndAcademicYearAcademicYearIdOrderBySchoolClassDisplayOrderAscSectionCodeAsc(
                        context.branchId(),
                        academicYearId
                )
                .stream()
                .map(
                        SectionResponse::fromEntity
                )
                .toList();
    }

    @Override
    public List<SectionResponse> getActiveSectionsByAcademicYear(
            Long academicYearId
    ) {
        UserBranchContext context =
                requireAuthenticatedBranch();

        requireOwnedAcademicYear(
                academicYearId,
                context.branchId()
        );

        return sectionRepository
                .findAllConsistentActiveByBranchAndAcademicYear(
                        context.branchId(),
                        academicYearId
                )
                .stream()
                .map(
                        SectionResponse::fromEntity
                )
                .toList();
    }

    @Override
    public List<SectionResponse> getSectionsByAcademicYearAndClass(
            Long academicYearId,
            Integer classId
    ) {
        UserBranchContext context =
                requireAuthenticatedBranch();

        requireOwnedAcademicYear(
                academicYearId,
                context.branchId()
        );

        requireClass(
                classId
        );

        return sectionRepository
                .findAllByBranchBranchIdAndAcademicYearAcademicYearIdAndSchoolClassClassIdOrderBySectionCodeAsc(
                        context.branchId(),
                        academicYearId,
                        classId
                )
                .stream()
                .map(
                        SectionResponse::fromEntity
                )
                .toList();
    }

    @Override
    public List<SectionResponse> getActiveSectionsByAcademicYearAndClass(
            Long academicYearId,
            Integer classId
    ) {
        UserBranchContext context =
                requireAuthenticatedBranch();

        requireOwnedAcademicYear(
                academicYearId,
                context.branchId()
        );

        requireActiveClass(
                classId
        );

        return sectionRepository
                .findAllConsistentActiveByBranchYearAndClass(
                        context.branchId(),
                        academicYearId,
                        classId
                )
                .stream()
                .map(
                        SectionResponse::fromEntity
                )
                .toList();
    }

    @Override
    public List<SectionResponse> getSectionsByStatus(
            ErpSection.Status status
    ) {
        if (status == null) {
            throw new BadRequestException(
                    "Section status is required."
            );
        }

        return getSections()
                .stream()
                .filter(
                        section ->
                                status.equals(
                                        section.status()
                                )
                )
                .toList();
    }

    @Override
    @Transactional
    public SectionResponse changeSectionActiveStatus(
            Long sectionId,
            boolean active,
            Long version
    ) {
        UserBranchContext context =
                requireAuthenticatedBranch();

        ErpSection section =
                requireOwnedSection(
                        sectionId,
                        context.branchId()
                );

        validateVersion(
                section,
                version
        );

        if (active) {
            validateAcademicYearAvailable(
                    section.getAcademicYear()
            );

            requireActiveClass(
                    section.getSchoolClass()
                            .getClassId()
            );

            section.setActive(
                    true
            );

            section.setStatus(
                    ErpSection.Status.ACTIVE
            );
        } else {
            section.setActive(
                    false
            );

            section.setStatus(
                    ErpSection.Status.INACTIVE
            );
        }

        section.setUpdatedBy(
                context.userId()
        );

        try {
            return SectionResponse.fromEntity(
                    sectionRepository.saveAndFlush(
                            section
                    )
            );
        } catch (OptimisticLockException exception) {
            throw new BadRequestException(
                    "Section was changed by another user. "
                            + "Refresh the page and try again."
            );
        }
    }

    private void applyRequest(
            ErpSection section,
            SectionRequest request,
            String normalizedCode
    ) {
        section.setSectionCode(
                normalizedCode
        );

        section.setSectionName(
                normalizeRequiredText(
                        request.sectionName(),
                        "Section name"
                )
        );

        section.setCapacity(
                request.capacity()
        );

        section.setDescription(
                normalizeOptionalText(
                        request.description()
                )
        );

        section.setStatus(
                request.status()
        );

        section.setActive(
                request.active() == null
                        || request.active()
        );

        if (
                ErpSection.Status.INACTIVE.equals(
                        section.getStatus()
                )
        ) {
            section.setActive(
                    false
            );
        }

        if (
                !Boolean.TRUE.equals(
                        section.getActive()
                )
        ) {
            section.setStatus(
                    ErpSection.Status.INACTIVE
            );
        }
    }

    private void validateDuplicateSectionCode(
            Integer branchId,
            Long academicYearId,
            Integer classId,
            String sectionCode,
            Long excludedSectionId
    ) {
        boolean duplicate =
                excludedSectionId == null
                        ? sectionRepository
                        .existsByBranchBranchIdAndAcademicYearAcademicYearIdAndSchoolClassClassIdAndSectionCodeIgnoreCase(
                                branchId,
                                academicYearId,
                                classId,
                                sectionCode
                        )
                        : sectionRepository
                        .existsByBranchBranchIdAndAcademicYearAcademicYearIdAndSchoolClassClassIdAndSectionCodeIgnoreCaseAndSectionIdNot(
                                branchId,
                                academicYearId,
                                classId,
                                sectionCode,
                                excludedSectionId
                        );

        if (duplicate) {
            throw new DuplicateResourceException(
                    "Section code '" + sectionCode
                            + "' already exists for the selected "
                            + "Academic Year and Class in this branch."
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

    private void validateAcademicYearAvailable(
            ErpAcademicYear academicYear
    ) {
        if (
                academicYear == null
                        || !Boolean.TRUE.equals(
                        academicYear.getActive()
                )
        ) {
            throw new BadRequestException(
                    "Sections cannot be managed under "
                            + "an inactive Academic Year."
            );
        }

        if (
                ErpAcademicYear.Status.CLOSED.equals(
                        academicYear.getStatus()
                )
        ) {
            throw new BadRequestException(
                    "Sections cannot be managed under "
                            + "a closed Academic Year."
            );
        }
    }

    private SchoolClass requireClass(
            Integer classId
    ) {
        if (
                classId == null
                        || classId <= 0
        ) {
            throw new BadRequestException(
                    "A valid Class ID is required."
            );
        }

        return schoolClassRepository
                .findById(
                        classId
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Selected Class was not found."
                        )
                );
    }

    private SchoolClass requireActiveClass(
            Integer classId
    ) {
        SchoolClass schoolClass =
                requireClass(
                        classId
                );

        if (
                schoolClass.getStatus() == null
                        || schoolClass.getStatus() != 1
        ) {
            throw new BadRequestException(
                    "Selected Class is inactive."
            );
        }

        return schoolClass;
    }

    private ErpSection requireOwnedSection(
            Long sectionId,
            Integer branchId
    ) {
        if (
                sectionId == null
                        || sectionId <= 0
        ) {
            throw new BadRequestException(
                    "A valid Section ID is required."
            );
        }

        ErpSection section =
                sectionRepository
                        .findBySectionIdAndBranchBranchId(
                                sectionId,
                                branchId
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Section was not found in "
                                                + "the authenticated branch."
                                )
                        );

        Integer academicYearBranchId =
                section.getAcademicYear() == null
                        || section.getAcademicYear()
                        .getBranch() == null
                        ? null
                        : section.getAcademicYear()
                        .getBranch()
                        .getBranchId();

        if (
                !Objects.equals(
                        branchId,
                        academicYearBranchId
                )
        ) {
            throw new BadRequestException(
                    "Section Academic Year ownership is inconsistent "
                            + "with the authenticated branch."
            );
        }

        return section;
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
            ErpSection section,
            Long requestVersion
    ) {
        if (requestVersion == null) {
            throw new BadRequestException(
                    "Section version is required. "
                            + "Refresh the page and try again."
            );
        }

        if (
                !Objects.equals(
                        section.getVersion(),
                        requestVersion
                )
        ) {
            throw new BadRequestException(
                    "Section was changed by another user. "
                            + "Refresh the page and try again."
            );
        }
    }

    private void requireRequest(
            SectionRequest request
    ) {
        if (request == null) {
            throw new BadRequestException(
                    "Section information is required."
            );
        }
    }

    private String normalizeCode(
            String value
    ) {
        return normalizeRequiredText(
                value,
                "Section code"
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