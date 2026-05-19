package com.codex.phoenixmiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class FlightParser {
    private static final String OCR_DIGIT = "[0-9OIL]";
    private static final String OCR_FLIGHT_DIGITS = OCR_DIGIT + "(?:\\s*[- ]?\\s*" + OCR_DIGIT + "){2,3}";
    private static final Pattern FLIGHT_WITH_CARRIER_PATTERN = Pattern.compile("(?<![A-Z0-9])([C0O]\\s*[- ]?\\s*A)\\s*[- ]?\\s*(" + OCR_FLIGHT_DIGITS + ")(?!\\s*[- ]?\\s*" + OCR_DIGIT + ")", Pattern.CASE_INSENSITIVE);
    private static final Pattern AIR_CHINA_NUMBER_PATTERN = Pattern.compile("(中国国航|国航|航班号|航班)\\s*[:：#号\\- ]?\\s*(" + OCR_FLIGHT_DIGITS + ")(?!\\s*[- ]?\\s*" + OCR_DIGIT + ")", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_PATTERN = Pattern.compile("(?<!\\d)(20\\d{2})[-年/.](\\d{1,2})[-月/.](\\d{1,2})");
    private static final Pattern SHORT_DATE_PATTERN = Pattern.compile("(?<!\\d)(\\d{1,2})[-月/.](\\d{1,2})(?!\\d)");
    private static final Pattern CABIN_PAREN_PATTERN = Pattern.compile("[舱等]?\\s*[（(]\\s*([A-Z])\\s*[）)]", Pattern.CASE_INSENSITIVE);
    private static final Pattern CABIN_TEXT_PATTERN = Pattern.compile("舱等\\s*[:：]?\\s*([A-Z])|([A-Z])\\s*舱", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXTRA_MILE_PATTERN = Pattern.compile("额外\\s*([0-9]{2,5})\\s*里程");
    private static final String[] FLIGHT_SCORE_KEYWORDS = {
            "中国国航", "国航", "航班号", "航班", "CA", "波音", "机型", "承运", "航空公司"
    };

    private FlightParser() {
    }

    static FlightInput parse(String text) {
        FlightInput input = new FlightInput();
        input.sourceText = text == null ? "" : text;
        String value = input.sourceText.replace('\u00A0', ' ');

        input.flightNumber = parseFlightNumber(value);

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

    private static String parseFlightNumber(String value) {
        List<FlightCandidate> candidates = new ArrayList<>();
        collectCarrierCandidates(value, candidates);
        collectAirChinaNumberCandidates(value, candidates);
        if (candidates.isEmpty()) {
            return "";
        }

        Collections.sort(candidates, (left, right) -> {
            if (left.score != right.score) {
                return Integer.compare(right.score, left.score);
            }
            if (left.flightNumber.length() != right.flightNumber.length()) {
                return Integer.compare(right.flightNumber.length(), left.flightNumber.length());
            }
            return Integer.compare(left.start, right.start);
        });
        return candidates.get(0).flightNumber;
    }

    private static void collectCarrierCandidates(String value, List<FlightCandidate> candidates) {
        Matcher matcher = FLIGHT_WITH_CARRIER_PATTERN.matcher(value);
        while (matcher.find()) {
            String carrier = matcher.group(1).replace(" ", "").replace("-", "").toUpperCase(Locale.US);
            int baseScore = carrier.startsWith("C") ? 120 : 105;
            addFlightCandidate(candidates, value, matcher.group(2), matcher.start(), matcher.end(), baseScore);
        }
    }

    private static void collectAirChinaNumberCandidates(String value, List<FlightCandidate> candidates) {
        Matcher matcher = AIR_CHINA_NUMBER_PATTERN.matcher(value);
        while (matcher.find()) {
            addFlightCandidate(candidates, value, matcher.group(2), matcher.start(), matcher.end(), 100);
        }
    }

    private static void addFlightCandidate(List<FlightCandidate> candidates, String value, String rawDigits, int start, int end, int baseScore) {
        String digits = normalizeFlightDigits(rawDigits);
        if (digits.length() < 3 || digits.length() > 4 || safeInt(digits, 0) <= 0) {
            return;
        }

        int score = baseScore + scoreKeywordDistance(value, start, end);
        if (digits.length() == 4) {
            score += 5;
        }
        candidates.add(new FlightCandidate("CA" + digits, score, start));
    }

    private static String normalizeFlightDigits(String rawDigits) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < rawDigits.length(); i++) {
            char ch = rawDigits.charAt(i);
            if (ch >= '0' && ch <= '9') {
                builder.append(ch);
                continue;
            }

            switch (Character.toUpperCase(ch)) {
                case 'O':
                    builder.append('0');
                    break;
                case 'I':
                case 'L':
                    builder.append('1');
                    break;
                default:
                    break;
            }
        }
        return builder.toString();
    }

    private static int scoreKeywordDistance(String value, int start, int end) {
        int score = 0;
        String upper = value.toUpperCase(Locale.US);
        for (String keyword : FLIGHT_SCORE_KEYWORDS) {
            String upperKeyword = keyword.toUpperCase(Locale.US);
            int from = 0;
            int index;
            while ((index = upper.indexOf(upperKeyword, from)) >= 0) {
                int distance = distanceBetween(start, end, index, index + keyword.length());
                if (distance <= 36) {
                    score += 40 - distance;
                }
                from = index + upperKeyword.length();
            }
        }
        return score;
    }

    private static int distanceBetween(int leftStart, int leftEnd, int rightStart, int rightEnd) {
        if (leftEnd < rightStart) {
            return rightStart - leftEnd;
        }
        if (rightEnd < leftStart) {
            return leftStart - rightEnd;
        }
        return 0;
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

    private static final class FlightCandidate {
        final String flightNumber;
        final int score;
        final int start;

        FlightCandidate(String flightNumber, int score, int start) {
            this.flightNumber = flightNumber;
            this.score = score;
            this.start = start;
        }
    }
}
