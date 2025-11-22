package com.second_project.book_store.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.second_project.book_store.entity.Book;
import com.second_project.book_store.entity.Order;
import com.second_project.book_store.entity.Order.OrderStatus;
import com.second_project.book_store.entity.Review;
import com.second_project.book_store.model.DashboardStatsDto;
import com.second_project.book_store.model.DashboardStatsDto.ChartDataPoint;
import com.second_project.book_store.model.DashboardStatsDto.LowStockBookDto;
import com.second_project.book_store.model.DashboardStatsDto.RecentOrderDto;
import com.second_project.book_store.model.DashboardStatsDto.RecentReviewDto;
import com.second_project.book_store.repository.BookRepository;
import com.second_project.book_store.repository.OrderRepository;
import com.second_project.book_store.repository.ReviewRepository;
import com.second_project.book_store.repository.UserRepository;
import com.second_project.book_store.service.DashboardService;

/**
 * Implementation of DashboardService.
 * 
 * BEST PRACTICE: Consider caching dashboard stats with Spring Cache
 * as this data doesn't need real-time accuracy and queries can be expensive.
 */
@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);
    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final int RECENT_ITEMS_LIMIT = 5;

    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public DashboardServiceImpl(BookRepository bookRepository,
                               OrderRepository orderRepository,
                               ReviewRepository reviewRepository,
                               UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    @Override
    public DashboardStatsDto getDashboardStats() {
        logger.info("Fetching dashboard statistics");

        DashboardStatsDto stats = new DashboardStatsDto();

        // Key Metrics
        stats.setTotalRevenue(orderRepository.calculateTotalRevenue());
        stats.setPendingOrdersCount(orderRepository.countByOrderStatus(OrderStatus.PENDING));
        stats.setTotalUsersCount(userRepository.count());
        stats.setVerifiedUsersCount(userRepository.countByIsEnabled(true));
        stats.setTotalBooksCount(bookRepository.countTotalBooks());
        stats.setAverageRating(reviewRepository.getOverallAverageRating());
        
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        stats.setNewOrdersToday(orderRepository.countOrdersToday(startOfDay));

        // Order Statistics by Status
        stats.setPendingOrders(orderRepository.countByOrderStatus(OrderStatus.PENDING));
        stats.setShippedOrders(orderRepository.countByOrderStatus(OrderStatus.SHIPPED));
        stats.setDeliveredOrders(orderRepository.countByOrderStatus(OrderStatus.DELIVERED));
        stats.setCancelledOrders(orderRepository.countByOrderStatus(OrderStatus.CANCELLED));

        // Low Stock Books
        List<Book> lowStockBooks = bookRepository.findLowStockBooks(LOW_STOCK_THRESHOLD);
        stats.setLowStockBooksCount((long) lowStockBooks.size());
        stats.setLowStockBooks(convertToLowStockBookDtos(lowStockBooks));

        // Recent Orders
        Page<Order> recentOrders = orderRepository.findRecentOrders(PageRequest.of(0, RECENT_ITEMS_LIMIT));
        stats.setRecentOrders(convertToRecentOrderDtos(recentOrders.getContent()));

        // Recent Reviews
        Page<Review> recentReviews = reviewRepository.findRecentReviews(PageRequest.of(0, RECENT_ITEMS_LIMIT));
        stats.setRecentReviews(convertToRecentReviewDtos(recentReviews.getContent()));

        // Chart Data - Last 7 days revenue
        stats.setRevenueChartData(getRevenueChartDataForDays(7));

        // Order Status Distribution (for pie chart)
        Map<String, Long> orderStatusDist = new HashMap<>();
        orderStatusDist.put("PENDING", stats.getPendingOrders());
        orderStatusDist.put("SHIPPED", stats.getShippedOrders());
        orderStatusDist.put("DELIVERED", stats.getDeliveredOrders());
        orderStatusDist.put("CANCELLED", stats.getCancelledOrders());
        stats.setOrderStatusDistribution(orderStatusDist);

        logger.info("Dashboard statistics fetched successfully");

        return stats;
    }

    @Override
    public DashboardStatsDto getRevenueChartData(int days) {
        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setRevenueChartData(getRevenueChartDataForDays(days));
        return stats;
    }

    /**
     * Get revenue data for last N days.
     */
    private List<ChartDataPoint> getRevenueChartDataForDays(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Object[]> dailyRevenue = orderRepository.getDailyRevenue(startDate);

        // Create map of date -> revenue
        Map<LocalDate, Double> revenueMap = dailyRevenue.stream()
            .collect(Collectors.toMap(
                row -> ((java.sql.Date) row[0]).toLocalDate(),
                row -> ((Number) row[1]).doubleValue()
            ));

        // Fill in missing dates with 0
        List<ChartDataPoint> chartData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Double revenue = revenueMap.getOrDefault(date, 0.0);
            chartData.add(new ChartDataPoint(date.format(formatter), revenue));
        }

        return chartData;
    }

    /**
     * Convert Book entities to LowStockBookDto.
     */
    private List<LowStockBookDto> convertToLowStockBookDtos(List<Book> books) {
        return books.stream()
            .map(book -> new LowStockBookDto(
                book.getBookId(),
                book.getTitle(),
                book.getBookDetail() != null ? book.getBookDetail().getQuantity() : 0
            ))
            .collect(Collectors.toList());
    }

    /**
     * Convert Order entities to RecentOrderDto.
     */
    private List<RecentOrderDto> convertToRecentOrderDtos(List<Order> orders) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        
        return orders.stream()
            .map(order -> {
                RecentOrderDto dto = new RecentOrderDto();
                dto.setOrderId(order.getOrderId());
                dto.setCustomerName(order.getUser().getFirstName() + " " + order.getUser().getLastName());
                dto.setCustomerEmail(order.getUser().getEmail());
                dto.setTotalAmount(order.getTotalAmount().doubleValue());
                dto.setOrderStatus(order.getOrderStatus().name());
                dto.setOrderDate(order.getOrderDate().format(formatter));
                return dto;
            })
            .collect(Collectors.toList());
    }

    /**
     * Convert Review entities to RecentReviewDto.
     */
    private List<RecentReviewDto> convertToRecentReviewDtos(List<Review> reviews) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        
        return reviews.stream()
            .map(review -> {
                RecentReviewDto dto = new RecentReviewDto();
                dto.setReviewId(review.getReviewId());
                dto.setBookTitle(review.getBook().getTitle());
                dto.setUserName(review.getUser().getFirstName() + " " + review.getUser().getLastName());
                dto.setRating(review.getRating());
                
                // Truncate comment if too long
                String comment = review.getComment();
                if (comment != null && comment.length() > 100) {
                    comment = comment.substring(0, 97) + "...";
                }
                dto.setComment(comment);
                dto.setCreatedAt(review.getCreatedAt().format(formatter));
                return dto;
            })
            .collect(Collectors.toList());
    }
}

