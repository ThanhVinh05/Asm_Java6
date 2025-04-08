package com.poly.service.impl;

import com.poly.controller.request.AddressRequest;
import com.poly.controller.response.AddressResponse;
import com.poly.exception.ResourceNotFoundException;
import com.poly.model.AddressEntity;
import com.poly.model.UserEntity;
import com.poly.repository.AddressRepository;
import com.poly.repository.UserRepository;
import com.poly.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "ADDRESS-SERVICE")
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void updateUserAddresses(Long userId, List<AddressRequest> addresses) {
        log.info("Updating addresses for user ID: {}", userId);

        // Verify user exists
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Delete all existing addresses
        List<AddressEntity> existingAddresses = addressRepository.findByUserId(userId);
        if (!existingAddresses.isEmpty()) {
            log.info("Deleting {} existing addresses for user ID: {}", existingAddresses.size(), userId);
            for (AddressEntity address : existingAddresses) {
                addressRepository.delete(address);
            }
        }

        // Save new addresses
        if (addresses != null && !addresses.isEmpty()) {
            log.info("Saving {} new addresses for user ID: {}", addresses.size(), userId);
            for (AddressRequest addressReq : addresses) {
                AddressEntity address = new AddressEntity();
                address.setUserId(userId);
                address.setStreetNumber(addressReq.getStreetNumber());
                address.setCommune(addressReq.getCommune());
                address.setDistrict(addressReq.getDistrict());
                address.setCity(addressReq.getCity());
                address.setCountry(addressReq.getCountry() != null ? addressReq.getCountry() : "Việt Nam");
                address.setAddressType(addressReq.getAddressType() != null ? addressReq.getAddressType() : 1);

                addressRepository.save(address);
            }
            log.info("Successfully saved new addresses for user ID: {}", userId);
        } else {
            log.info("No new addresses to save for user ID: {}", userId);
        }
    }

    @Override
    public List<AddressResponse> getUserAddresses(Long userId) {
        log.info("Getting addresses for user ID: {}", userId);

        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Get all addresses
        List<AddressEntity> addresses = addressRepository.findByUserId(userId);

        // Map to response
        return addresses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AddressResponse mapToResponse(AddressEntity address) {
        return AddressResponse.builder()
                .streetNumber(address.getStreetNumber())
                .commune(address.getCommune())
                .district(address.getDistrict())
                .city(address.getCity())
                .country(address.getCountry())
                .addressType(address.getAddressType())
                .build();
    }
}
