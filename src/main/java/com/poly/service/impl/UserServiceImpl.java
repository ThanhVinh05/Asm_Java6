package com.poly.service.impl;

import com.poly.common.Status;
import com.poly.common.UserType;
import com.poly.config.CustomUserDetails;
import com.poly.controller.request.UserCreationRequest;
import com.poly.controller.request.UserPasswordRequest;
import com.poly.controller.request.UserUpdateRequest;
import com.poly.controller.response.AddressResponse;
import com.poly.controller.response.UserPageResponse;
import com.poly.controller.response.UserResponse;
import com.poly.exception.InvalidDataException;
import com.poly.exception.ResourceNotFoundException;
import com.poly.model.AddressEntity;
import com.poly.model.UserEntity;
import com.poly.repository.AddressRepository;
import com.poly.repository.UserRepository;
import com.poly.service.EmailService;
import com.poly.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "USER-SERVICE")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public UserPageResponse findAll(String keyword, String sort, int page, int size) {
        log.info("findAll start");

        // Sorting
        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "id");
        if (StringUtils.hasLength(sort)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)"); // tencot:asc|desc
            Matcher matcher = pattern.matcher(sort);
            if (matcher.find()) {
                String columnName = matcher.group(1);
                if (matcher.group(3).equalsIgnoreCase("asc")) {
                    order = new Sort.Order(Sort.Direction.ASC, columnName);
                } else {
                    order = new Sort.Order(Sort.Direction.DESC, columnName);
                }
            }
        }

        // Xu ly truong hop FE muon bat dau voi page = 1
        int pageNo = 0;
        if (page > 0) {
            pageNo = page - 1;
        }

        // Paging
        Pageable pageable = PageRequest.of(pageNo, size, Sort.by(order));

        Page<UserEntity> entityPage;

        if (StringUtils.hasLength(keyword)) {
            keyword = "%" + keyword.toLowerCase() + "%";
            entityPage = userRepository.searchByKeyword(keyword, pageable);
        } else {
            entityPage = userRepository.findAll(pageable);
        }

        return getUserPageResponse(page, size, entityPage);
    }

    @Override
    public UserResponse findById(Long id) {
        log.info("Find user by id: {}", id);

        UserEntity userEntity = getUserEntity(id);
        List<AddressEntity> addressEntities = addressRepository.findByUserId(id);

        List<AddressResponse> addressResponses = addressEntities.stream()
                .map(entity -> AddressResponse.builder()
                        .streetNumber(entity.getStreetNumber())
                        .commune(entity.getCommune())
                        .district(entity.getDistrict())
                        .city(entity.getCity())
                        .country(entity.getCountry())
                        .addressType(entity.getAddressType())
                        .build())
                .collect(Collectors.toList());

        return UserResponse.builder()
                .id(id)
                .username(userEntity.getUsername())
                .birthday(userEntity.getBirthday())
                .gender(userEntity.getGender())
                .phone(userEntity.getPhone())
                .email(userEntity.getEmail())
                .addresses(addressResponses)
                .build();
    }

    private UserEntity getUserEntityFromUserDetails(UserDetails userDetails) {
        log.info("Getting UserEntity from UserDetails: {}", userDetails.getUsername());
        try {
            UserEntity userEntity = userRepository.findByUsername(userDetails.getUsername());
            if (userEntity == null) {
                log.error("No user found with username: {}", userDetails.getUsername());
                return null;
            }
            log.info("Found user entity: {}", userEntity);
            return userEntity;
        } catch (Exception e) {
            log.error("Error getting user entity from UserDetails", e);
            throw e;
        }
    }

    @Override
    public UserResponse getCurrentUserDetail() {
        log.info("Getting current user detail - START");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            log.info("Authentication object: {}", authentication);

            if (authentication == null || !authentication.isAuthenticated()) {
                log.error("No authenticated user found");
                throw new InvalidDataException("Người dùng chưa đăng nhập");
            }

            Object principal = authentication.getPrincipal();
            log.info("Principal type: {}", principal.getClass().getName());

            UserEntity userEntity;
            if (principal instanceof CustomUserDetails customUserDetails) {
                userEntity = customUserDetails.getUser();
            } else {
                log.error("Unsupported principal type");
                throw new InvalidDataException("Không thể lấy thông tin người dùng");
            }

            // Lấy địa chỉ của user
            List<AddressEntity> addressEntities = addressRepository.findByUserId(userEntity.getId());
            log.info("Found {} addresses for user", addressEntities.size());

            // Kiểm tra chi tiết về địa chỉ để debug
            if (addressEntities.isEmpty()) {
                log.warn("No addresses found for user ID: {}, creating default empty address", userEntity.getId());
                // Tạo địa chỉ mặc định trống để tránh null
                AddressEntity defaultEmptyAddress = new AddressEntity();
                defaultEmptyAddress.setUserId(userEntity.getId());
                defaultEmptyAddress.setAddressType(1);
                addressEntities.add(defaultEmptyAddress);
            } else {
                // Log chi tiết về địa chỉ đầu tiên để debug
                AddressEntity firstAddress = addressEntities.get(0);
                log.info(
                        "First address details: streetNumber={}, commune={}, district={}, city={}, country={}, type={}",
                        firstAddress.getStreetNumber(),
                        firstAddress.getCommune(),
                        firstAddress.getDistrict(),
                        firstAddress.getCity(),
                        firstAddress.getCountry(),
                        firstAddress.getAddressType());
            }

            List<AddressResponse> addressResponses = addressEntities.stream()
                    .map(entity -> {
                        // Đảm bảo không có giá trị null
                        return AddressResponse.builder()
                                .streetNumber(entity.getStreetNumber() != null ? entity.getStreetNumber() : "")
                                .commune(entity.getCommune() != null ? entity.getCommune() : "")
                                .district(entity.getDistrict() != null ? entity.getDistrict() : "")
                                .city(entity.getCity() != null ? entity.getCity() : "")
                                .country(entity.getCountry() != null ? entity.getCountry() : "Việt Nam")
                                .addressType(entity.getAddressType() != null ? entity.getAddressType() : 1)
                                .build();
                    })
                    .collect(Collectors.toList());

            // Log thông tin địa chỉ đã chuyển đổi
            if (!addressResponses.isEmpty()) {
                log.info("Converted first address: {}", addressResponses.get(0));
            }

            UserResponse response = UserResponse.builder()
                    .id(userEntity.getId())
                    .username(userEntity.getUsername())
                    .birthday(userEntity.getBirthday())
                    .gender(userEntity.getGender())
                    .phone(userEntity.getPhone())
                    .email(userEntity.getEmail())
                    .addresses(addressResponses)
                    .build();

            log.info("Returning user response with {} addresses", addressResponses.size());
            return response;
        } catch (Exception e) {
            log.error("Error in getCurrentUserDetail", e);
            throw e;
        }
    }

    @Override
    public UserResponse findByUsername(String username) {
        return null;
    }

    @Override
    public UserResponse findByEmail(String email) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long save(UserCreationRequest req) {
        log.info("Saving user: {}", req);

        UserEntity userByEmail = userRepository.findByEmail(req.getEmail());
        if (userByEmail != null) {
            throw new InvalidDataException("Email already exists");
        }

        UserEntity user = new UserEntity();
        user.setUsername(req.getUsername());
        user.setBirthday(req.getBirthday());
        user.setGender(req.getGender());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword())); // Encode password

        user.setType(UserType.USER); // Set type to OWNER
        user.setStatus(Status.NONE); // Set status to NONE

        // Generate secret code and save to db
        String secretCode = UUID.randomUUID().toString();
        user.setSecretCode(secretCode);
        log.info("secretCode = {}", secretCode);

        userRepository.save(user);
        log.info("Saved user: {}", user);

        if (user.getId() != null) {
            log.info("user id: {}", user.getId());
            List<AddressEntity> addresses = new ArrayList<>();
            AddressEntity addressEntity = new AddressEntity();
            addressEntity.setUserId(user.getId());
            addressEntity.setAddressType(1); // Set default address type
            addresses.add(addressEntity);
            addressRepository.saveAll(addresses);
            log.info("Saved addresses: {}", addresses);
        }

        // Send email verification
        try {
            emailService.sendVerificationEmail(req.getEmail(), req.getUsername(), secretCode); // Pass secretCode to email service
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return user.getId();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserUpdateRequest req) {
        log.info("Updating user: {}", req);

        // Get user by id
        UserEntity user = getUserEntity(req.getId());
        user.setUsername(req.getUsername());
        user.setBirthday(req.getBirthday());
        user.setGender(req.getGender());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());

        userRepository.save(user);
        log.info("Updated user: {}", user);

        // save address
        List<AddressEntity> addresses = new ArrayList<>();

        req.getAddresses().forEach(address -> {
            AddressEntity addressEntity = addressRepository.findByUserIdAndAddressType(user.getId(), address.getAddressType());
            if (addressEntity == null) {
                addressEntity = new AddressEntity();
            }
            addressEntity.setStreetNumber(address.getStreetNumber());
            addressEntity.setCommune(address.getCommune());
            addressEntity.setDistrict(address.getDistrict());
            addressEntity.setCity(address.getCity());
            addressEntity.setCountry(address.getCountry());
            addressEntity.setAddressType(address.getAddressType());
            addressEntity.setUserId(user.getId());

            addresses.add(addressEntity);
        });

        // save addresses
        addressRepository.saveAll(addresses);
        log.info("Updated addresses: {}", addresses);
    }

    @Override
    public void changePassword(UserPasswordRequest req) {
        log.info("Changing password for user: {}", req);

        // Get user by id
        UserEntity user = getUserEntity(req.getId());
        if (req.getPassword().equals(req.getConfirmPassword())) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        userRepository.save(user);
        log.info("Changed password for user: {}", user);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting user: {}", id);

        // Get user by id
        UserEntity user = getUserEntity(id);
        user.setStatus(Status.INACTIVE);

        userRepository.save(user);
        log.info("Deleted user id: {}", id);
    }

    /**
     * Get user by id
     *
     * @param id
     * @return
     */
    private UserEntity getUserEntity(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Convert EserEntities to UserResponse
     *
     * @param page
     * @param size
     * @param userEntities
     * @return
     */
    private static UserPageResponse getUserPageResponse(int page, int size, Page<UserEntity> userEntities) {
        log.info("Convert User Entity Page");

        List<UserResponse> userList = userEntities.stream().map(entity -> UserResponse.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .birthday(entity.getBirthday())
                .gender(entity.getGender())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .build()
        ).toList();

        UserPageResponse response = new UserPageResponse();
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(userEntities.getTotalElements());
        response.setTotalPages(userEntities.getTotalPages());
        response.setUsers(userList);

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmEmail(String secretCode) {
        UserEntity user = userRepository.findBySecretCode(secretCode);
        if (user == null) {
            throw new ResourceNotFoundException("Invalid secret code");
        }
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
        log.info("User status updated to ACTIVE for user: {}", user.getUsername()); // Thêm log
    }


}