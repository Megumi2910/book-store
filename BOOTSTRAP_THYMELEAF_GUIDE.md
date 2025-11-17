# Bootstrap & Thymeleaf Syntax Guide

This guide explains the Bootstrap CSS framework and Thymeleaf templating syntax used in your Book Store templates.

---

## 📦 BOOTSTRAP 5 - CSS Framework

Bootstrap is a CSS framework that provides pre-built classes for styling. You don't write custom CSS - you just add classes to HTML elements.

### **1. Grid System (Layout)**

Bootstrap uses a 12-column grid system for responsive layouts:

```html
<div class="container">          <!-- Centered container with max-width -->
    <div class="row">            <!-- Horizontal row -->
        <div class="col-md-6">   <!-- Column: 6/12 width on medium+ screens -->
```

**Classes Explained:**
- `container` - Creates a responsive container with max-width and horizontal padding
- `row` - Creates a horizontal group of columns
- `col-md-6` - Column that takes 6 out of 12 columns (50% width) on medium screens and up
  - `col-md-4` = 33% width
  - `col-md-5` = ~42% width
  - `col-12` = 100% width (full width on all screens)
- `justify-content-center` - Centers columns horizontally within the row

**Example from `reset-password.html` (line 10-12):**
```html
<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-6">
```
This creates a centered container, with a row that centers its content, containing a column that's 50% width on medium+ screens.

---

### **2. Spacing Utilities**

Bootstrap provides margin and padding utilities:

**Margin:**
- `mt-5` = margin-top (large spacing)
- `mt-3` = margin-top (medium spacing)
- `mt-2` = margin-top (small spacing)
- `mb-3` = margin-bottom
- `mb-0` = margin-bottom: 0

**Padding:**
- `p-3` = padding all sides
- `px-4` = padding left/right
- `py-2` = padding top/bottom

**Example:** `<div class="mt-3 text-center">` adds top margin and centers text.

---

### **3. Card Component**

Cards are Bootstrap's container for content:

```html
<div class="card shadow">
    <div class="card-header bg-primary text-white">
        <h3>Title</h3>
    </div>
    <div class="card-body">
        Content here
    </div>
</div>
```

**Classes:**
- `card` - Creates a card container
- `shadow` - Adds a subtle shadow effect
- `card-header` - Header section of the card
- `card-body` - Main content area
- `bg-primary` - Blue background color
- `text-white` - White text color

**Example from `login.html` (line 13-16):**
```html
<div class="card shadow">
    <div class="card-header bg-primary text-white">
        <h3 class="text-center mb-0">Login to Book Store</h3>
    </div>
```

---

### **4. Form Components**

Bootstrap provides styled form elements:

```html
<div class="mb-3">
    <label for="password" class="form-label">Password</label>
    <input type="password" class="form-control" id="password">
    <div class="form-text">Help text</div>
</div>
```

**Classes:**
- `mb-3` - Margin bottom for spacing between form fields
- `form-label` - Styles the label
- `form-control` - Styles input fields (adds padding, border, etc.)
- `form-text` - Small help text below inputs
- `form-check` - Container for checkboxes/radios
- `form-check-input` - Styles checkbox/radio inputs
- `form-check-label` - Styles checkbox/radio labels

**Example from `reset-password.html` (line 35-45):**
```html
<div class="mb-3">
    <label for="password" class="form-label">New Password</label>
    <input type="password" class="form-control" id="password" name="password">
    <div class="form-text">Password must be at least 8 characters.</div>
</div>
```

---

### **5. Buttons**

```html
<button class="btn btn-primary btn-lg">Click Me</button>
```

**Classes:**
- `btn` - Base button class (required)
- `btn-primary` - Blue button color
- `btn-lg` - Large button size
- `btn-sm` - Small button size
- Other colors: `btn-secondary`, `btn-success`, `btn-danger`, `btn-warning`

**Example:** `<button class="btn btn-primary btn-lg">Login</button>`

---

### **6. Alerts (Messages)**

Alerts display success/error messages:

```html
<div class="alert alert-success alert-dismissible fade show" role="alert">
    <span>Success message</span>
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
</div>
```

**Classes:**
- `alert` - Base alert class
- `alert-success` - Green alert (success)
- `alert-danger` - Red alert (error)
- `alert-info` - Blue alert (information)
- `alert-warning` - Yellow alert (warning)
- `alert-dismissible` - Makes alert closable
- `fade show` - Fade animation when showing
- `btn-close` - Close button (X)
- `data-bs-dismiss="alert"` - Bootstrap JS attribute to close alert

