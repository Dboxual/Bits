package com.bits.mobs;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MobKillTracker {

    // mob entity UUID → (player UUID → damage dealt)
    private final Map<UUID, Map<UUID, Double>> damageMap = new HashMap<>();
    // entity UUIDs known to have spawned from a spawner block
    private final Set<UUID> spawnerEntities = new HashSet<>();

    void recordDamage(UUID entityId, UUID playerId, double amount) {
        damageMap.computeIfAbsent(entityId, k -> new HashMap<>())
                .merge(playerId, amount, Double::sum);
    }

    void markSpawnerEntity(UUID entityId) {
        spawnerEntities.add(entityId);
    }

    boolean isSpawnerEntity(UUID entityId) {
        return spawnerEntities.contains(entityId);
    }

    /**
     * Returns the UUID of the player who dealt the most damage to this entity,
     * provided they dealt at least {@code minFraction} of the total damage.
     * Returns null if no qualifying player exists.
     */
    UUID getTopDamageDealer(UUID entityId, double minFraction) {
        Map<UUID, Double> damages = damageMap.get(entityId);
        if (damages == null || damages.isEmpty()) return null;

        double total = damages.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total <= 0) return null;

        UUID top = null;
        double topDamage = 0;
        for (Map.Entry<UUID, Double> e : damages.entrySet()) {
            if (e.getValue() > topDamage) {
                topDamage = e.getValue();
                top = e.getKey();
            }
        }

        return (top != null && topDamage / total >= minFraction) ? top : null;
    }

    void cleanup(UUID entityId) {
        damageMap.remove(entityId);
        spawnerEntities.remove(entityId);
    }

    /** Resolves the attacking player from a direct hit or a player-fired projectile. */
    static UUID resolveAttacker(Entity damager) {
        if (damager instanceof Player p) return p.getUniqueId();
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) return p.getUniqueId();
        return null;
    }
}
