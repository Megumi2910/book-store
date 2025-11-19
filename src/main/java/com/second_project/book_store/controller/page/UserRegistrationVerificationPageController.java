package com.second_project.book_store.controller.page;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.second_project.book_store.entity.User;
import com.second_project.book_store.security.CustomUserDetails;
import com.second_project.book_store.service.VerificationTokenService;


@Controller
public class UserRegistrationVerificationPageController {

    private final VerificationTokenService verificationTokenService;

    public UserRegistrationVerificationPageController(VerificationTokenService verificationTokenService) {
        this.verificationTokenService = verificationTokenService;
    }

    @GetMapping("/verify-registration")
    public String processRegistrationVerification(
        @RequestParam String token, 
        Authentication authentication) {

        // Verify the token and get the updated user
        User verifiedUser = verificationTokenService.verifyTokenAndReturnUser(token);
        
        // If the current logged-in user is the one being verified, update the session
        if (authentication != null) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            if (currentUser.getUserId().equals(verifiedUser.getUserId())) {
                // Create new CustomUserDetails with updated verification status
                CustomUserDetails updatedUserDetails = new CustomUserDetails(verifiedUser);
                
                // Update the Security Context
                Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    updatedUserDetails,
                    authentication.getCredentials(),
                    updatedUserDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(newAuth);


                verificationTokenService.deleteByUserId(currentUser.getUserId());
            }
        }
        
        return "verify-registration";
    }

}
