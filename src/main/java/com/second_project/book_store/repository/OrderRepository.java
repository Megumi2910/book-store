package com.second_project.book_store.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.second_project.book_store.entity.Order;
import com.second_project.book_store.entity.Order.OrderStatus;

/**
 * Repository interface for Order entity.
 * Provides CRUD operations and custom queries for order management.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find orders by user ID with pagination.
     * 
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of user's orders
     */
    Page<Order> findByUser_UserId(Long userId, Pageable pageable);

    /**
     * Find orders by status with pagination.
     * 
     * @param status Order status
     * @param pageable Pagination parameters
     * @return Page of orders with given status
     */
    Page<Order> findByOrderStatus(OrderStatus status, Pageable pageable);

    /**
     * Find orders by date range.
     * 
     * @param startDate Start date
     * @param endDate End date
     * @param pageable Pagination parameters
     * @return Page of orders in date range
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate >= :startDate AND o.orderDate <= :endDate")
    Page<Order> findByOrderDateBetween(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate, 
                                        Pageable pageable);

    /**
     * Get recent orders (for dashboard).
     * 
     * @param pageable Pagination parameters (use PageRequest.of(0, 10) for top 10)
     * @return Page of recent orders
     */
    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC")
    Page<Order> findRecentOrders(Pageable pageable);

    /**
     * Count orders by status.
     * 
     * @param status Order status
     * @return Count of orders with given status
     */
    Long countByOrderStatus(OrderStatus status);

    /**
     * Count orders created today.
     * 
     * @param startOfDay Start of today
     * @return Count of today's orders
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :startOfDay")
    Long countOrdersToday(@Param("startOfDay") LocalDateTime startOfDay);

    /**
     * Calculate total revenue for all delivered orders.
     * 
     * @return Total revenue
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderStatus = 'DELIVERED'")
    Double calculateTotalRevenue();

    /**
     * Calculate revenue for a date range.
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return Revenue in date range
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
           "WHERE o.orderStatus = 'DELIVERED' " +
           "AND o.orderDate >= :startDate AND o.orderDate <= :endDate")
    Double calculateRevenueByDateRange(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Get order count by status (for pie chart).
     * Returns array of [status, count].
     * 
     * @return List of status counts
     */
    @Query("SELECT o.orderStatus, COUNT(o) FROM Order o GROUP BY o.orderStatus")
    List<Object[]> getOrderCountByStatus();

    /**
     * Get daily revenue for last N days (for line chart).
     * Returns array of [date, revenue].
     * 
     * @param startDate Start date
     * @return List of daily revenues
     */
    @Query("SELECT CAST(o.orderDate AS date), SUM(o.totalAmount) FROM Order o " +
           "WHERE o.orderStatus = 'DELIVERED' AND o.orderDate >= :startDate " +
           "GROUP BY CAST(o.orderDate AS date) ORDER BY CAST(o.orderDate AS date)")
    List<Object[]> getDailyRevenue(@Param("startDate") LocalDateTime startDate);
}

