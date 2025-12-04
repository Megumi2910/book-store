package com.second_project.book_store.model;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for admin reports page.
 * Aggregates high-level sales statistics and top books.
 */
public class AdminReportDto {

    private LocalDate startDate;
    private LocalDate endDate;

    private double totalRevenue;
    private long totalOrders;
    private double averageOrderValue;

    private List<TopBook> topBooksByRevenue;
    private List<TopBook> topBooksByQuantity;

    public static class TopBook {
        private Long bookId;
        private String title;
        private long quantitySold;
        private double revenue;

        public TopBook() {
        }

        public TopBook(Long bookId, String title, long quantitySold, double revenue) {
            this.bookId = bookId;
            this.title = title;
            this.quantitySold = quantitySold;
            this.revenue = revenue;
        }

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

        public long getQuantitySold() {
            return quantitySold;
        }

        public void setQuantitySold(long quantitySold) {
            this.quantitySold = quantitySold;
        }

        public double getRevenue() {
            return revenue;
        }

        public void setRevenue(double revenue) {
            this.revenue = revenue;
        }
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public double getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(double averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public List<TopBook> getTopBooksByRevenue() {
        return topBooksByRevenue;
    }

    public void setTopBooksByRevenue(List<TopBook> topBooksByRevenue) {
        this.topBooksByRevenue = topBooksByRevenue;
    }

    public List<TopBook> getTopBooksByQuantity() {
        return topBooksByQuantity;
    }

    public void setTopBooksByQuantity(List<TopBook> topBooksByQuantity) {
        this.topBooksByQuantity = topBooksByQuantity;
    }
}



