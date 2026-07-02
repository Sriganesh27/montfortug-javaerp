package com.erp.montfortuganda.admission.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
public class VerifiedApplicationSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String applicationNo;
    private Long applicationId;
    private LocalDateTime verificationTimestamp;
    private LocalDateTime expirationTimestamp;
    private boolean isVerified;

    public VerifiedApplicationSession() {}

    public VerifiedApplicationSession(String applicationNo, Long applicationId, int expirationMinutes) {
        this.applicationNo = applicationNo;
        this.applicationId = applicationId;
        this.verificationTimestamp = LocalDateTime.now();
        this.expirationTimestamp = this.verificationTimestamp.plusMinutes(expirationMinutes);
        this.isVerified = true;
    }

    public boolean isValid() {
        return isVerified && LocalDateTime.now().isBefore(expirationTimestamp);
    }
}