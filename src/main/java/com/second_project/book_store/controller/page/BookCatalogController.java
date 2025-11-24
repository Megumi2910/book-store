package com.second_project.book_store.controller.page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.second_project.book_store.security.CustomUserDetails;

import com.second_project.book_store.model.BookDto;
import com.second_project.book_store.service.BookService;
import com.second_project.book_store.service.CartService;
import com.second_project.book_store.service.GenreService;

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

    public BookCatalogController(BookService bookService, GenreService genreService, CartService cartService) {
        this.bookService = bookService;
        this.genreService = genreService;
        this.cartService = cartService;
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
            Authentication authentication,
            Model model) {

        logger.debug("Browsing books - page: {}, size: {}, keyword: {}, genreId: {}, popular: {}", 
                    page, size, keyword, genreId, popular);

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
        }

        return "books/catalog";
    }

    /**
     * View book details.
     * Public access - no authentication required.
     */
    @GetMapping("/{id}")
    public String viewBookDetails(@PathVariable Long id, Authentication authentication, Model model) {
        logger.debug("Viewing book details for ID: {}", id);

        try {
            BookDto book = bookService.getBookById(id);
            model.addAttribute("book", book);

            // Add cart item count for authenticated users
            if (authentication != null && authentication.isAuthenticated()) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                Integer cartItemCount = cartService.getCartItemCount(userDetails.getUserId());
                model.addAttribute("cartItemCount", cartItemCount);
            }

            return "books/details";
        } catch (IllegalArgumentException e) {
            logger.warn("Book not found: {}", id);
            model.addAttribute("error", "Book not found");
            return "error";
        }
    }
}

