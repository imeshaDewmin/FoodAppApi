package com.selfstudy.foodapp.cart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartItemDto {

    private Long id;

    private MenuDto menu;

    private int quantity;

    private BigDecimal pricePerUnit;
    private BigDecimal subTotal;

}
