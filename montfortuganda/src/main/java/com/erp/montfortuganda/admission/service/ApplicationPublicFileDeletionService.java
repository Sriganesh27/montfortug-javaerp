package com.erp.montfortuganda.admission.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

/**
 * Deletes only public-portal admission files stored beneath
 * {@code erp.storage.location}.
 *
 * <p>This service never reads from or deletes files in the private
 * {@code erp-storage} branch/admin area. Browser-supplied paths must never be
 * passed here; callers must use the stored path reloaded from the committed
 * application-document record.</p>
 */
@Service
public class ApplicationPublicFileDeletionService {

    private static final String LEGACY_UPLOADS_PREFIX =
            "uploads/";

    private final Path publicUploadRoot;

    public ApplicationPublicFileDeletionService(
            @Value("${erp.storage.location:uploads}")
            String publicUploadLocation
    ) {
        if (!StringUtils.hasText(publicUploadLocation)) {
            throw new IllegalArgumentException(
                    "Public upload storage location is required."
            );
        }

        this.publicUploadRoot =
                Path.of(publicUploadLocation.trim())
                        .toAbsolutePath()
                        .normalize();
    }

    /**
     * Deletes one stored public application file and then removes empty child
     * directories up to—but never including—the configured upload root.
     *
     * @param storedPath database-stored public upload path
     * @return {@code true} when a file existed and was deleted;
     *         {@code false} when it was already absent
     */
    public boolean deleteApplicationDocument(
            String storedPath
    ) {
        Path targetFile =
                resolveStoredPublicPath(storedPath);

        rejectSymbolicPath(targetFile);

        try {
            if (!Files.exists(
                    targetFile,
                    LinkOption.NOFOLLOW_LINKS
            )) {
                return false;
            }

            if (!Files.isRegularFile(
                    targetFile,
                    LinkOption.NOFOLLOW_LINKS
            )) {
                throw new SecurityException(
                        "The stored application-document path "
                                + "does not reference a regular file."
                );
            }

            boolean deleted =
                    Files.deleteIfExists(targetFile);

            if (deleted) {
                removeEmptyParentDirectories(
                        targetFile.getParent()
                );
            }

            return deleted;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "The stored application document "
                            + "could not be deleted.",
                    exception
            );
        }
    }

    private Path resolveStoredPublicPath(
            String storedPath
    ) {
        if (!StringUtils.hasText(storedPath)) {
            throw new IllegalArgumentException(
                    "Stored application-document path is required."
            );
        }

        String normalizedText =
                storedPath.trim()
                        .replace('\\', '/');

        if (normalizedText.indexOf('\0') >= 0) {
            throw new SecurityException(
                    "Invalid stored application-document path."
            );
        }

        Path suppliedPath =
                Path.of(normalizedText)
                        .normalize();

        /*
         * Current records normally contain paths relative to the configured
         * upload root. An absolute filesystem path is accepted only when it
         * is already inside that same root.
         */
        if (suppliedPath.isAbsolute()) {
            Path absolutePath =
                    suppliedPath.toAbsolutePath()
                            .normalize();

            if (absolutePath.startsWith(publicUploadRoot)
                    && !absolutePath.equals(publicUploadRoot)) {
                return absolutePath;
            }

            /*
             * Compatibility for legacy database values such as:
             * /uploads/applications/.../file.pdf
             *
             * This is treated as a root-relative stored key, not as the
             * operating system's /uploads directory.
             */
            String legacyRelative =
                    stripLeadingSeparators(normalizedText);

            if (legacyRelative.startsWith(
                    LEGACY_UPLOADS_PREFIX
            )) {
                legacyRelative =
                        legacyRelative.substring(
                                LEGACY_UPLOADS_PREFIX.length()
                        );

                return resolveRelativePath(
                        legacyRelative
                );
            }

            throw new SecurityException(
                    "Stored application-document path is "
                            + "outside public upload storage."
            );
        }

        String relativeText =
                stripLeadingSeparators(normalizedText);

        if (relativeText.startsWith(
                LEGACY_UPLOADS_PREFIX
        )) {
            relativeText =
                    relativeText.substring(
                            LEGACY_UPLOADS_PREFIX.length()
                    );
        }

        return resolveRelativePath(relativeText);
    }

    private Path resolveRelativePath(
            String relativeText
    ) {
        if (!StringUtils.hasText(relativeText)) {
            throw new SecurityException(
                    "Invalid stored application-document path."
            );
        }

        Path resolvedPath =
                publicUploadRoot
                        .resolve(relativeText)
                        .normalize()
                        .toAbsolutePath();

        if (!resolvedPath.startsWith(publicUploadRoot)
                || resolvedPath.equals(publicUploadRoot)) {
            throw new SecurityException(
                    "Stored application-document path is "
                            + "outside public upload storage."
            );
        }

        return resolvedPath;
    }

    private void rejectSymbolicPath(
            Path targetFile
    ) {
        Path current =
                publicUploadRoot;

        Path relative =
                publicUploadRoot.relativize(
                        targetFile
                );

        for (Path segment : relative) {
            current = current.resolve(segment);

            if (Files.exists(
                    current,
                    LinkOption.NOFOLLOW_LINKS
            )
                    && Files.isSymbolicLink(current)) {
                throw new SecurityException(
                        "Symbolic links are not allowed in "
                                + "public application storage paths."
                );
            }
        }
    }

    private void removeEmptyParentDirectories(
            Path directory
    ) throws IOException {
        Path current =
                directory;

        while (current != null
                && current.startsWith(publicUploadRoot)
                && !current.equals(publicUploadRoot)) {
            rejectSymbolicPath(current);

            try {
                Files.delete(current);
            } catch (DirectoryNotEmptyException exception) {
                return;
            }

            current = current.getParent();
        }
    }

    private String stripLeadingSeparators(
            String value
    ) {
        int index = 0;

        while (index < value.length()) {
            char character =
                    value.charAt(index);

            if (character != '/'
                    && character != '\\') {
                break;
            }

            index++;
        }

        return value.substring(index);
    }
}
