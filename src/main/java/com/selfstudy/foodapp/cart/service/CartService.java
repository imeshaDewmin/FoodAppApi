package com.selfstudy.foodapp.cart.service;

import com.selfstudy.foodapp.cart.dto.CartDto;
import com.selfstudy.foodapp.response.Response;

public interface CartService {

    Response<?> addItemToCart(CartDto cartDto);
    Response<?> incrementItem(Long menuId);
    Response<?> decrementItem(Long menuId);
    Response<?> removeCartItem(Long cartItemId);
    Response<CartDto> getShoppingCart();
    Response<?> clearShoppingCart();

}
