package com.edulink.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
public class EduLinkBackendApplication {

    public static void main(String[] args) {
        System.out.println("🚀 Starting EduLink Pro Backend...");
        SpringApplication.run(EduLinkBackendApplication.class, args);
        
        // Get port from environment or default
        String port = System.getenv("PORT");
        if (port == null) {
            port = System.getProperty("server.port", "8765");
        }
        
        // Get base URL from environment or construct default
        String baseUrl = System.getenv("BASE_URL");
        if (baseUrl == null) {
            baseUrl = "http://localhost:" + port;
        }
        
        System.out.println("✅ EduLink Pro Backend started successfully!");
        System.out.println("📡 API Base URL: " + baseUrl + "/api");
        System.out.println("🔧 Test Endpoints:");
        System.out.println("   - GET  " + baseUrl + "/api/test/hello");
        System.out.println("   - GET  " + baseUrl + "/api/test/db-status");
        System.out.println("   - GET  " + baseUrl + "/api/test/users");
        System.out.println("   - POST " + baseUrl + "/api/test/create-user");
    }
}