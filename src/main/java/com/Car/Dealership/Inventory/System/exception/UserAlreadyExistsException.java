package com.Car.Dealership.Inventory.System.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String field, String value) {
        super(field + " already exists: " + value);
    }

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
