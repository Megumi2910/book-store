# 🔐 Spring Security Authentication - Complete Guide

## Table of Contents
1. [Authentication Flow Overview](#authentication-flow-overview)
2. [Key Concepts Explained](#key-concepts-explained)
3. [HTTP Basic Auth Deep Dive](#http-basic-auth-deep-dive)
4. [Authentication Object Explained](#authentication-object-explained)
5. [Principal Explained](#principal-explained)
6. [Complete Flow Walkthrough](#complete-flow-walkthrough)
7. [Code Examples](#code-examples)

---

## Authentication Flow Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    CLIENT REQUEST                              │
│  POST /api/v1/users/change-password                            │
│  Authorization: Basic base64(email:password)                   │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│              SPRING SECURITY FILTER CHAIN                      │
│  1. BasicAuthenticationFilter intercepts request              │
│  2. Extracts credentials from Authorization header              │
│  3. Creates UsernamePasswordAuthenticationToken                 │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│              AUTHENTICATION MANAGER                            │
│  AuthenticationManager.authenticate(token)                     │
│  └─> Delegates to DaoAuthenticationProvider                   │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│         CUSTOM USER DETAILS SERVICE                             │
│  CustomUserDetailsService.loadUserByUsername(email)            │
│  └─> Queries database: findByEmailIgnoreCase(email)           │
│  └─> Returns: CustomUserDetails(userId, email, role, ...)    │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│         DAO AUTHENTICATION PROVIDER                            │
│  1. Compares provided password with stored hash                │
│  2. Uses PasswordEncoder.matches(plain, hash)                  │
│  3. If match: Creates authenticated Authentication object     │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│         AUTHENTICATION OBJECT CREATED                          │
│  Authentication {                                                │
│    principal: CustomUserDetails(userId=1, email=..., ...)     │
│    credentials: null (cleared for security)                    │
│    authorities: [ROLE_USER]                                    │
│    authenticated: true                                          │
│    details: WebAuthenticationDetails(...)                      │
│  }                                                              │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│         SECURITY CONTEXT                                        │
│  SecurityContextHolder.getContext().setAuthentication(auth)    │
│  (Stored in ThreadLocal for current request)                   │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│              CONTROLLER METHOD                                 │
│  @PostMapping("/change-password")                              │
│  public ResponseEntity changePassword(                         │
│      Authentication authentication  ← Injected here!          │
│  ) {                                                            │
│    Long userId = getUserIdFromAuthentication(authentication); │
│    // userId extracted from authentication.principal            │
│  }                                                              │
└─────────────────────────────────────────────────────────────────┘
```

---

## Key Concepts Explained

### 1. **Authentication** (Interface)

**What it is:** Spring Security's core interface representing an authenticated user.

**What it contains:**
```java
public interface Authentication extends Principal, Serializable {
    // The authenticated user (our CustomUserDetails)
    Object getPrincipal();
    
    // User's credentials (password - usually null after authentication)
    Object getCredentials();
    
    // User's authorities/roles (e.g., ["ROLE_USER", "ROLE_ADMIN"])
    Collection<? extends GrantedAuthority> getAuthorities();
    
    // Authentication details (IP address, session ID, etc.)
    Object getDetails();
    
    // Whether this authentication is authenticated
    boolean isAuthenticated();
    
    // Set authenticated status
    void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException;
    
    // The username (email in our case)
    String getName();
}
```

**Key Points:**
- `Principal` is the authenticated user (our `CustomUserDetails`)
- `Credentials` are cleared after authentication (security best practice)
- `Authorities` are the user's roles/permissions
- `isAuthenticated()` must be `true` for the request to proceed

---

### 2. **Principal** (Concept)

**What it is:** The identity of the authenticated user.

**In our code:**
```java
// Principal = CustomUserDetails object
CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

// We can access:
Long userId = userDetails.getUserId();        // 1
String email = userDetails.getEmail();        // "user@example.com"
UserRole role = userDetails.getRole();        // USER or ADMIN
boolean enabled = userDetails.isEnabled();    // true/false
```

**Why it's called "Principal":**
- In security terminology, "principal" = the entity being authenticated
- It's the "who" in "who is making this request?"
- Spring Security stores it in `Authentication.principal`

**Real-world analogy:**
- **Principal** = Your ID card (proves who you are)
- **Credentials** = Your password (proves it's really you)
- **Authorities** = Your permissions (what you can do)

---

### 3. **SecurityContext** (Storage)

**What it is:** Thread-local storage for the current `Authentication` object.

**How it works:**
```java
// Spring Security automatically stores Authentication here:
SecurityContext context = SecurityContextHolder.getContext();
Authentication auth = context.getAuthentication();

// This is thread-safe - each HTTP request has its own SecurityContext
// When request ends, SecurityContext is cleared
```

**Key Points:**
- Stored in `ThreadLocal` (one per request thread)
- Automatically cleared after request completes
- Accessible throughout the request lifecycle

---

## HTTP Basic Auth Deep Dive

### How HTTP Basic Auth Works

**Step 1: Client sends credentials**
```http
POST /api/v1/users/change-password HTTP/1.1
Authorization: Basic bWlobm5xLjI0aXRAdmt1LnVkbi52bjoxMjM0NTY3OA==
Content-Type: application/json
```

**Step 2: What is that Base64 string?**

The string `bWlobm5xLjI0aXRAdmt1LnVkbi52bjoxMjM0NTY3OA==` is:
```
base64("minhnq.24it@vku.udn.vn:12345678")
```

**Decoding process:**
```java
// 1. Remove "Basic " prefix
String encoded = "bWlobm5xLjI0aXRAdmt1LnVkbi52bjoxMjM0NTY3OA==";

// 2. Decode from Base64
String decoded = Base64.getDecoder().decode(encoded);
// Result: "minhnq.24it@vku.udn.vn:12345678"

// 3. Split by ":"
String[] parts = decoded.split(":", 2);
String email = parts[0];      // "minhnq.24it@vku.udn.vn"
String password = parts[1];   // "12345678"
```

**Step 3: Spring Security extracts credentials**

```java
// Inside BasicAuthenticationFilter (Spring Security internal)
String authHeader = request.getHeader("Authorization");
// "Basic bWlobm5xLjI0aXRAdmt1LnVkbi52bjoxMjM0NTY3OA=="

// Extract and decode
String[] parts = authHeader.split(" ");
String encoded = parts[1];  // "bWlobm5xLjI0aXRAdmt1LnVkbi52bjoxMjM0NTY3OA=="
String decoded = Base64.getDecoder().decode(encoded);
// "minhnq.24it@vku.udn.vn:12345678"

String[] credentials = decoded.split(":", 2);
String email = credentials[0];
String password = credentials[1];

// Create authentication token
UsernamePasswordAuthenticationToken token = 
    new UsernamePasswordAuthenticationToken(email, password);
```

---

## Authentication Object Explained

### Structure of Authentication Object

```java
// After successful authentication, you get:
Authentication authentication = {
    principal: CustomUserDetails {
        userId: 1,
        email: "minhnq.24it@vku.udn.vn",
        password: "$2a$11$hashedPassword...",  // BCrypt hash
        role: USER,
        enabled: true
    },
    
    credentials: null,  // ✅ Cleared for security!
    
    authorities: [
        GrantedAuthority { authority: "ROLE_USER" }
    ],
    
    authenticated: true,  // ✅ Must be true!
    
    details: WebAuthenticationDetails {
        remoteAddress: "127.0.0.1",
        sessionId: "ABC123..."
    },
    
    name: "minhnq.24it@vku.udn.vn"  // Same as email
}
```

### Methods Available on Authentication

```java
// Get the principal (our CustomUserDetails)
Object principal = authentication.getPrincipal();
CustomUserDetails userDetails = (CustomUserDetails) principal;

// Get credentials (null after authentication)
Object credentials = authentication.getCredentials();  // null

// Get authorities (roles)
Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
// [ROLE_USER] or [ROLE_ADMIN]

// Get details (request info)
Object details = authentication.getDetails();
WebAuthenticationDetails webDetails = (WebAuthenticationDetails) details;
String ipAddress = webDetails.getRemoteAddress();

// Check if authenticated
boolean isAuth = authentication.isAuthenticated();  // true

// Get name (email in our case)
String name = authentication.getName();  // "minhnq.24it@vku.udn.vn"
```

---

## Principal Explained

### What is Principal?

**Definition:** The identity of the authenticated user.

**In our code:**
```java
// Principal = CustomUserDetails object
Object principal = authentication.getPrincipal();

// Type check and cast
if (principal instanceof CustomUserDetails) {
    CustomUserDetails userDetails = (CustomUserDetails) principal;
    
    // Now we can access user information!
    Long userId = userDetails.getUserId();
    String email = userDetails.getEmail();
    UserRole role = userDetails.getRole();
}
```

### Why Store CustomUserDetails as Principal?

**Before (storing entire User entity):**
```java
// ❌ BAD: Stores entire User entity
CustomUserDetails {
    User user;  // Contains lazy-loaded relationships!
}

// Problems:
// - LazyInitializationException when accessing user.getCart()
// - Serialization issues
// - Security risk (password hash stored longer)
// - Memory overhead
```

**After (storing only essential fields):**
```java
// ✅ GOOD: Stores only essential fields
CustomUserDetails {
    Long userId;      // 1
    String email;     // "user@example.com"
    String password;  // "$2a$11$hash..."
    UserRole role;    // USER
    boolean enabled;  // true
}

// Benefits:
// - No lazy loading issues
// - No serialization problems
// - Better security
// - Lower memory usage
```

---

## Complete Flow Walkthrough

### Example: User calls `/api/v1/users/change-password`

**Request:**
```http
POST http://127.0.0.1:8080/api/v1/users/change-password HTTP/1.1
Authorization: Basic bWlobm5xLjI0aXRAdmt1LnVkbi52bjoxMjM0NTY3OA==
Content-Type: application/json

{
    "currentPassword": "12345678",
    "password": "newPassword123",
    "matchingPassword": "newPassword123"
}
```

**Step-by-step execution:**

#### Step 1: BasicAuthenticationFilter intercepts
```java
// Spring Security internal code (simplified)
String authHeader = request.getHeader("Authorization");
// "Basic bWlobm5xLjI0aXRAdmt1LnVkbi52bjoxMjM0NTY3OA=="

// Extract credentials
String encoded = authHeader.substring(6);  // Remove "Basic "
String decoded = Base64.getDecoder().decode(encoded);
// "minhnq.24it@vku.udn.vn:12345678"

String[] parts = decoded.split(":", 2);
String email = parts[0];      // "minhnq.24it@vku.udn.vn"
String password = parts[1];    // "12345678"

// Create unauthenticated token
UsernamePasswordAuthenticationToken token = 
    new UsernamePasswordAuthenticationToken(email, password);
```

#### Step 2: AuthenticationManager processes token
```java
// AuthenticationManager delegates to DaoAuthenticationProvider
AuthenticationManager authManager = authenticationManager();
Authentication authenticated = authManager.authenticate(token);
```

#### Step 3: DaoAuthenticationProvider loads user
```java
// Inside DaoAuthenticationProvider
// 1. Extract email from token
String email = token.getName();  // "minhnq.24it@vku.udn.vn"

// 2. Load user via UserDetailsService
UserDetailsService userDetailsService = customUserDetailsService;
UserDetails userDetails = userDetailsService.loadUserByUsername(email);
// Returns: CustomUserDetails(userId=1, email=..., password=..., ...)

// 3. Verify password
PasswordEncoder encoder = passwordEncoder();
String providedPassword = (String) token.getCredentials();  // "12345678"
String storedHash = userDetails.getPassword();  // "$2a$11$hashed..."

boolean matches = encoder.matches(providedPassword, storedHash);
if (!matches) {
    throw new BadCredentialsException("Invalid password");
}

// 4. Create authenticated token
UsernamePasswordAuthenticationToken authenticated = 
    new UsernamePasswordAuthenticationToken(
        userDetails,           // principal
        null,                  // credentials (cleared!)
        userDetails.getAuthorities()  // authorities
    );
authenticated.setDetails(authenticationDetailsSource.buildDetails(request));
```

#### Step 4: CustomUserDetailsService.loadUserByUsername()
```java
@Override
public UserDetails loadUserByUsername(String email) {
    // 1. Null check
    if (email == null || email.isBlank()) {
        throw new UsernameNotFoundException("Email cannot be null");
    }
    
    // 2. Query database (case-insensitive)
    User user = userRepository.findByEmailIgnoreCase(email.trim())
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    
    // 3. Create CustomUserDetails (extracts only essential fields)
    return new CustomUserDetails(user);
    // Returns: CustomUserDetails(userId=1, email="...", password="...", role=USER, enabled=true)
}
```

#### Step 5: Authentication stored in SecurityContext
```java
// Spring Security automatically does this:
SecurityContext context = SecurityContextHolder.getContext();
context.setAuthentication(authenticated);

// Now available throughout the request:
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
```

#### Step 6: Controller method receives Authentication
```java
@PostMapping("/change-password")
public ResponseEntity changePassword(
        Authentication authentication,  // ← Spring injects this automatically!
        @Valid @RequestBody ChangePasswordRequestDto request) {
    
    // authentication object is already populated:
    // authentication.principal = CustomUserDetails(userId=1, ...)
    // authentication.authenticated = true
    // authentication.authorities = [ROLE_USER]
    
    // Extract userId from principal
    Long userId = getUserIdFromAuthentication(authentication);
    // userId = 1 (no database lookup needed!)
    
    // Process password change
    userService.changePassword(userId, request);
    
    return ResponseEntity.ok(Map.of("message", "Password changed"));
}
```

#### Step 7: Extract userId from Authentication
```java
private Long getUserIdFromAuthentication(Authentication authentication) {
    // 1. Get principal
    Object principal = authentication.getPrincipal();
    // principal = CustomUserDetails(userId=1, email=..., ...)
    
    // 2. Cast to CustomUserDetails
    CustomUserDetails userDetails = (CustomUserDetails) principal;
    
    // 3. Extract userId (no database lookup!)
    Long userId = userDetails.getUserId();  // 1
    
    return userId;
}
```

---

## Code Examples

### Example 1: Accessing Authentication in Controller

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(
            Authentication authentication) {
        
        // Get principal
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        // Extract information
        Long userId = userDetails.getUserId();
        String email = userDetails.getEmail();
        UserRole role = userDetails.getRole();
        boolean enabled = userDetails.isEnabled();
        
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "email", email,
            "role", role.name(),
            "enabled", enabled
        ));
    }
}
```

### Example 2: Checking User Role

```java
@PostMapping("/admin/users")
public ResponseEntity createUser(
        Authentication authentication,
        @RequestBody UserDto userDto) {
    
    // Get authorities
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    
    // Check if user has ADMIN role
    boolean isAdmin = authorities.stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    
    if (!isAdmin) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", "Access denied. Admin role required."));
    }
    
    // Process request...
    return ResponseEntity.ok(Map.of("message", "User created"));
}
```

### Example 3: Getting User Email

```java
@GetMapping("/my-orders")
public ResponseEntity getMyOrders(Authentication authentication) {
    // Method 1: From principal
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    String email = userDetails.getEmail();
    
    // Method 2: From getName() (same result)
    String email2 = authentication.getName();
    
    // Both return: "minhnq.24it@vku.udn.vn"
    
    // Use email to fetch orders
    List<Order> orders = orderService.findByUserEmail(email);
    return ResponseEntity.ok(orders);
}
```

### Example 4: Complete Authentication Inspection

```java
@GetMapping("/debug/auth")
public ResponseEntity debugAuthentication(Authentication authentication) {
    Map<String, Object> debugInfo = new HashMap<>();
    
    // Principal
    Object principal = authentication.getPrincipal();
    if (principal instanceof CustomUserDetails) {
        CustomUserDetails userDetails = (CustomUserDetails) principal;
        debugInfo.put("userId", userDetails.getUserId());
        debugInfo.put("email", userDetails.getEmail());
        debugInfo.put("role", userDetails.getRole().name());
        debugInfo.put("enabled", userDetails.isEnabled());
    }
    
    // Credentials (should be null after authentication)
    debugInfo.put("credentials", authentication.getCredentials());
    
    // Authorities
    List<String> authorities = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());
    debugInfo.put("authorities", authorities);
    
    // Details
    Object details = authentication.getDetails();
    if (details instanceof WebAuthenticationDetails) {
        WebAuthenticationDetails webDetails = (WebAuthenticationDetails) details;
        debugInfo.put("remoteAddress", webDetails.getRemoteAddress());
        debugInfo.put("sessionId", webDetails.getSessionId());
    }
    
    // Status
    debugInfo.put("authenticated", authentication.isAuthenticated());
    debugInfo.put("name", authentication.getName());
    
    return ResponseEntity.ok(debugInfo);
}
```

---

## Summary

### Key Takeaways

1. **Authentication Object:**
   - Contains `principal` (CustomUserDetails), `credentials` (null), `authorities` (roles)
   - Automatically injected into controller methods
   - Stored in SecurityContext (ThreadLocal)

2. **Principal:**
   - The authenticated user's identity
   - In our code: `CustomUserDetails` object
   - Contains: `userId`, `email`, `role`, `enabled`

3. **HTTP Basic Auth:**
   - Format: `Authorization: Basic base64(email:password)`
   - Spring Security automatically extracts and validates
   - No need to login first - credentials sent with each request

4. **Flow:**
   - Request → BasicAuthenticationFilter → AuthenticationManager → UserDetailsService → Controller
   - Authentication stored in SecurityContext
   - Available throughout request lifecycle

5. **Best Practices:**
   - Store only essential fields in CustomUserDetails (not entire User entity)
   - Use case-insensitive email lookup
   - Clear credentials after authentication
   - Extract userId from principal (no database lookup needed)

---

## Testing Tips

### Test HTTP Basic Auth in Postman:

1. Go to **Authorization** tab
2. Select **Basic Auth** type
3. Enter:
   - **Username:** `minhnq.24it@vku.udn.vn` (your email)
   - **Password:** `12345678` (your password)
4. Postman automatically encodes to Base64
5. Send request

### Test in VS Code REST Client:

```http
POST http://127.0.0.1:8080/api/v1/users/change-password HTTP/1.1
Authorization: Basic bWlobm5xLjI0aXRAdmt1LnVkbi52bjoxMjM0NTY3OA==
Content-Type: application/json

{
    "currentPassword": "12345678",
    "password": "newPassword123",
    "matchingPassword": "newPassword123"
}
```

### Generate Base64 manually:

```bash
# Linux/Mac
echo -n "email@example.com:password" | base64

# Online tool
https://www.base64encode.org/
```

---

## Common Questions

**Q: Why is credentials null after authentication?**
A: Security best practice - password is cleared after verification to prevent accidental exposure.

**Q: Can I access the User entity from Authentication?**
A: No, and you shouldn't! We store only essential fields in CustomUserDetails to avoid lazy loading issues.

**Q: How do I get userId without database lookup?**
A: Extract from `authentication.getPrincipal()` → cast to `CustomUserDetails` → call `getUserId()`.

**Q: What if Authentication is null?**
A: User is not authenticated. Check your security configuration and ensure endpoint requires authentication.

**Q: Can I use JWT instead of HTTP Basic Auth?**
A: Yes! HTTP Basic Auth is simpler, but JWT is better for stateless APIs. Both work with the same Authentication object.

---

This guide covers the complete authentication flow in your Spring Boot application! 🎉

