package com.deeptechhub.identityservice.service;

import com.deeptechhub.identityservice.dto.AuthRequest;
import com.deeptechhub.identityservice.dto.AuthResponse;
import com.deeptechhub.identityservice.dto.RegisterRequest;
import com.deeptechhub.identityservice.dto.ForgotPasswordRequest;
import com.deeptechhub.identityservice.dto.ResetPasswordRequest;
import com.deeptechhub.identityservice.domain.User;

public interface AuthService {
    User registerUser(RegisterRequest request);
    AuthResponse login(AuthRequest authRequest);
    void initiatePasswordReset(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    User verifyEmail(String token);
}
