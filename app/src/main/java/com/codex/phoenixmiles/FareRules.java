package com.codex.phoenixmiles;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class FareRules {
    private static final Map<Character, FareRule> RULES = new HashMap<>();

    static {
        add('F', "头等舱", 3.00, 3.00);
        add('A', "头等舱", 3.00, 3.00);
        add('J', "公务舱", 2.00, 2.00);
        add('C', "公务舱", 2.00, 2.00);
        add('D', "公务舱", 2.00, 2.00);
        add('Z', "公务舱", 1.25, 1.25);
        add('R', "公务舱", 1.25, 1.25);
        add('G', "超级经济舱", 1.10, 1.00);
        add('E', "超级经济舱", 1.00, 1.00);
        add('Y', "经济舱", 1.10, 1.00);
        add('B', "经济舱", 1.00, 1.00);
        add('M', "经济舱", 1.00, 1.00);
        add('U', "经济舱", 1.00, 1.00);
        add('H', "经济舱", 1.00, 1.00);
        add('Q', "经济舱", 1.00, 1.00);
        add('V', "经济舱", 1.00, 1.00);
        add('W', "经济舱", 0.50, 0.50);
        add('S', "经济舱", 0.50, 0.50);
        add('T', "经济舱", 0.50, 0.50);
        add('L', "经济舱", 0.25, 0.25);
        add('K', "经济舱", 0.25, 0.25);
        add('P', "经济舱", 0.25, 0.25);
    }

    private FareRules() {
    }

    static FareRule find(String bookingClass) {
        if (bookingClass == null || bookingClass.trim().isEmpty()) {
            return null;
        }
        char code = bookingClass.trim().toUpperCase(Locale.US).charAt(0);
        return RULES.get(code);
    }

    private static void add(char bookingClass, String cabin, double rate, double statusSegments) {
        RULES.put(bookingClass, new FareRule(bookingClass, cabin, rate, statusSegments));
    }
}
