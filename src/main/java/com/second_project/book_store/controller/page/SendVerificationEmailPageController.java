package com.second_project.book_store.controller.page;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.second_project.book_store.security.CustomUserDetails;
import com.second_project.book_store.service.UserService;


@Controller
public class SendVerificationEmailPageController {

    private final UserService userService;

    public SendVerificationEmailPageController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/send-verify-email")
    public String sendVerificationEmail(Authentication authentication, jakarta.servlet.http.HttpSession session) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/login"; // Redirect if not authenticated
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        // Send verification email (rate limiting checked inside service)
        userService.requestVerificationEmail(userDetails.getEmail());
        
        // Store the current time in session for rate limiting UI
        session.setAttribute("lastVerificationEmailSent", java.time.LocalDateTime.now());
        
        return "send-verify-email";
    }
    

}
