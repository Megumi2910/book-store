package com.second_project.book_store.controller.page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.second_project.book_store.model.ForgotPasswordRequestDto;
import com.second_project.book_store.service.UserService;

import jakarta.validation.Valid;

@Controller
public class ForgotPasswordPageController {

    private static final Logger log = LoggerFactory.getLogger(ForgotPasswordPageController.class);
    
    private final UserService userService;

    public ForgotPasswordPageController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequestDto());
        return "forgot-password";
    }
    
    @PostMapping("/forgot-password")
    public String processForgotPasswordRequest(
            @Valid @ModelAttribute("forgotPasswordRequest") ForgotPasswordRequestDto request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // Check for validation errors (email format, required field, etc.)
        if (bindingResult.hasErrors()) {
            // Return to form with validation errors displayed
            return "forgot-password";
        }
        
        try {
            // Email URL is configured via FrontendProperties (application.yml)
            userService.requestPasswordReset(request.getEmail());
            log.info("Password reset email sent successfully to: {}", request.getEmail());
        } catch (Exception e) {
            // For security: always show success message even if email doesn't exist
            // This prevents email enumeration attacks
            log.error("Failed to send password reset email to: {} - Error: {}", 
                     request.getEmail(), e.getMessage(), e);
        }
        
        redirectAttributes.addFlashAttribute("success", 
            "A password reset link has been sent. Please check your email.");

        return "redirect:/forgot-password";
    }
    
}
