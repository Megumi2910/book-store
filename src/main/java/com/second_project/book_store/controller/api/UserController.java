package com.second_project.book_store.controller.api;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.second_project.book_store.model.ChangePasswordRequestDto;
import com.second_project.book_store.model.ForgotPasswordRequestDto;
import com.second_project.book_store.model.ResetPasswordRequestDto;
import com.second_project.book_store.model.UserDto;
import com.second_project.book_store.service.ResetPasswordTokenService;
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
    private final ResetPasswordTokenService resetPasswordTokenService;

    public UserController(UserService userService, 
                         VerificationTokenService verificationTokenService,
                         ResetPasswordTokenService resetPasswordTokenService){
        this.userService = userService;
        this.verificationTokenService = verificationTokenService;
        this.resetPasswordTokenService = resetPasswordTokenService;
    }

    /**
     * Extracts the base application URL from the current request.
     * Uses Spring's ServletUriComponentsBuilder for reliable URL construction.
     * Handles context path, port, and scheme automatically.
     * Works correctly behind proxies and load balancers.
     * 
     * @return The base application URL (e.g., "http://localhost:8080" or "https://example.com/myapp")
     */
    private String getApplicationUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();
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
        // Get the application URL from the request using best practices
        String applicationUrl = getApplicationUrl();
        
        // Register user - event will handle token creation and email
        userService.registerUser(userDto, applicationUrl);

        return ResponseEntity.ok(Map.of("message", "User registered successfully! Please check your email for verification link."));
    }
    
    @GetMapping("/verify-registration")
    public ResponseEntity<Map<String, String>> verifyRegistration(@RequestParam String token){

        verificationTokenService.verifyToken(token);

        return ResponseEntity.ok(Map.of("message", "Registration successfully verified!"));
        
    }

    @GetMapping("/resend-verify-token")
    public ResponseEntity<Map<String, String>> resendVerificationToken(String email){
        // Get the application URL from the request using best practices
        String applicationUrl = getApplicationUrl();
        
        // Resend token - event will handle token creation and email
        userService.resendVerificationToken(email, applicationUrl);

        return ResponseEntity.ok(Map.of("message", "Verification token resent. Please check your email."));
    }

    /**
     * Step 1: Forgot Password - User requests password reset
     * User provides email, receives reset link via email
     * 
     * Flow: POST /api/v1/users/forgot-password → Email sent → User clicks link → GET /api/v1/users/reset-password?token=xxx → POST /api/v1/users/reset-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        String applicationUrl = getApplicationUrl();
        
        // Request password reset - event will handle token creation and email
        userService.requestPasswordReset(request.getEmail(), applicationUrl);
        
        return ResponseEntity.ok(Map.of(
            "message", 
            "If an account exists with this email, a password reset link has been sent. Please check your email."
        ));
    }

    /**
     * Step 2: Validate Reset Token - User clicks reset link from email
     * This endpoint validates the token and can redirect to password reset form
     * 
     * Flow: User clicks email link → GET /api/v1/users/reset-password?token=xxx → Returns token validity
     * 
     * Note: In a frontend application, this endpoint would typically redirect to a password reset form.
     * For API-only, it just validates the token and returns success.
     */
    @GetMapping("/reset-password")
    public ResponseEntity<Map<String, String>> validateResetToken(@RequestParam String token) {
        // Verify token is valid (will throw exception if not)
        resetPasswordTokenService.verifyToken(token);
        
        return ResponseEntity.ok(Map.of(
            "message", 
            "Token is valid. Please use POST /api/v1/users/reset-password with token and new password.",
            "token",
            token
        ));
    }

    /**
     * Step 3: Reset Password - User submits new password
     * 
     * Improved UX: Token can be provided in query parameter OR request body
     * This allows users to click email link and submit password without copying token
     * 
     * Flow Options:
     * Option A (Better UX): 
     *   - User clicks email link → GET /reset-password?token=xxx (validates)
     *   - User submits form → POST /reset-password?token=xxx + password in body
     * 
     * Option B (Alternative):
     *   - POST /reset-password with token + password in body
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestParam(required = false) String token,
            @Valid @RequestBody ResetPasswordRequestDto request) {
        
        // Use token from query parameter if provided, otherwise use token from request body
        // This improves UX - users can click email link and submit without copying token
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
        
        // Reset password - service will verify token and update password
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
