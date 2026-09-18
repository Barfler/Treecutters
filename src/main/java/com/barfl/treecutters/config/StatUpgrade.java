package com.barfl.treecutters.config;

import java.util.List;

public final class StatUpgrade {
    public final String key;
    public final String material;
    public final String stat;
    public final double basePrice;
    public final double amount;
    public final double priceScaling;
    public final int prestigeEvery;
    public final List<Double> polynomialPrice;
    public final double maxLevel;
    public final List<Double> priceBrackets;
    public final int levelReq;

    public StatUpgrade(String key, String material, String stat, double basePrice, double amount,
                        double priceScaling, int prestigeEvery, List<Double> polynomialPrice, double maxLevel,
                        List<Double> priceBrackets, int levelReq) {
        this.key = key;
        this.material = material;
        this.stat = stat;
        this.basePrice = basePrice;
        this.amount = amount;
        this.priceScaling = priceScaling;
        this.prestigeEvery = prestigeEvery;
        this.polynomialPrice = polynomialPrice;
        this.maxLevel = maxLevel;
        this.priceBrackets = priceBrackets;
        this.levelReq = levelReq;
    }

    public static Builder builder(String key) {
        return new Builder(key);
    }

    public static final class Builder {
        private final String key;
        private String material;
        private String stat;
        private double basePrice = 0;
        private double amount = 1;
        private double priceScaling = 1.15;
        private int prestigeEvery = -1;
        private List<Double> polynomialPrice = List.of();
        private double maxLevel = 50;
        private List<Double> priceBrackets = List.of();
        private int levelReq = 0;

        private Builder(String key) {
            this.key = key;
        }

        public Builder material(String v) { this.material = v; return this; }
        public Builder stat(String v) { this.stat = v; return this; }
        public Builder basePrice(double v) { this.basePrice = v; return this; }
        public Builder amount(double v) { this.amount = v; return this; }
        public Builder priceScaling(double v) { this.priceScaling = v; return this; }
        public Builder prestigeEvery(int v) { this.prestigeEvery = v; return this; }
        public Builder polynomialPrice(Double... v) { this.polynomialPrice = List.of(v); return this; }
        public Builder maxLevel(double v) { this.maxLevel = v; return this; }
        public Builder priceBrackets(double... v) {
            Double[] boxed = new Double[v.length];
            for (int i = 0; i < v.length; i++) boxed[i] = v[i];
            this.priceBrackets = List.of(boxed);
            return this;
        }
        public Builder levelReq(int v) { this.levelReq = v; return this; }

        public StatUpgrade build() {
            return new StatUpgrade(key, material, stat, basePrice, amount, priceScaling, prestigeEvery,
                    polynomialPrice, maxLevel, priceBrackets, levelReq);
        }
    }
}
