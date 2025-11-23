package com.second_project.book_store.config;

import com.second_project.book_store.config.properties.AdminProperties;
import com.second_project.book_store.entity.User;
import com.second_project.book_store.entity.User.UserRole;
import com.second_project.book_store.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

/**
 * DataSeeder - Creates initial admin user for development environment.
 * Only runs when 'dev' profile is active.
 * 
 * Best Practices Applied:
 * - Profile-specific (@Profile("dev")) - only runs in development
 * - Type-safe configuration using @ConfigurationProperties
 * - Configurable via application.yml and environment variables
 * - Proper logging using SLF4J
 * - Error handling to prevent startup failures
 * - Checks for existing admin before creating
 */
@Configuration
@Profile("dev")
public class DataSeeder {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final AdminProperties adminProperties;

    /**
     * Constructor injection - preferred over field injection.
     * Makes dependencies explicit and enables immutability.
     */
    public DataSeeder(AdminProperties adminProperties) {
        this.adminProperties = adminProperties;
    }

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        
        return args -> {
            try {
                Optional<User> adminUser = userRepository.findByRole(UserRole.ADMIN);
                
                if (adminUser.isEmpty()) {
                    logger.info("No ADMIN user found, creating one with email: {}", adminProperties.getEmail());
                    
                    User admin = new User();
                    admin.setFirstName(adminProperties.getFirstName());
                    admin.setLastName(adminProperties.getLastName());
                    admin.setEmail(adminProperties.getEmail());
                    admin.setPassword(passwordEncoder.encode(adminProperties.getDefaultPassword()));
                    admin.setRole(UserRole.ADMIN);
                    admin.setPhoneNumber(adminProperties.getPhoneNumber());
                    admin.setEnabled(true);
                    
                    userRepository.save(admin);
                    
                    logger.info("ADMIN user created successfully with email: {}", adminProperties.getEmail());
                } else {
                    logger.debug("ADMIN user already exists with email: {}", adminUser.get().getEmail());
                }
            } catch (Exception e) {
                // Log error but don't fail application startup
                logger.error("Failed to initialize admin user. Application will continue without admin user.", e);
            }
        };
    }
}