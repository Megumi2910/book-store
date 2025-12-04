package com.second_project.book_store.controller.page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.second_project.book_store.entity.User;
import com.second_project.book_store.entity.User.UserRole;
import com.second_project.book_store.service.UserService;

/**
 * Admin controller for managing users.
 * Provides list view and simple management actions like
 * enabling/disabling accounts and changing roles.
 */
@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * List users with optional keyword search.
     */
    @GetMapping({"", "/"})
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size,
            @RequestParam(required = false) String keyword,
            Model model) {

        logger.info("Admin listing users - page: {}, size: {}, keyword: {}", page, size, keyword);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage = userService.getUsers(pageable, keyword);

        model.addAttribute("userPage", userPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("roles", UserRole.values());

        model.addAttribute("pageTitle", "Users Management");

        return "admin/users/list";
    }

    /**
     * Toggle user enabled/disabled state.
     */
    @PostMapping("/{id}/toggle-enabled")
    public String toggleUserEnabled(
            @PathVariable("id") Long userId,
            RedirectAttributes redirectAttributes) {

        logger.info("Admin toggling enabled status for user {}", userId);

        try {
            userService.toggleUserEnabled(userId);
            redirectAttributes.addFlashAttribute("success", "User status updated successfully");
        } catch (Exception ex) {
            logger.warn("Failed to toggle user enabled status", ex);
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/users";
    }

    /**
     * Update user role (USER ↔ ADMIN).
     */
    @PostMapping("/{id}/role")
    public String updateUserRole(
            @PathVariable("id") Long userId,
            @RequestParam("role") UserRole role,
            RedirectAttributes redirectAttributes) {

        logger.info("Admin updating role for user {} to {}", userId, role);

        try {
            userService.updateUserRole(userId, role);
            redirectAttributes.addFlashAttribute("success", "User role updated successfully");
        } catch (Exception ex) {
            logger.warn("Failed to update user role", ex);
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/users";
    }
}


