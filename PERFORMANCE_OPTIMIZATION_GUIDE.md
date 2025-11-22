# 🚀 Performance Optimization Guide - When and How

## 📊 **Current Setup (✅ Already Configured)**

Your application now has **automatic batch fetching** enabled:

```yaml
hibernate:
  default_batch_fetch_size: 10  # Fetches lazy collections in batches
  jdbc.batch_size: 20            # Batches INSERT/UPDATE operations
  order_inserts: true            # Orders SQL for better batching
  order_updates: true
```

**What this means:**
- Loading 100 users with orders: Instead of 101 queries (1 + 100), you get ~11 queries (1 + 10 batches)
- **90% improvement with zero code changes!** ⭐

---

## 🎯 **When Do You Need More Optimization?**

### **Signs You DON'T Have Performance Problems:**
- ✅ Page loads in < 1 second
- ✅ Query logs show < 10 queries per request
- ✅ Database CPU < 50%
- ✅ Users aren't complaining

**Verdict:** You're good! Don't optimize further.

---

### **Signs You HAVE Performance Problems (N+1 Queries):**
- ❌ Page loads take > 2 seconds
- ❌ Query logs show 100+ queries per request
- ❌ Database CPU spiking
- ❌ Logs show repetitive SELECT queries

**Example N+1 Pattern in Logs:**
```sql
-- Loading users (1 query)
SELECT * FROM users WHERE id IN (1,2,3,4,5...10)

-- Then 10 queries for orders (even with batch fetching)
SELECT * FROM orders WHERE user_id IN (1,2,3,4,5,6,7,8,9,10)
-- This is GOOD! (1 batch query instead of 10)

-- But if you see 100+ individual queries:
SELECT * FROM orders WHERE user_id = 1
SELECT * FROM orders WHERE user_id = 2
... (98 more queries)
-- This is BAD! (batch fetching not working or too small batch size)
```

**Verdict:** Time to optimize!

---

## 🛠️ **Optimization Strategies (In Order of Simplicity)**

### **Strategy 1: Increase Batch Size (Simplest)**

If batch fetching isn't enough, increase the batch size:

```yaml
hibernate:
  default_batch_fetch_size: 50  # Increase from 10 to 50
```

**When to use:**
- You have N+1 but not severe
- Batch queries are working but need bigger batches

**Pros:**
- One line change
- No code changes
- Works for all relationships

**Cons:**
- Larger result sets in memory
- Not effective if you always need the data

---

### **Strategy 2: JOIN FETCH in Queries (Recommended)**

**For specific, known use cases where you ALWAYS need related data:**

#### **Example 1: User Profile Page**

```java
// UserRepository.java
@Query("SELECT u FROM User u LEFT JOIN FETCH u.orders WHERE u.userId = :id")
Optional<User> findByIdWithOrders(@Param("id") Long id);
```

**Use in service:**
```java
public UserProfileDto getUserProfile(Long userId) {
    User user = userRepository.findByIdWithOrders(userId)
        .orElseThrow(...);
    // user.getOrders() is already loaded - no additional query!
    return toDto(user);
}
```

**Result:** 1 query instead of 2+ (or N+1)

---

#### **Example 2: Order Details Page**

```java
// OrderRepository.java
@Query("""
    SELECT o FROM Order o 
    LEFT JOIN FETCH o.orderItems oi
    LEFT JOIN FETCH oi.book
    WHERE o.orderId = :id
""")
Optional<Order> findByIdWithItems(@Param("id") Long id);
```

**Result:** 1 query loads Order + Items + Books (instead of 3+ queries)

---

#### **Example 3: Book with Reviews**

```java
// BookRepository.java
@Query("""
    SELECT b FROM Book b 
    LEFT JOIN FETCH b.reviews r
    LEFT JOIN FETCH r.user
    WHERE b.bookId = :id
""")
Optional<Book> findByIdWithReviews(@Param("id") Long id);
```

