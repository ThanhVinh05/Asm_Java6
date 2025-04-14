package com.poly.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
// Bỏ import sai: import com.google.api.client.util.Value;
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
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory
import org.springframework.beans.factory.annotation.Value; // <<<----- IMPORT ĐÚNG CHO @Value
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority; // Import nếu cần gán quyền mặc định
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
// Bỏ import @Autowired không cần thiết nữa: import org.springframework.beans.factory.annotation.Autowired;


@Service
@RequiredArgsConstructor // Sử dụng Lombok để tạo constructor cho các field final
@Slf4j(topic = "AUTH-SERVICE")
public class AuthServiceImpl implements AuthService {

    // Khai báo final để Lombok inject qua constructor
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    // Sử dụng @Value của Spring để inject giá trị từ application.yml
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    // Logger thủ công để đảm bảo hoạt động
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Override
    public TokenResponse login(SignInRequest request) {
        logger.info("Login request: {}", request);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        UserEntity user = customUserDetails.getUser();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        logger.info("User authenticated successfully: {}", user.getUsername());
        logger.info("User ID: {}", user.getId());

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), authorities);
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getUsername(), authorities);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public TokenResponse loginWithGoogle(String googleToken) {
        // Log token nhận được (cắt ngắn)
        logger.info("Received Google Token in backend: {}", googleToken != null && googleToken.length() > 20 ? googleToken.substring(0, 20) + "..." : googleToken);
        logger.info("Backend configured Google Client ID for verification: {}", googleClientId);

        // Kiểm tra googleClientId đã được inject chưa
        if (googleClientId == null || googleClientId.isEmpty() || "null".equalsIgnoreCase(googleClientId)) {
            logger.error("Google Client ID is not configured or not injected properly in the backend! Value is: '{}'", googleClientId);
            throw new AuthenticationServiceException("Google Client ID is missing or invalid in backend configuration.");
        }

        try {
            // Verify Google token
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(googleToken);
            if (idToken == null) {
                logger.error("Google token verification failed (idToken is null). Token might be invalid, expired, or audience mismatch.");
                throw new InvalidDataException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified()); // Cách kiểm tra Boolean an toàn hơn

            if (!emailVerified) {
                logger.warn("Google email '{}' is not verified by Google.", email);
                // Cân nhắc: có thể không cho phép đăng nhập nếu email chưa được Google xác thực
                // throw new AuthenticationServiceException("Google email is not verified.");
            }

            logger.info("Google Token Payload - Email: {}, Name: {}, Picture: {}",
                    payload.getEmail(), payload.get("name"), payload.get("picture"));

            // Find or create user
            UserEntity user = userRepository.findByEmail(email);
            if (user == null) {
                logger.info("User with email {} not found. Creating new user.", email);
                user = new UserEntity();
                user.setEmail(email);
                String name = (String) payload.get("name");
                user.setUsername(name != null && !name.isEmpty() ? name : email); // Dùng tên Google nếu có
                user.setStatus(Status.ACTIVE); // User từ Google mặc định ACTIVE
                user.setType(UserType.USER); // User từ Google mặc định là USER
                // Không cần set password

                user = userRepository.save(user); // Lưu để có ID
                logger.info("New user created with ID: {}", user.getId());
                // QUAN TRỌNG: Đảm bảo user mới tạo có quyền (authorities). Logic này có thể nằm trong UserEntity hoặc bạn cần gán ở đây.
                // Ví dụ: user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))); userRepository.save(user);

            } else {
                logger.info("User found with email {}. User ID: {}", email, user.getId());
                // Cập nhật tên nếu khác (tùy chọn)
                String googleName = (String) payload.get("name");
                if (googleName != null && !googleName.isEmpty() && !googleName.equals(user.getUsername())) {
                    // Cẩn thận nếu username dùng để login thường
                    // user.setUsername(googleName);
                    // userRepository.save(user);
                    // logger.info("User username updated from Google for user ID: {}", user.getId());
                }
            }

            // Generate tokens
            Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
            if (authorities == null || authorities.isEmpty()) {
                logger.warn("User ID {} retrieved from DB has no authorities! Generating token might fail or lack permissions.", user.getId());
                // Xử lý gán quyền mặc định nếu cần thiết, ví dụ:
                // authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getType().name()));
            }

            String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), authorities);
            String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getUsername(), authorities);

            logger.info("Generated tokens for user ID: {}", user.getId());

            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (InvalidDataException e) {
            logger.error("Invalid Google token provided: {}", e.getMessage());
            throw new AuthenticationServiceException("Invalid Google token", e);
        } catch (Exception e) {
            logger.error("Google authentication process failed: {}", e.getMessage(), e); // Log cả stack trace
            throw new AuthenticationServiceException("Google authentication failed", e);
        }
    }

    @Override
    public void logout(String authorizationHeader) {
        // Có thể triển khai cơ chế blacklist token ở đây nếu cần
        logger.info("Logout requested. Token (potentially invalidated): {}", authorizationHeader);
    }
}