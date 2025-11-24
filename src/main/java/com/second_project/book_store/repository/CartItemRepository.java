package com.second_project.book_store.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.second_project.book_store.entity.CartItem;

/**
 * Repository interface for CartItem entity.
 * Provides CRUD operations for cart item management.
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find cart item by cart ID and book ID.
     * Used to check if a book already exists in cart.
     * 
     * @param cartId Cart ID
     * @param bookId Book ID
     * @return Optional containing cart item if found
     */
    Optional<CartItem> findByCart_CartIdAndBook_BookId(Long cartId, Long bookId);
}

