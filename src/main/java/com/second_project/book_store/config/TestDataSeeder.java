package com.second_project.book_store.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.second_project.book_store.entity.Book;
import com.second_project.book_store.entity.BookDetail;
import com.second_project.book_store.entity.Genre;
import com.second_project.book_store.entity.Order;
import com.second_project.book_store.entity.Order.OrderStatus;
import com.second_project.book_store.entity.OrderItem;
import com.second_project.book_store.entity.Payment;
import com.second_project.book_store.entity.Payment.PaymentMethod;
import com.second_project.book_store.entity.Payment.PaymentStatus;
import com.second_project.book_store.entity.Review;
import com.second_project.book_store.entity.User;
import com.second_project.book_store.entity.User.UserRole;
import com.second_project.book_store.repository.BookRepository;
import com.second_project.book_store.repository.GenreRepository;
import com.second_project.book_store.repository.OrderRepository;
import com.second_project.book_store.repository.ReviewRepository;
import com.second_project.book_store.repository.UserRepository;

/**
 * Test Data Seeder - Generates comprehensive test data for development/testing.
 * 
 * Creates:
 * - Multiple genres
 * - 50+ books across different genres
 * - 30+ test users (mix of verified/unverified)
 * - 100+ orders with various statuses
 * - 150+ reviews with different ratings
 * 
 * Only runs when 'dev' profile is active.
 * 
 * BEST PRACTICE: This is for development/testing only. Never run in production!
 */
@Configuration
@Profile("dev")
public class TestDataSeeder {

    private static final Logger logger = LoggerFactory.getLogger(TestDataSeeder.class);
    
    // Constructor to log when bean is created
    public TestDataSeeder() {
        logger.info("TestDataSeeder configuration class loaded (dev profile must be active)");
    }
    
