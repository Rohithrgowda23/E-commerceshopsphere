package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.dto.response.UserProfileResponse;
import com.ecommerce.userservice.exception.ForbiddenException;
import com.ecommerce.userservice.security.JwtAuthenticationFilter;
import com.ecommerce.userservice.security.SecurityUtils;
import com.ecommerce.userservice.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileService userProfileService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getProfile_owner_returns200() throws Exception {
        doNothing().when(securityUtils).assertOwnerOrAdmin("user-1");
        when(userProfileService.getProfile("user-1"))
                .thenReturn(UserProfileResponse.builder().userId("user-1").build());

        mockMvc.perform(get("/api/users/user-1"))
                .andExpect(status().isOk());
    }

    @Test
    void getProfile_notOwner_returns403() throws Exception {
        doThrow(new ForbiddenException("You may only access your own resources"))
                .when(securityUtils).assertOwnerOrAdmin("user-2");

        mockMvc.perform(get("/api/users/user-2"))
                .andExpect(status().isForbidden());
    }
}
