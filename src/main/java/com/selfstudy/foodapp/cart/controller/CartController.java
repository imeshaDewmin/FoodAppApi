package com.selfstudy.foodapp.cart.controller;

import com.selfstudy.foodapp.cart.dto.CartDto;
import com.selfstudy.foodapp.cart.service.CartService;
import com.selfstudy.foodapp.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/items")
    ResponseEntity<Response<?>> addItemToCart(@RequestBody CartDto cartDto){
        return ResponseEntity.ok(cartService.addItemToCart(cartDto));
    }

    @PutMapping("/add/{menuId}")
    ResponseEntity<Response<?>> incrementItem(@PathVariable Long menuId){
        return ResponseEntity.ok(cartService.incrementItem(menuId));
    }

    @PutMapping("/decrement/{menuId}")
    ResponseEntity<Response<?>> decrementItem(@PathVariable Long menuId){
        return ResponseEntity.ok(cartService.decrementItem(menuId));
    }

    @DeleteMapping("/remove/{cartItemId}")
    ResponseEntity<Response<?>> removeCartItem(@PathVariable Long cartItemId){
        return ResponseEntity.ok(cartService.removeCartItem(cartItemId));
    }

    @GetMapping("/all")
    ResponseEntity<Response<CartDto>> getShoppingCart(){
        return ResponseEntity.ok(cartService.getShoppingCart());
    }

    @DeleteMapping("")
    ResponseEntity<Response<?>> clearShoppingCart(){
        return ResponseEntity.ok(cartService.clearShoppingCart());
    }


}
