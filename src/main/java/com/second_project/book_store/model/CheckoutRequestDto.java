package com.second_project.book_store.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for checkout request.
 * Used when user submits checkout form.
 */
public class CheckoutRequestDto {

    @NotBlank(message = "Shipping address is required")
    @Size(max = 500, message = "Shipping address must not exceed 500 characters")
    private String shippingAddress;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // "COD" or "QR"

    public CheckoutRequestDto() {}

    public CheckoutRequestDto(String shippingAddress, String paymentMethod) {
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}

