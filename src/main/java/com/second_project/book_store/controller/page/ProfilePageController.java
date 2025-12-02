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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.second_project.book_store.entity.User;
import com.second_project.book_store.exception.PhoneNumberAlreadyExistedException;
import com.second_project.book_store.exception.UserNotFoundException;
import com.second_project.book_store.model.ProfileUpdateDto;
import com.second_project.book_store.security.CustomUserDetails;
import com.second_project.book_store.service.OrderService;
import com.second_project.book_store.service.ReviewService;
import com.second_project.book_store.service.UserService;

import jakarta.validation.Valid;


@Controller
@RequestMapping("/profile")
public class ProfilePageController {

    private static final Logger logger = LoggerFactory.getLogger(ProfilePageController.class);

    private final UserService userService;
    private final OrderService orderService;
    private final ReviewService reviewService;

    public ProfilePageController(UserService userService, OrderService orderService, ReviewService reviewService) {
        this.userService = userService;
        this.orderService = orderService;
        this.reviewService = reviewService;
    }

    /**
     * Show profile page with user information.
     * Available to all authenticated users (including unverified).
     */
    @GetMapping
    public String showProfilePage(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        logger.debug("Showing profile page for user {}", userId);

        User user = userService.findUserById(userId);

        // Create DTO for form binding
        ProfileUpdateDto profileDto = new ProfileUpdateDto(
            user.getFirstName(),
            user.getLastName(),
            user.getPhoneNumber(),
            user.getAddress()
        );

        // Add profile data
        model.addAttribute("profileUpdateDto", profileDto);
        model.addAttribute("userEmail", user.getEmail());
        model.addAttribute("isVerified", user.isEnabled());
        model.addAttribute("memberSince", user.getCreatedAt());
        model.addAttribute("lastUpdated", user.getUpdatedAt());

        // Add statistics
        Long orderCount = orderService.countUserOrders(userId);
        Long reviewCount = reviewService.countUserReviews(userId);
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("reviewCount", reviewCount);

        return "profile";
    }

    /**
     * Update user profile.
     * Available to all authenticated users (including unverified).
     */
    @PostMapping
    public String updateProfile(
            @Valid @ModelAttribute("profileUpdateDto") ProfileUpdateDto profileUpdateDto,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        logger.info("Updating profile for user {}", userId);

        if (bindingResult.hasErrors()) {
            // Re-add model attributes for the view
            User user = userService.findUserById(userId);
            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("isVerified", user.isEnabled());
            model.addAttribute("memberSince", user.getCreatedAt());
            model.addAttribute("lastUpdated", user.getUpdatedAt());
            
            Long orderCount = orderService.countUserOrders(userId);
            Long reviewCount = reviewService.countUserReviews(userId);
            model.addAttribute("orderCount", orderCount);
            model.addAttribute("reviewCount", reviewCount);
            
            return "profile";
        }

        try {
            userService.updateProfile(userId, profileUpdateDto);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            
            logger.info("Profile updated successfully for user {}", userId);
            return "redirect:/profile";

        } catch (UserNotFoundException e) {
            logger.error("User not found during profile update: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "An error occurred. Please try again.");
            return "redirect:/profile";

        } catch (PhoneNumberAlreadyExistedException e) {
            logger.warn("Phone number already exists: {}", e.getMessage());
            bindingResult.rejectValue("phoneNumber", "error.phoneNumber", e.getMessage());
            
            // Re-add model attributes for the view
            User user = userService.findUserById(userId);
            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("isVerified", user.isEnabled());
            model.addAttribute("memberSince", user.getCreatedAt());
            model.addAttribute("lastUpdated", user.getUpdatedAt());
            
            Long orderCount = orderService.countUserOrders(userId);
            Long reviewCount = reviewService.countUserReviews(userId);
            model.addAttribute("orderCount", orderCount);
            model.addAttribute("reviewCount", reviewCount);
            
            return "profile";
        }
    }
}
