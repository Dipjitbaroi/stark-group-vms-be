package com.starkgroup.vms.service;

import com.starkgroup.vms.domain.Vehicle;
import com.starkgroup.vms.dto.VehicleRequest;
import com.starkgroup.vms.dto.VehicleResponse;
import com.starkgroup.vms.exception.DuplicateVehicleNumberException;
import com.starkgroup.vms.exception.VehicleNotFoundException;
import com.starkgroup.vms.repository.VehicleRepository;
import com.starkgroup.vms.service.impl.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private Vehicle sampleVehicle;
    private VehicleRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleVehicle = new Vehicle(
                1L,
                "STK-8421",
                "Stark Logistics",
                "Heavy Truck",
                "Volvo",
                "FH16 750",
                "Active"
        );

        sampleRequest = new VehicleRequest(
                "  STK-8421  ",
                "Stark Logistics",
                "Heavy Truck",
                "Volvo",
                "FH16 750",
                "Active"
        );
    }

    @Test
    @DisplayName("Creating a valid vehicle succeeds and persists normalized fields")
    void createVehicle_Success_PersistsNormalizedFields() {
        when(vehicleRepository.existsByVehicleNumberIgnoreCase("STK-8421")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle toSave = invocation.getArgument(0);
            toSave.setId(1L);
            return toSave;
        });

        VehicleResponse response = vehicleService.createVehicle(sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getVehicleNumber()).isEqualTo("STK-8421");
        assertThat(response.getCompany()).isEqualTo("Stark Logistics");
        assertThat(response.getType()).isEqualTo("Heavy Truck");
        assertThat(response.getBrand()).isEqualTo("Volvo");
        assertThat(response.getModel()).isEqualTo("FH16 750");
        assertThat(response.getStatus()).isEqualTo("Active");

        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(captor.capture());
        assertThat(captor.getValue().getVehicleNumber()).isEqualTo("STK-8421");
    }

    @Test
    @DisplayName("Creating a vehicle with duplicate vehicle number throws DuplicateVehicleNumberException and never calls repository.save()")
    void createVehicle_DuplicateVehicleNumber_ThrowsException_NeverCallsSave() {
        when(vehicleRepository.existsByVehicleNumberIgnoreCase("STK-8421")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.createVehicle(sampleRequest))
                .isInstanceOf(DuplicateVehicleNumberException.class)
                .hasMessage("Vehicle with number 'STK-8421' already exists.");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Creating a vehicle with case and whitespace variation of duplicate throws DuplicateVehicleNumberException")
    void createVehicle_DuplicateWithWhitespaceAndCase_ThrowsException() {
        VehicleRequest requestWithVariations = new VehicleRequest(
                "   stk-8421   ",
                "Stark Logistics",
                "Heavy Truck",
                "Volvo",
                "FH16 750",
                "Active"
        );

        when(vehicleRepository.existsByVehicleNumberIgnoreCase("stk-8421")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.createVehicle(requestWithVariations))
                .isInstanceOf(DuplicateVehicleNumberException.class)
                .hasMessage("Vehicle with number 'stk-8421' already exists.");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("getAllVehicles returns list of vehicle responses")
    void getAllVehicles_ReturnsList() {
        when(vehicleRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(sampleVehicle));

        List<VehicleResponse> list = vehicleService.getAllVehicles();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getVehicleNumber()).isEqualTo("STK-8421");
        verify(vehicleRepository).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    @DisplayName("getAllVehicles with query parameters filters correctly")
    void getAllVehicles_WithFilters_ReturnsFilteredList() {
        when(vehicleRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(sampleVehicle));

        List<VehicleResponse> list = vehicleService.getAllVehicles("Volvo", "Active", "Stark Logistics", "Heavy Truck");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getBrand()).isEqualTo("Volvo");
        verify(vehicleRepository).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    @DisplayName("getVehicleById returns vehicle response when found")
    void getVehicleById_Success() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(sampleVehicle));

        VehicleResponse response = vehicleService.getVehicleById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getVehicleNumber()).isEqualTo("STK-8421");
        verify(vehicleRepository).findById(1L);
    }

    @Test
    @DisplayName("getVehicleById throws VehicleNotFoundException when ID does not exist")
    void getVehicleById_NotFound_ThrowsException() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.getVehicleById(99L))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessage("Vehicle not found with ID: 99");

        verify(vehicleRepository).findById(99L);
    }

    @Test
    @DisplayName("Updating a vehicle succeeds and updates fields with normalized vehicle number")
    void updateVehicle_Success() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(sampleVehicle));
        when(vehicleRepository.existsByVehicleNumberIgnoreCaseAndIdNot("STK-8421-NEW", 1L)).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehicleRequest updateReq = new VehicleRequest(
                "  STK-8421-NEW  ",
                "Stark Industries",
                "Electric Fleet",
                "Volvo",
                "FH Electric",
                "Inactive"
        );

        VehicleResponse updated = vehicleService.updateVehicle(1L, updateReq);

        assertThat(updated.getVehicleNumber()).isEqualTo("STK-8421-NEW");
        assertThat(updated.getCompany()).isEqualTo("Stark Industries");
        assertThat(updated.getType()).isEqualTo("Electric Fleet");
        assertThat(updated.getModel()).isEqualTo("FH Electric");
        assertThat(updated.getStatus()).isEqualTo("Inactive");
        verify(vehicleRepository).save(sampleVehicle);
    }

    @Test
    @DisplayName("Updating a non-existent vehicle throws VehicleNotFoundException")
    void updateVehicle_NotFound_ThrowsException() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.updateVehicle(99L, sampleRequest))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessage("Vehicle not found with ID: 99");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Updating a vehicle with duplicate vehicle number throws DuplicateVehicleNumberException")
    void updateVehicle_DuplicateOnUpdate_ThrowsException() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(sampleVehicle));
        when(vehicleRepository.existsByVehicleNumberIgnoreCaseAndIdNot("STK-2104", 1L)).thenReturn(true);

        VehicleRequest duplicateReq = new VehicleRequest(
                "STK-2104",
                "Stark Logistics",
                "Heavy Truck",
                "Volvo",
                "FH16 750",
                "Active"
        );

        assertThatThrownBy(() -> vehicleService.updateVehicle(1L, duplicateReq))
                .isInstanceOf(DuplicateVehicleNumberException.class)
                .hasMessage("Vehicle with number 'STK-2104' already exists.");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Deleting a vehicle succeeds when vehicle exists")
    void deleteVehicle_Success() {
        when(vehicleRepository.existsById(1L)).thenReturn(true);

        vehicleService.deleteVehicle(1L);

        verify(vehicleRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deleting a vehicle throws VehicleNotFoundException when vehicle does not exist")
    void deleteVehicle_NotFound_ThrowsException() {
        when(vehicleRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> vehicleService.deleteVehicle(99L))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessage("Vehicle not found with ID: 99");

        verify(vehicleRepository, never()).deleteById(anyLong());
    }
}
