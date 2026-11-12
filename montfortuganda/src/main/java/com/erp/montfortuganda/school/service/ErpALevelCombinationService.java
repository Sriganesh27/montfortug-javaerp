package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.DuplicateResourceException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.BranchLevel;
import com.erp.montfortuganda.school.entity.ErpALevelCombination;
import com.erp.montfortuganda.school.entity.ErpSubject;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.school.repository.ErpALevelCombinationRepository;
import com.erp.montfortuganda.school.repository.ErpSubjectRepository;
import com.erp.montfortuganda.student.entity.ErpStudentALevelSelection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Business operations for branch-specific A' Level combinations.
 *
 * This service deliberately uses the authenticated user's branch. A caller
 * cannot create or modify a combination for another branch by supplying a
 * branch ID.
 */
@Service
@Transactional(readOnly = true)
public class ErpALevelCombinationService {

    private static final int A_LEVEL_ID = 4;

    private final ErpALevelCombinationRepository combinationRepository;
    private final ErpSubjectRepository subjectRepository;
    private final BranchRepository branchRepository;
    private final CurrentUserService currentUserService;

    @PersistenceContext
    private EntityManager entityManager;

    public ErpALevelCombinationService(
            ErpALevelCombinationRepository combinationRepository,
            ErpSubjectRepository subjectRepository,
            BranchRepository branchRepository,
            CurrentUserService currentUserService
    ) {
        this.combinationRepository = combinationRepository;
        this.subjectRepository = subjectRepository;
        this.branchRepository = branchRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Creates a combination for the authenticated user's branch.
     */
    @Transactional
    public ErpALevelCombination createCombination(
            String combinationCode,
            String combinationName,
            Long principalSubject1Id,
            Long principalSubject2Id,
            Long principalSubject3Id,
            Long subsidiarySubjectId
    ) {
        UserBranchContext context = requireAuthenticatedBranch();
        requireAlevelOffered(context.branch());

        String code = normalizeCode(combinationCode);
        String name = normalizeRequiredText(
                combinationName,
                "Combination name"
        );

        if (combinationRepository
                .existsByBranch_BranchIdAndCombinationCodeIgnoreCase(
                        context.branchId(),
                        code
                )) {
            throw new DuplicateResourceException(
                    "Combination code already exists in this branch."
            );
        }

        ErpSubject principal1 = requireUsableSubject(
                principalSubject1Id, context.branchId(),
                "Principal subject 1"
        );
        ErpSubject principal2 = requireUsableSubject(
                principalSubject2Id, context.branchId(),
                "Principal subject 2"
        );
        ErpSubject principal3 = requireUsableSubject(
                principalSubject3Id, context.branchId(),
                "Principal subject 3"
        );
        ErpSubject subsidiary = requireUsableSubject(
                subsidiarySubjectId, context.branchId(),
                "Subsidiary subject"
        );

        validateDistinctSubjects(
                principal1, principal2, principal3, subsidiary
        );
        validateSubjectTypes(
                principal1, principal2, principal3, subsidiary
        );

        ErpALevelCombination combination =
                new ErpALevelCombination();

        combination.setBranch(context.branch());
        combination.setCombinationCode(code);
        combination.setCombinationName(name);
        combination.setPrincipalSubject1(principal1);
        combination.setPrincipalSubject2(principal2);
        combination.setPrincipalSubject3(principal3);
        combination.setSubsidiarySubject(subsidiary);
        combination.setActive(true);
        combination.setCreatedBy(asLong(context.userId()));
        combination.setUpdatedBy(asLong(context.userId()));

        return combinationRepository.saveAndFlush(combination);
    }

    /**
     * Updates a combination. Subject structure cannot be changed after the
     * combination has been selected by an applicant or student.
     */
    @Transactional
    public ErpALevelCombination updateCombination(
            Long combinationId,
            String combinationCode,
            String combinationName,
            Long principalSubject1Id,
            Long principalSubject2Id,
            Long principalSubject3Id,
            Long subsidiarySubjectId
    ) {
        UserBranchContext context = requireAuthenticatedBranch();
        requireAlevelOffered(context.branch());

        ErpALevelCombination combination = requireOwnedCombination(
                combinationId, context.branchId()
        );

        String code = normalizeCode(combinationCode);
        String name = normalizeRequiredText(
                combinationName,
                "Combination name"
        );

        if (combinationRepository
                .existsByBranch_BranchIdAndCombinationCodeIgnoreCaseAndCombinationIdNot(
                        context.branchId(),
                        code,
                        combinationId
                )) {
            throw new DuplicateResourceException(
                    "Combination code already exists in this branch."
            );
        }

        ErpSubject principal1 = requireUsableSubject(
                principalSubject1Id, context.branchId(),
                "Principal subject 1"
        );
        ErpSubject principal2 = requireUsableSubject(
                principalSubject2Id, context.branchId(),
                "Principal subject 2"
        );
        ErpSubject principal3 = requireUsableSubject(
                principalSubject3Id, context.branchId(),
                "Principal subject 3"
        );
        ErpSubject subsidiary = requireUsableSubject(
                subsidiarySubjectId, context.branchId(),
                "Subsidiary subject"
        );

        validateDistinctSubjects(
                principal1, principal2, principal3, subsidiary
        );
        validateSubjectTypes(
                principal1, principal2, principal3, subsidiary
        );

        boolean structureChanged =
                !sameId(combination.getPrincipalSubject1(), principal1)
                || !sameId(combination.getPrincipalSubject2(), principal2)
                || !sameId(combination.getPrincipalSubject3(), principal3)
                || !sameId(combination.getSubsidiarySubject(), subsidiary);

        if (structureChanged && isCombinationAssigned(combinationId)) {
            throw new BadRequestException(
                    "This combination has already been assigned. "
                    + "Its subjects cannot be changed. Deactivate it and "
                    + "create a new combination instead."
            );
        }

        combination.setCombinationCode(code);
        combination.setCombinationName(name);

        if (structureChanged) {
            combination.setPrincipalSubject1(principal1);
            combination.setPrincipalSubject2(principal2);
            combination.setPrincipalSubject3(principal3);
            combination.setSubsidiarySubject(subsidiary);
        }

        combination.setUpdatedBy(asLong(context.userId()));

        return combinationRepository.saveAndFlush(combination);
    }

    /**
     * Returns active combinations belonging only to the authenticated branch.
     */
    public List<ErpALevelCombination> getActiveCombinations() {
        UserBranchContext context = requireAuthenticatedBranch();
        requireAlevelOffered(context.branch());
        return combinationRepository
                .findAllByBranch_BranchIdAndActiveTrueOrderByCombinationCodeAsc(context.branchId());
    }

    /** Returns active and inactive combinations for the authenticated branch. */
    public List<ErpALevelCombination> getAllCombinations() {
        UserBranchContext context = requireAuthenticatedBranch();
        requireAlevelOffered(context.branch());
        return combinationRepository
                .findAllByBranch_BranchIdOrderByCombinationCodeAsc(context.branchId());
    }

    /**
     * Public read-only lookup for the S5 application form.
     * Only combinations from a branch that offers A' Level are returned.
     */
    @Transactional(readOnly = true)
    public List<ErpALevelCombination> getPublicActiveCombinations(
            Integer branchId
    ) {
        if (branchId == null) {
            throw new BadRequestException("Branch ID is required.");
        }

        Branch branch = branchRepository
                .findByIdWithLevels(branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch was not found."
                ));

        requireAlevelOffered(branch);

        return combinationRepository
                .findAllByBranch_BranchIdAndActiveTrueOrderByCombinationCodeAsc(
                        branchId
                );
    }

