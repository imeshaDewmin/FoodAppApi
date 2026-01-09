package com.selfstudy.foodapp.payment.controller;

import com.selfstudy.foodapp.payment.dto.PaymentDto;
import com.selfstudy.foodapp.payment.service.PaymentService;
import com.selfstudy.foodapp.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Response<?>>initializePayment(@RequestBody @Valid PaymentDto paymentDto){
        return ResponseEntity.ok(paymentService.initializePayment(paymentDto));
    }

    @PutMapping("/update")
    public void updatePaymentForOrder(@RequestBody PaymentDto paymentDto){
         paymentService.updatePaymentForOrder(paymentDto);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<List<PaymentDto>>>getAllPayments(){
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<PaymentDto>>getPaymentById(@PathVariable Long id){
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

}
