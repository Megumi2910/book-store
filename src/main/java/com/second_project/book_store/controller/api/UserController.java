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

import com.second_project.book_store.model.ForgotPasswordRequestDto;
import com.second_project.book_store.model.ResetPasswordRequestDto;
import com.second_project.book_store.model.UserDto;
import com.second_project.book_store.service.ResetPasswordTokenService;
import com.second_project.book_store.service.UserService;
import com.second_project.book_store.service.VerificationTokenService;

import jakarta.validation.Valid;


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
     * User provides token and new password
     * 
     * Flow: User fills form → POST /api/v1/users/reset-password → Password updated
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        // Reset password - service will verify token and update password
        userService.resetPassword(request);
        
        return ResponseEntity.ok(Map.of(
            "message", 
            "Password has been reset successfully. You can now login with your new password."
        ));
    }
}
