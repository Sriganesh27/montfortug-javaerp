package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.school.dto.SectionRequest;
import com.erp.montfortuganda.school.dto.SectionResponse;
import com.erp.montfortuganda.school.entity.ErpSection;

import java.util.List;

/**
 * Business operations for branch-owned Sections.
 *
 * <p>Implementations must resolve the branch from the authenticated user and
 * must verify that the selected Academic Year belongs to that same branch.</p>
 */
public interface SectionService {

    /**
     * Creates a Section for the authenticated branch.
     */
    SectionResponse createSection(
            SectionRequest request
    );

    /**
     * Updates a Section only when it belongs to the authenticated branch.
     */
    SectionResponse updateSection(
            Long sectionId,
            SectionRequest request
    );

    /**
     * Returns one Section owned by the authenticated branch.
     */
    SectionResponse getSection(
            Long sectionId
    );

    /**
     * Returns all Sections of the authenticated branch.
     */
    List<SectionResponse> getSections();

    /**
     * Returns active Sections of the authenticated branch.
     */
    List<SectionResponse> getActiveSections();

    /**
     * Returns Sections for one Academic Year owned by the authenticated
     * branch.
     */
    List<SectionResponse> getSectionsByAcademicYear(
            Long academicYearId
    );

    /**
     * Returns active Sections for one Academic Year owned by the authenticated
     * branch.
     */
    List<SectionResponse> getActiveSectionsByAcademicYear(
            Long academicYearId
    );

    /**
     * Returns Sections for one Class and Academic Year in the authenticated
     * branch.
     */
    List<SectionResponse> getSectionsByAcademicYearAndClass(
            Long academicYearId,
            Integer classId
    );

    /**
     * Returns active Sections for one Class and Academic Year in the
     * authenticated branch.
     */
    List<SectionResponse> getActiveSectionsByAcademicYearAndClass(
            Long academicYearId,
            Integer classId
    );

    /**
     * Returns Sections of the authenticated branch filtered by status.
     */
    List<SectionResponse> getSectionsByStatus(
            ErpSection.Status status
    );

    /**
     * Activates or deactivates a Section owned by the authenticated branch.
     */
    SectionResponse changeSectionActiveStatus(
            Long sectionId,
            boolean active,
            Long version
    );
}