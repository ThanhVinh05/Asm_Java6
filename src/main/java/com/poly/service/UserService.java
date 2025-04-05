package com.poly.service;


import com.poly.controller.request.UserCreationRequest;
import com.poly.controller.request.UserPasswordRequest;
import com.poly.controller.request.UserUpdateRequest;
import com.poly.controller.response.UserPageResponse;
import com.poly.controller.response.UserResponse;

public interface UserService {

    UserPageResponse findAll(String keyword, String sort, int page, int size);

    UserResponse findById(Long id);

    UserResponse getCurrentUserDetail();

    UserResponse findByUsername(String username);

    UserResponse findByEmail(String email);

    long save(UserCreationRequest req);

    void update(UserUpdateRequest req);

    void changePassword(UserPasswordRequest req);

    void delete(Long id);

    void confirmEmail(String secretCode);
}
