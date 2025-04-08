package com.poly.service.impl;

import com.poly.config.CustomUserDetails;
import com.poly.controller.request.SignInRequest;
import com.poly.controller.response.TokenResponse;
import com.poly.model.UserEntity;
import com.poly.service.AuthService;
import com.poly.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "AUTH-SERVICE")
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public TokenResponse login(SignInRequest request) {
        log.info("Login request: {}", request);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Thay đổi cách lấy thông tin user
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        UserEntity user = customUserDetails.getUser();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        log.info("User authenticated successfully: {}", user.getUsername());
        log.info("User ID: {}", user.getId());

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), authorities);
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getUsername(), authorities);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void logout(String authorizationHeader) {
        log.info("Token invalidated: {}", authorizationHeader);
    }
}