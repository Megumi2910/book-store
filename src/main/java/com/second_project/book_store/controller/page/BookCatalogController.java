package com.second_project.book_store.controller.page;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.second_project.book_store.model.BookDto;
import com.second_project.book_store.model.ReviewDto;
import com.second_project.book_store.security.CustomUserDetails;
import com.second_project.book_store.service.BookService;
import com.second_project.book_store.service.CartService;
import com.second_project.book_store.service.GenreService;
import com.second_project.book_store.service.OrderService;
import com.second_project.book_store.service.ReviewService;

import jakarta.validation.Valid;

/**
 * Controller for public book catalog pages.
 * Handles browsing books, viewing book details, etc.
 */
@Controller
@RequestMapping("/books")
public class BookCatalogController {

    private static final Logger logger = LoggerFactory.getLogger(BookCatalogController.class);
    private static final int DEFAULT_PAGE_SIZE = 12;

    private final BookService bookService;
    private final GenreService genreService;
    private final CartService cartService;
    private final ReviewService reviewService;
    private final OrderService orderService;

    public BookCatalogController(BookService bookService, GenreService genreService, 
                                  CartService cartService, ReviewService reviewService, OrderService orderService) {
        this.bookService = bookService;
        this.genreService = genreService;
        this.cartService = cartService;
        this.reviewService = reviewService;
        this.orderService = orderService;
    }

