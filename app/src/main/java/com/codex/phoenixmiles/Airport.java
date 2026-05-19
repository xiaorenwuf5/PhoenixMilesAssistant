package com.codex.phoenixmiles;

final class Airport {
    final String code;
    final String city;
    final String name;
    final double latitude;
    final double longitude;

    Airport(String code, String city, String name, double latitude, double longitude) {
        this.code = code;
        this.city = city;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    String displayName() {
        return city + " " + name + " (" + code + ")";
    }
}
