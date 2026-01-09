package com.selfstudy.foodapp.payment.service;

import com.selfstudy.foodapp.email_notification.dto.NotificationDto;
import com.selfstudy.foodapp.email_notification.service.NotificationService;
import com.selfstudy.foodapp.enums.OrderStatus;
import com.selfstudy.foodapp.enums.PaymentGateway;
import com.selfstudy.foodapp.enums.PaymentStatus;
import com.selfstudy.foodapp.exceptions.BadRequestException;
import com.selfstudy.foodapp.exceptions.NotFoundException;
import com.selfstudy.foodapp.order.entity.Order;
import com.selfstudy.foodapp.order.repository.OrderRepository;
import com.selfstudy.foodapp.payment.dto.PaymentDto;
import com.selfstudy.foodapp.payment.entity.Payment;
import com.selfstudy.foodapp.payment.repository.PaymentRepository;
import com.selfstudy.foodapp.response.Response;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static com.selfstudy.foodapp.enums.PaymentStatus.COMPLETED;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${stripe.api.secret.key}")
    private String secreteKey;

    @Value("${frontend.base.url}")
    private String frontendBaseUrl;

    @Override
    public Response<?> initializePayment(PaymentDto paymentDto) {
        log.info("Inside initializePayment()");

        Stripe.apiKey = secreteKey;

        Long orderId = paymentDto.getOrderId();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> new NotFoundException("Order not found"));

        if(order.getPaymentStatus() == COMPLETED){
            throw new BadRequestException("Payment already done");
        }

        if(!order.getTotalAmount().equals(paymentDto.getAmount())){
            throw new BadRequestException("Payment amount is invalid");
        }

        try{
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(paymentDto.getAmount().longValue())
                    .setCurrency("lkr")
                    .putMetadata("orderId", String.valueOf(orderId))
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            String uniqueTransactionId = intent.getClientSecret();

            return Response.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Payment success")
                    .data(uniqueTransactionId)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }


    }

    @Override
    public void updatePaymentForOrder(PaymentDto paymentDto) {
        log.info("Inside updatePaymentForOrder()");

        Long orderId = paymentDto.getOrderId();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(()-> new NotFoundException("Order not found"));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentGateway(PaymentGateway.STRIPE);
        payment.setTransactionId(paymentDto.getTransactionId());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus(paymentDto.getPaymentStatus());

        if(!paymentDto.isSuccess()){
            payment.setFailureReason(paymentDto.getFailureReason());
        }

        paymentRepository.save(payment);

        // Prepare email context. Context should be. imported from thymeleaf
        Context context = new Context(Locale.getDefault());
        context.setVariable("customerName", order.getUser().getName());
        context.setVariable("orderId", order.getId());
        context.setVariable("currentYear", Year.now().getValue());
        context.setVariable("amount", "lkr" + paymentDto.getAmount());

        if (paymentDto.isSuccess()) {
            order.setPaymentStatus(PaymentStatus.COMPLETED);
            order.setOrderStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);


            log.info("PAYMENT IS SUCCESSFUL ABOUT TO SEND EMAIL");

            // Add success-specific variables
            context.setVariable("transactionId", paymentDto.getTransactionId());
            context.setVariable("paymentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")));
            context.setVariable("frontendBaseUrl", this.frontendBaseUrl);

            String emailBody = templateEngine.process("payment-success", context);

            log.info("HAVE GOTTEN TEMPLATE");

            notificationService.sendEmail(NotificationDto.builder()
                    .recipient(order.getUser().getEmail())
                    .subject("Payment Successful - Order #" + order.getId())
                    .body(emailBody)
                    .isHtml(true)
                    .build());
        } else {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);


            log.info("PAYMENT IS FAILED ABOUT TO SEND EMAIL");
            // Add failure-specific variables
            context.setVariable("failureReason", paymentDto.getFailureReason());

            String emailBody = templateEngine.process("payment-failed", context);

            notificationService.sendEmail(NotificationDto.builder()
                    .recipient(order.getUser().getEmail())
                    .subject("Payment Failed - Order #" + order.getId())
                    .body(emailBody)
                    .isHtml(true)
                    .build());
        }

    }

    @Override
    public Response<List<PaymentDto>> getAllPayments() {
        log.info("Inside getAllPayments()");

        List<Payment> payments = paymentRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));

        List<PaymentDto> paymentDtos = payments.stream()
                .map(payment -> modelMapper.map(payment,PaymentDto.class))
                .toList();

        paymentDtos.forEach(item -> {
            item.setOrder(null);
            item.setUser(null);
        });

        return Response.<List<PaymentDto>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("All payments retrieved successfully")
                .data(paymentDtos)
                .build();
    }

    @Override
    public Response<PaymentDto> getPaymentById(Long paymentId) {
        log.info("Inside getPaymentById()");

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(()-> new NotFoundException("Payment not found for this id"));

        PaymentDto paymentDto = modelMapper.map(payment,PaymentDto.class);

        paymentDto.getUser().setRoles(null);
        paymentDto.getOrder().setUser(null);
        paymentDto.getOrder().getOrderItems().forEach(item->{
            item.getMenu().setReviews(null);
        });

        return Response.<PaymentDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Payment retrieved success for the id")
                .data(paymentDto)
                .build();
    }
}
