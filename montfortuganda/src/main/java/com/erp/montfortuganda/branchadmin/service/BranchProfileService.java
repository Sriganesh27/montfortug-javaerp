package com.erp.montfortuganda.branchadmin.service;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.branchadmin.dto.BranchProfileResponseDTO;
import com.erp.montfortuganda.branchadmin.dto.BranchProfileUpdateDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BranchProfileService {

    BranchProfileResponseDTO getProfile(CurrentUserContext context);

    BranchProfileResponseDTO updateProfile(
            CurrentUserContext context,
            BranchProfileUpdateDTO updateDTO,
            MultipartFile logo,
            MultipartFile photo,
            List<MultipartFile> documents,
            MultipartFile qrCode
    );
}
