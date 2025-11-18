package com.second_project.book_store.controller.api;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.second_project.book_store.model.ChangePasswordRequestDto;
import com.second_project.book_store.model.ForgotPasswordRequestDto;
import com.second_project.book_store.model.ResetPasswordRequestDto;
import com.second_project.book_store.model.UserDto;
import com.second_project.book_store.service.UserService;
import com.second_project.book_store.service.VerificationTokenService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;

import com.second_project.book_store.security.CustomUserDetails;


@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final VerificationTokenService verificationTokenService;

    public UserController(UserService userService, 
                         VerificationTokenService verificationTokenService){
        this.userService = userService;
        this.verificationTokenService = verificationTokenService;
    }

    /**
     * Extracts userId from Spring Security Authentication object.
     * 
     * BEST PRACTICE: Store userId in Authentication principal to avoid database lookups.
     * 
     * How it works:
     * 1. User logs in → CustomUserDetailsService.loadUserByUsername() is called
     * 2. Returns CustomUserDetails containing only essential fields (userId, email, role, enabled)
     * 3. Spring Security stores CustomUserDetails in Authentication.principal
     * 4. We can access userId directly without database lookup!
     * 
     * Benefits:
     * - No database lookup needed (better performance)
     * - No risk of lazy loading exceptions (we don't store entire User entity)
     * - Cleaner code
     * 
     * @param authentication Spring Security Authentication object (automatically injected)
     * @return User ID of the authenticated user (never null)
     * @throws IllegalStateException if authentication is null or principal is not CustomUserDetails
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        // Null safety check
        if (authentication == null) {
            throw new IllegalStateException("Authentication cannot be null. User must be authenticated.");
        }
        
        // Get the principal (UserDetails) from Authentication
        Object principal = authentication.getPrincipal();
        
        // Null safety check
        if (principal == null) {
            throw new IllegalStateException("Authentication principal cannot be null. " +
                    "Make sure CustomUserDetailsService is configured correctly.");
        }
        
        // Check if it's our CustomUserDetails
        if (principal instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principal;
            Long userId = userDetails.getUserId();
            
            // Additional null safety check (shouldn't happen, but defensive programming)
            if (userId == null) {
                throw new IllegalStateException("User ID cannot be null in CustomUserDetails. " +
                        "This indicates a configuration error.");
            }
            
            return userId; // ✅ Direct access - NO DATABASE LOOKUP!
        }
        
        // Fallback: If principal is not CustomUserDetails (shouldn't happen, but safety check)
        // This handles edge cases or if someone uses default Spring Security UserDetails
        throw new IllegalStateException("Authentication principal is not CustomUserDetails. " +
                "Expected: CustomUserDetails, but got: " + principal.getClass().getName() + ". " +
                "Make sure CustomUserDetailsService is configured correctly.");
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@Valid @RequestBody UserDto userDto) {
        // Register user - event will handle token creation and email
        // Email URL is configured via FrontendProperties (application.yml)
        userService.registerUser(userDto);

        return ResponseEntity.ok(Map.of("message", "User registered successfully! Please check your email for verification link."));
    }
    
    @GetMapping("/verify-registration")
    public ResponseEntity<Map<String, String>> verifyRegistration(@RequestParam String token){

        verificationTokenService.verifyToken(token);

        return ResponseEntity.ok(Map.of("message", "Registration successfully verified!"));
        
    }

    @GetMapping("/resend-verify-token")
    public ResponseEntity<Map<String, String>> resendVerificationToken(String email){
        // Resend token - event will handle token creation and email
        // Email URL is configured via FrontendProperties (application.yml)
        userService.resendVerificationToken(email);

        return ResponseEntity.ok(Map.of("message", "Verification token resent. Please check your email."));
    }

    /**
     * Forgot Password - User requests password reset
     * 
     * IMPROVED FLOW:
     * 1. User provides email → POST /api/v1/users/forgot-password
     * 2. System sends email with reset link → http://frontend.com/reset-password?token=xxx
     * 3. User clicks link → Frontend displays password reset form (token extracted from URL)
     * 4. User submits form → POST /api/v1/users/reset-password?token=xxx
     * 5. Backend validates token AND resets password in one step
     * 
     * Benefits:
     * - Better UX (frontend handles form display)
     * - More RESTful (single POST operation, no GET endpoint needed)
     * - Simpler API (one less endpoint)
     * 
     * @param request ForgotPasswordRequestDto containing email
     * @return Success message (always returns success for security - doesn't reveal if email exists)
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        // Request password reset - event will handle token creation and email
        // Email link URL is configured via FrontendProperties (application.yml)
        userService.requestPasswordReset(request.getEmail());
        
        return ResponseEntity.ok(Map.of(
            "message", 
            "If an account exists with this email, a password reset link has been sent. Please check your email."
        ));
    }

    /**
     * Reset Password - User submits new password with reset token
     * 
     * IMPROVED FLOW:
     * - Email link points to frontend: http://frontend.com/reset-password?token=xxx
     * - Frontend displays password reset form (token extracted from URL)
     * - User submits form → POST /api/v1/users/reset-password?token=xxx
     * - Backend validates token AND resets password in one step
     * 
     * Token can be provided in:
     * - Query parameter: POST /api/v1/users/reset-password?token=xxx (RECOMMENDED - better UX)
     * - Request body: POST /api/v1/users/reset-password with token in JSON (alternative)
     * 
     * Security:
     * - Token is validated before password reset
     * - Token is deleted after successful password reset
     * - Token expires after 15 minutes
     * 
     * @param token Reset token from email link (query parameter - recommended)
     * @param request ResetPasswordRequestDto containing password and matchingPassword
     * @return Success message
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestParam(required = false) String token,
            @Valid @RequestBody ResetPasswordRequestDto request) {
        
        // Use token from query parameter if provided, otherwise use token from request body
        // Query parameter is RECOMMENDED - allows frontend to extract token from URL and submit directly
        String resetToken = (token != null && !token.isBlank()) ? token : request.getToken();
        
        // Validate token is provided
        if (resetToken == null || resetToken.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "validation_failed", 
                            "message", "Reset token is required. Provide it in query parameter (?token=xxx) or request body."));
        }
        
        // Create a new DTO with the token (from query param or body)
        ResetPasswordRequestDto resetRequest = new ResetPasswordRequestDto(
            resetToken,
            request.getPassword(),
            request.getMatchingPassword()
        );
        
        // Reset password - service will verify token and update password in one step
        // Token validation happens inside resetPassword() method
        userService.resetPassword(resetRequest);
        
        return ResponseEntity.ok(Map.of(
            "message", 
            "Password has been reset successfully. You can now login with your new password."
        ));
    }

    /**
     * Change Password - For authenticated users who know their current password
     * 
     * Flow:
     * 1. User is authenticated (logged in via HTTP Basic Auth or form login)
     * 2. User provides current password + new password
     * 3. System verifies current password matches
     * 4. System verifies new password is different from current
     * 5. System updates password
     * 
     * Security:
     * - Requires authentication (user must be logged in)
     * - Verifies current password before allowing change
     * - Ensures new password is different from current password
     * 
     * Authentication:
     * - HTTP Basic Auth: Send Authorization header with email:password
     * - Form Login: User must be logged in via web form
     * 
     * Example HTTP Basic Auth (Postman/curl):
     * Authorization: Basic base64(email:password)
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequestDto request) {
        
        // Null safety check for request
        if (request == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "validation_failed", 
                            "message", "Request body cannot be null"));
        }
        
        // Get userId directly from Authentication principal - NO DATABASE LOOKUP NEEDED! ✅
        // This is the BEST PRACTICE approach!
        // CustomUserDetails stores only essential fields, avoiding lazy loading issues
        Long userId = getUserIdFromAuthentication(authentication);
        
        // Change password - service will verify current password and update
        userService.changePassword(userId, request);
        
        return ResponseEntity.ok(Map.of(
            "message", 
            "Password has been changed successfully."
        ));
    }
}
