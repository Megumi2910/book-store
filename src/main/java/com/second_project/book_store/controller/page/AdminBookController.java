package com.second_project.book_store.controller.page;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.second_project.book_store.entity.Genre;
import com.second_project.book_store.model.BookDto;
import com.second_project.book_store.model.GenreDto;
import com.second_project.book_store.service.BookService;
import com.second_project.book_store.service.GenreService;
import com.second_project.book_store.service.ImageUploadService;

import jakarta.validation.Valid;

/**
 * Controller for admin book management pages.
 * 
 * Handles:
 * - Book list with pagination and search
 * - Add new book
 * - Edit existing book
 * - View book details
 * - Delete book
 */
@Controller
@RequestMapping("/admin/books")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBookController {

    private static final Logger logger = LoggerFactory.getLogger(AdminBookController.class);
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final BookService bookService;
    private final GenreService genreService;
    private final ImageUploadService imageUploadService;

    public AdminBookController(BookService bookService, 
                              GenreService genreService,
                              ImageUploadService imageUploadService) {
        this.bookService = bookService;
        this.genreService = genreService;
        this.imageUploadService = imageUploadService;
    }

    /**
     * List all books with pagination and search.
     */
    @GetMapping({"", "/"})
    public String listBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long genreId,
            Model model) {
        
        logger.info("Admin listing books - page: {}, size: {}, keyword: {}, genreId: {}", 
                    page, size, keyword, genreId);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<BookDto> bookPage;

            if (keyword != null && !keyword.trim().isEmpty()) {
                bookPage = bookService.searchBooks(keyword, pageable);
                model.addAttribute("keyword", keyword);
            } else if (genreId != null) {
                bookPage = bookService.getBooksByGenre(genreId, pageable);
                model.addAttribute("genreId", genreId);
            } else {
                bookPage = bookService.getAllBooks(pageable);
            }

            model.addAttribute("bookPage", bookPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", bookPage.getTotalPages());
            model.addAttribute("totalItems", bookPage.getTotalElements());

            // Add genres for filter dropdown
            List<GenreDto> genres = genreService.getAllGenres();
            model.addAttribute("genres", genres);

            return "admin/books/list";
        } catch (Exception e) {
            logger.error("Error listing books", e);
            model.addAttribute("error", "Failed to load books");
            return "admin/books/list";
        }
    }

    /**
     * Show form to add new book.
     */
    @GetMapping("/new")
    public String showAddBookForm(Model model) {
        logger.info("Admin accessing add book form");

        BookDto bookDto = new BookDto();
        model.addAttribute("bookDto", bookDto);
        model.addAttribute("genres", genreService.getAllGenres());
        model.addAttribute("isEdit", false);

        return "admin/books/form";
    }

    /**
     * Process add book form submission.
     */
    @PostMapping("/new")
    public String addBook(
            @Valid @ModelAttribute("bookDto") BookDto bookDto,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        logger.info("Admin adding new book: {}", bookDto.getTitle());

        // Validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("genres", genreService.getAllGenres());
            model.addAttribute("isEdit", false);
            return "admin/books/form";
        }

        try {
            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = imageUploadService.uploadImage(imageFile, "books");
                bookDto.setImageUrl(imageUrl);
            } else if (bookDto.getImageUrl() == null || bookDto.getImageUrl().trim().isEmpty()) {
                // Use placeholder if no image provided (neither file nor manual URL)
                bookDto.setImageUrl(imageUploadService.getDefaultPlaceholderUrl());
            }
            // If user manually entered a URL and no file uploaded, keep the manual URL

            BookDto savedBook = bookService.createBook(bookDto);
            
            redirectAttributes.addFlashAttribute("success", 
                "Book '" + savedBook.getTitle() + "' added successfully!");
            
            return "redirect:/admin/books";
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error adding book: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("genres", genreService.getAllGenres());
            model.addAttribute("isEdit", false);
            return "admin/books/form";
        } catch (Exception e) {
            logger.error("Error adding book", e);
            model.addAttribute("error", "Failed to add book: " + e.getMessage());
            model.addAttribute("genres", genreService.getAllGenres());
            model.addAttribute("isEdit", false);
            return "admin/books/form";
        }
    }

    /**
     * Show form to edit existing book.
     */
    @GetMapping("/{id}/edit")
    public String showEditBookForm(@PathVariable Long id, Model model) {
        logger.info("Admin accessing edit form for book ID: {}", id);

        try {
            BookDto bookDto = bookService.getBookById(id);
            model.addAttribute("bookDto", bookDto);
            model.addAttribute("genres", genreService.getAllGenres());
            model.addAttribute("isEdit", true);

            return "admin/books/form";
        } catch (Exception e) {
            logger.error("Error loading book for edit", e);
            return "redirect:/admin/books?error=Book not found";
        }
    }

    /**
     * Process edit book form submission.
     */
    @PostMapping("/{id}/edit")
    public String updateBook(
            @PathVariable Long id,
            @Valid @ModelAttribute("bookDto") BookDto bookDto,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        logger.info("Admin updating book ID: {}", id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("genres", genreService.getAllGenres());
            model.addAttribute("isEdit", true);
            return "admin/books/form";
        }

        try {
            // Get existing book
            BookDto existingBook = bookService.getBookById(id);

            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                // Delete old image if it's not a placeholder
                if (existingBook.getImageUrl() != null && 
                    !existingBook.getImageUrl().equals(imageUploadService.getDefaultPlaceholderUrl())) {
                    imageUploadService.deleteImage(existingBook.getImageUrl());
                }
                
                String imageUrl = imageUploadService.uploadImage(imageFile, "books");
                bookDto.setImageUrl(imageUrl);
            } else {
                // If user manually entered a URL, use it; otherwise keep existing URL
                if (bookDto.getImageUrl() == null || bookDto.getImageUrl().trim().isEmpty()) {
                    bookDto.setImageUrl(existingBook.getImageUrl());
                }
                // If bookDto.getImageUrl() has a value, it means user manually entered it, so keep it
            }

            BookDto updatedBook = bookService.updateBook(id, bookDto);
            
            redirectAttributes.addFlashAttribute("success", 
                "Book '" + updatedBook.getTitle() + "' updated successfully!");
            
            return "redirect:/admin/books";
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error updating book: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("genres", genreService.getAllGenres());
            model.addAttribute("isEdit", true);
            return "admin/books/form";
        } catch (Exception e) {
            logger.error("Error updating book", e);
            model.addAttribute("error", "Failed to update book: " + e.getMessage());
            model.addAttribute("genres", genreService.getAllGenres());
            model.addAttribute("isEdit", true);
            return "admin/books/form";
        }
    }

    /**
     * View book details.
     */
    @GetMapping("/{id}")
    public String viewBook(@PathVariable Long id, Model model) {
        logger.info("Admin viewing book ID: {}", id);

        try {
            BookDto bookDto = bookService.getBookById(id);
            model.addAttribute("book", bookDto);

            // Get genre names for display
            List<String> genreNames = genreService.getAllGenres().stream()
                .filter(g -> bookDto.getGenreIds() != null && bookDto.getGenreIds().contains(g.getId()))
                .map(GenreDto::getName)
                .collect(Collectors.toList());
            model.addAttribute("genreNames", genreNames);

            return "admin/books/view";
        } catch (Exception e) {
            logger.error("Error viewing book", e);
            return "redirect:/admin/books?error=Book not found";
        }
    }

    /**
     * Delete book.
     */
    @PostMapping("/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.info("Admin deleting book ID: {}", id);

        try {
            BookDto book = bookService.getBookById(id);
            
            // Delete image if not placeholder
            if (book.getImageUrl() != null && 
                !book.getImageUrl().equals(imageUploadService.getDefaultPlaceholderUrl())) {
                imageUploadService.deleteImage(book.getImageUrl());
            }

            bookService.deleteBook(id);
            
            redirectAttributes.addFlashAttribute("success", 
                "Book '" + book.getTitle() + "' deleted successfully!");
        } catch (Exception e) {
            logger.error("Error deleting book", e);
            redirectAttributes.addFlashAttribute("error", 
                "Failed to delete book: " + e.getMessage());
        }

        return "redirect:/admin/books";
    }
}

