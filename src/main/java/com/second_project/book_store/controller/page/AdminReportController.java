package com.second_project.book_store.controller.page;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.second_project.book_store.model.AdminReportDto;
import com.second_project.book_store.service.BookService;
import com.second_project.book_store.service.ReportService;

/**
 * Admin controller for reports & analytics.
 */
@Controller
@RequestMapping("/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private static final Logger logger = LoggerFactory.getLogger(AdminReportController.class);

    private final ReportService reportService;
    private final BookService bookService;

    public AdminReportController(ReportService reportService, BookService bookService) {
        this.reportService = reportService;
        this.bookService = bookService;
    }

    @GetMapping({"", "/"})
    public String showReports(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(required = false) Long bookId,
            Model model) {

        LocalDate endDate;
        LocalDate startDate;

        // Parse end date safely
        try {
            endDate = (end != null && !end.isBlank()) ? LocalDate.parse(end) : LocalDate.now();
        } catch (DateTimeParseException ex) {
            logger.warn("Invalid end date '{}', falling back to today", end, ex);
            endDate = LocalDate.now();
            model.addAttribute("error", "Invalid end date format. Using today's date instead.");
        }

        // Parse start date safely (default: last 7 days)
        try {
            startDate = (start != null && !start.isBlank())
                    ? LocalDate.parse(start)
                    : endDate.minusDays(6);
        } catch (DateTimeParseException ex) {
            logger.warn("Invalid start date '{}', falling back to 7 days before end date", start, ex);
            startDate = endDate.minusDays(6);
            model.addAttribute("error", "Invalid start date format. Showing last 7 days instead.");
        }

        logger.info("Admin viewing reports from {} to {}, bookId: {}", startDate, endDate, bookId);

        AdminReportDto report = reportService.getReport(startDate, endDate, bookId);

        model.addAttribute("report", report);
        model.addAttribute("pageTitle", "Reports & Analytics");
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedBookId", bookId);

        // Add books for filter dropdown
        Pageable bookPageable = PageRequest.of(0, 1000, Sort.by("title").ascending());
        model.addAttribute("books", bookService.getAllBooks(bookPageable).getContent());

        return "admin/reports/index";
    }
}


