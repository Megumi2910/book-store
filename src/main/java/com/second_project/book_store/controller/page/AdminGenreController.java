package com.second_project.book_store.controller.page;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.second_project.book_store.model.GenreDto;
import com.second_project.book_store.service.GenreService;

import jakarta.validation.Valid;

/**
 * Controller for admin genre management pages.
 * 
 * Handles:
 * - Genre list
 * - Add new genre
 * - Edit existing genre
 * - Delete genre
 */
@Controller
@RequestMapping("/admin/genres")
@PreAuthorize("hasRole('ADMIN')")
public class AdminGenreController {

    private static final Logger logger = LoggerFactory.getLogger(AdminGenreController.class);

    private final GenreService genreService;

    public AdminGenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    /**
     * List all genres.
     */
    @GetMapping({"", "/"})
    public String listGenres(Model model) {
        logger.info("Admin listing genres");

        try {
            List<GenreDto> genres = genreService.getAllGenres();
            model.addAttribute("genres", genres);
            
            // Calculate stats for display
            if (genres != null && !genres.isEmpty()) {
                long totalBooks = genres.stream()
                    .mapToLong(g -> g.getBookCount() != null ? g.getBookCount() : 0L)
                    .sum();
                double avgBooksPerGenre = (double) totalBooks / genres.size();
                
                model.addAttribute("totalBooks", totalBooks);
                model.addAttribute("avgBooksPerGenre", avgBooksPerGenre);
            }
            
            // Add empty DTO for inline add form (optional)
            model.addAttribute("genreDto", new GenreDto());

            return "admin/genres/list";
        } catch (Exception e) {
            logger.error("Error listing genres", e);
            model.addAttribute("error", "Failed to load genres");
            return "admin/genres/list";
        }
    }

    /**
     * Show form to add new genre.
     */
    @GetMapping("/new")
    public String showAddGenreForm(Model model) {
        logger.info("Admin accessing add genre form");

        GenreDto genreDto = new GenreDto();
        model.addAttribute("genreDto", genreDto);
        model.addAttribute("isEdit", false);

        return "admin/genres/form";
    }

    /**
     * Process add genre form submission.
     */
    @PostMapping("/new")
    public String addGenre(
            @Valid @ModelAttribute("genreDto") GenreDto genreDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        logger.info("Admin adding new genre: {}", genreDto.getName());

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "admin/genres/form";
        }

        try {
            GenreDto savedGenre = genreService.createGenre(genreDto);
            
            redirectAttributes.addFlashAttribute("success", 
                "Genre '" + savedGenre.getName() + "' added successfully!");
            
            return "redirect:/admin/genres";
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error adding genre: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("isEdit", false);
            return "admin/genres/form";
        } catch (Exception e) {
            logger.error("Error adding genre", e);
            model.addAttribute("error", "Failed to add genre: " + e.getMessage());
            model.addAttribute("isEdit", false);
            return "admin/genres/form";
        }
    }

    /**
     * Show form to edit existing genre.
     */
    @GetMapping("/{id}/edit")
    public String showEditGenreForm(@PathVariable Long id, Model model) {
        logger.info("Admin accessing edit form for genre ID: {}", id);

        try {
            GenreDto genreDto = genreService.getGenreById(id);
            model.addAttribute("genreDto", genreDto);
            model.addAttribute("isEdit", true);

            return "admin/genres/form";
        } catch (Exception e) {
            logger.error("Error loading genre for edit", e);
            return "redirect:/admin/genres?error=Genre not found";
        }
    }

    /**
     * Process edit genre form submission.
     */
    @PostMapping("/{id}/edit")
    public String updateGenre(
            @PathVariable Long id,
            @Valid @ModelAttribute("genreDto") GenreDto genreDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        logger.info("Admin updating genre ID: {}", id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "admin/genres/form";
        }

        try {
            GenreDto updatedGenre = genreService.updateGenre(id, genreDto);
            
            redirectAttributes.addFlashAttribute("success", 
                "Genre '" + updatedGenre.getName() + "' updated successfully!");
            
            return "redirect:/admin/genres";
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error updating genre: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("isEdit", true);
            return "admin/genres/form";
        } catch (Exception e) {
            logger.error("Error updating genre", e);
            model.addAttribute("error", "Failed to update genre: " + e.getMessage());
            model.addAttribute("isEdit", true);
            return "admin/genres/form";
        }
    }

    /**
     * Delete genre.
     */
    @PostMapping("/{id}/delete")
    public String deleteGenre(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.info("Admin deleting genre ID: {}", id);

        try {
            GenreDto genre = genreService.getGenreById(id);
            
            // Check if genre has books
            Long bookCount = genreService.countBooksInGenre(id);
            if (bookCount > 0) {
                redirectAttributes.addFlashAttribute("error", 
                    "Cannot delete genre '" + genre.getName() + "' because it has " + 
                    bookCount + " book(s). Remove books from this genre first.");
                return "redirect:/admin/genres";
            }

            genreService.deleteGenre(id);
            
            redirectAttributes.addFlashAttribute("success", 
                "Genre '" + genre.getName() + "' deleted successfully!");
        } catch (IllegalStateException e) {
            logger.warn("Cannot delete genre: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting genre", e);
            redirectAttributes.addFlashAttribute("error", 
                "Failed to delete genre: " + e.getMessage());
        }

        return "redirect:/admin/genres";
    }
}

