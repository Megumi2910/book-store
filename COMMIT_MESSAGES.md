# Commit Messages for Recent Changes

## Suggested Commit Structure

### 1. Fix password change error display
```
fix: display password change errors inline instead of redirecting to error page

- Add InvalidPasswordException handling in ChangePasswordPageController
- Display field-specific errors under currentPassword field
- Update change-password.html template to show validation errors
- Improve user experience by showing errors directly on the form
```

**Files:**
- `src/main/java/com/second_project/book_store/controller/page/ChangePasswordPageController.java`
- `src/main/resources/templates/change-password.html`

---

### 2. Fix image URL validation and admin book form handling
```
fix: allow relative paths in image URL input and improve image handling

- Change imageUrl input type from "url" to "text" to support relative paths
- Fix image upload logic to prioritize manually entered URLs
- Add proper validation error display for imageUrl field
- Improve admin book form user experience
```

**Files:**
- `src/main/resources/templates/admin/books/form.html`
- `src/main/java/com/second_project/book_store/controller/page/AdminBookController.java`

---

### 3. Fix ISBN uniqueness constraint for empty values
```
fix: normalize empty ISBN strings to null to prevent unique constraint violations

- Add normalizeIsbn helper method in BookServiceImpl
- Convert empty/blank ISBN strings to null before saving
- Prevent duplicate entry errors for books without ISBN
- Database allows multiple NULL values in unique columns
```

**Files:**
- `src/main/java/com/second_project/book_store/service/impl/BookServiceImpl.java`

---

### 4. Implement user-facing storefront features
```
feat: implement public storefront with home page, catalog, and book details

- Add home page with recently added and popular books sections
- Implement horizontal scrolling for recently added books
- Create book catalog page with filtering options
- Add book details page with quantity selector and buy now option
- Update navigation and routing for public pages
- Allow guest users to browse books (redirect to login for cart actions)
```

**Files:**
- `src/main/java/com/second_project/book_store/controller/page/HomePageController.java`
- `src/main/java/com/second_project/book_store/controller/page/BookCatalogController.java`
- `src/main/java/com/second_project/book_store/service/impl/BookServiceImpl.java`
- `src/main/java/com/second_project/book_store/repository/BookRepository.java`
- `src/main/resources/templates/index.html`
- `src/main/resources/templates/books/catalog.html`
- `src/main/resources/templates/books/details.html`

---

### 5. Convert currency display from USD to VND
```
feat: change currency from USD ($) to Vietnamese Dong (VND)

- Create CurrencyUtils class for VND formatting
- Update all templates to use VND format (no decimals)
- Format amounts as whole numbers with comma separators
- Update currency displays across user and admin interfaces
```

**Files:**
- `src/main/java/com/second_project/book_store/util/CurrencyUtils.java`
- `src/main/resources/templates/admin/books/list.html`
- `src/main/resources/templates/admin/books/view.html`
- `src/main/resources/templates/admin/orders/details.html`
- `src/main/resources/templates/admin/orders/list.html`
- `src/main/resources/templates/admin/payments/details.html`
- `src/main/resources/templates/books/details.html`
- `src/main/resources/templates/cart/view.html`
- `src/main/resources/templates/orders/checkout.html`
- `src/main/resources/templates/orders/details.html`
- `src/main/resources/templates/orders/history.html`

---

### 6. Enhance cart functionality with multi-select and improved UX
```
feat: add multi-select cart items and improve quantity controls

- Add checkboxes for selecting multiple cart items
- Implement "Select All" functionality with auto-sync
- Add "Remove Selected" button for bulk operations
- Change quantity format to "- <number> +" style
- Add auto-increment/decrement on button hold
- Allow manual quantity input with validation
- Remove unnecessary "Qty:" label and refresh button
- Calculate totals only for selected items
- Update proceed to checkout to pass selected item IDs
```

**Files:**
- `src/main/resources/templates/cart/view.html`
- `src/main/java/com/second_project/book_store/controller/page/CartController.java`
- `src/main/java/com/second_project/book_store/service/impl/CartServiceImpl.java`

---

### 7. Improve checkout and order placement flow
```
feat: enhance checkout process with address pre-fill and selected items support

- Pre-fill shipping address from user's registered address
- Filter cart items based on selected items from cart view
- Add selectedCartItemIds to CheckoutRequestDto
- Update OrderServiceImpl to process only selected items
- Prevent cart clearing after order placement
- Improve error handling with proper cart filtering on errors
- Update checkout template to pass selected item IDs
```

