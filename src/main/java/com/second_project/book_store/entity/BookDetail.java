package com.second_project.book_store.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;

@Entity
public class BookDetail {

    @Id
    private Long bookDetailId;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String imageUrl;
    
    @Column(nullable = false)
    private Double price;
    
    @Column(nullable = false)
    private Long quantity;
    
    private String publisher;
    private LocalDateTime publishDate;

    @OneToOne //(fetch = FetchType.EAGER)
    @MapsId // maps PK of BookDetail to PK of Book
    @JoinColumn(
        name = "book_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK_BOOK_DETAIL")
    )
    private Book book;

    public BookDetail(){}

    public BookDetail(Long bookDetailId, String description, String imageUrl, double price, Long quantity,
            String publisher, LocalDateTime publishDate, Book book) {
        this.bookDetailId = bookDetailId;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
        this.publisher = publisher;
        this.publishDate = publishDate;
        this.book = book;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
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
                + publishDate + "]";
    }

    


}
