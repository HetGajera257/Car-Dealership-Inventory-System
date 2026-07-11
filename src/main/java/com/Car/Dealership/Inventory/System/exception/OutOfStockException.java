package com.Car.Dealership.Inventory.System.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class OutOfStockException extends RuntimeException {

    public OutOfStockException(Long vehicleId) {
        super("Vehicle with id " + vehicleId + " is out of stock");
    }

    public OutOfStockException(String message) {
        super(message);
    }
}
