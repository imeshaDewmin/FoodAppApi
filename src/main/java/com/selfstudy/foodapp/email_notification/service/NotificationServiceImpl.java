package com.selfstudy.foodapp.email_notification.service;

import com.selfstudy.foodapp.email_notification.dto.NotificationDto;
import com.selfstudy.foodapp.email_notification.entity.Notification;
import com.selfstudy.foodapp.email_notification.repository.NotificationRepository;
import com.selfstudy.foodapp.enums.NotificationType;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService{

    private final JavaMailSender javaMailSender;
    private final NotificationRepository notificationRepository;

    @Override
    @Async
    public void sendEmail(NotificationDto notificationDto) {
        log.info("Inside sendEmail() ");
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            helper.setTo(notificationDto.getRecipient());
            helper.setSubject(notificationDto.getSubject());
            helper.setText(notificationDto.getBody(), notificationDto.isHtml());

            javaMailSender.send(mimeMessage);


            Notification notificationToSave = Notification.builder()
                    .recipient(notificationDto.getRecipient())
                    .subject(notificationDto.getSubject())
                    .body(notificationDto.getBody())
                    .type(NotificationType.EMAIL)
                    .isHtml(notificationDto.isHtml())
                    .build();

            notificationRepository.save(notificationToSave);
            log.info("Saved to notification table");
        }
        catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
        }
    }

