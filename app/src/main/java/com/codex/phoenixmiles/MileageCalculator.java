package com.codex.phoenixmiles;

import java.util.Locale;

final class MileageCalculator {
    private MileageCalculator() {
    }

    static MileageResult calculate(FlightInput input) {
        if (input == null) {
            return MileageResult.error("没有可计算的航班信息。");
        }

        FareRule fareRule = FareRules.find(input.bookingClass);
        if (fareRule == null) {
            return MileageResult.error("暂未收录 " + input.bookingClass + " 舱的国航累计规则，请用官方计算器核对。");
        }

        Airport origin = AirportCatalog.byCode(input.originCode);
        Airport destination = AirportCatalog.byCode(input.destinationCode);
        if (origin == null || destination == null) {
            return MileageResult.error("无法识别起降机场，请手工输入三字码，例如 PEK / CKG。");
        }
        if (origin.code.equals(destination.code)) {
            return MileageResult.error("起飞机场和到达机场相同，请检查识别结果。");
        }

        int distanceKm = greatCircleKm(origin, destination);
        int accrualBaseKm = Math.max(distanceKm, 500);
        int baseMiles = (int) Math.round(accrualBaseKm * fareRule.accrualRate);
        int totalAvailableMiles = baseMiles + Math.max(0, input.extraNonStatusMiles);
        FastLineBonus fastLineBonus = FastLineRules.calculate(input, fareRule);
        double totalSegments = fareRule.statusSegments + fastLineBonus.bonusSegments;

        return new MileageResult(
                input,
                fareRule,
                origin,
                destination,
                distanceKm,
                accrualBaseKm,
                baseMiles,
                Math.max(0, input.extraNonStatusMiles),
                totalAvailableMiles,
                baseMiles,
                fareRule.statusSegments,
                fastLineBonus,
                totalSegments,
                null
        );
    }

    private static int greatCircleKm(Airport origin, Airport destination) {
        double radiusKm = 6371.0;
        double dLat = Math.toRadians(destination.latitude - origin.latitude);
        double dLon = Math.toRadians(destination.longitude - origin.longitude);
        double lat1 = Math.toRadians(origin.latitude);
        double lat2 = Math.toRadians(destination.latitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (int) Math.round(radiusKm * c);
    }

    static String formatSegments(double value) {
        if (Math.abs(value - Math.round(value)) < 0.000001) {
            return String.format(Locale.CHINA, "%.0f", value);
        }
        return String.format(Locale.CHINA, "%.2f", value);
    }
}
