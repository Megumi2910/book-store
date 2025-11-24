package com.second_project.book_store.controller.page;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.second_project.book_store.model.CartDto;
import com.second_project.book_store.security.CustomUserDetails;
import com.second_project.book_store.service.CartService;

/**
 * Controller for shopping cart pages.
 * Requires authentication.
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * View shopping cart.
     */
    @GetMapping
    public String viewCart(Authentication authentication, Model model) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        logger.debug("Viewing cart for user {}", userId);

        CartDto cart = cartService.getOrCreateCart(userId);
        model.addAttribute("cart", cart);

        return "cart/view";
    }

    /**
     * Add book to cart.
     * Requires authentication - redirects to login if not authenticated.
     */
    @PostMapping("/add")
    public String addToCart(
            @RequestParam Long bookId,
            @RequestParam(defaultValue = "1") Integer quantity,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        // Check authentication
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Please login to add items to cart");
            return "redirect:/login?redirect=/books/" + bookId;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        logger.info("Adding book {} to cart for user {}", bookId, userId);

        try {
            cartService.addToCart(userId, bookId, quantity);
            redirectAttributes.addFlashAttribute("success", 
                "Book added to cart successfully!");
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to add to cart: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/books/" + bookId;
    }

    /**
     * Update cart item quantity.
     */
    @PostMapping("/update/{cartItemId}")
    public String updateQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        logger.info("Updating cart item {} quantity to {} for user {}", cartItemId, quantity, userId);

        try {
            cartService.updateCartItemQuantity(userId, cartItemId, quantity);
            redirectAttributes.addFlashAttribute("success", "Cart updated successfully!");
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update cart: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cart";
    }

    /**
     * Remove item from cart.
     */
    @PostMapping("/remove/{cartItemId}")
    public String removeFromCart(
            @PathVariable Long cartItemId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        logger.info("Removing cart item {} for user {}", cartItemId, userId);

        try {
            cartService.removeFromCart(userId, cartItemId);
            redirectAttributes.addFlashAttribute("success", "Item removed from cart");
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to remove from cart: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cart";
    }

    /**
     * Buy Now - Add book to cart and redirect to checkout.
     * Requires authentication - redirects to login if not authenticated.
     */
    @PostMapping("/buy-now")
    public String buyNow(
            @RequestParam Long bookId,
            @RequestParam(defaultValue = "1") Integer quantity,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        // Check authentication
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Please login to purchase items");
            return "redirect:/login?redirect=/books/" + bookId;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        logger.info("Buy Now - Adding book {} to cart for user {} and redirecting to checkout", bookId, userId);

        try {
            cartService.addToCart(userId, bookId, quantity);
            return "redirect:/orders/checkout";
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to add to cart: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/books/" + bookId;
        }
    }

    /**
     * Remove multiple selected items from cart.
     */
    @PostMapping("/remove-selected")
    public String removeSelected(
            @RequestParam("cartItemIds") List<Long> cartItemIds,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        logger.info("Removing {} selected cart items for user {}", cartItemIds.size(), userId);

        try {
            int removedCount = 0;
            for (Long cartItemId : cartItemIds) {
                try {
                    cartService.removeFromCart(userId, cartItemId);
                    removedCount++;
                } catch (IllegalArgumentException e) {
                    logger.warn("Failed to remove cart item {}: {}", cartItemId, e.getMessage());
                }
            }
            
            if (removedCount > 0) {
                redirectAttributes.addFlashAttribute("success", 
                    removedCount + " item(s) removed from cart");
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Failed to remove selected items");
            }
        } catch (Exception e) {
            logger.error("Error removing selected items", e);
            redirectAttributes.addFlashAttribute("error", 
                "An error occurred while removing items");
        }

        return "redirect:/cart";
    }
}

