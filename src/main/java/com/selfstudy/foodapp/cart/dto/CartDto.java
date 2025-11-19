package com.selfstudy.foodapp.cart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartDto {
    private Long id;
    private Long menuId;
    private List<CartItemDto> cartItems;
    private int quantity;
    private BigDecimal totalAmount;


}
