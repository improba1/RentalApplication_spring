package com.example.demo.controller;

import java.util.List;

import javax.management.relation.Role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.User;
import com.example.demo.model.Vehicle;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    private static final Logger logger = LoggerFactory.getLogger(VehicleController.class);
    private final VehicleService vehicleService;
    private final UserRepository userRepository;

    @Autowired
    public VehicleController(VehicleService vehicleService, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public List<Vehicle> getAll() {
        return vehicleService.findAll();
    }

    @GetMapping("/active")
    public List<Vehicle> getAllActive() {
        return vehicleService.findAllActive();
    }

    @DeleteMapping("/delete/{id}")
    public void softDelete(
        @PathVariable String id,
        @AuthenticationPrincipal UserDetails userDetails) {
            String login = userDetails.getUsername();
            User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found: " + login));
            if ("ADMIN".equals(user.getRole().name())) {
                vehicleService.deleteById(id);
            }else{
                logger.warn("User {} attempted to delete vehicle with ID: {} without sufficient permissions", login, id);
                throw new SecurityException("You do not have permission to delete this vehicle.");
            }
            
    }

    @GetMapping("/available")
    public List<Vehicle> getAvailableVehicles() {
        return vehicleService.findAvailableVehicles();
    }

    @GetMapping("/rented")
    public List<Vehicle> getRentedVehicles(@AuthenticationPrincipal UserDetails userDetails) {
        String login = userDetails.getUsername();
        User user = userRepository.findByLogin(login)
            .orElseThrow(() -> new RuntimeException("User not found: " + login));
        if ("ADMIN".equals(user.getRole().name())) {
            return vehicleService.findRentedVehicles();
        }else{
            logger.warn("User {} attempted to access rented vehicles without sufficient permissions", login);
            throw new SecurityException("You do not have permission to view rented vehicles.");
        }
        
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable String id) {
        logger.info("Request received for vehicle with ID: {}", id);
        return vehicleService.findById(id)
            .map(vehicle -> {
            return ResponseEntity.ok(vehicle);
            })
            .orElseGet(() -> {
                return ResponseEntity.notFound().build();
            });
    }

    @PostMapping
    public ResponseEntity<Vehicle> addVehicle(
        @RequestBody Vehicle vehicle, 
        @AuthenticationPrincipal UserDetails userDetails) {
            String login = userDetails.getUsername();
            User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found: " + login));
            if ("ADMIN".equals(user.getRole().name())) {
                try {
                    Vehicle savedVehicle = vehicleService.save(vehicle);
                    return ResponseEntity.status(HttpStatus.CREATED).body(savedVehicle);
                } catch (Exception e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }} else {
                    logger.warn("User {} attempted to add a vehicle without sufficient permissions", login);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
    }
}
