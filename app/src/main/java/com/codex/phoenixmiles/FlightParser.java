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
    private static final String OCR_FLIGHT_TAIL = "(?![0-9OIL])";
    private static final String OCR_DATE_DIGIT = "[0-9OILS]";
    private static final String OCR_DATE_NUMBER = OCR_DATE_DIGIT + "(?:\\s*" + OCR_DATE_DIGIT + ")?";
    private static final String OCR_DATE_SEPARATOR = "[\\p{Pd}\\u2212\\uFF0D\\uFE63一年月/.]";
    private static final Pattern FLIGHT_WITH_CARRIER_PATTERN = Pattern.compile("(?<![A-Z0-9])([C0O]\\s*[- ]?\\s*A)\\s*[- ]?\\s*(" + OCR_FLIGHT_DIGITS + ")" + OCR_FLIGHT_TAIL, Pattern.CASE_INSENSITIVE);
    private static final Pattern AIR_CHINA_NUMBER_PATTERN = Pattern.compile("((?:中\\s*国\\s*)?国\\s*航|航\\s*班\\s*号|航\\s*班)\\s*[:：#号\\- ]?\\s*(" + OCR_FLIGHT_DIGITS + ")" + OCR_FLIGHT_TAIL, Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_PATTERN = Pattern.compile("(?<![0-9OILS])(20\\s*" + OCR_DATE_DIGIT + "\\s*" + OCR_DATE_DIGIT + ")\\s*" + OCR_DATE_SEPARATOR + "+\\s*(" + OCR_DATE_NUMBER + ")\\s*" + OCR_DATE_SEPARATOR + "+\\s*(" + OCR_DATE_NUMBER + ")(?![0-9OILS])", Pattern.CASE_INSENSITIVE);
    private static final Pattern SHORT_DATE_PATTERN = Pattern.compile("(?<![0-9OILS])(" + OCR_DATE_NUMBER + ")\\s*" + OCR_DATE_SEPARATOR + "+\\s*(" + OCR_DATE_NUMBER + ")(?![0-9OILS])", Pattern.CASE_INSENSITIVE);
    private static final Pattern CABIN_PAREN_PATTERN = Pattern.compile("(经济舱|超级经济舱|公务舱|头等舱|舱等)?\\s*[（(]\\s*([A-Z])\\s*[0-9OIL]?\\s*[）)]", Pattern.CASE_INSENSITIVE);
    private static final Pattern CABIN_TEXT_PATTERN = Pattern.compile("舱等\\s*[:：]?\\s*([A-Z])|([A-Z])\\s*舱", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXTRA_MILE_PATTERN = Pattern.compile("额外\\s*([0-9]{2,5})\\s*里程");
    private static final Pattern ROUTE_TIME_PATTERN = Pattern.compile("(?<![0-9])(?:[01]?\\d|2[0-3])\\s*[:：]\\s*[0-5]\\d(?![0-9])");
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

        String routeHint = routeHintText(value, input.flightNumber);
        applyAirports(input, routeAirports(value));
        if (input.originCode.isEmpty() || input.destinationCode.isEmpty()) {
            applyMissingAirports(input, AirportCatalog.findAllIn(routeHint));
        }
        if (input.originCode.isEmpty() || input.destinationCode.isEmpty()) {
            applyMissingAirports(input, AirportCatalog.findCityDefaultsIn(routeHint));
        }
        if (input.originCode.isEmpty() || input.destinationCode.isEmpty()) {
            applyMissingAirports(input, AirportCatalog.findAllIn(value));
        }

        input.bookingClass = parseCabin(value);

        Matcher extraMatcher = EXTRA_MILE_PATTERN.matcher(value.replace(" ", ""));
        if (extraMatcher.find()) {
            input.extraNonStatusMiles = safeInt(extraMatcher.group(1), 0);
        }

        return input;
    }

    private static void applyAirports(FlightInput input, List<Airport> airports) {
        if (!airports.isEmpty()) {
            input.originCode = airports.get(0).code;
        }
        if (airports.size() > 1) {
            input.destinationCode = airports.get(1).code;
        }
    }

    private static void applyMissingAirports(FlightInput input, List<Airport> airports) {
        for (Airport airport : airports) {
            if (airport == null) {
                continue;
            }
            if (input.originCode.isEmpty()) {
                if (!airport.code.equals(input.destinationCode)) {
                    input.originCode = airport.code;
                }
                continue;
            }
            if (input.destinationCode.isEmpty() && !input.originCode.equals(airport.code)) {
                input.destinationCode = airport.code;
                return;
            }
        }
    }

    private static List<Airport> routeAirports(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String[] lines = value.split("\\R");
        List<Airport> result = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            addRouteAirportsFromLine(result, lines, i);
        }
        return result;
    }

    private static void addRouteAirportsFromLine(List<Airport> result, String[] lines, int index) {
        String line = lines[index];
        StringBuilder candidate = new StringBuilder(line);
        if (ROUTE_TIME_PATTERN.matcher(line).find() && index + 1 < lines.length) {
            candidate.append('\n').append(lines[index + 1]);
        } else if (index > 0 && ROUTE_TIME_PATTERN.matcher(lines[index - 1]).find()) {
            candidate.insert(0, lines[index - 1] + "\n");
        }

        if (!ROUTE_TIME_PATTERN.matcher(candidate).find()) {
            return;
        }

        List<Airport> airports = AirportCatalog.findAllIn(candidate.toString());
        if (airports.isEmpty()) {
            airports = AirportCatalog.findAirportNamesIn(candidate.toString());
        }
        for (Airport airport : airports) {
            addUniqueAirport(result, airport);
        }
    }

    private static void addUniqueAirport(List<Airport> airports, Airport airport) {
        if (airport == null) {
            return;
        }
        for (Airport item : airports) {
            if (item.code.equals(airport.code)) {
                return;
            }
        }
        airports.add(airport);
    }

    private static String routeHintText(String value, String flightNumber) {
        String[] lines = value.split("\\R");
        String normalizedFlightNumber = flightNumber == null ? "" : compactOcrText(flightNumber).toUpperCase(Locale.US);
        String flightDigits = normalizedFlightNumber.startsWith("CA") ? normalizedFlightNumber.substring(2) : normalizedFlightNumber;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String compactLine = compactOcrText(lines[i]).toUpperCase(Locale.US);
            boolean containsFlight = !normalizedFlightNumber.isEmpty() && compactLine.contains(normalizedFlightNumber);
            boolean containsFlightDigits = !flightDigits.isEmpty() && compactLine.contains("CA" + flightDigits);
            if (!containsFlight && !containsFlightDigits) {
                continue;
            }

            appendRouteLine(builder, lines[i]);
            if (i + 1 < lines.length) {
                appendRouteLine(builder, lines[i + 1]);
            }
        }
        if (builder.length() > 0) {
            return builder.toString();
        }

        int subjectIndex = value.indexOf("主题");
        if (subjectIndex >= 0) {
            return value.substring(subjectIndex, Math.min(value.length(), subjectIndex + 80));
        }
        return "";
    }

    private static void appendRouteLine(StringBuilder builder, String line) {
        if (builder.length() > 0) {
            builder.append('\n');
        }
        builder.append(line);
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
        LocalDate date = parseDateFromText(value);
        if (date != null) {
            return date;
        }
        return parseDateFromText(compactOcrText(value));
    }

    private static LocalDate parseDateFromText(String value) {
        Matcher fullMatcher = DATE_PATTERN.matcher(value);
        while (fullMatcher.find()) {
            int year = safeDateInt(fullMatcher.group(1), 0);
            int month = safeDateInt(fullMatcher.group(2), 0);
            int day = safeDateInt(fullMatcher.group(3), 0);
            LocalDate date = safeDate(year, month, day);
            if (date != null) {
                return date;
            }
        }

        List<DateCandidate> candidates = new ArrayList<>();
        Matcher shortMatcher = SHORT_DATE_PATTERN.matcher(value);
        while (shortMatcher.find()) {
            int month = safeDateInt(shortMatcher.group(1), 0);
            int day = safeDateInt(shortMatcher.group(2), 0);
            LocalDate today = LocalDate.now();
            LocalDate date = safeDate(today.getYear(), month, day);
            if (date != null && date.isBefore(today.minusDays(90))) {
                date = safeDate(today.getYear() + 1, month, day);
            }
            int score = dateScore(value, shortMatcher.start(), shortMatcher.end());
            if (date != null && score > 0) {
                candidates.add(new DateCandidate(date, score, shortMatcher.start()));
            }
        }

        if (!candidates.isEmpty()) {
            Collections.sort(candidates, (left, right) -> {
                if (left.score != right.score) {
                    return Integer.compare(right.score, left.score);
                }
                return Integer.compare(left.start, right.start);
            });
            return candidates.get(0).date;
        }
        return null;
    }

    private static int dateScore(String value, int start, int end) {
        String before = compactOcrText(value.substring(Math.max(0, start - 10), start));
        String after = compactOcrText(value.substring(end, Math.min(value.length(), end + 18)));
        String widerAfter = compactOcrText(value.substring(end, Math.min(value.length(), end + 32)));
        int score = 80;
        if (after.contains("周") || after.contains("星期")) {
            score += 100;
        }
        if (widerAfter.contains("共") && (widerAfter.contains("小时") || widerAfter.contains("分钟"))) {
            score += 60;
        }
        if (widerAfter.contains("机场") || widerAfter.contains("航班")) {
            score += 20;
        }
        if (after.startsWith("折") || (before.contains("经济舱") && after.contains("折"))) {
            score -= 220;
        }
        if (after.contains("退改")
                || after.contains("托运")
                || after.contains("¥")
                || after.contains("￥")) {
            score -= 100;
        }
        return score;
    }

    private static String parseCabin(String value) {
        List<CabinCandidate> candidates = new ArrayList<>();

        Matcher parenMatcher = CABIN_PAREN_PATTERN.matcher(value);
        while (parenMatcher.find()) {
            String cabin = parenMatcher.group(2).toUpperCase(Locale.US);
            if (FareRules.find(cabin) != null) {
                candidates.add(new CabinCandidate(cabin, cabinScore(value, parenMatcher.start(), parenMatcher.end(), 80), parenMatcher.start()));
            }
        }

        Matcher textMatcher = CABIN_TEXT_PATTERN.matcher(value);
        while (textMatcher.find()) {
            String cabin = textMatcher.group(1) != null ? textMatcher.group(1) : textMatcher.group(2);
            if (cabin != null && FareRules.find(cabin) != null) {
                candidates.add(new CabinCandidate(cabin.toUpperCase(Locale.US), cabinScore(value, textMatcher.start(), textMatcher.end(), 50), textMatcher.start()));
            }
        }
        if (!candidates.isEmpty()) {
            Collections.sort(candidates, (left, right) -> {
                if (left.score != right.score) {
                    return Integer.compare(right.score, left.score);
                }
                return Integer.compare(left.start, right.start);
            });
            return candidates.get(0).cabin;
        }
        return "";
    }

    private static int cabinScore(String value, int start, int end, int baseScore) {
        int windowStart = Math.max(0, start - 32);
        int windowEnd = Math.min(value.length(), end + 4);
        String context = value.substring(windowStart, windowEnd);
        String compactContext = compactOcrText(context);
        int lineStart = value.lastIndexOf('\n', start);
        int lineEnd = value.indexOf('\n', end);
        if (lineStart < 0) {
            lineStart = 0;
        } else {
            lineStart += 1;
        }
        if (lineEnd < 0) {
            lineEnd = value.length();
        }
        String lineContext = value.substring(lineStart, lineEnd);
        String compactLineContext = compactOcrText(lineContext);
        String beforeContext = compactOcrText(value.substring(Math.max(0, start - 28), start));
        int score = baseScore;
        if (beforeContext.contains("成人")) {
            score += 120;
        } else if (compactLineContext.contains("成人") && !compactLineContext.contains("儿童")) {
            score += 60;
        }
        if (beforeContext.contains("儿童") || beforeContext.contains("婴儿")) {
            score -= 120;
        }
        if (beforeContext.contains("全价")) {
            score -= 80;
        } else if (compactLineContext.length() <= 48 && compactLineContext.contains("全价")) {
            score -= 30;
        }
        if (compactContext.contains("经济舱") || compactContext.contains("舱等") || compactContext.contains("舱位")) {
            score += 20;
        }
        if (compactContext.contains("票面价") || compactContext.contains("机票详情")) {
            score += 10;
        }
        return score;
    }

    private static String compactOcrText(String value) {
        return value == null ? "" : value.replaceAll("[\\s\\u00A0\\u2007\\u202F\\u200B\\u200C\\u200D]+", "");
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

    private static int safeDateInt(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        String normalized = value.toUpperCase(Locale.US)
                .replace('O', '0')
                .replace('I', '1')
                .replace('L', '1')
                .replace('S', '5')
                .replaceAll("[^0-9]", "");
        if (normalized.isEmpty()) {
            return fallback;
        }
        return safeInt(normalized, fallback);
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

    private static final class CabinCandidate {
        final String cabin;
        final int score;
        final int start;

        CabinCandidate(String cabin, int score, int start) {
            this.cabin = cabin;
            this.score = score;
            this.start = start;
        }
    }

    private static final class DateCandidate {
        final LocalDate date;
        final int score;
        final int start;

        DateCandidate(LocalDate date, int score, int start) {
            this.date = date;
            this.score = score;
            this.start = start;
        }
    }
}
