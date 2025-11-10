package com.second_project.book_store.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "review_id"}))
public class ReviewEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewEvaluationId;

    @Column(nullable = false)
    private boolean isLike;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK1_REVIEW_EVALUATION")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "review_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK2_REVIEW_EVALUATION")
    )
    private Review review;

    public ReviewEvaluation() {}

    public ReviewEvaluation(Long reviewEvaluationId, boolean isLike, LocalDateTime createdAt,
            LocalDateTime updatedAt, User user, Review review) {
        this.reviewEvaluationId = reviewEvaluationId;
        this.isLike = isLike;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.user = user;
        this.review = review;
    }

    public Long getReviewEvaluationId() {
        return reviewEvaluationId;
    }

    public void setReviewEvaluationId(Long reviewEvaluationId) {
        this.reviewEvaluationId = reviewEvaluationId;
    }

    public boolean isLike() {
        return isLike;
    }

    public void setLike(boolean isLike) {
        this.isLike = isLike;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }

    @Override
    public String toString() {
        return "ReviewEvaluation [reviewEvaluationId=" + reviewEvaluationId + ", isLike=" + isLike
                + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
    }
}
