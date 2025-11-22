package com.second_project.book_store.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.second_project.book_store.entity.Genre;
import com.second_project.book_store.model.GenreDto;
import com.second_project.book_store.repository.GenreRepository;
import com.second_project.book_store.service.GenreService;

/**
 * Implementation of GenreService.
 */
@Service
@Transactional(readOnly = true)
public class GenreServiceImpl implements GenreService {

    private static final Logger logger = LoggerFactory.getLogger(GenreServiceImpl.class);

    private final GenreRepository genreRepository;

    public GenreServiceImpl(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Override
    @Transactional
    public GenreDto createGenre(GenreDto genreDto) {
        logger.info("Creating new genre: {}", genreDto.getName());

        // Validate uniqueness
        if (genreRepository.existsByNameIgnoreCase(genreDto.getName())) {
            throw new IllegalArgumentException("Genre name already exists: " + genreDto.getName());
        }

        Genre genre = new Genre();
        genre.setName(genreDto.getName());

        Genre savedGenre = genreRepository.save(genre);

        logger.info("Genre created successfully with ID: {}", savedGenre.getId());

        return convertToDto(savedGenre);
    }

    @Override
    @Transactional
    public GenreDto updateGenre(Long genreId, GenreDto genreDto) {
        logger.info("Updating genre with ID: {}", genreId);

        Genre genre = genreRepository.findById(genreId)
            .orElseThrow(() -> new IllegalArgumentException("Genre not found: " + genreId));

        // Validate uniqueness (exclude current genre)
        if (genreRepository.existsByNameIgnoreCaseAndIdNot(genreDto.getName(), genreId)) {
            throw new IllegalArgumentException("Genre name already exists: " + genreDto.getName());
        }

        genre.setName(genreDto.getName());

        Genre updatedGenre = genreRepository.save(genre);

        logger.info("Genre updated successfully: {}", genreId);

        return convertToDto(updatedGenre);
    }

    @Override
    public GenreDto getGenreById(Long genreId) {
        Genre genre = genreRepository.findById(genreId)
            .orElseThrow(() -> new IllegalArgumentException("Genre not found: " + genreId));
        return convertToDto(genre);
    }

    @Override
    public Genre getGenreEntityById(Long genreId) {
        return genreRepository.findById(genreId)
            .orElseThrow(() -> new IllegalArgumentException("Genre not found: " + genreId));
    }

    @Override
    public List<GenreDto> getAllGenres() {
        List<Genre> genres = genreRepository.findAllByOrderByNameAsc();
        return genres.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteGenre(Long genreId) {
        logger.info("Deleting genre with ID: {}", genreId);

        if (!genreRepository.existsById(genreId)) {
            throw new IllegalArgumentException("Genre not found: " + genreId);
        }

        // Check if genre has books
        Long bookCount = genreRepository.countBooksByGenreId(genreId);
        if (bookCount > 0) {
            throw new IllegalStateException("Cannot delete genre with " + bookCount + " books. Remove books first.");
        }

        genreRepository.deleteById(genreId);

        logger.info("Genre deleted successfully: {}", genreId);
    }

    @Override
    public boolean genreNameExists(String name) {
        return genreRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public boolean genreNameExistsForDifferent(String name, Long genreId) {
        return genreRepository.existsByNameIgnoreCaseAndIdNot(name, genreId);
    }

    @Override
    public Long countBooksInGenre(Long genreId) {
        return genreRepository.countBooksByGenreId(genreId);
    }

    /**
     * Convert Genre entity to GenreDto.
     */
    private GenreDto convertToDto(Genre genre) {
        GenreDto dto = new GenreDto();
        dto.setId(genre.getId());
        dto.setName(genre.getName());
        
        // Include book count
        Long bookCount = genreRepository.countBooksByGenreId(genre.getId());
        dto.setBookCount(bookCount);
        
        return dto;
    }
}

