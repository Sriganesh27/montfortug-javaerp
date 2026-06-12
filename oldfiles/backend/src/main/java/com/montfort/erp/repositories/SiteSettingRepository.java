package com.montfort.erp.repositories;

import com.montfort.erp.entities.SiteSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SiteSettingRepository extends JpaRepository<SiteSetting, Integer> {
    Optional<SiteSetting> findBySettingKey(String settingKey);
}
