package com.erp.montfortuganda.superadmin;

import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.school.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/superadmin/branches")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class BranchPrivateFileController {

    private final BranchRepository branchRepository;
    private final FileStorageService fileStorageService;

    public BranchPrivateFileController(
            BranchRepository branchRepository,
            FileStorageService fileStorageService
    ) {
        this.branchRepository = branchRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/{branchId}/files/logo")
    public ResponseEntity<Resource> viewBranchLogo(
            @PathVariable Integer branchId
    ) {
        Branch branch = findBranch(branchId);

        return servePrivateFile(
                branch.getBranchLogoUrl()
        );
    }

    @GetMapping("/{branchId}/files/photo")
    public ResponseEntity<Resource> viewSchoolPhoto(
            @PathVariable Integer branchId
    ) {
        Branch branch = findBranch(branchId);

        return servePrivateFile(
                branch.getSchoolPhotoUrl()
        );
    }

    @GetMapping("/{branchId}/files/documents/{documentIndex}")
    public ResponseEntity<Resource> viewGovernmentDocument(
            @PathVariable Integer branchId,
            @PathVariable int documentIndex
    ) {
        Branch branch = findBranch(branchId);

        List<String> documentPaths =
                extractDocumentPaths(
                        branch.getGovDocumentUrl()
                );

        if (
                documentIndex < 0
                        || documentIndex >= documentPaths.size()
        ) {
            throw new ResponseStatusException(
                    NOT_FOUND,
                    "Branch document was not found."
            );
        }

        return servePrivateFile(
                documentPaths.get(documentIndex)
        );
    }

    private Branch findBranch(
            Integer branchId
    ) {
        if (branchId == null) {
            throw new ResponseStatusException(
                    NOT_FOUND,
                    "Branch was not found."
            );
        }

        return branchRepository
                .findById(branchId)
                .orElseThrow(
                        () -> new ResponseStatusException(
                                NOT_FOUND,
                                "Branch was not found."
                        )
                );
    }

    private ResponseEntity<Resource> servePrivateFile(
            String relativePath
    ) {
        if (
                relativePath == null
                        || relativePath.isBlank()
        ) {
            throw new ResponseStatusException(
                    NOT_FOUND,
                    "Branch file was not found."
            );
        }

        Resource resource =
                fileStorageService.loadPrivateFile(
                        relativePath
                );

        String contentType =
                fileStorageService.detectContentType(
                        relativePath
                );

        String filename =
                Path.of(relativePath)
                        .getFileName()
                        .toString();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(toMediaType(contentType))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\""
                                + filename
                                + "\""
                )
                .body(resource);
    }

    private List<String> extractDocumentPaths(
            String storedDocumentPaths
    ) {
        if (
                storedDocumentPaths == null
                        || storedDocumentPaths.isBlank()
        ) {
            return List.of();
        }

        return Arrays.stream(
                        storedDocumentPaths.split(",")
                )
                .map(String::trim)
                .filter(path -> !path.isBlank())
                .toList();
    }

    private MediaType toMediaType(
            String contentType
    ) {
        try {
            return MediaType.parseMediaType(
                    contentType
            );
        } catch (IllegalArgumentException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
