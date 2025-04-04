package com.poly.controller;

import com.poly.controller.request.SignInRequest;
import com.poly.controller.response.TokenResponse;
import com.poly.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j(topic = "AUTH-CONTROLLER")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody SignInRequest request) {
        log.info("Login request: {}", request);
        TokenResponse tokenResponse = authService.login(request);
        return ResponseEntity.ok(tokenResponse);
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authorizationHeader) {
        log.info("Logout request");
        authService.logout(authorizationHeader);
        return ResponseEntity.ok("Logout successful");
    }
}