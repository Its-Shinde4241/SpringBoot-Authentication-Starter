# SpringBoot Authentication Starter

A minimal Spring Boot starter providing JWT-based authentication with user registration, login, and secured endpoints.

## Tech Stack
- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Database persistence
- **MySQL** - Database (runtime)
- **JJWT 0.12.6** (io.jsonwebtoken) - JWT token handling
- **Lombok** - Code generation
- **Maven** - Build tool

## Project Structure
```
com.app.securitydemo/
├── controller/          # REST endpoints
│   ├── AuthController       # Registration & login
│   ├── HealthController     # Health check
│   └── HomeController       # Secured home endpoint
├── Service/             # Business logic
│   ├── JwtService          # JWT generation & validation
│   └── UserService         # User management & auth
├── model/               # Domain entities
│   ├── User                # User entity (JPA)
│   └── MyUserDetails       # UserDetails implementation
├── Repo/                # Data access
│   └── UserRepo            # JPA repository
├── dto/                 # Data transfer objects
│   ├── RegisterRequest
│   ├── LoginRequest
│   └── UserResponse
├── utils/               # Utilities
│   └── JwtAuthFilter       # JWT authentication filter
└── config/              # Configuration
    └── SecurityConfig      # Spring Security config
```

## Features
- ✅ User registration with email validation
- ✅ User login with JWT token generation
- ✅ JWT-based authentication for secured endpoints
- ✅ Token validation on every request
- ✅ Email uniqueness enforcement
- ✅ Password encoding (BCrypt)
- ✅ Health check endpoint

## Prerequisites
- JDK 21 or higher
- Maven 3.6+
- MySQL 8.0+ (or compatible database)
- IDE (IntelliJ IDEA recommended)

## Quick Start

### 1. Clone and navigate to project
```bash
cd C:\Coding_Projects\springbootProjects\SecurityDemo
```

### 2. Configure database
Edit `src/main/resources/application.properties`:
```properties
# Database configuration
spring.datasource.url=jdbc:mysql://localhost:3306/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

# JWT configuration
jwt.secret=YourSecureSecretKeyHereMustBeAtLeast256BitsForHS256Algorithm
```

**Important**: Generate a secure `jwt.secret` (minimum 32 characters for HS256). Example generation:
```java
// Use this to generate a secure key
byte[] keyBytes = Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded();
String base64Key = Base64.getEncoder().encodeToString(keyBytes);
```

### 3. Build the project
```bash
mvn clean package
```

### 4. Run the application
```bash
mvn spring-boot:run
```
Or run `SecurityDemoApplication.java` from your IDE.

The application starts on `http://localhost:8080`

## API Endpoints

### Public Endpoints

#### Register New User
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePassword123"
}
```

**Response (200 OK):**
```json
{
  "id": "uuid",
  "name": "John Doe",
  "email": "john@example.com",
  "createdAt": "2025-11-17T10:30:00"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePassword123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 86400000
}
```

#### Health Check
```http
GET /health
```

**Response (200 OK):**
```json
{
  "status": "UP"
}
```

### Secured Endpoints

#### Home (requires authentication)
```http
GET /home
Authorization: Bearer <your-jwt-token>
```

**Response (200 OK):**
```
Welcome to the home page!
```

## Email Validation & Constraints

The `User` entity enforces email uniqueness and non-null constraints:

```java
@Column(unique = true, nullable = false)
private String email;
```

**Best practices:**
- Add `@Email` and `@NotBlank` validation annotations to DTOs
- Use `@Valid` annotation on controller method parameters
- This ensures friendly validation errors before database constraints trigger

Example:
```java
@Email(message = "Email should be valid")
@NotBlank(message = "Email is required")
private String email;
```

## JWT Implementation Details

### JJWT Version & API Changes

This project uses **JJWT 0.12.6**. Important notes:

#### ✅ Correct Usage (Current Implementation)

**Token Generation:**
```java
Jwts.builder()
    .setClaims(claims)
    .setIssuedAt(new Date())
    .setExpiration(new Date(System.currentTimeMillis() + 86400000))
    .signWith(key)  // Use Key object
    .compact();
```

**Token Parsing:**
```java
Jwts.parserBuilder()
    .setSigningKey(key)
    .build()
    .parseClaimsJws(token)
    .getBody();
```

**Key Generation:**
```java
Keys.hmacShaKeyFor(secretKey.getBytes())
```

#### ❌ Deprecated/Removed Methods

- `Jwts.parser()` - Use `Jwts.parserBuilder()` instead
- `signWith(Key, SignatureAlgorithm)` - Use `signWith(Key)` instead
- `verifyWith()` - **Does not exist in JJWT**. Use `setSigningKey()` on parser builder
- `claims()`, `issuedAt()`, `expiration()` - Use `setClaims()`, `setIssuedAt()`, `setExpiration()`

### Token Expiration
- Default: 24 hours (86400000 milliseconds)
- Configurable in `JwtService.generateToken()`

## Troubleshooting

### "Cannot resolve method 'verifyWith()'"
**Solution:** There is no `verifyWith()` method in JJWT. Use the correct API:
```java
Jwts.parserBuilder()
    .setSigningKey(key)
    .build()
    .parseClaimsJws(token);
```

### "SignatureException: JWT signature does not match"
**Causes:**
- Secret key mismatch between token generation and validation
- Secret key too short (must be ≥256 bits for HS256)

**Solution:** Ensure `jwt.secret` is the same value used for both signing and verification, and is sufficiently long.

### "Weak keys forbidden"
**Solution:** Generate a cryptographically secure key:
```java
SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
String secret = Base64.getEncoder().encodeToString(key.getEncoded());
```

### IDE shows unresolved JJWT methods
**Solution:**
1. Refresh Maven dependencies: `mvn clean install -U`
2. Invalidate IDE caches and restart
3. Verify all three JJWT dependencies are present in `pom.xml`:
   - `jjwt-api`
   - `jjwt-impl`
   - `jjwt-jackson`

### Database connection errors
**Solution:**
- Verify MySQL is running
- Check database credentials in `application.properties`
- Ensure database exists or set `spring.jpa.hibernate.ddl-auto=create`

## Testing

Run unit tests:
```bash
mvn test
```

Run with coverage:
```bash
mvn test jacoco:report
```

## Security Best Practices

1. **Never commit `jwt.secret`** - Use environment variables or external config
2. **Use strong passwords** - Implement password strength validation
3. **HTTPS in production** - Never transmit tokens over HTTP
4. **Token rotation** - Implement refresh tokens for long-lived sessions
5. **Rate limiting** - Protect login/register endpoints from brute force
6. **Input validation** - Validate all user inputs on DTOs
7. **SQL injection protection** - Use JPA parameterized queries (already handled)

## Environment Variables (Production)

```bash
export JWT_SECRET=your-production-secret-key
export DB_URL=jdbc:mysql://production-db:3306/app
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_password
```

Update `application.properties`:
```properties
jwt.secret=${JWT_SECRET}
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

MIT License - feel free to use this project for learning and development.

## Additional Resources

- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/index.html)
- [JJWT Documentation](https://github.com/jwtk/jjwt)
- [JWT.io](https://jwt.io/) - Decode and verify JWT tokens
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)

## Support

For issues or questions, please open an issue in the repository.
