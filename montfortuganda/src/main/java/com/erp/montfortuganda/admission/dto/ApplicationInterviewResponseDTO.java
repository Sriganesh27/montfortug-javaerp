package com.erp.montfortuganda.admission.dto;

import com.erp.montfortuganda.admission.entity.ErpApplicationInterview;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Complete Entrance Test / interview state returned to the Branch Admin UI.
 *
 * <p>Overall totals are derived from subject-wise marks and are therefore
 * response-only values.</p>
 */
public record ApplicationInterviewResponseDTO(

        Long applicationId,
        String applicationNo,

        Long interviewId,

        String currentStage,

        ErpApplicationInterview.Status status,
        ErpApplicationInterview.Result result,

        Long employeeId,
        String employeeNo,
        String employeeName,

        LocalDateTime scheduledAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,

        List<SubjectMark> marks,

        BigDecimal maximumMarks,
        BigDecimal obtainedMarks,
        BigDecimal percentage,

        String employeeRemarks,
        String internalRemarks,

        Long createdBy,
        LocalDateTime createdAt,
        Long updatedBy,
        LocalDateTime updatedAt,

        boolean canSchedule,
        boolean canReschedule,
        boolean canStart,
        boolean canComplete,
        boolean canProceedToFeeDiscussion
) {

    /**
     * One subject-wise mark row shown in the Entrance Test UI.
     */
    public record SubjectMark(

            Long interviewMarkId,

            Long subjectId,
            String subjectCode,
            String subjectName,
            String subjectShortName,

            BigDecimal maximumMarks,
            BigDecimal obtainedMarks,
            BigDecimal percentage,

            String remarks
    ) {
    }
}
