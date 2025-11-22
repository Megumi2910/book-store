package com.second_project.book_store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.second_project.book_store.entity.Genre;

/**
 * Repository interface for Genre entity.
 * Provides CRUD operations for genre management.
 */
@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

    /**
     * Find genre by name (case-insensitive).
     * 
     * @param name Genre name
     * @return Optional containing the genre if found
     */
    @Query("SELECT g FROM Genre g WHERE LOWER(g.name) = LOWER(:name)")
    Optional<Genre> findByNameIgnoreCase(@Param("name") String name);

    /**
     * Check if genre name already exists (for validation).
     * 
     * @param name Genre name
     * @return true if exists
     */
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM Genre g " +
           "WHERE LOWER(g.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    /**
     * Check if genre name exists for a different genre (for edit validation).
     * 
     * @param name Genre name
     * @param id Current genre ID (exclude from check)
     * @return true if exists for different genre
     */
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM Genre g " +
           "WHERE LOWER(g.name) = LOWER(:name) AND g.id != :id")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("id") Long id);

    /**
     * Get all genres ordered by name.
     * 
     * @return List of genres sorted alphabetically
     */
    List<Genre> findAllByOrderByNameAsc();

    /**
     * Count books in a genre.
     * Used to prevent deletion of genres with books.
     * 
     * @param genreId Genre ID
     * @return Number of books in the genre
     */
    @Query("SELECT COUNT(b) FROM Book b JOIN b.genres g WHERE g.id = :genreId")
    Long countBooksByGenreId(@Param("genreId") Long genreId);
}

