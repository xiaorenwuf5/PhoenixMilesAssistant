package com.codex.phoenixmiles;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class OfficialMileageResult {
    final boolean success;
    final String message;
    final List<Row> rows;
    final Row matchedRow;

    OfficialMileageResult(boolean success, String message, List<Row> rows, Row matchedRow) {
        this.success = success;
        this.message = message;
        this.rows = rows;
        this.matchedRow = matchedRow;
    }

    boolean isRouteMismatch() {
        return message != null && message.contains("起降地") && message.contains("不一致");
    }

    static OfficialMileageResult fromJson(String json, String bookingClass) throws JSONException {
        JSONObject object = new JSONObject(json);
        boolean success = object.optBoolean("success") || object.optInt("status") == 1;
        String message = object.optString("message", "");
        JSONArray body = object.optJSONArray("body");
        List<Row> rows = new ArrayList<>();
        Row matched = null;
        String cabin = bookingClass == null ? "" : bookingClass.trim().toUpperCase(Locale.US);

        if (body != null) {
            for (int i = 0; i < body.length(); i++) {
                JSONObject item = body.getJSONObject(i);
                Row row = new Row(
                        item.optString("subClassName", ""),
                        item.optString("gradingMilageRate", ""),
                        item.optString("availableMileage", ""),
                        item.optString("gradingMileage", ""),
                        item.optString("gradingSeq", "")
                );
                rows.add(row);
                if (matched == null && !cabin.isEmpty() && row.containsCabin(cabin.charAt(0))) {
                    matched = row;
                }
            }
        }
        return new OfficialMileageResult(success, message, rows, matched);
    }

    static final class Row {
        final String subClassName;
        final String mileageRate;
        final String availableMileage;
        final String gradingMileage;
        final String gradingSeq;

        Row(String subClassName, String mileageRate, String availableMileage, String gradingMileage, String gradingSeq) {
            this.subClassName = subClassName;
            this.mileageRate = mileageRate;
            this.availableMileage = availableMileage;
            this.gradingMileage = gradingMileage;
            this.gradingSeq = gradingSeq;
        }

        boolean containsCabin(char bookingClass) {
            String cabin = String.valueOf(Character.toUpperCase(bookingClass));
            String normalized = "/" + subClassName.toUpperCase(Locale.US).replace("、", "/") + "/";
            return normalized.contains("/" + cabin + "/");
        }
    }
}
