package com.selfstudy.foodapp.order.service;

import com.selfstudy.foodapp.enums.OrderStatus;
import com.selfstudy.foodapp.order.dto.OrderDto;
import com.selfstudy.foodapp.order.dto.OrderItemDto;
import com.selfstudy.foodapp.response.Response;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {

    Response<?> placeOrderFromCart();
    Response<OrderDto> getOrderById(Long id);
    Response<Page<OrderDto>> getAllOrders(OrderStatus orderStatus, int page, int size);
    Response<List<OrderDto>> getOrdersOfUsers();
    Response<OrderItemDto>getOrderItemById(Long orderItemId);
    Response<OrderDto> updateOrderStatus(OrderDto orderDto);
    Response<Long> countUniqueCustomers();
}
