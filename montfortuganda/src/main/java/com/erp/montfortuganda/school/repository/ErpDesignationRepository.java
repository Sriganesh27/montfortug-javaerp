package com.erp.montfortuganda.school.repository;

import com.erp.montfortuganda.school.ErpDesignation;
import com.erp.montfortuganda.school.dto.DesignationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ErpDesignationRepository extends JpaRepository<ErpDesignation, Long> {

    Optional<ErpDesignation> findByDesignationIdAndActiveTrue(Long designationId);

    @Query("SELECT new com.erp.montfortuganda.school.dto.DesignationDTO(" +
            "d.designationId, d.designationCode, d.designationName, " +
            "0L, '', d.description, " +
            "d.active, d.displayOrder, d.createdAt, d.updatedAt, " +
            "(SELECT COUNT(emp) FROM ErpEmployee emp WHERE emp.designation.designationId = d.designationId AND emp.active = true)) " +
            "FROM ErpDesignation d " +
            "WHERE (:active IS NULL OR d.active = :active) " +
            "AND (:keyword IS NULL OR " +
            "     LOWER(d.designationName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "     LOWER(d.designationCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:createdAfter IS NULL OR d.createdAt >= :createdAfter) " +
            "AND (:createdBefore IS NULL OR d.createdAt <= :createdBefore)")
    Page<DesignationDTO> searchDesignations(
            @Param("keyword") String keyword,
            @Param("active") Boolean active,
            @Param("createdAfter") LocalDateTime createdAfter,
            @Param("createdBefore") LocalDateTime createdBefore,
            Pageable pageable);

    boolean existsByDesignationNameIgnoreCaseAndActiveTrue(String designationName);
}
