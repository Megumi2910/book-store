package com.second_project.book_store.controller.page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.second_project.book_store.model.CartDto;
import com.second_project.book_store.model.CheckoutRequestDto;
import com.second_project.book_store.model.OrderDto;
import com.second_project.book_store.security.CustomUserDetails;
import com.second_project.book_store.service.CartService;
import com.second_project.book_store.service.OrderService;
import com.second_project.book_store.service.UserService;

import jakarta.validation.Valid;

/**
 * Controller for order pages.
 * Requires authentication.
 */
@Controller
@RequestMapping("/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final OrderService orderService;
    private final CartService cartService;
    private final UserService userService;

    public OrderController(OrderService orderService, CartService cartService, UserService userService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.userService = userService;
    }

    /**
     * Show checkout page.
     */
    @GetMapping("/checkout")
    public String showCheckout(Authentication authentication, Model model) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        logger.debug("Showing checkout page for user {}", userId);

        // Get cart
        CartDto cart = cartService.getOrCreateCart(userId);
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            return "redirect:/cart?error=Cart is empty";
        }

        // Get user's address to pre-fill
        String userAddress = userService.findUserByEmail(userDetails.getEmail()).getAddress();
        CheckoutRequestDto checkoutRequest = new CheckoutRequestDto();
        if (userAddress != null && !userAddress.trim().isEmpty()) {
            checkoutRequest.setShippingAddress(userAddress);
        }

        model.addAttribute("cart", cart);
        model.addAttribute("checkoutRequest", checkoutRequest);
        model.addAttribute("userEmail", userDetails.getEmail());

        return "orders/checkout";
    }

    /**
     * Process checkout and create order.
     */
    @PostMapping("/checkout")
    public String processCheckout(
            @Valid @ModelAttribute("checkoutRequest") CheckoutRequestDto checkoutRequest,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        logger.info("Processing checkout for user {}", userId);

        if (bindingResult.hasErrors()) {
            CartDto cart = cartService.getOrCreateCart(userId);
            model.addAttribute("cart", cart);
            model.addAttribute("userEmail", userDetails.getEmail());
            return "orders/checkout";
        }

        try {
            OrderDto order = orderService.createOrderFromCart(userId, checkoutRequest);
            redirectAttributes.addFlashAttribute("success", 
                "Order placed successfully! Order ID: " + order.getOrderId());
            
            // If QR payment, redirect to order details to show QR code
            if ("QR".equalsIgnoreCase(checkoutRequest.getPaymentMethod())) {
                return "redirect:/orders/" + order.getOrderId() + "?showQR=true";
            }
            
            return "redirect:/orders/" + order.getOrderId();
        } catch (IllegalArgumentException e) {
            logger.warn("Checkout failed: {}", e.getMessage());
            CartDto cart = cartService.getOrCreateCart(userId);
            model.addAttribute("cart", cart);
            model.addAttribute("userEmail", userDetails.getEmail());
            model.addAttribute("error", e.getMessage());
            return "orders/checkout";
        }
    }

    /**
     * View order history.
     */
    @GetMapping
    public String viewOrderHistory(
            @RequestParam(defaultValue = "0") int page,
            Authentication authentication,
            Model model) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        logger.debug("Viewing order history for user {}", userId);

        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE, Sort.by("orderDate").descending());
        Page<OrderDto> orderPage = orderService.getUserOrders(userId, pageable);

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());

        return "orders/history";
    }

    /**
     * View order details.
     */
    @GetMapping("/{id}")
    public String viewOrderDetails(
            @PathVariable Long id,
            @RequestParam(required = false) Boolean showQR,
            Authentication authentication,
            Model model) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        logger.debug("Viewing order details for order {} by user {}", id, userId);

        try {
            OrderDto order = orderService.getOrderById(id);

            // Verify ownership
            if (!order.getUserId().equals(userId)) {
                model.addAttribute("error", "Order not found");
                return "error";
            }

            model.addAttribute("order", order);
            model.addAttribute("showQR", showQR != null && showQR);
            
            // Generate QR code URL if QR payment and pending
            if (order.getPaymentMethod() != null && 
                order.getPaymentMethod().name().equals("QR") && 
                order.getPaymentStatus() != null &&
                order.getPaymentStatus().name().equals("PENDING")) {
                // Convert total amount to integer (VND doesn't use decimals)
                long amount = order.getTotalAmount().longValue();
                String description = "Order #" + order.getOrderId();
                String qrCodeUrl = String.format(
                    "https://img.vietqr.io/image/MBBank-0774310907-compact2.jpg?amount=%d&addInfo=%s&accountName=Book Store",
                    amount, 
                    java.net.URLEncoder.encode(description, java.nio.charset.StandardCharsets.UTF_8)
                );
                model.addAttribute("qrCodeUrl", qrCodeUrl);
            }

            return "orders/details";
        } catch (IllegalArgumentException e) {
            logger.warn("Order not found: {}", id);
            model.addAttribute("error", "Order not found");
            return "error";
        }
    }

    /**
     * Cancel order.
     */
    @PostMapping("/{id}/cancel")
    public String cancelOrder(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        logger.info("Cancelling order {} by user {}", id, userId);

        try {
            orderService.cancelOrder(id, userId);
            redirectAttributes.addFlashAttribute("success", "Order cancelled successfully");
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to cancel order: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/orders/" + id;
    }
}

