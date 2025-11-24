package com.second_project.book_store.model;

import java.math.BigDecimal;

/**
 * DTO for OrderItem entity.
 * Used for displaying order items in the UI.
 */
public class OrderItemDto {

    private Long orderItemId;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookImageUrl;
    private Integer quantity;
    private BigDecimal priceAtPurchase;
    private BigDecimal subtotal;

    public OrderItemDto() {}

    public OrderItemDto(Long orderItemId, Long bookId, String bookTitle, String bookAuthor,
                        String bookImageUrl, Integer quantity, BigDecimal priceAtPurchase) {
        this.orderItemId = orderItemId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.bookImageUrl = bookImageUrl;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
        this.subtotal = priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
    }

    // Getters and Setters
    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
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

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public String getBookImageUrl() {
        return bookImageUrl;
    }

    public void setBookImageUrl(String bookImageUrl) {
        this.bookImageUrl = bookImageUrl;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        if (priceAtPurchase != null) {
            this.subtotal = priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public BigDecimal getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public void setPriceAtPurchase(BigDecimal priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
        if (quantity != null) {
            this.subtotal = priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public BigDecimal getSubtotal() {
        if (subtotal == null && priceAtPurchase != null && quantity != null) {
            subtotal = priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
        }
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}

