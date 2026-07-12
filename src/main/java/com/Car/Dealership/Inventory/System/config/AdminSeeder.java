package com.Car.Dealership.Inventory.System.config;

import com.Car.Dealership.Inventory.System.entity.Role;
import com.Car.Dealership.Inventory.System.entity.User;
import com.Car.Dealership.Inventory.System.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Remove old default admin if it still exists
        userRepository.findByEmail("admin@dealership.com")
                .ifPresent(userRepository::delete);

        // Seed the new admin if not already present
        if (userRepository.findByEmail("het@dealership.com").isEmpty()) {
            User admin = new User();
            admin.setUsername("het");
            admin.setEmail("het@dealership.com");
            admin.setPassword(passwordEncoder.encode("2572006@Het"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Admin user seeded: username=het | email=het@dealership.com");
        } else {
            System.out.println("Admin user already exists. Seeding skipped.");
        }
    }
}
