package com.second_project.book_store.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.second_project.book_store.entity.ReviewEvaluation;

/**
 * Repository interface for ReviewEvaluation entity.
 * Handles like/dislike operations on reviews.
 */
@Repository
public interface ReviewEvaluationRepository extends JpaRepository<ReviewEvaluation, Long> {

    /**
     * Find evaluation by user and review.
     * 
     * @param userId User ID
     * @param reviewId Review ID
     * @return Optional evaluation
     */
    Optional<ReviewEvaluation> findByUser_UserIdAndReview_ReviewId(Long userId, Long reviewId);

    /**
     * Check if user has evaluated a review.
     * 
     * @param userId User ID
     * @param reviewId Review ID
     * @return true if evaluation exists
     */
    boolean existsByUser_UserIdAndReview_ReviewId(Long userId, Long reviewId);

    /**
     * Count likes for a review.
     * 
     * @param reviewId Review ID
     * @return Number of likes
     */
    @Query("SELECT COUNT(re) FROM ReviewEvaluation re WHERE re.review.reviewId = :reviewId AND re.isLike = true")
    Long countLikesByReviewId(@Param("reviewId") Long reviewId);

    /**
     * Count dislikes for a review.
     * 
     * @param reviewId Review ID
     * @return Number of dislikes
     */
    @Query("SELECT COUNT(re) FROM ReviewEvaluation re WHERE re.review.reviewId = :reviewId AND re.isLike = false")
    Long countDislikesByReviewId(@Param("reviewId") Long reviewId);

    /**
     * Delete evaluation by user and review.
     * 
     * @param userId User ID
     * @param reviewId Review ID
     */
    void deleteByUser_UserIdAndReview_ReviewId(Long userId, Long reviewId);
}

