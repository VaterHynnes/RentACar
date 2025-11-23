package de.rentacar.rental.web;

import de.rentacar.rental.application.RentalService;
import de.rentacar.rental.domain.DamageReport;
import de.rentacar.rental.domain.Rental;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * REST Controller für Vermietungsprozess
 */
@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @PostMapping("/checkout")
    public ResponseEntity<Rental> performCheckout(@RequestBody CheckoutRequest request,
                                                  Authentication authentication,
                                                  HttpServletRequest httpRequest) {
        Rental rental = rentalService.performCheckout(
                request.bookingId(),
                request.mileage(),
                request.condition(),
                authentication.getName(),
                httpRequest.getRemoteAddr()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(rental);
    }

    @PostMapping("/{id}/checkin")
    public ResponseEntity<Rental> performCheckin(@PathVariable Long id,
                                                 @RequestBody CheckinRequest request,
                                                 Authentication authentication,
                                                 HttpServletRequest httpRequest) {
        Rental rental = rentalService.performCheckin(
                id,
                request.mileage(),
                request.condition(),
                authentication.getName(),
                httpRequest.getRemoteAddr()
        );
        return ResponseEntity.ok(rental);
    }

    @PostMapping("/{id}/damage")
    public ResponseEntity<DamageReport> createDamageReport(@PathVariable Long id,
                                                          @RequestBody DamageReportRequest request,
                                                          Authentication authentication,
                                                          HttpServletRequest httpRequest) {
        DamageReport damageReport = rentalService.createDamageReport(
                id,
                request.description(),
                request.repairCost(),
                request.notes(),
                authentication.getName(),
                httpRequest.getRemoteAddr()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(damageReport);
    }

    public record CheckoutRequest(
            Long bookingId,
            Long mileage,
            String condition
    ) {}

    public record CheckinRequest(
            Long mileage,
            String condition
    ) {}

    public record DamageReportRequest(
            String description,
            BigDecimal repairCost,
            String notes
    ) {}
}

