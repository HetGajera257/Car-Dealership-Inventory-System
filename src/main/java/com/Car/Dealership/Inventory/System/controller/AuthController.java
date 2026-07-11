package com.Car.Dealership.Inventory.System.controller;

import com.Car.Dealership.Inventory.System.dto.LoginRequest;
import com.Car.Dealership.Inventory.System.entity.User;
import com.Car.Dealership.Inventory.System.repository.UserRepository;
import com.Car.Dealership.Inventory.System.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        User registeredUser = authService.register(user);
        System.out.println("Registered user: " + registeredUser);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        final String finalIdentifier = (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty())
                ? loginRequest.getUsername() : loginRequest.getEmail();
        String token = authService.login(finalIdentifier, loginRequest.getPassword());
        
        User user = userRepository.findByEmail(finalIdentifier)
                .or(() -> userRepository.findByUsername(finalIdentifier))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("role", user.getRole().name());
        return ResponseEntity.ok(response);
    }
}
