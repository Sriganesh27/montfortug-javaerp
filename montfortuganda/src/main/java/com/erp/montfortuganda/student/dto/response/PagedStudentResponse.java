package com.erp.montfortuganda.student.dto.response;

import java.util.List;

/**
 * Paginated response returned by the Student List API.
 */
public record PagedStudentResponse(

        List<StudentSummaryResponse> students,

        int page,

        int size,

        long totalElements,

        int totalPages,

        boolean first,

        boolean last,

        boolean hasNext,

        boolean hasPrevious

) {

    public PagedStudentResponse {

        students = students == null
                ? List.of()
                : List.copyOf(students);

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative."
            );
        }

        if (size < 1) {
            throw new IllegalArgumentException(
                    "Page size must be greater than zero."
            );
        }

        if (totalElements < 0) {
            throw new IllegalArgumentException(
                    "Total elements cannot be negative."
            );
        }

        if (totalPages < 0) {
            throw new IllegalArgumentException(
                    "Total pages cannot be negative."
            );
        }
    }
}