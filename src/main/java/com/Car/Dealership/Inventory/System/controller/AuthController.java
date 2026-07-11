package com.Car.Dealership.Inventory.System.controller;

import com.Car.Dealership.Inventory.System.dto.LoginRequest;
import com.Car.Dealership.Inventory.System.entity.User;
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

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        User registeredUser = authService.register(user);
        System.out.println("Registered user: " + registeredUser);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
        String identifier = loginRequest.getEmail();
        if (identifier == null || identifier.trim().isEmpty()) {
            identifier = loginRequest.getUsername();
        }
        String token = authService.login(identifier, loginRequest.getPassword());
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }
}
