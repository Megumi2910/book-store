package com.second_project.book_store.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.second_project.book_store.entity.Book;
import com.second_project.book_store.entity.BookDetail;
import com.second_project.book_store.entity.Cart;
import com.second_project.book_store.entity.CartItem;
import com.second_project.book_store.entity.Order;
import com.second_project.book_store.entity.Order.OrderStatus;
import com.second_project.book_store.entity.OrderItem;
import com.second_project.book_store.entity.Payment;
import com.second_project.book_store.entity.Payment.PaymentMethod;
import com.second_project.book_store.entity.Payment.PaymentStatus;
import com.second_project.book_store.entity.User;
import com.second_project.book_store.model.CheckoutRequestDto;
import com.second_project.book_store.model.OrderDto;
import com.second_project.book_store.model.OrderItemDto;
import com.second_project.book_store.repository.BookRepository;
import com.second_project.book_store.repository.CartRepository;
import com.second_project.book_store.repository.OrderRepository;
import com.second_project.book_store.repository.PaymentRepository;
import com.second_project.book_store.repository.UserRepository;
import com.second_project.book_store.service.CartService;
import com.second_project.book_store.service.OrderService;

/**
 * Implementation of OrderService.
 * 
 * BEST PRACTICES:
 * - @Transactional for write operations
 * - Validate stock before creating order
 * - Use optimistic locking (@Version) to prevent concurrent modifications
 * - Log important operations
 */
