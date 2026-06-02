package com.bits.mobs;

import java.util.HashMap;
import java.util.Map;

class PlayerKillData {

    private String date;
    private long earned;
    private final Map<String, Integer> kills;

    PlayerKillData(String date, long earned, Map<String, Integer> kills) {
        this.date = date;
        this.earned = earned;
        this.kills = kills;
    }

    String getDate() { return date; }
    long getEarned() { return earned; }
    Map<String, Integer> getKills() { return kills; }

    void addEarned(long amount) { earned += amount; }

    int getKillCount(String mobType) {
        return kills.getOrDefault(mobType, 0);
    }

    void incrementKills(String mobType) {
        kills.merge(mobType, 1, Integer::sum);
    }

    void resetForDate(String newDate) {
        date = newDate;
        earned = 0;
        kills.clear();
    }

    static PlayerKillData fresh(String date) {
        return new PlayerKillData(date, 0, new HashMap<>());
    }
}
