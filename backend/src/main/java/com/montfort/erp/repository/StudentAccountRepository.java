package com.montfort.erp.repository;

import com.montfort.erp.entity.StudentAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentAccountRepository extends JpaRepository<StudentAccount, Long> {
    Optional<StudentAccount> findByUsernameAndBranchIdAndIsActive(String username, Long branchId, Integer isActive);
}
