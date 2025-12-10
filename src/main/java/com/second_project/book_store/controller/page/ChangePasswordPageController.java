package com.second_project.book_store.controller.page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.second_project.book_store.exception.InvalidPasswordException;
import com.second_project.book_store.exception.UserNotFoundException;
import com.second_project.book_store.model.ChangePasswordRequestDto;
import com.second_project.book_store.security.CustomUserDetails;
import com.second_project.book_store.service.UserService;

import jakarta.validation.Valid;



@Controller
public class ChangePasswordPageController {

    private static final Logger logger = LoggerFactory.getLogger(ChangePasswordPageController.class);
    private final UserService userService;

    public ChangePasswordPageController(UserService userService){
        this.userService = userService;
    }    

    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {

        model.addAttribute("changePasswordRequest", new ChangePasswordRequestDto());
        return "change-password";
    }
    
    @PostMapping("/change-password")
    public String postMethodName(
        @Valid @ModelAttribute("changePasswordRequest") ChangePasswordRequestDto requestDto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes,
        Authentication authentication
    ) {

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = customUserDetails.getUserId();

        logger.info("User with ID = {} changing password", userId);

        if (bindingResult.hasErrors()){
            return "change-password";
        }

        try {

            userService.changePassword(userId, requestDto);
            redirectAttributes.addFlashAttribute("success", "Password changed successfully.");
            
            return "redirect:/change-password";

        } catch (UserNotFoundException e) {
            
            logger.warn("Unknown user error", e.getMessage());
            return "change-password";

        } catch (InvalidPasswordException e){

            logger.warn("Validation error changing password : {}", e.getMessage());
    
            // Add error to BindingResult so it appears as field error
            if (e.getMessage().contains("Current password is incorrect")) {
                bindingResult.rejectValue("currentPassword", "error.currentPassword", e.getMessage());
            }
            else if (e.getMessage().contains("New password must be different from current password")){
                bindingResult.rejectValue("password", "error.password", e.getMessage());
            }
            
            return "change-password";
        }
    }
    
    
}