**Result:** 1 query loads Book + Reviews + Users

---

### **Strategy 3: Entity Graphs (Advanced - Only If Needed)**

**Use when:**
- You have multiple fetch scenarios for same entity
- You want dynamic fetch strategies
- JOIN FETCH queries become too complex

#### **Example: User Entity Graph**

**Step 1: Define on Entity**
```java
@Entity
@NamedEntityGraph(
    name = "User.withOrders",
    attributeNodes = @NamedAttributeNode("orders")
)
@NamedEntityGraph(
    name = "User.withOrdersAndItems",
    attributeNodes = @NamedAttributeNode(
        value = "orders",
        subgraph = "orderItems"
    ),
    subgraphs = @NamedSubgraph(
        name = "orderItems",
        attributeNodes = @NamedAttributeNode("orderItems")
    )
)
public class User {
    // ... entity fields
}
```

**Step 2: Use in Repository**
```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Default method (batch fetching)
    Optional<User> findByEmail(String email);
    
    // With orders only
    @EntityGraph("User.withOrders")
    Optional<User> findWithOrdersByEmail(String email);
    
    // With orders and items
    @EntityGraph("User.withOrdersAndItems")
    Optional<User> findWithFullOrdersByEmail(String email);
}
```

**Step 3: Use in Service**
```java
// Use case 1: Just need user info
User user = userRepository.findByEmail(email).orElseThrow(...);

// Use case 2: Need user + orders
User userWithOrders = userRepository.findWithOrdersByEmail(email).orElseThrow(...);

// Use case 3: Need everything
User fullUser = userRepository.findWithFullOrdersByEmail(email).orElseThrow(...);
```

**Pros:**
- Very flexible
- Reusable across multiple queries
- Can handle complex graphs

**Cons:**
- More code to maintain
- Learning curve
- Overkill for simple cases

---

## 📈 **Performance Comparison**

### **Scenario: Load User Profile with 10 Orders**

#### **Without Optimization:**
```
Query 1: SELECT * FROM users WHERE id = 1
Query 2: SELECT * FROM orders WHERE user_id = 1
Total: 2 queries, ~50ms
```

#### **With Batch Fetching (Current Setup):**
```
Query 1: SELECT * FROM users WHERE id = 1
Query 2: SELECT * FROM orders WHERE user_id = 1
Total: 2 queries, ~50ms (same, but helps with multiple users!)
```

#### **With JOIN FETCH:**
```
Query 1: SELECT u.*, o.* FROM users u 
         LEFT JOIN orders o ON u.id = o.user_id 
         WHERE u.id = 1
Total: 1 query, ~30ms (faster!)
```

### **Scenario: Load 100 Users with Orders**

#### **Without Optimization (N+1 Problem):**
```
Query 1: SELECT * FROM users (100 users)
Query 2-101: SELECT * FROM orders WHERE user_id = ? (100 queries!)
Total: 101 queries, ~2000ms 🐌
```

#### **With Batch Fetching (Current Setup):**
```
Query 1: SELECT * FROM users (100 users)
Query 2-11: SELECT * FROM orders WHERE user_id IN (?,?,?...) (10 batch queries)
Total: 11 queries, ~300ms ⚡ (85% faster!)
```

#### **With JOIN FETCH:**
```
Query 1: SELECT u.*, o.* FROM users u LEFT JOIN orders o ON u.id = o.user_id
Total: 1 query, ~200ms ⚡⚡ (90% faster!)
```

---

## 🎯 **Recommended Approach for Your Project**

### **Phase 1: Now (✅ Done!)**
```yaml
# Batch fetching configured - no code changes needed
default_batch_fetch_size: 10
```

### **Phase 2: As You Build Features**

Add **JOIN FETCH** queries for known use cases:

