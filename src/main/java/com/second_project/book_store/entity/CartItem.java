package com.second_project.book_store.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "UK_CART_BOOK", columnNames = {"cart_id", "book_id"}))
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;

    @Column(nullable = false)
    @NotNull
    @Positive
    private Integer quantity;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "cart_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK1_CART_ITEM")
    )
    @NotNull
    private Cart cart;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "book_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "FK2_CART_ITEM")
    )
    @NotNull
    private Book book;

    public CartItem() {}

    public CartItem(Long cartItemId, Integer quantity, Cart cart, Book book) {
        this.cartItemId = cartItemId;
        this.quantity = quantity;
        this.cart = cart;
        this.book = book;
    }

    // Helper method to increment quantity
    public void incrementQuantity(int amount) {
        this.quantity += amount;
    }

    // Helper method to decrement quantity
    public void decrementQuantity(int amount) {
        this.quantity = Math.max(1, this.quantity - amount);
    }

    // Getters and Setters
    public Long getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(Long cartItemId) {
        this.cartItemId = cartItemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    @Override
    public String toString() {
        return "CartItem [cartItemId=" + cartItemId + ", quantity=" + quantity + "]";
    }
}
