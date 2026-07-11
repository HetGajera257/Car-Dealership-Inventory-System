package com.Car.Dealership.Inventory.System.repository;

import com.Car.Dealership.Inventory.System.entity.Category;
import com.Car.Dealership.Inventory.System.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @Query("SELECT v FROM Vehicle v WHERE " +
           "LOWER(v.make) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(v.model) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Vehicle> searchByKeyword(@Param("keyword") String keyword);

    List<Vehicle> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    @Query("SELECT v FROM Vehicle v WHERE " +
           "(:make IS NULL OR LOWER(v.make) LIKE LOWER(CONCAT('%', :make, '%'))) AND " +
           "(:model IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%'))) AND " +
           "(:category IS NULL OR v.category = :category) AND " +
           "(:minPrice IS NULL OR v.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR v.price <= :maxPrice)")
    List<Vehicle> searchVehicles(
            @Param("make") String make,
            @Param("model") String model,
            @Param("category") Category category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );
}
