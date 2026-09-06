package com.ecommerce.userservice.service.impl;

import com.ecommerce.userservice.dto.request.AddressRequest;
import com.ecommerce.userservice.dto.response.AddressResponse;
import com.ecommerce.userservice.entity.Address;
import com.ecommerce.userservice.exception.ResourceNotFoundException;
import com.ecommerce.userservice.mapper.UserMapper;
import com.ecommerce.userservice.repository.AddressRepository;
import com.ecommerce.userservice.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressServiceImpl.class);

    private final AddressRepository addressRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(String userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(userMapper::toAddressResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressResponse addAddress(String userId, AddressRequest request) {
        if (request.isDefault()) {
            clearExistingDefault(userId);
        }

        Address address = Address.builder()
                .userId(userId)
                .label(request.getLabel())
                .recipientName(request.getRecipientName())
                .phoneNumber(request.getPhoneNumber())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .isDefault(request.isDefault())
                .build();

        Address saved = addressRepository.save(address);
        log.info("Added address id={} for userId={}", saved.getId(), userId);
        return userMapper.toAddressResponse(saved);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(String userId, String addressId, AddressRequest request) {
        Address address = findOwnedAddressOrThrow(userId, addressId);

        if (request.isDefault() && !address.isDefault()) {
            clearExistingDefault(userId);
        }

        address.setLabel(request.getLabel());
        address.setRecipientName(request.getRecipientName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setDefault(request.isDefault());

        Address updated = addressRepository.save(address);
        log.info("Updated address id={} for userId={}", addressId, userId);
        return userMapper.toAddressResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAddress(String userId, String addressId) {
        Address address = findOwnedAddressOrThrow(userId, addressId);
        addressRepository.delete(address);
        log.info("Deleted address id={} for userId={}", addressId, userId);
    }

    private Address findOwnedAddressOrThrow(String userId, String addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No address found with id: " + addressId + " for this user"));
    }

    private void clearExistingDefault(String userId) {
        addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .forEach(existing -> {
                    existing.setDefault(false);
                    addressRepository.save(existing);
                });
    }
}
