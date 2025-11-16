package com.second_project.book_store.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.second_project.book_store.entity.User;
import com.second_project.book_store.repository.UserRepository;

/**
 * Custom UserDetailsService implementation.
 * Spring Security uses this to load user details during authentication.
 * 
 * Flow:
 * 1. User submits login form with email/password
 * 2. Spring Security calls loadUserByUsername(email)
 * 3. We return CustomUserDetails containing only essential user fields
 * 4. Spring Security stores this in Authentication principal
 * 5. Controllers can access userId directly without database lookup!
 * 
 * BEST PRACTICES IMPLEMENTED:
 * - Case-insensitive email lookup (handles "user@example.com" and "User@Example.com")
 * - Null safety checks
 * - Stores only necessary fields (not entire User entity)
 * - Proper exception handling
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Constructor injection for UserRepository.
     * 
     * @param userRepository User repository (must not be null)
     * @throws IllegalArgumentException if userRepository is null
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("UserRepository cannot be null");
        }
        this.userRepository = userRepository;
    }

    /**
     * Loads user by email (username) for Spring Security authentication.
     * 
     * BEST PRACTICE: Uses case-insensitive email lookup to handle case variations.
     * Example: "user@example.com" and "User@Example.com" are treated as the same.
     * 
     * This method is called automatically by Spring Security when:
     * - User submits login form
     * - User authenticates via HTTP Basic Auth
     * - Spring Security needs to verify credentials
     * 
     * @param email The email address (used as username) - case-insensitive
     * @return CustomUserDetails containing essential user information
     * @throws UsernameNotFoundException if user not found or email is null/blank
     * @throws IllegalArgumentException if email is null or blank
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Null safety check
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Email cannot be null or blank");
        }
        
        // BEST PRACTICE: Use case-insensitive lookup for authentication
        // This handles case variations: "user@example.com" = "User@Example.com"
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        // Return CustomUserDetails with only essential fields
        // This avoids storing entire User entity and prevents lazy loading issues
        return new CustomUserDetails(user);
    }
}

