package com.selfstudy.foodapp.review.service;

import com.selfstudy.foodapp.response.Response;
import com.selfstudy.foodapp.review.dto.ReviewDto;

import java.util.List;

public interface ReviewService {

    Response<ReviewDto> createReview(ReviewDto reviewDto);
    Response<List<ReviewDto>> getReviewsForMenu(Long menuId);
    Response<Double>getAverageRatingForMenu(Long menuId);


}
