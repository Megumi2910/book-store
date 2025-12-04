package com.second_project.book_store.controller.page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.second_project.book_store.model.ReviewDto;
import com.second_project.book_store.service.ReviewService;

/**
 * Admin controller for managing reviews.
 * Allows viewing all reviews and removing comments while keeping ratings.
 */
@Controller
@RequestMapping("/admin/reviews")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private static final Logger logger = LoggerFactory.getLogger(AdminReviewController.class);
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * List reviews with optional rating filter.
     */
    @GetMapping({"", "/"})
    public String listReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size,
            @RequestParam(required = false) Integer rating,
            Model model) {

        logger.info("Admin listing reviews - page: {}, size: {}, rating: {}", page, size, rating);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReviewDto> reviewPage = reviewService.getAllReviews(pageable, rating);

        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviewPage.getTotalPages());
        model.addAttribute("totalItems", reviewPage.getTotalElements());
        model.addAttribute("selectedRating", rating);
        model.addAttribute("pageTitle", "Reviews Management");

        return "admin/reviews/list";
    }

    /**
     * Remove the textual comment from a review while keeping the rating.
     */
    @PostMapping("/{id}/remove-comment")
    public String removeComment(
            @PathVariable("id") Long reviewId,
            RedirectAttributes redirectAttributes) {

        logger.info("Admin removing comment for review {}", reviewId);

        try {
            reviewService.removeReviewComment(reviewId);
            redirectAttributes.addFlashAttribute("success", "Review comment removed. Rating has been kept.");
        } catch (Exception ex) {
            logger.warn("Failed to remove review comment", ex);
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/reviews";
    }
}


