package com.codex.phoenixmiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class AirportCatalog {
    private static final Map<String, Airport> AIRPORTS = new HashMap<>();
    private static final Map<String, String> ALIASES = new HashMap<>();

    static {
        add(new Airport("PEK", "北京", "首都", 40.0799, 116.6031),
                "北京首都", "首都机场", "首都国际机场", "首都国际机场T3", "首都国际机场T2", "PEK");
        add(new Airport("PKX", "北京", "大兴", 39.5098, 116.4105),
                "北京大兴", "大兴机场", "大兴国际机场", "PKX");
        add(new Airport("CKG", "重庆", "江北", 29.7192, 106.6417),
                "重庆江北", "江北机场", "江北国际机场", "江北国际机场T3", "江北国际机场T2", "CKG");
        add(new Airport("SHA", "上海", "虹桥", 31.1979, 121.3363),
                "上海虹桥", "虹桥机场", "虹桥国际机场", "SHA");
        add(new Airport("PVG", "上海", "浦东", 31.1443, 121.8083),
                "上海浦东", "浦东机场", "浦东国际机场", "PVG");
        add(new Airport("CAN", "广州", "白云", 23.3924, 113.2988),
                "广州白云", "白云机场", "白云国际机场", "CAN");
        add(new Airport("SZX", "深圳", "宝安", 22.6393, 113.8107),
                "深圳宝安", "宝安机场", "宝安国际机场", "SZX");
        add(new Airport("CTU", "成都", "双流", 30.5785, 103.9471),
                "成都双流", "双流机场", "双流国际机场", "CTU");
        add(new Airport("TFU", "成都", "天府", 30.3125, 104.4410),
                "成都天府", "天府机场", "天府国际机场", "TFU");
        add(new Airport("HGH", "杭州", "萧山", 30.2369, 120.4324),
                "杭州萧山", "萧山机场", "萧山国际机场", "HGH");
        add(new Airport("XMN", "厦门", "高崎", 24.5440, 118.1277),
                "厦门高崎", "高崎机场", "高崎国际机场", "XMN");
        add(new Airport("WUH", "武汉", "天河", 30.7838, 114.2081),
                "武汉天河", "天河机场", "天河国际机场", "WUH");
        add(new Airport("URC", "乌鲁木齐", "地窝堡", 43.9071, 87.4742),
                "乌鲁木齐地窝堡", "地窝堡机场", "地窝堡国际机场", "URC");
        add(new Airport("XIY", "西安", "咸阳", 34.4471, 108.7516),
                "西安咸阳", "咸阳机场", "咸阳国际机场", "XIY");
        add(new Airport("TSN", "天津", "滨海", 39.1244, 117.3462),
                "天津滨海", "滨海机场", "滨海国际机场", "TSN");
        add(new Airport("NKG", "南京", "禄口", 31.7420, 118.8620),
                "南京禄口", "禄口机场", "禄口国际机场", "NKG");
        add(new Airport("TAO", "青岛", "胶东", 36.3619, 120.0882),
                "青岛胶东", "胶东机场", "胶东国际机场", "TAO");
        add(new Airport("CSX", "长沙", "黄花", 28.1892, 113.2196),
                "长沙黄花", "黄花机场", "黄花国际机场", "CSX");
        add(new Airport("KMG", "昆明", "长水", 25.1019, 102.9292),
                "昆明长水", "长水机场", "长水国际机场", "KMG");
        add(new Airport("TAO", "青岛", "胶东", 36.3619, 120.0882),
                "青岛流亭", "流亭机场");
    }

    private AirportCatalog() {
    }

    static Airport byCode(String code) {
        if (code == null) {
            return null;
        }
        return AIRPORTS.get(code.trim().toUpperCase(Locale.US));
    }

    static Airport findFirstIn(String text) {
        List<Airport> airports = findAllIn(text);
        return airports.isEmpty() ? null : airports.get(0);
    }

    static List<Airport> findAllIn(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String normalized = normalize(text);
        List<Hit> hits = new ArrayList<>();
        for (Map.Entry<String, String> entry : ALIASES.entrySet()) {
            int index = normalized.indexOf(entry.getKey());
            if (index >= 0) {
                hits.add(new Hit(index, entry.getKey().length(), AIRPORTS.get(entry.getValue())));
            }
        }

        Collections.sort(hits, (left, right) -> {
            if (left.index != right.index) {
                return Integer.compare(left.index, right.index);
            }
            return Integer.compare(right.length, left.length);
        });

        List<Airport> result = new ArrayList<>();
        for (Hit hit : hits) {
            if (hit.airport == null || containsCode(result, hit.airport.code)) {
                continue;
            }
            result.add(hit.airport);
        }
        return result;
    }

    static String normalize(String value) {
        return value == null ? "" : value
                .toUpperCase(Locale.US)
                .replace(" ", "")
                .replace("\n", "")
                .replace("\r", "")
                .replace("T1", "")
                .replace("T2", "")
                .replace("T3", "")
                .replace("航站楼", "")
                .replace("机场", "机场");
    }

    private static void add(Airport airport, String... aliases) {
        AIRPORTS.put(airport.code, airport);
        ALIASES.put(airport.code, airport.code);
        ALIASES.put(normalize(airport.city + airport.name), airport.code);
        ALIASES.put(normalize(airport.name + "机场"), airport.code);
        for (String alias : aliases) {
            ALIASES.put(normalize(alias), airport.code);
        }
    }

    private static boolean containsCode(List<Airport> airports, String code) {
        for (Airport airport : airports) {
            if (airport.code.equals(code)) {
                return true;
            }
        }
        return false;
    }

    private static final class Hit {
        final int index;
        final int length;
        final Airport airport;

        Hit(int index, int length, Airport airport) {
            this.index = index;
            this.length = length;
            this.airport = airport;
        }
    }
}
