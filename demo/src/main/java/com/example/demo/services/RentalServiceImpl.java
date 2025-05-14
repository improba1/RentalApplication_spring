package com.example.demo.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.model.Rental;
import com.example.demo.model.Vehicle;
import com.example.demo.repository.RentalRepository;
import com.example.demo.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private final VehicleRepository vehicleRepository;
    private final RentalRepository rentalRepository;

    @Override
    public boolean isVehicleRented(String vehicleId) {
        return rentalRepository.findByVehicleIdAndActiveTrue(vehicleId)
                .map(rental -> true)
                .orElse(false);
    }

    @Override
    public Optional<Rental> findActiveRentalByVehicleId(String vehicleId) {
       return rentalRepository.findByVehicleIdAndActiveTrue(vehicleId);
    }

    @Override
    public Rental rent(String vehicleId, String userId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        if (vehicle.isRented() || !vehicle.isActive()) {
            throw new RuntimeException("Vehicle is not available for rent");
        }
        vehicle.setRented(true);
        vehicleRepository.save(vehicle);
        Rental rental = new Rental();
        rental.setVehicleId(vehicleId);
        rental.setUserId(userId);
        rental.setRentedAt(LocalDateTime.now());
        return rentalRepository.save(rental);
    }

    @Override
    public boolean returnRental(String vehicleId, String userId) {
        Optional<Rental> rentalOpt = rentalRepository.findByVehicleIdAndActiveTrue(vehicleId);
        if (rentalOpt.isEmpty()){
            return false;
        }

        Rental rental = rentalOpt.get();
        if(!rental.getUserId().equals(userId)){
            return false;
        }
        rental.setActive(false);
        rental.setReturnAt(LocalDateTime.now());
        rentalRepository.save(rental);
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElseThrow();
        vehicle.setRented(false);
        vehicleRepository.save(vehicle);
        return true;
    }

    @Override
    public List<Rental> findAll() {
        return rentalRepository.findAll();
    }
    
}
