package com.starkgroup.vms.config;

import com.starkgroup.vms.domain.Vehicle;
import com.starkgroup.vms.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final VehicleRepository vehicleRepository;
    private final boolean seedEnabled;

    public DataInitializer(
            VehicleRepository vehicleRepository,
            @Value("${vms.seed.enabled:true}") boolean seedEnabled) {
        this.vehicleRepository = vehicleRepository;
        this.seedEnabled = seedEnabled;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("Database seeding is disabled (vms.seed.enabled=false).");
            return;
        }

        if (vehicleRepository.count() == 0) {
            log.info("Seeding initial Stark Group corporate fleet vehicles into database...");

            List<Vehicle> initialFleet = List.of(
                    new Vehicle("STK-8421", "Stark Logistics", "Heavy Truck", "Volvo", "FH16 750", "Active"),
                    new Vehicle("STK-2104", "Stark Industries", "Cargo Van", "Mercedes-Benz", "Sprinter 314", "Active"),
                    new Vehicle("STK-9055", "Apex Fleet Corp", "SUV", "Toyota", "Land Cruiser 300", "Active"),
                    new Vehicle("STK-3312", "Stark Logistics", "Pickup", "Ford", "F-150 Lightning", "Active"),
                    new Vehicle("STK-5520", "Apex Fleet Corp", "Sedan", "Tesla", "Model 3 Performance", "Inactive"),
                    new Vehicle("STK-7744", "Stark Industries", "Delivery Truck", "Isuzu", "N-Series NPR", "Active"),
                    new Vehicle("STK-1198", "Stark Logistics", "Heavy Hauler", "Scania", "R 500 V8", "Inactive")
            );

            vehicleRepository.saveAll(initialFleet);
            log.info("Seeded {} fleet vehicles successfully.", initialFleet.size());
        }
    }
}
