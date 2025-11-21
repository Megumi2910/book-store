package com.second_project.book_store.exception.handler.page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.second_project.book_store.exception.ExpiredTokenException;
import com.second_project.book_store.exception.InvalidPasswordException;
import com.second_project.book_store.exception.RateLimitException;
import com.second_project.book_store.exception.ResetPasswordTokenNotFoundException;
import com.second_project.book_store.exception.UserAlreadyEnabledException;
import com.second_project.book_store.exception.UserAlreadyExistedException;
import com.second_project.book_store.exception.UserNotFoundException;
import com.second_project.book_store.exception.VerificationTokenNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Global exception handler for Thymeleaf page controllers.
 * 
 * KEY DIFFERENCES FROM ApiExceptionHandler:
 * - Uses @ControllerAdvice (not @RestControllerAdvice)
 * - Returns view names (String) instead of ResponseEntity
 * - Uses Model or RedirectAttributes for error messages
 * - Provides user-friendly error pages
 * 
 * BEST PRACTICES:
 * 1. Always provide user-friendly error messages
 * 2. Use RedirectAttributes for redirects (flash scope)
 * 3. Use Model for direct view rendering
 * 4. Log errors for debugging
 * 5. Don't expose sensitive information to users
 */
@ControllerAdvice(basePackages = "com.second_project.book_store.controller.page")
public class PageExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(PageExceptionHandler.class);

    /**
     * Handles expired token exceptions (verification or password reset)
     * Returns to appropriate page with error message
     */
    @ExceptionHandler(ExpiredTokenException.class)
    public String handleExpiredTokenException(ExpiredTokenException ex, 
                                              HttpServletRequest request,
                                              Model model) {
        model.addAttribute("error", "Token Expired");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", request.getRequestURI());
        return "error";
    }

    /**
     * Handles user not found exceptions
     * Typically occurs during login, password reset, or verification
     */
    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundException(UserNotFoundException ex,
                                              HttpServletRequest request,
                                              Model model) {
        model.addAttribute("error", "User Not Found");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", request.getRequestURI());
        return "error";
    }

    /**
     * Handles verification token not found exceptions
     * Occurs when token doesn't exist or was already used
     */
    @ExceptionHandler(VerificationTokenNotFoundException.class)
    public String handleVerificationTokenNotFoundException(VerificationTokenNotFoundException ex,
                                                           HttpServletRequest request,
                                                           Model model) {
        model.addAttribute("error", "Verification Token Not Found");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", request.getRequestURI());
        return "error";
    }

    /**
     * Handles user already enabled/verified exceptions
     * Occurs when trying to verify an already verified account
     */
    @ExceptionHandler(UserAlreadyEnabledException.class)
    public String handleUserAlreadyEnabledException(UserAlreadyEnabledException ex,
                                                    RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("info", ex.getMessage());
        return "redirect:/login";
    }

    /**
     * Handles rate limit exceptions
     * Occurs when user tries to send verification email too frequently
     * Shows error page with countdown
     */
    @ExceptionHandler(RateLimitException.class)
    public String handleRateLimitException(RateLimitException ex, Model model) {
        model.addAttribute("error", "Rate Limit Exceeded");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("secondsRemaining", ex.getSecondsRemaining());
        model.addAttribute("isRateLimitError", true);
        return "send-verify-email"; // Return to same page with error message
    }

    /**
     * Handles user already exists exceptions
     * Occurs during registration when email already exists
     */
    @ExceptionHandler(UserAlreadyExistedException.class)
    public String handleUserAlreadyExistedException(UserAlreadyExistedException ex,
                                                    RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/register";
    }

    /**
     * Handles reset password token not found exceptions
     * Occurs when reset token doesn't exist or was already used
     */
    @ExceptionHandler(ResetPasswordTokenNotFoundException.class)
    public String handleResetPasswordTokenNotFoundException(ResetPasswordTokenNotFoundException ex,
                                                            HttpServletRequest request,
                                                            Model model) {
        model.addAttribute("error", "Reset Token Not Found");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", request.getRequestURI());
        return "error";
    }

    /**
     * Handles invalid password exceptions
     * Occurs during password change when current password is wrong
     * or new password doesn't meet requirements
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public String handleInvalidPasswordException(InvalidPasswordException ex,
                                                 HttpServletRequest request,
                                                 Model model) {
        String path = request.getRequestURI();
        
        // If on reset-password page, show error on that page
        if (path.contains("/reset-password")) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("token", request.getParameter("token"));
            return "reset-password";
        }
        
        // Otherwise, show generic error page
        model.addAttribute("error", "Invalid Password");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", path);
        return "error";
    }

    /**
     * Handles validation errors from form submissions
     * Provides detailed field-level error messages
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public String handleValidationException(WebExchangeBindException ex,
                                           HttpServletRequest request,
                                           Model model) {
        model.addAttribute("error", "Validation Error");
        model.addAttribute("message", "Please check your input and try again.");
        model.addAttribute("validationErrors", ex.getBindingResult().getAllErrors());
        model.addAttribute("path", request.getRequestURI());
        return "error";
    }

    /**
     * Handles all other unexpected exceptions
     * Provides a generic error page without exposing sensitive details
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex,
                                        HttpServletRequest request,
                                        Model model) {
        // Log the full exception for debugging (don't show to user)
        logger.error("Unexpected error occurred at path: {} - Error: {}", 
                     request.getRequestURI(), ex.getMessage(), ex);
        
        model.addAttribute("error", "Something Went Wrong");
        model.addAttribute("message", "An unexpected error occurred. Please try again later.");
        model.addAttribute("path", request.getRequestURI());
        return "error";
    }
}
