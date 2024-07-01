package me.kubbidev.renapowered.common.module.ranking.util;

public final class RankingUtil {
    private RankingUtil() {
    }

    public static long getLevelExp(int level) {
        return 5 * ((long) level * level) + (50L * level) + 100;
    }

    public static long getLevelTotalExp(int level) {
        long exp = 0;
        for (int i = 0; i < level; i++) {
            exp += getLevelExp(i);
        }
        return exp;
    }

    public static int getLevelFromExp(long exp) {
        int level = 0;
        while (exp >= getLevelExp(level)) {
            exp -= getLevelExp(level);
            level++;
        }
        return level;
    }

    public static long getRemainingExp(long totalExp) {
        long remaining = totalExp;
        for (int i = 0; i < getLevelFromExp(totalExp); i++) {
            remaining -= getLevelExp(i);
        }
        return remaining;
    }
}