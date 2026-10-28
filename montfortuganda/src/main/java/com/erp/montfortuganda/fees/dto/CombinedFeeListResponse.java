package com.erp.montfortuganda.fees.dto;

import java.util.List;

/** Pagination metadata for the unified mandatory/optional fee list. Page numbers are zero-based. */
public record CombinedFeeListResponse(
        List<CombinedFeeListItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious
) {
}
