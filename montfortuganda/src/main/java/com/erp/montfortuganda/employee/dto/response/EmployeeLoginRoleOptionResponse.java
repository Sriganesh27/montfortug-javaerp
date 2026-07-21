package com.erp.montfortuganda.employee.dto.response;

/**
 * Lightweight assignable login-role option returned to the Branch Admin
 * Employee screens.
 *
 * <p>Only active, non-protected roles are returned. The frontend displays the
 * role name and submits only the role ID when creating an Employee account.</p>
 */
@SuppressWarnings("unused")
public record EmployeeLoginRoleOptionResponse(

        Long roleId,

        String roleCode,

        String roleName
) {
}
