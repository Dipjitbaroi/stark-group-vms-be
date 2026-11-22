package com.starkgroup.vms.security;

import com.starkgroup.vms.TestEnvHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;
    private String testUsername;
    private String testRole;
    private String testFullName;

    @BeforeEach
    void setUp() {
        String secret = TestEnvHelper.get("VMS_JWT_SECRET");
        long expirationMs = TestEnvHelper.getLong("VMS_JWT_EXPIRATION_MS");
        jwtService = new JwtService(secret, expirationMs);

        testUsername = TestEnvHelper.get("VMS_ADMIN_USERNAME");
        testRole = TestEnvHelper.get("VMS_ADMIN_ROLE");
        testFullName = TestEnvHelper.get("VMS_ADMIN_FULL_NAME");
    }

    @Test
    @DisplayName("Generate token creates valid non-null JWT")
    void generateToken_Success() {
        String token = jwtService.generateToken(testUsername, testRole, testFullName);
        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
        assertEquals(testUsername, jwtService.extractUsername(token));
    }

    @Test
    @DisplayName("Validate token returns false for invalid token")
    void validateToken_Invalid() {
        assertFalse(jwtService.validateToken("invalid.token.string"));
        assertFalse(jwtService.validateToken(null));
        assertFalse(jwtService.validateToken(""));
    }

    @Test
    @DisplayName("Validate token returns false for expired token")
    void validateToken_Expired() {
        String secret = TestEnvHelper.get("VMS_JWT_SECRET");
        JwtService shortLivedJwt = new JwtService(secret, -1000);
        String expiredToken = shortLivedJwt.generateToken(testUsername, testRole, testFullName);
        assertFalse(shortLivedJwt.validateToken(expiredToken));
    }
}
