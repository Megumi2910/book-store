package com.second_project.book_store.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Cart entity.
 * Used for displaying cart information in the UI.
 */
public class CartDto {

    private Long cartId;
    private List<CartItemDto> cartItems = new ArrayList<>();
    private BigDecimal totalAmount;
    private Integer totalItems;

    public CartDto() {
        this.totalAmount = BigDecimal.ZERO;
        this.totalItems = 0;
    }

    public CartDto(Long cartId, List<CartItemDto> cartItems) {
        this.cartId = cartId;
        this.cartItems = cartItems != null ? cartItems : new ArrayList<>();
        calculateTotals();
    }

    /**
     * Calculate total amount and total items from cart items.
     */
    public void calculateTotals() {
        if (cartItems == null || cartItems.isEmpty()) {
            this.totalAmount = BigDecimal.ZERO;
            this.totalItems = 0;
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        int items = 0;

        for (CartItemDto item : cartItems) {
            if (item.getSubtotal() != null) {
                total = total.add(item.getSubtotal());
            }
            if (item.getQuantity() != null) {
                items += item.getQuantity();
            }
        }

        this.totalAmount = total;
        this.totalItems = items;
    }

    // Getters and Setters
    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public List<CartItemDto> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItemDto> cartItems) {
        this.cartItems = cartItems;
        calculateTotals();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }
}

