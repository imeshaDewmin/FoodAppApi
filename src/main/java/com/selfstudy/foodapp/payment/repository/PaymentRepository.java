package com.selfstudy.foodapp.payment.repository;

import com.selfstudy.foodapp.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {

}
