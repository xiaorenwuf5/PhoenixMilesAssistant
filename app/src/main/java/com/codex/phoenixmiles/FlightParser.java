package com.codex.phoenixmiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class FlightParser {
    private static final Pattern FLIGHT_PATTERN = Pattern.compile("\\b(CA)\\s*([0-9]{3,4})\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_PATTERN = Pattern.compile("(?<!\\d)(20\\d{2})[-年/.](\\d{1,2})[-月/.](\\d{1,2})");
    private static final Pattern SHORT_DATE_PATTERN = Pattern.compile("(?<!\\d)(\\d{1,2})[-月/.](\\d{1,2})(?!\\d)");
    private static final Pattern CABIN_PAREN_PATTERN = Pattern.compile("[舱等]?\\s*[（(]\\s*([A-Z])\\s*[）)]", Pattern.CASE_INSENSITIVE);
    private static final Pattern CABIN_TEXT_PATTERN = Pattern.compile("舱等\\s*[:：]?\\s*([A-Z])|([A-Z])\\s*舱", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXTRA_MILE_PATTERN = Pattern.compile("额外\\s*([0-9]{2,5})\\s*里程");

    private FlightParser() {
    }

    static FlightInput parse(String text) {
        FlightInput input = new FlightInput();
        input.sourceText = text == null ? "" : text;
        String value = input.sourceText.replace('\u00A0', ' ');

        Matcher flightMatcher = FLIGHT_PATTERN.matcher(value);
        if (flightMatcher.find()) {
            input.flightNumber = (flightMatcher.group(1) + flightMatcher.group(2)).toUpperCase(Locale.US);
        }

        input.travelDate = parseDate(value);

        List<Airport> airports = AirportCatalog.findAllIn(value);
        if (!airports.isEmpty()) {
            input.originCode = airports.get(0).code;
        }
        if (airports.size() > 1) {
            input.destinationCode = airports.get(1).code;
        }

        input.bookingClass = parseCabin(value);

        Matcher extraMatcher = EXTRA_MILE_PATTERN.matcher(value.replace(" ", ""));
        if (extraMatcher.find()) {
            input.extraNonStatusMiles = safeInt(extraMatcher.group(1), 0);
        }

        return input;
    }

    private static LocalDate parseDate(String value) {
        Matcher fullMatcher = DATE_PATTERN.matcher(value);
        if (fullMatcher.find()) {
            int year = safeInt(fullMatcher.group(1), 0);
            int month = safeInt(fullMatcher.group(2), 0);
            int day = safeInt(fullMatcher.group(3), 0);
            return safeDate(year, month, day);
        }

        Matcher shortMatcher = SHORT_DATE_PATTERN.matcher(value);
        if (shortMatcher.find()) {
            int month = safeInt(shortMatcher.group(1), 0);
            int day = safeInt(shortMatcher.group(2), 0);
            LocalDate today = LocalDate.now();
            LocalDate date = safeDate(today.getYear(), month, day);
            if (date != null && date.isBefore(today.minusDays(90))) {
                date = safeDate(today.getYear() + 1, month, day);
            }
            return date;
        }
        return null;
    }

    private static String parseCabin(String value) {
        Matcher parenMatcher = CABIN_PAREN_PATTERN.matcher(value);
        while (parenMatcher.find()) {
            String cabin = parenMatcher.group(1).toUpperCase(Locale.US);
            if (FareRules.find(cabin) != null) {
                return cabin;
            }
        }

        Matcher textMatcher = CABIN_TEXT_PATTERN.matcher(value);
        while (textMatcher.find()) {
            String cabin = textMatcher.group(1) != null ? textMatcher.group(1) : textMatcher.group(2);
            if (cabin != null && FareRules.find(cabin) != null) {
                return cabin.toUpperCase(Locale.US);
            }
        }
        return "";
    }

    private static LocalDate safeDate(int year, int month, int day) {
        try {
            if (year < 2000 || month < 1 || month > 12 || day < 1 || day > 31) {
                return null;
            }
            return LocalDate.of(year, month, day);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static int safeInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }
}
