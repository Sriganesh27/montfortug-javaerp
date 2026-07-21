package com.erp.montfortuganda.auth.repository;

import com.erp.montfortuganda.auth.entity.ErpRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ErpRoleRepository extends JpaRepository<ErpRole, Long> {

    Optional<ErpRole> findByRoleCode(
            String roleCode
    );

    /**
     * Returns active roles in display order for secure server-side filtering
     * before exposing Employee login-role options.
     */
    List<ErpRole> findAllByActiveTrueOrderByRoleNameAsc();
}
