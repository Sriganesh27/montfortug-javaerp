package com.erp.montfortuganda.settings.service;

import com.erp.montfortuganda.settings.dto.SiteSettingDTO;
import java.util.List;

public interface SiteSettingService {
    List<SiteSettingDTO> getAllSettings();
    SiteSettingDTO saveSetting(SiteSettingDTO settingDTO);
}