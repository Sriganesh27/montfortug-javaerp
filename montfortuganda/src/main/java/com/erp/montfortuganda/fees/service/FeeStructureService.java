package com.erp.montfortuganda.fees.service;

import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.fees.dto.*;
import com.erp.montfortuganda.fees.entity.*;
import com.erp.montfortuganda.fees.entity.ErpFeeStructure.StructureStatus;
import com.erp.montfortuganda.fees.repository.*;
import com.erp.montfortuganda.school.dto.SchoolClassResponse;
import com.erp.montfortuganda.school.entity.*;
import com.erp.montfortuganda.school.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service @RequiredArgsConstructor
@Transactional(readOnly=true)
public class FeeStructureService {
 private final ErpFeeStructureRepository structures;
 private final ErpFeeStructureItemRepository independentItems;
 private final BranchRepository branches;
 private final AcademicYearRepository years;
 private final SchoolClassRepository classes;
 private final LevelRepository levels;
 private final CurrentUserService currentUser;
 private final BranchAccessService branchAccess;

 public CombinedFeeListResponse list(Long yearId, Integer levelId, Integer classId,
   String status, String source, String search, Integer page, Integer size) {
  int p = page == null ? 0 : page;
  int s = size == null ? 10 : size;
  if (p < 0 || s < 1 || s > 100) {
   throw bad("Page must be >= 0 and size must be between 1 and 100.");
  }

  String normalizedSource = source == null || source.isBlank()
      ? null : source.trim().toUpperCase(Locale.ROOT);
  String normalizedStatus = status == null || status.isBlank()
      ? null : status.trim().toUpperCase(Locale.ROOT);
  if (normalizedStatus != null
      && !Set.of("DRAFT", "PUBLISHED", "ARCHIVED", "ACTIVE", "INACTIVE").contains(normalizedStatus)) {
   throw bad("Status must be DRAFT, PUBLISHED, ARCHIVED, ACTIVE, or INACTIVE.");
  }
  if (normalizedSource != null
      && !normalizedSource.equals("MANDATORY")
      && !normalizedSource.equals("OPTIONAL")) {
   throw bad("Source must be MANDATORY or OPTIONAL.");
  }

  Integer branch = currentBranch();
  String q = search == null || search.isBlank()
      ? null : search.trim().toLowerCase(Locale.ROOT);
  List<CombinedFeeListItemResponse> all = new ArrayList<>();

  if (normalizedSource == null || normalizedSource.equals("MANDATORY")) {
   structures.findAllByBranch_BranchId(branch).stream()
       .filter(e -> yearId == null || Objects.equals(e.getAcademicYear().getAcademicYearId(), yearId))
       .filter(e -> levelId == null || Objects.equals(e.getLevel().getLevelId(), levelId))
       .filter(e -> classId == null
           || Objects.equals(e.getStartClass().getClassId(), classId)
           || (e.getEndClass() != null && Objects.equals(e.getEndClass().getClassId(), classId)))
       .filter(e -> normalizedStatus == null
           || (normalizedStatus.equals("ACTIVE") && Boolean.TRUE.equals(e.getActive()))
           || (normalizedStatus.equals("INACTIVE") && !Boolean.TRUE.equals(e.getActive()))
           || (!normalizedStatus.equals("ACTIVE") && !normalizedStatus.equals("INACTIVE")
               && e.getStructureStatus() != null && e.getStructureStatus().name().equals(normalizedStatus)))
       .filter(e -> matchesSearch(q, e.getFeeName(), e.getFeeType(), e.getFeeCode(), e.getRemarks()))
       .map(this::mandatoryListResponse)
       .forEach(all::add);
  }

  // Optional fees are branch-wide definitions, so academic-year/level/class filters
  // must not hide them from the combined list. Those filters only apply to mandatory fees.
  if (normalizedSource == null || normalizedSource.equals("OPTIONAL")) {
   independentItems.findAllByBranch_BranchId(branch).stream()
       .filter(e -> normalizedStatus == null || (e.getStructureStatus() != null && e.getStructureStatus().name().equals(normalizedStatus)) || (normalizedStatus.equals("ACTIVE") && Boolean.TRUE.equals(e.getActive())) || (normalizedStatus.equals("INACTIVE") && !Boolean.TRUE.equals(e.getActive())))
       .filter(e -> matchesSearch(q, e.getFeeName(), e.getFeeType(), e.getFeeCode(), e.getRemarks()))
       .map(this::optionalListResponse)
       .forEach(all::add);
  }

  all.sort(Comparator
      .comparing(CombinedFeeListItemResponse::createdAt,
          Comparator.nullsLast(Comparator.reverseOrder()))
      .thenComparing(CombinedFeeListItemResponse::feeSource)
      .thenComparing(CombinedFeeListItemResponse::id, Comparator.nullsLast(Comparator.reverseOrder())));

  long total = all.size();
  int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / s);
  int from = (int) Math.min((long) p * s, total);
  int to = (int) Math.min((long) from + s, total);
  List<CombinedFeeListItemResponse> pageItems = List.copyOf(all.subList(from, to));
  return new CombinedFeeListResponse(pageItems, p, s, total, totalPages,
      p == 0, totalPages == 0 || p >= totalPages - 1,
      p < totalPages - 1, p > 0);
 }

 private boolean matchesSearch(String query, String... values) {
  if (query == null) return true;
  for (String value : values) {
   if (value != null && value.toLowerCase(Locale.ROOT).contains(query)) return true;
  }
  return false;
 }

 private CombinedFeeListItemResponse mandatoryListResponse(ErpFeeStructure e) {
  return new CombinedFeeListItemResponse(
      e.getFeeStructureId(), "MANDATORY",
      e.getAcademicYear().getAcademicYearId(),
      e.getAcademicYear().getAcademicYearCode(),
      e.getAcademicYear().getAcademicYearName(),
      e.getLevel().getLevelId(), e.getLevel().getLevelName(),
      e.getStartClass().getClassId(), e.getStartClass().getClassName(),
      e.getStartClass().getClassCode(),
      e.getEndClass() == null ? null : e.getEndClass().getClassId(),
      e.getEndClass() == null ? null : e.getEndClass().getClassName(),
      e.getEndClass() == null ? null : e.getEndClass().getClassCode(),
      e.getFeeName(), e.getFeeType(), e.getFeeCode(), e.getAmount(),
      e.getChargingFrequency() == null ? null : e.getChargingFrequency().name(),
      e.getVersionNo(), e.getStructureStatus() == null ? null : e.getStructureStatus().name(),
      e.getActive(), e.getPublishedAt(), e.getRemarks(), e.getCreatedAt(), e.getUpdatedAt());
 }

 private CombinedFeeListItemResponse optionalListResponse(ErpFeeStructureItem i) {
  return new CombinedFeeListItemResponse(
      i.getFeeStructureItemId(), "OPTIONAL",
      null, null, null, null, null, null, null, null, null, null, null,
      i.getFeeName(), i.getFeeType(), i.getFeeCode(), i.getAmount(),
      i.getChargingFrequency() == null ? null : i.getChargingFrequency().name(),
      null, i.getStructureStatus() == null ? null : i.getStructureStatus().name(),
      i.getActive(), null, i.getRemarks(), i.getCreatedAt(), i.getUpdatedAt());
 }

 public FeeStructureResponse get(Long id){return response(require(id,currentBranch()));}

 @Transactional public FeeStructureResponse create(FeeStructureCreateRequest r) {
  Integer branch=currentBranch(); Long uid=currentUserId();
  Branch b=branches.findById(branch).orElseThrow(()->missing("Branch not found."));
  ErpAcademicYear year=years.findByAcademicYearIdAndBranchBranchIdAndActiveTrue(r.academicYearId(),branch)
    .orElseThrow(()->bad("Select an active academic year belonging to this branch."));
  Level level=levels.findById(r.levelId()).orElseThrow(()->missing("Level not found."));
  if(level.getStatus()==null||level.getStatus()!=1) throw bad("Selected level is inactive.");
  SchoolClass start=classForBranch(r.startClassId(),branch,level);
  SchoolClass end=r.endClassId()==null?null:classForBranch(r.endClassId(),branch,level);
  if(end!=null && end.getDisplayOrder()!=null && start.getDisplayOrder()!=null && end.getDisplayOrder()<start.getDisplayOrder())
    throw bad("End class must be the same as or later than start class.");
  String name=required(r.feeName(),"Fee name"), type=required(r.feeType(),"Fee type").toUpperCase(Locale.ROOT), code=normalizeCode(r.feeCode());
  if(structures.existsByBranch_BranchIdAndAcademicYear_AcademicYearIdAndLevel_LevelIdAndStartClass_ClassIdAndFeeNameIgnoreCaseAndFeeTypeIgnoreCaseAndActiveTrue(
    branch,year.getAcademicYearId(),level.getLevelId(),start.getClassId(),name,type))
    throw new ResponseStatusException(HttpStatus.CONFLICT,"An active fee with this name and type already exists for the selected start class and academic year.");
  ErpFeeStructure e=new ErpFeeStructure(); e.setBranch(b);e.setAcademicYear(year);e.setLevel(level);e.setStartClass(start);e.setEndClass(end);
  e.setFeeName(name);e.setFeeType(type);e.setFeeCode(code);e.setAmount(money(r.amount()));e.setChargingFrequency(r.chargingFrequency());
  e.setVersionNo(nextVersion(branch,year.getAcademicYearId(),level.getLevelId(),start.getClassId(),name,type));
  e.setStructureStatus(StructureStatus.DRAFT);e.setActive(true);e.setRemarks(clean(r.remarks()));e.setCreatedBy(uid);e.setUpdatedBy(uid);
  return response(structures.saveAndFlush(e));
 }
 @Transactional public FeeStructureResponse update(Long id,FeeStructureUpdateRequest r) {
  Integer branch=currentBranch(); ErpFeeStructure e=require(id,branch); requireDraft(e);
  ErpAcademicYear year=years.findByAcademicYearIdAndBranchBranchIdAndActiveTrue(r.academicYearId(),branch)
    .orElseThrow(()->bad("Select an active academic year belonging to this branch."));
  Level level=levels.findById(r.levelId()).orElseThrow(()->missing("Level not found."));
  SchoolClass start=classForBranch(r.startClassId(),branch,level);
  SchoolClass end=r.endClassId()==null?null:classForBranch(r.endClassId(),branch,level);
  if(end!=null&&end.getDisplayOrder()!=null&&start.getDisplayOrder()!=null&&end.getDisplayOrder()<start.getDisplayOrder())
    throw bad("End class must be the same as or later than start class.");
  e.setAcademicYear(year);e.setLevel(level);e.setStartClass(start);e.setEndClass(end);
  e.setFeeName(required(r.feeName(),"Fee name"));e.setFeeType(required(r.feeType(),"Fee type").toUpperCase(Locale.ROOT));e.setFeeCode(normalizeCode(r.feeCode()));
  e.setAmount(money(r.amount()));e.setChargingFrequency(r.chargingFrequency());e.setRemarks(clean(r.remarks()));e.setUpdatedBy(currentUserId());
  return response(structures.saveAndFlush(e));
 }
 @Transactional public FeeStructureResponse publish(Long id) {
  ErpFeeStructure e=require(id,currentBranch());
  if(e.getStructureStatus()==StructureStatus.ARCHIVED) throw conflict("Archived fee definitions cannot be published.");
  if(e.getFeeName()==null||e.getFeeName().isBlank()||e.getFeeType()==null||e.getFeeType().isBlank()||e.getAmount()==null)
    throw bad("Fee name, type and amount are required before publishing.");
  e.setStructureStatus(StructureStatus.PUBLISHED);e.setPublishedAt(LocalDateTime.now());e.setUpdatedBy(currentUserId());
  return response(structures.saveAndFlush(e));
 }
 @Transactional public FeeStructureResponse archive(Long id) {
  ErpFeeStructure e=require(id,currentBranch());e.setStructureStatus(StructureStatus.ARCHIVED);e.setActive(false);e.setUpdatedBy(currentUserId());
  return response(structures.saveAndFlush(e));
 }
 @Transactional public FeeStructureResponse setMandatoryActive(Long id, boolean active) {
  ErpFeeStructure e = require(id, currentBranch());
  e.setActive(active);
  e.setUpdatedBy(currentUserId());
  return response(structures.saveAndFlush(e));
 }

 public List<SchoolClassResponse> availableClasses(Long yearId,Integer levelId) {
  Integer branch=currentBranch();
  if(levelId==null)throw bad("Select a level before loading classes.");
  if(yearId!=null) years.findByAcademicYearIdAndBranchBranchIdAndActiveTrue(yearId,branch)
    .orElseThrow(()->bad("Selected academic year is inactive or belongs to another branch."));
  Level level=levels.findById(levelId).orElseThrow(()->missing("Level not found."));
  if(level.getStatus()==null||level.getStatus()!=1)throw bad("Selected level is inactive.");
  List<SchoolClass> result=yearId==null?classes.findPublicClassesByBranchAndLevel(branch,levelId,1):
    classes.findAvailableForBranchAndAcademicYear(branch,yearId,levelId,1);
  return result.stream().map(SchoolClassResponse::fromEntity).toList();
 }

 public List<FeeStructureItemResponse> listIndependentItems(boolean activeOnly) {
  Integer branch=currentBranch();
  List<ErpFeeStructureItem> items = activeOnly
      ? independentItems.findAllByBranch_BranchIdAndActiveTrue(branch)
      : independentItems.findAllByBranch_BranchId(branch);
  items.sort(Comparator.comparing(ErpFeeStructureItem::getCreatedAt,
      Comparator.nullsLast(Comparator.reverseOrder()))
      .thenComparing(ErpFeeStructureItem::getFeeStructureItemId,
          Comparator.nullsLast(Comparator.reverseOrder())));
  return items.stream().map(this::itemResponse).toList();
 }
 @Transactional public FeeStructureItemResponse createIndependentItem(FeeStructureItemRequest r) {
  Integer branch=currentBranch();String type=required(r.feeType(),"Fee type").toUpperCase(Locale.ROOT),name=required(r.feeName(),"Fee name");
  if(independentItems.existsByBranch_BranchIdAndFeeTypeIgnoreCaseAndFeeNameIgnoreCaseAndActiveTrue(branch,type,name))
    throw conflict("An active independent fee with this type and name already exists in this branch.");
  ErpFeeStructureItem i=new ErpFeeStructureItem();i.setBranch(branches.findById(branch).orElseThrow(()->missing("Branch not found.")));
  applyItem(i,r,type,name);i.setStructureStatus(r.structureStatus());i.setActive(true);i.setCreatedBy(currentUserId());i.setUpdatedBy(currentUserId());
  return itemResponse(independentItems.saveAndFlush(i));
 }
 @Transactional public FeeStructureItemResponse updateIndependentItem(Long id,FeeStructureItemRequest r) {
  ErpFeeStructureItem i=independentItems.findByFeeStructureItemIdAndBranch_BranchId(id,currentBranch()).orElseThrow(()->missing("Independent fee not found."));
  i.setFeeType(required(r.feeType(),"Fee type").toUpperCase(Locale.ROOT));i.setFeeCode(normalizeCode(r.feeCode()));i.setFeeName(required(r.feeName(),"Fee name"));
  applyItem(i,r,i.getFeeType(),i.getFeeName());i.setStructureStatus(r.structureStatus());i.setUpdatedBy(currentUserId());return itemResponse(independentItems.saveAndFlush(i));
 }
 @Transactional public FeeStructureItemResponse deactivateIndependentItem(Long id) {
  ErpFeeStructureItem i=independentItems.findByFeeStructureItemIdAndBranch_BranchId(id,currentBranch()).orElseThrow(()->missing("Independent fee not found."));
  i.setActive(false);i.setUpdatedBy(currentUserId());return itemResponse(independentItems.saveAndFlush(i));
 }
 @Transactional public FeeStructureItemResponse setIndependentItemActive(Long id, boolean active) {
  ErpFeeStructureItem i = independentItems
      .findByFeeStructureItemIdAndBranch_BranchId(id, currentBranch())
      .orElseThrow(() -> missing("Independent fee not found."));
  i.setActive(active);
  i.setUpdatedBy(currentUserId());
  return itemResponse(independentItems.saveAndFlush(i));
 }
 private void applyItem(ErpFeeStructureItem i,FeeStructureItemRequest r,String type,String name) {
  i.setFeeType(type);
  i.setFeeCode(normalizeCode(r.feeCode()));
  i.setFeeName(name);
  i.setAmount(money(r.amount()));
  i.setChargingFrequency(r.chargingFrequency());
  i.setRemarks(clean(r.remarks()));
 }
 private Integer nextVersion(Integer b,Long y,Integer l,Integer c,String n,String t) {
  Integer v=structures.maxVersion(b,y,l,c,n,t);return (v==null?0:v)+1;
 }
 private SchoolClass classForBranch(Integer id,Integer branch,Level level) {
  SchoolClass c=classes.findByClassIdAndBranch_BranchId(id,branch).orElseThrow(()->bad("Selected class does not belong to this branch."));
  if(c.getLevel()==null||!Objects.equals(c.getLevel().getLevelId(),level.getLevelId()))throw bad("Selected class does not belong to the selected level.");
  if(c.getStatus()==null||c.getStatus()!=1)throw bad("Selected class is inactive.");
  return c;
 }
 private ErpFeeStructure require(Long id,Integer branch){return structures.findByFeeStructureIdAndBranch_BranchId(id,branch).orElseThrow(()->missing("Fee definition not found for this branch."));}
 private void requireDraft(ErpFeeStructure e){if(e.getStructureStatus()!=StructureStatus.DRAFT)throw conflict("Only draft fee definitions can be edited.");}
 private FeeStructureResponse response(ErpFeeStructure e){return new FeeStructureResponse(e.getFeeStructureId(),e.getBranch().getBranchId(),
  e.getAcademicYear().getAcademicYearId(),e.getAcademicYear().getAcademicYearCode(),e.getAcademicYear().getAcademicYearName(),
  e.getLevel().getLevelId(),e.getLevel().getLevelName(),e.getStartClass().getClassId(),e.getStartClass().getClassName(),
  e.getEndClass()==null?null:e.getEndClass().getClassId(),e.getEndClass()==null?null:e.getEndClass().getClassName(),
  e.getFeeName(),e.getFeeType(),e.getFeeCode(),e.getAmount(),e.getChargingFrequency(),e.getVersionNo(),e.getStructureStatus(),e.getActive(),
  e.getPublishedAt(),e.getRemarks(),e.getRowVersion(),e.getCreatedAt(),e.getUpdatedAt());}
 private FeeStructureListItemResponse listResponse(ErpFeeStructure e){return new FeeStructureListItemResponse(e.getFeeStructureId(),
  e.getAcademicYear().getAcademicYearId(),e.getAcademicYear().getAcademicYearCode(),e.getAcademicYear().getAcademicYearName(),
  e.getLevel().getLevelId(),e.getLevel().getLevelName(),e.getStartClass().getClassId(),e.getStartClass().getClassName(),
  e.getEndClass()==null?null:e.getEndClass().getClassId(),e.getEndClass()==null?null:e.getEndClass().getClassName(),
  e.getFeeName(),e.getFeeType(),e.getFeeCode(),e.getAmount(),e.getChargingFrequency(),e.getVersionNo(),e.getStructureStatus(),e.getActive(),
  e.getPublishedAt(),e.getCreatedAt(),e.getUpdatedAt());}
 private FeeStructureItemResponse itemResponse(ErpFeeStructureItem i){return new FeeStructureItemResponse(i.getFeeStructureItemId(),i.getBranch().getBranchId(),
  i.getFeeType(), i.getFeeCode(), i.getFeeName(), i.getAmount(), i.getChargingFrequency(),
  i.getStructureStatus(), i.getActive(), i.getRemarks(), i.getRowVersion(), i.getCreatedAt(), i.getUpdatedAt());}
 private Integer currentBranch(){return branchAccess.getValidatedBranchId(currentUser.getCurrentUserContext());}
 private Long currentUserId(){Integer id=currentUser.getCurrentUserContext().getUserId();return id==null?null:id.longValue();}
 private String required(String s,String label){if(s==null||s.isBlank())throw bad(label+" is required.");return s.trim();}
 private String clean(String s){return s==null||s.isBlank()?null:s.trim();}
 private String normalizeCode(String s){return required(s,"Fee code").toUpperCase(Locale.ROOT);}
 private BigDecimal money(BigDecimal a){if(a==null||a.compareTo(BigDecimal.ZERO)<0||a.stripTrailingZeros().scale()>2)throw bad("Amount must be non-negative and have at most two decimal places.");return a.setScale(2,java.math.RoundingMode.UNNECESSARY);}
 private ResponseStatusException bad(String m){return new ResponseStatusException(HttpStatus.BAD_REQUEST,m);}
 private ResponseStatusException missing(String m){return new ResponseStatusException(HttpStatus.NOT_FOUND,m);}
 private ResponseStatusException conflict(String m){return new ResponseStatusException(HttpStatus.CONFLICT,m);}
}
