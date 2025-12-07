package com.second_project.book_store.controller.page;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

import com.second_project.book_store.model.BookDto;
import com.second_project.book_store.security.CustomUserDetails;
import com.second_project.book_store.service.BookService;
import com.second_project.book_store.service.CartService;

import jakarta.servlet.http.HttpSession;

/**
 * Thymeleaf controller for home page.
 * 
 * This is the default page users see after successful login.
 * URL: http://127.0.0.1:8080/
 */
@Controller
public class HomePageController {

    private final BookService bookService;
    private final CartService cartService;

    public HomePageController(BookService bookService, CartService cartService) {
        this.bookService = bookService;
        this.cartService = cartService;
    }

    /**
     * Display home page.
     * 
     * @param authentication Spring Security Authentication object (automatically injected if user is logged in)
     * @param model Model for Thymeleaf template
     * @param session HTTP session for rate limit tracking
     * @return Thymeleaf template name
     */
    @GetMapping("/")
    public String home(Authentication authentication, Model model, HttpSession session) {
        // Check if user is authenticated
        if (authentication != null && authentication.isAuthenticated()) {
            // Get user details from authentication
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                model.addAttribute("userName", userDetails.getFullName());
                model.addAttribute("userEmail", userDetails.getEmail());
                model.addAttribute("userRole", userDetails.getRole().name());
                model.addAttribute("isVerified", userDetails.isVerified());
                model.addAttribute("isAuthenticated", true);
                
                // Calculate remaining cooldown time for rate limiting
                LocalDateTime lastSentTime = (LocalDateTime) session.getAttribute("lastVerificationEmailSent");
                if (lastSentTime != null) {
                    long secondsSinceLastEmail = ChronoUnit.SECONDS.between(lastSentTime, LocalDateTime.now());
                    long secondsRemaining = Math.max(0, 60 - secondsSinceLastEmail);
                    model.addAttribute("emailCooldownSeconds", secondsRemaining);
                    model.addAttribute("emailSentBefore", true);
                } else {
                    model.addAttribute("emailCooldownSeconds", 0);
                    model.addAttribute("emailSentBefore", false);
                }
            }
        } else {
            model.addAttribute("isAuthenticated", false);
        }

        // Get recently added books for carousel (limited to 10 for better content display)
        // Responsive dots will be calculated dynamically based on screen size
        List<BookDto> recentlyAddedBooks = bookService.getRecentlyAddedBooks(10);
        model.addAttribute("recentlyAddedBooks", recentlyAddedBooks);

        // Get popular books (for 3-row grid - 9 books)
        List<BookDto> popularBooks = bookService.getPopularBooks(9);
        model.addAttribute("popularBooks", popularBooks);

        // Add cart item count for authenticated users
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Integer cartItemCount = cartService.getCartItemCount(userDetails.getUserId());
            model.addAttribute("cartItemCount", cartItemCount);
        }
        
        return "index";  // Thymeleaf template: src/main/resources/templates/index.html
    }
}

