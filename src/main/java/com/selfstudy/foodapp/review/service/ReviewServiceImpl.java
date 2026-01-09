package com.selfstudy.foodapp.review.service;

import com.selfstudy.foodapp.auth_users.entity.User;
import com.selfstudy.foodapp.auth_users.service.UserService;
import com.selfstudy.foodapp.enums.OrderStatus;
import com.selfstudy.foodapp.exceptions.BadRequestException;
import com.selfstudy.foodapp.exceptions.NotFoundException;
import com.selfstudy.foodapp.menu.entity.Menu;
import com.selfstudy.foodapp.menu.repository.MenuRepository;
import com.selfstudy.foodapp.order.entity.Order;
import com.selfstudy.foodapp.order.repository.OrderRepository;
import com.selfstudy.foodapp.response.Response;
import com.selfstudy.foodapp.review.dto.ReviewDto;
import com.selfstudy.foodapp.review.entity.Review;
import com.selfstudy.foodapp.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ModelMapper modelMapper;


    @Override
    @Transactional
    public Response<ReviewDto> createReview(ReviewDto reviewDto) {
        log.info("Inside createReview()");

        User user = userService.getCurrentLoggedInUser();

        if(reviewDto.getMenuId() == null || reviewDto.getOrderId() == null){
            throw new BadRequestException("Order id and menu id are required");
        }

        Menu menu = menuRepository.findById(reviewDto.getMenuId())
                .orElseThrow(()-> new NotFoundException("Menu not found"));

        Order order = orderRepository.findById(reviewDto.getOrderId())
                .orElseThrow(()-> new NotFoundException("Order not found"));

        if(!order.getUser().getId().equals(user.getId())){
            throw new BadRequestException("This order doesn't belong to you");
        }

        if(!order.getOrderStatus().equals(OrderStatus.DELIVERED)){
            throw new BadRequestException("You can't review orders that not delivered to you");
        }

        if (reviewRepository.existsByUserIdAndMenuIdAndOrderId(
                user.getId(),
                reviewDto.getMenuId(),
                reviewDto.getOrderId())) {
            throw new BadRequestException("You've already reviewed this item from this order");
        }

        Review review = Review.builder()
                .user(user)
                .menu(menu)
                .rating(reviewDto.getRating())
                .comment(reviewDto.getComment())
                .createdAt(LocalDateTime.now())
                .orderId(reviewDto.getOrderId())
                .build();

        reviewRepository.save(review);

        ReviewDto savedReview = modelMapper.map(review,ReviewDto.class);

        return Response.<ReviewDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Review created successfully")
                .data(savedReview)
                .build();
    }

    @Override
    public Response<List<ReviewDto>> getReviewsForMenu(Long menuId) {
        log.info("Inside getReviewsForMenu()");

        List<Review> reviews = reviewRepository.findByMenuIdOrderByIdDesc(menuId);

        List<ReviewDto> reviewDtos = reviews.stream()
                .map(review -> modelMapper.map(review,ReviewDto.class))
                .toList();

        return Response.<List<ReviewDto>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Get Reviews for menu Success")
                .data(reviewDtos)
                .build();
    }

    @Override
    public Response<Double> getAverageRatingForMenu(Long menuId) {
        log.info("Inside getAverageRatingForMenu()");

        Double averageRating = reviewRepository.calculateAverageRatingByMenuId(menuId);

        return Response.<Double>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Get average rating success")
                .data(averageRating != null ? averageRating : 0.0)
                .build();
    }
}