    /**
     * Returns a combination belonging only to the authenticated branch.
     */
    public ErpALevelCombination getCombination(Long combinationId) {
        UserBranchContext context = requireAuthenticatedBranch();
        requireAlevelOffered(context.branch());
        return requireOwnedCombination(combinationId, context.branchId());
    }

    /**
     * Deactivates a combination without deleting its history or assignments.
     */
    @Transactional
    public ErpALevelCombination deactivateCombination(
            Long combinationId
    ) {
        UserBranchContext context = requireAuthenticatedBranch();
        requireAlevelOffered(context.branch());

        ErpALevelCombination combination = requireOwnedCombination(
                combinationId,
                context.branchId()
        );

        combination.setActive(false);
        combination.setUpdatedBy(asLong(context.userId()));

        return combinationRepository.saveAndFlush(combination);
    }

    /** Reactivates a branch-owned combination without changing its subject structure. */
    @Transactional
    public ErpALevelCombination activateCombination(Long combinationId) {
        UserBranchContext context = requireAuthenticatedBranch();
        requireAlevelOffered(context.branch());
        ErpALevelCombination combination = requireOwnedCombination(combinationId, context.branchId());
        combination.setActive(true);
        combination.setUpdatedBy(asLong(context.userId()));
        return combinationRepository.saveAndFlush(combination);
    }

    private UserBranchContext requireAuthenticatedBranch() {
        CurrentUserContext user =
                currentUserService.getCurrentUserContext();

        if (user.getBranchId() == null) {
            throw new BadRequestException(
                    "The authenticated user is not assigned to a branch."
            );
        }

        Branch branch = branchRepository
                .findByIdWithLevels(user.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assigned branch was not found."
                ));

