package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.dto.ApplicationInterviewCompleteRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationInterviewResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationInterviewScheduleRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationInterviewWaitlistResultRequestDTO;
import com.erp.montfortuganda.auth.service.CurrentUserContext;

public interface ApplicationInterviewService {

    ApplicationInterviewResponseDTO getInterview(
            CurrentUserContext context,
            Long applicationId
    );

    ApplicationInterviewResponseDTO scheduleInterview(
            CurrentUserContext context,
            Long applicationId,
            ApplicationInterviewScheduleRequestDTO request
    );

    ApplicationInterviewResponseDTO rescheduleInterview(
            CurrentUserContext context,
            Long applicationId,
            ApplicationInterviewScheduleRequestDTO request
    );

    ApplicationInterviewResponseDTO startInterview(
            CurrentUserContext context,
            Long applicationId
    );

    ApplicationInterviewResponseDTO completeInterview(
            CurrentUserContext context,
            Long applicationId,
            ApplicationInterviewCompleteRequestDTO request
    );

    ApplicationInterviewResponseDTO updateWaitlistResult(
            CurrentUserContext context,
            Long applicationId,
            ApplicationInterviewWaitlistResultRequestDTO request
    );
}
