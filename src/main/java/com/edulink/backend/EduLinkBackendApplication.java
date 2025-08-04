package com.edulink.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
public class EduLinkBackendApplication {

    public static void main(String[] args) {
        System.out.println("🚀 Starting EduLink Pro Backend...");
        SpringApplication.run(EduLinkBackendApplication.class, args);
        System.out.println("✅ EduLink Pro Backend started successfully!");
        System.out.println("📡 API Base URL: http://localhost:8765/api");
        System.out.println("🔧 Test Endpoints:");
        System.out.println("   - GET  http://localhost:8765/api/test/hello");
        System.out.println("   - GET  http://localhost:8765/api/test/db-status");
        System.out.println("   - GET  http://localhost:8765/api/test/users");
        System.out.println("   - POST http://localhost:8765/api/test/create-user");
    }
}