package com.second_project.book_store.controller.page;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.second_project.book_store.security.CustomUserDetails;

/**
 * Thymeleaf controller for home page.
 * 
 * This is the default page users see after successful login.
 * URL: http://127.0.0.1:8080/
 */
@Controller
public class HomePageController {

    /**
     * Display home page.
     * 
     * @param authentication Spring Security Authentication object (automatically injected if user is logged in)
     * @param model Model for Thymeleaf template
     * @return Thymeleaf template name
     */
    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        // Check if user is authenticated
        if (authentication != null && authentication.isAuthenticated()) {
            // Get user details from authentication
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                model.addAttribute("userEmail", userDetails.getEmail());
                model.addAttribute("userRole", userDetails.getRole().name());
                model.addAttribute("isAuthenticated", true);
            }
        } else {
            model.addAttribute("isAuthenticated", false);
        }
        
        return "index";  // Thymeleaf template: src/main/resources/templates/index.html
    }
}

