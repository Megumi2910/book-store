package com.second_project.book_store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.second_project.book_store.entity.Book;

/**
 * Repository interface for Book entity.
 * Provides CRUD operations and custom queries for book management.
 * 
 * BEST PRACTICE: Use projections and DTOs for complex queries to avoid N+1 problems.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Find book by ISBN (unique identifier).
     * 
     * @param isbn The ISBN-13 code
     * @return Optional containing the book if found
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Find all books with pagination support.
     * Use for admin book list with sorting.
     * 
     * @param pageable Pagination and sorting parameters
     * @return Page of books
     */
    Page<Book> findAll(Pageable pageable);

    /**
     * Search books by title (case-insensitive, partial match).
     * 
     * @param title Search term
     * @param pageable Pagination parameters
     * @return Page of matching books
     */
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Book> searchByTitle(@Param("title") String title, Pageable pageable);

    /**
     * Search books by author (case-insensitive, partial match).
     * 
     * @param author Search term
     * @param pageable Pagination parameters
     * @return Page of matching books
     */
    @Query("SELECT b FROM Book b WHERE LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))")
    Page<Book> searchByAuthor(@Param("author") String author, Pageable pageable);

    /**
     * Search books by title or author.
     * 
     * @param keyword Search term
     * @param pageable Pagination parameters
     * @return Page of matching books
     */
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find books by genre.
     * 
     * @param genreId Genre ID
     * @param pageable Pagination parameters
     * @return Page of books in the genre
     */
    @Query("SELECT b FROM Book b JOIN b.genres g WHERE g.id = :genreId")
    Page<Book> findByGenreId(@Param("genreId") Long genreId, Pageable pageable);

    /**
     * Find books with low stock (quantity below threshold).
     * Useful for admin dashboard alerts.
     * 
     * @param threshold Stock threshold
     * @return List of books with low stock
     */
    @Query("SELECT b FROM Book b JOIN b.bookDetail bd WHERE bd.quantity < :threshold")
    List<Book> findLowStockBooks(@Param("threshold") Integer threshold);

    /**
     * Count total books in catalog.
     * 
     * @return Total book count
     */
    @Query("SELECT COUNT(b) FROM Book b")
    Long countTotalBooks();

    /**
     * Get total inventory value (sum of price * quantity for all books).
     * 
     * @return Total inventory value
     */
    @Query("SELECT COALESCE(SUM(bd.price * bd.quantity), 0) FROM BookDetail bd")
    Double getTotalInventoryValue();

    /**
     * Check if ISBN already exists (for validation during add/edit).
     * 
     * @param isbn ISBN to check
     * @return true if exists
     */
    boolean existsByIsbn(String isbn);

    /**
     * Check if ISBN exists for a different book (for edit validation).
     * 
     * @param isbn ISBN to check
     * @param bookId Current book ID (exclude from check)
     * @return true if exists for different book
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Book b " +
           "WHERE b.isbn = :isbn AND b.bookId != :bookId")
    boolean existsByIsbnAndBookIdNot(@Param("isbn") String isbn, @Param("bookId") Long bookId);
}

