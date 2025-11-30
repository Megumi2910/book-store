package com.second_project.book_store.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.second_project.book_store.exception.PhoneNumberAlreadyExistedException;
import com.second_project.book_store.exception.UserAlreadyExistedException;
import com.second_project.book_store.model.UserDto;
import com.second_project.book_store.service.UserService;

import jakarta.validation.Valid;




@Controller
public class RegistrationPageController {

    private final UserService userService;

    public RegistrationPageController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "register";
    }
    
    @PostMapping("/register")
    public String processRegistration(
        @Valid @ModelAttribute UserDto userDto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        
        if (bindingResult.hasErrors()){
            return "register";
        }
        
        try {
            userService.registerUser(userDto);
        
            redirectAttributes.addFlashAttribute("registration_successful", "User registered successfully. Please verify your account after logging in.");
            
            return "redirect:/login";

        } catch (UserAlreadyExistedException e) {

            bindingResult.rejectValue("email", "error.email", e.getMessage());

            return "register";
            
        } catch (PhoneNumberAlreadyExistedException e){

            bindingResult.rejectValue("phoneNumber", "error.phoneNumber", e.getMessage());
            
            return "register";
        }

        
    }
    

}
