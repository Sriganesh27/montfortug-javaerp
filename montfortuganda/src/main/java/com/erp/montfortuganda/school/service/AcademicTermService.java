package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.school.dto.AcademicTermRequest;
import com.erp.montfortuganda.school.dto.AcademicTermResponse;
import com.erp.montfortuganda.school.entity.ErpAcademicTerm;

import java.util.List;

/**
 * Business operations for Academic Terms.
 *
 * <p>Terms inherit branch ownership through their parent Academic Year.
 * Implementations must verify that the Academic Year belongs to the
 * authenticated branch before reading or modifying a Term.</p>
 */
public interface AcademicTermService {

    /**
     * Creates an Academic Term under an Academic Year owned by the
     * authenticated branch.
     */
    AcademicTermResponse createAcademicTerm(
            AcademicTermRequest request
    );

    /**
     * Updates an Academic Term only when its Academic Year belongs to the
     * authenticated branch.
     */
    AcademicTermResponse updateAcademicTerm(
            Long termId,
            AcademicTermRequest request
    );

    /**
     * Returns one Academic Term visible to the authenticated branch.
     */
    AcademicTermResponse getAcademicTerm(
            Long termId
    );

    /**
     * Returns all Terms under one Academic Year owned by the authenticated
     * branch.
     */
    List<AcademicTermResponse> getAcademicTerms(
            Long academicYearId
    );

    /**
     * Returns active Terms under one Academic Year owned by the authenticated
     * branch.
     */
    List<AcademicTermResponse> getActiveAcademicTerms(
            Long academicYearId
    );

    /**
     * Returns Terms under one Academic Year filtered by status.
     */
    List<AcademicTermResponse> getAcademicTermsByStatus(
            Long academicYearId,
            ErpAcademicTerm.Status status
    );

    /**
     * Returns the current active Term for the authenticated branch's current
     * Academic Year.
     */
    AcademicTermResponse getCurrentAcademicTerm();

    /**
     * Makes one Term current inside its Academic Year and clears the current
     * flag from the other Terms of that same Academic Year.
     */
    AcademicTermResponse makeCurrentAcademicTerm(
            Long termId
    );

    /**
     * Activates or deactivates a Term visible to the authenticated branch.
     */
    AcademicTermResponse changeAcademicTermActiveStatus(
            Long termId,
            boolean active,
            Long version
    );
}