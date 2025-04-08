package com.poly.controller;

import com.poly.controller.request.AddressRequest;
import com.poly.service.AddressService;
import com.poly.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/address")
@Tag(name = "Address Controller")
@Slf4j(topic = "ADDRESS-CONTROLLER")
@RequiredArgsConstructor
@Validated
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "Update User Address", description = "API update address for current logged in user")
    @PutMapping("/upd")
    public Map<String, Object> updateAddress(@RequestBody @Valid List<AddressRequest> addresses) {
        log.info("Updating address for current user");

        Long userId = SecurityUtils.getCurrentUserId();
        addressService.updateUserAddresses(userId, addresses);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.ACCEPTED.value());
        result.put("message", "Address updated successfully");
        result.put("data", "");

        return result;
    }

    @Operation(summary = "Get User Addresses", description = "API get addresses for current logged in user")
    @GetMapping("/list")
    public Map<String, Object> getUserAddresses() {
        log.info("Getting addresses for current user");

        Long userId = SecurityUtils.getCurrentUserId();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "User addresses");
        result.put("data", addressService.getUserAddresses(userId));

        return result;
    }
}
