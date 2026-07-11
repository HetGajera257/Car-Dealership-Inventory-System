package com.Car.Dealership.Inventory.System.service;

import com.Car.Dealership.Inventory.System.entity.User;

public interface AuthService {
    User register(User user);
    String login(String email, String password);
}
