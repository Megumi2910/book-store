package com.second_project.book_store.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.second_project.book_store.model.AdminReportDto;
import com.second_project.book_store.model.AdminReportDto.TopBook;
import com.second_project.book_store.repository.OrderItemRepository;
import com.second_project.book_store.repository.OrderRepository;
import com.second_project.book_store.service.ReportService;

/**
 * Implementation of ReportService using existing order data.
 */
@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public ReportServiceImpl(OrderRepository orderRepository,
                             OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public AdminReportDto getReport(LocalDate startDate, LocalDate endDate) {
        logger.info("Generating admin report from {} to {}", startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        AdminReportDto dto = new AdminReportDto();
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);

        // High-level order stats (DELIVERED orders within the selected range)
        Double revenue = orderRepository.calculateRevenueByDateRange(startDateTime, endDateTime);
        dto.setTotalRevenue(revenue != null ? revenue.doubleValue() : 0.0);

        long deliveredOrdersInRange = orderRepository.countDeliveredOrdersByDateRange(startDateTime, endDateTime);
        dto.setTotalOrders(deliveredOrdersInRange);

        dto.setAverageOrderValue(deliveredOrdersInRange > 0
                ? dto.getTotalRevenue() / deliveredOrdersInRange
                : 0.0);

        // Top books by quantity
        List<Object[]> topByQtyRows = orderItemRepository.findTopBooksByQuantityInDateRange(startDateTime, endDateTime);
        List<TopBook> topByQty = topByQtyRows.stream()
                .map(row -> new TopBook(
                        (Long) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).doubleValue()))
                .collect(Collectors.toList());
        dto.setTopBooksByQuantity(topByQty);

        // Top books by revenue
        List<Object[]> topByRevRows = orderItemRepository.findTopBooksByRevenueInDateRange(startDateTime, endDateTime);
        List<TopBook> topByRev = topByRevRows.stream()
                .map(row -> new TopBook(
                        (Long) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).doubleValue()))
                .collect(Collectors.toList());
        dto.setTopBooksByRevenue(topByRev);

        return dto;
    }
}


