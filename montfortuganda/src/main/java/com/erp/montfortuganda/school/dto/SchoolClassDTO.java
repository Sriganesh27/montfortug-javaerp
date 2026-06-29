package com.erp.montfortuganda.school.dto;
import lombok.Data;

@Data
public class SchoolClassDTO {
    private Integer classId;
    private Integer levelId;
    private String classCode;
    private String className;

    public SchoolClassDTO(Integer classId, Integer levelId, String classCode, String className) {
        this.classId = classId;
        this.levelId = levelId;
        this.classCode = classCode;
        this.className = className;
    }
}