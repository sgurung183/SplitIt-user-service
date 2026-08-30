package com.SplitIt.user_service.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-key-must-be-longer-than-256-bits-for-hs256";

    @Test
    void generateToken_thenParse_roundTripsUserIdAndEmail() {
        JwtService jwtService = new JwtService(SECRET, 3600000);

        String token = jwtService.generateToken(42L, "jane@example.com");

        assertThat(jwtService.extractEmail(token)).isEqualTo("jane@example.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    void parseClaims_whenTokenExpired_throwsExpiredJwtException() {
        JwtService jwtService = new JwtService(SECRET, -1000);

        String token = jwtService.generateToken(1L, "jane@example.com");

        assertThatThrownBy(() -> jwtService.parseClaims(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void parseClaims_whenSignedWithDifferentSecret_throwsSignatureException() {
        JwtService issuer = new JwtService(SECRET, 3600000);
        JwtService verifier = new JwtService("a-completely-different-secret-key-also-long-enough-for-hs256", 3600000);

        String token = issuer.generateToken(1L, "jane@example.com");

        assertThatThrownBy(() -> verifier.parseClaims(token))
                .isInstanceOf(SignatureException.class);
    }
}
