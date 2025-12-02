package com.second_project.book_store.service;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.second_project.book_store.model.ReviewDto;

/**
 * Service interface for Review management.
 * Provides business logic for review operations.
 */
public interface ReviewService {

    /**
     * Create a new review for a book.
     * 
     * @param reviewDto Review data
     * @param userId User ID
     * @return Created review DTO
     * @throws IllegalArgumentException if user already reviewed the book
     */
    ReviewDto createReview(ReviewDto reviewDto, Long userId);

    /**
     * Update an existing review.
     * Only the review owner can update.
     * 
     * @param reviewId Review ID
     * @param reviewDto Updated review data
     * @param userId User ID (for authorization)
     * @return Updated review DTO
     * @throws IllegalArgumentException if user is not the review owner
     */
    ReviewDto updateReview(Long reviewId, ReviewDto reviewDto, Long userId);

    /**
     * Get review by ID with like/dislike counts.
     * 
     * @param reviewId Review ID
     * @param currentUserId Current user ID (can be null for guests)
     * @return Review DTO
     */
    ReviewDto getReviewById(Long reviewId, Long currentUserId);

    /**
     * Get all reviews for a book with pagination.
     * Sorted by most liked by default.
     * 
     * @param bookId Book ID
     * @param pageable Pagination parameters
     * @param currentUserId Current user ID (can be null for guests)
     * @return Page of reviews
     */
    Page<ReviewDto> getReviewsByBookId(Long bookId, Pageable pageable, Long currentUserId);

    /**
     * Get reviews by user with pagination.
     * 
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of user's reviews
     */
    Page<ReviewDto> getReviewsByUserId(Long userId, Pageable pageable);

    /**
     * Get average rating for a book.
     * 
     * @param bookId Book ID
     * @return Average rating (0.0 if no reviews)
     */
    Double getAverageRating(Long bookId);

    /**
     * Get total review count for a book.
     * 
     * @param bookId Book ID
     * @return Total review count
     */
    Long getReviewCount(Long bookId);

    /**
     * Get rating distribution for a book.
     * Returns map with rating (1-5) as key and count as value.
     * 
     * @param bookId Book ID
     * @return Rating distribution map
     */
    Map<Integer, Long> getRatingDistribution(Long bookId);

    /**
     * Check if user has already reviewed a book.
     * 
     * @param userId User ID
     * @param bookId Book ID
     * @return true if user has reviewed the book
     */
    boolean hasUserReviewedBook(Long userId, Long bookId);

    /**
     * Get user's review for a specific book.
     * 
     * @param userId User ID
     * @param bookId Book ID
     * @return Review DTO or null if not found
     */
    ReviewDto getUserReviewForBook(Long userId, Long bookId);

    /**
     * Like a review.
     * If user already liked, removes the like.
     * If user disliked, changes to like.
     * 
     * @param reviewId Review ID
     * @param userId User ID
     * @return Updated like count
     */
    Integer likeReview(Long reviewId, Long userId);

    /**
     * Dislike a review.
     * If user already disliked, removes the dislike.
     * If user liked, changes to dislike.
     * 
     * @param reviewId Review ID
     * @param userId User ID
     * @return Updated dislike count
     */
    Integer dislikeReview(Long reviewId, Long userId);

    /**
     * Get like count for a review.
     * 
     * @param reviewId Review ID
     * @return Like count
     */
    Integer getLikeCount(Long reviewId);

    /**
     * Get dislike count for a review.
     * 
     * @param reviewId Review ID
     * @return Dislike count
     */
    Integer getDislikeCount(Long reviewId);

    /**
     * Count total reviews for a user.
     * 
     * @param userId User ID
     * @return Total review count
     */
    Long countUserReviews(Long userId);
}

