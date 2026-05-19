package com.codex.phoenixmiles;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

final class FastLineRules {
    private static final LocalDate START_2026 = LocalDate.of(2026, 1, 1);
    private static final LocalDate END_2026 = LocalDate.of(2026, 12, 31);
    private static final LocalDate PEK_XIY_START = LocalDate.of(2026, 3, 29);
    private static final Set<String> ROUTES = new HashSet<>(Arrays.asList(
            route("PEK", "SHA"),
            route("PEK", "HGH"),
            route("PEK", "CTU"),
            route("PEK", "CKG"),
            route("PEK", "CAN"),
            route("PEK", "SZX"),
            route("PEK", "XMN"),
            route("PEK", "WUH"),
            route("PEK", "URC"),
            route("PEK", "XIY"),
            route("CTU", "HGH"),
            route("CTU", "SZX"),
            route("CTU", "TSN"),
            route("TFU", "TSN"),
            route("CKG", "SZX")
    ));

    private FastLineRules() {
    }

    static FastLineBonus calculate(FlightInput input, FareRule fareRule) {
        if (input == null || fareRule == null || input.travelDate == null) {
            return FastLineBonus.none();
        }
        if (!input.normalizedFlightNumber().startsWith("CA")) {
            return FastLineBonus.none();
        }
        String route = route(input.originCode, input.destinationCode);
        if (!ROUTES.contains(route)) {
            return FastLineBonus.none();
        }
        if (route.equals(route("PEK", "XIY")) && input.travelDate.isBefore(PEK_XIY_START)) {
            return FastLineBonus.none();
        }
        if (input.travelDate.isBefore(START_2026) || input.travelDate.isAfter(END_2026)) {
            return FastLineBonus.none();
        }

        double rate = bonusRate(fareRule.bookingClass);
        if (rate <= 0) {
            return FastLineBonus.none();
        }
        double segments = ceil2(fareRule.statusSegments * rate);
        return new FastLineBonus(true, rate, segments);
    }

    private static double bonusRate(char bookingClass) {
        char cabin = Character.toUpperCase(bookingClass);
        if ("FAJCDZRGEY".indexOf(cabin) >= 0) {
            return 0.30;
        }
        if ("BMUHQV".indexOf(cabin) >= 0) {
            return 0.20;
        }
        if ("WSTLKP".indexOf(cabin) >= 0) {
            return 0.10;
        }
        return 0;
    }

    private static String route(String left, String right) {
        String first = left == null ? "" : left.toUpperCase(Locale.US);
        String second = right == null ? "" : right.toUpperCase(Locale.US);
        return first.compareTo(second) <= 0 ? first + "-" + second : second + "-" + first;
    }

    private static double ceil2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.CEILING).doubleValue();
    }
}
