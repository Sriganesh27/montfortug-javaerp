package com.erp.montfortuganda.fees.controller;

import com.erp.montfortuganda.dto.ApiResponse;
import com.erp.montfortuganda.fees.dto.*;
import com.erp.montfortuganda.fees.entity.ErpFeeStructure.StructureStatus;
import com.erp.montfortuganda.fees.service.FeeStructureService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Validated @RestController @RequestMapping("/api/branchadmin/fees/structures")
@PreAuthorize("hasRole('BRANCH_ADMIN')") @RequiredArgsConstructor
public class FeeStructureController {
 private static final CacheControl NO_STORE=CacheControl.noStore().mustRevalidate();
 private final FeeStructureService service;

 @GetMapping public ResponseEntity<ApiResponse<CombinedFeeListResponse>> list(
  @RequestParam(required=false) @Positive Long academicYearId,
  @RequestParam(required=false) @Positive Integer levelId,
  @RequestParam(required=false) @Positive Integer classId,
  @RequestParam(required=false) String status,
  @RequestParam(required=false) String source,
  @RequestParam(required=false) @Size(max=100) String search,
  @RequestParam(defaultValue="0") @Min(0) Integer page,
  @RequestParam(defaultValue="10") @Min(1) @Max(100) Integer size) {
  return ok(ApiResponse.success("Fee definitions fetched successfully.",service.list(academicYearId,levelId,classId,status,source,search,page,size)));
 }
 @GetMapping("/{id:\\d+}") public ResponseEntity<ApiResponse<FeeStructureResponse>> get(@PathVariable @Positive Long id) {
  return ok(ApiResponse.success("Fee definition fetched successfully.",service.get(id)));
 }
 @PostMapping public ResponseEntity<ApiResponse<FeeStructureResponse>> create(@Valid @RequestBody FeeStructureCreateRequest r) {
  return ResponseEntity.status(HttpStatus.CREATED).cacheControl(NO_STORE).body(ApiResponse.success("Draft mandatory fee definition created.",service.create(r)));
 }
 @PutMapping("/{id:\\d+}") public ResponseEntity<ApiResponse<FeeStructureResponse>> update(@PathVariable @Positive Long id,@Valid @RequestBody FeeStructureUpdateRequest r) {
  return ok(ApiResponse.success("Draft mandatory fee definition updated.",service.update(id,r)));
 }
 @PostMapping("/{id:\\d+}/publish") public ResponseEntity<ApiResponse<FeeStructureResponse>> publish(@PathVariable @Positive Long id) {
  return ok(ApiResponse.success("Fee definition published.",service.publish(id)));
 }
 @PostMapping("/{id:\\d+}/archive") public ResponseEntity<ApiResponse<FeeStructureResponse>> archive(@PathVariable @Positive Long id) {
  return ok(ApiResponse.success("Fee definition archived.",service.archive(id)));
 }
 @PostMapping("/{id:\\d+}/active") public ResponseEntity<ApiResponse<FeeStructureResponse>> setMandatoryActive(@PathVariable @Positive Long id,@RequestParam boolean active) {
  return ok(ApiResponse.success(active ? "Mandatory fee activated." : "Mandatory fee deactivated.",service.setMandatoryActive(id,active)));
 }
 @GetMapping("/available-classes") public ResponseEntity<ApiResponse<List<com.erp.montfortuganda.school.dto.SchoolClassResponse>>> classes(
  @RequestParam(required=false) @Positive Long academicYearId,@RequestParam @Positive Integer levelId) {
  return ok(ApiResponse.success("Available classes fetched.",service.availableClasses(academicYearId,levelId)));
 }

 // Independent fees have their own branch-scoped endpoints and no parent structure id.
 @GetMapping("/independent-items") public ResponseEntity<ApiResponse<List<FeeStructureItemResponse>>> independentItems(
  @RequestParam(defaultValue="true") boolean activeOnly) {
  return ok(ApiResponse.success("Independent fee definitions fetched.",service.listIndependentItems(activeOnly)));
 }
 @PostMapping("/independent-items") public ResponseEntity<ApiResponse<FeeStructureItemResponse>> createIndependent(@Valid @RequestBody FeeStructureItemRequest r) {
  return ResponseEntity.status(HttpStatus.CREATED).cacheControl(NO_STORE).body(ApiResponse.success("Independent fee definition created.",service.createIndependentItem(r)));
 }
 @PutMapping("/independent-items/{itemId:\\d+}") public ResponseEntity<ApiResponse<FeeStructureItemResponse>> updateIndependent(
  @PathVariable @Positive Long itemId,@Valid @RequestBody FeeStructureItemRequest r) {
  return ok(ApiResponse.success("Independent fee definition updated.",service.updateIndependentItem(itemId,r)));
 }
 @PostMapping("/independent-items/{itemId:\\d+}/active") public ResponseEntity<ApiResponse<FeeStructureItemResponse>> setIndependentActive(@PathVariable @Positive Long itemId,@RequestParam boolean active) {
  return ok(ApiResponse.success(active ? "Optional fee activated." : "Optional fee deactivated.",service.setIndependentItemActive(itemId,active)));
 }

 // Student assignment processing is intentionally deferred to the later student-related phase.
 @PostMapping("/{id:\\d+}/assignments") public ResponseEntity<ApiResponse<Object>> assignmentsDeferred(@PathVariable @Positive Long id) {
  return ResponseEntity.status(HttpStatus.CONFLICT).cacheControl(NO_STORE).body(ApiResponse.error("Student fee assignment processing is outside the current fee-definition phase."));
 }
 private <T> ResponseEntity<T> ok(T body){return ResponseEntity.ok().cacheControl(NO_STORE).body(body);}
}
