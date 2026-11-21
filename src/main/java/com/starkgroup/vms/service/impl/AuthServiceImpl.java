package com.starkgroup.vms.service.impl;

import com.starkgroup.vms.dto.AuthResponse;
import com.starkgroup.vms.dto.LoginRequest;
import com.starkgroup.vms.exception.InvalidCredentialsException;
import com.starkgroup.vms.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final String configuredUsername;
    private final String configuredPassword;
    private final String configuredFullName;
    private final String configuredRole;

    public AuthServiceImpl(
            @Value("${vms.auth.admin.username}") String configuredUsername,
            @Value("${vms.auth.admin.password}") String configuredPassword,
            @Value("${vms.auth.admin.fullName:Fleet Manager}") String configuredFullName,
            @Value("${vms.auth.admin.role:FLEET_ADMIN}") String configuredRole) {
        this.configuredUsername = configuredUsername;
        this.configuredPassword = configuredPassword;
        this.configuredFullName = configuredFullName;
        this.configuredRole = configuredRole;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String inputUser = request.getEmailOrUsername() != null ? request.getEmailOrUsername().trim() : "";
        String inputPass = request.getPassword() != null ? request.getPassword() : "";

        // Support matching strictly against configured credentials (case-insensitive for username/email)
        boolean matchesUser = inputUser.equalsIgnoreCase(configuredUsername);

        boolean matchesPass = inputPass.equals(configuredPassword);

        if (!matchesUser || !matchesPass) {
            throw new InvalidCredentialsException("Invalid username/email or password.");
        }

        String token = "stark-vms-token-" + UUID.randomUUID();
        return new AuthResponse(
                token,
                inputUser,
                configuredFullName,
                configuredRole,
                "Authentication successful. Welcome to Stark Group VMS."
        );
    }
}
