# ChatterBox 💬

A modern real-time chat application built with Next.js (App Router) and Spring Boot, featuring secure authentication, friendship management, and seamless messaging experience.

## ✨ Features

- **Real-time chat messaging** with WebSocket connections using STOMP protocol
- **Secure authentication** using JWT HTTP-only cookies with rate limiting
- **Email verification** for account security with dedicated email service
- **Friendship system** with friend requests, blocking, and user search
- **Chat room management** - create, join, leave, and manage group chats
- **Message operations** - send, edit, and delete messages in real-time
- **User search** with debounced queries and user profiles
- **Modern UI** with smooth animations, responsive design, and dark theme
- **Type-safe** development with robust backend API and comprehensive error handling

## 🛠️ Tech Stack

### Frontend (chatter-box)

- **Next.js 15** (App Router) - React framework for production
- **React 19** - Latest React with concurrent features
- **TypeScript** - Type-safe development
- **Tailwind CSS 4** - Utility-first CSS framework
- **shadcn/ui** - Beautiful and accessible UI components
- - **shadcn-chat** - Chat UI components built on shadcn/ui
- **Tailwind Motion** - Smooth animations and transitions
- **Radix UI** - Headless UI primitives
- **TanStack Query** - Server state management
- **Zustand** - Client state management
- **STOMP.js** - WebSocket client for real-time messaging
- **Axios** - HTTP client for API requests
- **Lucide React** - Icon library
- **Sonner** - Toast notifications

### Backend (api)

- **Spring Boot 3.3.0** (Java 21) - Java-based backend framework
- **Spring Security** - Authentication and authorization
- **Spring WebSocket** - Real-time bidirectional communication with STOMP
- **Spring Data JPA** - Database operations
- **PostgreSQL** - Primary database
- **H2 Database** - In-memory database for testing
- **JWT** - Secure token-based authentication
- **JavaMailSender** - Email verification system
- **Resilience4j** - Rate limiting and circuit breaking
- **Lombok** - Boilerplate code reduction

### Email Service (chatterbox-email-service)

- **Quarkus 3.24.1** - Supersonic Subatomic Java framework
- **Quarkus Mailer** - Email sending capabilities
- **Quarkus Qute** - Template engine for email content
- **JWT** - Secure service-to-service communication

## 🏗️ Architecture

### Microservices Architecture

```
ChatterBox/
├── chatter-box/           # Next.js frontend application
├── api/                   # Spring Boot main API service
├── chatterbox-email-service/  # Quarkus email microservice
└── bin/                   # Build and deployment scripts
```

### Authentication Architecture

#### V1 Auth (Internal Email Processing)

- Uses Spring Boot's JavaMailSender for direct email sending
- Synchronous email processing within the main API
- Suitable for simpler email requirements

#### V2 Auth (External Email Service)

- Delegates email operations to dedicated Quarkus email service
- Asynchronous processing using virtual threads
- Enhanced fault tolerance and scalability
- JWT-based service authentication
- Rate limiting and monitoring capabilities

### Key Components

#### Authentication & Security

- JWT-based authentication with HTTP-only cookies
- Rate limiting on authentication endpoints
- Email verification for account security
- Password reset functionality
- Protected routes with role-based access

#### Service-to-Service Communication

- **Main API → Email Service**: RESTful communication using Spring RestClient
- **Authentication**: JWT-based service authentication with access tokens
- **Async Processing**: Virtual threads for non-blocking email operations
- **Fault Tolerance**: Graceful handling of email service failures
- **Rate Limiting**: Email service implements rate limiting (20 requests/5 minutes)

#### Real-time Communication

- WebSocket connections using STOMP protocol
- Real-time message broadcasting
- Live chat room updates
- Connection state management

#### Social Features

- Friend request system (send, accept, reject)
- User blocking functionality
- User search with debounced queries
- User profiles and relationship status

#### Chat Management

- Create and manage chat rooms
- Add/remove members from group chats
- Leave chat rooms
- Message history and persistence

## 📁 Project Structure

