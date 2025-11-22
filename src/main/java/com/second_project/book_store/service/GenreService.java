package com.second_project.book_store.service;

import java.util.List;

import com.second_project.book_store.entity.Genre;
import com.second_project.book_store.model.GenreDto;

/**
 * Service interface for Genre management.
 * Provides business logic for genre operations.
 */
public interface GenreService {

    /**
     * Create a new genre.
     * 
     * @param genreDto Genre data
     * @return Created genre DTO
     */
    GenreDto createGenre(GenreDto genreDto);

    /**
     * Update an existing genre.
     * 
     * @param genreId Genre ID
     * @param genreDto Updated genre data
     * @return Updated genre DTO
     */
    GenreDto updateGenre(Long genreId, GenreDto genreDto);

    /**
     * Get genre by ID.
     * 
     * @param genreId Genre ID
     * @return Genre DTO
     */
    GenreDto getGenreById(Long genreId);

    /**
     * Get genre entity by ID (for internal use).
     * 
     * @param genreId Genre ID
     * @return Genre entity
     */
    Genre getGenreEntityById(Long genreId);

    /**
     * Get all genres sorted by name.
     * 
     * @return List of genres
     */
    List<GenreDto> getAllGenres();

    /**
     * Delete a genre.
     * Throws exception if genre has books.
     * 
     * @param genreId Genre ID
     */
    void deleteGenre(Long genreId);

    /**
     * Check if genre name already exists.
     * 
     * @param name Genre name
     * @return true if exists
     */
    boolean genreNameExists(String name);

    /**
     * Check if genre name exists for a different genre.
     * 
     * @param name Genre name
     * @param genreId Current genre ID
     * @return true if exists for different genre
     */
    boolean genreNameExistsForDifferent(String name, Long genreId);

    /**
     * Count books in a genre.
     * 
     * @param genreId Genre ID
     * @return Number of books
     */
    Long countBooksInGenre(Long genreId);
}

