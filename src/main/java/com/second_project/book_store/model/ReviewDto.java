package com.second_project.book_store.model;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for Review entity.
 * Used for transferring review data between layers.
 */
public class ReviewDto {

    private Long reviewId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    private String comment;

    private Long userId;
    private String userName; // Full name of the reviewer
    private String userEmail;

    private Long bookId;
    private String bookTitle;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Like/Dislike counts
    private Integer likeCount = 0;
    private Integer dislikeCount = 0;

    // Current user's interaction with this review
    private Boolean currentUserLiked = false;
    private Boolean currentUserDisliked = false;

    // Permission flags
    private Boolean canEdit = false; // Can current user edit this review

    public ReviewDto() {}

    public ReviewDto(Long reviewId, Integer rating, String comment, Long userId, String userName,
                     Long bookId, String bookTitle, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.reviewId = reviewId;
        this.rating = rating;
        this.comment = comment;
        this.userId = userId;
        this.userName = userName;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(Integer dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public Boolean getCurrentUserLiked() {
        return currentUserLiked;
    }

    public void setCurrentUserLiked(Boolean currentUserLiked) {
        this.currentUserLiked = currentUserLiked;
    }

    public Boolean getCurrentUserDisliked() {
        return currentUserDisliked;
    }

    public void setCurrentUserDisliked(Boolean currentUserDisliked) {
        this.currentUserDisliked = currentUserDisliked;
    }

    public Boolean getCanEdit() {
        return canEdit;
    }

    public void setCanEdit(Boolean canEdit) {
        this.canEdit = canEdit;
    }

    @Override
    public String toString() {
        return "ReviewDto [reviewId=" + reviewId + ", rating=" + rating + ", userName=" + userName
                + ", bookTitle=" + bookTitle + ", createdAt=" + createdAt + ", likeCount=" + likeCount
                + ", dislikeCount=" + dislikeCount + "]";
    }
}

