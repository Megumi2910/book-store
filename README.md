# Book Store Application

A Spring Boot application for managing an online book store with user authentication, email verification, and order management.

## 🚀 Getting Started

### Prerequisites

- **Java 21** or higher
- **Maven 3.6+**
- **MySQL 8.0+** (or compatible database)
- **VS Code** with Java Extension Pack (for development)

### Environment Setup

This application uses **environment variables** for sensitive configuration. You have two options:

#### Option 1: VS Code Launch Configuration (Recommended for Development)

The project includes a `.vscode/launch.json` file with all required environment variables. Simply:

1. Open the project in VS Code
2. Go to Run and Debug (F5)
3. Select "BookStoreApplication"
4. Click Run

**Note:** `.vscode/` is already in `.gitignore`, so your credentials won't be committed to git.

#### Option 2: System Environment Variables

Set these environment variables in your system:

```bash
# Database Configuration
export DB_PASSWORD=your_database_password
export DB_USERNAME=root

# Admin User Configuration (for dev profile only)
export ADMIN_DEFAULT_PASSWORD=your_admin_password
export ADMIN_FIRST_NAME=Admin
export ADMIN_LAST_NAME=User
export ADMIN_EMAIL=admin@example.com

# Mail Configuration (Gmail SMTP)
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your_email@gmail.com
export MAIL_PASSWORD=your_app_password

# CORS Configuration
export CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080
```

**Windows PowerShell:**
```powershell
$env:DB_PASSWORD="your_database_password"
$env:ADMIN_DEFAULT_PASSWORD="your_admin_password"
# ... etc
```

**Windows CMD:**
```cmd
set DB_PASSWORD=your_database_password
set ADMIN_DEFAULT_PASSWORD=your_admin_password
# ... etc
```

### Database Setup

1. Create MySQL databases for each environment:
   ```sql
   CREATE DATABASE `book_store-dev`;
   CREATE DATABASE `book_store-qa`;
   CREATE DATABASE `book_store-prod`;
   ```

2. The application will automatically create tables using JPA/Hibernate (`ddl-auto: update` in dev profile).

### Running the Application

#### Using VS Code
1. Press `F5` or go to Run and Debug
2. Select "BookStoreApplication"
3. Application will start on `http://localhost:8080`

#### Using Maven
```bash
# Development profile (default)
mvn spring-boot:run

# QA profile
mvn spring-boot:run -Dspring-boot.run.profiles=qa

# Production profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

#### Using JAR
```bash
# Build the application
mvn clean package

# Run with dev profile
java -jar target/book_store-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

# Run with environment variables
java -jar target/book_store-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  -DDB_PASSWORD=your_password \
  -DMAIL_PASSWORD=your_mail_password
```

## 📋 Configuration

### Application Profiles

The application supports three profiles:

#### `dev` (Development)
- **Database:** `book_store-dev`
- **DDL Mode:** `update` (auto-creates/updates tables)
- **SQL Logging:** Enabled
- **Logging Level:** DEBUG
- **Data Seeder:** Creates admin user automatically
- **Open Session in View:** Enabled (for development convenience)

#### `qa` (Quality Assurance)
- **Database:** `book_store-qa`
- **DDL Mode:** `validate` (validates schema, no changes)
- **SQL Logging:** Disabled
- **Logging Level:** INFO
- **Open Session in View:** Disabled (best practice)

#### `prod` (Production)
- **Database:** `book_store-prod`
- **DDL Mode:** `validate` (validates schema, no changes)
- **SQL Logging:** Disabled
- **Logging Level:** WARN
- **Open Session in View:** Disabled (best practice)
- **Actuator:** Limited exposure, secured endpoints

### Configuration Files

- **`application.yml`**: Main configuration file with profile-specific settings
- **`.vscode/launch.json`**: VS Code debug configuration with environment variables (not committed to git)

### Type-Safe Configuration Properties

The application uses Spring Boot's `@ConfigurationProperties` for type-safe configuration:

- **`AdminProperties`**: Admin user settings (`admin.*`)
- **`CorsProperties`**: CORS settings (`app.cors.*`)

Benefits:
- ✅ Compile-time type checking
- ✅ IDE autocomplete support
- ✅ Centralized configuration
- ✅ Validation support

### Understanding CORS (Cross-Origin Resource Sharing)

**Important:** CORS is NOT related to database ports (like MySQL port 3030). It's about web browser security for frontend applications.

#### What is CORS?
CORS (Cross-Origin Resource Sharing) is a browser security feature that controls which **frontend web applications** (running in a browser) can make HTTP requests to your backend API.

#### Architecture Example:
```
┌─────────────────────────────────────────────────────────┐
│  User's Browser                                         │
│                                                          │
│  ┌──────────────┐         HTTP Request                 │
│  │ Frontend App │ ────────────────────────────────┐   │
│  │ :3000        │  (React/Vue/Angular)             │   │
│  │              │                                   │   │
│  └──────────────┘                                   │   │
│         │                                           │   │
│         │ CORS Policy Check                         │   │
│         │ (Browser checks if origin is allowed)     │   │
│         ▼                                           │   │
│  ┌──────────────┐                                   │   │
│  │ Backend API  │ ◄─────────────────────────────────┘   │
│  │ :8080        │  (Spring Boot)                        │
│  └──────────────┘                                       │
└─────────────────────────────────────────────────────────┘
         │
         │ JDBC Connection (No CORS needed)
         │ (Server-to-server communication)
         ▼
