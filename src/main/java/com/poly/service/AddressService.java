package com.poly.service;

import com.poly.controller.request.AddressRequest;
import com.poly.controller.response.AddressResponse;

import java.util.List;

public interface AddressService {

    /**
     * Update addresses for a user
     * @param userId User ID
     * @param addresses List of address requests
     */
    void updateUserAddresses(Long userId, List<AddressRequest> addresses);

    /**
     * Get all addresses for a user
     * @param userId User ID
     * @return List of address responses
     */
    List<AddressResponse> getUserAddresses(Long userId);
}