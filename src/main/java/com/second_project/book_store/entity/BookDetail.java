package com.second_project.book_store.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
public class BookDetail {

    @Id
    private Long bookDetailId;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String imageUrl;
    
    @Column(
        nullable = false,
        precision = 10,
        scale = 2
        )
    private BigDecimal price;
    
    @Column(nullable = false)
    @Min(0)
    private Integer quantity; // Changed from Long to Integer for consistency
    
    private String publisher;
    private LocalDate publishDate;

    @Column(nullable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @NotNull
    private LocalDateTime updatedAt;

    @OneToOne //(fetch = FetchType.EAGER)
    @MapsId // maps PK of BookDetail to PK of Book
    @JoinColumn(
        name = "book_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_BOOK_DETAIL")
    )
    @NotNull
    private Book book;

    public BookDetail(){}

    public BookDetail(Long bookDetailId, String description, String imageUrl, BigDecimal price, Integer quantity,
            String publisher, LocalDate publishDate, LocalDateTime createdAt, LocalDateTime updatedAt, Book book) {
        this.bookDetailId = bookDetailId;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
        this.publisher = publisher;
        this.publishDate = publishDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.book = book;
    }

    // Audit annotations
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getBookDetailId() {
        return bookDetailId;
    }

    public void setBookDetailId(Long bookDetailId) {
        this.bookDetailId = bookDetailId;
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

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
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

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    @Override
    public String toString() {
        return "BookDetail [bookDetailId=" + bookDetailId + ", description=" + description + ", imageUrl=" + imageUrl
                + ", price=" + price + ", quantity=" + quantity + ", publisher=" + publisher + ", publishDate="
                + publishDate + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
    }

    


}
