package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.BranchNotAssignedException;
import com.erp.montfortuganda.exception.DuplicateResourceException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.school.dto.SchoolClassRequest;
import com.erp.montfortuganda.school.dto.SchoolClassUpdateRequest;
import com.erp.montfortuganda.school.dto.SchoolClassResponse;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import com.erp.montfortuganda.school.entity.Level;
import com.erp.montfortuganda.school.entity.SchoolClass;
import com.erp.montfortuganda.school.repository.AcademicYearRepository;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.school.repository.LevelRepository;
import com.erp.montfortuganda.school.repository.SchoolClassRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class SchoolClassManagementService {
    private final SchoolClassRepository schoolClassRepository;
    private final AcademicYearRepository academicYearRepository;
    private final LevelRepository levelRepository;
    private final BranchRepository branchRepository;
    private final CurrentUserService currentUserService;

    public SchoolClassManagementService(
            SchoolClassRepository schoolClassRepository,
            AcademicYearRepository academicYearRepository,
            LevelRepository levelRepository,
            BranchRepository branchRepository,
            CurrentUserService currentUserService) {
        this.schoolClassRepository = schoolClassRepository;
        this.academicYearRepository = academicYearRepository;
        this.levelRepository = levelRepository;
        this.branchRepository = branchRepository;
        this.currentUserService = currentUserService;
    }

    public List<SchoolClassResponse> listClasses() {
        Integer branchId = requireBranchId();
        return schoolClassRepository.findAllByBranch_BranchIdOrderByDisplayOrderAscClassIdAsc(branchId)
                .stream().map(SchoolClassResponse::fromEntity).toList();
    }

    /**
     * Lists active classes in the authenticated branch that were introduced
     * on or before the selected academic year. Level filtering is performed
     * by the repository, not by the frontend.
     */
    public List<SchoolClassResponse> listAvailableClasses(Long academicYearId, Integer levelId) {
        if (academicYearId == null || academicYearId <= 0) {
            throw new BadRequestException("A valid academic year is required.");
        }

        Integer branchId = requireBranchId();
        academicYearRepository
                .findByAcademicYearIdAndBranchBranchIdAndActiveTrue(academicYearId, branchId)
                .orElseThrow(() -> new BadRequestException(
                        "Selected academic year is inactive or does not belong to your branch."));

        if (levelId != null) {
            requireActiveLevel(levelId);
        }

        return schoolClassRepository
                .findAvailableForBranchAndAcademicYear(branchId, academicYearId, levelId, 1)
                .stream()
                .map(SchoolClassResponse::fromEntity)
                .toList();
    }

    public SchoolClassResponse getClass(Integer classId) {
        Integer branchId = requireBranchId();
        return SchoolClassResponse.fromEntity(requireOwnedClass(classId, branchId));
    }

    @Transactional
    public SchoolClassResponse createClass(SchoolClassRequest request) {
        validateRequest(request);
        Integer branchId = requireBranchId();
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated branch was not found."));
        if (branch.getIsActive() == null || branch.getIsActive() != 1) {
            throw new BadRequestException("Authenticated branch is inactive.");
        }

        String code = normalizeCode(request.classCode());
        validateUniqueCode(branchId, code, null);
        ErpAcademicYear year = academicYearRepository
                .findByAcademicYearIdAndBranchBranchId(request.academicYearId(), branchId)
                .orElseThrow(() -> new BadRequestException("Selected academic year does not belong to your branch."));
        Level level = requireActiveLevel(request.levelId());

        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setBranch(branch);
        schoolClass.setAcademicYear(year);
        Integer nextDisplayOrder = schoolClassRepository.findTopByBranch_BranchIdOrderByDisplayOrderDescClassIdDesc(branchId)
                .map(existing -> existing.getDisplayOrder() == null ? 1 : existing.getDisplayOrder() + 1)
                .orElse(1);
        schoolClass.setDisplayOrder(nextDisplayOrder);
        applyRequest(schoolClass, request, code, level);
        return SchoolClassResponse.fromEntity(schoolClassRepository.saveAndFlush(schoolClass));
    }

    @Transactional
    public SchoolClassResponse updateClass(Integer classId, SchoolClassUpdateRequest request) {
        validateRequest(request);
        Integer branchId = requireBranchId();
        SchoolClass schoolClass = requireOwnedClass(classId, branchId);
        String code = normalizeCode(request.classCode());
        validateUniqueCode(branchId, code, classId);
        ErpAcademicYear year = academicYearRepository
                .findByAcademicYearIdAndBranchBranchId(request.academicYearId(), branchId)
                .orElseThrow(() -> new BadRequestException("Selected academic year does not belong to your branch."));
        Level level = requireActiveLevel(request.levelId());
        schoolClass.setAcademicYear(year);
        schoolClass.setLevel(level);
        schoolClass.setClassCode(code);
        schoolClass.setClassName(request.className().trim());
        schoolClass.setStatus(request.status());
        return SchoolClassResponse.fromEntity(schoolClassRepository.saveAndFlush(schoolClass));
    }

    private void applyRequest(SchoolClass schoolClass, SchoolClassRequest request, String code, Level level) {
        schoolClass.setLevel(level);
        schoolClass.setClassCode(code);
        schoolClass.setClassName(request.className().trim());
        schoolClass.setStatus(request.status());
    }

    private void validateRequest(SchoolClassRequest request) {
        if (request == null) throw new BadRequestException("Class details are required.");
        validateStatus(request.status());
    }

    private void validateRequest(SchoolClassUpdateRequest request) {
        if (request == null) throw new BadRequestException("Class details are required.");
        validateStatus(request.status());
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BadRequestException("Status must be 1 (Active) or 0 (Inactive).");
        }
    }

    private void validateUniqueCode(Integer branchId, String code, Integer classId) {
        boolean duplicate = classId == null
                ? schoolClassRepository.existsByBranch_BranchIdAndClassCodeIgnoreCase(branchId, code)
                : schoolClassRepository.existsByBranch_BranchIdAndClassCodeIgnoreCaseAndClassIdNot(branchId, code, classId);
        if (duplicate) throw new DuplicateResourceException("A class with this code already exists in your branch.");
    }

    private Level requireActiveLevel(Integer levelId) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new ResourceNotFoundException("Selected level was not found."));
        if (level.getStatus() == null || level.getStatus() != 1) {
            throw new BadRequestException("Selected level is inactive.");
        }
        return level;
    }

    private SchoolClass requireOwnedClass(Integer classId, Integer branchId) {
        return schoolClassRepository.findByClassIdAndBranch_BranchId(classId, branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Class was not found in your branch."));
    }

    private Integer requireBranchId() {
        CurrentUserContext context = currentUserService.getCurrentUserContext();
        Integer branchId = context.getBranchId();
        if (branchId == null || branchId <= 0) {
            throw new BranchNotAssignedException("Authenticated user is not assigned to a branch.");
        }
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated branch was not found."));
        if (branch.getIsActive() == null || branch.getIsActive() != 1) {
            throw new BadRequestException("Authenticated branch is inactive.");
        }
        return branchId;
    }

    private String normalizeCode(String value) {
        if (value == null || value.isBlank()) throw new BadRequestException("Class code is required.");
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
