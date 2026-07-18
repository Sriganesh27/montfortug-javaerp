package com.erp.montfortuganda.school.repository;
import com.erp.montfortuganda.school.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Integer> {
    java.util.List<SchoolClass> findByStatusOrderByDisplayOrderAsc(Integer status);
}