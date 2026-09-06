package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.request.AddressRequest;
import com.ecommerce.userservice.dto.response.AddressResponse;
import com.ecommerce.userservice.entity.Address;
import com.ecommerce.userservice.exception.ResourceNotFoundException;
import com.ecommerce.userservice.mapper.UserMapper;
import com.ecommerce.userservice.repository.AddressRepository;
import com.ecommerce.userservice.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    @Test
    void addAddress_firstDefaultAddress_savesSuccessfully() {
        AddressRequest request = new AddressRequest(
                "Home", "Jane Doe", "+15551234567", "123 Main St", null,
                "Springfield", "IL", "62704", "USA", true);

        when(addressRepository.findByUserIdAndIsDefaultTrue("user-1")).thenReturn(List.of());
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toAddressResponse(any(Address.class)))
                .thenReturn(AddressResponse.builder().userId("user-1").isDefault(true).build());

        AddressResponse response = addressService.addAddress("user-1", request);

        assertEquals(true, response.isDefault());
    }

    @Test
    void addAddress_newDefault_clearsPreviousDefault() {
        AddressRequest request = new AddressRequest(
                "Office", "Jane Doe", "+15551234567", "456 Work Ave", null,
                "Springfield", "IL", "62704", "USA", true);

        Address previousDefault = Address.builder()
                .id("addr-old").userId("user-1").isDefault(true).build();

        when(addressRepository.findByUserIdAndIsDefaultTrue("user-1")).thenReturn(List.of(previousDefault));
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toAddressResponse(any(Address.class)))
                .thenReturn(AddressResponse.builder().build());

        addressService.addAddress("user-1", request);

        assertEquals(false, previousDefault.isDefault());
        verify(addressRepository, times(2)).save(any(Address.class)); // old default + new address
    }

    @Test
    void deleteAddress_notOwnedByUser_throwsResourceNotFound() {
        when(addressRepository.findByIdAndUserId("addr-1", "user-1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> addressService.deleteAddress("user-1", "addr-1"));
    }

    @Test
    void deleteAddress_ownedByUser_deletesSuccessfully() {
        Address address = Address.builder().id("addr-1").userId("user-1").build();
        when(addressRepository.findByIdAndUserId("addr-1", "user-1")).thenReturn(Optional.of(address));

        addressService.deleteAddress("user-1", "addr-1");

        verify(addressRepository).delete(address);
    }
}
