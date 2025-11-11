package com.second_project.book_store.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
        "/",
        "/verify-registration",
        "/register",
        "/resend-verify-token",
        "/reset-password",
        "/save-password",
        "/change-password",
        "/login/**",
        "/error",
        "/css/**",
        "/js/**",
        "/images/**",
        "/webjars/**"
    };

    private static final String[] CSRF_IGNORED_ENDPOINTS = {
        "/api/**",
        "/register",
        "/verify-registration",
        "/resend-verify-token",
        "/reset-password",
        "/save-password",
        "/change-password"
    };

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
        @Value("${app.cors.allowed-origins:*}") String allowedOrigins
    ) {
        String[] originArray = Arrays.stream(allowedOrigins.split(","))
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

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.ignoringRequestMatchers(CSRF_IGNORED_ENDPOINTS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return httpSecurity.build();
    }
}
