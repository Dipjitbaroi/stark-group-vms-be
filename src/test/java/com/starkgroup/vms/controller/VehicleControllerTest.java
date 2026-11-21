package com.starkgroup.vms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.starkgroup.vms.dto.VehicleRequest;
import com.starkgroup.vms.dto.VehicleResponse;
import com.starkgroup.vms.exception.DuplicateVehicleNumberException;
import com.starkgroup.vms.exception.GlobalExceptionHandler;
import com.starkgroup.vms.exception.VehicleNotFoundException;
import com.starkgroup.vms.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private VehicleController vehicleController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(vehicleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("GET /api/vehicles returns 200 OK and list of vehicles")
    void getAllVehicles_Returns200AndList() throws Exception {
        VehicleResponse response = new VehicleResponse(
                1L, "STK-8421", "Stark Logistics", "Heavy Truck", "Volvo", "FH16 750", "Active",
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(vehicleService.getAllVehicles(any(), any(), any(), any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].vehicleNumber", is("STK-8421")))
                .andExpect(jsonPath("$[0].company", is("Stark Logistics")));
    }

    @Test
    @DisplayName("GET /api/vehicles with query parameters returns 200 OK filtered")
    void getAllVehicles_WithQueryParameters_Returns200() throws Exception {
        VehicleResponse response = new VehicleResponse(
                1L, "STK-8421", "Stark Logistics", "Heavy Truck", "Volvo", "FH16 750", "Active",
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(vehicleService.getAllVehicles("Volvo", "Active", "Stark Logistics", "Heavy Truck"))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/vehicles")
                        .param("search", "Volvo")
                        .param("status", "Active")
                        .param("company", "Stark Logistics")
                        .param("type", "Heavy Truck"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].brand", is("Volvo")));
    }

    @Test
    @DisplayName("GET /api/vehicles/{id} returns 200 OK when found")
    void getVehicleById_Returns200() throws Exception {
        VehicleResponse response = new VehicleResponse(
                1L, "STK-8421", "Stark Logistics", "Heavy Truck", "Volvo", "FH16 750", "Active",
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(vehicleService.getVehicleById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.vehicleNumber", is("STK-8421")));
    }

    @Test
    @DisplayName("GET /api/vehicles/{id} returns 404 Not Found when non-existent")
    void getVehicleById_NotFound_Returns404() throws Exception {
        when(vehicleService.getVehicleById(99L))
                .thenThrow(new VehicleNotFoundException("Vehicle not found with ID: 99"));

        mockMvc.perform(get("/api/vehicles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("99")));
    }

    @Test
    @DisplayName("POST /api/vehicles returns 201 Created on valid input")
    void createVehicle_ValidPayload_Returns201() throws Exception {
        VehicleRequest request = new VehicleRequest(
                "STK-8421", "Stark Logistics", "Heavy Truck", "Volvo", "FH16 750", "Active"
        );

        VehicleResponse response = new VehicleResponse(
                1L, "STK-8421", "Stark Logistics", "Heavy Truck", "Volvo", "FH16 750", "Active",
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(vehicleService.createVehicle(any(VehicleRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.vehicleNumber", is("STK-8421")));
    }

    @Test
    @DisplayName("POST /api/vehicles returns 409 Conflict when duplicate vehicleNumber exists")
    void createVehicle_DuplicateVehicleNumber_Returns409() throws Exception {
        VehicleRequest request = new VehicleRequest(
                "STK-8421", "Stark Logistics", "Heavy Truck", "Volvo", "FH16 750", "Active"
        );

        when(vehicleService.createVehicle(any(VehicleRequest.class)))
                .thenThrow(new DuplicateVehicleNumberException("Vehicle with number 'STK-8421' already exists."));

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", is("Vehicle with number 'STK-8421' already exists.")));
    }

    @Test
    @DisplayName("POST /api/vehicles returns 400 Bad Request on invalid input")
    void createVehicle_InvalidInput_Returns400() throws Exception {
        VehicleRequest invalidRequest = new VehicleRequest(
                "", "", "", "", "", ""
        );

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.errors.vehicleNumber", is("Vehicle number is required")));
    }

    @Test
    @DisplayName("PUT /api/vehicles/{id} returns 200 OK on valid update")
    void updateVehicle_ValidPayload_Returns200() throws Exception {
        VehicleRequest request = new VehicleRequest(
                "STK-8421", "Stark Industries", "Cargo Van", "Mercedes", "Sprinter", "Active"
        );

        VehicleResponse response = new VehicleResponse(
                1L, "STK-8421", "Stark Industries", "Cargo Van", "Mercedes", "Sprinter", "Active",
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(vehicleService.updateVehicle(eq(1L), any(VehicleRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company", is("Stark Industries")));
    }

    @Test
    @DisplayName("PUT /api/vehicles/{id} returns 409 Conflict when update causes duplicate vehicleNumber")
    void updateVehicle_DuplicateNumber_Returns409() throws Exception {
        VehicleRequest request = new VehicleRequest(
                "STK-2104", "Stark Industries", "Cargo Van", "Mercedes", "Sprinter", "Active"
        );

        when(vehicleService.updateVehicle(eq(1L), any(VehicleRequest.class)))
                .thenThrow(new DuplicateVehicleNumberException("Vehicle with number 'STK-2104' already exists."));

        mockMvc.perform(put("/api/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("STK-2104")));
    }

    @Test
    @DisplayName("DELETE /api/vehicles/{id} returns 204 No Content on success")
    void deleteVehicle_Returns204() throws Exception {
        doNothing().when(vehicleService).deleteVehicle(1L);

        mockMvc.perform(delete("/api/vehicles/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/vehicles/{id} returns 404 Not Found when ID does not exist")
    void deleteVehicle_NotFound_Returns404() throws Exception {
        doThrow(new VehicleNotFoundException("Vehicle not found with ID: 99"))
                .when(vehicleService).deleteVehicle(99L);

        mockMvc.perform(delete("/api/vehicles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}
