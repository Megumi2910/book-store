# 🔍 Authentication Code Walkthrough - Your Codebase

This document shows **exactly** what happens in YOUR code when a user authenticates.

---

## Scenario: User calls `/api/v1/users/change-password`

### Request:
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

---

## Step-by-Step Code Execution

### Step 1: Spring Security Filter Chain (`WebSecurityConfig.java`)

**File:** `src/main/java/com/second_project/book_store/config/WebSecurityConfig.java`

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
    httpSecurity
        .httpBasic(Customizer.withDefaults())  // ← Enables HTTP Basic Auth
        .authorizeHttpRequests(authz -> authz
            .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
            .anyRequest().authenticated()  // ← This endpoint requires auth!
        );
}
```

**What happens:**
- Spring Security's `BasicAuthenticationFilter` intercepts the request
- Sees `Authorization: Basic ...` header
- Extracts credentials and creates `UsernamePasswordAuthenticationToken`

---

### Step 2: Authentication Manager (`WebSecurityConfig.java`)

**File:** `src/main/java/com/second_project/book_store/config/WebSecurityConfig.java`

```java
@Bean
DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);  // ← Your CustomUserDetailsService
    authProvider.setPasswordEncoder(passwordEncoder());      // ← BCrypt encoder
    return authProvider;
}

@Bean
AuthenticationManager authenticationManager() {
    return new ProviderManager(authenticationProvider());
}
```

**What happens:**
- `AuthenticationManager` receives the token
- Delegates to `DaoAuthenticationProvider`
- Provider calls `userDetailsService.loadUserByUsername(email)`

---

### Step 3: Load User (`CustomUserDetailsService.java`)

**File:** `src/main/java/com/second_project/book_store/security/CustomUserDetailsService.java`

```java
@Override
public UserDetails loadUserByUsername(String email) {
    // email = "minhnq.24it@vku.udn.vn" (from Authorization header)
    
    // 1. Null safety check
    if (email == null || email.isBlank()) {
        throw new UsernameNotFoundException("Email cannot be null or blank");
    }
    
    // 2. Query database (case-insensitive!)
    User user = userRepository.findByEmailIgnoreCase(email.trim())
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    
    // user = User entity from database:
    // {
    //   userId: 1,
    //   email: "minhnq.24it@vku.udn.vn",
    //   password: "$2a$11$hashedPassword...",
    //   role: USER,
    //   enabled: true,
    //   cart: Cart(...),      ← Lazy-loaded!
    //   orders: List<Order>,  ← Lazy-loaded!
    //   ...
    // }
    
    // 3. Create CustomUserDetails (extracts only essential fields)
    return new CustomUserDetails(user);
    // Returns: CustomUserDetails {
    //   userId: 1,
    //   email: "minhnq.24it@vku.udn.vn",
    //   password: "$2a$11$hashedPassword...",
    //   role: USER,
    //   enabled: true
    // }
    // ✅ NO lazy-loaded relationships stored!
}
```

**Key Points:**
- Uses `findByEmailIgnoreCase()` for case-insensitive lookup
- Extracts only essential fields (not entire User entity)
- Returns `CustomUserDetails` object

---

### Step 4: Create CustomUserDetails (`CustomUserDetails.java`)

**File:** `src/main/java/com/second_project/book_store/security/CustomUserDetails.java`

```java
public CustomUserDetails(User user) {
    if (user == null) {
        throw new IllegalArgumentException("User cannot be null");
    }
    
    // Extract only essential fields
    this.userId = user.getUserId();           // 1
    this.email = user.getEmail();             // "minhnq.24it@vku.udn.vn"
    this.password = user.getPassword();        // "$2a$11$hashedPassword..."
    this.role = user.getRole();                // USER
    this.enabled = user.isEnabled();           // true
    
    // ✅ We DON'T store: user.getCart(), user.getOrders(), etc.
    // This prevents LazyInitializationException!
}
```

**What's stored:**
```java
CustomUserDetails {
    userId: 1,
    email: "minhnq.24it@vku.udn.vn",
    password: "$2a$11$hashedPassword...",
    role: USER,
    enabled: true
}
```

**What's NOT stored:**
- `user.cart` (lazy-loaded relationship)
- `user.orders` (lazy-loaded relationship)
- `user.reviews` (lazy-loaded relationship)

---

### Step 5: Password Verification (`DaoAuthenticationProvider` - Spring Security Internal)

```java
// Inside DaoAuthenticationProvider (Spring Security internal)
PasswordEncoder encoder = passwordEncoder();  // BCryptPasswordEncoder(11)

String providedPassword = "12345678";  // From Authorization header
String storedHash = userDetails.getPassword();  // "$2a$11$hashedPassword..."

boolean matches = encoder.matches(providedPassword, storedHash);
// BCrypt compares: "12345678" with "$2a$11$hashedPassword..."
// Returns: true if password matches

