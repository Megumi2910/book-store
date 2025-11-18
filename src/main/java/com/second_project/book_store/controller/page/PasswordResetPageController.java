package com.second_project.book_store.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.second_project.book_store.model.ResetPasswordRequestDto;
import com.second_project.book_store.service.ResetPasswordTokenService;
import com.second_project.book_store.service.UserService;

import jakarta.validation.Valid;

/**
 * Thymeleaf controller for password reset page.
 * 
 * This controller handles the password reset flow using Thymeleaf templates:
 * 1. GET /reset-password?token=xxx → Display password reset form
 * 2. POST /reset-password?token=xxx → Process password reset
 * 
 * Flow:
 * - User clicks email link → GET /reset-password?token=xxx
 * - Thymeleaf displays form (reset-password.html)
 * - User submits form → POST /reset-password?token=xxx
 * - Password is reset → Redirect to login page
 */
@Controller
public class PasswordResetPageController {

    private final ResetPasswordTokenService resetPasswordTokenService;
    private final UserService userService;

    public PasswordResetPageController(ResetPasswordTokenService resetPasswordTokenService,
                                      UserService userService) {
        this.resetPasswordTokenService = resetPasswordTokenService;
        this.userService = userService;
    }

    /**
     * Display password reset form.
     * User clicks email link → This page displays form with token pre-filled.
     * 
     * URL: http://localhost:8080/reset-password?token=xxx
     * 
     * @param token Reset token from email link
     * @param model Model for Thymeleaf template
     * @return Thymeleaf template name
     */
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        try {
            // Validate token before showing form
            resetPasswordTokenService.verifyToken(token);
            
            // Token is valid - show form
            model.addAttribute("token", token);
            model.addAttribute("resetPasswordRequest", new ResetPasswordRequestDto());
            return "reset-password";  // Thymeleaf template: src/main/resources/templates/reset-password.html
        } catch (Exception e) {
            // Token is invalid/expired
            model.addAttribute("error", "Invalid or expired reset token. Please request a new password reset.");
            model.addAttribute("token", token);  // Keep token for retry
            return "reset-password";  // Show form with error message
        }
    }

    /**
     * Process password reset form submission.
     * User submits form → Password is reset.
     * 
     * @param token Reset token from form (query parameter)
     * @param request ResetPasswordRequestDto containing password and matchingPassword
     * @param bindingResult Holds validation errors
     * @param model Model for returning to form with errors
     * @param redirectAttributes For flash messages
     * @return Redirect to login page on success, back to form on error
     */
    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam String token,
            @Valid @ModelAttribute("resetPasswordRequest") ResetPasswordRequestDto request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // Check for validation errors (password length, matching passwords, etc.)
        if (bindingResult.hasErrors()) {
            // Return to form with validation errors displayed
            model.addAttribute("token", token);
            return "reset-password";
        }
        
        try {
            // Create reset request with token
            ResetPasswordRequestDto resetRequest = new ResetPasswordRequestDto(
                token,
                request.getPassword(),
                request.getMatchingPassword()
            );
            
            // Reset password (validates token inside)
            userService.resetPassword(resetRequest);
            
            // Success - redirect to login page with success message
            redirectAttributes.addFlashAttribute("success", 
                "Password has been reset successfully! Please login with your new password.");
            return "redirect:/login";
        } catch (Exception e) {
            // Error - redirect back to form with error message
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }
}

