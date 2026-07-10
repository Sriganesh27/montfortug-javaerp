package com.erp.montfortuganda.school.mapper;

import com.erp.montfortuganda.school.ErpDesignation;
import com.erp.montfortuganda.school.dto.DesignationDTO;
import org.springframework.stereotype.Component;

@Component
public class DesignationMapper {
    public DesignationDTO toDTO(ErpDesignation entity) {
        DesignationDTO dto = new DesignationDTO();
        dto.setDesignationId(entity.getDesignationId());
        dto.setDesignationCode(entity.getDesignationCode());
        dto.setDesignationName(entity.getDesignationName());
        dto.setDescription(entity.getDescription());
        dto.setDisplayOrder(entity.getDisplayOrder());
        dto.setActive(entity.getActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}

