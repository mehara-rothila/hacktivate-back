# Configuration Setup

## Environment Setup

1. **Copy the environment template:**
   ```bash
   cp .env.example .env
   ```

2. **Fill in required environment variables in .env:**
   - `MONGODB_URI`: Your MongoDB connection string
   - `JWT_SECRET`: Strong secret key for JWT tokens

## Profiles

- **Development:** `SPRING_PROFILES_ACTIVE=dev` (default)
- **Production:** `SPRING_PROFILES_ACTIVE=prod`

## Running the Application

### Development
```bash
# Set environment variables
export MONGODB_URI="mongodb://localhost:27017/edulink_dev"
export JWT_SECRET="your-development-secret"

# Run
./mvnw spring-boot:run
```

### Production
```bash
# Set required environment variables
export MONGODB_URI="your-production-mongodb-uri"
export JWT_SECRET="your-strong-production-secret"
export SPRING_PROFILES_ACTIVE=prod

# Run
java -jar target/edulink-backend.jar
```

## Security Notes

- Never commit `.env` files
- Use strong secrets in production
- Database credentials should be environment-specific
- All sensitive configuration is now externalized