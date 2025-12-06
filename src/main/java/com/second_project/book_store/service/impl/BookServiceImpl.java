package com.second_project.book_store.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.second_project.book_store.entity.Book;
import com.second_project.book_store.entity.BookDetail;
import com.second_project.book_store.entity.Genre;
import com.second_project.book_store.model.BookDto;
import com.second_project.book_store.repository.BookRepository;
import com.second_project.book_store.repository.GenreRepository;
import com.second_project.book_store.repository.ReviewRepository;
import com.second_project.book_store.service.BookService;

/**
 * Implementation of BookService.
 * 
 * BEST PRACTICES:
 * - @Transactional(readOnly=true) by default for performance
 * - Override with @Transactional for write operations
 * - Use DTOs to avoid lazy loading issues
 * - Log important operations
 */
@Service
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);

    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;
    private final ReviewRepository reviewRepository;

    public BookServiceImpl(BookRepository bookRepository, 
                          GenreRepository genreRepository,
                          ReviewRepository reviewRepository) {
        this.bookRepository = bookRepository;
        this.genreRepository = genreRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional
    public BookDto createBook(BookDto bookDto) {
        logger.info("Creating new book: {}", bookDto.getTitle());

        // Normalize ISBN first
        String normalizedIsbn = normalizeIsbn(bookDto.getIsbn());
        
        // Validate ISBN uniqueness (only if not null)
        if (normalizedIsbn != null) {
            if (bookRepository.existsByIsbn(normalizedIsbn)) {
                throw new IllegalArgumentException("ISBN already exists: " + normalizedIsbn);
            }
        }

        // Create Book entity
        Book book = new Book();
        book.setTitle(bookDto.getTitle());
        book.setAuthor(bookDto.getAuthor());
        book.setIsbn(normalizedIsbn);

        // Create BookDetail entity
        BookDetail bookDetail = new BookDetail();
        bookDetail.setDescription(bookDto.getDescription());
        bookDetail.setImageUrl(bookDto.getImageUrl());
        bookDetail.setPrice(bookDto.getPrice());
        bookDetail.setQuantity(bookDto.getQuantity());
        bookDetail.setPublisher(bookDto.getPublisher());
        bookDetail.setPublishDate(bookDto.getPublishDate());

        // Set bidirectional relationship
        book.setBookDetail(bookDetail);
        bookDetail.setBook(book);

        // Set genres
        if (bookDto.getGenreIds() != null && !bookDto.getGenreIds().isEmpty()) {
            Set<Genre> genres = new HashSet<>();
            for (Long genreId : bookDto.getGenreIds()) {
                Genre genre = genreRepository.findById(genreId)
                    .orElseThrow(() -> new IllegalArgumentException("Genre not found: " + genreId));
                genres.add(genre);
            }
            book.setGenres(genres);
        }

        // Save (cascades to BookDetail)
        Book savedBook = bookRepository.save(book);

        logger.info("Book created successfully with ID: {}", savedBook.getBookId());

        return convertToDto(savedBook);
    }

    @Override
    @Transactional
    public BookDto updateBook(Long bookId, BookDto bookDto) {
        logger.info("Updating book with ID: {}", bookId);

        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));

        // Normalize ISBN first
        String normalizedIsbn = normalizeIsbn(bookDto.getIsbn());
        
        // Validate ISBN uniqueness (exclude current book, only if not null)
        if (normalizedIsbn != null) {
            if (bookRepository.existsByIsbnAndBookIdNot(normalizedIsbn, bookId)) {
                throw new IllegalArgumentException("ISBN already exists: " + normalizedIsbn);
            }
        }

        // Update Book fields
        book.setTitle(bookDto.getTitle());
        book.setAuthor(bookDto.getAuthor());
        book.setIsbn(normalizedIsbn);

        // Update BookDetail fields
        BookDetail bookDetail = book.getBookDetail();
        if (bookDetail == null) {
            bookDetail = new BookDetail();
            book.setBookDetail(bookDetail);
            bookDetail.setBook(book);
        }
        bookDetail.setDescription(bookDto.getDescription());
        bookDetail.setImageUrl(bookDto.getImageUrl());
        bookDetail.setPrice(bookDto.getPrice());
        bookDetail.setQuantity(bookDto.getQuantity());
        bookDetail.setPublisher(bookDto.getPublisher());
        bookDetail.setPublishDate(bookDto.getPublishDate());

        // Update genres
        if (bookDto.getGenreIds() != null) {
            book.getGenres().clear();
            for (Long genreId : bookDto.getGenreIds()) {
                Genre genre = genreRepository.findById(genreId)
                    .orElseThrow(() -> new IllegalArgumentException("Genre not found: " + genreId));
                book.getGenres().add(genre);
            }
        }

        Book updatedBook = bookRepository.save(book);

        logger.info("Book updated successfully: {}", bookId);

        return convertToDto(updatedBook);
    }

    @Override
    public BookDto getBookById(Long bookId) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));
        return convertToDto(book);
    }

    @Override
    public Book getBookEntityById(Long bookId) {
        return bookRepository.findById(bookId)
            .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));
    }

    @Override
    public Page<BookDto> getAllBooks(Pageable pageable) {
        // Check if sorting requires BookDetail fields (price or quantity)
        if (requiresBookDetailSort(pageable)) {
            Page<Book> books = bookRepository.findAllWithBookDetail(pageable);
            return books.map(this::convertToDto);
        } else {
            Page<Book> books = bookRepository.findAll(pageable);
            return books.map(this::convertToDto);
        }
    }

    @Override
    public Page<BookDto> searchBooks(String keyword, Pageable pageable) {
        // Check if sorting requires BookDetail fields (price or quantity)
        if (requiresBookDetailSort(pageable)) {
            Page<Book> books = bookRepository.searchByKeywordWithBookDetail(keyword, pageable);
            return books.map(this::convertToDto);
        } else {
            Page<Book> books = bookRepository.searchByKeyword(keyword, pageable);
            return books.map(this::convertToDto);
        }
    }

    @Override
    public Page<BookDto> getBooksByGenre(Long genreId, Pageable pageable) {
        // Check if sorting requires BookDetail fields (price or quantity)
        if (requiresBookDetailSort(pageable)) {
            Page<Book> books = bookRepository.findByGenreIdWithBookDetail(genreId, pageable);
            return books.map(this::convertToDto);
        } else {
            Page<Book> books = bookRepository.findByGenreId(genreId, pageable);
            return books.map(this::convertToDto);
        }
    }

    /**
     * Check if the Pageable's Sort requires BookDetail fields (price or quantity).
     * This determines whether we need to use repository methods with explicit JOINs.
     * 
     * @param pageable Pageable with Sort
     * @return true if sorting by bookDetail.price or bookDetail.quantity
     */
    private boolean requiresBookDetailSort(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return false;
        }
        
        return pageable.getSort().stream()
            .anyMatch(order -> {
                String property = order.getProperty();
                return "bookDetail.price".equals(property) || 
                       "bookDetail.quantity".equals(property) ||
                       "price".equals(property) || 
                       "stock".equals(property);
            });
    }

    @Override
    @Transactional
    public void deleteBook(Long bookId) {
        logger.info("Deleting book with ID: {}", bookId);

        if (!bookRepository.existsById(bookId)) {
            throw new IllegalArgumentException("Book not found: " + bookId);
        }

        // BEST PRACTICE: Consider soft delete in production
        // For now, we do hard delete (cascades to BookDetail)
        bookRepository.deleteById(bookId);

        logger.info("Book deleted successfully: {}", bookId);
    }

    @Override
    public List<BookDto> getLowStockBooks(Integer threshold) {
        List<Book> books = bookRepository.findLowStockBooks(threshold);
        return books.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateBookStock(Long bookId, Integer quantity) {
        logger.info("Updating stock for book ID {}: new quantity = {}", bookId, quantity);

        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));

        if (book.getBookDetail() != null) {
            book.getBookDetail().setQuantity(quantity);
            bookRepository.save(book);
        }
    }

    @Override
    public boolean isbnExists(String isbn) {
        return bookRepository.existsByIsbn(isbn);
    }

    @Override
    public boolean isbnExistsForDifferentBook(String isbn, Long bookId) {
        return bookRepository.existsByIsbnAndBookIdNot(isbn, bookId);
    }

    @Override
    public List<BookDto> getRecentlyAddedBooks(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<Book> books = bookRepository.findRecentlyAddedBooks(pageable);
        return books.getContent().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<BookDto> getPopularBooks(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<Book> books = bookRepository.findPopularBooks(pageable);
        return books.getContent().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    public Page<BookDto> getPopularBooks(Pageable pageable) {
        Page<Book> books = bookRepository.findPopularBooks(pageable);
        return books.map(this::convertToDto);
    }

    /**
     * Convert Book entity to BookDto.
     * 
     * BEST PRACTICE: Use mapper libraries like MapStruct in production
     * for complex mappings.
     */
    private BookDto convertToDto(Book book) {
        BookDto dto = new BookDto();
        dto.setBookId(book.getBookId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setIsbn(book.getIsbn());
        dto.setCreatedAt(book.getCreatedAt());
        dto.setUpdatedAt(book.getUpdatedAt());
        dto.setVersion(book.getVersion());

        // Set BookDetail fields
        if (book.getBookDetail() != null) {
            BookDetail detail = book.getBookDetail();
            dto.setDescription(detail.getDescription());
            dto.setImageUrl(detail.getImageUrl());
            dto.setPrice(detail.getPrice());
            dto.setQuantity(detail.getQuantity());
            dto.setPublisher(detail.getPublisher());
            dto.setPublishDate(detail.getPublishDate());
        }

        // Set genre IDs
        if (book.getGenres() != null && !book.getGenres().isEmpty()) {
            Set<Long> genreIds = book.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
            dto.setGenreIds(genreIds);
        }

        // Set review statistics
        dto.setAverageRating(reviewRepository.getAverageRatingForBook(book.getBookId()));
        dto.setReviewCount(reviewRepository.countByBook_BookId(book.getBookId()));

        return dto;
    }

    /**
     * Normalize ISBN: convert empty strings to null.
     * This prevents unique constraint violations since databases allow multiple NULLs
     * but not duplicate empty strings.
     * 
     * @param isbn The ISBN string (can be null, empty, or blank)
     * @return null if ISBN is null/empty/blank, otherwise trimmed ISBN
     */
    private String normalizeIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return null;
        }
        return isbn.trim();
    }
}

