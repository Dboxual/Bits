package com.bops.mobs;

import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StreakTracker {

    private final Map<UUID, PlayerStreak> data = new HashMap<>();

    public int recordKill(UUID playerId, EntityType type) {
        PlayerStreak streak = data.get(playerId);
        if (streak == null || streak.type != type) {
            data.put(playerId, new PlayerStreak(type, 1));
            return 1;
        }
        return ++streak.count;
    }

    private static class PlayerStreak {
        final EntityType type;
        int count;

        PlayerStreak(EntityType type, int count) {
            this.type = type;
            this.count = count;
        }
    }
}
