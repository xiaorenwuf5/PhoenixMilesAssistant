package com.codex.phoenixmiles;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class OfficialMileageClient {
    private static final String CALCULATOR_URL = "https://ffp.airchina.com.cn/plan/mileage_accumulate_calculator.html";
    private static final String API_URL = "https://ffp.airchina.com.cn/apigateway/user/jsonp/mileageCumulateCalculation";
    private static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 15) PhoenixMilesAssistant/0.2";

    private OfficialMileageClient() {
    }

    static OfficialMileageResult query(FlightInput input, String memberGrade) throws Exception {
        String cookies = fetchSessionCookies();

        JSONObject payload = new JSONObject();
        payload.put("org", input.originCode);
        payload.put("des", input.destinationCode);
        payload.put("flightDate", input.dateText());
        payload.put("flightNo", input.normalizedFlightNumber());
        payload.put("memberGrade", memberGrade == null || memberGrade.isEmpty() ? "Normal" : memberGrade);
        if (input.bookingClass != null && !input.bookingClass.trim().isEmpty()) {
            payload.put("cabinCode", input.bookingClass.trim().toUpperCase());
        }

        String response = postForm(API_URL, cookies, "data=" + URLEncoder.encode(payload.toString(), "UTF-8"));
        return OfficialMileageResult.fromJson(response, input.bookingClass);
    }

    private static String fetchSessionCookies() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(CALCULATOR_URL).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        connection.setConnectTimeout(12000);
        connection.setReadTimeout(12000);
        try {
            readFully(connection);
            return collectCookies(connection.getHeaderFields());
        } finally {
            connection.disconnect();
        }
    }

    private static String postForm(String url, String cookies, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setConnectTimeout(12000);
        connection.setReadTimeout(12000);
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        connection.setRequestProperty("Origin", "https://ffp.airchina.com.cn");
        connection.setRequestProperty("Referer", CALCULATOR_URL);
        if (!cookies.isEmpty()) {
            connection.setRequestProperty("Cookie", cookies);
        }
        connection.setFixedLengthStreamingMode(bytes.length);
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(bytes);
        }
        try {
            return readFully(connection);
        } finally {
            connection.disconnect();
        }
    }

    private static String collectCookies(Map<String, List<String>> headers) {
        List<String> cookies = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() == null || !"Set-Cookie".equalsIgnoreCase(entry.getKey())) {
                continue;
            }
            for (String header : entry.getValue()) {
                for (String part : header.split(",(?=[^;,]+=)")) {
                    String pair = part.trim().split(";", 2)[0].trim();
                    if (!pair.isEmpty()) {
                        cookies.add(pair);
                    }
                }
            }
        }
        return String.join("; ", cookies);
    }

    private static String readFully(HttpURLConnection connection) throws IOException {
        InputStream stream = connection.getResponseCode() >= 400
                ? connection.getErrorStream()
                : connection.getInputStream();
        if (stream == null) {
            throw new IOException("国航接口无响应，HTTP " + connection.getResponseCode());
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }
}
