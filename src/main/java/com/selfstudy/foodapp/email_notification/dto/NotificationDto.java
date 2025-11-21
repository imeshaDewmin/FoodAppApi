package com.selfstudy.foodapp.email_notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.selfstudy.foodapp.enums.NotificationType;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class NotificationDto {
    private Long id;

    private String subject;

    @NotBlank(message="Recipient is required")
    private String recipient;

    @Lob
    private String body;

    private NotificationType type;

    private LocalDateTime createdAt;

    private boolean isHtml;
}
