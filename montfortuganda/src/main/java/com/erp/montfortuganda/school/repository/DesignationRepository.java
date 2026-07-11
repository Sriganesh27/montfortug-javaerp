package com.erp.montfortuganda.school.repository;

import com.erp.montfortuganda.school.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long>, JpaSpecificationExecutor<Designation> {
    Optional<Designation> findByDesignationCode(String code);
    Optional<Designation> findByDesignationName(String name);
}