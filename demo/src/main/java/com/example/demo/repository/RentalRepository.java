package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Rental;
import com.example.demo.model.Vehicle;

@Repository
public interface RentalRepository extends JpaRepository<Rental, String> {
     Optional<Rental> findByVehicleIdAndActiveTrue(String vehicleId);
}
