package com.erp.montfortuganda.infrastructure.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL = "@#$%&*!";
    private static final String ALL_CHARS = LOWERCASE + UPPERCASE + NUMBERS + SPECIAL;

    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String generateSecureTemporaryPassword() {
        int length = 12;
        List<Character> password = new ArrayList<>(length);

        password.add(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.add(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.add(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        password.add(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        for (int i = 4; i < length; i++) {
            password.add(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        Collections.shuffle(password, random);

        StringBuilder sb = new StringBuilder();
        for (char c : password) {
            sb.append(c);
        }
        return sb.toString();
    }

    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}