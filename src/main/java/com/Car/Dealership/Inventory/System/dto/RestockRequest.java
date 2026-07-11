package com.Car.Dealership.Inventory.System.dto;

public class RestockRequest {
    private int quantity;

    public RestockRequest() {}

    public RestockRequest(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
