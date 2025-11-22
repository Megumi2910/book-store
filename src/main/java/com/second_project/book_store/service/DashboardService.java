package com.second_project.book_store.service;

import com.second_project.book_store.model.DashboardStatsDto;

/**
 * Service interface for admin dashboard.
 * Provides aggregated statistics and metrics.
 */
public interface DashboardService {

    /**
     * Get comprehensive dashboard statistics.
     * Includes metrics, charts data, and recent activities.
     * 
     * @return Dashboard statistics DTO
     */
    DashboardStatsDto getDashboardStats();

    /**
     * Get revenue for last N days (for chart).
     * 
     * @param days Number of days
     * @return Dashboard stats with revenue chart data
     */
    DashboardStatsDto getRevenueChartData(int days);
}

