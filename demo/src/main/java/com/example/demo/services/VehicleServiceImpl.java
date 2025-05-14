package com.example.demo.services;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.example.demo.model.Vehicle;
import com.example.demo.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {
    private final VehicleRepository vehicleRepository;
    @Override
    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
    }

    @Override
    public List<Vehicle> findAllActive() {
        return vehicleRepository.findByActiveTrue();
    }

    @Override
    public Optional<Vehicle> findById(String id) {
        return vehicleRepository.findById(id);
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    @Override
    public List<Vehicle> findAvailableVehicles() {
        return vehicleRepository.findByRentedFalse();
    }

    @Override
    public List<Vehicle> findRentedVehicles() {
        return vehicleRepository.findByRentedTrue();
    }

    @Override
    public boolean isAvailable(String vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .map(vehicle -> !vehicle.isRented())
                .orElse(false);
    }

    @Override
    public void deleteById(String id) {
        vehicleRepository.findById(id).ifPresent(vehicle -> {
            vehicle.setActive(false);
            vehicleRepository.save(vehicle);
        });
    }
    
}
