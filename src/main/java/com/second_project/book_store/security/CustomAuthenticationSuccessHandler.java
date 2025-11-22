package com.second_project.book_store.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Custom authentication success handler.
 * 
 * Redirects users to different pages based on their role:
 * - ADMIN users → Admin Dashboard (/admin/dashboard)
 * - Regular users → Homepage (/)
 * 
 * BEST PRACTICE: Role-based redirection improves UX by taking users
 * directly to their relevant interface.
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        logger.info("User authenticated successfully: {}", authentication.getName());

        // Check if user has ADMIN role
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            logger.info("Admin user detected, redirecting to admin dashboard");
            response.sendRedirect("/admin/dashboard");
        } else {
            logger.info("Regular user detected, redirecting to homepage");
            response.sendRedirect("/");
        }
    }
}