if (!matches) {
    throw new BadCredentialsException("Invalid password");
}
```

**What happens:**
- BCrypt compares plain password with stored hash
- If match: authentication succeeds
- If no match: throws `BadCredentialsException`

---

### Step 6: Create Authenticated Token (Spring Security Internal)

```java
// After password verification succeeds:
UsernamePasswordAuthenticationToken authenticated = 
    new UsernamePasswordAuthenticationToken(
        userDetails,                    // principal = CustomUserDetails
        null,                          // credentials = null (cleared!)
        userDetails.getAuthorities()   // authorities = [ROLE_USER]
    );
authenticated.setAuthenticated(true);
```

**Result:**
```java
Authentication {
    principal: CustomUserDetails {
        userId: 1,
        email: "minhnq.24it@vku.udn.vn",
        password: "$2a$11$hashedPassword...",
        role: USER,
        enabled: true
    },
    credentials: null,  // ✅ Cleared for security!
    authorities: [GrantedAuthority { authority: "ROLE_USER" }],
    authenticated: true,
    details: WebAuthenticationDetails {
        remoteAddress: "127.0.0.1",
        sessionId: "ABC123..."
    },
    name: "minhnq.24it@vku.udn.vn"
}
```

---

### Step 7: Store in SecurityContext (Spring Security Internal)

```java
// Spring Security automatically does this:
SecurityContext context = SecurityContextHolder.getContext();
context.setAuthentication(authenticated);

