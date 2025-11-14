package com.second_project.book_store.controller.api;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.second_project.book_store.model.UserDto;
import com.second_project.book_store.service.UserService;
import com.second_project.book_store.service.VerificationTokenService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final VerificationTokenService verificationTokenService;

    public UserController(UserService userService, VerificationTokenService verificationTokenService){
        this.userService = userService;
        this.verificationTokenService = verificationTokenService;
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
    public String registerUser(@Valid @RequestBody UserDto userDto) {
        // Get the application URL from the request using best practices
        String applicationUrl = getApplicationUrl();
        
        // Register user - event will handle token creation and email
        userService.registerUser(userDto, applicationUrl);

        return "User registered successfully! Please check your email for verification link.";
    }
    
    @GetMapping("/verify-registration")
    public ResponseEntity<Map<String, String>> verifyRegistration(String token){

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
}
