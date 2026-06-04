package com.montfort.erp.modules.branch.repository;

import com.montfort.erp.modules.branch.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, Long> {
}

