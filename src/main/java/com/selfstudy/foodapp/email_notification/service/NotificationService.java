package com.selfstudy.foodapp.email_notification.service;

import com.selfstudy.foodapp.email_notification.dto.NotificationDto;

public interface NotificationService {

    void sendEmail(NotificationDto notificationDto);
}
