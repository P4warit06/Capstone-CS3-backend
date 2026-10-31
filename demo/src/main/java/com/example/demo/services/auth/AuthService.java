package com.example.demo.services.auth;

import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.example.demo.entities.User;
import com.example.demo.entities.UserRole;
import com.example.demo.repositories.UserRepository;

@Service
public class AuthService {
    private static final String ALLOWED_DOMAIN = "@ad.sit.kmutt.ac.th";

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Returns the existing user, or creates one (role student) on first login. */
    @Transactional
    public User sync(Jwt token) {
        String email = extractEmail(token);

        return userRepository.findByEmailIgnoreCase(email)
                .map(user -> {
                    // Backfill for users created before studentId was extracted
                    if (user.getStudentId() == null) {
                        user.setStudentId(extractStudentId(email));
                    }
                    return user;
                })
                .orElseGet(() -> createUser(token, email));
    }

    /** Returns the user without creating one. 404 if /auth/sync was never called. */
    @Transactional(readOnly = true)
    public User getProfile(Jwt token) {
        String email = extractEmail(token);
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private User createUser(Jwt token, String email) {
        String fullName = firstNonBlank(token.getClaimAsString("name"), email.substring(0, email.indexOf('@')));
        String[] parts = fullName.trim().split("\\s+", 2);

        User user = new User();
        user.setEmail(email);
        user.setStudentId(extractStudentId(email));
        user.setDisplayName(fullName.trim());
        user.setFirstName(firstNonBlank(token.getClaimAsString("given_name"), parts[0]));
        user.setLastName(firstNonBlank(token.getClaimAsString("family_name"), parts.length > 1 ? parts[1] : ""));
        user.setRole(UserRole.student);

        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            // Two sync calls at the same time: the other one created the user first
            return userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> e);
        }
    }

    /** SIT accounts only, otherwise 403 (SCRUM-134 condition 1.1). */
    private String extractEmail(Jwt token) {
        // v2 ID tokens put the sign-in name in preferred_username; "email" is often missing
        String email = firstNonBlank(
                token.getClaimAsString("preferred_username"),
                token.getClaimAsString("email"),
                token.getClaimAsString("upn"));

        if (email == null || !email.toLowerCase(Locale.ROOT).endsWith(ALLOWED_DOMAIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only " + ALLOWED_DOMAIN + " accounts are allowed");
        }
        return email.toLowerCase(Locale.ROOT);
    }

    /** Student emails are "{studentId}@ad.sit.kmutt.ac.th"; staff emails are not numeric → null. */
    private static String extractStudentId(String email) {
        String local = email.substring(0, email.indexOf('@'));
        return local.matches("\\d{11}") ? local : null;
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}
