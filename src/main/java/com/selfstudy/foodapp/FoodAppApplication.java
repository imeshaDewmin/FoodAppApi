package com.selfstudy.foodapp;

import com.selfstudy.foodapp.email_notification.dto.NotificationDto;
import com.selfstudy.foodapp.email_notification.service.NotificationService;
import com.selfstudy.foodapp.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
//@RequiredArgsConstructor
public class FoodAppApplication {

//    private final NotificationService notificationService;

    public static void main(String[] args) {

        SpringApplication.run(FoodAppApplication.class, args);
    }


//    	@Bean
//        CommandLineRunner runner(){
//		return args -> {
//			NotificationDto notificationDTO = NotificationDto.builder()
//					.recipient("dewmin2001@gmail.com")
//					.subject("Hello Dew")
//					.body("Hey this is a test email")
//					.type(NotificationType.EMAIL)
//					.build();
//
//			notificationService.sendEmail(notificationDTO);
//		};
	}

