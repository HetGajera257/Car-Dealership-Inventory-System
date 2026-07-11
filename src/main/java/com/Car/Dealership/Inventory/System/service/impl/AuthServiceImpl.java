package com.Car.Dealership.Inventory.System.service.impl;

import com.Car.Dealership.Inventory.System.entity.User;
import com.Car.Dealership.Inventory.System.repository.UserRepository;
import com.Car.Dealership.Inventory.System.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Override
    public User register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        // Note: Password hashing should be integrated here (e.g. BCryptPasswordEncoder)
        return userRepository.save(user);
    }

    @Override
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Note: Password matching should be done using a password encoder here
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid email or password");
        }

        // Note: Generate and return a real JWT token here
        return "dummy-jwt-token-for-" + user.getEmail();
    }
}
