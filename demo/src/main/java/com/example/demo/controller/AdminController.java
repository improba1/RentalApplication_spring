package com.example.demo.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.LocationDto;
import com.example.demo.repository.VehicleRepository;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final VehicleRepository vehicleRepository;

    public AdminController(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @GetMapping("/vehicles/locations")
    public List<LocationDto> getAllVehicleLocations() {
        return vehicleRepository.findAll().stream()
                .map(vehicle -> new LocationDto(vehicle.getId(), vehicle.getLatitude(), vehicle.getLongitude()))
                .toList();
    }
}