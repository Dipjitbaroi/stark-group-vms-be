package com.starkgroup.vms.service.impl;

import com.starkgroup.vms.domain.Vehicle;
import com.starkgroup.vms.dto.VehicleRequest;
import com.starkgroup.vms.dto.VehicleResponse;
import com.starkgroup.vms.exception.DuplicateVehicleNumberException;
import com.starkgroup.vms.exception.VehicleNotFoundException;
import com.starkgroup.vms.repository.VehicleRepository;
import com.starkgroup.vms.service.VehicleService;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public VehicleResponse createVehicle(VehicleRequest request) {
        String normalizedNumber = normalizeVehicleNumber(request.getVehicleNumber());

        if (vehicleRepository.existsByVehicleNumberIgnoreCase(normalizedNumber)) {
            throw new DuplicateVehicleNumberException("Vehicle with number '" + normalizedNumber + "' already exists.");
        }

        Vehicle vehicle = new Vehicle(
                normalizedNumber,
                request.getCompany(),
                request.getType(),
                request.getBrand(),
                request.getModel(),
                request.getStatus()
        );

        Vehicle saved = vehicleRepository.save(vehicle);
        return VehicleResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles() {
        return getAllVehicles(null, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles(String search, String status, String company, String type) {
        Specification<Vehicle> spec = (root, query, cb) -> cb.conjunction();

        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All")) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("status")), status.trim().toLowerCase()));
        }

        if (company != null && !company.isBlank() && !company.equalsIgnoreCase("All")) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("company"), company.trim()));
        }

        if (type != null && !type.isBlank() && !type.equalsIgnoreCase("All")) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("type"), type.trim()));
        }

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("vehicleNumber")), pattern),
                    cb.like(cb.lower(root.get("brand")), pattern),
                    cb.like(cb.lower(root.get("model")), pattern),
                    cb.like(cb.lower(root.get("company")), pattern),
                    cb.like(cb.lower(root.get("type")), pattern)
            ));
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        return vehicleRepository.findAll(spec, sort)
                .stream()
                .map(VehicleResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with ID: " + id));
        return VehicleResponse.fromEntity(vehicle);
    }

    @Override
    public VehicleResponse updateVehicle(Long id, VehicleRequest request) {
        Vehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with ID: " + id));

        String normalizedNumber = normalizeVehicleNumber(request.getVehicleNumber());

        if (vehicleRepository.existsByVehicleNumberIgnoreCaseAndIdNot(normalizedNumber, id)) {
            throw new DuplicateVehicleNumberException("Vehicle with number '" + normalizedNumber + "' already exists.");
        }

        existing.setVehicleNumber(normalizedNumber);
        existing.setCompany(request.getCompany());
        existing.setType(request.getType());
        existing.setBrand(request.getBrand());
        existing.setModel(request.getModel());
        existing.setStatus(request.getStatus());

        Vehicle updated = vehicleRepository.save(existing);
        return VehicleResponse.fromEntity(updated);
    }

    @Override
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new VehicleNotFoundException("Vehicle not found with ID: " + id);
        }
        vehicleRepository.deleteById(id);
    }

    private String normalizeVehicleNumber(String vehicleNumber) {
        return vehicleNumber != null ? vehicleNumber.trim() : "";
    }
}
