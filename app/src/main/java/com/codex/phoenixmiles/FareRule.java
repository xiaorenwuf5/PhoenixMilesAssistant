package com.codex.phoenixmiles;

final class FareRule {
    final char bookingClass;
    final String cabin;
    final double accrualRate;
    final double statusSegments;

    FareRule(char bookingClass, String cabin, double accrualRate, double statusSegments) {
        this.bookingClass = bookingClass;
        this.cabin = cabin;
        this.accrualRate = accrualRate;
        this.statusSegments = statusSegments;
    }

    String percentText() {
        return Math.round(accrualRate * 100) + "%";
    }
}
