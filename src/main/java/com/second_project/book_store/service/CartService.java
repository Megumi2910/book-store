package com.second_project.book_store.service;

import com.second_project.book_store.model.CartDto;

/**
 * Service interface for cart management.
 * Handles cart operations like adding items, updating quantities, etc.
 */
public interface CartService {

    /**
     * Get or create cart for user.
     * Each user has exactly one cart (created on first access).
     * 
     * @param userId User ID
     * @return CartDto containing cart information
     */
    CartDto getOrCreateCart(Long userId);

    /**
     * Add book to cart or update quantity if already exists.
     * 
     * @param userId User ID
     * @param bookId Book ID to add
     * @param quantity Quantity to add (defaults to 1)
     * @return Updated CartDto
     * @throws IllegalArgumentException if book not found or insufficient stock
     */
    CartDto addToCart(Long userId, Long bookId, Integer quantity);

    /**
     * Update cart item quantity.
     * 
     * @param userId User ID
     * @param cartItemId Cart item ID
     * @param quantity New quantity
     * @return Updated CartDto
     * @throws IllegalArgumentException if cart item not found or insufficient stock
     */
    CartDto updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity);

    /**
     * Remove item from cart.
     * 
     * @param userId User ID
     * @param cartItemId Cart item ID to remove
     * @return Updated CartDto
     * @throws IllegalArgumentException if cart item not found
     */
    CartDto removeFromCart(Long userId, Long cartItemId);

    /**
     * Clear all items from cart.
     * 
     * @param userId User ID
     */
    void clearCart(Long userId);

    /**
     * Get cart item count (for navbar badge).
     * 
     * @param userId User ID
     * @return Total number of items in cart
     */
    Integer getCartItemCount(Long userId);
}

