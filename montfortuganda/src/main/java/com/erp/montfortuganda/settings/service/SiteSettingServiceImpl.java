package com.erp.montfortuganda.settings.service;

import com.erp.montfortuganda.settings.SiteSetting;
import com.erp.montfortuganda.settings.SiteSettingRepository;
import com.erp.montfortuganda.settings.dto.SiteSettingDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SiteSettingServiceImpl implements SiteSettingService {

    @Autowired
    private SiteSettingRepository siteSettingRepository;

    @Override
    public List<SiteSettingDTO> getAllSettings() {
        return siteSettingRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public SiteSettingDTO saveSetting(SiteSettingDTO dto) {
        SiteSetting setting = siteSettingRepository.findBySettingKey(dto.getSettingKey()).orElse(new SiteSetting());
        setting.setSettingKey(dto.getSettingKey());
        setting.setSettingValue(dto.getSettingValue());
        return mapToDTO(siteSettingRepository.save(setting));
    }

    private SiteSettingDTO mapToDTO(SiteSetting setting) {
        SiteSettingDTO dto = new SiteSettingDTO();
        dto.setSettingId(setting.getSettingId());
        dto.setSettingKey(setting.getSettingKey());
        dto.setSettingValue(setting.getSettingValue());
        return dto;
    }
}