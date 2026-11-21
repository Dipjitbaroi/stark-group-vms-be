package com.starkgroup.vms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_number", nullable = false, unique = true, length = 50)
    private String vehicleNumber;

    @Column(name = "company", nullable = false, length = 100)
    private String company;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "brand", nullable = false, length = 50)
    private String brand;

    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Vehicle() {
    }

    public Vehicle(String vehicleNumber, String company, String type, String brand, String model, String status) {
        this.vehicleNumber = vehicleNumber != null ? vehicleNumber.trim() : null;
        this.company = company != null ? company.trim() : null;
        this.type = type != null ? type.trim() : null;
        this.brand = brand != null ? brand.trim() : null;
        this.model = model != null ? model.trim() : null;
        this.status = status != null ? status.trim() : null;
    }

    public Vehicle(Long id, String vehicleNumber, String company, String type, String brand, String model, String status) {
        this(vehicleNumber, company, type, brand, model, status);
        this.id = id;
    }

    @PrePersist
    public void prePersist() {
        if (this.vehicleNumber != null) {
            this.vehicleNumber = this.vehicleNumber.trim();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        if (this.vehicleNumber != null) {
            this.vehicleNumber = this.vehicleNumber.trim();
        }
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber != null ? vehicleNumber.trim() : null;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company != null ? company.trim() : null;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type != null ? type.trim() : null;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand != null ? brand.trim() : null;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model != null ? model.trim() : null;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status != null ? status.trim() : null;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle vehicle)) return false;
        return Objects.equals(id, vehicle.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                ", vehicleNumber='" + vehicleNumber + '\'' +
                ", company='" + company + '\'' +
                ", type='" + type + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
