package com.poly.service;

import com.poly.config.CustomUserDetails;
import com.poly.model.UserEntity;
import com.poly.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceDetail implements UserDetailsService {

    private final UserRepository userRepository;

    public UserServiceDetail(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            log.error("User not found with username: {}", username);
            throw new UsernameNotFoundException("User not found");
        }
        log.info("Found user: {}", user.getUsername());
        return new CustomUserDetails(user);
    }

    public UserDetailsService userDetailsService() {
        return this;
    }
}