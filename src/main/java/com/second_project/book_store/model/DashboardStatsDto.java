package com.second_project.book_store.model;

import java.util.List;
import java.util.Map;

/**
 * DTO for admin dashboard statistics.
 * Aggregates various metrics for dashboard display.
 * 
 * BEST PRACTICE: Use dedicated DTO for complex aggregations
 * instead of making multiple separate calls from the view.
 */
public class DashboardStatsDto {

    // Key Metrics
    private Double totalRevenue;
    private Long pendingOrdersCount;
    private Long totalUsersCount;
    private Long verifiedUsersCount;
    private Long totalBooksCount;
    private Long lowStockBooksCount;
    private Double averageRating;
    private Long newOrdersToday;

    // Order Statistics
    private Long pendingOrders;
    private Long shippedOrders;
    private Long deliveredOrders;
    private Long cancelledOrders;

    // Charts Data
    private List<ChartDataPoint> revenueChartData; // Last 7/30 days
    private Map<String, Long> orderStatusDistribution; // Pie chart

    // Recent Activities
    private List<RecentOrderDto> recentOrders;
    private List<RecentReviewDto> recentReviews;
    private List<LowStockBookDto> lowStockBooks;

    public DashboardStatsDto() {}

    // Getters and Setters
    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getPendingOrdersCount() {
        return pendingOrdersCount;
    }

    public void setPendingOrdersCount(Long pendingOrdersCount) {
        this.pendingOrdersCount = pendingOrdersCount;
    }

    public Long getTotalUsersCount() {
        return totalUsersCount;
    }

    public void setTotalUsersCount(Long totalUsersCount) {
        this.totalUsersCount = totalUsersCount;
    }

    public Long getVerifiedUsersCount() {
        return verifiedUsersCount;
    }

    public void setVerifiedUsersCount(Long verifiedUsersCount) {
        this.verifiedUsersCount = verifiedUsersCount;
    }

    public Long getTotalBooksCount() {
        return totalBooksCount;
    }

    public void setTotalBooksCount(Long totalBooksCount) {
        this.totalBooksCount = totalBooksCount;
    }

    public Long getLowStockBooksCount() {
        return lowStockBooksCount;
    }

    public void setLowStockBooksCount(Long lowStockBooksCount) {
        this.lowStockBooksCount = lowStockBooksCount;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getNewOrdersToday() {
        return newOrdersToday;
    }

    public void setNewOrdersToday(Long newOrdersToday) {
        this.newOrdersToday = newOrdersToday;
    }

    public Long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(Long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public Long getShippedOrders() {
        return shippedOrders;
    }

    public void setShippedOrders(Long shippedOrders) {
        this.shippedOrders = shippedOrders;
    }

    public Long getDeliveredOrders() {
        return deliveredOrders;
    }

    public void setDeliveredOrders(Long deliveredOrders) {
        this.deliveredOrders = deliveredOrders;
    }

    public Long getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(Long cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }

    public List<ChartDataPoint> getRevenueChartData() {
        return revenueChartData;
    }

    public void setRevenueChartData(List<ChartDataPoint> revenueChartData) {
        this.revenueChartData = revenueChartData;
    }

    public Map<String, Long> getOrderStatusDistribution() {
        return orderStatusDistribution;
    }

    public void setOrderStatusDistribution(Map<String, Long> orderStatusDistribution) {
        this.orderStatusDistribution = orderStatusDistribution;
    }

    public List<RecentOrderDto> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<RecentOrderDto> recentOrders) {
        this.recentOrders = recentOrders;
    }

    public List<RecentReviewDto> getRecentReviews() {
        return recentReviews;
    }

    public void setRecentReviews(List<RecentReviewDto> recentReviews) {
        this.recentReviews = recentReviews;
    }

    public List<LowStockBookDto> getLowStockBooks() {
        return lowStockBooks;
    }

    public void setLowStockBooks(List<LowStockBookDto> lowStockBooks) {
        this.lowStockBooks = lowStockBooks;
    }

    /**
     * Inner class for chart data points.
     */
    public static class ChartDataPoint {
        private String label; // Date or category
        private Double value;

        public ChartDataPoint() {}

        public ChartDataPoint(String label, Double value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }
    }

    /**
     * Inner class for recent orders (simplified view).
     */
    public static class RecentOrderDto {
        private Long orderId;
        private String customerName;
        private String customerEmail;
        private Double totalAmount;
        private String orderStatus;
        private String orderDate;

        public RecentOrderDto() {}

        // Getters and Setters
        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getCustomerEmail() {
            return customerEmail;
        }

        public void setCustomerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
        }

        public Double getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(Double totalAmount) {
            this.totalAmount = totalAmount;
        }

        public String getOrderStatus() {
            return orderStatus;
        }

        public void setOrderStatus(String orderStatus) {
            this.orderStatus = orderStatus;
        }

        public String getOrderDate() {
            return orderDate;
        }

        public void setOrderDate(String orderDate) {
            this.orderDate = orderDate;
        }
    }

    /**
     * Inner class for recent reviews (simplified view).
     */
    public static class RecentReviewDto {
        private Long reviewId;
        private String bookTitle;
        private String userName;
        private Integer rating;
        private String comment;
        private String createdAt;

        public RecentReviewDto() {}

        // Getters and Setters
        public Long getReviewId() {
            return reviewId;
        }

        public void setReviewId(Long reviewId) {
            this.reviewId = reviewId;
        }

        public String getBookTitle() {
            return bookTitle;
        }

        public void setBookTitle(String bookTitle) {
            this.bookTitle = bookTitle;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public Integer getRating() {
            return rating;
        }

        public void setRating(Integer rating) {
            this.rating = rating;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }

    /**
     * Inner class for low stock books.
     */
    public static class LowStockBookDto {
        private Long bookId;
        private String title;
        private Integer quantity;

        public LowStockBookDto() {}

        public LowStockBookDto(Long bookId, String title, Integer quantity) {
            this.bookId = bookId;
            this.title = title;
            this.quantity = quantity;
        }

        // Getters and Setters
        public Long getBookId() {
            return bookId;
        }

        public void setBookId(Long bookId) {
            this.bookId = bookId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}

