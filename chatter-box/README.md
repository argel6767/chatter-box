# ChatterBox Frontend 🎨

The Next.js frontend application for ChatterBox, providing a modern, responsive chat interface with real-time messaging capabilities.

## 🛠️ Tech Stack

- **Next.js 15.3.3** (App Router) - React framework for production
- **React 19** - Latest React with concurrent features
- **TypeScript** - Type-safe development
- **Tailwind CSS 4** - Utility-first CSS framework
- **shadcn/ui** - Beautiful and accessible UI components
- **Radix UI** - Headless UI primitives
- **TanStack Query** - Server state management
- **Zustand** - Client state management
- **STOMP.js** - WebSocket client for real-time messaging
- **Axios** - HTTP client for API requests
- **Lucide React** - Icon library
- **Sonner** - Toast notifications

## ✨ Features

- **Real-time Chat** - WebSocket-based messaging with STOMP protocol
- **Authentication** - JWT-based auth with protected routes
- **User Management** - Profile management and user search
- **Friendship System** - Friend requests, blocking, and relationship management
- **Chat Rooms** - Create and manage group conversations
- **Responsive Design** - Mobile-first design with dark theme support
- **Modern UI** - Smooth animations and accessible components
- **Type Safety** - Full TypeScript coverage with type definitions

## 🚀 Quick Start

### Prerequisites

- Node.js 18+
- npm, yarn, pnpm, or bun

### Local Development

1. **Install dependencies**

   ```bash
   npm install
   ```

2. **Set up environment variables**

   ```bash
   # Create .env.local file
   NEXT_PUBLIC_API_URL=http://localhost:8080
   NEXT_PUBLIC_WS_URL=ws://localhost:8080
   ```

3. **Run development server**

   ```bash
   npm run dev
   ```

4. **Open your browser**
   Navigate to [http://localhost:3000](http://localhost:3000)

### Production Build

```bash
# Build for production
npm run build

# Start production server
npm start
```

## 🏗️ Project Structure

```
src/
├── app/                    # Next.js App Router
│   ├── auth/              # Authentication pages
│   ├── (protected)/       # Protected chat routes
│   └── layout.tsx         # Root layout
├── components/            # Reusable UI components
│   ├── ui/               # shadcn/ui components
│   │   └── chat/         # Chat-specific components
│   └── providers.tsx     # Context providers
├── hooks/                # Custom React hooks
├── lib/                  # Utilities and configurations
│   ├── models/           # TypeScript type definitions
│   └── utils.ts          # Utility functions
└── web_socket/           # WebSocket service
```

## 🔧 Key Components

### Authentication

- **Protected Routes** - Route protection with authentication checks
- **Login/Register** - User authentication forms
- **Email Verification** - Account verification flow

### Chat Interface

- **Chat Bubbles** - Message display with timestamps and user info
- **Chat Input** - Message composition with real-time typing
- **Message List** - Scrollable message history with auto-scroll
- **Expandable Chats** - Collapsible chat interface for mobile

### User Management

- **User Search** - Debounced user search functionality
- **Friend Requests** - Send, accept, and manage friend requests
- **User Profiles** - View and manage user information

## 🧪 Testing

```bash
# Run tests
npm test

# Run tests in watch mode
npm test -- --watch

# Run tests with coverage
npm test -- --coverage
```

## 📦 Build & Deploy

### Vercel Deployment

The app is configured for Vercel deployment with API proxy:

```json
{
  "rewrites": [
    {
      "source": "/api/:path*",
      "destination": "https://chatter-box-api-lyb64.kinsta.app/:path*"
    }
  ]
}
```

### Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `NEXT_PUBLIC_API_URL` | Backend API URL | Yes |
| `NEXT_PUBLIC_WS_URL` | WebSocket URL | Yes |

## 🎨 UI Components

### shadcn/ui Components

- **Button** - Variant-based button components
- **Dialog** - Modal dialogs and overlays
- **Dropdown Menu** - Context menus and navigation
- **Avatar** - User profile images
- **Card** - Content containers
- **Input** - Form inputs and text fields
- **Loading** - Loading states and skeletons
- **Toast** - Notification system with Sonner

### shadcn-chat Chat Components

- **ChatBubble** - Individual message display
- **ChatInput** - Message composition
- **ChatMessageList** - Message history container
- **ExpandableChat** - Responsive chat interface

## 🔄 State Management

### Server State (TanStack Query)

- API data fetching and caching
- Real-time data synchronization
- Optimistic updates
- Error handling and retries

### Client State (Zustand)

- User authentication state
- UI state management
- Chat room state
- WebSocket connection state

## 🌐 WebSocket Integration

### STOMP Protocol

- Real-time message delivery
- Connection state management
- Automatic reconnection
- Message acknowledgment

### Connection Management

- Secure WebSocket connections
- Authentication via JWT tokens
- Connection health monitoring
- Graceful error handling

## 🎯 Performance

- **Turbopack** - Fast development builds
- **Image Optimization** - Next.js automatic image optimization
- **Code Splitting** - Automatic route-based code splitting
- **Bundle Analysis** - Built-in bundle analyzer
- **TypeScript** - Compile-time error checking

## 🤝 Contributing

1. Follow Next.js and React best practices
2. Use TypeScript for all new code
3. Add tests for new components
4. Follow the existing component patterns
5. Ensure responsive design and accessibility

---

