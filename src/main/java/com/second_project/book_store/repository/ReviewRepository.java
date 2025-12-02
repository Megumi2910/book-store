package com.second_project.book_store.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.second_project.book_store.entity.Review;

/**
 * Repository interface for Review entity.
 * Provides CRUD operations and custom queries for review management.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Find reviews by book ID with pagination.
     * 
     * @param bookId Book ID
     * @param pageable Pagination parameters
     * @return Page of book's reviews
     */
    Page<Review> findByBook_BookId(Long bookId, Pageable pageable);

    /**
     * Find reviews by user ID with pagination.
     * 
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of user's reviews
     */
    Page<Review> findByUser_UserId(Long userId, Pageable pageable);

    /**
     * Find reviews by rating.
     * 
     * @param rating Rating value (1-5)
     * @param pageable Pagination parameters
     * @return Page of reviews with given rating
     */
    Page<Review> findByRating(Integer rating, Pageable pageable);

    /**
     * Get recent reviews (for dashboard).
     * 
     * @param pageable Pagination parameters
     * @return Page of recent reviews
     */
    @Query("SELECT r FROM Review r ORDER BY r.createdAt DESC")
    Page<Review> findRecentReviews(Pageable pageable);

    /**
     * Calculate average rating for a book.
     * 
     * @param bookId Book ID
     * @return Average rating (or 0 if no reviews)
     */
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.book.bookId = :bookId")
    Double getAverageRatingForBook(@Param("bookId") Long bookId);

    /**
     * Get overall average rating across all books.
     * 
     * @return Average rating
     */
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r")
    Double getOverallAverageRating();

    /**
     * Count reviews for a book.
     * 
     * @param bookId Book ID
     * @return Number of reviews
     */
    Long countByBook_BookId(Long bookId);

    /**
     * Count reviews by user ID.
     * 
     * @param userId User ID
     * @return Number of user's reviews
     */
    Long countByUser_UserId(Long userId);

    /**
     * Check if user has already reviewed a book.
     * 
     * @param userId User ID
     * @param bookId Book ID
     * @return true if review exists
     */
    boolean existsByUser_UserIdAndBook_BookId(Long userId, Long bookId);

    /**
     * Get rating distribution for a book (for star rating display).
     * Returns array of [rating, count].
     * 
     * @param bookId Book ID
     * @return List of rating counts
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.book.bookId = :bookId GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistributionForBook(@Param("bookId") Long bookId);
}

