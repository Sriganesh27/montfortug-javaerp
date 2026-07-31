package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.school.dto.AcademicYearRequest;
import com.erp.montfortuganda.school.dto.AcademicYearResponse;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;

import java.util.List;

/**
 * Business operations for branch-owned Academic Years.
 *
 * <p>Every method must resolve the branch from the authenticated user.
 * Browser-supplied branch IDs must never control Academic Year ownership.</p>
 */
public interface AcademicYearService {

    /**
     * Creates an Academic Year for the authenticated branch.
     */
    AcademicYearResponse createAcademicYear(
            AcademicYearRequest request
    );

    /**
     * Updates an Academic Year only when it belongs to the authenticated
     * branch.
     */
    AcademicYearResponse updateAcademicYear(
            Long academicYearId,
            AcademicYearRequest request
    );

    /**
     * Returns one Academic Year owned by the authenticated branch.
     */
    AcademicYearResponse getAcademicYear(
            Long academicYearId
    );

    /**
     * Returns all Academic Years of the authenticated branch.
     */
    List<AcademicYearResponse> getAcademicYears();

    /**
     * Returns active Academic Years of the authenticated branch.
     */
    List<AcademicYearResponse> getActiveAcademicYears();

    /**
     * Returns Academic Years of the authenticated branch filtered by status.
     */
    List<AcademicYearResponse> getAcademicYearsByStatus(
            ErpAcademicYear.Status status
    );

    /**
     * Returns the current active Academic Year of the authenticated branch.
     */
    AcademicYearResponse getCurrentAcademicYear();

    /**
     * Marks one Academic Year as current and clears the current flag from all
     * other Academic Years in the same branch.
     */
    AcademicYearResponse makeCurrentAcademicYear(
            Long academicYearId
    );

    /**
     * Activates or deactivates an Academic Year owned by the authenticated
     * branch.
     */
    AcademicYearResponse changeAcademicYearActiveStatus(
            Long academicYearId,
            boolean active,
            Long version
    );
}