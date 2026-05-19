package com.codex.phoenixmiles;

final class FastLineBonus {
    final boolean applies;
    final double bonusRate;
    final double bonusSegments;

    FastLineBonus(boolean applies, double bonusRate, double bonusSegments) {
        this.applies = applies;
        this.bonusRate = bonusRate;
        this.bonusSegments = bonusSegments;
    }

    static FastLineBonus none() {
        return new FastLineBonus(false, 0, 0);
    }
}
