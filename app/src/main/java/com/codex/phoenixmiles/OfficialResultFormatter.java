package com.codex.phoenixmiles;

final class OfficialResultFormatter {
    private OfficialResultFormatter() {
    }

    static String format(FlightInput input, OfficialMileageResult result) {
        return format(input, result, "");
    }

    static String format(FlightInput input, OfficialMileageResult result, String memberTierLabel) {
        if (result == null) {
            return "国航官方查询没有返回结果。";
        }
        if (!result.success) {
            String message = result.message == null || result.message.isEmpty() ? "未知错误" : result.message;
            if (result.isRouteMismatch()) {
                return "国航官方查询失败：航班号和出发/到达机场不匹配。\n"
                        + "请核对 OCR 识别的航班号、出发机场、到达机场，必要时点击截图放大核对。\n\n"
                        + "当前查询："
                        + input.normalizedFlightNumber()
                        + "  "
                        + input.dateText()
                        + "  "
                        + input.originCode
                        + " -> "
                        + input.destinationCode
                        + "\n"
                        + formatMemberTierLine(memberTierLabel)
                        + "官方返回："
                        + message;
            }
            return "国航官方查询失败：" + message;
        }
        if (result.matchedRow == null) {
            return "国航官方已返回结果，但没有找到 " + input.bookingClass + " 舱对应行。\n\n" + formatAllRows(result);
        }

        OfficialMileageResult.Row row = result.matchedRow;
        StringBuilder builder = new StringBuilder();
        builder.append("国航官方查询结果\n");
        builder.append(input.normalizedFlightNumber())
                .append("  ")
                .append(input.dateText())
                .append("  ")
                .append(input.originCode)
                .append(" -> ")
                .append(input.destinationCode)
                .append("\n\n");
        if (memberTierLabel != null && !memberTierLabel.trim().isEmpty()) {
            builder.append("会员级别：")
                    .append(memberTierLabel.trim())
                    .append("\n");
        }
        builder.append("匹配舱位：")
                .append(row.subClassName)
                .append("\n");
        builder.append("里程累积率：")
                .append(row.mileageRate)
                .append("\n");
        builder.append("累积可用里程数：")
                .append(row.availableMileage)
                .append("\n");
        builder.append("累积定级里程数：")
                .append(row.gradingMileage)
                .append("\n");
        builder.append("累积定级航段数：")
                .append(row.gradingSeq)
                .append("\n\n");
        builder.append("来源：国航凤凰知音里程累积计算器。结果仍以实际入账为准。");
        return builder.toString();
    }

    private static String formatMemberTierLine(String memberTierLabel) {
        if (memberTierLabel == null || memberTierLabel.trim().isEmpty()) {
            return "";
        }
        return "会员级别：" + memberTierLabel.trim() + "\n";
    }

    private static String formatAllRows(OfficialMileageResult result) {
        StringBuilder builder = new StringBuilder("官方返回全部舱位：\n");
        for (OfficialMileageResult.Row row : result.rows) {
            builder.append(row.subClassName)
                    .append("  ")
                    .append(row.mileageRate)
                    .append("  可用")
                    .append(row.availableMileage)
                    .append("  定级")
                    .append(row.gradingMileage)
                    .append("  航段")
                    .append(row.gradingSeq)
                    .append("\n");
        }
        return builder.toString().trim();
    }
}