    private static final String[] FIRST_NAMES = {
        "John", "Jane", "Alice", "Bob", "Charlie", "Diana", "Eve", "Frank",
        "Grace", "Henry", "Ivy", "Jack", "Kate", "Liam", "Mia", "Noah",
        "Olivia", "Paul", "Quinn", "Rachel", "Sam", "Tina", "Uma", "Victor",
        "Wendy", "Xander", "Yara", "Zoe"
    };
    
    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
        "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Wilson",
        "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Lee"
    };
    
    private static final String[] BOOK_TITLES = {
        "The Great Adventure", "Mystery of the Lost City", "Journey to the Stars",
        "Secrets of the Deep", "The Last Kingdom", "Echoes of Time", "Beyond the Horizon",
        "The Hidden Truth", "Winds of Change", "The Final Stand", "Shadows and Light",
        "The Ancient Code", "Rivers of Gold", "Mountains of Fire", "Desert Dreams",
        "Ocean's Call", "Forest Whispers", "City of Lights", "Valley of Shadows",
        "Island of Secrets", "The Forgotten Realm", "Chronicles of Power",
        "The Broken Crown", "Sword of Destiny", "Shield of Honor", "Arrow of Justice",
        "The Dark Prophecy", "Light of Hope", "Dawn of Ages", "Twilight's End",
        "The First Dawn", "Night's Embrace", "Day's Promise", "Season's Change",
        "The Winter King", "Summer's Heat", "Autumn's Fall", "Spring's Bloom",
        "The Eternal Flame", "Frozen Heart", "Burning Desire", "Cool Breeze",
        "The Silent Storm", "Thunder's Roar", "Lightning Strike", "Rain's Blessing",
        "The Golden Path", "Silver Lining", "Bronze Age", "Iron Will"
    };
    
    private static final String[] AUTHORS = {
        "J.K. Rowling", "George R.R. Martin", "Stephen King", "Agatha Christie",
        "Jane Austen", "Charles Dickens", "Mark Twain", "Ernest Hemingway",
        "F. Scott Fitzgerald", "Virginia Woolf", "Toni Morrison", "Harper Lee",
        "J.R.R. Tolkien", "Isaac Asimov", "Ray Bradbury", "Ursula K. Le Guin",
        "Margaret Atwood", "Neil Gaiman", "Terry Pratchett", "Brandon Sanderson",
        "Patrick Rothfuss", "Robin Hobb", "Joe Abercrombie", "Scott Lynch",
        "Brent Weeks", "Peter V. Brett", "N.K. Jemisin", "Octavia Butler"
    };
    
    private static final String[] GENRES = {
        "Fiction", "Mystery", "Thriller", "Romance", "Science Fiction",
        "Fantasy", "Horror", "Historical Fiction", "Biography", "Autobiography",
        "Memoir", "Self-Help", "Business", "Philosophy", "Poetry", "Drama",
        "Comedy", "Adventure", "Young Adult", "Children's", "Non-Fiction",
        "Cookbook", "Travel", "Art", "Music", "Sports", "Health", "Education"
    };

    @Bean
    CommandLineRunner seedTestData(
            UserRepository userRepository,
            GenreRepository genreRepository,
            BookRepository bookRepository,
            OrderRepository orderRepository,
            ReviewRepository reviewRepository,
            PasswordEncoder passwordEncoder) {
        
        logger.info("TestDataSeeder bean created - will run on startup if 'dev' profile is active");
        
        return args -> {
            logger.info("========================================");
            logger.info("=== STARTING TEST DATA SEEDING ===");
            logger.info("========================================");
            
            try {
                long existingBookCount = bookRepository.count();
                long existingReviewCount = reviewRepository.count();
                logger.info("Current counts - Books: {}, Reviews: {}", existingBookCount, existingReviewCount);
                
                // Get existing data for reviews/orders
                List<Book> existingBooks = bookRepository.findAll();
                List<User> existingUsers = userRepository.findAll();
                
                // 1. Create Genres (always ensure genres exist)
                logger.info("Creating/ensuring genres...");
                List<Genre> genres = createGenres(genreRepository);
                logger.info("Genres available: {}", genres.size());
                
                // 2. Create Test Users (only if we don't have enough)
                List<User> users = existingUsers;
                if (existingUsers.size() < 10) {
                    logger.info("Creating additional test users...");
                    List<User> newUsers = createTestUsers(userRepository, passwordEncoder);
                    users.addAll(newUsers);
                    logger.info("Total users now: {}", users.size());
                } else {
                    logger.info("Sufficient users exist ({}), skipping user creation", users.size());
                }
                
                // 3. Create Books (only if we don't have enough)
                List<Book> books = existingBooks;
                if (existingBookCount < 50) {
                    logger.info("Creating additional books...");
                    if (genres.isEmpty()) {
                        logger.error("Cannot create books without genres!");
                    } else {
                        List<Book> newBooks = createBooks(bookRepository, genres);
                        books = bookRepository.findAll(); // Refresh to get all books
                        logger.info("Created {} new books, total now: {}", newBooks.size(), books.size());
                    }
                } else {
                    logger.info("Sufficient books exist ({}), skipping book creation", existingBookCount);
                }
                
                // 4. Create Orders (always try to create if we have users and books)
                List<Order> orders = new ArrayList<>();
                if (users.isEmpty() || books.isEmpty()) {
                    logger.warn("Cannot create orders: users={}, books={}", users.size(), books.size());
                } else {
                    // Get existing orders
                    orders = orderRepository.findAll();
                    long existingOrderCount = orders.size();
                    
                    if (existingOrderCount < 50) {
                        logger.info("Creating additional orders...");
                        List<Order> newOrders = createOrders(orderRepository, users, books);
                        orders = orderRepository.findAll(); // Refresh to get all orders
                        logger.info("Created {} new orders, total now: {}", newOrders.size(), orders.size());
                    } else {
                        logger.info("Sufficient orders exist ({}), skipping order creation", existingOrderCount);
                    }
                }
                
                // 5. Create Reviews (always try to create if we have orders)
                if (orders.isEmpty()) {
                    logger.warn("No orders available, cannot create reviews");
                } else {
                    logger.info("Creating reviews from existing and new orders...");
                    int reviewCount = createReviews(reviewRepository, users, books, orders);
                    logger.info("Created {} reviews (total now: {})", reviewCount, reviewRepository.count());
                }
                
                logger.info("=== Test Data Seeding Complete ===");
                logger.info("Final Summary:");
                logger.info("  - Genres: {}", genres.size());
                logger.info("  - Users: {}", users.size());
                logger.info("  - Books: {}", books.size());
                logger.info("  - Orders: {}", orders.size());
                logger.info("  - Reviews: {}", reviewRepository.count());
                
            } catch (Exception e) {
                logger.error("========================================");
                logger.error("ERROR SEEDING TEST DATA", e);
                logger.error("Exception type: {}", e.getClass().getName());
                logger.error("Exception message: {}", e.getMessage());
                logger.error("Stack trace:", e);
                logger.error("========================================");
                e.printStackTrace();
                // Don't fail startup - just log the error
            } finally {
                logger.info("========================================");
                logger.info("=== TEST DATA SEEDING FINISHED ===");
                logger.info("========================================");
            }
        };
    }
    
    private List<Genre> createGenres(GenreRepository genreRepository) {
        List<Genre> genres = new ArrayList<>();
        for (String genreName : GENRES) {
            Genre genre = genreRepository.findByNameIgnoreCase(genreName)
                    .orElseGet(() -> {
                        // Create if doesn't exist
                        Genre newGenre = new Genre();
                        newGenre.setName(genreName);
                        return genreRepository.save(newGenre);
                    });
            genres.add(genre);
        }
        logger.info("Ensured {} genres exist (created new ones if needed)", genres.size());
        return genres;
    }
    
    private List<User> createTestUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        List<User> users = new ArrayList<>();
        Random random = new Random();
        
        // Create 30 test users
        for (int i = 0; i < 30; i++) {
            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            String email = String.format("user%d@test.com", i + 1);
            
            // Skip if user already exists
            if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
                continue;
            }
            
            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setPhoneNumber(String.format("0905%06d", 100000 + i));
            user.setAddress(String.format("Test Address %d, Test City", i + 1));
            user.setPassword(passwordEncoder.encode("Test@123")); // Same password for all test users
            user.setRole(UserRole.USER);
            user.setEnabled(random.nextDouble() > 0.3); // 70% verified
            
            users.add(userRepository.save(user));
        }
        
        return users;
    }
    
    private List<Book> createBooks(BookRepository bookRepository, List<Genre> genres) {
        List<Book> books = new ArrayList<>();
        Random random = new Random();
        
        // Create 50 books (or until we reach 50 total)
        long currentCount = bookRepository.count();
        int booksToCreate = (int) Math.max(0, 50 - currentCount);
        
        if (booksToCreate == 0) {
            logger.info("Already have 50 books, skipping book creation");
            return books;
        }
        
        logger.info("Creating {} more books to reach 50 total", booksToCreate);
        
        int booksCreated = 0;
        int attemptCount = 0;
        int maxAttempts = booksToCreate * 10; // Prevent infinite loop if all ISBNs are taken
        
        while (booksCreated < booksToCreate && attemptCount < maxAttempts) {
            attemptCount++;
            
            // Use a unique index to avoid duplicates, incrementing on each attempt
            int bookIndex = (int) currentCount + booksCreated + attemptCount;
            String title = BOOK_TITLES[bookIndex % BOOK_TITLES.length] + 
                          (bookIndex >= BOOK_TITLES.length ? " " + (bookIndex / BOOK_TITLES.length + 1) : "");
            String author = AUTHORS[random.nextInt(AUTHORS.length)];
            String isbn = String.format("978%010d", 1000000000L + bookIndex);
            
            // Skip if book with this ISBN already exists and try next ISBN
            if (bookRepository.findByIsbn(isbn).isPresent()) {
                logger.debug("Book with ISBN {} already exists, trying next ISBN", isbn);
                continue;
            }
            
            Book book = new Book();
            book.setTitle(title);
            book.setAuthor(author);
            book.setIsbn(isbn);
            
            // Assign 1-3 random genres
            Set<Genre> bookGenres = new HashSet<>();
            int genreCount = random.nextInt(3) + 1;
            for (int j = 0; j < genreCount; j++) {
                bookGenres.add(genres.get(random.nextInt(genres.size())));
            }
            book.setGenres(bookGenres);
            
            // Create BookDetail
            BookDetail detail = new BookDetail();
            detail.setDescription("This is a test book description for " + title + ". " +
                    "It contains interesting content that will engage readers.");
            detail.setPrice(BigDecimal.valueOf(50000 + random.nextInt(500000))); // 50k - 550k VND
            detail.setQuantity(random.nextInt(100) + 1); // 1-100 stock
            detail.setPublisher("Test Publisher " + (booksCreated % 5 + 1));
            detail.setPublishDate(LocalDateTime.now().minusYears(random.nextInt(5)).minusMonths(random.nextInt(12)).toLocalDate());
            detail.setBook(book);
            
            book.setBookDetail(detail);
            
            books.add(bookRepository.save(book));
            booksCreated++;
        }
        
        if (booksCreated < booksToCreate) {
            logger.warn("Only created {} out of {} requested books after {} attempts. " +
                       "Some ISBNs may already exist.", booksCreated, booksToCreate, attemptCount);
        } else {
            logger.info("Successfully created {} books", booksCreated);
        }
        
        return books;
    }
    
    private List<Order> createOrders(OrderRepository orderRepository, List<User> users, List<Book> books) {
        List<Order> orders = new ArrayList<>();
        Random random = new Random();
        
        if (users.isEmpty() || books.isEmpty()) {
            return orders;
        }
        
        // Create 100 orders - ensure at least 60% are DELIVERED for reviews
        for (int i = 0; i < 100; i++) {
            User user = users.get(random.nextInt(users.size()));
            // Bias towards DELIVERED status (60% chance) to enable more reviews
            OrderStatus status;
            if (random.nextDouble() < 0.6) {
                status = OrderStatus.DELIVERED;
            } else {
                // Select from non-DELIVERED statuses only to maintain 60% target
                OrderStatus[] nonDeliveredStatuses = Arrays.stream(OrderStatus.values())
                    .filter(s -> s != OrderStatus.DELIVERED)
                    .toArray(OrderStatus[]::new);
                status = nonDeliveredStatuses[random.nextInt(nonDeliveredStatuses.length)];
            }
            
            // Create order with 1-5 items
            Order order = new Order();
            order.setUser(user);
            order.setOrderStatus(status);
            order.setShippingAddress(user.getAddress());
            order.setOrderDate(LocalDateTime.now()
                    .minusDays(random.nextInt(60))
                    .minusHours(random.nextInt(24))
                    .minusMinutes(random.nextInt(60)));
            
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            int itemCount = random.nextInt(5) + 1;
            Set<Book> usedBooks = new HashSet<>();
            
            for (int j = 0; j < itemCount; j++) {
                Book book;
                do {
                    book = books.get(random.nextInt(books.size()));
                } while (usedBooks.contains(book));
                usedBooks.add(book);
                
                OrderItem item = new OrderItem();
                item.setBook(book);
                int quantity = random.nextInt(3) + 1;
                item.setQuantity(quantity);
                BigDecimal price = book.getBookDetail().getPrice();
                item.setPriceAtPurchase(price);
                
                // Use helper method to properly set bidirectional relationship
                order.addOrderItem(item);
                
                totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(quantity)));
            }
            
            order.setTotalAmount(totalAmount);
            
            // Create payment
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setPaymentMethod(PaymentMethod.values()[random.nextInt(PaymentMethod.values().length)]);
            payment.setPaymentStatus(status == OrderStatus.CANCELLED ? PaymentStatus.FAILED :
                    status == OrderStatus.DELIVERED ? PaymentStatus.PAID : PaymentStatus.PENDING);
            payment.setTransactionCode("TXN" + String.format("%08d", 10000000 + i));
            if (payment.getPaymentStatus() == PaymentStatus.PAID) {
                payment.setPaidAt(order.getOrderDate().plusHours(random.nextInt(24)));
            }
            order.setPayment(payment);
            
            orders.add(orderRepository.save(order));
        }
        
        return orders;
    }
    
    private int createReviews(ReviewRepository reviewRepository, List<User> users, List<Book> books, List<Order> orders) {
        Random random = new Random();
        int reviewCount = 0;
        
        if (users.isEmpty() || books.isEmpty()) {
            return 0;
        }
        
        // Get delivered orders to ensure users can review
        List<Order> deliveredOrders = orders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.DELIVERED)
                .toList();
        
        if (deliveredOrders.isEmpty()) {
            logger.warn("No delivered orders found, cannot create reviews");
            return 0;
        }
        
        // Create reviews based on actual delivered orders (more realistic)
        // This ensures every delivered order item gets a review
        Set<String> reviewedPairs = new HashSet<>();
        
        for (Order order : deliveredOrders) {
            User user = order.getUser();
            
            for (OrderItem item : order.getOrderItems()) {
                Book book = item.getBook();
                String reviewKey = user.getUserId() + "_" + book.getBookId();
                
                // Skip if already reviewed
                if (reviewedPairs.contains(reviewKey) || 
                    reviewRepository.existsByUser_UserIdAndBook_BookId(user.getUserId(), book.getBookId())) {
                    continue;
                }
                
                Review review = new Review();
                review.setUser(user);
                review.setBook(book);
                
                // Rating distribution: more positive reviews (60% 4-5 stars, 30% 3 stars, 10% 1-2 stars)
                int rating;
                double ratingRoll = random.nextDouble();
                if (ratingRoll < 0.6) {
                    rating = random.nextBoolean() ? 5 : 4; // 4 or 5 stars
                } else if (ratingRoll < 0.9) {
                    rating = 3; // 3 stars
                } else {
                    rating = random.nextBoolean() ? 1 : 2; // 1 or 2 stars
                }
                review.setRating(rating);
                
                // 85% have comments
                if (random.nextDouble() > 0.15) {
                    String[] comments = {
                        "Great book! Highly recommend.",
                        "Amazing story, couldn't put it down.",
                        "Good read, but could be better.",
                        "Not my favorite, but okay.",
                        "Excellent writing and plot.",
                        "Disappointing ending.",
                        "One of the best books I've read.",
                        "Well-written and engaging.",
                        "Interesting concept, but execution was lacking.",
                        "Perfect for a weekend read.",
                        "Too slow-paced for my taste.",
                        "Brilliant character development.",
                        "The plot was predictable.",
                        "Loved every page!",
                        "Could use more action.",
                        "Beautiful prose and storytelling.",
                        "Not worth the hype.",
                        "A masterpiece of literature.",
                        "Decent book, nothing special.",
                        "Absolutely fantastic!",
                        "Very engaging from start to finish.",
                        "The characters are well-developed.",
                        "A bit too long for my taste.",
                        "Would definitely read again.",
                        "Not what I expected, but in a good way."
                    };
                    review.setComment(comments[random.nextInt(comments.length)]);
                }
                
                reviewRepository.save(review);
                reviewedPairs.add(reviewKey);
                reviewCount++;
            }
        }
        
        // If we still need more reviews, create additional ones
        int targetReviews = 150;
        int attempts = 0;
        int maxAttempts = 300;
        
        while (reviewCount < targetReviews && attempts < maxAttempts) {
            attempts++;
            User user = users.get(random.nextInt(users.size()));
            Book book = books.get(random.nextInt(books.size()));
            String reviewKey = user.getUserId() + "_" + book.getBookId();
            
            // Check if already reviewed
            if (reviewedPairs.contains(reviewKey) || 
                reviewRepository.existsByUser_UserIdAndBook_BookId(user.getUserId(), book.getBookId())) {
                continue;
            }
            
            // Check if user has a delivered order for this book
            boolean hasDeliveredOrder = deliveredOrders.stream()
                    .anyMatch(order -> order.getUser().getUserId().equals(user.getUserId()) &&
                            order.getOrderItems().stream()
                                    .anyMatch(item -> item.getBook().getBookId().equals(book.getBookId())));
            
            if (!hasDeliveredOrder) {
                continue; // Skip if user hasn't purchased this book
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
                String[] comments = {
                    "Great book! Highly recommend.",
                    "Amazing story, couldn't put it down.",
                    "Good read, but could be better.",
                    "Not my favorite, but okay.",
                    "Excellent writing and plot.",
                    "Disappointing ending.",
                    "One of the best books I've read.",
                    "Well-written and engaging.",
                    "Interesting concept, but execution was lacking.",
                    "Perfect for a weekend read.",
                    "Too slow-paced for my taste.",
                    "Brilliant character development.",
                    "The plot was predictable.",
                    "Loved every page!",
                    "Could use more action.",
                    "Beautiful prose and storytelling.",
                    "Not worth the hype.",
                    "A masterpiece of literature.",
                    "Decent book, nothing special.",
                    "Absolutely fantastic!"
                };
                review.setComment(comments[random.nextInt(comments.length)]);
            }
            
            reviewRepository.save(review);
            reviewedPairs.add(reviewKey);
            reviewCount++;
        }
        
        return reviewCount;
    }
}

