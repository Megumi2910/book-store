package com.second_project.book_store.controller.page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.second_project.book_store.model.DashboardStatsDto;
import com.second_project.book_store.service.DashboardService;

/**
 * Controller for admin dashboard pages.
 * 
 * SECURITY: All methods require ADMIN role via @PreAuthorize
 * 
 * BEST PRACTICE:
 * - Use @PreAuthorize for method-level security
 * - Return view names that map to Thymeleaf templates
 * - Use Model to pass data to views
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")  // All endpoints require ADMIN role
public class AdminDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Admin dashboard home page.
     * Displays key metrics, charts, and recent activities.
     * 
     * @param model Spring Model
     * @return View name
     */
    @GetMapping({"", "/", "/dashboard"})
    public String showDashboard(Model model) {
        logger.info("Admin accessing dashboard");

        try {
            DashboardStatsDto stats = dashboardService.getDashboardStats();
            model.addAttribute("stats", stats);
            
            return "admin/dashboard/index";
        } catch (Exception e) {
            logger.error("Error loading dashboard", e);
            model.addAttribute("error", "Failed to load dashboard data");
            return "admin/dashboard/index";
        }
    }
}

