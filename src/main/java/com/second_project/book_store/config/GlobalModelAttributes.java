package com.second_project.book_store.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.second_project.book_store.config.properties.AdminProperties;

/**
 * Global model attributes for all Thymeleaf page controllers.
 * 
 * This class automatically adds common attributes to all page templates,
 * avoiding the need to add them in every controller method.
 * 
 * BEST PRACTICES:
 * - Use @ControllerAdvice to apply to all controllers in specified packages
 * - Use @ModelAttribute to add attributes to all models
 * - Keep attributes minimal and commonly used
 */
@ControllerAdvice(basePackages = "com.second_project.book_store.controller.page")
public class GlobalModelAttributes {

    private final AdminProperties adminProperties;

    public GlobalModelAttributes(AdminProperties adminProperties) {
        this.adminProperties = adminProperties;
    }

    /**
     * Add admin email to all page models for footer display.
     */
    @ModelAttribute("adminEmail")
    public String getAdminEmail() {
        return adminProperties.getEmail();
    }
}

