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
import com.example.demo.model.Vehicle;
import com.example.demo.repository.UserRepository;
import com.example.demo.services.RentalService;
import com.example.demo.services.VehicleService;
import com.example.demo.services.impl.RentalServiceImpl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;
    private final UserRepository userRepository;
    private final VehicleService vehicleService;

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
            Vehicle vehicle = vehicleService.findById(rentalRequest.vehicleId).orElseThrow();
            if (isInAllowedZone(vehicle)){
                boolean success = rentalService.returnRental(rentalRequest.vehicleId, user.getId());
                return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().body("Return failed");
            }else{
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("This vehicle is not in allowed zone!");
            }
        }

    @GetMapping
    public List<Rental> getAllRentals() {
        return rentalService.findAll();
    }

    private boolean isInAllowedZone(Vehicle vehicle){
        //51.251901, 22.570862
            double companyLat = 51.251901;
            double companyLon = 22.570862;
            return distance(vehicle.getLatitude(), vehicle.getLongitude(), companyLat, companyLon) < 0.5; 
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371; 
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c; 
    }
}
