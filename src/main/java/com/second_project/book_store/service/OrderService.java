package com.second_project.book_store.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.second_project.book_store.entity.Order.OrderStatus;
import com.second_project.book_store.model.CheckoutRequestDto;
import com.second_project.book_store.model.OrderDto;

/**
 * Service interface for order management.
 * Handles order creation, retrieval, and status updates.
 */
public interface OrderService {

    /**
     * Create order from user's cart.
     * Converts cart items to order items and creates payment.
     * 
     * @param userId User ID
     * @param checkoutRequest Checkout information (shipping address, payment method)
     * @return Created OrderDto
     * @throws IllegalArgumentException if cart is empty, insufficient stock, or invalid payment method
     */
    OrderDto createOrderFromCart(Long userId, CheckoutRequestDto checkoutRequest);

    /**
     * Get order by ID.
     * 
     * @param orderId Order ID
     * @return OrderDto
     * @throws IllegalArgumentException if order not found
     */
    OrderDto getOrderById(Long orderId);

    /**
     * Get user's orders with pagination.
     * 
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of OrderDto
     */
    Page<OrderDto> getUserOrders(Long userId, Pageable pageable);

    /**
     * Get all orders with pagination (admin).
     * 
     * @param pageable Pagination parameters
     * @return Page of OrderDto
     */
    Page<OrderDto> getAllOrders(Pageable pageable);

    /**
     * Get orders by status with pagination (admin).
     * 
     * @param status Order status
     * @param pageable Pagination parameters
     * @return Page of OrderDto
     */
    Page<OrderDto> getOrdersByStatus(OrderStatus status, Pageable pageable);

    /**
     * Update order status (admin).
     * 
     * @param orderId Order ID
     * @param newStatus New order status
     * @return Updated OrderDto
     * @throws IllegalArgumentException if order not found or invalid status transition
     */
    OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus);

    /**
     * Cancel order (user or admin).
     * Only pending orders can be cancelled.
     * 
     * @param orderId Order ID
     * @param userId User ID (for authorization check)
     * @return Updated OrderDto
     * @throws IllegalArgumentException if order not found, not owned by user, or cannot be cancelled
     */
    OrderDto cancelOrder(Long orderId, Long userId);

    /**
     * Count total orders for a user.
     * 
     * @param userId User ID
     * @return Total order count
     */
    Long countUserOrders(Long userId);

    /**
     * Get order item for delivered order -> Fetch bookId and userId to determine if the user can post a review (user can only post a review for a delivered order)
     * @param bookId The bookId of the order item from a delivered order;
     * @param userId The userId of the delivered order
     * @return true if the condition above is satisfied
     */
    boolean verifyIfExistOrderItemForDeliveredOrder (Long bookId, Long userId);
}

