package com.second_project.book_store.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.second_project.book_store.entity.Cart;

/**
 * Repository interface for Cart entity.
 * Provides CRUD operations for cart management.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find cart by user ID.
     * Each user has exactly one cart (OneToOne relationship).
     * 
     * @param userId User ID
     * @return Optional containing cart if found
     */
    Optional<Cart> findByUser_UserId(Long userId);
}

