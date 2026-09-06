package com.ecommerce.orderservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Order items are NOT part of this request — they are pulled live from
 * the caller's cart (via cart-service) at checkout time, so the order
 * always reflects exactly what's in the cart the user is checking out.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "Recipient name is required")
    private String shippingRecipientName;

    @NotBlank(message = "Phone number is required")
    private String shippingPhoneNumber;

    @NotBlank(message = "Address line 1 is required")
    private String shippingAddressLine1;

    private String shippingAddressLine2;

    @NotBlank(message = "City is required")
    private String shippingCity;

    @NotBlank(message = "State is required")
    private String shippingState;

    @NotBlank(message = "Postal code is required")
    private String shippingPostalCode;

    @NotBlank(message = "Country is required")
    private String shippingCountry;
}
