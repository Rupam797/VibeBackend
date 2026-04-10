# MyVibe Backend - Real-Time Chat Engine 🚀

The robust, production-grade backend power for the MyVibe chat application. Built with Java and Spring Boot, it utilizes WebSockets for instantaneous messaging and MongoDB for persistent data storage.

## 🏗️ Project Structure

The project follows a standard Spring Boot architectural pattern:

```text
com.substring.chat
├── config         # Configuration for CORS and WebSockets
├── controllers    # REST Controllers and WebSocket Message Handlers
├── entities       # MongoDB Document Entities (Room, Message)
├── playload       # Data Transfer Objects (DTOs) for requests
└── repositories   # Spring Data MongoDB Repository Interfaces
```

## 🛠️ Tech Stack

- **Language**: Java 21+
- **Framework**: Spring Boot 3.x
- **Messaging**: Spring WebSocket with STOMP & SockJS
- **Database**: MongoDB
- **Security**: Environment-based configuration for Production/Local parity
- **Utilities**: Lombok (for boilerplate reduction)

## 📡 API Endpoints

### REST API (`/api/v1/rooms`)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/` | Creates a new chat room. |
| `GET` | `/{roomId}` | Joins/Retrieves an existing room. |
| `GET` | `/{roomId}/messages` | Retrieves paginated message history for a room. |

### WebSocket API (`/chat`)

| Action | Destination | Description |
| :--- | :--- | :--- |
| `SUBSCRIBE` | `/topic/room/{roomId}` | Listen for new messages & system broadcasts. |
| `SUBSCRIBE` | `/topic/room/{roomId}/users` | Listen for real-time online user count updates. |
| `SEND` | `/app/sendMessage/{roomId}` | Send a new message to the room. |
| `SEND` | `/app/joinRoom/{roomId}` | Initialize user presence in a room. |

## 🌟 Key Backend Logic

- **Thread-Safe Presence Tracking**: Uses `ConcurrentHashMap` to track active WebSockets and link them to users/rooms.
- **Auto-Cleanup**: Listens for `SessionDisconnectEvent` to automatically broadcast "User Left" notifications when a connection is lost (tab closed, etc.).
- **UTC Time Persistence**: Forced JVM-level UTC timezone to ensure messages are stored and retrieved consistently across global users.
- **CORS Management**: Dynamically handles origins for local development and production deployments (Vercel/Render).

## 🚀 Getting Started

1. **Prerequisites**: Ensure you have JDK 21 and MongoDB installed or a MongoDB Atlas URI ready.
2. **Environment Variables**: Set the following:
   - `MONGODB_URI`: Your MongoDB connection string.
   - `FRONTEND_URL`: Allowed origins for CORS.
3. **Build & Run**:
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

## 📄 License
This project is licensed under the MIT License.
