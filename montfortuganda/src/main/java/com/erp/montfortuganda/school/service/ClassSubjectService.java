package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.BranchNotAssignedException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.school.dto.ClassSubjectRequest;
import com.erp.montfortuganda.school.dto.ClassSubjectResponse;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import com.erp.montfortuganda.school.entity.ErpClassSubject;
import com.erp.montfortuganda.school.entity.ErpSubject;
import com.erp.montfortuganda.school.entity.Level;
import com.erp.montfortuganda.school.entity.SchoolClass;
import com.erp.montfortuganda.school.repository.AcademicYearRepository;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.school.repository.ErpClassSubjectRepository;
import com.erp.montfortuganda.school.repository.ErpSubjectRepository;
import com.erp.montfortuganda.school.repository.LevelRepository;
import com.erp.montfortuganda.school.repository.SchoolClassRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClassSubjectService {

    private final ErpClassSubjectRepository classSubjectRepository;
    private final BranchRepository branchRepository;
    private final AcademicYearRepository academicYearRepository;
    private final ErpSubjectRepository subjectRepository;
    private final SchoolClassRepository classRepository;
    private final LevelRepository levelRepository;
    private final CurrentUserService currentUserService;

    public ClassSubjectService(
            ErpClassSubjectRepository classSubjectRepository,
            BranchRepository branchRepository,
            AcademicYearRepository academicYearRepository,
            ErpSubjectRepository subjectRepository,
            SchoolClassRepository classRepository,
            LevelRepository levelRepository,
            CurrentUserService currentUserService) {
        this.classSubjectRepository = classSubjectRepository;
        this.branchRepository = branchRepository;
        this.academicYearRepository = academicYearRepository;
        this.subjectRepository = subjectRepository;
        this.classRepository = classRepository;
        this.levelRepository = levelRepository;
        this.currentUserService = currentUserService;
    }

    public List<ClassSubjectResponse> list(Long academicYearId) {
        Integer branchId = requireBranchId();
        requireAcademicYear(academicYearId, branchId);
        return classSubjectRepository
                .findAllByBranch_BranchIdAndAcademicYear_AcademicYearIdAndActiveTrueOrderByLevel_DisplayOrderAscStartClass_DisplayOrderAscSubject_DisplayOrderAsc(
                        branchId, academicYearId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public ClassSubjectResponse create(ClassSubjectRequest request) {
        if (request == null) throw new BadRequestException("Subject configuration details are required.");
        Integer branchId = requireBranchId();
        CurrentUserContext user = currentUserService.getCurrentUserContext();
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BranchNotAssignedException("Authenticated branch was not found."));
        ErpAcademicYear year = requireAcademicYear(request.academicYearId(), branchId);
        Level level = requireLevel(request.levelId());
        ErpSubject subject = subjectRepository
                .findBySubjectIdAndBranch_BranchIdAndActiveTrue(request.subjectId(), branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject was not found in your school."));
        SchoolClass start = requireAvailableClass(request.startClassId(), branchId, year.getAcademicYearId());
        SchoolClass end = request.endClassId() == null ? null
                : requireAvailableClass(request.endClassId(), branchId, year.getAcademicYearId());
        validateLevelClasses(level, start, end);
        validateRange(start, end);
        validateNoOverlappingRule(branchId, year.getAcademicYearId(), level.getLevelId(), subject.getSubjectId(), start, end, null);

        ErpClassSubject entity = new ErpClassSubject();
        entity.setBranch(branch);
        entity.setAcademicYear(year);
        entity.setLevel(level);
        entity.setSubject(subject);
        entity.setStartClass(start);
        entity.setEndClass(end);
        entity.setSubjectRequirement(request.subjectRequirement());
        entity.setActive(true);
        entity.setCreatedBy(user.getUserId() == null ? null : user.getUserId().longValue());
        entity.setUpdatedBy(user.getUserId() == null ? null : user.getUserId().longValue());
        return toResponse(classSubjectRepository.saveAndFlush(entity));
    }

    @Transactional
    public ClassSubjectResponse update(Long id, ClassSubjectRequest request) {
        if (request == null) throw new BadRequestException("Subject configuration details are required.");
        Integer branchId = requireBranchId();
        CurrentUserContext user = currentUserService.getCurrentUserContext();
        ErpClassSubject entity = classSubjectRepository.findByClassSubjectIdAndBranch_BranchId(id, branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Class subject configuration was not found."));
        ErpAcademicYear year = requireAcademicYear(request.academicYearId(), branchId);
        Level level = requireLevel(request.levelId());
        ErpSubject subject = subjectRepository
                .findBySubjectIdAndBranch_BranchIdAndActiveTrue(request.subjectId(), branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject was not found in your school."));
        SchoolClass start = requireAvailableClass(request.startClassId(), branchId, year.getAcademicYearId());
        SchoolClass end = request.endClassId() == null ? null
                : requireAvailableClass(request.endClassId(), branchId, year.getAcademicYearId());
        validateLevelClasses(level, start, end);
        validateRange(start, end);
        validateNoOverlappingRule(branchId, year.getAcademicYearId(), level.getLevelId(), subject.getSubjectId(), start, end, id);

        entity.setAcademicYear(year);
        entity.setLevel(level);
        entity.setSubject(subject);
        entity.setStartClass(start);
        entity.setEndClass(end);
        entity.setSubjectRequirement(request.subjectRequirement());
        entity.setUpdatedBy(user.getUserId() == null ? null : user.getUserId().longValue());
        return toResponse(classSubjectRepository.saveAndFlush(entity));
    }

    @Transactional
    public void deactivate(Long id) {
        Integer branchId = requireBranchId();
        CurrentUserContext user = currentUserService.getCurrentUserContext();
        ErpClassSubject entity = classSubjectRepository.findByClassSubjectIdAndBranch_BranchId(id, branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Class subject configuration was not found."));
        entity.setActive(false);
        entity.setUpdatedBy(user.getUserId() == null ? null : user.getUserId().longValue());
        classSubjectRepository.saveAndFlush(entity);
    }

    private Integer requireBranchId() {
        Integer branchId = currentUserService.getCurrentUserContext().getBranchId();
        if (branchId == null || branchId <= 0) {
            throw new BranchNotAssignedException("Your account is not assigned to a school branch.");
        }
        return branchId;
    }

    private ErpAcademicYear requireAcademicYear(Long id, Integer branchId) {
        if (id == null || id <= 0) throw new BadRequestException("Academic year is required.");
        return academicYearRepository.findByAcademicYearIdAndBranchBranchIdAndActiveTrue(id, branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year was not found in your school."));
    }

    private Level requireLevel(Integer levelId) {
        if (levelId == null || levelId <= 0) {
            throw new BadRequestException("School level is required.");
        }
        Level level = levelRepository.findById(levelId)
                .filter(item -> item.getStatus() == null || item.getStatus() == 1)
                .orElseThrow(() -> new ResourceNotFoundException("School level was not found or is inactive."));
        return level;
    }

    private SchoolClass requireAvailableClass(Integer id, Integer branchId, Long academicYearId) {
        if (id == null || !classRepository.existsAvailableForBranchAndAcademicYear(id, branchId, academicYearId, 1)) {
            throw new ResourceNotFoundException("Class was not found or is not available for the selected academic year.");
        }
        return classRepository.findByClassIdAndBranch_BranchId(id, branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Class was not found in your school."));
    }

    private void validateLevelClasses(Level level, SchoolClass start, SchoolClass end) {
        if (start.getLevel() == null || !level.getLevelId().equals(start.getLevel().getLevelId())) {
            throw new BadRequestException("Starting class must belong to the selected level.");
        }
        if (end != null && (end.getLevel() == null || !level.getLevelId().equals(end.getLevel().getLevelId()))) {
            throw new BadRequestException("Ending class must belong to the selected level.");
        }
    }

    private void validateRange(SchoolClass start, SchoolClass end) {
        if (end != null && end.getDisplayOrder() < start.getDisplayOrder()) {
            throw new BadRequestException("Ending class must be the same as or after the starting class.");
        }
    }

    private void validateNoOverlappingRule(Integer branchId, Long yearId, Integer levelId, Long subjectId,
                                           SchoolClass start, SchoolClass end, Long ignoredId) {
        int proposedStart = start.getDisplayOrder();
        int proposedEnd = end == null ? proposedStart : end.getDisplayOrder();
        for (ErpClassSubject existing : classSubjectRepository
                .findAllByBranch_BranchIdAndAcademicYear_AcademicYearIdAndActiveTrue(branchId, yearId)) {
            if (ignoredId != null && ignoredId.equals(existing.getClassSubjectId())) continue;
            if (!subjectId.equals(existing.getSubject().getSubjectId())) continue;
            if (existing.getLevel() == null || !levelId.equals(existing.getLevel().getLevelId())) continue;
            int existingStart = existing.getStartClass().getDisplayOrder();
            int existingEnd = existing.getEndClass() == null
                    ? existingStart : existing.getEndClass().getDisplayOrder();
            if (proposedStart <= existingEnd && existingStart <= proposedEnd) {
                throw new BadRequestException("This subject already has an active configuration that overlaps the selected class range for this academic year.");
            }
        }
    }

    private ClassSubjectResponse toResponse(ErpClassSubject entity) {
        return new ClassSubjectResponse(
                entity.getClassSubjectId(),
                entity.getAcademicYear().getAcademicYearId(),
                entity.getAcademicYear().getAcademicYearName(),
                entity.getLevel().getLevelId(),
                entity.getLevel().getLevelName(),
                entity.getSubject().getSubjectId(),
                entity.getSubject().getSubjectCode(),
                entity.getSubject().getSubjectName(),
                entity.getStartClass().getClassId(),
                entity.getStartClass().getClassCode(),
                entity.getEndClass() == null ? null : entity.getEndClass().getClassId(),
                entity.getEndClass() == null ? null : entity.getEndClass().getClassCode(),
                entity.getSubjectRequirement(),
                entity.getVersion());
    }
}
