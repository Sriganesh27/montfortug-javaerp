package com.erp.montfortuganda.school;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Integer> {
    // Spring Boot automatically handles finding, saving, and deleting schools!
}