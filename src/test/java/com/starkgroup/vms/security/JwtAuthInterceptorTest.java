package com.starkgroup.vms.security;

import com.starkgroup.vms.TestEnvHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuthInterceptorTest {

    private JwtService jwtService;
    private JwtAuthInterceptor interceptor;
    private String testUsername;
    private String testRole;
    private String testFullName;

    @BeforeEach
    void setUp() {
        String secret = TestEnvHelper.get("VMS_JWT_SECRET");
        long expirationMs = TestEnvHelper.getLong("VMS_JWT_EXPIRATION_MS");
        jwtService = new JwtService(secret, expirationMs);
        interceptor = new JwtAuthInterceptor(jwtService);

        testUsername = TestEnvHelper.get("VMS_ADMIN_USERNAME");
        testRole = TestEnvHelper.get("VMS_ADMIN_ROLE");
        testFullName = TestEnvHelper.get("VMS_ADMIN_FULL_NAME");
    }

    @Test
    @DisplayName("OPTIONS preflight requests are allowed without token")
    void preHandle_OptionsRequest_Allowed() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/vehicles");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
        assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("Valid Bearer token allows request and sets user attribute")
    void preHandle_ValidToken_Allowed() throws Exception {
        String token = jwtService.generateToken(testUsername, testRole, testFullName);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/vehicles");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
        assertEquals(testUsername, request.getAttribute("authenticatedUser"));
    }

    @Test
    @DisplayName("Missing Authorization header returns 401 Unauthorized")
    void preHandle_MissingToken_Returns401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/vehicles");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());
        assertFalse(result);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Full authentication is required"));
    }

    @Test
    @DisplayName("Invalid token returns 401 Unauthorized")
    void preHandle_InvalidToken_Returns401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/vehicles");
        request.addHeader("Authorization", "Bearer bad.token.here");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());
        assertFalse(result);
        assertEquals(401, response.getStatus());
    }
}
