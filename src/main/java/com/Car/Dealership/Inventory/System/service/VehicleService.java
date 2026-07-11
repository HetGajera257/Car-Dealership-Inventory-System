package com.Car.Dealership.Inventory.System.service;

import com.Car.Dealership.Inventory.System.entity.Category;
import com.Car.Dealership.Inventory.System.entity.Vehicle;
import com.Car.Dealership.Inventory.System.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public Vehicle addVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public Optional<Vehicle> getVehicleById(Long id) {
        return vehicleRepository.findById(id);
    }

    public List<Vehicle> searchVehicles(String keyword) {
        return vehicleRepository.searchByKeyword(keyword);
    }

    public List<Vehicle> searchVehicles(String make, String model, Category category, BigDecimal minPrice, BigDecimal maxPrice) {
        return vehicleRepository.searchVehicles(make, model, category, minPrice, maxPrice);
    }

    public List<Vehicle> searchVehiclesByPrice(BigDecimal minPrice, BigDecimal maxPrice) {
        return vehicleRepository.findByPriceBetween(minPrice, maxPrice);
    }

    @Transactional
    public Vehicle updateVehicle(Long id, Vehicle vehicleDetails) {
        return vehicleRepository.findById(id).map(vehicle -> {
            vehicle.setMake(vehicleDetails.getMake());
            vehicle.setModel(vehicleDetails.getModel());
            vehicle.setCategory(vehicleDetails.getCategory());
            vehicle.setPrice(vehicleDetails.getPrice());
            vehicle.setQuantity(vehicleDetails.getQuantity());
            return vehicleRepository.save(vehicle);
        }).orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + id));
    }

    @Transactional
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new RuntimeException("Vehicle not found with id: " + id);
        }
        vehicleRepository.deleteById(id);
    }

    @Transactional
    public Vehicle purchaseVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + id));
        
        if (vehicle.getQuantity() <= 0) {
            throw new RuntimeException("Vehicle is out of stock");
        }
        
        vehicle.setQuantity(vehicle.getQuantity() - 1);
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public Vehicle restockVehicle(Long id, int quantityToAdd) {
        if (quantityToAdd <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive");
        }
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + id));
        
        vehicle.setQuantity(vehicle.getQuantity() + quantityToAdd);
        return vehicleRepository.save(vehicle);
    }
}
