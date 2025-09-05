# 🔧 EduLink Backend API - Spring Boot Application

> **Modern Educational Management System Backend with Spring Boot & MongoDB**

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.4-green?style=for-the-badge&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)](https://www.oracle.com/java/)
[![MongoDB](https://img.shields.io/badge/MongoDB-6.0-green?style=for-the-badge&logo=mongodb)](https://www.mongodb.com/)
[![Maven](https://img.shields.io/badge/Maven-3.8-red?style=for-the-badge&logo=apache-maven)](https://maven.apache.org/)

## 🚀 Quick Start

### Prerequisites
- **Java 17+** 
- **Maven 3.8+**
- **MongoDB** (Atlas or Local)

### Environment Setup
```bash
# 1. Clone the repository
git clone https://github.com/mehara-rothila/hacktivate-back.git
cd hacktivate-back

# 2. Copy environment template
cp .env.example .env

# 3. Configure environment variables
# Edit .env with your MongoDB URI and JWT secret
MONGODB_URI=mongodb://localhost:27017/edulink_dev
JWT_SECRET=your-super-secret-jwt-key-here

# 4. Run the application
./mvnw spring-boot:run
```

**API Server:** `http://localhost:8765`

---

## 🏗️ Architecture Overview

### 🎯 Core Technologies
- **Framework:** Spring Boot 3.5.4
- **Language:** Java 17
- **Database:** MongoDB with Spring Data
- **Security:** JWT Authentication + Spring Security
- **Build Tool:** Apache Maven
- **Real-time:** WebSocket Support

### 📁 Project Structure
```
src/main/java/com/edulink/backend/
├── 🏛️ config/           # Configuration classes
│   ├── SecurityConfig.java        # Security & JWT setup
│   ├── WebSocketConfig.java       # WebSocket configuration
│   └── AdminUserInitializer.java  # Default admin setup
├── 🎮 controller/        # REST API endpoints
│   ├── AuthController.java        # Authentication APIs
│   ├── UserController.java        # User management
│   ├── CourseController.java      # Course operations
│   ├── AppointmentController.java # Appointment booking
│   └── QueryController.java       # Q&A system
├── 📊 model/entity/      # MongoDB entities
│   ├── User.java                  # User profiles
│   ├── Course.java               # Course information
│   ├── Appointment.java          # Meeting bookings
│   └── Query.java                # Student queries
├── 🗄️ repository/       # Data access layer
├── ⚙️ service/          # Business logic
└── 🛡️ security/         # JWT & authentication
```

---

## 🌐 API Endpoints

### 🔐 Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/login` | User login |
| `POST` | `/api/auth/register` | User registration |
| `POST` | `/api/auth/refresh` | Token refresh |
| `POST` | `/api/auth/logout` | User logout |

### 👥 User Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/users/profile` | Get user profile |
| `PUT` | `/api/users/profile` | Update profile |
| `GET` | `/api/users/lecturers` | List all lecturers |

### 📚 Course System
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/courses` | List all courses |
| `POST` | `/api/courses` | Create course |
| `GET` | `/api/courses/{id}` | Course details |
| `PUT` | `/api/courses/{id}` | Update course |

### 📅 Appointments
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/appointments` | User appointments |
| `POST` | `/api/appointments` | Book appointment |
| `PUT` | `/api/appointments/{id}` | Update appointment |
| `DELETE` | `/api/appointments/{id}` | Cancel appointment |

### ❓ Query System
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/queries` | List queries |
| `POST` | `/api/queries` | Submit query |
| `GET` | `/api/queries/{id}` | Query details |
| `POST` | `/api/queries/{id}/reply` | Reply to query |

### 📢 Announcements
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/announcements` | List announcements |
| `POST` | `/api/announcements` | Create announcement |
| `PUT` | `/api/announcements/{id}` | Update announcement |

### 💬 Real-time Features
| Endpoint | Type | Description |
|----------|------|-------------|
| `/ws/chat` | WebSocket | Real-time messaging |
| `/ws/notifications` | WebSocket | Live notifications |

---

## ⚙️ Configuration

### 🔧 Environment Variables
```bash
# Required
MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/edulink
JWT_SECRET=your-super-secret-key-minimum-256-bits
JWT_EXPIRATION=3600
JWT_REFRESH_EXPIRATION=36000

# Optional
PORT=8765
CORS_ORIGINS=http://localhost:3000,http://localhost:3001
LOG_LEVEL=INFO
MAIL_ENABLED=false
```

### 🗂️ Profiles
- **Development:** `SPRING_PROFILES_ACTIVE=dev`
- **Production:** `SPRING_PROFILES_ACTIVE=prod`

---

## 🚦 Development Commands

```bash
# Development
./mvnw spring-boot:run                 # Start development server
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Building
./mvnw clean compile                   # Compile source
./mvnw clean package                   # Create JAR
./mvnw clean install                   # Install dependencies

# Testing
./mvnw test                           # Run tests
./mvnw test -Dspring.profiles.active=test

# Production
java -jar target/edulink-backend.jar  # Run production JAR
```

---

## 🛡️ Security Features

### 🔐 Authentication
- **JWT Token-based** authentication
- **Role-based access** control (Student/Lecturer/Admin)
- **Password encryption** using BCrypt
- **Token refresh** mechanism

### 🛡️ API Security
- **CORS configuration** for frontend integration
- **Request validation** and sanitization
- **Rate limiting** on sensitive endpoints
- **Error handling** without information leakage

---

## 📊 Database Schema

### 👤 User Entity
```java
{
  "id": "ObjectId",
  "username": "String",
  "email": "String",
  "password": "String (encrypted)",
  "role": "STUDENT | LECTURER | ADMIN",
  "profile": {
    "firstName": "String",
    "lastName": "String",
    "phone": "String"
  },
  "createdAt": "DateTime",
  "updatedAt": "DateTime"
}
```

### 📚 Course Entity
```java
{
  "id": "ObjectId",
  "name": "String",
  "description": "String",
  "lecturer": "ObjectId (User)",
  "students": ["ObjectId (User)"],
  "schedule": "String",
  "isActive": "Boolean",
  "createdAt": "DateTime"
}
```

---

## 🔧 Advanced Features

### 📅 Appointment System
- **Smart scheduling** with availability checking
- **Recurring appointments** support
- **Email notifications** (configurable)
- **Conflict detection** and resolution

### 💬 Real-time Communication
- **WebSocket integration** for live chat
- **Announcement broadcasting** to all users
- **Real-time notifications** system

### 📈 Analytics & Monitoring
- **Spring Boot Actuator** for health checks
- **Performance metrics** tracking
- **Custom logging** with configurable levels

---

## 🚀 Deployment

### 📦 Production Build
```bash
# Create production JAR
./mvnw clean package -Dmaven.test.skip=true

# Run with production profile
java -Dspring.profiles.active=prod -jar target/edulink-backend.jar
```

### 🐳 Docker Deployment
```bash
# Build Docker image
docker build -t edulink-backend .

# Run container
docker run -p 8765:8765 \
  -e MONGODB_URI="your-mongo-uri" \
  -e JWT_SECRET="your-jwt-secret" \
  edulink-backend
```

### ☁️ Cloud Deployment
- **MongoDB Atlas** for database
- **Heroku/AWS/Railway** for application hosting
- **Environment variables** for configuration

---

## 🧪 Testing

### 🔍 API Testing
Use the provided `test-endpoints.http` file with your HTTP client:
```bash
# Using VS Code REST Client extension
# Open test-endpoints.http and click "Send Request"
```

### 🧪 Unit Testing
```bash
./mvnw test                           # All tests
./mvnw test -Dtest=UserServiceTest    # Specific test class
```

---

## 🐛 Troubleshooting

### Common Issues
1. **Database Connection**
   - Verify MongoDB URI in environment variables
   - Check network connectivity to MongoDB Atlas

2. **JWT Issues**
   - Ensure JWT_SECRET is at least 256 bits
   - Check token expiration settings

3. **CORS Errors**
   - Update CORS_ORIGINS with your frontend URL
   - Verify preflight request handling

### 📝 Logs
```bash
# Enable debug logging
LOG_LEVEL=DEBUG ./mvnw spring-boot:run

# Check application logs
tail -f logs/application.log
```

---

## 🤝 API Integration

### Frontend Integration
```javascript
// Example API call
const response = await fetch('http://localhost:8765/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    username: 'student@example.com',
    password: 'password123'
  })
});

const data = await response.json();
localStorage.setItem('token', data.token);
```

### WebSocket Connection
```javascript
const socket = new WebSocket('ws://localhost:8765/ws/chat');
socket.onmessage = (event) => {
  const message = JSON.parse(event.data);
  // Handle real-time message
};
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

---

## 📞 Support & Documentation

- **API Documentation:** Available at `/swagger-ui.html` when running
- **Health Check:** `GET /actuator/health`
- **Configuration Guide:** [README-CONFIGURATION.md](README-CONFIGURATION.md)

---

<div align="center">

**🔧 EduLink Backend API - Powering Modern Education**

[🌐 Frontend Repository](https://github.com/mehara-rothila/hackvite_front) • [📚 API Docs](#) • [🚀 Deploy Guide](#)

</div>