package com.second_project.book_store.service;

import java.time.LocalDate;

import com.second_project.book_store.model.AdminReportDto;

/**
 * Service for generating admin reports over a date range.
 */
public interface ReportService {

    /**
     * Generates a summary report for the given date range (inclusive).
     *
     * @param startDate Start date (inclusive)
     * @param endDate   End date (inclusive)
     * @param bookId    Optional book ID filter, can be null
     * @return AdminReportDto containing aggregated statistics
     */
    AdminReportDto getReport(LocalDate startDate, LocalDate endDate, Long bookId);
}