@Service
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    public OrderServiceImpl(OrderRepository orderRepository,
                           PaymentRepository paymentRepository,
                           CartRepository cartRepository,
                           BookRepository bookRepository,
                           UserRepository userRepository,
                           CartService cartService) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.cartRepository = cartRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
    }

    @Override
    @Transactional
    public OrderDto createOrderFromCart(Long userId, CheckoutRequestDto checkoutRequest) {
        logger.info("Creating order from cart for user {}", userId);
        logger.debug("Selected cart item IDs: {}", checkoutRequest.getSelectedCartItemIds());

        // Get user's cart
        Cart cart = cartRepository.findByUser_UserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + userId));

        List<CartItem> allCartItems = cart.getCartItems();
        if (allCartItems == null || allCartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }
        
        logger.debug("Total cart items: {}", allCartItems.size());

        // Filter cart items if selectedCartItemIds is provided
        List<CartItem> cartItemsToProcess = new ArrayList<>(allCartItems);
        if (checkoutRequest.getSelectedCartItemIds() != null && !checkoutRequest.getSelectedCartItemIds().trim().isEmpty()) {
            String[] selectedIds = checkoutRequest.getSelectedCartItemIds().split(",");
            List<Long> selectedIdList = new ArrayList<>();
            for (String id : selectedIds) {
                try {
                    selectedIdList.add(Long.parseLong(id.trim()));
                } catch (NumberFormatException e) {
                    logger.warn("Invalid cart item ID in selectedCartItemIds: {}", id);
                }
            }
            
            if (!selectedIdList.isEmpty()) {
                logger.debug("Filtering cart items. Selected IDs: {}", selectedIdList);
                cartItemsToProcess = allCartItems.stream()
                    .filter(item -> item != null && selectedIdList.contains(item.getCartItemId()))
                    .collect(java.util.stream.Collectors.toList());
                
                logger.debug("Filtered cart items count: {}", cartItemsToProcess.size());
                
                if (cartItemsToProcess.isEmpty()) {
                    throw new IllegalArgumentException("No valid items selected for checkout. Please ensure the selected items are still in your cart.");
                }
            }
        }
        
        // Final check to ensure we have items to process
        if (cartItemsToProcess == null || cartItemsToProcess.isEmpty()) {
            throw new IllegalArgumentException("No items available for checkout");
        }
        
        logger.debug("Processing {} cart items for checkout", cartItemsToProcess.size());

        // Validate payment method
        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.valueOf(checkoutRequest.getPaymentMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment method: " + checkoutRequest.getPaymentMethod());
        }

        // Validate stock and calculate total
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem cartItem : cartItemsToProcess) {
            Book book = cartItem.getBook();
            BookDetail bookDetail = book.getBookDetail();
            
            if (bookDetail == null) {
                throw new IllegalArgumentException("Book details not found for book: " + book.getTitle());
            }

            Integer availableStock = bookDetail.getQuantity();
            Integer requestedQuantity = cartItem.getQuantity();

            if (availableStock == null || availableStock < requestedQuantity) {
                throw new IllegalArgumentException(
                    String.format("Insufficient stock for '%s'. Available: %d, Requested: %d",
                                 book.getTitle(),
                                 availableStock != null ? availableStock : 0,
                                 requestedQuantity));
            }

            BigDecimal itemPrice = bookDetail.getPrice();
            if (itemPrice == null) {
                throw new IllegalArgumentException("Price not set for book: " + book.getTitle());
            }

            totalAmount = totalAmount.add(itemPrice.multiply(BigDecimal.valueOf(requestedQuantity)));
        }

        // Get user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setShippingAddress(checkoutRequest.getShippingAddress());

        // Create order items and update stock
        for (CartItem cartItem : cartItemsToProcess) {
            Book book = cartItem.getBook();
            BookDetail bookDetail = book.getBookDetail();

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setBook(book);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(bookDetail.getPrice());
            order.addOrderItem(orderItem);

            // Update stock
            Integer newStock = bookDetail.getQuantity() - cartItem.getQuantity();
            bookDetail.setQuantity(newStock);
        }

        // Save order (cascades to order items)
        Order savedOrder = orderRepository.save(order);

        // Create payment
        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTransactionCode(generateTransactionCode());
        savedOrder.setPayment(payment);
        paymentRepository.save(payment);

        // Note: Cart is NOT cleared - items remain in cart for potential future orders

        logger.info("Order created successfully: orderId={}, totalAmount={}", 
                   savedOrder.getOrderId(), totalAmount);

        return convertToDto(savedOrder);
    }

    @Override
    public OrderDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        return convertToDto(order);
    }

    @Override
    public Page<OrderDto> getUserOrders(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUser_UserId(userId, pageable);
        List<OrderDto> orderDtos = orders.getContent().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        return new PageImpl<>(orderDtos, pageable, orders.getTotalElements());
    }

    @Override
    public Page<OrderDto> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        List<OrderDto> orderDtos = orders.getContent().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        return new PageImpl<>(orderDtos, pageable, orders.getTotalElements());
    }

    @Override
    public Page<OrderDto> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByOrderStatus(status, pageable);
        List<OrderDto> orderDtos = orders.getContent().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        return new PageImpl<>(orderDtos, pageable, orders.getTotalElements());
    }

    @Override
    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        logger.info("Updating order {} status to {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        OrderStatus currentStatus = order.getOrderStatus();

        // Validate status transition
        if (currentStatus == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot update status of cancelled order");
        }

        if (currentStatus == OrderStatus.DELIVERED && newStatus != OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Cannot change status of delivered order");
        }

        order.setOrderStatus(newStatus);

        // If status changed to DELIVERED, mark payment as PAID (if COD)
        if (newStatus == OrderStatus.DELIVERED && order.getPayment() != null) {
            Payment payment = order.getPayment();
            if (payment.getPaymentMethod() == PaymentMethod.COD && 
                payment.getPaymentStatus() == PaymentStatus.PENDING) {
                payment.setPaymentStatus(PaymentStatus.PAID);
                payment.setPaidAt(LocalDateTime.now());
                paymentRepository.save(payment);
            }
        }

        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto cancelOrder(Long orderId, Long userId) {
        logger.info("Cancelling order {} by user {}", orderId, userId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Verify ownership (unless admin)
        if (!order.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to user");
        }

        // Only pending orders can be cancelled
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only pending orders can be cancelled");
        }

        // Restore stock
        for (OrderItem orderItem : order.getOrderItems()) {
            Book book = orderItem.getBook();
            BookDetail bookDetail = book.getBookDetail();
            if (bookDetail != null) {
                Integer currentStock = bookDetail.getQuantity() != null ? bookDetail.getQuantity() : 0;
                bookDetail.setQuantity(currentStock + orderItem.getQuantity());
            }
        }

        // Update order status
        order.setOrderStatus(OrderStatus.CANCELLED);

        // Update payment status if pending
        if (order.getPayment() != null && order.getPayment().getPaymentStatus() == PaymentStatus.PENDING) {
            order.getPayment().setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(order.getPayment());
        }

        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }

    /**
     * Generate a fake transaction code for payment simulation.
     */
    private String generateTransactionCode() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Convert Order entity to OrderDto.
     */
    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setOrderId(order.getOrderId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setOrderDate(order.getOrderDate());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setUserId(order.getUser().getUserId());
        dto.setUserEmail(order.getUser().getEmail());

        // Convert order items
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            List<OrderItemDto> itemDtos = order.getOrderItems().stream()
                .map(this::convertOrderItemToDto)
                .collect(Collectors.toList());
            dto.setOrderItems(itemDtos);
        }

        // Convert payment information
        if (order.getPayment() != null) {
            Payment payment = order.getPayment();
            dto.setPaymentId(payment.getPaymentId());
            dto.setPaymentMethod(payment.getPaymentMethod());
            dto.setPaymentStatus(payment.getPaymentStatus());
            dto.setTransactionCode(payment.getTransactionCode());
            dto.setPaidAt(payment.getPaidAt());
        }

        return dto;
    }

    /**
     * Convert OrderItem entity to OrderItemDto.
     */
    private OrderItemDto convertOrderItemToDto(OrderItem orderItem) {
        Book book = orderItem.getBook();
        BookDetail bookDetail = book.getBookDetail();

        String imageUrl = bookDetail != null && bookDetail.getImageUrl() != null
            ? bookDetail.getImageUrl()
            : "/images/placeholder.jpg";

        return new OrderItemDto(
            orderItem.getOrderItemId(),
            book.getBookId(),
            book.getTitle(),
            book.getAuthor(),
            imageUrl,
            orderItem.getQuantity(),
            orderItem.getPriceAtPurchase()
        );
    }

    @Override
    public Long countUserOrders(Long userId) {
        return orderRepository.countByUser_UserId(userId);
    }
}


