# ChatterBox 💬

A modern real-time chat application built with Next.js (App Router) and Spring Boot, featuring secure authentication and seamless messaging experience.

## ✨ Features

- **Real-time chat room messaging** with WebSocket connections
- **Secure authentication** using JWT HTTP-only cookies
- **Email verification** for account security
- **Modern UI** with smooth animations and responsive design
- **Type-safe** development with robust backend API

## 🛠️ Tech Stack

### Frontend (chatter-box)
- **Next.js** (App Router) - React framework for production
- **Tailwind CSS** - Utility-first CSS framework
- **shadcn/ui** - Beautiful and accessible UI components
- **shadcn-chat** - Chat UI components built on shadcn/ui
- **Tailwind Motion** - Smooth animations and transitions

### Backend (api)
- **Spring Boot** (Java 21) - Java-based backend framework
- **WebSockets** - Real-time bidirectional communication
- **JWT** - Secure token-based authentication
- **JavaMailSender** - Email verification system

## 📁 Project Structure

```
chatterbox/
├── chatter-box/     # Next.js application (App Router)
│   ├── app/         # Application routes and UI
│   ├── components/  # Reusable UI components
│   └── hooks/       # Custom Hooks
|   └── lib/   # Utilities and configurations
├── api/             # Spring Boot application
│   ├── src/main/java/ # Java source code
│   └── src/main/resources/ # Configuration files
└── README.md
└── bin/            # Build scripts

```

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
- [Tailwind CSS](https://tailwindcss.com/) for styling
- [Spring Boot](https://spring.io/projects/spring-boot) for robust backend

---

Built with ❤️ using Next.js and Spring Boot
