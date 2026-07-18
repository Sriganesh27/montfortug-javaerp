package com.erp.montfortuganda.school.repository;

import com.erp.montfortuganda.school.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Integer> {

}