    /**
     * Browse all books with pagination, search, and genre filtering.
     * Public access - no authentication required.
     */
    @GetMapping({"", "/"})
    public String browseBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Boolean popular,
            @RequestParam(required = false) Boolean recent,
            Authentication authentication,
            Model model) {

        logger.debug("Browsing books - page: {}, size: {}, keyword: {}, genreId: {}, popular: {}, recent: {}", 
                    page, size, keyword, genreId, popular, recent);

        // Ensure size doesn't exceed reasonable limit
        if (size > 50) {
            size = DEFAULT_PAGE_SIZE;
        }

        Pageable pageable;
        Page<BookDto> bookPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            bookPage = bookService.searchBooks(keyword.trim(), pageable);
            model.addAttribute("keyword", keyword.trim());
        } else if (genreId != null) {
            pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            bookPage = bookService.getBooksByGenre(genreId, pageable);
            model.addAttribute("genreId", genreId);
        } else if (popular != null && popular) {
            // Popular books page - show popular books first
            pageable = PageRequest.of(page, size);
            bookPage = bookService.getPopularBooks(pageable);
            model.addAttribute("popular", true);
        } else if (recent != null && recent) {
            // Recently added books page - show newest books first
            pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            bookPage = bookService.getAllBooks(pageable); // Already sorted by createdAt DESC
            model.addAttribute("recent", true);
        } else {
            pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            bookPage = bookService.getAllBooks(pageable);
        }

        // Add genres for filter sidebar
        model.addAttribute("genres", genreService.getAllGenres());
        model.addAttribute("bookPage", bookPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("totalItems", bookPage.getTotalElements());

        // Add cart item count for authenticated users
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Integer cartItemCount = cartService.getCartItemCount(userDetails.getUserId());
            model.addAttribute("cartItemCount", cartItemCount);
            model.addAttribute("isVerified", userDetails.isVerified());
        }

        return "books/catalog";
    }

    /**
     * View book details.
     * Public access - no authentication required.
     */
    @GetMapping("/{id}")
    public String viewBookDetails(@PathVariable Long id, 
                                   @RequestParam(defaultValue = "0") int reviewPage,
                                   Authentication authentication, 
                                   Model model) {
        logger.debug("Viewing book details for ID: {}", id);

        try {
            BookDto book = bookService.getBookById(id);
            model.addAttribute("book", book);

            Long currentUserId = null;
            boolean hasReviewed = false;
            ReviewDto userReview = null;
            boolean purchased = false;

            // Add cart item count for authenticated users
            if (authentication != null && authentication.isAuthenticated()) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                currentUserId = userDetails.getUserId();

                // Check if user is verified or not
                boolean isVerified = userDetails.isVerified();
                model.addAttribute("isVerified", isVerified);

                // Check if user has a delivered order for the current book (users can only post reviews for purchased books)
                purchased = orderService.verifyIfExistOrderItemForDeliveredOrder(id, currentUserId);

                if (isVerified) {
                    Integer cartItemCount = cartService.getCartItemCount(currentUserId);
                    model.addAttribute("cartItemCount", cartItemCount);
                    
                    // Check if user already reviewed this book
                    userReview = reviewService.getUserReviewForBook(currentUserId, id);
                    hasReviewed = (userReview != null); 
                    model.addAttribute("userReview", userReview);
                                   
                    // Only set userRole if userReview exists to avoid NPE
                    if (userReview != null) {
                        model.addAttribute("userRole", userReview.getUserRole());
                    }
                }
            } else {
                // For guest users, explicitly set isVerified to false
                model.addAttribute("isVerified", false);
            }

            model.addAttribute("purchased", purchased);
            
            // Always set hasReviewed to prevent null SpEL errors in template
            model.addAttribute("hasReviewed", hasReviewed);

            // Add review statistics
            Double averageRating = reviewService.getAverageRating(id);
            Long reviewCount = reviewService.getReviewCount(id);
            Map<Integer, Long> ratingDistribution = reviewService.getRatingDistribution(id);

            model.addAttribute("averageRating", averageRating);
            model.addAttribute("reviewCount", reviewCount);
            model.addAttribute("ratingDistribution", ratingDistribution);

            // Add reviews with pagination (10 per page, sorted by most liked)
            Pageable pageable = PageRequest.of(reviewPage, 10);
            Page<ReviewDto> reviewsPage = reviewService.getReviewsByBookId(id, pageable, currentUserId);
            
            model.addAttribute("reviewsPage", reviewsPage);
            model.addAttribute("currentReviewPage", reviewPage);

            return "books/details";
        } catch (IllegalArgumentException e) {
            logger.warn("Book not found: {}", id);
            model.addAttribute("error", "Book not found");
            return "error";
        }
    }

    /**
     * Submit a new review for a book.
     * Requires authentication.
     */
    @PostMapping("/{id}/reviews/submit")
    public String submitReview(@PathVariable Long id,
                                @Valid @ModelAttribute ReviewDto reviewDto,
                                BindingResult bindingResult,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to submit a review");
            return "redirect:/login";
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        logger.debug("User {} submitting review for book {}", userId, id);

        // Validation
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please provide a valid rating (1-5 stars)");
            return "redirect:/books/" + id + "#reviews";
        }

        try {
            reviewDto.setBookId(id);
            reviewService.createReview(reviewDto, userId);
            redirectAttributes.addFlashAttribute("success", "Your review has been submitted successfully!");
            logger.info("Review submitted successfully by user {} for book {}", userId, id);
        } catch (IllegalArgumentException e) {
            logger.warn("Review submission failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            logger.error("Error submitting review", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while submitting your review");
        }

        return "redirect:/books/" + id + "#reviews";
    }

    /**
     * Update an existing review.
     * Requires authentication and ownership.
     */
    @PostMapping("/{bookId}/reviews/{reviewId}/update")
    public String updateReview(@PathVariable Long bookId,
                                @PathVariable Long reviewId,
                                @Valid @ModelAttribute ReviewDto reviewDto,
                                BindingResult bindingResult,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to update a review");
            return "redirect:/login";
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        logger.debug("User {} updating review {} for book {}", userId, reviewId, bookId);

        // Validation
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please provide a valid rating (1-5 stars)");
            return "redirect:/books/" + bookId + "#reviews";
        }

        try {
            reviewDto.setBookId(bookId);
            reviewService.updateReview(reviewId, reviewDto, userId);
            redirectAttributes.addFlashAttribute("success", "Your review has been updated successfully!");
            logger.info("Review {} updated successfully by user {}", reviewId, userId);
        } catch (IllegalArgumentException e) {
            logger.warn("Review update failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating review", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred while updating your review");
        }

        return "redirect:/books/" + bookId + "#reviews";
    }

    /**
     * Like a review.
     * Requires authentication.
     */
    @PostMapping("/{bookId}/reviews/{reviewId}/like")
    public String likeReview(@PathVariable Long bookId,
                              @PathVariable Long reviewId,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to like a review");
            return "redirect:/login";
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        logger.debug("User {} liking review {}", userId, reviewId);

        try {
            reviewService.likeReview(reviewId, userId);
            logger.info("Review {} liked by user {}", reviewId, userId);
        } catch (Exception e) {
            logger.error("Error liking review", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred");
        }

        return "redirect:/books/" + bookId + "#review-" + reviewId;
    }

    /**
     * Dislike a review.
     * Requires authentication.
     */
    @PostMapping("/{bookId}/reviews/{reviewId}/dislike")
    public String dislikeReview(@PathVariable Long bookId,
                                  @PathVariable Long reviewId,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to dislike a review");
            return "redirect:/login";
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        logger.debug("User {} disliking review {}", userId, reviewId);

        try {
            reviewService.dislikeReview(reviewId, userId);
            logger.info("Review {} disliked by user {}", reviewId, userId);
        } catch (Exception e) {
            logger.error("Error disliking review", e);
            redirectAttributes.addFlashAttribute("error", "An error occurred");
        }

        return "redirect:/books/" + bookId + "#review-" + reviewId;
    }
}

