package com.second_project.book_store.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class ChangePasswordPageController {

    @GetMapping("/change-password")
    public String showChangePasswordForm(@RequestParam String param) {

        
        return new String();
    }
    
}
