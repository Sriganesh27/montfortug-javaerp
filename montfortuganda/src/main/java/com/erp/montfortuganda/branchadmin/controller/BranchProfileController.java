package com.erp.montfortuganda.branchadmin.controller;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.branchadmin.dto.BranchProfileResponseDTO;
import com.erp.montfortuganda.branchadmin.dto.BranchProfileUpdateDTO;
import com.erp.montfortuganda.branchadmin.service.BranchProfileService;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.school.service.FileStorageService;
import com.erp.montfortuganda.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/branchadmin/profile")
@PreAuthorize("hasRole('BRANCH_ADMIN')")
@RequiredArgsConstructor
public class BranchProfileController {

    private final BranchProfileService branchProfileService;
    private final CurrentUserService currentUserService;
    private final BranchAccessService branchAccessService;
    private final BranchRepository branchRepository;
    private final FileStorageService fileStorageService;

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

    /**
     * Serves private media for the authenticated Branch Admin's own branch.
     * The branch ID is derived from the authenticated server-side context;
     * it is never accepted from the browser.
     */
    @GetMapping("/files/logo")
    public ResponseEntity<Resource> viewBranchLogo() {
        Branch branch = getCurrentBranch();
        return servePrivateFile(branch.getBranchLogoUrl());
    }

    @GetMapping("/files/photo")
    public ResponseEntity<Resource> viewSchoolPhoto() {
        Branch branch = getCurrentBranch();
        return servePrivateFile(branch.getSchoolPhotoUrl());
    }

    @GetMapping("/files/qr-code")
    public ResponseEntity<Resource> viewQrCode() {
        Branch branch = getCurrentBranch();
        return servePrivateFile(branch.getQrCodeUrl());
    }

    @GetMapping("/files/documents/{documentIndex}")
    public ResponseEntity<Resource> viewGovernmentDocument(
            @PathVariable int documentIndex
    ) {
        Branch branch = getCurrentBranch();
        List<String> paths = extractDocumentPaths(branch.getGovDocumentUrl());

        if (documentIndex < 0 || documentIndex >= paths.size()) {
            throw new ResponseStatusException(NOT_FOUND, "Branch document was not found.");
        }

        return servePrivateFile(paths.get(documentIndex));
    }

    private Branch getCurrentBranch() {
        CurrentUserContext context = currentUserService.getCurrentUserContext();
        Integer branchId = branchAccessService.getValidatedBranchId(context);
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Branch was not found."));
    }

    private ResponseEntity<Resource> servePrivateFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new ResponseStatusException(NOT_FOUND, "Branch file was not found.");
        }

        Resource resource = fileStorageService.loadPrivateFile(relativePath);
        String contentType = fileStorageService.detectContentType(relativePath);
        String filename = Path.of(relativePath).getFileName().toString().replace("\"", "\\\"");
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException exception) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    private List<String> extractDocumentPaths(String storedDocumentPaths) {
        if (storedDocumentPaths == null || storedDocumentPaths.isBlank()) {
            return List.of();
        }
        return Arrays.stream(storedDocumentPaths.split(","))
                .map(String::trim)
                .filter(path -> !path.isBlank())
                .toList();
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
