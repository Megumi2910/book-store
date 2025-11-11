package com.second_project.book_store.controller.api;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.second_project.book_store.model.UserDto;
import com.second_project.book_store.service.UserService;
import com.second_project.book_store.service.VerificationTokenService;

import jakarta.servlet.http.HttpServletRequest;
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


    @PostMapping("/register")
    public String registerUser(@Valid @RequestBody UserDto userDto, HttpServletRequest request) {
        // Get the application URL from the request
        String applicationUrl = request.getScheme() + "://" + 
                               request.getServerName() + ":" + 
                               request.getServerPort() + 
                               request.getContextPath();
        
        // Register user - event will handle token creation and email
        userService.registerUser(userDto, applicationUrl);

        return "User registered successfully! Please check your email for verification link.";
    }
    
    @GetMapping("/verify-registration")
    public ResponseEntity<Map<String, String>> verifyRegistration(String token){

        verificationTokenService.verifyToken(token);

        return ResponseEntity.ok(Map.of("message", "Registration successfully verified"));

        
    }

    
}
