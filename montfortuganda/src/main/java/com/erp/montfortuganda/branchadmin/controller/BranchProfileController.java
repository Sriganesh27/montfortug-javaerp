package com.erp.montfortuganda.branchadmin.controller;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.branchadmin.dto.BranchProfileResponseDTO;
import com.erp.montfortuganda.branchadmin.dto.BranchProfileUpdateDTO;
import com.erp.montfortuganda.branchadmin.service.BranchProfileService;
import com.erp.montfortuganda.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/branchadmin/profile")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
@RequiredArgsConstructor
public class BranchProfileController {

    private final BranchProfileService branchProfileService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<BranchProfileResponseDTO>> getProfile() {
        CurrentUserContext context = currentUserService.getCurrentUserContext();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Branch profile fetched successfully",
                        branchProfileService.getProfile(context)
                )
        );
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BranchProfileResponseDTO>> updateProfile(
            @Valid @ModelAttribute BranchProfileUpdateDTO updateDTO,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents,
            @RequestPart(value = "qrCode", required = false) MultipartFile qrCode
    ) {
        CurrentUserContext context = currentUserService.getCurrentUserContext();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Branch profile updated successfully",
                        branchProfileService.updateProfile(
                                context,
                                updateDTO,
                                logo,
                                photo,
                                documents,
                                qrCode
                        )
                )
        );
    }
}
