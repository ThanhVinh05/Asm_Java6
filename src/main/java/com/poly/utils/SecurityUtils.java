package com.poly.utils;

import com.poly.config.CustomUserDetails;
import com.poly.exception.InvalidDataException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidDataException("Người dùng chưa đăng nhập");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUserId();
        }

        throw new InvalidDataException("Không thể lấy thông tin người dùng");
    }

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidDataException("Người dùng chưa đăng nhập");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUsername();
        }

        throw new InvalidDataException("Không thể lấy thông tin người dùng");
    }

    private SecurityUtils() {
        throw new IllegalStateException("Utility class");
    }
}