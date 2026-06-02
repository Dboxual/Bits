package com.bits.mobs;

import com.bits.BitsPlugin;
import org.bukkit.entity.EntityType;

public class MobRewardConfig {

    private final BitsPlugin plugin;

    public MobRewardConfig(BitsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("mob-rewards.enabled", true);
    }

    double getMinDamageFraction() {
        return plugin.getConfig().getDouble("mob-rewards.min-damage-fraction", 0.10);
    }

    boolean rewardSpawnerMobs() {
        return plugin.getConfig().getBoolean("mob-rewards.reward-spawner-mobs", false);
    }

    long getDailyCap() {
        return plugin.getConfig().getLong("mob-rewards.daily-cap", 0);
    }

    double getDecayPerKill() {
        return plugin.getConfig().getDouble("mob-rewards.diminishing-returns.decay-per-kill", 0.10);
    }

    double getFloor() {
        return plugin.getConfig().getDouble("mob-rewards.diminishing-returns.floor", 0.10);
    }

    long getBaseReward(EntityType type) {
        return plugin.getConfig().getLong("mob-rewards.rewards." + type.name(), 0);
    }
}