        return new UserBranchContext(
                branch.getBranchId(),
                branch,
                user.getUserId()
        );
    }

    private void requireAlevelOffered(Branch branch) {
        boolean offered = branch.getBranchLevels() != null
                && branch.getBranchLevels().stream()
                .map(BranchLevel::getLevel)
                .filter(Objects::nonNull)
                .anyMatch(level ->
                        Objects.equals(level.getLevelId(), A_LEVEL_ID)
                );

        if (!offered) {
            throw new BadRequestException(
                    "This branch does not offer Senior Secondary (A' Level)."
            );
        }
    }

    private ErpALevelCombination requireOwnedCombination(
            Long combinationId,
            Integer branchId
    ) {
        if (combinationId == null) {
            throw new BadRequestException(
                    "Combination ID is required."
            );
        }

        return combinationRepository
                .findByCombinationIdAndBranch_BranchId(
                        combinationId,
                        branchId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "A' Level combination was not found in this branch."
                ));
    }

    private ErpSubject requireUsableSubject(
            Long subjectId,
            Integer branchId,
            String label
    ) {
        if (subjectId == null) {
            throw new BadRequestException(label + " is required.");
        }

        ErpSubject subject = subjectRepository
                .findBySubjectIdAndBranch_BranchIdAndActiveTrue(
                        subjectId,
                        branchId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        label + " was not found or is not active in this branch."
                ));

        if (subject.getStatus() != ErpSubject.Status.ACTIVE) {
            throw new BadRequestException(
                    label + " must have ACTIVE status."
            );
        }

        // Reuse the existing Class Subjects entity directly. This avoids
        // changing ErpClassSubjectRepository or adding a new repository method.
        Long configuredCount = entityManager.createQuery(
                        "select count(cs) from ErpClassSubject cs " +
                                "where cs.branch.branchId = :branchId " +
                                "and cs.level.levelId = :levelId " +
                                "and cs.subject.subjectId = :subjectId " +
                                "and cs.active = true",
                        Long.class
                )
                .setParameter("branchId", branchId)
                .setParameter("levelId", A_LEVEL_ID)
                .setParameter("subjectId", subjectId)
                .getSingleResult();

        if (configuredCount == null || configuredCount == 0L) {
            throw new BadRequestException(
                    label + " must be configured in Senior Secondary Class Subjects."
            );
        }

        return subject;
    }

    private void validateDistinctSubjects(
            ErpSubject principal1,
            ErpSubject principal2,
            ErpSubject principal3,
            ErpSubject subsidiary
    ) {
        List<Long> ids = List.of(
                principal1.getSubjectId(),
                principal2.getSubjectId(),
                principal3.getSubjectId(),
                subsidiary.getSubjectId()
        );

        if (ids.stream().distinct().count() != 4) {
            throw new BadRequestException(
                    "All four subjects must be different."
            );
        }
    }

    private void validateSubjectTypes(
            ErpSubject principal1,
            ErpSubject principal2,
            ErpSubject principal3,
            ErpSubject subsidiary
    ) {
        if (principal1.getSubjectType()
                == ErpSubject.SubjectType.SUBSIDIARY
                || principal2.getSubjectType()
                == ErpSubject.SubjectType.SUBSIDIARY
                || principal3.getSubjectType()
                == ErpSubject.SubjectType.SUBSIDIARY) {
            throw new BadRequestException(
                    "The three principal subjects cannot be subsidiary subjects."
            );
        }

        if (subsidiary.getSubjectType()
                != ErpSubject.SubjectType.SUBSIDIARY) {
            throw new BadRequestException(
                    "The fourth subject must have type SUBSIDIARY."
            );
        }
    }

    private boolean isCombinationAssigned(Long combinationId) {
        Long count = entityManager.createQuery(
                """
                select count(selection)
                from ErpStudentALevelSelection selection
                where selection.combination.combinationId = :combinationId
                """,
                Long.class
        ).setParameter("combinationId", combinationId)
         .getSingleResult();

        return count != null && count > 0;
    }

    private boolean sameId(
            ErpSubject current,
            ErpSubject requested
    ) {
        return current != null
                && requested != null
                && Objects.equals(
                        current.getSubjectId(),
                        requested.getSubjectId()
                );
    }

    private String normalizeCode(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(
                    "Combination code is required."
            );
        }

        String normalized = value.trim()
                .toUpperCase(Locale.ROOT);

        if (normalized.length() > 30) {
            throw new BadRequestException(
                    "Combination code cannot exceed 30 characters."
            );
        }

        return normalized;
    }

    private String normalizeRequiredText(
            String value,
            String label
    ) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(label + " is required.");
        }

        String normalized = value.trim();

        if (normalized.length() > 150) {
            throw new BadRequestException(
                    label + " cannot exceed 150 characters."
            );
        }

        return normalized;
    }

    private Long asLong(Integer value) {
        return value == null ? null : value.longValue();
    }

    private record UserBranchContext(
            Integer branchId,
            Branch branch,
            Integer userId
    ) {
    }
}
