package com.ecommerce.notificationservice.controller;

import com.ecommerce.notificationservice.dto.response.NotificationResponse;
import com.ecommerce.notificationservice.dto.response.PagedResponse;
import com.ecommerce.notificationservice.security.JwtAuthenticationFilter;
import com.ecommerce.notificationservice.security.SecurityUtils;
import com.ecommerce.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getMyNotifications_returns200() throws Exception {
        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(notificationService.getNotificationsForUser("user-1", 0, 20)).thenReturn(
                PagedResponse.<NotificationResponse>builder().content(List.of()).build());

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk());
    }
}
