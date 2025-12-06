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

import com.second_project.book_store.entity.Order.OrderStatus;
import com.second_project.book_store.model.OrderDto;
import com.second_project.book_store.service.OrderService;

/**
 * Controller for admin order management pages.
 * Requires ADMIN role.
 */
@Controller
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private static final Logger logger = LoggerFactory.getLogger(AdminOrderController.class);

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * List all orders with pagination and filtering.
     */
    @GetMapping({"", "/"})
    public String listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            Model model) {

        logger.info("Admin listing orders - page: {}, size: {}, status: {}", page, size, status);

        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        Page<OrderDto> orderPage;

        if (status != null && !status.trim().isEmpty()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orderPage = orderService.getOrdersByStatus(orderStatus, pageable);
                model.addAttribute("selectedStatus", status);
            } catch (IllegalArgumentException e) {
                orderPage = orderService.getAllOrders(pageable);
            }
        } else {
            orderPage = orderService.getAllOrders(pageable);
        }

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());
        model.addAttribute("statuses", OrderStatus.values());

        return "admin/orders/list";
    }

    /**
     * View order details.
     */
    @GetMapping("/{id}")
    public String viewOrderDetails(@PathVariable Long id, Model model) {
        logger.info("Admin viewing order details for ID: {}", id);

        try {
            OrderDto order = orderService.getOrderById(id);
            model.addAttribute("order", order);
            model.addAttribute("statuses", OrderStatus.values());
            return "admin/orders/details";
        } catch (IllegalArgumentException e) {
            logger.warn("Order not found: {}", id);
            return "redirect:/admin/orders?error=Order not found";
        }
    }

    /**
     * Update order status.
     */
    @PostMapping("/{id}/status")
    public String updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status,
            RedirectAttributes redirectAttributes) {

        logger.info("Admin updating order {} status to {}", id, status);

        try {
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("success", 
                "Order status updated to " + status);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update order status: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/orders/" + id;
    }
}

