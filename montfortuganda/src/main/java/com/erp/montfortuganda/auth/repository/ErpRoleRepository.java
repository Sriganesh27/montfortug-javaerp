package com.erp.montfortuganda.auth.repository;

import com.erp.montfortuganda.auth.ErpRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ErpRoleRepository extends JpaRepository<ErpRole, Integer> {
    Optional<ErpRole> findByRoleCode(String roleCode);
}