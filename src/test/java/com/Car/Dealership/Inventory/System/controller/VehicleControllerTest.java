package com.Car.Dealership.Inventory.System.controller;

import com.Car.Dealership.Inventory.System.entity.Category;
import com.Car.Dealership.Inventory.System.entity.Vehicle;
import com.Car.Dealership.Inventory.System.exception.OutOfStockException;
import com.Car.Dealership.Inventory.System.exception.VehicleNotFoundException;
import com.Car.Dealership.Inventory.System.filter.JwtAuthFilter;
import com.Car.Dealership.Inventory.System.service.VehicleService;
import com.Car.Dealership.Inventory.System.util.JwtUtil;
import com.Car.Dealership.Inventory.System.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.annotation.Import;

@WebMvcTest(VehicleController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleService vehicleService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        testVehicle = new Vehicle(1L, "Toyota", "Camry", Category.SEDAN, new BigDecimal("25000.00"), 5);
    }

    // ─── Unauthenticated Request ─────────────────────────────────────────────

    @Test
    void getAllVehicles_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isUnauthorized());
    }

    // ─── Authenticated General User ──────────────────────────────────────────

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void getAllVehicles_Authenticated_ReturnsList() throws Exception {
        when(vehicleService.getAllVehicles()).thenReturn(List.of(testVehicle));

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].make").value("Toyota"))
                .andExpect(jsonPath("$[0].category").value("SEDAN"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void addVehicle_Authenticated_ReturnsCreated() throws Exception {
        when(vehicleService.addVehicle(any(Vehicle.class))).thenReturn(testVehicle);

        mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testVehicle))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.category").value("SEDAN"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void searchVehicles_ValidParams_ReturnsMatches() throws Exception {
        when(vehicleService.searchVehicles("Toyota", null, Category.SEDAN, null, null))
                .thenReturn(List.of(testVehicle));

        mockMvc.perform(get("/api/vehicles/search")
                .param("make", "Toyota")
                .param("category", "Sedan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].make").value("Toyota"))
                .andExpect(jsonPath("$[0].category").value("SEDAN"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void searchVehicles_InvalidCategory_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/vehicles/search")
                .param("category", "invalid-category-name"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void updateVehicle_Exists_ReturnsOk() throws Exception {
        when(vehicleService.updateVehicle(eq(1L), any(Vehicle.class))).thenReturn(testVehicle);

        mockMvc.perform(put("/api/vehicles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testVehicle))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Camry"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void updateVehicle_NotExists_ReturnsNotFound() throws Exception {
        when(vehicleService.updateVehicle(eq(99L), any(Vehicle.class)))
                .thenThrow(new VehicleNotFoundException(99L));

        mockMvc.perform(put("/api/vehicles/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testVehicle))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Vehicle not found with id: 99"));
    }

    // ─── Delete Endpoint Authorization ───────────────────────────────────────

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void deleteVehicle_AsUser_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/vehicles/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(vehicleService, never()).deleteVehicle(anyLong());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void deleteVehicle_AsAdmin_ReturnsNoContent() throws Exception {
        doNothing().when(vehicleService).deleteVehicle(1L);

        mockMvc.perform(delete("/api/vehicles/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(vehicleService, times(1)).deleteVehicle(1L);
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void deleteVehicle_NotFound_ReturnsNotFound() throws Exception {
        doThrow(new VehicleNotFoundException(99L)).when(vehicleService).deleteVehicle(99L);

        mockMvc.perform(delete("/api/vehicles/99")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Vehicle not found with id: 99"));
    }

    // ─── Purchase Endpoint ───────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void purchaseVehicle_Success_ReturnsOk() throws Exception {
        Vehicle purchasedVehicle = new Vehicle(1L, "Toyota", "Camry", Category.SEDAN, new BigDecimal("25000.00"), 4);
        when(vehicleService.purchaseVehicle(1L)).thenReturn(purchasedVehicle);

        mockMvc.perform(post("/api/vehicles/1/purchase")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(4));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void purchaseVehicle_NotFound_ReturnsNotFound() throws Exception {
        when(vehicleService.purchaseVehicle(99L)).thenThrow(new VehicleNotFoundException(99L));

        mockMvc.perform(post("/api/vehicles/99/purchase")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Vehicle not found with id: 99"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void purchaseVehicle_OutOfStock_ReturnsConflict() throws Exception {
        when(vehicleService.purchaseVehicle(1L)).thenThrow(new OutOfStockException(1L));

        mockMvc.perform(post("/api/vehicles/1/purchase")
                .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Vehicle with id 1 is out of stock"));
    }

    // ─── Restock Endpoint ────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void restockVehicle_Success_ReturnsOk() throws Exception {
        Vehicle restockedVehicle = new Vehicle(1L, "Toyota", "Camry", Category.SEDAN, new BigDecimal("25000.00"), 15);
        when(vehicleService.restockVehicle(1L, 10)).thenReturn(restockedVehicle);

        mockMvc.perform(post("/api/vehicles/1/restock")
                .param("quantity", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(15));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void restockVehicle_AsUser_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/vehicles/1/restock")
                .param("quantity", "10")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void restockVehicle_InvalidQuantity_ReturnsBadRequest() throws Exception {
        when(vehicleService.restockVehicle(1L, -5)).thenThrow(new IllegalArgumentException("Quantity to add must be positive"));

        mockMvc.perform(post("/api/vehicles/1/restock")
                .param("quantity", "-5")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Quantity to add must be positive"));
    }

    // ─── No Resource Found Handler Test ──────────────────────────────────────

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void nonExistentEndpoint_ReturnsNotFoundDetails() throws Exception {
        mockMvc.perform(get("/api/invalid-endpoint-url")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Resource not found: api/invalid-endpoint-url"));
    }
}
