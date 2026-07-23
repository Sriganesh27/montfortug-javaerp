package com.erp.montfortuganda.employee.bulkimport.service;

import com.erp.montfortuganda.employee.bulkimport.exception.EmployeeBulkTemplateException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;

/**
 * Loads the internal default profile image used only for Employee bulk import.
 *
 * <p>The Excel template does not contain a profile-photo column. The existing
 * Employee registration request requires valid photo data and metadata, so
 * bulk import supplies this approved internal image.</p>
 */
@Service
public class EmployeeBulkDefaultPhotoService {

    private static final String RESOURCE_PATH =
            "static/images/default-employee-profile.png";

    private static final String FILE_NAME =
            "default-employee-profile.png";

    private static final String CONTENT_TYPE =
            "image/png";

    private volatile DefaultPhoto cachedPhoto;

    public DefaultPhoto getDefaultPhoto() {
        DefaultPhoto current = cachedPhoto;

        if (current != null) {
            return current;
        }

        synchronized (this) {
            if (cachedPhoto == null) {
                cachedPhoto = loadPhoto();
            }

            return cachedPhoto;
        }
    }

    private DefaultPhoto loadPhoto() {
        ClassPathResource resource =
                new ClassPathResource(RESOURCE_PATH);

        if (!resource.exists()) {
            throw new EmployeeBulkTemplateException(
                    "Default Employee profile image is missing: "
                            + RESOURCE_PATH
            );
        }

        try {
            byte[] bytes =
                    resource.getInputStream().readAllBytes();

            if (bytes.length == 0) {
                throw new EmployeeBulkTemplateException(
                        "Default Employee profile image is empty."
                );
            }

            String base64Data =
                    Base64.getEncoder().encodeToString(bytes);

            return new DefaultPhoto(
                    base64Data,
                    FILE_NAME,
                    CONTENT_TYPE,
                    (long) bytes.length
            );
        } catch (IOException exception) {
            throw new EmployeeBulkTemplateException(
                    "Could not load the default Employee profile image.",
                    exception
            );
        }
    }

    public record DefaultPhoto(
            String base64Data,
            String fileName,
            String contentType,
            Long fileSize
    ) {
    }
}