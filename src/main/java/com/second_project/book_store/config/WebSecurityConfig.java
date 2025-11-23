package com.second_project.book_store.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.second_project.book_store.config.properties.CorsProperties;
import com.second_project.book_store.security.CustomAuthenticationSuccessHandler;
import com.second_project.book_store.security.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final CorsProperties corsProperties;
    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    /**
     * Constructor injection for dependencies.
     * 
     * BEST PRACTICE: Explicitly inject UserDetailsService instead of relying on auto-detection.
     * This makes the configuration more explicit and easier to test.
     * 
     * @param corsProperties CORS configuration properties
     * @param userDetailsService Custom UserDetailsService implementation
     * @param authenticationSuccessHandler Custom success handler for role-based redirection
     * @throws IllegalArgumentException if any parameter is null
     */
    public WebSecurityConfig(CorsProperties corsProperties, 
                            CustomUserDetailsService userDetailsService,
                            CustomAuthenticationSuccessHandler authenticationSuccessHandler) {
        if (corsProperties == null) {
            throw new IllegalArgumentException("CorsProperties cannot be null");
        }
        if (userDetailsService == null) {
            throw new IllegalArgumentException("CustomUserDetailsService cannot be null");
        }
        if (authenticationSuccessHandler == null) {
            throw new IllegalArgumentException("CustomAuthenticationSuccessHandler cannot be null");
        }
        this.corsProperties = corsProperties;
        this.userDetailsService = userDetailsService;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    private static final String[] PUBLIC_ENDPOINTS = {
        "/",
        "/verify-registration",
        "/register",
        "/send-verify-email",
        "/resend-verify-token",
        "/reset-password",  // ✅ Thymeleaf page - GET and POST (must be public!)
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
        "/api/v1/users/reset-password"  // API endpoint - POST only
    };

    private static final String[] ADMIN_ENDPOINTS = {
        "/admin/**"
    };

    /**
     * Endpoints to ignore CSRF protection.
     * 
     * ONLY include:
     * - REST APIs (stateless, use tokens instead of cookies)
     * - External webhooks (can't provide CSRF tokens)
     * 
     * DO NOT include:
     * - Web form endpoints (need CSRF protection!)
     * - Any POST/PUT/DELETE that changes state
     */
    private static final String[] CSRF_IGNORED_ENDPOINTS = {
        "/api/**"  // REST APIs only - stateless, no session, no CSRF needed
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
     * Security filter chain for REST API endpoints (/api/**).
     * 
     * Uses HTTP Basic Authentication for stateless API access.
     * - Postman, mobile apps, and other REST clients send credentials with each request
     * - No session management (stateless)
     * - CSRF protection disabled
     * 
     * @param httpSecurity HttpSecurity builder
     * @return Configured SecurityFilterChain for API endpoints
     * @throws Exception if configuration fails
     */
    @Bean
    @Order(1)  // Higher priority - evaluated first
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        
        httpSecurity
            // Apply this configuration only to /api/** endpoints
            .securityMatcher("/api/**")
            
            // CORS configuration for cross-origin requests
            .cors(Customizer.withDefaults())
            
            // CSRF protection disabled for REST APIs (stateless)
            .csrf(csrf -> csrf.disable())
            
            // Authorization rules
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/v1/auth/**").permitAll()  // Public auth endpoints
                .requestMatchers("/api/v1/users/register").permitAll()
                .requestMatchers("/api/v1/users/verify-registration").permitAll()
                .requestMatchers("/api/v1/users/resend-verify-token").permitAll()
                .requestMatchers("/api/v1/users/forgot-password").permitAll()
                .requestMatchers("/api/v1/users/reset-password").permitAll()
                .anyRequest().authenticated()  // All other API endpoints require authentication
            )
            
            // HTTP Basic Authentication for REST APIs
            .httpBasic(Customizer.withDefaults())
            
            // Disable session creation for APIs (stateless)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return httpSecurity.build();
    }
    
    /**
     * Security filter chain for web pages (Thymeleaf UI).
     * 
     * Uses Form Login for browser-based authentication.
     * - Session-based authentication
     * - Supports logout (unlike HTTP Basic Auth)
     * - CSRF protection enabled
     * 
     * @param httpSecurity HttpSecurity builder
     * @return Configured SecurityFilterChain for web pages
     * @throws Exception if configuration fails
     */
    @Bean
    @Order(2)  // Lower priority - evaluated after API filter chain
    SecurityFilterChain webSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
            // CORS configuration for cross-origin requests
            .cors(Customizer.withDefaults())
            
            // CSRF protection: Disabled for REST APIs (stateless), enabled for web forms
            .csrf(csrf -> csrf.ignoringRequestMatchers(CSRF_IGNORED_ENDPOINTS))
            
            // Authorization rules
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")  // Admin pages require ADMIN role
                .anyRequest().authenticated()
            )
            
            // Form login for web pages (Thymeleaf)
            // NO httpBasic() here - this allows logout to work properly!
            // Browser-based sessions work perfectly with form login
            .formLogin(form -> form
                .loginPage("/login")  // Custom login page
                .loginProcessingUrl("/login")  // POST endpoint (Spring Security handles automatically)
                .successHandler(authenticationSuccessHandler)  // Custom handler for role-based redirection
                .failureUrl("/login?error=true")  // Redirect back to login on failure
                .usernameParameter("username")  // Form field name (we use email as username)
                .passwordParameter("password")  // Form field name
                .permitAll()  // Allow everyone to access login page
            )
            
            // Logout configuration
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            
            // Security Headers - Protection against common web vulnerabilities
            .headers(headers -> headers
                // Prevent clickjacking attacks by denying the page to be displayed in frames
                .frameOptions(frame -> frame.deny())
                
                // Prevent MIME type sniffing
                .contentTypeOptions(contentType -> contentType.disable()) // Already enabled by default
                
                // XSS Protection (legacy, but still useful for older browsers)
                .xssProtection(xss -> xss
                    .headerValue(org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                )
                
                // Content Security Policy - control what resources can be loaded
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; " +
                                    "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; " +
                                    "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; " +
                                    "img-src 'self' data: https:; " +
                                    "font-src 'self' https://cdn.jsdelivr.net;")
                )
                
                // Referrer Policy - control how much referrer information is sent
                .referrerPolicy(referrer -> referrer
                    .policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
                
                // Permissions Policy (formerly Feature Policy)
                // Note: Deprecated in Spring Security 6.4, but still functional
                .permissionsPolicy(permissions -> permissions
                    .policy("geolocation=(), microphone=(), camera=()")
                )
            );

        return httpSecurity.build();
    }
}