```java
// Add these as you discover the need

// User profile page
@Query("SELECT u FROM User u LEFT JOIN FETCH u.orders WHERE u.userId = :id")
Optional<User> findByIdWithOrders(@Param("id") Long id);

// Order details page  
@Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.orderId = :id")
Optional<Order> findByIdWithItems(@Param("id") Long id);

// Book catalog with details
@Query("SELECT b FROM Book b LEFT JOIN FETCH b.bookDetail WHERE b.bookId = :id")
Optional<Book> findByIdWithDetail(@Param("id") Long id);
```

### **Phase 3: Only If Needed**

If you have complex scenarios with multiple fetch patterns, **then** add Entity Graphs.

**Signs you need Entity Graphs:**
- Multiple fetch patterns for same entity (5+ different ways to load User)
- Complex nested relationships
- Dynamic fetch requirements based on user role/context

---

## 🔍 **How to Monitor Performance**

### **Enable Query Logging (Dev Environment)**

Already enabled in your `application-dev.yml`:
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true  # Shows which entity caused query
```

### **What to Look For in Logs**

#### **Good Pattern:**
```sql
-- Single query or small number of queries
Hibernate: SELECT u.* FROM users u WHERE u.id = ?
Hibernate: SELECT o.* FROM orders o WHERE o.user_id IN (?,?,?,?,?)
-- 2 queries total ✅
```

#### **Bad Pattern (N+1):**
```sql
Hibernate: SELECT u.* FROM users
Hibernate: SELECT o.* FROM orders WHERE user_id = 1
Hibernate: SELECT o.* FROM orders WHERE user_id = 2
Hibernate: SELECT o.* FROM orders WHERE user_id = 3
... (97 more queries)
-- 100 queries total ❌
```

### **Use Hibernate Statistics (Optional)**

Enable in `application-dev.yml`:
```yaml
spring:
  jpa:
    properties:
      hibernate:
        generate_statistics: true
```

**Check in logs:**
```
Statistics:
  Sessions opened: 1
  Queries executed: 2
  Query cache hits: 0
  Collections loaded: 1
```

---

## 💡 **Pro Tips**

### **1. Pagination is Your Friend**

Instead of loading 1000 users:
```java
// Load 20 at a time
Page<User> users = userRepository.findAll(PageRequest.of(0, 20));
```

### **2. DTOs Over Full Entities**

For list views, project only what you need:
```java
@Query("SELECT new com.example.UserSummaryDto(u.id, u.firstName, u.email) FROM User u")
List<UserSummaryDto> findAllSummaries();
```

**Benefits:**
- Less data transferred
- No lazy loading issues
- Faster queries

### **3. Read-Only Queries**

For read operations, use `@Transactional(readOnly = true)`:
```java
@Transactional(readOnly = true)
public List<UserDto> getAllUsers() {
    // Hibernate optimizes for read-only
}
```

---

## 🎓 **Summary: What to Remember**

1. **✅ Batch fetching configured** - 80% of optimization done!

2. **Use JOIN FETCH** for specific cases:
   - User profile → load user + orders
   - Order details → load order + items
   - Book details → load book + reviews

3. **Entity Graphs** only if:
   - Multiple complex fetch patterns
   - Dynamic requirements
   - JOIN FETCH becomes too messy

4. **Monitor first, optimize later**:
   - Enable query logging in dev
   - Look for N+1 patterns
   - Optimize hot paths only

5. **Don't premature optimize**:
   - If page loads fast → don't touch it
   - If query count is low → don't optimize
   - If users happy → you're done

---

## 🚀 **Next Steps**

1. **Test your current setup** - It's already optimized!
2. **Add JOIN FETCH** as you build features and discover needs
3. **Monitor query logs** in development
4. **Only add Entity Graphs** if you have complex scenarios

Your application is now **production-ready** with smart default optimizations! 🎉

---

**Questions?** Check query logs and add JOIN FETCH for specific slow pages.

**Still slow?** Then it's time for Entity Graphs or caching strategies.

