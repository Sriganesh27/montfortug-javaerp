package com.erp.montfortuganda.scholarship.repository;

import com.erp.montfortuganda.scholarship.ScholarshipAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScholarshipAllocationRepository extends JpaRepository<ScholarshipAllocation, Integer> {
    List<ScholarshipAllocation> findByDonationId(Integer donationId);
}