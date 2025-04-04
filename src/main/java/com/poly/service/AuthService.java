package com.poly.service;

import com.poly.controller.request.SignInRequest;
import com.poly.controller.response.TokenResponse;

public interface AuthService {
    TokenResponse login(SignInRequest request);
    void logout(String authorizationHeader);
}
