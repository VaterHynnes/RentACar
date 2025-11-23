package de.rentacar.booking.domain;

import de.rentacar.vehicle.domain.VehicleType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Domain Service für Preisberechnung
 */
@Service
public class PriceCalculationService {

    private static final BigDecimal BASE_PRICE_KLEINWAGEN = BigDecimal.valueOf(30.00);
    private static final BigDecimal BASE_PRICE_KOMPAKTKLASSE = BigDecimal.valueOf(40.00);
    private static final BigDecimal BASE_PRICE_MITTELKLASSE = BigDecimal.valueOf(60.00);
    private static final BigDecimal BASE_PRICE_OBERKLASSE = BigDecimal.valueOf(100.00);
    private static final BigDecimal BASE_PRICE_SUV = BigDecimal.valueOf(80.00);
    private static final BigDecimal BASE_PRICE_VAN = BigDecimal.valueOf(70.00);
    private static final BigDecimal BASE_PRICE_SPORTWAGEN = BigDecimal.valueOf(150.00);

    /**
     * Berechnet den Gesamtpreis basierend auf Fahrzeugtyp und Dauer
     */
    public BigDecimal calculateTotalPrice(VehicleType vehicleType, LocalDate pickupDate, LocalDate returnDate) {
        if (pickupDate.isAfter(returnDate)) {
            throw new IllegalArgumentException("Abholdatum muss vor Rückgabedatum liegen");
        }

        long days = ChronoUnit.DAYS.between(pickupDate, returnDate) + 1;
        if (days < 1) {
            throw new IllegalArgumentException("Mindestmietdauer: 1 Tag");
        }

        BigDecimal dailyPrice = getDailyPriceForType(vehicleType);
        return dailyPrice.multiply(BigDecimal.valueOf(days));
    }

    private BigDecimal getDailyPriceForType(VehicleType type) {
        return switch (type) {
            case KLEINWAGEN -> BASE_PRICE_KLEINWAGEN;
            case KOMPAKTKLASSE -> BASE_PRICE_KOMPAKTKLASSE;
            case MITTELKLASSE -> BASE_PRICE_MITTELKLASSE;
            case OBERKLASSE -> BASE_PRICE_OBERKLASSE;
            case SUV -> BASE_PRICE_SUV;
            case VAN -> BASE_PRICE_VAN;
            case SPORTWAGEN -> BASE_PRICE_SPORTWAGEN;
        };
    }
}

