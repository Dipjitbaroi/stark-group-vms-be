package com.starkgroup.vms.dto;

public class AuthResponse {

    private String token;
    private String emailOrUsername;
    private String fullName;
    private String role;
    private String message;

    public AuthResponse() {
    }

    public AuthResponse(String token, String emailOrUsername, String fullName, String role, String message) {
        this.token = token;
        this.emailOrUsername = emailOrUsername;
        this.fullName = fullName;
        this.role = role;
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmailOrUsername() {
        return emailOrUsername;
    }

    public void setEmailOrUsername(String emailOrUsername) {
        this.emailOrUsername = emailOrUsername;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
