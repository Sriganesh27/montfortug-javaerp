package com.erp.montfortuganda.fees.dto;

import java.util.List;

/** Page metadata and normalized rows from both fee-definition sources. Page numbers are zero-based. */
public record FeeStructureListResponse(
        List<CombinedFeeListItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious
) { }
