package com.second_project.book_store.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.second_project.book_store.entity.Book;
import com.second_project.book_store.model.BookDto;

/**
 * Service interface for Book management.
 * Provides business logic for book operations.
 */
public interface BookService {

    /**
     * Create a new book with details.
     * 
     * @param bookDto Book data
     * @return Created book DTO
     */
    BookDto createBook(BookDto bookDto);

    /**
     * Update an existing book.
     * 
     * @param bookId Book ID
     * @param bookDto Updated book data
     * @return Updated book DTO
     */
    BookDto updateBook(Long bookId, BookDto bookDto);

    /**
     * Get book by ID.
     * 
     * @param bookId Book ID
     * @return Book DTO
     */
    BookDto getBookById(Long bookId);

    /**
     * Get book entity by ID (for internal use).
     * 
     * @param bookId Book ID
     * @return Book entity
     */
    Book getBookEntityById(Long bookId);

    /**
     * Get all books with pagination.
     * 
     * @param pageable Pagination parameters
     * @return Page of books
     */
    Page<BookDto> getAllBooks(Pageable pageable);

    /**
     * Search books by keyword (title or author).
     * 
     * @param keyword Search term
     * @param pageable Pagination parameters
     * @return Page of matching books
     */
    Page<BookDto> searchBooks(String keyword, Pageable pageable);

    /**
     * Search books by genre.
     * 
     * @param genreId Genre ID
     * @param pageable Pagination parameters
     * @return Page of books in genre
     */
    Page<BookDto> getBooksByGenre(Long genreId, Pageable pageable);

    /**
     * Delete a book (soft delete recommended).
     * 
     * @param bookId Book ID
     */
    void deleteBook(Long bookId);

    /**
     * Get books with low stock.
     * 
     * @param threshold Stock threshold
     * @return List of low stock books
     */
    List<BookDto> getLowStockBooks(Integer threshold);

    /**
     * Update book stock quantity.
     * 
     * @param bookId Book ID
     * @param quantity New quantity
     */
    void updateBookStock(Long bookId, Integer quantity);

    /**
     * Check if ISBN already exists.
     * 
     * @param isbn ISBN to check
     * @return true if exists
     */
    boolean isbnExists(String isbn);

    /**
     * Check if ISBN exists for a different book.
     * 
     * @param isbn ISBN to check
     * @param bookId Current book ID
     * @return true if exists for different book
     */
    boolean isbnExistsForDifferentBook(String isbn, Long bookId);

    /**
     * Get recently added books.
     * 
     * @param limit Maximum number of books to return
     * @return List of recently added books
     */
    List<BookDto> getRecentlyAddedBooks(int limit);

    /**
     * Get popular books (based on order count).
     * 
     * @param limit Maximum number of books to return
     * @return List of popular books
     */
    List<BookDto> getPopularBooks(int limit);

    /**
     * Get popular books with pagination.
     * 
     * @param pageable Pagination parameters
     * @return Page of popular books
     */
    Page<BookDto> getPopularBooks(Pageable pageable);
}

