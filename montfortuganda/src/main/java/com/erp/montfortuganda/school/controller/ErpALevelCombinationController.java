package com.erp.montfortuganda.school.controller;

import com.erp.montfortuganda.school.dto.ErpALevelCombinationRequest;
import com.erp.montfortuganda.school.dto.ErpALevelCombinationResponse;
import com.erp.montfortuganda.school.entity.ErpALevelCombination;
import com.erp.montfortuganda.school.entity.ErpSubject;
import com.erp.montfortuganda.school.service.ErpALevelCombinationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for branch-scoped A' Level combination management.
 *
 * Branch ownership is enforced by the service using the authenticated user;
 * this controller intentionally does not accept a branch ID.
 */
@RestController
@RequestMapping("/api/school/a-level-combinations")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
public class ErpALevelCombinationController {

    private final ErpALevelCombinationService combinationService;

    public ErpALevelCombinationController(
            ErpALevelCombinationService combinationService
    ) {
        this.combinationService = combinationService;
    }

    @PostMapping
    public ResponseEntity<ErpALevelCombinationResponse> create(
            @Valid @RequestBody ErpALevelCombinationRequest request
    ) {
        ErpALevelCombination combination =
                combinationService.createCombination(
                        request.combinationCode(),
                        request.combinationName(),
                        request.principalSubject1Id(),
                        request.principalSubject2Id(),
                        request.principalSubject3Id(),
                        request.subsidiarySubjectId()
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(combination));
    }

    @PutMapping("/{combinationId}")
    public ResponseEntity<ErpALevelCombinationResponse> update(
            @PathVariable Long combinationId,
            @Valid @RequestBody ErpALevelCombinationRequest request
    ) {
        ErpALevelCombination combination =
                combinationService.updateCombination(
                        combinationId,
                        request.combinationCode(),
                        request.combinationName(),
                        request.principalSubject1Id(),
                        request.principalSubject2Id(),
                        request.principalSubject3Id(),
                        request.subsidiarySubjectId()
                );

        return ResponseEntity.ok(toResponse(combination));
    }

    @GetMapping
    public ResponseEntity<List<ErpALevelCombinationResponse>> getAll() {
        List<ErpALevelCombinationResponse> responses =
                combinationService.getAllCombinations()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{combinationId}")
    public ResponseEntity<ErpALevelCombinationResponse> getOne(
            @PathVariable Long combinationId
    ) {
        return ResponseEntity.ok(
                toResponse(combinationService.getCombination(combinationId))
        );
    }

    @PatchMapping("/{combinationId}/deactivate")
    public ResponseEntity<ErpALevelCombinationResponse> deactivate(
            @PathVariable Long combinationId
    ) {
        ErpALevelCombination combination =
                combinationService.deactivateCombination(combinationId);

        return ResponseEntity.ok(toResponse(combination));
    }

    @PatchMapping("/{combinationId}/activate")
    public ResponseEntity<ErpALevelCombinationResponse> activate(
            @PathVariable Long combinationId
    ) {
        ErpALevelCombination combination = combinationService.activateCombination(combinationId);
        return ResponseEntity.ok(toResponse(combination));
    }

    private ErpALevelCombinationResponse toResponse(
            ErpALevelCombination combination
    ) {
        ErpSubject subject1 = combination.getPrincipalSubject1();
        ErpSubject subject2 = combination.getPrincipalSubject2();
        ErpSubject subject3 = combination.getPrincipalSubject3();
        ErpSubject subsidiary = combination.getSubsidiarySubject();

        return new ErpALevelCombinationResponse(
                combination.getCombinationId(),
                combination.getBranch().getBranchId(),
                combination.getCombinationCode(),
                combination.getCombinationName(),

                subject1.getSubjectId(),
                subject1.getSubjectCode(),
                subject1.getSubjectName(),

                subject2.getSubjectId(),
                subject2.getSubjectCode(),
                subject2.getSubjectName(),

                subject3.getSubjectId(),
                subject3.getSubjectCode(),
                subject3.getSubjectName(),

                subsidiary.getSubjectId(),
                subsidiary.getSubjectCode(),
                subsidiary.getSubjectName(),

                combination.getActive(),
                combination.getVersion(),
                combination.getCreatedAt(),
                combination.getUpdatedAt()
        );
    }
}