**Example from `login.html` (line 19-22):**
```html
<div th:if="${success}" class="alert alert-success alert-dismissible fade show">
    <span th:text="${success}"></span>
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
</div>
```

---

### **7. Navigation Bar**

```html
<nav class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="container">
        <a class="navbar-brand" href="/">Brand</a>
        <div class="collapse navbar-collapse">
            <ul class="navbar-nav ms-auto">
                <li class="nav-item">
                    <a class="nav-link" href="/login">Login</a>
                </li>
            </ul>
        </div>
    </div>
</nav>
```

**Classes:**
- `navbar` - Base navbar class
- `navbar-expand-lg` - Expands on large screens, collapses on mobile
- `navbar-dark` - Light text color (for dark backgrounds)
- `navbar-brand` - Brand/logo link
- `navbar-nav` - Navigation links container
- `nav-item` - Individual navigation item
- `nav-link` - Navigation link styling
- `ms-auto` - Margin start auto (pushes content to right)
- `dropdown` - Dropdown menu container
- `dropdown-toggle` - Dropdown trigger button
- `dropdown-menu` - Dropdown menu container
- `dropdown-item` - Individual dropdown item

**Example from `index.html` (line 12-44):** Full navigation bar with dropdown menu.

---

### **8. Text Utilities**

```html
<div class="text-center">Centered text</div>
<div class="text-danger">Red text</div>
<div class="text-muted">Gray text</div>
```

**Classes:**
- `text-center` - Centers text
- `text-left` - Left-aligns text
- `text-right` - Right-aligns text
- `text-danger` - Red text color
- `text-success` - Green text color
- `text-muted` - Gray text color
- `text-white` - White text color
- `text-decoration-none` - Removes underline from links

---

### **9. Display Utilities**

```html
<div class="d-grid">Full width button</div>
```

**Classes:**
- `d-grid` - Makes element display as grid (useful for full-width buttons)
- `d-flex` - Flexbox display
- `d-none` - Hides element

**Example:** `<div class="d-grid"><button>Full Width Button</button></div>`

---

### **10. Badges**

```html
<span class="badge bg-primary">USER</span>
```

**Classes:**
- `badge` - Creates a badge/pill element
- `bg-primary` - Blue background

---

## 🍃 THYMELEAF - Template Engine

Thymeleaf is a Java template engine that processes HTML templates on the server and injects dynamic data.

### **1. Namespace Declaration**

```html
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
```

- `xmlns:th` - Thymeleaf namespace (allows `th:` attributes)
- `xmlns:sec` - Spring Security namespace (allows `sec:` attributes)

---

### **2. Conditional Rendering (`th:if`)**

Shows/hides elements based on conditions:

```html
<div th:if="${success}">This shows if 'success' variable exists and is truthy</div>
<div th:if="${error}">This shows if 'error' variable exists</div>
```

**Example from `reset-password.html` (line 19):**
```html
<div th:if="${success}" class="alert alert-success">
    <span th:text="${success}"></span>
</div>
```
This div only appears if the controller passes a `success` variable in the model.

---

### **3. Text Output (`th:text`)**

Inserts text content from server variables:

```html
<span th:text="${userEmail}">user@example.com</span>
```

- `${userEmail}` - Expression that gets the `userEmail` variable from the model
- The text `user@example.com` is a fallback (shown in static HTML preview)

**Example from `index.html` (line 28):**
```html
<span th:text="${userEmail}">user@example.com</span>
```
Displays the user's email from the server, or shows "user@example.com" in static preview.

---

### **4. Attribute Values (`th:value`)**

Sets attribute values dynamically:

```html
<input type="hidden" name="token" th:value="${token}">
```

**Example from `reset-password.html` (line 33):**
```html
<input type="hidden" name="token" th:value="${token}">
```
Sets the input's value to the `token` variable from the server.

---

### **5. Links (`th:href`)**

Creates dynamic URLs:

```html
<a th:href="@{/login}">Login</a>
<a th:href="@{/reset-password}">Reset Password</a>
```

- `@{/login}` - URL expression (Thymeleaf handles context path automatically)
- `@{/reset-password}` - Creates URL like `/book_store/reset-password` (if context path exists)

**Example from `login.html` (line 79):**
```html
<a th:href="@{/forgot-password}">Forgot your password?</a>
```

---

### **6. Form Actions (`th:action`)**

Sets form submission URL:

```html
<form th:action="@{/login}" method="post">
```

**Example from `login.html` (line 38):**
```html
<form th:action="@{/login}" method="post">
```
Form submits to `/login` endpoint.

---

### **7. Form Binding (`th:object` and `th:field`)**

Binds form to a Java object:

