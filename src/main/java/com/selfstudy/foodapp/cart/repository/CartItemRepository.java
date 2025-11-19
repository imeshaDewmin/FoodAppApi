package com.selfstudy.foodapp.cart.repository;

import com.selfstudy.foodapp.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem,Long> {
}
