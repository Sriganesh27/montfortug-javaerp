package com.erp.montfortuganda.fees.dto;

import jakarta.validation.constraints.Size;

public record FeeStructureUpdateRequest(
        @Size(max = 500) String remarks
) {
}
