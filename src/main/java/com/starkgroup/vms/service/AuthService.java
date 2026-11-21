package com.starkgroup.vms.service;

import com.starkgroup.vms.dto.AuthResponse;
import com.starkgroup.vms.dto.LoginRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);
}
