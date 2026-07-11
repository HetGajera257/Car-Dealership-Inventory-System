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
        if (userRepository.findByEmail("admin@dealership.com").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@dealership.com");
            admin.setPassword(passwordEncoder.encode("adminpassword"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Admin user seeded successfully with email: admin@dealership.com / password: adminpassword");
        } else {
            System.out.println("Admin user already exists. Seeding skipped.");
        }
    }
}
