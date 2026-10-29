
package com.klu.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JWTServiceTest {

    private JWTService jwtService;

    // Must match the key used by JWTService for these tests.
    private static final String SECRET =
            "mySecretKeyForSmartEnergyProject2026SecureJwtToken123456";

    private SecretKey testKey;

    @BeforeEach
    void setUp() {
        jwtService = new JWTService();

        testKey = Keys.hmacShaKeyFor(
                SECRET.getBytes(StandardCharsets.UTF_8)
        );
    }

    // Test 1: Generate a valid JWT
    @Test
    void shouldGenerateValidToken() {
        String token = jwtService.generateToken(
                "Aravind", "USER"
        );

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(jwtService.isTokenValid(token));
    }

    // Test 2: Extract username and role
    @Test
    void shouldExtractUsernameAndRole() {
        String token = jwtService.generateToken(
                "Aravind", "USER"
        );

        assertEquals(
                "Aravind",
                jwtService.extractUsername(token)
        );

        assertEquals(
                "USER",
                jwtService.extractRole(token)
        );
    }

    // Test 3: Reject a malformed token
    @Test
    void shouldRejectMalformedToken() {
        assertFalse(
                jwtService.isTokenValid("abc.def.ghi")
        );
    }

    // Test 4: Reject a token with a tampered signature
    @Test
    void shouldRejectTamperedToken() {
        String token = jwtService.generateToken(
                "Aravind", "USER"
        );

        String[] parts = token.split("\\.");

        String signature = parts[2];

        String modifiedSignature =
                (signature.charAt(0) == 'A' ? "B" : "A")
                + signature.substring(1);

        String tamperedToken =
                parts[0] + "." + parts[1] + "." + modifiedSignature;

        assertFalse(
                jwtService.isTokenValid(tamperedToken)
        );
    }

    // Test 5: Reject an expired, correctly signed token
    @Test
    void shouldRejectExpiredToken() {
        long now = System.currentTimeMillis();

        String expiredToken = Jwts.builder()
                .setSubject("Aravind")
                .claim("role", "USER")
                .setIssuedAt(new Date(now - 10000))
                .setExpiration(new Date(now - 1000))
                .signWith(testKey, SignatureAlgorithm.HS256)
                .compact();

        assertFalse(
                jwtService.isTokenValid(expiredToken)
        );
    }

    // Test 6: Reject a token signed with another key
    @Test
    void shouldRejectTokenSignedWithWrongKey() {
        SecretKey wrongKey = Keys.hmacShaKeyFor(
                "DifferentTestSigningKeyForSmartGrid2026SecureToken"
                        .getBytes(StandardCharsets.UTF_8)
        );

        String wrongKeyToken = Jwts.builder()
                .setSubject("Aravind")
                .claim("role", "USER")
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + 60000)
                )
                .signWith(wrongKey, SignatureAlgorithm.HS256)
                .compact();

        assertFalse(
                jwtService.isTokenValid(wrongKeyToken)
        );
    }
}
