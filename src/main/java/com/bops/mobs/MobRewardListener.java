package com.bops.mobs;

import com.bops.BopsPlugin;
import com.bops.data.BalanceManager;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.concurrent.ThreadLocalRandom;

public class MobRewardListener implements Listener {

    private final BopsPlugin plugin;
    private final BalanceManager balances;
    private final StreakTracker streaks = new StreakTracker();

    public MobRewardListener(BopsPlugin plugin, BalanceManager balances) {
        this.plugin = plugin;
        this.balances = balances;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!plugin.getConfig().getBoolean("mob-rewards.enabled", true)) return;

        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        if (!(event.getEntity() instanceof Monster)) return;

        String mobKey = event.getEntityType().name();
        boolean debug = plugin.getConfig().getBoolean("debug", false);

        double min = plugin.getConfig().getDouble("mob-rewards.reward-range.min", 0.1);
        double max = plugin.getConfig().getDouble("mob-rewards.reward-range.max", 0.5);
        double baseReward = (min >= max) ? min : ThreadLocalRandom.current().nextDouble(min, max);

        int streakCount = streaks.recordKill(killer.getUniqueId(), event.getEntityType());
        double multiplier = 1.0;

        if (plugin.getConfig().getBoolean("mob-rewards.diminishing-returns.enabled", true)) {
            int threshold = plugin.getConfig().getInt("mob-rewards.diminishing-returns.same-mob-threshold", 5);
            if (streakCount > threshold) {
                double reductionPerKill = plugin.getConfig().getDouble("mob-rewards.diminishing-returns.reduction-per-kill", 0.10);
                double minMultiplier = plugin.getConfig().getDouble("mob-rewards.diminishing-returns.min-multiplier", 0.10);
                multiplier = Math.max(minMultiplier, 1.0 - (reductionPerKill * (streakCount - threshold)));
            }
        }

        double finalReward = Math.round(baseReward * multiplier * 100.0) / 100.0;

        if (finalReward <= 0) {
            if (debug) {
                plugin.getLogger().info("[Bops Debug] No reward: " + killer.getName()
                        + " killed " + mobKey + " — final reward rounded to 0"
                        + " | streak=" + streakCount);
            }
            return;
        }

        balances.deposit(killer.getUniqueId(), finalReward);
        balances.save();

        if (debug) {
            plugin.getLogger().info("[Bops Debug] " + killer.getName() + " killed " + mobKey
                    + " | base=" + String.format("%.2f", baseReward)
                    + " | multiplier=" + String.format("%.2f", multiplier)
                    + " | final=" + String.format("%.2f", finalReward)
                    + " | streak=" + streakCount);
        }
    }
}
