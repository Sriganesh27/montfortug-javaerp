package com.erp.montfortuganda.admission.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    // Stores IP addresses and the exact time of their last submission
    private final ConcurrentHashMap<String, LocalDateTime> requestCache = new ConcurrentHashMap<>();

    // 10 minute cooldown between applications from the same WiFi/IP
    private static final int COOLDOWN_MINUTES = 10;

    public void checkRateLimit(String ipAddress) throws Exception {
        LocalDateTime lastRequest = requestCache.get(ipAddress);

        if (lastRequest != null && lastRequest.plusMinutes(COOLDOWN_MINUTES).isAfter(LocalDateTime.now())) {
            throw new Exception("Rate Limit Exceeded: Please wait " + COOLDOWN_MINUTES + " minutes before submitting another application.");
        }

        // Log the successful attempt
        requestCache.put(ipAddress, LocalDateTime.now());
    }
}