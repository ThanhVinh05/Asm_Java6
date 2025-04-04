package com.poly.service;

import com.poly.controller.request.SignInRequest;
import com.poly.controller.response.TokenResponse;

public interface AuthenticationService {

    TokenResponse getAccessToken(SignInRequest request);

    TokenResponse getRefreshToken(String request);
}