```html
<form th:action="@{/reset-password}" method="post" th:object="${resetPasswordRequest}">
    <input th:field="*{password}" name="password">
    <input th:field="*{matchingPassword}" name="matchingPassword">
</form>
```

- `th:object="${resetPasswordRequest}"` - Binds form to the `resetPasswordRequest` object
- `th:field="*{password}"` - Binds input to `resetPasswordRequest.password` field
  - `*{...}` means "from the object bound to this form"
  - Automatically sets `name`, `id`, and `value` attributes

**Example from `reset-password.html` (line 31, 41, 58):**
```html
<form th:action="@{/reset-password}" method="post" th:object="${resetPasswordRequest}">
    <input th:field="*{password}" name="password">
    <input th:field="*{matchingPassword}" name="matchingPassword">
</form>
```

---

### **8. CSRF Token**

Spring Security requires CSRF tokens for POST requests:

```html
<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
```

- `${_csrf.parameterName}` - Gets the CSRF parameter name (usually `_csrf`)
- `${_csrf.token}` - Gets the actual CSRF token value

**Example from `login.html` (line 40):**
```html
<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
```

---

### **9. Validation Errors (`th:errors`)**

Displays form validation errors:

```html
<div th:if="${#fields.hasErrors('password')}" class="text-danger">
    <small th:errors="*{password}"></small>
</div>
```

- `${#fields.hasErrors('password')}` - Checks if `password` field has errors
- `th:errors="*{password}"` - Displays error messages for the `password` field

**Example from `reset-password.html` (line 47-49):**
```html
<div th:if="${#fields.hasErrors('password')}" class="text-danger">
    <small th:errors="*{password}"></small>
</div>
```

---

### **10. Spring Security (`sec:authorize`)**

Shows/hides content based on authentication status:

```html
<li sec:authorize="!isAuthenticated()">
    <a th:href="@{/login}">Login</a>
</li>

<div sec:authorize="isAuthenticated()">
    Welcome, authenticated user!
</div>
```

- `sec:authorize="!isAuthenticated()"` - Shows if user is NOT logged in
- `sec:authorize="isAuthenticated()"` - Shows if user IS logged in

**Example from `index.html` (line 20-21, 26, 52, 58):**
```html
<li class="nav-item" sec:authorize="!isAuthenticated()">
    <a class="nav-link" th:href="@{/login}">Login</a>
</li>

<div class="alert alert-success" sec:authorize="isAuthenticated()">
    Welcome back!
</div>
```

---

## 🔄 HOW THEY WORK TOGETHER

**Example from `reset-password.html`:**

```html
<div th:if="${success}" class="alert alert-success alert-dismissible fade show">
    <span th:text="${success}"></span>
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
</div>
```

**Breakdown:**
1. **Thymeleaf (`th:if`)**: Checks if `success` variable exists → shows/hides the div
2. **Bootstrap (`alert alert-success`)**: Styles it as a green success message box
3. **Thymeleaf (`th:text`)**: Inserts the success message text
4. **Bootstrap (`btn-close`, `data-bs-dismiss`)**: Adds a close button with Bootstrap JS functionality

---

## 📝 QUICK REFERENCE

### Bootstrap Classes Used:
- Layout: `container`, `row`, `col-md-*`, `justify-content-center`
- Spacing: `mt-*`, `mb-*`, `p-*`
- Cards: `card`, `card-header`, `card-body`, `shadow`
- Forms: `form-control`, `form-label`, `form-text`, `form-check`
- Buttons: `btn`, `btn-primary`, `btn-lg`
- Alerts: `alert`, `alert-success`, `alert-danger`, `alert-dismissible`
- Text: `text-center`, `text-danger`, `text-muted`
- Display: `d-grid`
- Navigation: `navbar`, `nav-link`, `dropdown`

### Thymeleaf Attributes Used:
- `th:if` - Conditional rendering
- `th:text` - Text output
- `th:value` - Attribute value
- `th:href` - Link URL
- `th:action` - Form action URL
- `th:object` - Form object binding
- `th:field` - Form field binding
- `th:name` - Dynamic attribute name
- `th:errors` - Validation errors
- `sec:authorize` - Security-based rendering

---

## 🎯 KEY TAKEAWAYS

1. **Bootstrap** = Pre-built CSS classes for styling (no custom CSS needed)
2. **Thymeleaf** = Server-side template engine that injects dynamic data
3. **Together** = Bootstrap handles appearance, Thymeleaf handles dynamic content
4. **`th:` attributes** = Processed on server, removed in final HTML
5. **Bootstrap classes** = Applied in browser, control visual appearance

Both work together seamlessly - Thymeleaf generates the HTML with Bootstrap classes, and Bootstrap styles it in the browser!

