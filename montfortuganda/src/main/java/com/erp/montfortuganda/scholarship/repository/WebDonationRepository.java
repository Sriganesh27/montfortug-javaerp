package com.erp.montfortuganda.scholarship.repository;

import com.erp.montfortuganda.scholarship.entity.WebDonation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebDonationRepository extends JpaRepository<WebDonation, Long> {
}