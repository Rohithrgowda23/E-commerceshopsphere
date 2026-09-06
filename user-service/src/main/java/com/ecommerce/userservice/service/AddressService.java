package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.request.AddressRequest;
import com.ecommerce.userservice.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {

    List<AddressResponse> getAddresses(String userId);

    AddressResponse addAddress(String userId, AddressRequest request);

    AddressResponse updateAddress(String userId, String addressId, AddressRequest request);

    void deleteAddress(String userId, String addressId);
}
