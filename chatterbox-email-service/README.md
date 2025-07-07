# Chatterbox Email Service

A dedicated Quarkus-based microservice responsible for handling email communications within the Chatterbox application ecosystem. This service operates independently from the main Spring Boot API and specializes in sending transactional emails such as verification codes and password reset notifications.

## Overview

The Chatterbox Email Service is designed as a lightweight, fast, and reliable email delivery system built on Quarkus. It provides a clean separation of concerns by handling all email-related functionality outside of the main application API.

## Features

- **Verification Code Emails**: Send email verification codes for user account activation
- **Password Reset Emails**: Handle forgot password email notifications
- **Reactive Programming**: Non-blocking email delivery using Mutiny for improved performance
- **Rate Limiting**: Built-in rate limiting
- **Authentication**: Access token-based security for API endpoints
- **Error Handling**: Comprehensive exception handling with proper HTTP status codes
- **HTML Email Templates**: Inline HTML templates for branded email communications
- **Health Checks**: Built-in health monitoring and status endpoints
- **Configuration Management**: Externalized configuration for different environments

## Technology Stack

- **Quarkus**: Java framework with reactive capabilities
- **Java 21+**: Programming language
- **Maven**: Build and dependency management
- **Quarkus Mailer**: Reactive email sending capabilities
- **SmallRye Mutiny**: Reactive programming library
- **SmallRye Fault Tolerance**: Rate limiting and resilience patterns
- **RESTEasy Reactive**: REST API implementation

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.8+
- SMTP server configuration (Gmail, SendGrid, etc.)
- Secret key for API authentication

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd chatterbox-email-service
```

2. Configure your email settings in `application.properties`:
```properties
# SMTP Configuration
quarkus.mailer.host=smtp.gmail.com
quarkus.mailer.port=587
quarkus.mailer.username=your-email@gmail.com
quarkus.mailer.password=your-app-password
quarkus.mailer.start-tls=REQUIRED
quarkus.mailer.from=noreply@chatterbox.com

# Application Configuration
frontend.domain=https://your-frontend-domain.com/
secret.key=your-secret-access-key
```

3. Build the application:
```bash
mvn clean package
```

4. Run in development mode:
```bash
mvn quarkus:dev
```

## API Endpoints

### Send Verification Email
```
POST /api/v1/emails/verify
Content-Type: application/json
Access-Token: your-secret-key

{
  "email": "user@example.com",
  "username": "JohnDoe",
  "code": "123456"
}
```

### Send Password Reset Email
```
POST /api/v1/emails/reset-password
Content-Type: application/json
Access-Token: your-secret-key

{
  "email": "user@example.com",
  "username": "JohnDoe",
  "code": "abc123def456"
}
```

### Health Check
```
GET /q/health
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `QUARKUS_MAILER_HOST` | SMTP server hostname | `localhost` |
| `QUARKUS_MAILER_PORT` | SMTP server port | `587` |
| `QUARKUS_MAILER_USERNAME` | SMTP authentication username | - |
| `QUARKUS_MAILER_PASSWORD` | SMTP authentication password | - |
| `QUARKUS_MAILER_FROM` | Default sender email address | - |
| `FRONTEND_DOMAIN` | Frontend domain for email links | - |
| `SECRET_KEY` | API access token for authentication | - |

### Email Templates

Email templates are embedded as HTML strings in the `EmailService` class:
- **Verification Email**: Contains a verification link and expires in 10 minutes
- **Password Reset Email**: Contains a reset link and expires in 10 minutes

Both templates include:
- Responsive HTML design
- ChatterBox branding
- Verification/reset links that redirect to the frontend domain
- Expiration notices and support contact information

## Development

### Running Tests
```bash
mvn test
```

### Building for Production
```bash
mvn package -Pnative
```

### Docker Support
```bash
# Build container
docker build -t chatterbox-email-service .

# Run container
docker run -p 8080:8080 chatterbox-email-service
```

## Integration with Main API

The main Spring Boot API will communicate with this service via HTTP REST calls. Ensure proper network configuration and service discovery if running in a containerized environment.

## Deployment

`chatterbox-email-service` will be deployed to Heroku once connection is implemented between the service and the main `API`.

## Security

The service implements several security measures:

- **Access Token Authentication**: All endpoints require a valid `Access-Token` header
- **Rate Limiting**: Each endpoint is limited to 5 requests per 10 minutes per client
- **Input Validation**: Request bodies are validated for required fields
- **Error Handling**: Comprehensive exception handling without exposing sensitive information

### Error Responses

The service returns structured error responses:

```json
{
  "errorMessage": "Invalid access token",
  "timestamp": "1704067200000"
}
```

**HTTP Status Codes:**
- `400 Bad Request`: Invalid request body
- `401 Unauthorized`: Invalid or missing access token
- `429 Too Many Requests`: Rate limit exceeded
- `500 Internal Server Error`: Server-side errors

## Troubleshooting

### Logs

Enable debug logging for email operations:
```properties
quarkus.log.category."io.quarkus.mailer".level=DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and questions, please contact the development team or create an issue in the project repository.
