package com.selfstudy.foodapp.order.controller;

import com.selfstudy.foodapp.enums.OrderStatus;
import com.selfstudy.foodapp.order.dto.OrderDto;
import com.selfstudy.foodapp.order.dto.OrderItemDto;
import com.selfstudy.foodapp.order.service.OrderService;
import com.selfstudy.foodapp.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/order")
@RequiredArgsConstructor
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/checkout")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<Response<?>> placeOrderFromCart(){
        return ResponseEntity.ok(orderService.placeOrderFromCart());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<OrderDto>> getOrderById(@PathVariable Long id){
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<Page<OrderDto>>> getAllOrders(
            @RequestParam(required = false) OrderStatus orderStatus,
            @RequestParam(defaultValue ="0") int page,
            @RequestParam(defaultValue = "100") int size){
        return ResponseEntity.ok(orderService.getAllOrders(orderStatus,page,size));
    }

    @GetMapping("/userOrders")
    public ResponseEntity<Response<List<OrderDto>>> getOrdersOfUsers(){
        return ResponseEntity.ok(orderService.getOrdersOfUsers());
    }

    @GetMapping("orderItem/{orderItemId}")
    public ResponseEntity<Response<OrderItemDto>>getOrderItemById(@PathVariable Long orderItemId){
        return ResponseEntity.ok(orderService.getOrderItemById(orderItemId));
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<OrderDto>>updateOrderStatus(@RequestBody OrderDto orderDto){
        return ResponseEntity.ok(orderService.updateOrderStatus(orderDto));
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<Long>>countUniqueCustomers(){
       return ResponseEntity.ok(orderService.countUniqueCustomers());
    }

}
