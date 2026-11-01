package com.example.demo.config;

import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // Public signing keys for all Microsoft Entra ID (Azure AD) tenants
    private static final String MICROSOFT_JWK_SET_URI =
            "https://login.microsoftonline.com/common/discovery/v2.0/keys";

    // Multi-tenant app: issuer is https://login.microsoftonline.com/{tenantId}/v2.0
    private static final Pattern MICROSOFT_ISSUER =
            Pattern.compile("^https://login\\.microsoftonline\\.com/[0-9a-fA-F-]{36}/v2\\.0$");

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Paths are relative to the context path (/api)
                        .requestMatchers("/v1/auth/**").authenticated()
                        // Everything else stays public for now (course catalog, etc.)
                        .anyRequest().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    /**
     * Verifies the Microsoft ID token sent by the frontend:
     * signature (JWKS), exp/nbf, aud = our client ID, iss = a Microsoft tenant.
     */
    @Bean
    public JwtDecoder jwtDecoder(@Value("${azure.client-id}") String clientId) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(MICROSOFT_JWK_SET_URI).build();

        OAuth2TokenValidator<Jwt> audience = jwt ->
                jwt.getAudience() != null && jwt.getAudience().contains(clientId)
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(
                                new OAuth2Error("invalid_token", "Token was not issued for this app", null));

        OAuth2TokenValidator<Jwt> issuer = jwt -> {
            String iss = jwt.getClaimAsString(JwtClaimNames.ISS);
            return iss != null && MICROSOFT_ISSUER.matcher(iss).matches()
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(
                            new OAuth2Error("invalid_token", "Token was not issued by Microsoft", null));
        };

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(), audience, issuer));
        return decoder;
    }
}
