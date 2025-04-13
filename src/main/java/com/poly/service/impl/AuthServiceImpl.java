package com.poly.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Value;
import com.poly.common.Status;
import com.poly.common.UserType;
import com.poly.config.CustomUserDetails;
import com.poly.controller.request.SignInRequest;
import com.poly.controller.response.TokenResponse;
import com.poly.exception.InvalidDataException;
import com.poly.model.UserEntity;
import com.poly.repository.UserRepository;
import com.poly.service.AuthService;
import com.poly.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationServiceException;
import java.util.Collection;
import java.util.Collections;

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
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Autowired
    private UserRepository userRepository;

    @Override
    public TokenResponse loginWithGoogle(String googleToken) {
        try {
            // Verify Google token
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(googleToken);
            if (idToken == null) {
                throw new InvalidDataException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();

            // Find or create user
            UserEntity user = userRepository.findByEmail(email);
            if (user == null) {
                // Create new user
                user = new UserEntity();
                user.setEmail(email);
                user.setUsername(email); // Use email as username
                user.setStatus(Status.ACTIVE);
                user.setType(UserType.USER);
                // Set other necessary fields
                userRepository.save(user);
            }

            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getAuthorities());
            String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getUsername(), user.getAuthorities());

            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (Exception e) {
            log.error("Google authentication error: {}", e.getMessage());
            throw new AuthenticationServiceException("Google authentication failed");
        }
    }

    @Override
    public void logout(String authorizationHeader) {
        log.info("Token invalidated: {}", authorizationHeader);
    }
}