package com.second_project.book_store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.second_project.book_store.entity.Payment;
import com.second_project.book_store.entity.Payment.PaymentStatus;

/**
 * Repository interface for Payment entity.
 * Provides CRUD operations and custom queries for payment management.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by order ID.
     * Each order has exactly one payment (OneToOne relationship).
     * 
     * @param orderId Order ID
     * @return Optional containing payment if found
     */
    @Query("SELECT p FROM Payment p WHERE p.order.orderId = :orderId")
    Optional<Payment> findByOrderId(@Param("orderId") Long orderId);

    /**
     * Find payments by status with pagination.
     * 
     * @param status Payment status
     * @param pageable Pagination parameters
     * @return Page of payments with given status
     */
    Page<Payment> findByPaymentStatus(PaymentStatus status, Pageable pageable);

    /**
     * Find payments by order ID (for order details).
     * 
     * @param orderId Order ID
     * @return List of payments (should be one, but using List for consistency)
     */
    @Query("SELECT p FROM Payment p WHERE p.order.orderId = :orderId")
    List<Payment> findAllByOrderId(@Param("orderId") Long orderId);

    /**
     * Count payments by status.
     * 
     * @param status Payment status
     * @return Count of payments with given status
     */
    Long countByPaymentStatus(PaymentStatus status);
}

