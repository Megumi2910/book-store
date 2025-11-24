package com.second_project.book_store.model;

import java.math.BigDecimal;

/**
 * DTO for CartItem entity.
 * Used for displaying cart items in the UI.
 */
public class CartItemDto {

    private Long cartItemId;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String bookImageUrl;
    private BigDecimal bookPrice;
    private Integer quantity;
    private Integer availableStock;
    private BigDecimal subtotal;

    public CartItemDto() {}

    public CartItemDto(Long cartItemId, Long bookId, String bookTitle, String bookAuthor, 
                      String bookImageUrl, BigDecimal bookPrice, Integer quantity, 
                      Integer availableStock) {
        this.cartItemId = cartItemId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.bookImageUrl = bookImageUrl;
        this.bookPrice = bookPrice;
        this.quantity = quantity;
        this.availableStock = availableStock;
        this.subtotal = bookPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // Getters and Setters
    public Long getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(Long cartItemId) {
        this.cartItemId = cartItemId;
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

    public BigDecimal getBookPrice() {
        return bookPrice;
    }

    public void setBookPrice(BigDecimal bookPrice) {
        this.bookPrice = bookPrice;
        // Recalculate subtotal when price changes
        if (quantity != null) {
            this.subtotal = bookPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        // Recalculate subtotal when quantity changes
        if (bookPrice != null) {
            this.subtotal = bookPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }

    public BigDecimal getSubtotal() {
        if (subtotal == null && bookPrice != null && quantity != null) {
            subtotal = bookPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}

