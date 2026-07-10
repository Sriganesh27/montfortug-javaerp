package com.erp.montfortuganda.school.mapper;

import com.erp.montfortuganda.school.ErpDepartment;
import com.erp.montfortuganda.school.dto.DepartmentDTO;
import com.erp.montfortuganda.school.projection.DepartmentSearchProjection;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public DepartmentDTO toDTO(ErpDepartment entity) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setDepartmentId(entity.getDepartmentId());
        dto.setDepartmentCode(entity.getDepartmentCode());
        dto.setDepartmentName(entity.getDepartmentName());
        dto.setDepartmentType(entity.getDepartmentType());
        dto.setDescription(entity.getDescription());
        dto.setActive(entity.getActive());
        dto.setDisplayOrder(entity.getDisplayOrder());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public DepartmentDTO toDTO(DepartmentSearchProjection projection) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setDepartmentId(projection.getDepartmentId());
        dto.setDepartmentCode(projection.getDepartmentCode());
        dto.setDepartmentName(projection.getDepartmentName());
        dto.setDepartmentType(projection.getDepartmentType());
        dto.setDescription(projection.getDescription());
        dto.setActive(projection.getActive());
        dto.setDisplayOrder(projection.getDisplayOrder());
        dto.setCreatedAt(projection.getCreatedAt());
        dto.setUpdatedAt(projection.getUpdatedAt());
        dto.setDesignationCount(projection.getDesignationCount() != null ? projection.getDesignationCount() : 0);
        dto.setEmployeeCount(projection.getEmployeeCount() != null ? projection.getEmployeeCount() : 0);
        return dto;
    }
}