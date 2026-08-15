package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.school.dto.SubjectRequestDTO;
import com.erp.montfortuganda.school.dto.SubjectResponseDTO;

import java.util.List;

/**
 * Business operations for branch-owned Subjects.
 *
 * <p>Implementations must always resolve the branch from the authenticated
 * user. Browser supplied branch identifiers must never be trusted.</p>
 */
public interface SubjectService {

    /**
     * Creates a Subject for the authenticated branch.
     */
    SubjectResponseDTO createSubject(
            SubjectRequestDTO request
    );

    /**
     * Updates a Subject only when it belongs to the authenticated branch.
     */
    SubjectResponseDTO updateSubject(
            Long subjectId,
            SubjectRequestDTO request
    );

    /**
     * Returns one Subject owned by the authenticated branch.
     */
    SubjectResponseDTO getSubject(
            Long subjectId
    );

    /**
     * Returns all non-deleted Subjects for the authenticated branch.
     */
    List<SubjectResponseDTO> getSubjects();

    /**
     * Returns only ACTIVE Subjects for the authenticated branch.
     *
     * This is the list Entrance Test mark entry should use.
     */
    List<SubjectResponseDTO> getActiveSubjects();

    /**
     * Activates or deactivates a Subject owned by the authenticated branch.
     */
    SubjectResponseDTO changeSubjectActiveStatus(
            Long subjectId,
            boolean active
    );
}
