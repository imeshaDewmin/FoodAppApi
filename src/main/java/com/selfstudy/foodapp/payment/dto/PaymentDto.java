package com.selfstudy.foodapp.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.selfstudy.foodapp.auth_users.dto.UserDTO;
import com.selfstudy.foodapp.enums.PaymentGateway;
import com.selfstudy.foodapp.enums.PaymentStatus;
import com.selfstudy.foodapp.order.dto.OrderDto;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentDto {
    private Long id;

    private Long orderId;

    private BigDecimal amount;

    private PaymentStatus paymentStatus;

    private String transactionId;

    private PaymentGateway paymentGateway;

    private String failureReason;

    private LocalDateTime paymentDate;

    private boolean success;

    private OrderDto order;

    private UserDTO user;
}
