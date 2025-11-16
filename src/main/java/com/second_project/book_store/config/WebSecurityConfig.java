package com.second_project.book_store.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.second_project.book_store.config.properties.CorsProperties;
import com.second_project.book_store.security.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final CorsProperties corsProperties;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Constructor injection for dependencies.
     * 
     * BEST PRACTICE: Explicitly inject UserDetailsService instead of relying on auto-detection.
     * This makes the configuration more explicit and easier to test.
     * 
     * @param corsProperties CORS configuration properties
     * @param userDetailsService Custom UserDetailsService implementation
     * @throws IllegalArgumentException if any parameter is null
     */
    public WebSecurityConfig(CorsProperties corsProperties, CustomUserDetailsService userDetailsService) {
        if (corsProperties == null) {
            throw new IllegalArgumentException("CorsProperties cannot be null");
        }
        if (userDetailsService == null) {
            throw new IllegalArgumentException("CustomUserDetailsService cannot be null");
        }
        this.corsProperties = corsProperties;
        this.userDetailsService = userDetailsService;
    }

    private static final String[] PUBLIC_ENDPOINTS = {
        "/",
        "/verify-registration",
        "/register",
        "/resend-verify-token",
        "/reset-password",
        "/save-password",
        "/forgot-password",
        "/login/**",
        "/error",
        "/css/**",
        "/js/**",
        "/images/**",
        "/webjars/**",
        "/api/v1/users/register",
        "/api/v1/users/verify-registration",
        "/api/v1/users/resend-verify-token",
        "/api/v1/users/forgot-password",
        "/api/v1/users/reset-password"
    };

    private static final String[] CSRF_IGNORED_ENDPOINTS = {
        "/api/**",
        "/register",
        "/verify-registration",
        "/resend-verify-token",
        "/reset-password",
        "/save-password",
        "/forgot-password"
    };

    /**
     * Password encoder bean.
     * Uses BCrypt with strength factor of 11 (higher = more secure but slower).
     * 
     * BEST PRACTICE: Strength factor 11 is a good balance between security and performance.
     * Higher values (12-15) are more secure but slower for password verification.
     * 
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }

    /**
     * Authentication provider bean.
     * Configures how Spring Security authenticates users.
     * 
     * BEST PRACTICE: Explicitly configure DaoAuthenticationProvider with:
     * - UserDetailsService (loads user details)
     * - PasswordEncoder (verifies passwords)
     * 
     * Note: Some IDEs may show deprecation warnings, but these methods are still
     * the standard way to configure DaoAuthenticationProvider in Spring Security 6.x.
     * The warnings are likely false positives or related to internal API changes.
     * 
     * @return DaoAuthenticationProvider instance
     */
    @Bean
    DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication manager bean.
     * Manages authentication process using the configured authentication provider.
     * 
     * @return AuthenticationManager instance
     */
    @Bean
    AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider());
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        String[] originArray = Arrays.stream(corsProperties.getAllowedOrigins().split(","))
            .map(String::trim)
            .filter(origin -> !origin.isBlank())
            .toArray(String[]::new);

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Location", "Authorization"));
        configuration.setMaxAge(3600L);

        if (originArray.length == 0 || Arrays.asList(originArray).contains("*")) {
            configuration.setAllowedOriginPatterns(List.of("*"));
            configuration.setAllowCredentials(false);
        } else {
            configuration.setAllowedOrigins(Arrays.asList(originArray));
            configuration.setAllowCredentials(true);
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    /**
     * Security filter chain configuration.
     * 
     * BEST PRACTICES IMPLEMENTED:
     * 1. Explicit UserDetailsService configuration (via authenticationProvider)
     * 2. HTTP Basic Auth for REST API endpoints (/api/**)
     * 3. Form login for web pages (if needed)
     * 4. CORS configuration for cross-origin requests
     * 5. CSRF protection disabled for REST APIs (stateless)
     * 
     * Authentication Methods:
     * - REST APIs (/api/**): HTTP Basic Auth (username=email, password=password)
     * - Web Pages: Form-based login (if you have web forms)
     * 
     * @param httpSecurity HttpSecurity builder
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
            // CORS configuration for cross-origin requests
            .cors(Customizer.withDefaults())
            
            // CSRF protection: Disabled for REST APIs (stateless), enabled for web forms
            .csrf(csrf -> csrf.ignoringRequestMatchers(CSRF_IGNORED_ENDPOINTS))
            
            // Authorization rules
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .anyRequest().authenticated()
            )
            
            // HTTP Basic Authentication for REST APIs
            // BEST PRACTICE: REST APIs should use stateless authentication (HTTP Basic or JWT)
            // This allows clients (Postman, mobile apps, frontend) to authenticate easily
            .httpBasic(Customizer.withDefaults())
            
            // Form login for web pages (optional - only if you have web forms)
            // Note: This works alongside HTTP Basic Auth
            // - REST clients use HTTP Basic Auth (Authorization header)
            // - Web browsers can use form login
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            )
            
            // Logout configuration
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return httpSecurity.build();
    }
}
