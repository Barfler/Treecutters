package com.barfl.treecutters.config;

import java.util.LinkedHashMap;
import java.util.Map;

public final class StatShopIcons {
    private StatShopIcons() {
    }

    public static final Map<String, StatUpgrade> ALL = new LinkedHashMap<>();

    private static void reg(StatUpgrade u) {
        ALL.put(u.key, u);
    }

    static {
        reg(StatUpgrade.builder("sweep")
                .material("iron_axe").stat("sweep")
                .basePrice(15).priceScaling(1.15).maxLevel(130)
                .build());

        reg(StatUpgrade.builder("logFortune")
                .material("spruce_sapling").stat("fortune")
                .amount(0.06).basePrice(15).priceScaling(1.9)
                .build());

        reg(StatUpgrade.builder("treeGrowthSpeed")
                .material("cherry_sapling").stat("tree_growth")
                .amount(1).basePrice(30).priceScaling(1.4).maxLevel(60).levelReq(6)
                .build());

        reg(StatUpgrade.builder("jumpHeight")
                .material("rabbit_foot").stat("jump_height")
                .amount(1).basePrice(100).priceScaling(1.5).maxLevel(4).levelReq(3)
                .build());

        reg(StatUpgrade.builder("treeType")
                .material("jungle_sapling").stat("tree_type")
                .prestigeEvery(5).amount(1)
                .priceBrackets(
                        43, 182, 482, 844,
                        1341, 449, 1588, 3455, 5890,
                        7941, 938, 5498, 13842, 23960,
                        15973, 4219, 27610, 65510, 143120,
                        200899, 9380, 62475, 122967, 273040,
                        290221, 24758, 135589, 309243, 577550,
                        151739, 36228, 176622, 366407, 743620,
                        497044, 97572, 486084, 1237604, 1652486,
                        2410494, 192148, 1239852, 1978089, 2450283,
                        3193494, 401102, 1950193, 4012904, 8401355,
                        10000000, 500000, 1000000, 3000000, 9000000,
                        12000000, 16000000, 22000000, 30000000, 40000000,
                        50000000, 60000000, 70000000, 80000000, 90000000,
                        100000000, 110000000, 120000000, 130000000, 140000000,
                        150000000, 160000000, 170000000, 180000000, 190000000,
                        200000000, 220000000, 240000000, 260000000, 280000000,
                        300000000, 330000000, 360000000, 390000000, 420000000,
                        500000000, 540000000, 580000000, 620000000, 640000000,
                        700000000, 750000000, 800000000, 850000000, 900000000,
                        900000000, 930000000, 960000000, 975000000, 990000000,
                        1_000_000_000,
                        1_200_000_000, 1_400_000_000, 1_600_000_000, 1_800_000_000,
                        2_000_000_000,
                        2_400_000_000L, 2_800_000_000L, 3_200_000_000L, 3_600_000_000L,
                        4_000_000_000L
                )
                .maxLevel(100_000_000)
                .build());

        reg(StatUpgrade.builder("speed")
                .material("iron_boots").stat("walk_speed")
                .amount(1).basePrice(100).priceScaling(1.25).maxLevel(10).levelReq(11)
                .build());

        reg(StatUpgrade.builder("abilityCooldown")
                .material("clock").stat("ability_cooldown")
                .amount(1).basePrice(300).priceScaling(3).maxLevel(5).levelReq(16)
                .build());

        reg(StatUpgrade.builder("swingRange")
                .material("iron_axe").stat("swing_range")
                .amount(0.5).basePrice(1000).priceScaling(4).maxLevel(10).levelReq(21)
                .build());

        reg(StatUpgrade.builder("turret")
                .material("dispenser").stat("turret")
                .amount(1).basePrice(40000).priceScaling(2).maxLevel(20).levelReq(26)
                .build());

        reg(StatUpgrade.builder("cutoffRate")
                .material("netherite_axe").stat("cutoff_rate")
                .amount(0.01).basePrice(50000).priceScaling(3).maxLevel(10).levelReq(31)
                .build());

        reg(StatUpgrade.builder("multishot")
                .material("crossbow").stat("multishot")
                .amount(1).basePrice(100000).priceScaling(10).maxLevel(4).levelReq(36)
                .build());

        reg(StatUpgrade.builder("timber")
                .material("wooden_axe").stat("timber")
                .amount(0.25).basePrice(200000).priceScaling(2).maxLevel(12).levelReq(41)
                .build());

        reg(StatUpgrade.builder("extendedJumps")
                .material("leather_boots").stat("triple_jumps")
                .amount(1).basePrice(1_000_000).priceScaling(5).maxLevel(4).levelReq(46)
                .build());

        reg(StatUpgrade.builder("throwingAxeSpeed")
                .material("diamond_axe").stat("throwing_axe_speed")
                .amount(3).basePrice(1_000_000).priceScaling(2).maxLevel(10).levelReq(51)
                .build());

        reg(StatUpgrade.builder("combo")
                .material("lead").stat("combo_str")
                .amount(0.05).basePrice(2_000_000).priceScaling(1.8).maxLevel(10).levelReq(56)
                .build());
    }
}
