package com.example.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Rental;
import com.example.demo.services.RentalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;

    @GetMapping("/rent")
    public Rental rent(@RequestParam String vehicleId, @RequestParam String userId) {
        return rentalService.rent(vehicleId, userId);
    }

    @PostMapping("/return")
    public ResponseEntity<?> returnVehicle(@RequestParam String vehicleId, @RequestParam String userId) {
        boolean success = rentalService.returnRental(vehicleId, userId);
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body("Return failed");
    }

    @GetMapping
    public List<Rental> getAllRentals() {
        return rentalService.findAll();
    }
}
