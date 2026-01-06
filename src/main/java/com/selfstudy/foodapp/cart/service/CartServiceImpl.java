package com.selfstudy.foodapp.cart.service;

import com.selfstudy.foodapp.auth_users.entity.User;
import com.selfstudy.foodapp.auth_users.service.UserService;
import com.selfstudy.foodapp.cart.dto.CartDto;
import com.selfstudy.foodapp.cart.dto.CartItemDto;
import com.selfstudy.foodapp.cart.entity.Cart;
import com.selfstudy.foodapp.cart.entity.CartItem;
import com.selfstudy.foodapp.cart.repository.CartItemRepository;
import com.selfstudy.foodapp.cart.repository.CartRepository;
import com.selfstudy.foodapp.exceptions.NotFoundException;
import com.selfstudy.foodapp.menu.entity.Menu;
import com.selfstudy.foodapp.menu.repository.MenuRepository;
import com.selfstudy.foodapp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Response<?> addItemToCart(CartDto cartDto) {
        log.info("Inside addItemToCart()");

        Long menuId = cartDto.getMenuId();

        int quantity = cartDto.getQuantity();

        User user = userService.getCurrentLoggedInUser();

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(()-> new NotFoundException("Menu item not found"));

        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseGet(()->{
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setCartItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });

        //check if the item is already in the cart

        Optional<CartItem> optionalCartItem = cart.getCartItems().stream()
                .filter(cartItem ->cartItem.getMenu().getId().equals(menuId))
                .findFirst();

        //if present, increment item

        if(optionalCartItem.isPresent()){
            CartItem cartItem = optionalCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setSubTotal(cartItem.getPricePerUnit().multiply(BigDecimal.valueOf(quantity)));
            cartItemRepository.save(cartItem);
        }else{
            //if not present, add it
            CartItem newCartItem = CartItem.builder()
                    .cart(cart)
                    .menu(menu)
                    .quantity(quantity)
                    .pricePerUnit(menu.getPrice())
                    .subTotal(menu.getPrice().multiply(BigDecimal.valueOf(quantity)))
                    .build();

            cart.getCartItems().add(newCartItem);
            cartItemRepository.save(newCartItem);
        }
        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Item added to cart successfully")
                .build();
    }

    @Override
    public Response<?> incrementItem(Long menuId) {
        log.info("Inside incrementItem()");

        User user = userService.getCurrentLoggedInUser();

        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(()-> new NotFoundException("Cart not found"));

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getMenu().getId().equals(menuId))
                .findFirst().orElseThrow(()-> new NotFoundException("Menu not found in the cart"));

        int newQuantity = cartItem.getQuantity() + 1;

        cartItem.setQuantity(newQuantity);
        cartItem.setSubTotal(cartItem.getPricePerUnit().multiply(BigDecimal.valueOf(newQuantity)));

        cartItemRepository.save(cartItem);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Cart item increment success")
                .build();
    }

    @Override
    public Response<?> decrementItem(Long menuId) {
        log.info("Inside decrementItem()");

        User user = userService.getCurrentLoggedInUser();

        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(()-> new NotFoundException("Cart not found"));

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getMenu().getId().equals(menuId))
                .findFirst().orElseThrow(()-> new NotFoundException("Menu not found in the cart"));

        int newQuantity = cartItem.getQuantity() - 1;

        if(newQuantity > 0) {
            cartItem.setQuantity(newQuantity);
            cartItem.setSubTotal(cartItem.getPricePerUnit().multiply(BigDecimal.valueOf(newQuantity)));
            cartItemRepository.save(cartItem);
        }else {
            cart.getCartItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        }

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Cart item decrement success")
                .build();
    }

    @Override
    public Response<?> removeCartItem(Long cartItemId) {
        log.info("Inside removeCartItem()");

        User user = userService.getCurrentLoggedInUser();

        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(()-> new NotFoundException("Cart not found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(()-> new NotFoundException("Cart item not found"));

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Cart item removed successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<CartDto> getShoppingCart() {
        log.info("Inside getShoppingCart()");

        User user = userService.getCurrentLoggedInUser();

        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(()-> new NotFoundException("Cart not found"));

        List<CartItem> cartItems = cart.getCartItems();

        CartDto cartDto = modelMapper.map(cart, CartDto.class);

        // Calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (cartItems != null) { // Add null check here
            for (CartItem item : cartItems) {
                totalAmount = totalAmount.add(item.getSubTotal());
            }
        }

        cartDto.setTotalAmount(totalAmount); //set the totalAmount

        //remove the review from the response
        if (cartDto.getCartItems() != null) {
            cartDto.getCartItems()
                    .forEach(item -> item.getMenu().setReviews(null));
        }

        return Response.<CartDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Shopping cart retrieved successfully")
                .data(cartDto)
                .build();

    }

    @Override
    public Response<?> clearShoppingCart() {
        log.info("Inside clearShoppingCart()");

        User user = userService.getCurrentLoggedInUser();

        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found for user"));

        //Delete cart items from the database first
        cartItemRepository.deleteAll(cart.getCartItems());

        //Clear the cart's item collection
        cart.getCartItems().clear();

        //update the database
        cartRepository.save(cart);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Shopping cart cleared successfully")
                .build();
    }
}
