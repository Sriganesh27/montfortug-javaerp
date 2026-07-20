package com.erp.montfortuganda.infrastructure.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({
        "unused",
        "SpellCheckingInspection"
})
@Service
public class PasswordService {

    private static final int TEMPORARY_PASSWORD_LENGTH = 16;

    /*
     * Similar-looking characters are intentionally excluded:
     * lowercase l, uppercase I and O, and digits 0 and 1.
     */
    private static final String LOWERCASE =
            "abcdefghijkmnopqrstuvwxyz";

    private static final String UPPERCASE =
            "ABCDEFGHJKLMNPQRSTUVWXYZ";

    private static final String NUMBERS =
            "23456789";

    private static final String SPECIAL =
            "@#$%&*!";

    private static final String ALL_CHARACTERS =
            LOWERCASE
                    + UPPERCASE
                    + NUMBERS
                    + SPECIAL;

    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;

    public PasswordService(
            PasswordEncoder passwordEncoder
    ) {
        this.passwordEncoder = passwordEncoder;
        this.secureRandom = new SecureRandom();
    }

    public String generateSecureTemporaryPassword() {
        List<Character> passwordCharacters =
                new ArrayList<>(
                        TEMPORARY_PASSWORD_LENGTH
                );

        passwordCharacters.add(
                randomCharacter(LOWERCASE)
        );

        passwordCharacters.add(
                randomCharacter(UPPERCASE)
        );

        passwordCharacters.add(
                randomCharacter(NUMBERS)
        );

        passwordCharacters.add(
                randomCharacter(SPECIAL)
        );

        while (
                passwordCharacters.size()
                        < TEMPORARY_PASSWORD_LENGTH
        ) {
            passwordCharacters.add(
                    randomCharacter(ALL_CHARACTERS)
            );
        }

        Collections.shuffle(
                passwordCharacters,
                secureRandom
        );

        StringBuilder password =
                new StringBuilder(
                        TEMPORARY_PASSWORD_LENGTH
                );

        for (Character character : passwordCharacters) {
            password.append(character);
        }

        return password.toString();
    }

    public String hashPassword(
            String rawPassword
    ) {
        if (
                rawPassword == null
                        || rawPassword.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Password cannot be empty."
            );
        }

        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(
            String rawPassword,
            String encodedPassword
    ) {
        if (
                rawPassword == null
                        || encodedPassword == null
        ) {
            return false;
        }

        return passwordEncoder.matches(
                rawPassword,
                encodedPassword
        );
    }

    private char randomCharacter(
            String characters
    ) {
        return characters.charAt(
                secureRandom.nextInt(
                        characters.length()
                )
        );
    }
}