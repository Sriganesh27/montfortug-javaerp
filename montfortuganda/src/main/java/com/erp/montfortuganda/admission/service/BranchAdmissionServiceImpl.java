package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.entity.ErpApplication;
import com.erp.montfortuganda.admission.repository.ErpApplicationRepository;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.admission.dto.ApplicationSummaryDTO;
import com.erp.montfortuganda.admission.mapper.ApplicationMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BranchAdmissionServiceImpl implements BranchAdmissionService {

    private final ErpApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;
    private final BranchAccessService branchAccessService;

    public BranchAdmissionServiceImpl(
            ErpApplicationRepository applicationRepository,
            ApplicationMapper applicationMapper,
            BranchAccessService branchAccessService) {
        this.applicationRepository = applicationRepository;
        this.applicationMapper = applicationMapper;
        this.branchAccessService = branchAccessService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationSummaryDTO> getBranchApplications(CurrentUserContext ctx, int page, int size) {

        Integer branchId = branchAccessService.getValidatedBranchId(ctx);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ErpApplication> appsPage = applicationRepository.findByBranch_BranchId(branchId, pageable);

        return appsPage.map(applicationMapper::toSummaryDTO);
    }
}