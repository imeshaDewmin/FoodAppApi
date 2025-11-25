package com.selfstudy.foodapp.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.selfstudy.foodapp.menu.dto.MenuDto;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderItemDto {

    private Long id;

    private Long menuId;

    private MenuDto menu;

    private int quantity;

    private BigDecimal pricePerUnit;

    private BigDecimal subTotal;
}
