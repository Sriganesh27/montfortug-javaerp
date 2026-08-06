package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.dto.ApplicationSummaryDTO;
import com.erp.montfortuganda.admission.dto.BranchApplicationDetailsResponseDTO;
import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.entity.ErpApplicationDocument;
import com.erp.montfortuganda.admission.entity.ErpApplicationDocumentRequest;
import com.erp.montfortuganda.admission.entity.ErpApplicationStatusHistory;
import com.erp.montfortuganda.admission.mapper.ApplicationMapper;
import com.erp.montfortuganda.admission.mapper.BranchApplicationDetailsMapper;
import com.erp.montfortuganda.admission.repository.ErpApplicationDocumentRepository;
import com.erp.montfortuganda.admission.repository.ErpApplicationDocumentRequestRepository;
import com.erp.montfortuganda.admission.repository.ErpApplicationRepository;
import com.erp.montfortuganda.admission.repository.ErpApplicationStatusHistoryRepository;
import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.exception.BadRequestException;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.school.entity.ErpAcademicTerm;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import com.erp.montfortuganda.school.entity.SchoolClass;
import com.erp.montfortuganda.school.repository.AcademicTermRepository;
import com.erp.montfortuganda.school.repository.AcademicYearRepository;
import com.erp.montfortuganda.school.repository.SchoolClassRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Branch-scoped admission application service.
 *
 * <p>All application access is restricted to the branch derived from the
 * authenticated user context. Browser-supplied branch IDs are not accepted.</p>
 */
@Service
public class BranchAdmissionServiceImpl
        implements BranchAdmissionService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ErpApplicationRepository applicationRepository;
    private final ErpApplicationDocumentRepository documentRepository;
    private final ErpApplicationDocumentRequestRepository
            documentRequestRepository;
    private final ErpApplicationStatusHistoryRepository
            statusHistoryRepository;

    private final AcademicYearRepository academicYearRepository;
    private final AcademicTermRepository academicTermRepository;
    private final SchoolClassRepository schoolClassRepository;

    private final ApplicationMapper applicationMapper;
    private final BranchApplicationDetailsMapper
            applicationDetailsMapper;
    private final BranchAccessService branchAccessService;

    public BranchAdmissionServiceImpl(
            ErpApplicationRepository applicationRepository,
            ErpApplicationDocumentRepository documentRepository,
            ErpApplicationDocumentRequestRepository
                    documentRequestRepository,
            ErpApplicationStatusHistoryRepository
                    statusHistoryRepository,
            AcademicYearRepository academicYearRepository,
            AcademicTermRepository academicTermRepository,
            SchoolClassRepository schoolClassRepository,
            ApplicationMapper applicationMapper,
            BranchApplicationDetailsMapper
                    applicationDetailsMapper,
            BranchAccessService branchAccessService
    ) {
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
        this.documentRequestRepository =
                documentRequestRepository;
        this.statusHistoryRepository =
                statusHistoryRepository;
        this.academicYearRepository =
                academicYearRepository;
        this.academicTermRepository =
                academicTermRepository;
        this.schoolClassRepository =
                schoolClassRepository;
        this.applicationMapper = applicationMapper;
        this.applicationDetailsMapper =
                applicationDetailsMapper;
        this.branchAccessService = branchAccessService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationSummaryDTO> getBranchApplications(
            CurrentUserContext context,
            int page,
            int size
    ) {
        Integer branchId =
                branchAccessService.getValidatedBranchId(
                        context
                );

        if (page < 0) {
            throw new BadRequestException(
                    "Page number cannot be negative."
            );
        }

        if (size <= 0) {
            throw new BadRequestException(
                    "Page size must be greater than zero."
            );
        }

        int safePageSize =
                Math.min(size, MAX_PAGE_SIZE);

        Pageable pageable =
                PageRequest.of(
                        page,
                        safePageSize,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        Page<ErpApplication> applicationPage =
                applicationRepository
                        .findActiveByBranchId(
                                branchId,
                                pageable
                        );

        /*
         * Resolve every visible Class in one batch. The previous
         * compatibility mapper call did not receive a SchoolClass and
         * therefore returned "Not Available" for every row.
         */
        List<Integer> classIds =
                applicationPage.getContent()
                        .stream()
                        .map(
                                ErpApplication::getBranchClassId
                        )
                        .filter(
                                classId -> classId != null
                                        && classId > 0
                        )
                        .distinct()
                        .toList();

        Map<Integer, SchoolClass> classesById =
                schoolClassRepository
                        .findAllById(classIds)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        SchoolClass::getClassId,
                                        Function.identity()
                                )
                        );

        return applicationPage.map(
                application ->
                        applicationMapper.toSummaryDTO(
                                application,
                                classesById.get(
                                        application
                                                .getBranchClassId()
                                )
                        )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BranchApplicationDetailsResponseDTO
    getBranchApplicationDetails(
            CurrentUserContext context,
            Long applicationId
    ) {
        Integer branchId =
                branchAccessService.getValidatedBranchId(
                        context
                );

        Long validatedApplicationId =
                requirePositiveId(
                        applicationId,
                        "Application ID"
                );

        ErpApplication application =
                applicationRepository
                        .findActiveBranchApplication(
                                validatedApplicationId,
                                branchId
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Application was not found."
                                )
                        );

        ErpAcademicYear academicYear =
                academicYearRepository
                        .findByAcademicYearIdAndBranchBranchId(
                                application.getAcademicYearId(),
                                branchId
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Application academic year was not found."
                                )
                        );

        SchoolClass schoolClass =
                schoolClassRepository
                        .findById(
                                application.getBranchClassId()
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Application class was not found."
                                )
                        );

        ErpAcademicTerm joiningTerm =
                resolveJoiningTerm(
                        application,
                        branchId
                );

        List<ErpApplicationDocument> documents =
                documentRepository
                        .findAllByApplication_ApplicationIdAndApplication_Branch_BranchIdAndActiveTrueOrderByUploadedAtDesc(
                                validatedApplicationId,
                                branchId
                        );

        List<ErpApplicationDocumentRequest>
                documentRequests =
                documentRequestRepository
                        .findAllByApplication_ApplicationIdAndApplication_Branch_BranchIdAndActiveTrueOrderByRequestedAtDesc(
                                validatedApplicationId,
                                branchId
                        );

        List<ErpApplicationStatusHistory>
                statusHistory =
                statusHistoryRepository
                        .findByApplication_ApplicationIdOrderByChangedAtDesc(
                                validatedApplicationId
                        );

        return applicationDetailsMapper
                .toDetailsResponse(
                        application,
                        academicYear,
                        schoolClass,
                        joiningTerm,
                        documents,
                        documentRequests,
                        statusHistory
                );
    }

    private ErpAcademicTerm resolveJoiningTerm(
            ErpApplication application,
            Integer branchId
    ) {
        if (application.getJoiningTermId() == null) {
            return null;
        }

        return academicTermRepository
                .findByTermIdAndBranchId(
                        application.getJoiningTermId(),
                        branchId
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Application joining term was not found."
                        )
                );
    }

    private Long requirePositiveId(
            Long id,
            String fieldName
    ) {
        if (id == null || id <= 0) {
            throw new BadRequestException(
                    fieldName + " is invalid."
            );
        }

        return id;
    }
}
