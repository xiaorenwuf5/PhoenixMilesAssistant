package com.codex.phoenixmiles;

import java.util.Locale;

final class ResultFormatter {
    private ResultFormatter() {
    }

    static String format(MileageResult result) {
        if (result == null) {
            return "还没有结果。";
        }
        if (result.isError()) {
            return result.error;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(result.input.normalizedFlightNumber())
                .append("  ")
                .append(result.input.dateText())
                .append("\n");
        builder.append(result.origin.displayName())
                .append(" -> ")
                .append(result.destination.displayName())
                .append("\n\n");
        builder.append("累计舱位：")
                .append(result.fareRule.cabin)
                .append(" ")
                .append(result.fareRule.bookingClass)
                .append(" 舱，基础累积率 ")
                .append(result.fareRule.percentText())
                .append("\n");
        builder.append("基础可用里程：")
                .append(result.baseAvailableMiles)
                .append("\n");
        if (result.extraNonStatusMiles > 0) {
            builder.append("产品额外里程：")
                    .append(result.extraNonStatusMiles)
                    .append("（非定级）\n");
            builder.append("预计可用里程合计：")
                    .append(result.totalAvailableMiles)
                    .append("\n");
        }
        builder.append("定级里程：")
                .append(result.statusMiles)
                .append("\n");
        builder.append("基础定级航段：")
                .append(MileageCalculator.formatSegments(result.baseStatusSegments))
                .append("\n");

        if (result.fastLineBonus.applies) {
            builder.append("国航快线额外航段：")
                    .append(MileageCalculator.formatSegments(result.fastLineBonus.bonusSegments))
                    .append("（")
                    .append(String.format(Locale.CHINA, "%.0f%%", result.fastLineBonus.bonusRate * 100))
                    .append("）\n");
            builder.append("预计定级航段合计：")
                    .append(MileageCalculator.formatSegments(result.totalStatusSegments))
                    .append("\n");
        } else {
            builder.append("预计定级航段合计：")
                    .append(MileageCalculator.formatSegments(result.totalStatusSegments))
                    .append("\n");
        }

        builder.append("\n航距估算：")
                .append(result.distanceKm)
                .append(" 公里");
        if (result.accrualBaseKm > result.distanceKm) {
            builder.append("，按最低 ")
                    .append(result.accrualBaseKm)
                    .append(" 公里参与计算");
        }
        builder.append("\n");
        builder.append("提示：本地用机场坐标估算航距，官方入账以 IATA 航距、实际承运、旅行日规则为准。");
        return builder.toString();
    }
}
