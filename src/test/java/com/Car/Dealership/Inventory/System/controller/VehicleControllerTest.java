package com.Car.Dealership.Inventory.System.controller;

import com.Car.Dealership.Inventory.System.entity.Category;
import com.Car.Dealership.Inventory.System.entity.Vehicle;
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
                .thenThrow(new RuntimeException("Vehicle not found"));

        mockMvc.perform(put("/api/vehicles/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testVehicle))
                .with(csrf()))
                .andExpect(status().isNotFound());
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
        doThrow(new RuntimeException("Vehicle not found")).when(vehicleService).deleteVehicle(99L);

        mockMvc.perform(delete("/api/vehicles/99")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
