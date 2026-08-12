package com.erp.montfortuganda.admission.service;

import com.erp.montfortuganda.admission.dto.ApplicationSchoolVisitCompleteRequestDTO;
import com.erp.montfortuganda.admission.dto.ApplicationSchoolVisitResponseDTO;
import com.erp.montfortuganda.admission.dto.ApplicationSchoolVisitScheduleRequestDTO;
import com.erp.montfortuganda.auth.service.CurrentUserContext;

/**
 * Branch-scoped School Visit workflow operations for admission applications.
 *
 * <p>The authenticated user context is always supplied by the controller.
 * Implementations must resolve and validate Branch ownership on the server and
 * must never trust a browser-supplied Branch ID.</p>
 */
public interface ApplicationSchoolVisitService {

    ApplicationSchoolVisitResponseDTO getSchoolVisit(
            CurrentUserContext context,
            Long applicationId
    );

    /**
     * Schedules the School Visit date/time.
     *
     * <p>No employee is assigned during scheduling.</p>
     */
    ApplicationSchoolVisitResponseDTO scheduleSchoolVisit(
            CurrentUserContext context,
            Long applicationId,
            ApplicationSchoolVisitScheduleRequestDTO request
    );

    /**
     * Changes the planned School Visit date/time.
     */
    ApplicationSchoolVisitResponseDTO rescheduleSchoolVisit(
            CurrentUserContext context,
            Long applicationId,
            ApplicationSchoolVisitScheduleRequestDTO request
    );

    /**
     * Records the actual School Visit attendance details and responsible
     * employee when the parent/student arrives.
     *
     * <p>This existing method is reused for the visit-day action. It must not
     * send an applicant email. The implementation will mark the School Visit
     * as ATTENDED and move the application into the ENTRANCE_TEST stage.</p>
     */
    ApplicationSchoolVisitResponseDTO completeSchoolVisit(
            CurrentUserContext context,
            Long applicationId,
            ApplicationSchoolVisitCompleteRequestDTO request
    );
}
