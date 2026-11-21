package com.starkgroup.vms.service;

import com.starkgroup.vms.dto.VehicleRequest;
import com.starkgroup.vms.dto.VehicleResponse;

import java.util.List;

public interface VehicleService {

    VehicleResponse createVehicle(VehicleRequest request);

    default List<VehicleResponse> getAllVehicles() {
        return getAllVehicles(null, null, null, null);
    }

    List<VehicleResponse> getAllVehicles(String search, String status, String company, String type);

    VehicleResponse getVehicleById(Long id);

    VehicleResponse updateVehicle(Long id, VehicleRequest request);

    void deleteVehicle(Long id);
}
