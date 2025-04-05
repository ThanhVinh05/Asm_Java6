package com.poly.controller;

import com.poly.controller.request.UserCreationRequest;
import com.poly.controller.request.UserPasswordRequest;
import com.poly.controller.request.UserUpdateRequest;
import com.poly.controller.response.UserResponse;
import com.poly.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Tag(name = "User Controller")
@Slf4j(topic = "USER-CONTROLLER")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get user list", description = "API retrieve user from database")
    @GetMapping("/list")
    public Map<String, Object> getList(@RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String sort,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        log.info("Get user list");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "user list");
        result.put("data", userService.findAll(keyword, sort, page, size));

        return result;
    }

    @Operation(summary = "Get user detail", description = "API retrieve user detail by ID from database")
    @GetMapping("/{userId}")
    public Map<String, Object> getUserDetail(@PathVariable @Min(value = 1, message = "userId must be equals or greater than 1") Long userId) {
        log.info("Get user detail by ID: {}", userId);

        // Kiểm tra vai trò người dùng
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails authenticatedUser = (UserDetails) authentication.getPrincipal();
            if (!authenticatedUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN")) &&
                    !authenticatedUser.getUsername().equals(userService.findById(userId).getUsername())) {
                throw new AccessDeniedException("You do not have permission to access this resource");
            }
        }

        UserResponse userDetail = userService.findById(userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "user");
        result.put("data", userDetail);

        return result;
    }

    @Operation(summary = "Create User", description = "API add new user to database")
    @PostMapping("/add")
    public ResponseEntity<Object> createUser(@RequestBody @Valid UserCreationRequest request) {
        log.info("Create User: {}", request);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "User created successfully");
        result.put("data", userService.save(request));

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Update User", description = "API update user to database")
    @PutMapping("/upd")
    public Map<String, Object> updateUser(@RequestBody @Valid UserUpdateRequest request) {
        log.info("Updating user: {}", request);

        userService.update(request);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.ACCEPTED.value());
        result.put("message", "User updated successfully");
        result.put("data", "");

        return result;
    }

    @Operation(summary = "Change Password", description = "API change password for user to database")
    @PatchMapping("/change-pwd")
    public Map<String, Object> changePassword(@RequestBody @Valid UserPasswordRequest request) {
        log.info("Changing password for user: {}", request);

        userService.changePassword(request);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.NO_CONTENT.value());
        result.put("message", "Password updated successfully");
        result.put("data", "");

        return result;
    }

    @Operation(summary = "Delete user", description = "API activate user from database")
    @DeleteMapping("/del/{userId}")
    public Map<String, Object> deleteUser(@PathVariable @Min(value = 1, message = "userId must be equals or greater than 1") Long userId) {
        log.info("Deleting user: {}", userId);

        userService.delete(userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.RESET_CONTENT.value());
        result.put("message", "User deleted successfully");
        result.put("data", "");

        return result;
    }

    @Operation(summary = "Confirm Email", description = "Confirm email for account")
    @GetMapping("/confirm-email")
    public void confirmEmail(@RequestParam String secretCode, HttpServletResponse response) throws IOException {
        log.info("Confirm email for account with secretCode: {}", secretCode);

        try {
            userService.confirmEmail(secretCode); // Call service to confirm email
        } catch (Exception e) {
            log.error("Verification fail", e.getMessage(), e);
        } finally {
            response.sendRedirect("http://localhost:5173/login"); // Redirect to login page
        }
    }
    @GetMapping("/profile")  // Chỉ có GET mapping
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    public Map<String, Object> getCurrentUserProfile() {
        log.info("Get current user profile - START");
        try {
            // Log thông tin authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            log.info("Current authentication: {}", auth);
            log.info("Is authenticated: {}", auth.isAuthenticated());
            log.info("Principal: {}", auth.getPrincipal());
            log.info("Authorities: {}", auth.getAuthorities());

            UserResponse userDetail = userService.getCurrentUserDetail();
            log.info("User detail response: {}", userDetail);

            if (userDetail == null) {
                Map<String, Object> errorResult = new LinkedHashMap<>();
                errorResult.put("status", HttpStatus.NOT_FOUND.value());
                errorResult.put("message", "User not found");
                errorResult.put("data", null);
                return errorResult;
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("status", HttpStatus.OK.value());
            result.put("message", "User profile retrieved successfully");
            result.put("data", userDetail);

            return result;
        } catch (Exception e) {
            log.error("Error getting user profile", e);
            Map<String, Object> errorResult = new LinkedHashMap<>();
            errorResult.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResult.put("message", "Error retrieving user profile");
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }
}