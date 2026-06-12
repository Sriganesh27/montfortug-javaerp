package com.montfort.erp.modules.finance.repository;

import com.montfort.erp.modules.finance.entity.StudentAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentAccountRepository extends JpaRepository<StudentAccount, Long> {
    Optional<StudentAccount> findByUsernameAndBranchIdAndIsActive(String username, Long branchId, Integer isActive);
    Optional<StudentAccount> findByUsernameAndIsActive(String username, Integer isActive);
}

