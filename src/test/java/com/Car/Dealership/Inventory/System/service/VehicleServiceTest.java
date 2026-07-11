package com.Car.Dealership.Inventory.System.service;

import com.Car.Dealership.Inventory.System.entity.Category;
import com.Car.Dealership.Inventory.System.entity.Vehicle;
import com.Car.Dealership.Inventory.System.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        testVehicle = new Vehicle(1L, "Toyota", "Camry", Category.SEDAN, new BigDecimal("25000.00"), 5);
    }

    // ─── Add Vehicle ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("addVehicle: saves and returns the vehicle")
    void addVehicle_SavesAndReturnsVehicle() {
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);

        Vehicle result = vehicleService.addVehicle(testVehicle);

        assertThat(result).isNotNull();
        assertThat(result.getMake()).isEqualTo("Toyota");
        assertThat(result.getCategory()).isEqualTo(Category.SEDAN);
        verify(vehicleRepository, times(1)).save(testVehicle);
    }

    // ─── Get All Vehicles ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllVehicles: returns all vehicles from repository")
    void getAllVehicles_ReturnsAllVehicles() {
        Vehicle v2 = new Vehicle(2L, "Honda", "Civic", Category.HATCHBACK, new BigDecimal("18000.00"), 3);
        when(vehicleRepository.findAll()).thenReturn(List.of(testVehicle, v2));

        List<Vehicle> result = vehicleService.getAllVehicles();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMake()).isEqualTo("Toyota");
        assertThat(result.get(1).getCategory()).isEqualTo(Category.HATCHBACK);
    }

    @Test
    @DisplayName("getAllVehicles: returns empty list when no vehicles exist")
    void getAllVehicles_EmptyRepository_ReturnsEmptyList() {
        when(vehicleRepository.findAll()).thenReturn(List.of());

        List<Vehicle> result = vehicleService.getAllVehicles();

        assertThat(result).isEmpty();
    }

    // ─── Get Vehicle By Id ────────────────────────────────────────────────────

    @Test
    @DisplayName("getVehicleById: returns vehicle when ID exists")
    void getVehicleById_Exists_ReturnsVehicle() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        Optional<Vehicle> result = vehicleService.getVehicleById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getModel()).isEqualTo("Camry");
    }

    @Test
    @DisplayName("getVehicleById: returns empty Optional when ID not found")
    void getVehicleById_NotExists_ReturnsEmpty() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Vehicle> result = vehicleService.getVehicleById(99L);

        assertThat(result).isEmpty();
    }

    // ─── Search Vehicles ──────────────────────────────────────────────────────

    @Test
    @DisplayName("searchVehicles (keyword): returns matching vehicles")
    void searchVehicles_ByKeyword_ReturnsMatches() {
        when(vehicleRepository.searchByKeyword("toyota")).thenReturn(List.of(testVehicle));

        List<Vehicle> result = vehicleService.searchVehicles("toyota");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMake()).isEqualTo("Toyota");
    }

    @Test
    @DisplayName("searchVehicles (dynamic): filters by make and category")
    void searchVehicles_DynamicFilters_ReturnsMakeAndCategory() {
        when(vehicleRepository.searchVehicles("Toyota", null, Category.SEDAN, null, null))
                .thenReturn(List.of(testVehicle));

        List<Vehicle> result = vehicleService.searchVehicles("Toyota", null, Category.SEDAN, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo(Category.SEDAN);
    }

    @Test
    @DisplayName("searchVehicles (price range): returns vehicles within range")
    void searchVehiclesByPrice_ReturnsVehiclesInRange() {
        BigDecimal min = new BigDecimal("20000");
        BigDecimal max = new BigDecimal("30000");
        when(vehicleRepository.findByPriceBetween(min, max)).thenReturn(List.of(testVehicle));

        List<Vehicle> result = vehicleService.searchVehiclesByPrice(min, max);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPrice()).isEqualByComparingTo("25000.00");
    }

    // ─── Update Vehicle ───────────────────────────────────────────────────────

    @Test
    @DisplayName("updateVehicle: updates and returns the vehicle when ID exists")
    void updateVehicle_Exists_UpdatesAndReturns() {
        Vehicle updatedDetails = new Vehicle(null, "Honda", "Accord", Category.SUV, new BigDecimal("30000.00"), 10);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        Vehicle result = vehicleService.updateVehicle(1L, updatedDetails);

        assertThat(result.getMake()).isEqualTo("Honda");
        assertThat(result.getModel()).isEqualTo("Accord");
        assertThat(result.getCategory()).isEqualTo(Category.SUV);
        assertThat(result.getPrice()).isEqualByComparingTo("30000.00");
        verify(vehicleRepository, times(1)).save(testVehicle);
    }

    @Test
    @DisplayName("updateVehicle: throws RuntimeException when ID not found")
    void updateVehicle_NotExists_ThrowsException() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.updateVehicle(99L, testVehicle))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Vehicle not found with id: 99");
    }

    // ─── Delete Vehicle ───────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteVehicle: deletes successfully when ID exists")
    void deleteVehicle_Exists_DeletesSuccessfully() {
        when(vehicleRepository.existsById(1L)).thenReturn(true);
        doNothing().when(vehicleRepository).deleteById(1L);

        assertThatCode(() -> vehicleService.deleteVehicle(1L)).doesNotThrowAnyException();

        verify(vehicleRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteVehicle: throws RuntimeException when ID not found")
    void deleteVehicle_NotExists_ThrowsException() {
        when(vehicleRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> vehicleService.deleteVehicle(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Vehicle not found with id: 99");

        verify(vehicleRepository, never()).deleteById(anyLong());
    }

    // ─── Purchase Vehicle ─────────────────────────────────────────────────────

    @Test
    @DisplayName("purchaseVehicle: decrements quantity by 1 when in stock")
    void purchaseVehicle_InStock_DecrementsQuantity() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        Vehicle result = vehicleService.purchaseVehicle(1L);

        assertThat(result.getQuantity()).isEqualTo(4); // was 5
    }

    @Test
    @DisplayName("purchaseVehicle: throws RuntimeException when out of stock")
    void purchaseVehicle_OutOfStock_ThrowsException() {
        testVehicle.setQuantity(0);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        assertThatThrownBy(() -> vehicleService.purchaseVehicle(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Vehicle is out of stock");
    }

    // ─── Restock Vehicle ──────────────────────────────────────────────────────

    @Test
    @DisplayName("restockVehicle: increments quantity correctly")
    void restockVehicle_AddsQuantity() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        Vehicle result = vehicleService.restockVehicle(1L, 10);

        assertThat(result.getQuantity()).isEqualTo(15); // 5 + 10
    }

    @Test
    @DisplayName("restockVehicle: throws IllegalArgumentException for non-positive quantity")
    void restockVehicle_NegativeQuantity_ThrowsException() {
        assertThatThrownBy(() -> vehicleService.restockVehicle(1L, -5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity to add must be positive");

        verify(vehicleRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("restockVehicle: throws IllegalArgumentException for zero quantity")
    void restockVehicle_ZeroQuantity_ThrowsException() {
        assertThatThrownBy(() -> vehicleService.restockVehicle(1L, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity to add must be positive");
    }
}
