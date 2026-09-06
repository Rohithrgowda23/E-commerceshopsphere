package com.ecommerce.orderservice.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserClientResponse {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
}