```
ChatterBox/
├── chatter-box/           # Next.js frontend (App Router)
│   ├── src/
│   │   ├── app/           # Application routes
│   │   │   ├── auth/      # Authentication pages
│   │   │   └── (protected)/ # Protected chat routes
│   │   ├── components/    # Reusable UI components
│   │   ├── hooks/         # Custom React hooks
│   │   ├── lib/           # Utilities and type definitions
│   │   └── web_socket/    # WebSocket service
│   └── public/            # Static assets
├── api/                   # Spring Boot main API
│   ├── src/main/java/com/chat_room_app/
│   │   ├── auth/          # Authentication & authorization
│   │   ├── chatroom/      # Chat room management
│   │   ├── friends/       # Friendship system
│   │   ├── message/       # Message handling
│   │   ├── users/         # User management
│   │   ├── web_socket/    # WebSocket configuration
│   │   ├── email/         # Email service integration
│   │   ├── jwt/           # JWT utilities
│   │   └── configs/       # Application configuration
│   └── src/main/resources/ # Configuration files
├── chatterbox-email-service/  # Quarkus email microservice
│   └── src/main/java/com/chatter_box/email_service/
│       ├── email/         # Email sending logic
│       └── auth/          # Service authentication
└── bin/                   # Build and deployment scripts
```

## 🚀 Getting Started

### Prerequisites

- Java 21
- Node.js 18+
- Maven
- PostgreSQL (for production)
- Email service credentials

### Development Setup

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd ChatterBox
   ```

2. **Backend Setup**

   ```bash
   cd api
   # Set up environment variables in .env file
   python ../bin/run_server.py
   ```

3. **Email Service Setup**

   ```bash
   cd chatterbox-email-service
   # Configure email service properties
   mvn quarkus:dev
   ```

4. **Frontend Setup**

   ```bash
   cd chatter-box
   npm install
   npm run dev
   ```

### Environment Configuration

Create `.env` files in the respective directories with:

- Database connection strings
- JWT secret keys
- Email service credentials
- Frontend domain for CORS

## 🔧 API Endpoints

### Authentication (`/api/v1/auths`)

- `POST /register` - User registration
- `POST /login` - User login
- `POST /verify` - Email verification
- `POST /logout` - User logout
- `PUT /password` - Change password
- `POST /forgot/{username}` - Forgot password
- `PUT /reset` - Reset password

### Authentication V2 (`/api/v2/auths`) - Email Service Integration

- `POST /register` - User registration with async email service integration
- `POST /resend-verification/{username}` - Resend verification email via email service
- `POST /forgot/{username}` - Send forgot password email via email service

### Users (`/api/v1/users`)

- `GET /me` - Get current user
- `DELETE /me` - Delete account
- `GET /{searchUserId}` - Get user profile
- `GET /query` - Search users

### Friends (`/api/v1/friends`)

- `POST /request/{friendId}` - Send friend request
- `PUT /accept/{friendshipId}` - Accept friend request
- `DELETE /remove/{friendshipId}` - Remove friendship
- `POST /block/{friendId}` - Block user
- `GET /requests` - Get friend requests
- `GET /blocked` - Get blocked users

### Chat Rooms (`/api/v1/chats`)

- `POST /` - Create chat room
- `GET /{id}` - Get chat room details
- `DELETE /{id}` - Delete chat room
- `PUT /{chatRoomId}/members/{username}` - Add member
- `DELETE /{chatRoomId}/members/{username}` - Remove member
- `DELETE /{chatRoomId}/members/me` - Leave chat room

### WebSocket Messages

- `/app/chat.sendMessage` - Send message
- `/app/chat.deleteMessage` - Delete message
- `/app/chat.editMessage` - Edit message

### ChatterBox Email Service Integration (`/api/v1/emails`)

- `POST /verify` - Send verification email (called by main API)
- `POST /reset-password` - Send password reset email (called by main API)

## 🧪 Testing

The project includes comprehensive test suites:

- Unit tests for all controllers
- Integration tests for API endpoints
- Frontend component testing with Jest and Testing Library

## 🚀 Deployment

### Docker Support

- Docker Compose configuration for local development
- Dockerfile for production deployment
- Containerized email service

### Build Scripts

- `bin/build_api_image.py` - Build API Docker image
- `bin/push_api_image.py` - Push to container registry

### Dev Scripts

- `bin/run_server.py` - Run main API
- `bin/run_email_service.py` - Run email service

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [shadcn/ui](https://ui.shadcn.com/) for beautiful components
- [shadcn-chat](https://github.com/jakobhoeg/shadcn-chat) for chat UI components
- [Radix UI](https://www.radix-ui.com/) for accessible primitives
- [Tailwind CSS](https://tailwindcss.com/) for styling
- [Spring Boot](https://spring.io/projects/spring-boot) for robust backend
- [Quarkus](https://quarkus.io/) for fast email service

---

Built with ❤️ using Next.js, Spring Boot, and Quarkus
