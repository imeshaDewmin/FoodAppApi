package com.selfstudy.foodapp.order.service;

import com.selfstudy.foodapp.auth_users.entity.User;
import com.selfstudy.foodapp.auth_users.service.UserService;
import com.selfstudy.foodapp.cart.entity.Cart;
import com.selfstudy.foodapp.cart.entity.CartItem;
import com.selfstudy.foodapp.cart.repository.CartRepository;
import com.selfstudy.foodapp.cart.service.CartService;
import com.selfstudy.foodapp.email_notification.dto.NotificationDto;
import com.selfstudy.foodapp.email_notification.service.NotificationService;
import com.selfstudy.foodapp.enums.OrderStatus;
import com.selfstudy.foodapp.enums.PaymentStatus;
import com.selfstudy.foodapp.exceptions.BadRequestException;
import com.selfstudy.foodapp.exceptions.NotFoundException;
import com.selfstudy.foodapp.order.dto.OrderDto;
import com.selfstudy.foodapp.order.dto.OrderItemDto;
import com.selfstudy.foodapp.order.entity.Order;
import com.selfstudy.foodapp.order.entity.OrderItem;
import com.selfstudy.foodapp.order.repository.OrderItemRepository;
import com.selfstudy.foodapp.order.repository.OrderRepository;
import com.selfstudy.foodapp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private ModelMapper modelMapper;

    @Value("${base.payment.link}")
    private String basePaymentLink;

    @Override
    @Transactional
    public Response<?> placeOrderFromCart() {
        log.info("Inside placeOrderFromCart()");

        User customer = userService.getCurrentLoggedInUser();

        String deliveryAddress= customer.getAddress();

        if(deliveryAddress == null){
            throw new NotFoundException("Delivery address is not present for the user");
        }

        Cart cart = cartRepository.findByUser_Id(customer.getId())
                .orElseThrow(()-> new NotFoundException("Cart is not found for the user"));

        List<CartItem> cartItems = cart.getCartItems();

        if(cartItems == null || cartItems.isEmpty()){
            throw new BadRequestException("Cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems){
            OrderItem orderItem = OrderItem.builder()
                    .menu(cartItem.getMenu())
                    .quantity(cartItem.getQuantity())
                    .pricePerUnit(cartItem.getPricePerUnit())
                    .subTotal(cartItem.getSubTotal())
                    .build();
            orderItems.add(orderItem);
            totalAmount=totalAmount.add(orderItem.getSubTotal());
        }

        Order order = Order.builder()
                .user(customer)
                .orderDate(LocalDateTime.now())
                .orderItems(orderItems)
                .totalAmount(totalAmount)
                .orderStatus(OrderStatus.INITIALIZED)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        Order savedOrder =orderRepository.save(order);
        orderItems.forEach(orderItem -> orderItem.setOrder(savedOrder));
        orderItemRepository.saveAll(orderItems);

        cartService.clearShoppingCart();

        OrderDto orderDto = modelMapper.map(savedOrder,OrderDto.class);

        sendConfirmationEmail(customer,orderDto);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Order successfully placed")
                .build();

    }

    @Override
    public Response<OrderDto> getOrderById(Long id) {
        log.info("Inside getOrderById()");

        Order order = orderRepository.findById(id)
                .orElseThrow(()->new NotFoundException("Order doesn't exist for this id"));

        OrderDto orderDto = modelMapper.map(order,OrderDto.class);

        return Response.<OrderDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Order retrieved successfully")
                .data(orderDto)
                .build();
    }

    @Override
    public Response<Page<OrderDto>> getAllOrders(OrderStatus orderStatus, int page, int size) {
        log.info("Inside getAllOrders()");

        Pageable pageable = PageRequest.of(page,size, Sort.by(Sort.Direction.DESC,"id"));

        Page<Order> orderPage;

        if(orderStatus != null){
            orderPage = orderRepository.findByOrderStatus(orderStatus,pageable);
        }else{
            orderPage=orderRepository.findAll(pageable);
        }

        Page<OrderDto> orderDtoPage  = orderPage.map(order -> {
            OrderDto dto = modelMapper.map(order, OrderDto.class);
            dto.getOrderItems().forEach(orderItemDTO -> orderItemDTO.getMenu().setReviews(null));
            return dto;
        });

        return Response.<Page<OrderDto>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Orders retrieved successfully")
                .data(orderDtoPage)
                .build();

    }

    @Override
    public Response<List<OrderDto>> getOrdersOfUsers() {
        log.info("Inside getOrdersOfUsers()");

        User customer = userService.getCurrentLoggedInUser();

        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(customer);

        List<OrderDto> orderDtos = orders.stream()
                .map(order -> modelMapper.map(order,OrderDto.class))
                .toList();

        orderDtos.forEach(orderItem -> {
            orderItem.setUser(null);
            orderItem.getOrderItems().forEach(item-> item.getMenu().setReviews(null));
        });

        return Response.<List<OrderDto>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Orders for user retrieved successfully")
                .data(orderDtos)
                .build();
    }

    @Override
    public Response<OrderItemDto> getOrderItemById(Long orderItemId) {
        log.info("Inside getOrderItemById()");

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(()-> new NotFoundException("Order item not found"));

        OrderItemDto orderItemDto = modelMapper.map(orderItem,OrderItemDto.class);

        return Response.<OrderItemDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Order item retrieved success")
                .data(orderItemDto)
                .build();
    }

    @Override
    public Response<OrderDto> updateOrderStatus(OrderDto orderDto) {
        log.info("Inside updateOrderStatus()");

        Order order = orderRepository.findById(orderDto.getId())
                .orElseThrow(()-> new NotFoundException("Order not found"));

        OrderStatus orderStatus = orderDto.getOrderStatus();
        order.setOrderStatus(orderStatus);

        orderRepository.save(order);

        return Response.<OrderDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Order status update success")
                .build();
    }

    @Override
    public Response<Long> countUniqueCustomers() {
        log.info("Inside countUniqueCustomers()");

        Long uniqueCustomers = orderRepository.countDistinctUsers();

        return Response.<Long>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Unique customer count retrieved successfully")
                .data(uniqueCustomers)
                .build();
    }


    private void sendConfirmationEmail(User customer, OrderDto orderDto){
        String subject = "Your order confirmation - #" + orderDto.getId();

        //create a Thymeleaf context and set variables. import the context from Thymeleaf
        Context context = new Context(Locale.getDefault());

        context.setVariable("customerName", customer.getName());
        context.setVariable("orderId", String.valueOf(orderDto.getId()));
        context.setVariable("orderDate", orderDto.getOrderDate().toString());
        context.setVariable("totalAmount", orderDto.getTotalAmount().toString());

        // Format delivery address
        String deliveryAddress = orderDto.getUser().getAddress();
        context.setVariable("deliveryAddress", deliveryAddress);

        context.setVariable("currentYear", java.time.Year.now());

        // Build the order items HTML using StringBuilder
        StringBuilder orderItemsHtml = new StringBuilder();

        for (OrderItemDto item : orderDto.getOrderItems()) {
            orderItemsHtml.append("<div class=\"order-item\">")
                    .append("<p>").append(item.getMenu().getName()).append(" x ").append(item.getQuantity()).append("</p>")
                    .append("<p> $ ").append(item.getSubTotal()).append("</p>")
                    .append("</div>");
        }

        context.setVariable("orderItemsHtml", orderItemsHtml.toString());
        context.setVariable("totalItems", orderDto.getOrderItems().size());


        String paymentLink = basePaymentLink + orderDto.getId() + "&amount=" + orderDto.getTotalAmount(); // Replace "yourdomain.com"
        context.setVariable("paymentLink", paymentLink);

        // Process the Thymeleaf template to generate the HTML email body
        String emailBody = templateEngine.process("order-confirmation", context);  // "order-confirmation" is the template name

        notificationService.sendEmail(NotificationDto.builder()
                .recipient(customer.getEmail())
                .subject(subject)
                .body(emailBody)
                .isHtml(true)
                .build());

    }
}
