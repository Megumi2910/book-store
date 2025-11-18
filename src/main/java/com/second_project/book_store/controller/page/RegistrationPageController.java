package com.second_project.book_store.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.second_project.book_store.model.UserDto;
import com.second_project.book_store.service.UserService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;




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

        userService.registerUser(userDto);
        
        redirectAttributes.addFlashAttribute("success", "User registered successfully. Please check your email to activate your account.");

        return "redirect:/register";
    }
    

}
