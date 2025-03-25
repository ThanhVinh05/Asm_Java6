package com.poly.controller;

import com.poly.common.Gender;
import com.poly.controller.request.UserCreationRequest;
import com.poly.controller.request.UserPasswordRequest;
import com.poly.controller.request.UserUpdateRequest;
import com.poly.controller.response.UserPageResponse;
import com.poly.controller.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mockup/user")
@Tag(name = "Mockup User Controller")
public class MockupUserController {

    @Operation(summary = "Get user list", description = "API retrieve user from database")
    @GetMapping("/list")
    public Map<String, Object> getList(@RequestParam(required = false) String keyword,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        UserResponse userResponse1 = new UserResponse();
        userResponse1.setId(1l);
        userResponse1.setFirstName("Phan");
        userResponse1.setLastName("Vinh");
        userResponse1.setGender(Gender.MALE);
        userResponse1.setBirthday(new Date());
        userResponse1.setUsername("admin");
        userResponse1.setEmail("vinhptps38840@gmail.com");
        userResponse1.setPhone("0373696299");

        UserResponse userResponse2 = new UserResponse();
        userResponse2.setId(2l);
        userResponse2.setFirstName("Mai");
        userResponse2.setLastName("Nam");
        userResponse2.setGender(Gender.MALE);
        userResponse2.setBirthday(new Date());
        userResponse2.setUsername("user");
        userResponse2.setEmail("nammvps38841@gmail.com");
        userResponse2.setPhone("0905021503");

        List<UserResponse> userList = List.of(userResponse1, userResponse2);

        UserPageResponse userPageResponse = new UserPageResponse();
        userPageResponse.setPageNumber(page);
        userPageResponse.setPageSize(size);
        userPageResponse.setTotalPages(1);
        userPageResponse.setTotalElements(2);
        userPageResponse.setUsers(userList);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "user list");
        result.put("data", userPageResponse);

        return result;
    }

    @Operation(summary = "Get user detail", description = "API retrieve user detail by ID from database")
    @GetMapping("/{userId}")
    public Map<String, Object> getUserDetail(@PathVariable Long userId) {

        UserResponse userResponse = new UserResponse();
        userResponse.setId(1l);
        userResponse.setFirstName("Phan");
        userResponse.setLastName("Vinh");
        userResponse.setGender(Gender.MALE);
        userResponse.setBirthday(new Date());
        userResponse.setUsername("admin");
        userResponse.setEmail("vinhptps38840@gmail.com");
        userResponse.setPhone("0373696299");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "user");
        result.put("data", userResponse);

        return result;
    }

    @Operation(summary = "Create User", description = "API add new user to database")
    @PostMapping("/add")
    public Map<String, Object> createUser(UserCreationRequest request) {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "User created successfully");
        result.put("data", 1);

        return result;
    }

    @Operation(summary = "Update User", description = "API update user to database")
    @PutMapping("/upd")
    public Map<String, Object> updateUser(UserUpdateRequest request) {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.ACCEPTED.value());
        result.put("message", "User updated successfully");
        result.put("data", "");

        return result;
    }

    @Operation(summary = "Change Password", description = "API change password for user to database")
    @PatchMapping("/change-pwd")
    public Map<String, Object> changePassword(UserPasswordRequest request) {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.NO_CONTENT.value());
        result.put("message", "Password updated successfully");
        result.put("data", "");

        return result;
    }

    @Operation(summary = "Delete user", description = "API activate user from database")
    @DeleteMapping("/del/{userId}")
    public Map<String, Object> deleteUser(@PathVariable Long userId) {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.RESET_CONTENT.value());
        result.put("message", "User deleted successfully");
        result.put("data", "");

        return result;
    }
}