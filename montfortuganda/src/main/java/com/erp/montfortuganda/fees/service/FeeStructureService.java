package com.erp.montfortuganda.fees.service;

import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.fees.dto.*;
import com.erp.montfortuganda.fees.entity.ErpFeeStructure;
import com.erp.montfortuganda.fees.entity.ErpFeeStructure.StructureStatus;
import com.erp.montfortuganda.fees.entity.ErpFeeStructureItem;
import com.erp.montfortuganda.fees.repository.ErpFeeStructureItemRepository;
import com.erp.montfortuganda.fees.repository.ErpFeeStructureRepository;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.ErpAcademicTerm;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import com.erp.montfortuganda.school.entity.SchoolClass;
import com.erp.montfortuganda.school.repository.AcademicTermRepository;
import com.erp.montfortuganda.school.repository.AcademicYearRepository;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.school.repository.SchoolClassRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Branch-scoped business logic for fee structures. Branch ownership always
 * comes from the authenticated server-side context, never from request JSON.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeeStructureService {

    private final ErpFeeStructureRepository structureRepository;
    private final ErpFeeStructureItemRepository itemRepository;
    private final BranchRepository branchRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AcademicTermRepository academicTermRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final CurrentUserService currentUserService;
    private final BranchAccessService branchAccessService;

    public List<FeeStructureResponse> list(
            Long academicYearId,
            Integer classId,
            Long termId,
            StructureStatus status
    ) {
        Integer branchId = currentBranchId();
        validateOptionalPositive(academicYearId, "Academic year ID");
        validateOptionalPositive(classId, "Class ID");
        validateOptionalPositive(termId, "Term ID");

        if (academicYearId != null) {
            academicYearRepository.findByAcademicYearIdAndBranchBranchId(
                    academicYearId, branchId
            ).orElseThrow(() -> notFound("Academic year was not found for this branch."));
        }
        if (classId != null && schoolClassRepository.findByClassIdAndBranch_BranchId(
                classId, branchId
        ).isEmpty()) {
            throw notFound("Class was not found for this branch.");
        }
        if (termId != null && academicTermRepository.findByTermIdAndBranchId(
                termId, branchId
        ).isEmpty()) {
            throw notFound("Term was not found for this branch.");
        }

        List<ErpFeeStructure> structures = structureRepository.searchForBranch(
                branchId, academicYearId, classId, termId, status
        );
        if (structures.isEmpty()) return List.of();

        Map<Long, List<ErpFeeStructureItem>> itemsByStructure =
                loadItems(structures.stream().map(ErpFeeStructure::getFeeStructureId).toList());

        return structures.stream()
                .map(s -> toResponse(s, itemsByStructure.getOrDefault(s.getFeeStructureId(), List.of())))
                .toList();
    }

    public FeeStructureResponse get(Long feeStructureId) {
        Integer branchId = currentBranchId();
        ErpFeeStructure structure = getStructure(feeStructureId, branchId);
        return toResponse(
                structure,
                itemRepository.findAllByFeeStructure_FeeStructureIdOrderByDisplayOrderAscFeeStructureItemIdAsc(
                        structure.getFeeStructureId()
                )
        );
    }

    @Transactional
    public FeeStructureResponse create(FeeStructureCreateRequest request) {
        Integer branchId = currentBranchId();
        CurrentUserContext user = currentUserService.getCurrentUserContext();
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> notFound("School branch was not found."));

        ErpAcademicYear year = academicYearRepository
                .findByAcademicYearIdAndBranchBranchIdAndActiveTrue(
                        request.academicYearId(), branchId
                )
                .orElseThrow(() -> badRequest("Select an active academic year belonging to this branch."));

        SchoolClass schoolClass = schoolClassRepository
                .findByClassIdAndBranch_BranchId(request.classId(), branchId)
                .orElseThrow(() -> badRequest("Select a class belonging to this branch."));

        boolean classAvailable = schoolClassRepository.existsAvailableForBranchAndAcademicYear(
                request.classId(), branchId, year.getAcademicYearId(), 1
        );
        if (!classAvailable) {
            throw badRequest("The selected class is not active or is not available in the selected academic year.");
        }

        ErpAcademicTerm term = academicTermRepository
                .findActiveByTermIdAndBranchId(request.termId(), branchId)
                .orElseThrow(() -> badRequest("Select an active term belonging to this branch."));

        if (!Objects.equals(
                term.getAcademicYear().getAcademicYearId(),
                year.getAcademicYearId()
        )) {
            throw badRequest("The selected term does not belong to the selected academic year.");
        }

        if (structureRepository.existsBySchoolClass_ClassIdAndTerm_TermIdAndStructureStatus(
                schoolClass.getClassId(), term.getTermId(), StructureStatus.DRAFT
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A draft fee structure already exists for this class and term. Open that draft instead of creating a duplicate."
            );
        }

        Integer maxVersion = structureRepository.findMaximumVersion(
                schoolClass.getClassId(), term.getTermId()
        );
        int nextVersion = (maxVersion == null ? 0 : maxVersion) + 1;

        ErpFeeStructure structure = new ErpFeeStructure();
        structure.setBranch(branch);
        structure.setAcademicYear(year);
        structure.setSchoolClass(schoolClass);
        structure.setTerm(term);
        structure.setVersionNo(nextVersion);
        structure.setStructureStatus(StructureStatus.DRAFT);
        structure.setRemarks(normalizeOptional(request.remarks()));
        structure.setCreatedBy(asLong(user.getUserId()));
        structure.setUpdatedBy(asLong(user.getUserId()));

        try {
            structure = structureRepository.saveAndFlush(structure);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A fee structure version already exists for this class and term. Refresh the page and try again.",
                    exception
            );
        }

        return toResponse(structure, List.of());
    }

    @Transactional
    public FeeStructureResponse update(
            Long feeStructureId,
            FeeStructureUpdateRequest request
    ) {
        Integer branchId = currentBranchId();
        Long userId = currentUserId();
        ErpFeeStructure structure = getStructureForUpdate(feeStructureId, branchId);
        requireDraft(structure);

        structure.setRemarks(normalizeOptional(request.remarks()));
        structure.setUpdatedBy(userId);

        return toResponse(structureRepository.saveAndFlush(structure),
                itemRepository.findAllByFeeStructure_FeeStructureIdOrderByDisplayOrderAscFeeStructureItemIdAsc(
                        structure.getFeeStructureId()));
    }

    @Transactional
    public FeeStructureItemResponse addItem(
            Long feeStructureId,
            FeeStructureItemRequest request
    ) {
        Integer branchId = currentBranchId();
        Long userId = currentUserId();
        ErpFeeStructure structure = getStructureForUpdate(feeStructureId, branchId);
        requireDraft(structure);

        String feeType = normalizeFeeType(request.feeType());
        String feeName = normalizeRequired(request.feeName(), "Fee name");
        validateItemAmounts(request);

        if (itemRepository.existsByFeeStructure_FeeStructureIdAndFeeTypeIgnoreCaseAndFeeNameIgnoreCase(
                feeStructureId, feeType, feeName
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This fee type and fee name already exist in the selected structure."
            );
        }

        ErpFeeStructureItem item = new ErpFeeStructureItem();
        item.setFeeStructure(structure);
        applyItemRequest(item, request, feeType, feeName);
        item.setActive(true);
        item.setCreatedBy(userId);
        item.setUpdatedBy(userId);
        try {
            return toItemResponse(itemRepository.saveAndFlush(item));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A fee with the same type and name already exists in this structure.",
                    exception
            );
        }
    }

    @Transactional
    public FeeStructureItemResponse updateItem(
            Long feeStructureId,
            Long itemId,
            FeeStructureItemRequest request
    ) {
        Integer branchId = currentBranchId();
        Long userId = currentUserId();
        ErpFeeStructure structure = getStructureForUpdate(feeStructureId, branchId);
        requireDraft(structure);

        ErpFeeStructureItem item = itemRepository
                .findByFeeStructureItemIdAndFeeStructure_FeeStructureId(itemId, feeStructureId)
                .orElseThrow(() -> notFound("Fee item was not found in this structure."));

        String feeType = normalizeFeeType(request.feeType());
        String feeName = normalizeRequired(request.feeName(), "Fee name");
        validateItemAmounts(request);

        if (itemRepository.existsByFeeStructure_FeeStructureIdAndFeeTypeIgnoreCaseAndFeeNameIgnoreCaseAndFeeStructureItemIdNot(
                feeStructureId, feeType, feeName, itemId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another fee item already uses this fee type and name."
            );
        }

        applyItemRequest(item, request, feeType, feeName);
        item.setUpdatedBy(userId);
        try {
            return toItemResponse(itemRepository.saveAndFlush(item));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The fee item conflicts with an existing item in this structure.",
                    exception
            );
        }
    }

    @Transactional
    public FeeStructureItemResponse deactivateItem(Long feeStructureId, Long itemId) {
        Integer branchId = currentBranchId();
        ErpFeeStructure structure = getStructureForUpdate(feeStructureId, branchId);
        requireDraft(structure);

        ErpFeeStructureItem item = itemRepository
                .findByFeeStructureItemIdAndFeeStructure_FeeStructureId(itemId, feeStructureId)
                .orElseThrow(() -> notFound("Fee item was not found in this structure."));

        if (Boolean.TRUE.equals(item.getActive())) {
            item.setActive(false);
            item.setUpdatedBy(currentUserId());
        }
        return toItemResponse(itemRepository.saveAndFlush(item));
    }

    @Transactional
    public FeeStructureResponse publish(Long feeStructureId) {
        Integer branchId = currentBranchId();
        Long userId = currentUserId();
        ErpFeeStructure structure = getStructureForUpdate(feeStructureId, branchId);
        requireDraft(structure);

        List<ErpFeeStructureItem> activeItems =
                itemRepository.findAllByFeeStructure_FeeStructureIdOrderByDisplayOrderAscFeeStructureItemIdAsc(
                        feeStructureId
                ).stream().filter(item -> Boolean.TRUE.equals(item.getActive())).toList();

        if (activeItems.isEmpty()) {
            throw badRequest("Add at least one active fee item before publishing the structure.");
        }
        for (ErpFeeStructureItem item : activeItems) {
            if (item.getAmount() == null || item.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw badRequest("Every active fee item must have a valid non-negative amount.");
            }
        }

        // Keep historical versions, but make the newly published version the
        // only published version for this class and term.
        List<ErpFeeStructure> previousPublished =
                structureRepository.findByClassTermAndStatusForUpdate(
                        structure.getSchoolClass().getClassId(),
                        structure.getTerm().getTermId(),
                        StructureStatus.PUBLISHED
                );
        for (ErpFeeStructure previous : previousPublished) {
            if (!previous.getFeeStructureId().equals(structure.getFeeStructureId())) {
                previous.setStructureStatus(StructureStatus.ARCHIVED);
                previous.setUpdatedBy(userId);
            }
        }

        structure.setStructureStatus(StructureStatus.PUBLISHED);
        structure.setPublishedAt(LocalDateTime.now());
        structure.setUpdatedBy(userId);

        structureRepository.saveAll(previousPublished);
        structure = structureRepository.saveAndFlush(structure);

        return toResponse(
                structure,
                itemRepository.findAllByFeeStructure_FeeStructureIdOrderByDisplayOrderAscFeeStructureItemIdAsc(
                        structure.getFeeStructureId()
                )
        );
    }

    @Transactional
    public FeeStructureResponse archive(Long feeStructureId) {
        Integer branchId = currentBranchId();
        ErpFeeStructure structure = getStructureForUpdate(feeStructureId, branchId);

        if (structure.getStructureStatus() == StructureStatus.ARCHIVED) {
            return get(feeStructureId);
        }

        structure.setStructureStatus(StructureStatus.ARCHIVED);
        structure.setUpdatedBy(currentUserId());
        structure = structureRepository.saveAndFlush(structure);

        return toResponse(
                structure,
                itemRepository.findAllByFeeStructure_FeeStructureIdOrderByDisplayOrderAscFeeStructureItemIdAsc(
                        structure.getFeeStructureId()
                )
        );
    }

    private Map<Long, List<ErpFeeStructureItem>> loadItems(List<Long> structureIds) {
        return itemRepository
                .findAllByFeeStructure_FeeStructureIdInOrderByFeeStructure_FeeStructureIdAscDisplayOrderAscFeeStructureItemIdAsc(
                        structureIds
                )
                .stream()
                .collect(Collectors.groupingBy(
                        item -> item.getFeeStructure().getFeeStructureId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private ErpFeeStructure getStructure(Long id, Integer branchId) {
        if (id == null || id <= 0) throw badRequest("Fee structure ID must be positive.");
        return structureRepository.findByFeeStructureIdAndBranch_BranchId(id, branchId)
                .orElseThrow(() -> notFound("Fee structure was not found for this branch."));
    }

    private ErpFeeStructure getStructureForUpdate(Long id, Integer branchId) {
        if (id == null || id <= 0) throw badRequest("Fee structure ID must be positive.");
        return structureRepository.findForUpdate(id, branchId)
                .orElseThrow(() -> notFound("Fee structure was not found for this branch."));
    }

    private Integer currentBranchId() {
        CurrentUserContext context = currentUserService.getCurrentUserContext();
        return branchAccessService.getValidatedBranchId(context);
    }

    private Long currentUserId() {
        return asLong(currentUserService.getCurrentUserContext().getUserId());
    }

    private Long asLong(Integer value) {
        return value == null ? null : value.longValue();
    }

    private void requireDraft(ErpFeeStructure structure) {
        if (structure.getStructureStatus() != StructureStatus.DRAFT) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only draft fee structures can be changed. Create a new version to revise a published structure."
            );
        }
    }

    private void validateItemAmounts(FeeStructureItemRequest request) {
        if (request.amount() == null) {
            throw badRequest("Fee amount is required.");
        }
        if (request.amount().stripTrailingZeros().scale() > 2) {
            throw badRequest("Fee amount cannot have more than two non-zero decimal places.");
        }
        if (request.amount().compareTo(BigDecimal.ZERO) < 0) {
            throw badRequest("Fee amount cannot be negative.");
        }
        if (request.displayOrder() == null || request.displayOrder() < 0) {
            throw badRequest("Display order cannot be negative.");
        }
    }

    private void applyItemRequest(
            ErpFeeStructureItem item,
            FeeStructureItemRequest request,
            String feeType,
            String feeName
    ) {
        item.setFeeType(feeType);
        item.setFeeName(feeName);
        item.setAmount(request.amount().setScale(2, java.math.RoundingMode.UNNECESSARY));
        item.setChargingFrequency(request.chargingFrequency());
        item.setDueDate(request.dueDate());
        item.setMandatory(request.mandatory());
        item.setDisplayOrder(request.displayOrder());
        item.setRemarks(normalizeOptional(request.remarks()));
    }

    private String normalizeFeeType(String value) {
        String normalized = normalizeRequired(value, "Fee type").toUpperCase(Locale.ROOT);
        if (normalized.length() > 50) throw badRequest("Fee type cannot exceed 50 characters.");
        return normalized;
    }

    private String normalizeRequired(String value, String label) {
        if (value == null || value.isBlank()) throw badRequest(label + " is required.");
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private void validateOptionalPositive(Number value, String label) {
        if (value != null && value.longValue() <= 0) {
            throw badRequest(label + " must be greater than zero.");
        }
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private FeeStructureResponse toResponse(
            ErpFeeStructure structure,
            List<ErpFeeStructureItem> items
    ) {
        SchoolClass schoolClass = structure.getSchoolClass();
        return new FeeStructureResponse(
                structure.getFeeStructureId(),
                structure.getBranch().getBranchId(),
                structure.getAcademicYear().getAcademicYearId(),
                structure.getAcademicYear().getAcademicYearCode(),
                structure.getAcademicYear().getAcademicYearName(),
                schoolClass.getClassId(),
                schoolClass.getClassCode(),
                schoolClass.getClassName(),
                schoolClass.getLevel() == null ? null : schoolClass.getLevel().getLevelId(),
                schoolClass.getLevel() == null ? null : schoolClass.getLevel().getLevelName(),
                structure.getTerm().getTermId(),
                structure.getTerm().getTermCode(),
                structure.getTerm().getTermName(),
                structure.getVersionNo(),
                structure.getStructureStatus(),
                structure.getPublishedAt(),
                structure.getRemarks(),
                structure.getRowVersion(),
                structure.getCreatedAt(),
                structure.getUpdatedAt(),
                items.stream().map(this::toItemResponse).toList()
        );
    }

    private FeeStructureItemResponse toItemResponse(ErpFeeStructureItem item) {
        return new FeeStructureItemResponse(
                item.getFeeStructureItemId(),
                item.getFeeStructure().getFeeStructureId(),
                item.getFeeType(),
                item.getFeeName(),
                item.getAmount(),
                item.getChargingFrequency(),
                item.getDueDate(),
                item.getMandatory(),
                item.getDisplayOrder(),
                item.getActive(),
                item.getRemarks(),
                item.getRowVersion(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
