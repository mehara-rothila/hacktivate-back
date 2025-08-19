package com.edulink.backend.config;

import com.edulink.backend.model.entity.User;
import com.edulink.backend.repository.UserRepository;
import com.edulink.backend.service.PasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    @Override
    public void run(String... args) throws Exception {
        createDefaultAdminUser();
    }

    private void createDefaultAdminUser() {
        String adminEmail = "admin@gmail.com";
        String adminPassword = "admin123";

        try {
            // Check if admin user already exists
            if (userRepository.findByEmail(adminEmail).isPresent()) {
                log.info("Default admin user already exists with email: {}", adminEmail);
                return;
            }

            // Create admin user profile
            User.UserProfile adminProfile = User.UserProfile.builder()
                    .firstName("Admin")
                    .lastName("User")
                    .department("Administration")
                    .title("Mr.")
                    .position("System Administrator")
                    .employmentType("full-time")
                    .status("active")
                    .updatedAt(LocalDateTime.now())
                    .build();

            // Create admin user
            User adminUser = User.builder()
                    .email(adminEmail)
                    .password(passwordService.hashPassword(adminPassword))
                    .role(User.UserRole.ADMIN)
                    .profile(adminProfile)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // Save admin user
            User savedAdmin = userRepository.save(adminUser);

            log.info("✅ Default admin user created successfully!");
            log.info("📧 Email: {}", adminEmail);
            log.info("🔑 Password: {}", adminPassword);
            log.info("🆔 User ID: {}", savedAdmin.getId());
            log.info("⚠️  Please change the default password after first login!");

        } catch (Exception e) {
            log.error("❌ Failed to create default admin user: {}", e.getMessage(), e);
        }
    }
}
