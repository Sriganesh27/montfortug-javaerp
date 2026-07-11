package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.common.dto.PagedResponse;
import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.dto.DesignationDTO;
import org.springframework.data.domain.Pageable;

public interface DesignationService {
    DesignationDTO createDesignation(DesignationDTO dto);
    DesignationDTO updateDesignation(Long id, DesignationDTO dto);
    DesignationDTO getDesignationById(Long id);
    PagedResponse<DesignationDTO> searchDesignations(String keyword, Integer branchId, RecordStatus status, Boolean active, Pageable pageable);
}