package com.second_project.book_store.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.second_project.book_store.entity.OrderItem;
import com.second_project.book_store.entity.Order.OrderStatus;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long>{

    /**
     * Verify if a user has a delivered order containing the book.
     */
    boolean existsByBook_BookIdAndOrder_User_UserIdAndOrder_OrderStatus(Long bookId,
                                                                        Long userId,
                                                                        OrderStatus status);

    /**
     * Aggregates top books by quantity sold within a date range (for reports).
     * Returns rows of [bookId, title, quantity, revenue].
     */
    @Query("SELECT oi.book.bookId, oi.book.title, SUM(oi.quantity), SUM(oi.priceAtPurchase * oi.quantity) " +
           "FROM OrderItem oi " +
           "WHERE oi.order.orderStatus = 'DELIVERED' " +
           "AND oi.order.orderDate >= :startDate AND oi.order.orderDate <= :endDate " +
           "GROUP BY oi.book.bookId, oi.book.title " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopBooksByQuantityInDateRange(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Aggregates top books by revenue within a date range (for reports).
     * Returns rows of [bookId, title, quantity, revenue].
     */
    @Query("SELECT oi.book.bookId, oi.book.title, SUM(oi.quantity), SUM(oi.priceAtPurchase * oi.quantity) " +
           "FROM OrderItem oi " +
           "WHERE oi.order.orderStatus = 'DELIVERED' " +
           "AND oi.order.orderDate >= :startDate AND oi.order.orderDate <= :endDate " +
           "GROUP BY oi.book.bookId, oi.book.title " +
           "ORDER BY SUM(oi.priceAtPurchase * oi.quantity) DESC")
    List<Object[]> findTopBooksByRevenueInDateRange(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

}
