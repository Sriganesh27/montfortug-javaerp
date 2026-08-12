package com.erp.montfortuganda.admission.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight employee option used by Admission workflow dropdowns such as
 * School Visit and Entrance Test assignment.
 *
 * <p>Only non-sensitive display fields are exposed. Branch ownership and
 * employment eligibility are enforced by the backend service.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationEmployeeOptionDTO {

    private Long employeeId;
    private String employeeNo;
    private String fullName;
    private String departmentName;
    private String designationName;
}
