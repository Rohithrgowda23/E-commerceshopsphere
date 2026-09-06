package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.dto.request.AddressRequest;
import com.ecommerce.userservice.dto.response.AddressResponse;
import com.ecommerce.userservice.security.SecurityUtils;
import com.ecommerce.userservice.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{id}/addresses")
@RequiredArgsConstructor
@Tag(name = "User Addresses", description = "Manage a user's shipping/billing addresses")
public class AddressController {

    private final AddressService addressService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "List a user's addresses (owner or ADMIN only)")
    public ResponseEntity<List<AddressResponse>> getAddresses(@PathVariable String id) {
        securityUtils.assertOwnerOrAdmin(id);
        return ResponseEntity.ok(addressService.getAddresses(id));
    }

    @PostMapping
    @Operation(summary = "Add a new address (owner or ADMIN only)")
    public ResponseEntity<AddressResponse> addAddress(
            @PathVariable String id, @Valid @RequestBody AddressRequest request) {
        securityUtils.assertOwnerOrAdmin(id);
        AddressResponse response = addressService.addAddress(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{addressId}")
    @Operation(summary = "Update an existing address (owner or ADMIN only)")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable String id, @PathVariable String addressId, @Valid @RequestBody AddressRequest request) {
        securityUtils.assertOwnerOrAdmin(id);
        return ResponseEntity.ok(addressService.updateAddress(id, addressId, request));
    }

    @DeleteMapping("/{addressId}")
    @Operation(summary = "Delete an address (owner or ADMIN only)")
    public ResponseEntity<Void> deleteAddress(@PathVariable String id, @PathVariable String addressId) {
        securityUtils.assertOwnerOrAdmin(id);
        addressService.deleteAddress(id, addressId);
        return ResponseEntity.noContent().build();
    }
}
