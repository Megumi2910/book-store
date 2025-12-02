package com.second_project.book_store.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.second_project.book_store.entity.Book;
import com.second_project.book_store.entity.Review;
import com.second_project.book_store.entity.ReviewEvaluation;
import com.second_project.book_store.entity.User;
import com.second_project.book_store.model.ReviewDto;
import com.second_project.book_store.repository.BookRepository;
import com.second_project.book_store.repository.ReviewEvaluationRepository;
import com.second_project.book_store.repository.ReviewRepository;
import com.second_project.book_store.repository.UserRepository;
import com.second_project.book_store.service.ReviewService;

/**
 * Implementation of ReviewService.
 * Handles all review-related business logic.
 */
@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository reviewRepository;
    private final ReviewEvaluationRepository reviewEvaluationRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             ReviewEvaluationRepository reviewEvaluationRepository,
                             UserRepository userRepository,
                             BookRepository bookRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewEvaluationRepository = reviewEvaluationRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public ReviewDto createReview(ReviewDto reviewDto, Long userId) {
        logger.debug("Creating review for book {} by user {}", reviewDto.getBookId(), userId);

        // Check if user already reviewed this book
        if (reviewRepository.existsByUser_UserIdAndBook_BookId(userId, reviewDto.getBookId())) {
            throw new IllegalArgumentException("You have already reviewed this book");
        }

        // Get user and book
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Book book = bookRepository.findById(reviewDto.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));

        // Create review entity
        Review review = new Review();
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());
        review.setUser(user);
        review.setBook(book);

        // Save review
        review = reviewRepository.save(review);

        logger.info("Review created successfully: {}", review.getReviewId());

        return convertToDto(review, userId);
    }

    @Override
    public ReviewDto updateReview(Long reviewId, ReviewDto reviewDto, Long userId) {
        logger.debug("Updating review {} by user {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        // Check if user owns this review
        if (!review.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("You are not authorized to edit this review");
        }

        // Update review
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());

        review = reviewRepository.save(review);

        logger.info("Review updated successfully: {}", reviewId);

        return convertToDto(review, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDto getReviewById(Long reviewId, Long currentUserId) {
        logger.debug("Getting review by ID: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        return convertToDto(review, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDto> getReviewsByBookId(Long bookId, Pageable pageable, Long currentUserId) {
        logger.debug("Getting reviews for book {}, page {}", bookId, pageable.getPageNumber());

        Page<Review> reviewPage = reviewRepository.findByBook_BookId(bookId, pageable);

        // Convert to DTOs and sort by most liked
        List<ReviewDto> reviewDtos = reviewPage.getContent().stream()
                .map(review -> convertToDto(review, currentUserId))
                .sorted((r1, r2) -> {
                    // Sort by net likes (likes - dislikes) descending
                    int netLikes1 = r1.getLikeCount() - r1.getDislikeCount();
                    int netLikes2 = r2.getLikeCount() - r2.getDislikeCount();
                    if (netLikes1 != netLikes2) {
                        return Integer.compare(netLikes2, netLikes1);
                    }
                    // If same net likes, sort by creation date (newest first)
                    return r2.getCreatedAt().compareTo(r1.getCreatedAt());
                })
                .toList();

        return new PageImpl<>(reviewDtos, pageable, reviewPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDto> getReviewsByUserId(Long userId, Pageable pageable) {
        logger.debug("Getting reviews by user {}", userId);

        Page<Review> reviewPage = reviewRepository.findByUser_UserId(userId, pageable);

        List<ReviewDto> reviewDtos = reviewPage.getContent().stream()
                .map(review -> convertToDto(review, userId))
                .toList();

        return new PageImpl<>(reviewDtos, pageable, reviewPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(Long bookId) {
        logger.debug("Getting average rating for book {}", bookId);
        return reviewRepository.getAverageRatingForBook(bookId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getReviewCount(Long bookId) {
        logger.debug("Getting review count for book {}", bookId);
        return reviewRepository.countByBook_BookId(bookId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, Long> getRatingDistribution(Long bookId) {
        logger.debug("Getting rating distribution for book {}", bookId);

        List<Object[]> distribution = reviewRepository.getRatingDistributionForBook(bookId);

        // Initialize map with all ratings (1-5) set to 0
        Map<Integer, Long> ratingMap = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingMap.put(i, 0L);
        }

        // Fill in actual counts
        for (Object[] row : distribution) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            ratingMap.put(rating, count);
        }

        return ratingMap;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewedBook(Long userId, Long bookId) {
        return reviewRepository.existsByUser_UserIdAndBook_BookId(userId, bookId);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDto getUserReviewForBook(Long userId, Long bookId) {
        logger.debug("Getting review by user {} for book {}", userId, bookId);

        // Find review by user and book
        Optional<Review> reviewOpt = reviewRepository.findByBook_BookId(bookId, Pageable.unpaged())
                .stream()
                .filter(review -> review.getUser().getUserId().equals(userId))
                .findFirst();

        return reviewOpt.map(review -> convertToDto(review, userId)).orElse(null);
    }

    @Override
    public Integer likeReview(Long reviewId, Long userId) {
        logger.debug("User {} liking review {}", userId, reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<ReviewEvaluation> existingEval = reviewEvaluationRepository
                .findByUser_UserIdAndReview_ReviewId(userId, reviewId);

        if (existingEval.isPresent()) {
            ReviewEvaluation evaluation = existingEval.get();
            if (evaluation.isLike()) {
                // User already liked, remove the like
                reviewEvaluationRepository.delete(evaluation);
                logger.info("Removed like from review {}", reviewId);
            } else {
                // User disliked, change to like
                evaluation.setLike(true);
                reviewEvaluationRepository.save(evaluation);
                logger.info("Changed dislike to like for review {}", reviewId);
            }
        } else {
            // Create new like
            ReviewEvaluation evaluation = new ReviewEvaluation();
            evaluation.setUser(user);
            evaluation.setReview(review);
            evaluation.setLike(true);
            evaluation.setCreatedAt(java.time.LocalDateTime.now());
            evaluation.setUpdatedAt(java.time.LocalDateTime.now());
            reviewEvaluationRepository.save(evaluation);
            logger.info("Added like to review {}", reviewId);
        }

        return getLikeCount(reviewId);
    }

    @Override
    public Integer dislikeReview(Long reviewId, Long userId) {
        logger.debug("User {} disliking review {}", userId, reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<ReviewEvaluation> existingEval = reviewEvaluationRepository
                .findByUser_UserIdAndReview_ReviewId(userId, reviewId);

        if (existingEval.isPresent()) {
            ReviewEvaluation evaluation = existingEval.get();
            if (!evaluation.isLike()) {
                // User already disliked, remove the dislike
                reviewEvaluationRepository.delete(evaluation);
                logger.info("Removed dislike from review {}", reviewId);
            } else {
                // User liked, change to dislike
                evaluation.setLike(false);
                reviewEvaluationRepository.save(evaluation);
                logger.info("Changed like to dislike for review {}", reviewId);
            }
        } else {
            // Create new dislike
            ReviewEvaluation evaluation = new ReviewEvaluation();
            evaluation.setUser(user);
            evaluation.setReview(review);
            evaluation.setLike(false);
            evaluation.setCreatedAt(java.time.LocalDateTime.now());
            evaluation.setUpdatedAt(java.time.LocalDateTime.now());
            reviewEvaluationRepository.save(evaluation);
            logger.info("Added dislike to review {}", reviewId);
        }

        return getDislikeCount(reviewId);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getLikeCount(Long reviewId) {
        Long count = reviewEvaluationRepository.countLikesByReviewId(reviewId);
        return count != null ? count.intValue() : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getDislikeCount(Long reviewId) {
        Long count = reviewEvaluationRepository.countDislikesByReviewId(reviewId);
        return count != null ? count.intValue() : 0;
    }

    /**
     * Convert Review entity to ReviewDto.
     * 
     * @param review Review entity
     * @param currentUserId Current user ID (can be null)
     * @return ReviewDto
     */
    private ReviewDto convertToDto(Review review, Long currentUserId) {
        ReviewDto dto = new ReviewDto();
        dto.setReviewId(review.getReviewId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setUserId(review.getUser().getUserId());
        dto.setUserName(review.getUser().getFirstName() + " " + review.getUser().getLastName());
        dto.setUserEmail(review.getUser().getEmail());
        dto.setUserRole(review.getUser().getRole());
        dto.setBookId(review.getBook().getBookId());
        dto.setBookTitle(review.getBook().getTitle());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());

        // Set like/dislike counts
        dto.setLikeCount(getLikeCount(review.getReviewId()));
        dto.setDislikeCount(getDislikeCount(review.getReviewId()));

        // Set current user's interaction
        if (currentUserId != null) {
            Optional<ReviewEvaluation> evaluation = reviewEvaluationRepository
                    .findByUser_UserIdAndReview_ReviewId(currentUserId, review.getReviewId());
            if (evaluation.isPresent()) {
                if (evaluation.get().isLike()) {
                    dto.setCurrentUserLiked(true);
                } else {
                    dto.setCurrentUserDisliked(true);
                }
            }

            // Check if current user can edit this review
            dto.setCanEdit(review.getUser().getUserId().equals(currentUserId));
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUserReviews(Long userId) {
        return reviewRepository.countByUser_UserId(userId);
    }
}

