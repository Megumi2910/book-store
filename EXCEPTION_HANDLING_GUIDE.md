# Exception Handling Guide

This guide explains how exception handling works in the Book Store application.

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Exception Handlers](#exception-handlers)
3. [Custom Exceptions](#custom-exceptions)
4. [Best Practices](#best-practices)
5. [Examples](#examples)

---

## Architecture Overview

The application uses **two separate exception handlers**:

### 1. **ApiExceptionHandler** - For REST APIs
- **Package**: `com.second_project.book_store.exception.handler.api`
- **Annotation**: `@RestControllerAdvice`
- **Scope**: `com.second_project.book_store.controller.api`
- **Returns**: JSON responses (`ResponseEntity<Map<String, ?>>`)
- **Use Case**: Handles exceptions from REST API endpoints (`/api/**`)

### 2. **PageExceptionHandler** - For Thymeleaf Pages
- **Package**: `com.second_project.book_store.exception.handler.page`
- **Annotation**: `@ControllerAdvice`
- **Scope**: `com.second_project.book_store.controller.page`
- **Returns**: View names (`String`) or redirects
- **Use Case**: Handles exceptions from Thymeleaf page controllers

---

## Exception Handlers

### ApiExceptionHandler - REST API

Returns structured JSON error responses for API clients.

#### Response Format
```json
{
    "error": "error_code",
    "message": "Human-readable error message"
}
```

#### Handled Exceptions

| Exception | HTTP Status | Error Code | Description |
|-----------|-------------|------------|-------------|
| `ExpiredTokenException` | 401 UNAUTHORIZED | `expired` | Token has expired |
| `UserNotFoundException` | 404 NOT_FOUND | `not found` | User doesn't exist |
| `VerificationTokenNotFoundException` | 404 NOT_FOUND | `not found` | Verification token not found |
| `ResetPasswordTokenNotFoundException` | 404 NOT_FOUND | `not found` | Reset token not found |
| `UserAlreadyEnabledException` | 400 BAD_REQUEST | `already_verified` | User already verified |
| `UserAlreadyExistedException` | 409 CONFLICT | `user_already_exists` | Email already registered |
| `InvalidPasswordException` | 400 BAD_REQUEST | `invalid_password` | Password validation failed |
| `MethodArgumentNotValidException` | 400 BAD_REQUEST | `validation_failed` | Form validation failed |

#### Example Response
```json
{
    "error": "invalid_password",
    "message": "Current password is incorrect"
}
```

---

### PageExceptionHandler - Thymeleaf Pages

Returns user-friendly error pages or redirects with flash messages.

#### Handled Exceptions

| Exception | Action | Redirect/View | Message Type |
|-----------|--------|---------------|--------------|
| `ExpiredTokenException` | Show error page | `error` | Model attribute |
| `UserNotFoundException` | Show error page | `error` | Model attribute |
| `VerificationTokenNotFoundException` | Show error page | `error` | Model attribute |
| `ResetPasswordTokenNotFoundException` | Show error page | `error` | Model attribute |
| `UserAlreadyEnabledException` | Redirect to login | `redirect:/login` | Flash (info) |
| `UserAlreadyExistedException` | Redirect to register | `redirect:/register` | Flash (error) |
| `InvalidPasswordException` | Show error on form | `reset-password` or `error` | Model attribute |
| `WebExchangeBindException` | Show validation errors | `error` | Model attribute |
| `Exception` (generic) | Show generic error | `error` | Model attribute |

#### Error Page Template
Location: `src/main/resources/templates/error.html`

**Model Attributes:**
- `error` - Error title (e.g., "Token Expired")
- `message` - Detailed error message
- `path` - Request path (for debugging)
- `validationErrors` - List of validation errors (optional)

---

## Custom Exceptions

All custom exceptions extend `RuntimeException` and are located in `com.second_project.book_store.exception`.

### 1. ExpiredTokenException
**Thrown when**: Verification or password reset token has expired
```java
throw new ExpiredTokenException("Verification token has expired. Please request a new one.");
```

### 2. UserNotFoundException
**Thrown when**: User not found by email or ID
```java
throw new UserNotFoundException("User not found with email: " + email);
```

### 3. VerificationTokenNotFoundException
**Thrown when**: Email verification token not found or already used
```java
throw new VerificationTokenNotFoundException("Invalid verification token");
```

### 4. ResetPasswordTokenNotFoundException
**Thrown when**: Password reset token not found or already used
```java
throw new ResetPasswordTokenNotFoundException("Invalid or expired reset token");
```

### 5. UserAlreadyEnabledException
**Thrown when**: Trying to verify an already verified account
```java
throw new UserAlreadyEnabledException("User is already verified. No need to resend verification token.");
```

### 6. UserAlreadyExistedException
**Thrown when**: Trying to register with an existing email
```java
throw new UserAlreadyExistedException("User already exists with email: " + email);
```

### 7. InvalidPasswordException
**Thrown when**: Password validation fails (change password, reset password)
```java
throw new InvalidPasswordException("Current password is incorrect");
```

---

## Best Practices

### 1. Use Appropriate Exception Types
```java
// ✅ Good - Specific exception
throw new UserNotFoundException("User not found with email: " + email);

// ❌ Bad - Generic exception
throw new RuntimeException("User not found");
```

### 2. Provide User-Friendly Messages
```java
// ✅ Good - Clear and actionable
throw new ExpiredTokenException("Your verification token has expired. Please request a new verification email.");

// ❌ Bad - Technical jargon
throw new ExpiredTokenException("Token TTL exceeded");
```

### 3. Don't Expose Sensitive Information
```java
// ✅ Good - Generic message for security
throw new UserNotFoundException("Invalid email or password");

// ❌ Bad - Reveals if email exists (security risk)
throw new UserNotFoundException("No user found with email: " + email);
```

### 4. Use Flash Attributes for Redirects (Pages)
```java
// In PageExceptionHandler
redirectAttributes.addFlashAttribute("error", ex.getMessage());
return "redirect:/login";
```

### 5. Use Model Attributes for Direct Views (Pages)
```java
// In PageExceptionHandler
model.addAttribute("error", "Token Expired");
model.addAttribute("message", ex.getMessage());
return "error";
```

### 6. Return Consistent JSON Structure (APIs)
```java
// In ApiExceptionHandler
return ResponseEntity.status(HttpStatus.BAD_REQUEST)
    .body(Map.of("error", "error_code", "message", exception.getMessage()));
```

---

## Examples

### Example 1: API Registration with Existing Email

**Request:**
```http
POST /api/v1/users/register
Content-Type: application/json

{
    "email": "existing@example.com",
    "password": "password123",
    ...
}
```

**Response (409 CONFLICT):**
```json
{
    "error": "user_already_exists",
    "message": "User already exists with email: existing@example.com"
}
```

**Flow:**
1. `UserServiceImpl.registerUser()` throws `UserAlreadyExistedException`
2. `ApiExceptionHandler.handleUserAlreadyExistedException()` catches it
3. Returns 409 status with JSON error

---

### Example 2: Page Reset Password with Expired Token

**Request:**
```
GET /reset-password?token=expired-token-123
```

**Response:**
- Shows `error.html` page
- Error title: "Token Expired"
- Error message: "Your reset token has expired. Please request a new password reset."
- Actions: "Go Back" or "Home Page"

**Flow:**
1. `PasswordResetPageController.showResetPasswordForm()` calls `resetPasswordTokenService.verifyToken()`
2. Service throws `ExpiredTokenException`
3. `PageExceptionHandler.handleExpiredTokenException()` catches it
4. Returns `error` view with model attributes

---

### Example 3: Page Login with Already Verified Account

**Request:**
```
GET /verify-registration?token=already-used-token
```

**Response:**
- Redirects to `/login`
- Flash message (info): "User is already verified. No need to resend verification token."

**Flow:**
1. `VerificationTokenService.verifyToken()` throws `UserAlreadyEnabledException`
2. `PageExceptionHandler.handleUserAlreadyEnabledException()` catches it
3. Redirects to `/login` with flash attribute

---

### Example 4: API Validation Error

**Request:**
```http
POST /api/v1/users/register
Content-Type: application/json

{
    "email": "invalid-email",
    "password": "123",
    "matchingPassword": "456"
}
```

**Response (400 BAD_REQUEST):**
```json
{
    "error": "validation_failed",
    "message": "Validation failed. Please check the following fields:",
    "errors": {
        "email": "Invalid email format",
        "password": "Password must be at least 8 characters",
        "matchingPassword": "Passwords do not match"
    }
}
```

**Flow:**
1. `@Valid` annotation triggers `MethodArgumentNotValidException`
2. `ApiExceptionHandler.handleValidationExceptions()` catches it
3. Returns 400 status with detailed field errors

---

## Testing Exception Handling

### Test API Exception Handler
```java
@Test
void testUserNotFoundException() throws Exception {
    mockMvc.perform(post("/api/v1/users/forgot-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"nonexistent@example.com\"}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("not found"))
        .andExpect(jsonPath("$.message").exists());
}
```

### Test Page Exception Handler
```java
@Test
void testExpiredTokenRedirect() throws Exception {
    mockMvc.perform(get("/reset-password")
            .param("token", "expired-token"))
        .andExpect(status().isOk())
        .andExpect(view().name("error"))
        .andExpect(model().attribute("error", "Token Expired"));
}
```

---

## Logging

### Current Implementation
```java
// In PageExceptionHandler (generic exception handler)
System.err.println("Unexpected error: " + ex.getMessage());
ex.printStackTrace();
```

### Recommended: Use SLF4J Logger
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice
public class PageExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(PageExceptionHandler.class);
    
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, HttpServletRequest request, Model model) {
        logger.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        // ...
    }
}
```

---

## Summary

| Aspect | API Handler | Page Handler |
|--------|-------------|--------------|
| **Annotation** | `@RestControllerAdvice` | `@ControllerAdvice` |
| **Scope** | `/api/**` endpoints | Page controllers |
| **Returns** | JSON (`ResponseEntity`) | Views (`String`) |
| **Error Display** | JSON response | HTML error page |
| **Redirects** | Not used | With flash messages |
| **Best For** | Mobile apps, SPA | Server-side rendered pages |

Both handlers work together to provide comprehensive error handling for your application!

