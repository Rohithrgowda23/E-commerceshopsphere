package com.ecommerce.notificationservice.service;

import com.ecommerce.notificationservice.dto.response.PagedResponse;
import com.ecommerce.notificationservice.entity.Notification;
import com.ecommerce.notificationservice.entity.NotificationType;
import com.ecommerce.notificationservice.mapper.NotificationMapper;
import com.ecommerce.notificationservice.repository.NotificationRepository;
import com.ecommerce.notificationservice.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void logNotification_newEvent_savesNotification() {
        when(notificationRepository.existsBySourceEventId("event-1")).thenReturn(false);

        notificationService.logNotification(
                "event-1", NotificationType.ORDER_CREATED, "user-1", "order-1", "Title", "Message");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals("event-1", captor.getValue().getSourceEventId());
        assertEquals(NotificationType.ORDER_CREATED, captor.getValue().getType());
    }

    @Test
    void logNotification_duplicateEvent_skipsSave() {
        when(notificationRepository.existsBySourceEventId("event-1")).thenReturn(true);

        notificationService.logNotification(
                "event-1", NotificationType.ORDER_CREATED, "user-1", "order-1", "Title", "Message");

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void getNotificationsForUser_returnsPagedResponse() {
        Notification notification = Notification.builder()
                .userId("user-1").type(NotificationType.ORDER_CREATED)
                .title("Title").message("Message").build();
        Page<Notification> page = new PageImpl<>(List.of(notification));

        when(notificationRepository.findByUserId(any(), any())).thenReturn(page);
        when(notificationMapper.toResponse(any(Notification.class))).thenReturn(
                com.ecommerce.notificationservice.dto.response.NotificationResponse.builder()
                        .userId("user-1").build());

        PagedResponse<com.ecommerce.notificationservice.dto.response.NotificationResponse> response =
                notificationService.getNotificationsForUser("user-1", 0, 20);

        assertEquals(1, response.getContent().size());
    }
}