┌──────────────┐
│ MySQL        │
│ :3030        │  (Database - NOT related to CORS!)
└──────────────┘
```

#### CORS Configuration Explained:

```yaml
app:
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:8080}
```

**What this means:**
- `http://localhost:3000` = Your frontend application (React/Vue/Angular running on port 3000)
- `http://localhost:8080` = Your backend API (Spring Boot running on port 8080)

**Common Scenarios:**

1. **Frontend on port 3000, Backend on port 8080:**
   ```yaml
   allowed-origins: "http://localhost:3000"
   ```
   ✅ Frontend can call backend API

2. **Multiple frontend apps:**
   ```yaml
   allowed-origins: "http://localhost:3000,http://localhost:3001"
   ```
   ✅ Both frontends can call backend API

3. **Production with domain:**
   ```yaml
   allowed-origins: "https://myapp.com,https://www.myapp.com"
   ```
   ✅ Production frontend can call backend API

**Important Notes:**
- ❌ **CORS is NOT about database ports** (MySQL port 3030 is unrelated)
- ✅ **CORS is about browser security** - it only applies to HTTP requests from web browsers
- ✅ **Database connections don't use CORS** - Spring Boot connects directly to MySQL via JDBC (server-to-server)
- ✅ **CORS only matters when you have a separate frontend** - if you're using server-side rendering (Thymeleaf), you might not need CORS

#### When Do You Need CORS?
- ✅ You have a **separate frontend application** (React, Vue, Angular, etc.)
- ✅ Frontend runs on a **different port** than your backend
- ✅ Frontend makes **AJAX/fetch requests** to your backend API

#### When DON'T You Need CORS?
- ❌ **Server-side rendering only** (Thymeleaf templates served from same origin)
- ❌ **Database connections** (MySQL, PostgreSQL, etc. - these are server-to-server)
- ❌ **Backend-to-backend** communication (microservices talking to each other)

## 🔒 Security

### Password Encoding
- Uses **BCrypt** with strength factor of **11**
- All passwords are hashed before storage

### Spring Security Configuration
- **Public Endpoints:** Registration, login, verification, static resources
- **CSRF Protection:** Enabled for web forms, disabled for REST APIs
- **CORS:** Configurable via `app.cors.allowed-origins`
- **Session Management:** HTTP sessions with JSESSIONID cookie

### Admin User Creation
- Admin user is automatically created in `dev` profile only
- Configured via `AdminProperties` and environment variables
- Password is hashed using BCrypt

## 📧 Email Configuration

### Gmail Setup
1. Enable 2-Factor Authentication on your Gmail account
2. Generate an App Password:
   - Go to Google Account → Security → App passwords
   - Create a new app password for "Mail"
   - Use this password in `MAIL_PASSWORD` environment variable

### Email Templates
Email functionality is used for:
- User registration verification
- Password reset tokens
- Account notifications

## 🏗️ Project Structure

```
src/main/java/com/second_project/book_store/
├── config/
│   ├── properties/          # Type-safe configuration classes
│   ├── DataSeeder.java      # Dev profile data initialization
│   └── WebSecurityConfig.java
├── controller/
│   └── api/                 # REST API controllers
├── entity/                  # JPA entities
├── model/                   # DTOs (Data Transfer Objects)
├── repository/              # Spring Data JPA repositories
├── service/                 # Business logic
│   └── impl/                # Service implementations
├── exception/               # Custom exceptions
│   └── handler/             # Exception handlers
├── event/                   # Spring events
│   └── listener/            # Event listeners
└── validator/               # Custom validators
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run with specific profile
mvn test -Dspring.profiles.active=dev
```

## 📝 API Endpoints

### Public Endpoints
- `POST /api/v1/users/register` - User registration
- `GET /api/v1/users/verify-registration` - Verify registration token
- `GET /api/v1/users/resend-verify-token` - Resend verification token

### Protected Endpoints
- All other endpoints require authentication

## 🛠️ Development Tips

### Hot Reload
The application includes Spring Boot DevTools for automatic restart on code changes.

### Database Inspection
- **Dev Profile:** SQL queries are logged to console
- Use a database client (e.g., MySQL Workbench, DBeaver) to inspect data

### Logging
- Check console output for application logs
- Logging levels are profile-specific (DEBUG in dev, WARN in prod)

## 🚨 Troubleshooting

### Application Won't Start
1. **Check environment variables:** Ensure all required variables are set
2. **Check database connection:** Verify MySQL is running and credentials are correct
3. **Check port availability:** Ensure port 8080 is not in use

### Admin User Not Created
- Admin user is only created in `dev` profile
- Check logs for errors during initialization
- Verify `ADMIN_EMAIL` and `ADMIN_DEFAULT_PASSWORD` are set

### Email Not Sending
- Verify Gmail App Password is correct
- Check `MAIL_USERNAME` and `MAIL_PASSWORD` environment variables
- Ensure 2FA is enabled on Gmail account

## 📚 Best Practices Applied

✅ **Environment Variables** - Sensitive data never in code  
✅ **Profile-Based Configuration** - Different settings per environment  
✅ **Type-Safe Configuration** - Using `@ConfigurationProperties`  
✅ **Proper Logging** - SLF4J instead of System.out.println  
✅ **Error Handling** - Graceful failure handling  
✅ **Security** - Password hashing, CSRF protection, CORS configuration  
✅ **Documentation** - Comprehensive code comments and README  

## 📄 License

[Your License Here]

## 👤 Author

[Your Name]

---

**Note:** Remember to never commit sensitive information like passwords or API keys to version control. The `.vscode/launch.json` file is already excluded via `.gitignore`.

