package com.selfstudy.foodapp.review.controller;

import com.selfstudy.foodapp.response.Response;
import com.selfstudy.foodapp.review.dto.ReviewDto;
import com.selfstudy.foodapp.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<ReviewDto>> createReview(@RequestBody @Valid ReviewDto reviewDto){
        return ResponseEntity.ok(reviewService.createReview(reviewDto));
    }

    @GetMapping("/menu/{menuId}")
    public ResponseEntity<Response<List<ReviewDto>>> getReviewsForMenu(@PathVariable Long menuId){
        return ResponseEntity.ok(reviewService.getReviewsForMenu(menuId));
    }

    @GetMapping("/menu/rating/{menuId}")
    public ResponseEntity<Response<Double>>getAverageRatingForMenu(@PathVariable Long menuId){
        return ResponseEntity.ok(reviewService.getAverageRatingForMenu(menuId));
    }
}
