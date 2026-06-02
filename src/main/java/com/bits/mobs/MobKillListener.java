package com.bits.mobs;

import com.bits.data.BalanceManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.UUID;

public class MobKillListener implements Listener {

    private final MobRewardConfig config;
    private final MobKillTracker tracker;
    private final KillDataStorage killData;
    private final BalanceManager balances;

    public MobKillListener(MobRewardConfig config, MobKillTracker tracker,
                           KillDataStorage killData, BalanceManager balances) {
        this.config = config;
        this.tracker = tracker;
        this.killData = killData;
        this.balances = balances;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            tracker.markSpawnerEntity(event.getEntity().getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Mob)) return;
        UUID attacker = MobKillTracker.resolveAttacker(event.getDamager());
        if (attacker == null) return;
        tracker.recordDamage(event.getEntity().getUniqueId(), attacker, event.getFinalDamage());
    }

    @EventHandler(ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;

        UUID entityId = mob.getUniqueId();

        if (!config.rewardSpawnerMobs() && tracker.isSpawnerEntity(entityId)) {
            tracker.cleanup(entityId);
            return;
        }

        UUID winner = tracker.getTopDamageDealer(entityId, config.getMinDamageFraction());
        tracker.cleanup(entityId);

        if (winner == null) return;

        long base = config.getBaseReward(mob.getType());
        if (base <= 0) return;

        long reward = killData.calculateAndRecord(winner, mob.getType().name(), base, config);
        if (reward <= 0) return;

        balances.deposit(winner, reward);
        balances.save();
        killData.save();

        Player player = Bukkit.getPlayer(winner);
        if (player != null) {
            String mobName = mob.getType().name().replace('_', ' ');
            mobName = Character.toUpperCase(mobName.charAt(0)) + mobName.substring(1).toLowerCase();
            player.sendMessage("§a+" + reward + " §eBits §8[" + mobName + "]");
        }
    }
}
