package com.erp.montfortuganda.school.mapper;

import com.erp.montfortuganda.school.dto.DepartmentDTO;
import com.erp.montfortuganda.school.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public DepartmentDTO toDto(Department entity) {
        if (entity == null) return null;

        DepartmentDTO dto = new DepartmentDTO();
        dto.setDepartmentId(entity.getDepartmentId());

        if (entity.getBranch() != null) {
            dto.setBranchId(entity.getBranch().getBranchId());
        }

        dto.setDepartmentCode(entity.getDepartmentCode());
        dto.setDepartmentName(entity.getDepartmentName());
        dto.setIsAcademic(entity.getIsAcademic());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setActive(entity.getActive());
        dto.setVersion(entity.getVersion());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        return dto;
    }

    public void updateEntityFromDto(DepartmentDTO dto, Department entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setDepartmentCode(dto.getDepartmentCode().trim().toUpperCase());
        entity.setDepartmentName(dto.getDepartmentName().trim());

        if (dto.getIsAcademic() != null) {
            entity.setIsAcademic(dto.getIsAcademic());
        }

        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription().trim());
        }

        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }

        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }

        if (dto.getVersion() != null) {
            entity.setVersion(dto.getVersion());
        }
    }
}