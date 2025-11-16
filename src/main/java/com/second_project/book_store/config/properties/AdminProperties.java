package com.second_project.book_store.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Type-safe configuration properties for Admin user settings.
 * 
 * This class binds to properties under 'admin' prefix in application.yml.
 * Benefits of @ConfigurationProperties:
 * - Type safety (compile-time checking)
 * - IDE autocomplete support
 * - Validation support
 * - Centralized configuration
 * 
 * Usage in application.yml:
 * admin:
 *   default-password: ${ADMIN_DEFAULT_PASSWORD}
 *   first-name: ${ADMIN_FIRST_NAME:Admin}
 *   last-name: ${ADMIN_LAST_NAME:User}
 *   email: ${ADMIN_EMAIL}
 */
@Configuration
@ConfigurationProperties(prefix = "admin")
public class AdminProperties {

    /**
     * Default password for admin user (from environment variable).
     * Required - no default value for security.
     */
    private String defaultPassword;

    /**
     * Admin user's first name.
     * Default: "Admin" if not specified.
     */
    private String firstName = "Admin";

    /**
     * Admin user's last name.
     * Default: "User" if not specified.
     */
    private String lastName = "User";

    /**
     * Admin user's email address (from environment variable).
     * Required - no default value.
     */
    private String email;

    // Getters and Setters
    public String getDefaultPassword() {
        return defaultPassword;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

