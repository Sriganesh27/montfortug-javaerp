package com.erp.montfortuganda.school.projection;

import com.erp.montfortuganda.school.enums.DepartmentType;
import java.time.LocalDateTime;

public interface DepartmentSearchProjection {
    Long getDepartmentId();
    String getDepartmentCode();
    String getDepartmentName();
    DepartmentType getDepartmentType();
    String getDescription();
    Boolean getActive();
    Integer getDisplayOrder();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    Long getDesignationCount();
    Long getEmployeeCount();
}