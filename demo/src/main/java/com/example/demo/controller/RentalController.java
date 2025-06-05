package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.RentalRequest;
import com.example.demo.model.Rental;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.RentalService;
import com.example.demo.services.impl.RentalServiceImpl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;
    private final UserRepository userRepository;

   @PostMapping("/rent")
    public ResponseEntity<Rental> rentVehicle(
        @RequestBody RentalRequest rentalRequest, 
        @AuthenticationPrincipal UserDetails userDetails) {
            if (rentalRequest.vehicleId == null || userDetails.getUsername() == null) {
                return ResponseEntity.badRequest().build();
            }else if(rentalService.isVehicleRented(rentalRequest.vehicleId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle is already rented");

            }
            try {
                String username = userDetails.getUsername();
                User user = userRepository.findByLogin(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                Rental rental = rentalService.rent(rentalRequest.vehicleId, user.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(rental);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

    @PostMapping("/return")
public ResponseEntity<?> returnVehicle(
    @RequestBody RentalRequest rentalRequest, 
    @AuthenticationPrincipal UserDetails userDetails) {
    String login = userDetails.getUsername();
    User user = userRepository.findByLogin(login)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    boolean success = rentalService.returnRental(rentalRequest.vehicleId, user.getId());
    return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body("Return failed");
}

    @GetMapping
    public List<Rental> getAllRentals() {
        return rentalService.findAll();
    }
}
