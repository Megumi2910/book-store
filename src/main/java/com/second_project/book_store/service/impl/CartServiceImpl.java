package com.second_project.book_store.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.second_project.book_store.entity.Book;
import com.second_project.book_store.entity.BookDetail;
import com.second_project.book_store.entity.Cart;
import com.second_project.book_store.entity.CartItem;
import com.second_project.book_store.entity.User;
import com.second_project.book_store.model.CartDto;
import com.second_project.book_store.model.CartItemDto;
import com.second_project.book_store.repository.BookRepository;
import com.second_project.book_store.repository.CartItemRepository;
import com.second_project.book_store.repository.CartRepository;
import com.second_project.book_store.repository.UserRepository;
import com.second_project.book_store.service.CartService;

/**
 * Implementation of CartService.
 * 
 * BEST PRACTICES:
 * - @Transactional for write operations
 * - Validate stock availability before adding/updating
 * - Use DTOs to avoid lazy loading issues
 * - Log important operations
 */
@Service
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           BookRepository bookRepository,
                           UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public CartDto getOrCreateCart(Long userId) {
        logger.debug("Getting or creating cart for user ID: {}", userId);

        Cart cart = cartRepository.findByUser_UserId(userId)
            .orElseGet(() -> {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
                
                Cart newCart = new Cart();
                newCart.setUser(user);
                Cart saved = cartRepository.save(newCart);
                logger.info("Created new cart for user ID: {}", userId);
                return saved;
            });

        return convertToDto(cart);
    }

    @Override
    @Transactional
    public CartDto addToCart(Long userId, Long bookId, Integer quantity) {
        logger.info("Adding book {} to cart for user {}", bookId, userId);

        if (quantity == null || quantity <= 0) {
            quantity = 1;
        }

        // Get or create cart
        Cart cart = cartRepository.findByUser_UserId(userId)
            .orElseGet(() -> {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
                Cart newCart = new Cart();
                newCart.setUser(user);
                return cartRepository.save(newCart);
            });

        // Get book and validate stock
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));

        BookDetail bookDetail = book.getBookDetail();
        if (bookDetail == null) {
            throw new IllegalArgumentException("Book details not found for book: " + bookId);
        }

        Integer availableStock = bookDetail.getQuantity();
        if (availableStock == null || availableStock <= 0) {
            throw new IllegalArgumentException("Book is out of stock: " + book.getTitle());
        }

        // Check if item already exists in cart
        CartItem existingItem = cartItemRepository
            .findByCart_CartIdAndBook_BookId(cart.getCartId(), bookId)
            .orElse(null);

        if (existingItem != null) {
            // Update quantity
            int newQuantity = existingItem.getQuantity() + quantity;
            if (newQuantity > availableStock) {
                throw new IllegalArgumentException(
                    String.format("Insufficient stock. Available: %d, Requested: %d", 
                                 availableStock, newQuantity));
            }
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
            logger.info("Updated cart item quantity to {} for book {}", newQuantity, bookId);
        } else {
            // Create new cart item
            if (quantity > availableStock) {
                throw new IllegalArgumentException(
                    String.format("Insufficient stock. Available: %d, Requested: %d", 
                                 availableStock, quantity));
            }
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setBook(book);
            cartItem.setQuantity(quantity);
            cart.addCartItem(cartItem);
            cartItemRepository.save(cartItem);
            logger.info("Added new item to cart: book {}, quantity {}", bookId, quantity);
        }

        return convertToDto(cart);
    }

    @Override
    @Transactional
    public CartDto updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity) {
        logger.info("Updating cart item {} quantity to {} for user {}", cartItemId, quantity, userId);

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));

        // Verify ownership
        if (!cartItem.getCart().getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Cart item does not belong to user");
        }

        // Validate stock
        BookDetail bookDetail = cartItem.getBook().getBookDetail();
        if (bookDetail == null) {
            throw new IllegalArgumentException("Book details not found");
        }

        Integer availableStock = bookDetail.getQuantity();
        if (quantity > availableStock) {
            throw new IllegalArgumentException(
                String.format("Insufficient stock. Available: %d, Requested: %d", 
                             availableStock, quantity));
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        Cart cart = cartItem.getCart();
        return convertToDto(cart);
    }

    @Override
    @Transactional
    public CartDto removeFromCart(Long userId, Long cartItemId) {
        logger.info("Removing cart item {} for user {}", cartItemId, userId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));

        // Verify ownership
        if (!cartItem.getCart().getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Cart item does not belong to user");
        }

        Cart cart = cartItem.getCart();
        cart.removeCartItem(cartItem);
        cartItemRepository.delete(cartItem);

        return convertToDto(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        logger.info("Clearing cart for user {}", userId);

        Cart cart = cartRepository.findByUser_UserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + userId));

        cart.clearCart();
        cartRepository.save(cart);
    }

    @Override
    public Integer getCartItemCount(Long userId) {
        Cart cart = cartRepository.findByUser_UserId(userId).orElse(null);
        if (cart == null || cart.getCartItems() == null) {
            return 0;
        }

        return cart.getCartItems().stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
    }

    /**
     * Convert Cart entity to CartDto.
     */
    private CartDto convertToDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setCartId(cart.getCartId());

        if (cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
            List<CartItemDto> itemDtos = cart.getCartItems().stream()
                .map(this::convertCartItemToDto)
                .collect(Collectors.toList());
            dto.setCartItems(itemDtos);
        }

        dto.calculateTotals();
        return dto;
    }

    /**
     * Convert CartItem entity to CartItemDto.
     */
    private CartItemDto convertCartItemToDto(CartItem cartItem) {
        Book book = cartItem.getBook();
        BookDetail bookDetail = book.getBookDetail();

        String imageUrl = bookDetail != null && bookDetail.getImageUrl() != null
            ? bookDetail.getImageUrl()
            : "/images/placeholder.jpg";

        BigDecimal price = bookDetail != null && bookDetail.getPrice() != null
            ? bookDetail.getPrice()
            : BigDecimal.ZERO;

        Integer availableStock = bookDetail != null && bookDetail.getQuantity() != null
            ? bookDetail.getQuantity()
            : 0;

        return new CartItemDto(
            cartItem.getCartItemId(),
            book.getBookId(),
            book.getTitle(),
            book.getAuthor(),
            imageUrl,
            price,
            cartItem.getQuantity(),
            availableStock
        );
    }
}

