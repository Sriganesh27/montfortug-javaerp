package com.erp.montfortuganda.admission.controller;

import com.erp.montfortuganda.admission.dto.VerifiedApplicationSession;
import com.erp.montfortuganda.admission.service.PublicApplicationReceiptService;
import com.erp.montfortuganda.admission.service.PublicApplicationReceiptService.PublicReceiptLogoResource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Session-protected public receipt endpoints.
 *
 * <p>The application ID is recovered only from the verified public session
 * created after application number and date-of-birth verification. The
 * browser cannot choose another application or branch by query parameter.</p>
 */
@RestController
@RequestMapping("/api/public/applications/receipt")
public class PublicApplicationReceiptController {

    private static final String VERIFIED_APPLICATION_SESSION_KEY =
            "VERIFIED_APPLICATION";

    private final PublicApplicationReceiptService receiptService;

    public PublicApplicationReceiptController(
            PublicApplicationReceiptService receiptService
    ) {
        this.receiptService = receiptService;
    }

    /**
     * Returns the latest printable application details and current workflow
     * state for the application stored in the verified public session.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getReceipt(
            HttpServletRequest request
    ) {
        VerifiedApplicationSession verifiedSession =
                requireVerifiedApplicationSession(request);

        Map<String, Object> response =
                receiptService.getReceiptDetails(
                        verifiedSession.getApplicationId()
                );

        return ResponseEntity.ok()
                .headers(noStoreHeaders())
                .body(response);
    }

    /**
     * Streams the selected application's school logo from private Branch/Admin
     * storage without exposing the stored path.
     */
    @GetMapping("/logo")
    public ResponseEntity<Resource> getReceiptSchoolLogo(
            HttpServletRequest request
    ) {
        VerifiedApplicationSession verifiedSession =
                requireVerifiedApplicationSession(request);

        PublicReceiptLogoResource logo =
                receiptService.loadSchoolLogo(
                        verifiedSession.getApplicationId()
                );

        ContentDisposition disposition =
                ContentDisposition.inline()
                        .filename(
                                logo.fileName(),
                                StandardCharsets.UTF_8
                        )
                        .build();

        return ResponseEntity.ok()
                .headers(noStoreHeaders())
                .contentType(
                        MediaType.parseMediaType(
                                logo.contentType()
                        )
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .header(
                        "X-Content-Type-Options",
                        "nosniff"
                )
                .cacheControl(
                        CacheControl.noStore()
                                .mustRevalidate()
                )
                .body(logo.resource());
    }

    private VerifiedApplicationSession
    requireVerifiedApplicationSession(
            HttpServletRequest request
    ) {
        HttpSession session =
                request.getSession(false);

        if (session == null) {
            throw new ReceiptSessionExpiredException();
        }

        Object sessionValue =
                session.getAttribute(
                        VERIFIED_APPLICATION_SESSION_KEY
                );

        if (!(sessionValue
                instanceof VerifiedApplicationSession verifiedSession)
                || !verifiedSession.isValid()
                || verifiedSession.getApplicationId() == null
                || verifiedSession.getApplicationId() <= 0) {

            session.invalidate();

            throw new ReceiptSessionExpiredException();
        }

        return verifiedSession;
    }

    private HttpHeaders noStoreHeaders() {
        HttpHeaders headers =
                new HttpHeaders();

        headers.set(
                HttpHeaders.CACHE_CONTROL,
                "private, no-store, no-cache, must-revalidate, max-age=0"
        );
        headers.setPragma("no-cache");
        headers.setExpires(0);

        return headers;
    }

    /**
     * Kept local to this public controller so session expiry returns the same
     * clear 403 response without exposing application existence.
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(
            ReceiptSessionExpiredException.class
    )
    public ResponseEntity<Map<String, Object>>
    handleReceiptSessionExpired() {
        return ResponseEntity.status(403)
                .headers(noStoreHeaders())
                .body(
                        Map.of(
                                "success",
                                false,
                                "message",
                                "Session expired. Please track the application again."
                        )
                );
    }

    private static final class ReceiptSessionExpiredException
            extends RuntimeException {
    }
}
