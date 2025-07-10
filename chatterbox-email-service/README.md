# ChatterBox Email Service 📧

A dedicated Quarkus microservice for handling email communications in the ChatterBox ecosystem. Provides transactional emails for user verification and password reset functionality.

## 🛠️ Tech Stack

- **Quarkus 3.24.1** - Supersonic Subatomic Java framework
- **Java 21** - Programming language
- **Quarkus Mailer** - Reactive email sending
- **Quarkus Qute** - Template engine for email content
- **SmallRye Fault Tolerance** - Rate limiting and resilience
- **SmallRye JWT** - JWT-based service authentication (SOON)
- **RESTEasy Reactive** - REST API implementation

## ✨ Features

- **Email Verification** - Send verification codes for account activation
- **Password Reset** - Handle forgot password email notifications
- **Rate Limiting** - 20 requests per 5 minutes per endpoint
- **Service Authentication** - JWT-based access token security
- **Reactive Processing** - Non-blocking email delivery with Mutiny
- **HTML Templates** - Branded email templates with responsive design
- **Health Monitoring** - Built-in health checks and status endpoints

## 🚀 Quick Start

### Prerequisites

- Java 21
- Maven 3.8+
- SMTP server configuration (Gmail, SendGrid, etc.)
- Secret key for API authentication

### Local Development

1. **Set up environment variables**

   ```bash
   # Create .env file or set environment variables
   FRONTEND_DOMAIN=http://localhost:3000
   EMAIL_USERNAME=your-email@gmail.com
   EMAIL_APP_PASSWORD=your-app-password
   SECRET_KEY=your-secret-access-key
   IS_MOCKING=false
   PORT=8081
   ```

2. **Run in development mode**

   ```bash
   ./mvnw quarkus:dev
   ```

3. **Build and run**

   ```bash
   ./mvnw clean package
   java -jar target/quarkus-app/quarkus-run.jar
   ```

### Docker Deployment

```bash
# Build native image
./mvnw package -Pnative

# Build Docker image
docker build -t chatterbox-email-service .

# Run container
docker run -p 8081:8080 --env-file .env chatterbox-email-service
```

## 🔧 API Endpoints

### Email Operations

- `POST /api/v1/emails/verify` - Send verification email
- `POST /api/v1/emails/reset-password` - Send password reset email

### Health & Monitoring

- `GET /q/health` - Health check endpoint

## 🔒 Security

- **Access Token Authentication** - All endpoints require valid `Access-Token` header
- **Rate Limiting** - 20 requests per 5 minutes per endpoint
- **Input Validation** - Request body validation for required fields
- **Error Handling** - Structured error responses without sensitive data exposure

### Error Responses

```json
{
  "errorMessage": "Invalid access token",
  "timestamp": "1704067200000"
}
```

**HTTP Status Codes:**

- `400 Bad Request` - Invalid request body
- `401 Unauthorized` - Invalid or missing access token
- `429 Too Many Requests` - Rate limit exceeded
- `500 Internal Server Error` - Server-side errors

## 📧 Email Templates

### Verification Email

- HTML template with ChatterBox branding
- Verification link with 10-minute expiration
- Responsive design for mobile and desktop

### Password Reset Email

- HTML template with ChatterBox branding
- Reset link with 10-minute expiration
- Support contact information

## 🔧 Configuration

### Environment Variables

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `FRONTEND_DOMAIN` | Frontend URL for email links | Yes | - |
| `EMAIL_USERNAME` | SMTP username (Gmail) | Yes | - |
| `EMAIL_APP_PASSWORD` | SMTP app password | Yes | - |
| `SECRET_KEY` | API access token | Yes | - |
| `IS_MOCKING` | Mock email sending | No | false |
| `PORT` | Service port | No | 8080 |

### SMTP Configuration

```properties
quarkus.mailer.host=smtp.gmail.com
quarkus.mailer.port=587
quarkus.mailer.start-tls=REQUIRED
quarkus.mailer.username=${EMAIL_USERNAME}
quarkus.mailer.password=${EMAIL_APP_PASSWORD}
quarkus.mailer.from=${EMAIL_USERNAME}
```

## 🧪 Testing

```bash
# Run tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report

# Integration tests
./mvnw verify
```

## 📊 Monitoring

- **Health Check**: `GET /q/health`
- **Metrics**: `GET /q/metrics`
- **Logging**: Quarkus logging with configurable levels
- **Rate Limiting**: Built-in monitoring and alerts

## 🔄 Integration

### Main API Communication
The Spring Boot main API communicates with this service via HTTP REST calls:

```java
// Example from main API
restClient.post()
    .uri("/api/v1/emails/verify")
    .header("Access-Token", accessToken)
    .body(verifyUserDto)
    .retrieve();
```

### Service Discovery

- **Port**: 8081 (configurable)
- **Health Endpoint**: `/q/health`
- **Base URL**: Configurable via `EMAIL_SERVICE_URL` in main API

## 🚀 Deployment

### Native Image Build

```bash
# Build native executable
./mvnw package -Pnative

# Run native executable
./target/chatterbox-email-service-1.0.0-SNAPSHOT-runner
```

### Docker Compose

```yaml
email-service:
  image: chatterbox-email-service
  ports:
    - "8081:8080"
  environment:
    - FRONTEND_DOMAIN=http://localhost:3000
    - EMAIL_USERNAME=${EMAIL_USERNAME}
    - EMAIL_APP_PASSWORD=${EMAIL_APP_PASSWORD}
    - SECRET_KEY=${SECRET_KEY}
```

## 🤝 Contributing

1. Follow Quarkus best practices
2. Add tests for new features
3. Update email templates as needed
4. Ensure proper error handling
5. Follow existing code style

---
