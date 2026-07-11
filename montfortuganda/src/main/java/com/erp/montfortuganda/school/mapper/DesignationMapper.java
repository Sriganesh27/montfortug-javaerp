package com.erp.montfortuganda.school.mapper;

import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.dto.DesignationDTO;
import com.erp.montfortuganda.school.entity.Designation;
import org.springframework.stereotype.Component;

@Component
public class DesignationMapper {

    public DesignationDTO toDto(Designation entity) {
        if (entity == null) return null;

        DesignationDTO dto = new DesignationDTO();
        dto.setId(entity.getDesignationId());
        dto.setDesignationCode(entity.getDesignationCode());
        dto.setDesignationName(entity.getDesignationName());
        dto.setDescription(entity.getDescription());

        try {
            dto.setStatus(RecordStatus.valueOf(entity.getStatus()));
        } catch (IllegalArgumentException | NullPointerException e) {
            dto.setStatus(RecordStatus.ACTIVE);
        }

        dto.setActive(entity.getActive());
        dto.setVersion(entity.getVersion());

        return dto;
    }

    public Designation toEntity(DesignationDTO dto) {
        if (dto == null) return null;

        Designation entity = new Designation();
        entity.setDesignationCode(dto.getDesignationCode());
        entity.setDesignationName(dto.getDesignationName());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus().name() : "ACTIVE");
        entity.setActive(dto.getActive() != null ? dto.getActive() : true);

        return entity;
    }

    public void updateEntityFromDto(DesignationDTO dto, Designation entity) {
        if (dto.getDesignationCode() != null) entity.setDesignationCode(dto.getDesignationCode());
        if (dto.getDesignationName() != null) entity.setDesignationName(dto.getDesignationName());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus().name());
        if (dto.getActive() != null) entity.setActive(dto.getActive());
    }
}