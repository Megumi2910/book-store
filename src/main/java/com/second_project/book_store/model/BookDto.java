package com.second_project.book_store.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for Book entity with BookDetail information.
 * Used for admin book management (add/edit operations).
 * 
 * BEST PRACTICE: Separate DTO from entity to:
 * - Control what data is exposed
 * - Avoid circular references
 * - Add validation specific to the use case
 */
public class BookDto {

    private Long bookId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 255, message = "Author must not exceed 255 characters")
    private String author;

    @Pattern(regexp = "^(?:\\d{13})?$", message = "ISBN must be 13 digits (or empty)")
    private String isbn;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private String imageUrl;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be 0 or greater")
    private Integer quantity;

    @Size(max = 255, message = "Publisher must not exceed 255 characters")
    private String publisher;

    private LocalDateTime publishDate;

    @NotNull(message = "At least one genre must be selected")
    @Size(min = 1, message = "At least one genre must be selected")
    private Set<Long> genreIds;

    // Readonly fields (for edit form)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    // For display purposes
    private Double averageRating;
    private Long reviewCount;

    public BookDto() {}

    public BookDto(Long bookId, String title, String author, String isbn, String description, String imageUrl,
                   BigDecimal price, Integer quantity, String publisher, LocalDateTime publishDate,
                   Set<Long> genreIds, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
        this.publisher = publisher;
        this.publishDate = publishDate;
        this.genreIds = genreIds;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public LocalDateTime getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDateTime publishDate) {
        this.publishDate = publishDate;
    }

    public Set<Long> getGenreIds() {
        return genreIds;
    }

    public void setGenreIds(Set<Long> genreIds) {
        this.genreIds = genreIds;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Long reviewCount) {
        this.reviewCount = reviewCount;
    }

    @Override
    public String toString() {
        return "BookDto [bookId=" + bookId + ", title=" + title + ", author=" + author + ", isbn=" + isbn + ", price="
                + price + ", quantity=" + quantity + "]";
    }
}

