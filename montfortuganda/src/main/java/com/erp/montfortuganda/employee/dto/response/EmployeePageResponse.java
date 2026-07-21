package com.erp.montfortuganda.employee.dto.response;

import java.util.List;

/**
 * Paginated Employee search response returned by the branch-scoped Employee
 * search API.

 * The field names intentionally match the current Employee frontend:
 * content, pageNumber, totalPages and totalElements.
 */
@SuppressWarnings("unused")
public record EmployeePageResponse(

        List<EmployeeSummaryResponse> content,

        Integer pageNumber,

        Integer pageSize,

        Long totalElements,

        Integer totalPages,

        Integer numberOfElements,

        Boolean first,

        Boolean last,

        Boolean empty,

        String sortBy,

        String sortDirection
) {

    public EmployeePageResponse {
        content = content == null
                ? List.of()
                : List.copyOf(content);

        pageNumber = pageNumber == null
                ? 0
                : pageNumber;

        pageSize = pageSize == null
                ? content.size()
                : pageSize;

        totalElements = totalElements == null
                ? (long) content.size()
                : totalElements;

        totalPages = totalPages == null
                ? 0
                : totalPages;

        numberOfElements = numberOfElements == null
                ? content.size()
                : numberOfElements;

        first = first == null
                ? pageNumber == 0
                : first;

        last = last == null
                ? totalPages == 0
                  || pageNumber >= totalPages - 1
                : last;

        empty = empty == null
                ? content.isEmpty()
                : empty;
    }
}