// Now available throughout the request:
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
// auth = the Authentication object above
```

---

### Step 8: Controller Method (`UserController.java`)

**File:** `src/main/java/com/second_project/book_store/controller/api/UserController.java`

```java
@PostMapping("/change-password")
public ResponseEntity<Map<String, String>> changePassword(
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

**Key Point:**
- `Authentication authentication` parameter is **automatically injected** by Spring
- No need to manually retrieve it from SecurityContext
- Already contains authenticated user information

---

### Step 9: Extract UserId (`UserController.java`)

**File:** `src/main/java/com/second_project/book_store/controller/api/UserController.java`

```java
private Long getUserIdFromAuthentication(Authentication authentication) {
    // 1. Null safety check
    if (authentication == null) {
        throw new IllegalStateException("Authentication cannot be null");
    }
    
    // 2. Get principal
    Object principal = authentication.getPrincipal();
    // principal = CustomUserDetails {
    //   userId: 1,
    //   email: "minhnq.24it@vku.udn.vn",
    //   ...
    // }
    
    // 3. Null safety check
    if (principal == null) {
        throw new IllegalStateException("Principal cannot be null");
    }
    
    // 4. Type check and cast
    if (principal instanceof CustomUserDetails) {
        CustomUserDetails userDetails = (CustomUserDetails) principal;
        
        // 5. Extract userId (no database lookup!)
        Long userId = userDetails.getUserId();  // 1
        
        // 6. Additional null check
        if (userId == null) {
            throw new IllegalStateException("User ID cannot be null");
        }
        
        return userId;  // ✅ Returns: 1
    }
    
    // 7. Fallback (shouldn't happen)
    throw new IllegalStateException("Principal is not CustomUserDetails");
}
```

**What happens:**
1. Gets `principal` from `Authentication`
2. Casts to `CustomUserDetails`
3. Extracts `userId` directly
4. **No database lookup needed!** ✅

---

## Visual Representation

```
┌─────────────────────────────────────────────────────────────┐
│                    HTTP REQUEST                            │
│  POST /api/v1/users/change-password                        │
│  Authorization: Basic bWlobm5xLjI0aXRAdkt1LnVkbi52bjoxMjM0NTY3OA== │
└──────────────────────┬────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│         BasicAuthenticationFilter                           │
│  Extracts: email="minhnq.24it@vku.udn.vn"                 │
│           password="12345678"                               │
└──────────────────────┬────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│         AuthenticationManager                               │
│  Delegates to: DaoAuthenticationProvider                    │
└──────────────────────┬────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│         CustomUserDetailsService.loadUserByUsername()      │
│  Query: findByEmailIgnoreCase("minhnq.24it@vku.udn.vn")   │
│  Returns: CustomUserDetails(userId=1, email=..., ...)     │
└──────────────────────┬────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│         DaoAuthenticationProvider                           │
│  Verifies: passwordEncoder.matches("12345678", hash)      │
│  Result: true ✅                                            │
└──────────────────────┬────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│         Authentication Object Created                      │
│  {                                                          │
│    principal: CustomUserDetails(userId=1, ...),           │
│    credentials: null,                                      │
│    authorities: [ROLE_USER],                              │
│    authenticated: true                                     │
│  }                                                          │
└──────────────────────┬────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│         SecurityContext.setAuthentication()               │
│  Stored in ThreadLocal for this request                    │
└──────────────────────┬────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│         UserController.changePassword()                    │
│  Authentication authentication ← Injected automatically!   │
│  Long userId = getUserIdFromAuthentication(authentication) │
│  userId = 1 (from principal, no DB lookup!)               │
└─────────────────────────────────────────────────────────────┘
```

---

## Understanding Authentication Object Methods

### What `authentication.getPrincipal()` Returns

```java
// In your controller:
Authentication authentication = ...;  // Injected by Spring

// Get principal
Object principal = authentication.getPrincipal();
// Type: CustomUserDetails
// Value: CustomUserDetails {
//   userId: 1,
//   email: "minhnq.24it@vku.udn.vn",
//   password: "$2a$11$hashedPassword...",
//   role: USER,
//   enabled: true
// }

// Cast to CustomUserDetails
CustomUserDetails userDetails = (CustomUserDetails) principal;

// Access fields
Long userId = userDetails.getUserId();        // 1
String email = userDetails.getEmail();        // "minhnq.24it@vku.udn.vn"
UserRole role = userDetails.getRole();        // USER
boolean enabled = userDetails.isEnabled();    // true
```

### What `authentication.getCredentials()` Returns

```java
Object credentials = authentication.getCredentials();
// Returns: null ✅

// Why null? Security best practice!
// Password is cleared after authentication to prevent accidental exposure
```

### What `authentication.getAuthorities()` Returns

```java
Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
// Returns: [GrantedAuthority { authority: "ROLE_USER" }]

// Check if user has specific role:
boolean isAdmin = authorities.stream()
    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
// false (user is USER, not ADMIN)

boolean isUser = authorities.stream()
    .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"));
// true
```

### What `authentication.getName()` Returns

```java
String name = authentication.getName();
// Returns: "minhnq.24it@vku.udn.vn"

// This is the same as:
CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
String email = userDetails.getEmail();
// "minhnq.24it@vku.udn.vn"

// getName() is a convenience method that returns the username (email in our case)
```

### What `authentication.getDetails()` Returns

```java
Object details = authentication.getDetails();
// Type: WebAuthenticationDetails
// Value: WebAuthenticationDetails {
//   remoteAddress: "127.0.0.1",
//   sessionId: "ABC123..."
// }

// Cast and access:
WebAuthenticationDetails webDetails = (WebAuthenticationDetails) details;
String ipAddress = webDetails.getRemoteAddress();  // "127.0.0.1"
String sessionId = webDetails.getSessionId();     // "ABC123..."
```

---

## Complete Example: Accessing All Authentication Data

```java
@GetMapping("/debug/my-auth")
public ResponseEntity<Map<String, Object>> debugMyAuth(
        Authentication authentication) {
    
    Map<String, Object> result = new HashMap<>();
    
    // 1. Principal (CustomUserDetails)
    if (authentication.getPrincipal() instanceof CustomUserDetails) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        result.put("userId", userDetails.getUserId());
        result.put("email", userDetails.getEmail());
        result.put("role", userDetails.getRole().name());
        result.put("enabled", userDetails.isEnabled());
    }
    
    // 2. Credentials (should be null)
    result.put("credentials", authentication.getCredentials());
    
    // 3. Authorities (roles)
    List<String> authorities = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());
    result.put("authorities", authorities);
    
    // 4. Details (request info)
    if (authentication.getDetails() instanceof WebAuthenticationDetails) {
        WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
        result.put("remoteAddress", details.getRemoteAddress());
        result.put("sessionId", details.getSessionId());
    }
    
    // 5. Status
    result.put("authenticated", authentication.isAuthenticated());
    result.put("name", authentication.getName());
    
    return ResponseEntity.ok(result);
}
```

**Response:**
```json
{
  "userId": 1,
  "email": "minhnq.24it@vku.udn.vn",
  "role": "USER",
  "enabled": true,
  "credentials": null,
  "authorities": ["ROLE_USER"],
  "remoteAddress": "127.0.0.1",
  "sessionId": "ABC123...",
  "authenticated": true,
  "name": "minhnq.24it@vku.udn.vn"
}
```

---

## Key Takeaways

1. **Authentication Object:**
   - Automatically injected into controller methods
   - Contains `principal` (CustomUserDetails), `credentials` (null), `authorities` (roles)
   - Available throughout request lifecycle

2. **Principal:**
   - The authenticated user's identity
   - In your code: `CustomUserDetails` object
   - Contains: `userId`, `email`, `role`, `enabled`
   - Extract userId: `((CustomUserDetails) authentication.getPrincipal()).getUserId()`

3. **No Database Lookup Needed:**
   - UserId is stored in `CustomUserDetails` (principal)
   - Extract directly: `getUserIdFromAuthentication(authentication)`
   - No need to query database again!

4. **HTTP Basic Auth:**
   - Format: `Authorization: Basic base64(email:password)`
   - Spring Security automatically extracts and validates
   - Credentials sent with each request (stateless)

5. **Best Practices:**
   - Store only essential fields in CustomUserDetails
   - Use case-insensitive email lookup
   - Clear credentials after authentication
   - Extract userId from principal (no DB lookup)

---

This walkthrough shows exactly what happens in YOUR codebase! 🎯

