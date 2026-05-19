package com.codex.phoenixmiles;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class FlightInput {
    String flightNumber = "";
    LocalDate travelDate;
    String originCode = "";
    String destinationCode = "";
    String bookingClass = "";
    int extraNonStatusMiles;
    String sourceText = "";

    String dateText() {
        return travelDate == null ? "" : travelDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    boolean hasRequiredFields() {
        return !flightNumber.isEmpty()
                && travelDate != null
                && !originCode.isEmpty()
                && !destinationCode.isEmpty()
                && !bookingClass.isEmpty();
    }

    String normalizedFlightNumber() {
        return flightNumber == null ? "" : flightNumber.replace(" ", "").toUpperCase(Locale.US);
    }
}
