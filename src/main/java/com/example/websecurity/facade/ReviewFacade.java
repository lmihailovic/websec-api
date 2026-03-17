package com.example.websecurity.facade;


import com.example.websecurity.api.dto.ReviewResponse;
import com.example.websecurity.api.dto.UpdateReviewRequest;
import com.example.websecurity.persistence.Review;
import com.example.websecurity.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewFacade {

    private final ReviewService reviewService;

    public ReviewResponse getReviewById(Long userId, long reviewId) {
        Review review = reviewService.getReviewByIdAndUserId(reviewId, userId); // izmena
        return ReviewResponse.builder()
                .id(review.getId())
                .movieTitle(review.getMovieTitle())
                .reviewText(review.getReviewText())
                .rating(review.getRating())
                .reviewDate(review.getCreated())
                .build();
    }

    public ReviewResponse updateReview(Long userId, Long reviewId, UpdateReviewRequest updateReviewRequest) {
        Review updatedReview = reviewService.updateReviewForUser(
            reviewId,
            userId,
            Review.builder()
                .reviewText(updateReviewRequest.getReviewText())
                .rating(updateReviewRequest.getRating())
                .build()
        );
        return ReviewResponse.builder()
                .id(updatedReview.getId())
                .movieTitle(updatedReview.getMovieTitle())
                .reviewText(updatedReview.getReviewText())
                .rating(updatedReview.getRating())
                .reviewDate(updatedReview.getCreated())
                .build();
    }

    public List<ReviewResponse> getReviewsForUser(Long userId) {
        List <ReviewResponse> reviewResponses = new ArrayList<>();
        List<Review> reviews = reviewService.getReviewsByUser(userId);
        for (Review review : reviews) {
            reviewResponses.add(
                    ReviewResponse.builder()
                            .id(review.getId())
                            .movieTitle(review.getMovieTitle())
                            .rating(review.getRating())
                            .build()
            );
        }
        return reviewResponses;
    }
}
