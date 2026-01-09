package com.selfstudy.foodapp.payment.service;

import com.selfstudy.foodapp.payment.dto.PaymentDto;
import com.selfstudy.foodapp.response.Response;

import java.util.List;

public interface PaymentService {

    Response<?> initializePayment(PaymentDto paymentDto);
    void updatePaymentForOrder(PaymentDto paymentDto);
    Response<List<PaymentDto>> getAllPayments();
    Response<PaymentDto> getPaymentById(Long paymentId);

}
