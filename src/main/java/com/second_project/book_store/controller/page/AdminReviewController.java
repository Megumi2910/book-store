package com.second_project.book_store.controller.page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

import com.second_project.book_store.entity.Book;
import com.second_project.book_store.entity.Order;
import com.second_project.book_store.entity.Order.OrderStatus;
import com.second_project.book_store.entity.OrderItem;
import com.second_project.book_store.entity.Review;
import com.second_project.book_store.entity.User;
import com.second_project.book_store.model.ReviewDto;
import com.second_project.book_store.repository.OrderItemRepository;
import com.second_project.book_store.repository.OrderRepository;
import com.second_project.book_store.repository.ReviewRepository;
import com.second_project.book_store.service.BookService;
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
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BookService bookService;

    public AdminReviewController(ReviewService reviewService,
                                ReviewRepository reviewRepository,
                                OrderRepository orderRepository,
                                OrderItemRepository orderItemRepository,
                                BookService bookService) {
        this.reviewService = reviewService;
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.bookService = bookService;
    }

    /**
     * List reviews with optional rating and book filters.
     */
    @GetMapping({"", "/"})
    public String listReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Long bookId,
            Model model) {

        logger.info("Admin listing reviews - page: {}, size: {}, rating: {}, bookId: {}", page, size, rating, bookId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReviewDto> reviewPage = reviewService.getAllReviews(pageable, rating, bookId);

        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviewPage.getTotalPages());
        model.addAttribute("totalItems", reviewPage.getTotalElements());
        model.addAttribute("selectedRating", rating);
        model.addAttribute("selectedBookId", bookId);
        model.addAttribute("pageTitle", "Reviews Management");

        // Add books for filter dropdown
        Pageable bookPageable = PageRequest.of(0, 1000, Sort.by("title").ascending());
        model.addAttribute("books", bookService.getAllBooks(bookPageable).getContent());

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

    /**
     * Manually create test reviews for books that have delivered orders.
     * Useful for testing when seeder doesn't run.
     */
    @PostMapping("/create-test-reviews")
    @Transactional
    public String createTestReviews(
            @RequestParam(defaultValue = "100") int count,
            RedirectAttributes redirectAttributes) {

        logger.info("Admin manually creating {} test reviews", count);

        try {
            // Get all delivered orders with orderItems eagerly fetched
            List<Order> deliveredOrders = orderRepository.findByOrderStatusWithItems(OrderStatus.DELIVERED);
            
            if (deliveredOrders.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", 
                    "No delivered orders found. Create some orders first.");
                return "redirect:/admin/reviews";
            }

            Random random = new Random();
            int created = 0;
            Set<String> reviewedPairs = new HashSet<>();
            String[] comments = {
                "Great book! Highly recommend.",
                "Amazing story, couldn't put it down.",
                "Good read, but could be better.",
                "Excellent writing and plot.",
                "One of the best books I've read.",
                "Well-written and engaging.",
                "Perfect for a weekend read.",
                "Brilliant character development.",
                "Loved every page!",
                "Beautiful prose and storytelling.",
                "A masterpiece of literature.",
                "Absolutely fantastic!",
                "Very engaging from start to finish.",
                "The characters are well-developed.",
                "Would definitely read again."
            };

            // Create reviews from delivered orders
            // Allow multiple reviews for the same book (from different users) for pagination testing
            for (Order order : deliveredOrders) {
                if (created >= count) break;
                
                User user = order.getUser();
                for (OrderItem item : order.getOrderItems()) {
                    if (created >= count) break;
                    
                    Book book = item.getBook();
                    String reviewKey = user.getUserId() + "_" + book.getBookId();
                    
                    // Only skip if THIS user already reviewed THIS book in this batch
                    // This allows multiple users to review the same book (good for pagination testing)
                    if (reviewedPairs.contains(reviewKey)) {
                        continue;
                    }
                    
                    // Check if this specific user-book pair already exists in DB
                    // But allow creating multiple reviews for the same book from different users
                    if (reviewRepository.existsByUser_UserIdAndBook_BookId(user.getUserId(), book.getBookId())) {
                        continue;
                    }
                    
                    Review review = new Review();
                    review.setUser(user);
                    review.setBook(book);
                    
                    // Rating distribution: 60% 4-5 stars, 30% 3 stars, 10% 1-2 stars
                    int rating;
                    double ratingRoll = random.nextDouble();
                    if (ratingRoll < 0.6) {
                        rating = random.nextBoolean() ? 5 : 4;
                    } else if (ratingRoll < 0.9) {
                        rating = 3;
                    } else {
                        rating = random.nextBoolean() ? 1 : 2;
                    }
                    review.setRating(rating);
                    
                    // 85% have comments
                    if (random.nextDouble() > 0.15) {
                        review.setComment(comments[random.nextInt(comments.length)]);
                    }
                    
                    reviewRepository.save(review);
                    reviewedPairs.add(reviewKey);
                    created++;
                }
            }
            
            // If we haven't created enough reviews yet, create more for popular books
            // This ensures we get many reviews on the same books for pagination testing
            if (created < count && !deliveredOrders.isEmpty()) {
                logger.info("Creating additional reviews to reach target count (for pagination testing)...");
                
                // Get all books from delivered orders
                Set<Book> booksToReview = new HashSet<>();
                for (Order order : deliveredOrders) {
                    for (OrderItem item : order.getOrderItems()) {
                        booksToReview.add(item.getBook());
                    }
                }
                
                // Get all users who have delivered orders
                Set<User> usersWithOrders = new HashSet<>();
                for (Order order : deliveredOrders) {
                    usersWithOrders.add(order.getUser());
                }
                
                // Create additional reviews by mixing users and books
                List<Book> bookList = new ArrayList<>(booksToReview);
                List<User> userList = new ArrayList<>(usersWithOrders);
                
                int attempts = 0;
                int maxAttempts = count * 10; // Prevent infinite loop
                
                while (created < count && attempts < maxAttempts && !bookList.isEmpty() && !userList.isEmpty()) {
                    attempts++;
                    
                    Book book = bookList.get(random.nextInt(bookList.size()));
                    User user = userList.get(random.nextInt(userList.size()));
                    String reviewKey = user.getUserId() + "_" + book.getBookId();
                    
                    // Skip if this user already reviewed this book
                    if (reviewedPairs.contains(reviewKey) || 
                        reviewRepository.existsByUser_UserIdAndBook_BookId(user.getUserId(), book.getBookId())) {
                        continue;
                    }
                    
                    // Verify that this user actually has a delivered order containing this book
                    // This ensures business logic: only users who purchased the book can review it
                    if (!orderItemRepository.existsByBook_BookIdAndOrder_User_UserIdAndOrder_OrderStatus(
                            book.getBookId(), user.getUserId(), OrderStatus.DELIVERED)) {
                        continue; // Skip this combination - user hasn't purchased this book
                    }
                    
                    Review review = new Review();
                    review.setUser(user);
                    review.setBook(book);
                    
                    // Rating distribution
                    int rating;
                    double ratingRoll = random.nextDouble();
                    if (ratingRoll < 0.6) {
                        rating = random.nextBoolean() ? 5 : 4;
                    } else if (ratingRoll < 0.9) {
                        rating = 3;
                    } else {
                        rating = random.nextBoolean() ? 1 : 2;
                    }
                    review.setRating(rating);
                    
                    // 85% have comments
                    if (random.nextDouble() > 0.15) {
                        review.setComment(comments[random.nextInt(comments.length)]);
                    }
                    
                    reviewRepository.save(review);
                    reviewedPairs.add(reviewKey);
                    created++;
                }
            }

            redirectAttributes.addFlashAttribute("success", 
                String.format("Successfully created %d test reviews!", created));
            logger.info("Created {} test reviews", created);
            
        } catch (Exception e) {
            logger.error("Error creating test reviews", e);
            redirectAttributes.addFlashAttribute("error", 
                "Failed to create test reviews: " + e.getMessage());
        }

        return "redirect:/admin/reviews";
    }
}



