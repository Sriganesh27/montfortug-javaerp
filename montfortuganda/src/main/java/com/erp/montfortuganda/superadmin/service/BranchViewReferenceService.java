package com.erp.montfortuganda.superadmin.service;

import com.erp.montfortuganda.school.repository.BranchRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Creates opaque, URL-safe references for Branch View navigation.
 *
 * The branch ID is encrypted server-side and is never placed directly in
 * the browser URL. No database table is required; the reference is
 * stateless and can therefore be resolved after a full browser refresh.
 */
@Service
public class BranchViewReferenceService {

    private static final String VERSION = "1";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final BranchRepository branchRepository;
    private final SecretKeySpec encryptionKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public BranchViewReferenceService(
            BranchRepository branchRepository,
            @Value("${jwt.secret}") String secret
    ) {
        this.branchRepository = branchRepository;
        this.encryptionKey = new SecretKeySpec(
                sha256(secret),
                "AES"
        );
    }

    public String createReference(Integer branchId) {
        if (branchId == null || branchId <= 0) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "A valid branch is required."
            );
        }

        if (!branchRepository.existsById(branchId)) {
            throw new ResponseStatusException(
                    NOT_FOUND,
                    "Branch was not found."
            );
        }

        byte[] iv = new byte[IV_LENGTH_BYTES];
        secureRandom.nextBytes(iv);

        String payload = VERSION + ":" + branchId;

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    encryptionKey,
                    new GCMParameterSpec(TAG_LENGTH_BITS, iv)
            );

            byte[] encrypted = cipher.doFinal(
                    payload.getBytes(StandardCharsets.UTF_8)
            );

            byte[] token = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, token, 0, iv.length);
            System.arraycopy(
                    encrypted,
                    0,
                    token,
                    iv.length,
                    encrypted.length
            );

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(token);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "Unable to create Branch View reference.",
                    exception
            );
        }
    }

    public Integer resolveReference(String reference) {
        if (reference == null || reference.isBlank()) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Branch View reference is required."
            );
        }

        final byte[] token;
        try {
            token = Base64.getUrlDecoder().decode(reference);
        } catch (IllegalArgumentException exception) {
            throw invalidReference();
        }

        if (token.length <= IV_LENGTH_BYTES) {
            throw invalidReference();
        }

        byte[] iv = new byte[IV_LENGTH_BYTES];
        byte[] encrypted = new byte[token.length - IV_LENGTH_BYTES];
        System.arraycopy(token, 0, iv, 0, IV_LENGTH_BYTES);
        System.arraycopy(
                token,
                IV_LENGTH_BYTES,
                encrypted,
                0,
                encrypted.length
        );

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    encryptionKey,
                    new GCMParameterSpec(TAG_LENGTH_BITS, iv)
            );

            String payload = new String(
                    cipher.doFinal(encrypted),
                    StandardCharsets.UTF_8
            );

            String[] parts = payload.split(":", -1);
            if (parts.length != 2 || !VERSION.equals(parts[0])) {
                throw invalidReference();
            }

            int branchId;
            try {
                branchId = Integer.parseInt(parts[1]);
            } catch (NumberFormatException exception) {
                throw invalidReference();
            }

            if (branchId <= 0 || !branchRepository.existsById(branchId)) {
                throw new ResponseStatusException(
                        NOT_FOUND,
                        "Branch was not found."
                );
            }

            return branchId;
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (GeneralSecurityException | RuntimeException exception) {
            throw invalidReference();
        }
    }

    private ResponseStatusException invalidReference() {
        return new ResponseStatusException(
                NOT_FOUND,
                "Branch View reference was not found."
        );
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "Unable to initialize Branch View reference encryption.",
                    exception
            );
        }
    }
}
