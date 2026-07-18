package com.erp.montfortuganda.school.repository;
import com.erp.montfortuganda.school.entity.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LevelRepository extends JpaRepository<Level, Integer> {}