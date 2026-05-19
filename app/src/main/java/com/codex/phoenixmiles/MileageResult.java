package com.codex.phoenixmiles;

final class MileageResult {
    final FlightInput input;
    final FareRule fareRule;
    final Airport origin;
    final Airport destination;
    final int distanceKm;
    final int accrualBaseKm;
    final int baseAvailableMiles;
    final int extraNonStatusMiles;
    final int totalAvailableMiles;
    final int statusMiles;
    final double baseStatusSegments;
    final FastLineBonus fastLineBonus;
    final double totalStatusSegments;
    final String error;

    MileageResult(
            FlightInput input,
            FareRule fareRule,
            Airport origin,
            Airport destination,
            int distanceKm,
            int accrualBaseKm,
            int baseAvailableMiles,
            int extraNonStatusMiles,
            int totalAvailableMiles,
            int statusMiles,
            double baseStatusSegments,
            FastLineBonus fastLineBonus,
            double totalStatusSegments,
            String error
    ) {
        this.input = input;
        this.fareRule = fareRule;
        this.origin = origin;
        this.destination = destination;
        this.distanceKm = distanceKm;
        this.accrualBaseKm = accrualBaseKm;
        this.baseAvailableMiles = baseAvailableMiles;
        this.extraNonStatusMiles = extraNonStatusMiles;
        this.totalAvailableMiles = totalAvailableMiles;
        this.statusMiles = statusMiles;
        this.baseStatusSegments = baseStatusSegments;
        this.fastLineBonus = fastLineBonus;
        this.totalStatusSegments = totalStatusSegments;
        this.error = error;
    }

    static MileageResult error(String message) {
        return new MileageResult(null, null, null, null, 0, 0, 0, 0, 0, 0, 0, FastLineBonus.none(), 0, message);
    }

    boolean isError() {
        return error != null;
    }
}