**Files:**
- `src/main/java/com/second_project/book_store/controller/page/OrderController.java`
- `src/main/java/com/second_project/book_store/service/impl/OrderServiceImpl.java`
- `src/main/java/com/second_project/book_store/model/CheckoutRequestDto.java`
- `src/main/resources/templates/orders/checkout.html`

---

### 8. Integrate VietQR payment QR code generation
```
feat: add VietQR payment QR code display for orders

- Integrate VietQR API for QR code generation
- Display QR code on order details page after checkout
- Show QR code only for QR payment method
- Add showQR parameter to order details view
```

**Files:**
- `src/main/java/com/second_project/book_store/controller/page/OrderController.java`
- `src/main/resources/templates/orders/details.html`

---

### 9. Add firstName and lastName to CustomUserDetails
```
feat: add firstName and lastName fields to CustomUserDetails

- Extend CustomUserDetails with firstName and lastName
- Add getFullName() method for display purposes
- Update constructors to include new fields
- Remove redundant fetchUserName methods from UserService
- Improve user name display consistency across templates
```

**Files:**
- `src/main/java/com/second_project/book_store/security/CustomUserDetails.java`
- `src/main/java/com/second_project/book_store/service/UserService.java`
- `src/main/java/com/second_project/book_store/service/impl/UserServiceImpl.java`
- `src/main/java/com/second_project/book_store/repository/UserRepository.java`
- `src/main/resources/templates/index.html`
- `src/main/resources/templates/admin/layout/header.html`
- `src/main/resources/templates/forgot-password.html`
- `src/main/resources/templates/register.html`

---

### 10. Fix database precision for VND currency (large amounts)
```
fix: increase database precision for price and amount columns

- Change DECIMAL(10,2) to DECIMAL(15,0) for VND (no decimals)
- Update Order.totalAmount precision
- Update OrderItem.priceAtPurchase precision
- Update BookDetail.price precision
- Add SQL migration script for manual database update
- Prevent "Data truncation" errors for large amounts
```

**Files:**
- `src/main/java/com/second_project/book_store/entity/Order.java`
- `src/main/java/com/second_project/book_store/entity/OrderItem.java`
- `src/main/java/com/second_project/book_store/entity/BookDetail.java`
- `fix_price_precision.sql`

---

### 11. Update admin dashboard to use VND currency
```
fix: update admin dashboard currency display to VND

- Change Total Revenue card to use CurrencyUtils.formatVND()
- Update revenue chart tooltip to show VND instead of $
- Update chart Y-axis ticks to display VND format
- Change icon from currency-dollar to cash-coin
- Add formatVND(Double) overload for dashboard stats
```

**Files:**
- `src/main/resources/templates/admin/dashboard/index.html`
- `src/main/java/com/second_project/book_store/util/CurrencyUtils.java`

---

### 12. Fix import statements and code cleanup
```
fix: add missing imports and fix fully qualified class names

- Add Payment import to AdminPaymentController
- Add OrderStatus import to AdminOrderController
- Improve code readability by using imports instead of FQCN
```

**Files:**
- `src/main/java/com/second_project/book_store/controller/page/AdminPaymentController.java`

---

### 13. Configure session management for concurrent sessions
```
feat: allow multiple concurrent sessions per user

- Configure Spring Security session management
- Set maximumSessions to -1 (unlimited)
- Allow users to login from multiple devices
```

**Files:**
- `src/main/java/com/second_project/book_store/config/WebSecurityConfig.java`

---

### 14. Add login redirect parameter support
```
feat: preserve redirect URL after login for cart actions

- Add redirect parameter support in CustomAuthenticationSuccessHandler
- Update login template to preserve redirect parameter
- Improve UX by redirecting users back to intended page after login
```

**Files:**
- `src/main/java/com/second_project/book_store/security/CustomAuthenticationSuccessHandler.java`
- `src/main/resources/templates/login.html`

---

## Alternative: Single Large Commit

If you prefer a single commit for all changes:

```
feat: major storefront implementation and currency conversion to VND

- Implement public storefront with home page, catalog, and book details
- Add multi-select cart with improved quantity controls
- Enhance checkout flow with address pre-fill and selected items
- Integrate VietQR payment QR code generation
- Convert all currency displays from USD to VND
- Add firstName/lastName to CustomUserDetails
- Fix password change error display
- Fix image URL validation and ISBN uniqueness issues
- Increase database precision for VND amounts
- Update admin dashboard to use VND currency
- Add session management for concurrent sessions
- Improve login redirect handling
```



