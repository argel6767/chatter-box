# ChatterBox API 🚀

The main Spring Boot API service for ChatterBox, providing authentication, real-time messaging, and user management capabilities.

## 🛠️ Tech Stack

- **Spring Boot 3.3.0** (Java 21)
- **Spring Security** - Authentication & authorization
- **Spring WebSocket** - Real-time messaging with STOMP
- **Spring Data JPA** - Database operations
- **PostgreSQL** - Primary database
- **H2 Database** - Testing database
- **JWT** - Token-based authentication
- **Resilience4j** - Rate limiting & circuit breaking
- **Lombok** - Boilerplate reduction
- **Virtual Threads** - Non-blocking operations

## 🏗️ Architecture

### Core Modules

- **Authentication** - JWT-based auth with V1 (internal) and V2 (external email service)
- **Users** - User management and profiles
- **Friends** - Friendship system with requests and blocking
- **Chat Rooms** - Group chat management
- **Messages** - Real-time messaging via WebSocket
- **WebSocket** - STOMP-based real-time communication

### Service Integration

- **Email Service** - External Quarkus service for email operations
- **Async Processing** - Virtual threads for non-blocking operations
- **Fault Tolerance** - Graceful handling of service failures

## 🚀 Quick Start

### Prerequisites

- Java 21
- Maven 3.9+
- PostgreSQL (or use Docker Compose)

### Local Development

1. **Set up environment variables**

   ```bash
   # Create .env file in api/ directory
   DATASOURCE_USERNAME=myuser
   DATASOURCE_PASSWORD=secret
   DATA_SOURCE_URL=jdbc:postgresql://localhost:5432/chatterbox_db
   FRONTEND_DOMAIN=http://localhost:3000
   SECRET_KEY=your-secret-key
   EMAIL_SERVICE_URL=http://localhost:8081
   ACCESS_TOKEN_HEADER=your-access-token
   ```

2. **Start PostgreSQL (optional)**

   ```bash
   docker-compose up postgres -d
   ```

3. **Run the application**

   ```bash
   # Using Maven wrapper
   ./mvnw spring-boot:run
   
   # Or using Python script
   python ../bin/run_server.py
   ```

### Docker Deployment

```bash
# Build image
docker build -t chatterbox-api .

# Run container
docker run -p 8080:8080 --env-file .env chatterbox-api
```

## 🔧 API Endpoints

### Authentication

- `POST /api/v1/auths/register` - User registration
- `POST /api/v1/auths/login` - User login
- `POST /api/v1/auths/verify` - Email verification
- `POST /api/v2/auths/register` - Registration with external email service

### Users

- `GET /api/v1/users/me` - Get current user
- `GET /api/v1/users/query` - Search users
- `GET /api/v1/users/{id}` - Get user profile

### Friends

- `POST /api/v1/friends/request/{friendId}` - Send friend request
- `PUT /api/v1/friends/accept/{friendshipId}` - Accept request
- `GET /api/v1/friends/requests` - Get pending requests

### Chat Rooms

- `POST /api/v1/chats` - Create chat room
- `GET /api/v1/chats/{id}` - Get chat details
- `PUT /api/v1/chats/{id}/members/{username}` - Add member

### WebSocket

- `/ws` - WebSocket endpoint
- `/app/chat.sendMessage` - Send message
- `/app/chat.editMessage` - Edit message
- `/app/chat.deleteMessage` - Delete message

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AuthControllerTest

# Run with coverage
./mvnw test jacoco:report
```

## 📊 Monitoring

- **Health Check**: `GET /actuator/health`
- **Metrics**: `GET /actuator/metrics`
- **Rate Limiting**: Configured on auth endpoints
- **Logging**: Comprehensive logging with SLF4J

## 🔒 Security

- JWT-based authentication with HTTP-only cookies
- Rate limiting on sensitive endpoints
- CORS configuration for frontend
- Input validation and sanitization
- Service-to-service authentication

## 📦 Build & Deploy

### Maven Commands

```bash
# Clean build
./mvnw clean package

# Run tests
./mvnw test

# Build Docker image
./mvnw spring-boot:build-image
```

### Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `DATASOURCE_USERNAME` | Database username | Yes |
| `DATASOURCE_PASSWORD` | Database password | Yes |
| `DATA_SOURCE_URL` | Database connection URL | Yes |
| `SECRET_KEY` | JWT signing key | Yes |
| `FRONTEND_DOMAIN` | Frontend URL for CORS | Yes |
| `EMAIL_SERVICE_URL` | Email service URL | Yes |
| `ACCESS_TOKEN_HEADER` | Email service auth token | Yes |

## 🤝 Contributing

1. Follow Spring Boot best practices
2. Add tests for new features
3. Update API documentation
4. Ensure proper error handling
5. Follow the existing code style

---
