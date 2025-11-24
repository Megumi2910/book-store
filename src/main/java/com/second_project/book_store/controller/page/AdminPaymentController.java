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

import com.second_project.book_store.entity.Payment.PaymentStatus;
import com.second_project.book_store.model.OrderDto;
import com.second_project.book_store.repository.PaymentRepository;
import com.second_project.book_store.service.OrderService;

/**
 * Controller for admin payment management pages.
 * Requires ADMIN role.
 */
@Controller
@RequestMapping("/admin/payments")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

    private static final Logger logger = LoggerFactory.getLogger(AdminPaymentController.class);
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;

    public AdminPaymentController(PaymentRepository paymentRepository, OrderService orderService) {
        this.paymentRepository = paymentRepository;
        this.orderService = orderService;
    }

    /**
     * List all payments with pagination and filtering.
     */
    @GetMapping({"", "/"})
    public String listPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            Model model) {

        logger.info("Admin listing payments - page: {}, size: {}, status: {}", page, size, status);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<com.second_project.book_store.entity.Payment> paymentPage;

        if (status != null && !status.trim().isEmpty()) {
            try {
                PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
                paymentPage = paymentRepository.findByPaymentStatus(paymentStatus, pageable);
                model.addAttribute("selectedStatus", status);
            } catch (IllegalArgumentException e) {
                paymentPage = paymentRepository.findAll(pageable);
            }
        } else {
            paymentPage = paymentRepository.findAll(pageable);
        }

        model.addAttribute("paymentPage", paymentPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paymentPage.getTotalPages());
        model.addAttribute("totalItems", paymentPage.getTotalElements());
        model.addAttribute("statuses", PaymentStatus.values());

        return "admin/payments/list";
    }

    /**
     * View payment details (via order).
     */
    @GetMapping("/{orderId}")
    public String viewPaymentDetails(@PathVariable Long orderId, Model model) {
        logger.info("Admin viewing payment details for order ID: {}", orderId);

        try {
            OrderDto order = orderService.getOrderById(orderId);
            model.addAttribute("order", order);
            return "admin/payments/details";
        } catch (IllegalArgumentException e) {
            logger.warn("Order not found: {}", orderId);
            return "redirect:/admin/payments?error=Order not found";
        }
    }

    /**
     * Mark payment as paid (for COD orders).
     */
    @PostMapping("/{orderId}/mark-paid")
    public String markPaymentAsPaid(
            @PathVariable Long orderId,
            RedirectAttributes redirectAttributes) {

        logger.info("Admin marking payment as paid for order {}", orderId);

        try {
            // Update order status to DELIVERED, which will automatically mark COD payment as PAID
            orderService.updateOrderStatus(orderId, com.second_project.book_store.entity.Order.OrderStatus.DELIVERED);
            redirectAttributes.addFlashAttribute("success", "Payment marked as paid");
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to mark payment as paid: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/payments/" + orderId;
    }
}

