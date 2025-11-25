package com.second_project.book_store.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.second_project.book_store.entity.User;
import com.second_project.book_store.entity.User.UserRole;

/**
 * Custom UserDetails implementation that stores only essential user information.
 * 
 * BEST PRACTICE: Store only necessary fields instead of entire User entity to avoid:
 * - LazyInitializationException (from accessing lazy-loaded relationships)
 * - Serialization issues (when stored in session/cache)
 * - Security risks (password hash stored longer than needed)
 * - Memory overhead (storing unnecessary data)
 * 
 * Benefits:
 * - No database lookup needed in controllers
 * - Better performance and memory efficiency
 * - Cleaner code
 * - userId, firstName, lastName, fullName, email, role, enabled available directly from Authentication
 * - No risk of lazy loading exceptions
 */
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String password;
    private final UserRole role;
    private final boolean enabled;

    /**
     * Constructor with null safety checks.
     * 
     * @param userId User ID (required)
     * @param firstName User first name (required)
     * @param lastName User last name (required)
     * @param email User email (required)
     * @param password Hashed password (required)
     * @param role User role (required)
     * @param enabled Whether account is enabled/verified (required)
     * @throws IllegalArgumentException if any required parameter is null
     */
    public CustomUserDetails(Long userId, String firstName, String lastName, String email, String password, UserRole role, boolean enabled) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be null or blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be null or blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        if (role == null) {
            throw new IllegalArgumentException("User role cannot be null");
        }
        
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
    }

    /**
     * Convenience constructor from User entity.
     * Extracts only necessary fields to avoid storing entire entity.
     * 
     * @param user User entity (must not be null)
     * @throws IllegalArgumentException if user is null or required fields are missing
     */
    public CustomUserDetails(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        this.userId = user.getUserId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.role = user.getRole();
        this.enabled = user.isEnabled();
        
        // Validate extracted fields
        if (this.userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (this.firstName == null || this.firstName.isBlank()) {
            throw new IllegalArgumentException("User first name cannot be null or blank");
        }
        if (this.lastName == null || this.lastName.isBlank()) {
            throw new IllegalArgumentException("User last name cannot be null or blank");
        }
        if (this.email == null || this.email.isBlank()) {
            throw new IllegalArgumentException("User email cannot be null or blank");
        }
        if (this.password == null || this.password.isBlank()) {
            throw new IllegalArgumentException("User password cannot be null or blank");
        }
        if (this.role == null) {
            throw new IllegalArgumentException("User role cannot be null");
        }
    }

    /**
     * Returns the userId directly.
     * This is what we want to use in controllers!
     * 
     * @return User ID (never null)
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Returns the user's first name.
     * 
     * @return User first name (never null)
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the user's last name.
     * 
     * @return User last name (never null)
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the user's full name (first name + last name).
     * This is the preferred method for displaying user names in the UI.
     * 
     * @return User full name formatted as "FirstName LastName" (never null)
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Returns the user's email.
     * 
     * @return User email (never null)
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the user's role.
     * 
     * @return User role (never null)
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * Returns whether the account is verified (email confirmed).
     * Use this method in business logic to check verification status.
     * 
     * This is different from isEnabled() which is used by Spring Security.
     * 
     * @return true if account is verified, false otherwise
     */
    public boolean isVerified() {
        return enabled;
    }

    /**
     * Returns the email as the username.
     * Spring Security uses this for authentication.getName()
     * 
     * @return Email address (never null)
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Returns the hashed password.
     * Spring Security uses this to verify login credentials.
     * 
     * @return Hashed password (never null)
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns user's authorities (roles).
     * Converts UserRole enum to Spring Security GrantedAuthority.
     * Format: "ROLE_ADMIN" or "ROLE_USER"
     * 
     * @return Collection of authorities (never null)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convert UserRole to Spring Security authority
        // Format: "ROLE_ADMIN" or "ROLE_USER"
        String authority = "ROLE_" + role.name();
        return Collections.singletonList(new SimpleGrantedAuthority(authority));
    }

    /**
     * Account is not expired.
     * Can be extended to support account expiration logic.
     * 
     * @return true (account never expires by default)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true; // You can add account expiration logic here
    }

    /**
     * Account is not locked.
     * Can be extended to support account locking logic.
     * 
     * @return true (account never locked by default)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true; // You can add account locking logic here
    }

    /**
     * Credentials (password) are not expired.
     * Can be extended to support password expiration logic.
     * 
     * @return true (credentials never expire by default)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true; // You can add password expiration logic here
    }

    /**
     * Account is enabled for Spring Security authentication.
     * 
     * IMPORTANT: Always returns true to allow unverified users to log in.
     * Spring Security checks this method during authentication.
     * 
     * To check if account is actually verified, use isVerified() instead.
     * This allows us to:
     * - Let unverified users log in
     * - Show them a notification to verify their account
     * - Restrict features for unverified users
     * 
     * @return true (always allows login regardless of verification status)
     */
    @Override
    public boolean isEnabled() {
        return true; // Always allow login, verification checked separately
    }

    /**
     * Returns a string representation of CustomUserDetails.
     * Excludes password for security reasons.
     * 
     * @return String representation (password excluded)
     */
    @Override
    public String toString() {
        return "CustomUserDetails{" +
                "userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", enabled=" + enabled +
                '}';
    }
}

