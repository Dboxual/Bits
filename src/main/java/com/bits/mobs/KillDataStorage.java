package com.bits.mobs;

import com.bits.BitsPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KillDataStorage {

    private final BitsPlugin plugin;
    private final File file;
    private final Map<UUID, PlayerKillData> data = new HashMap<>();

    public KillDataStorage(BitsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "mob_kills.yml");
    }

    public void load() {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        data.clear();
        String today = LocalDate.now().toString();
        if (!yaml.isConfigurationSection("players")) return;
        for (String key : yaml.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String path = "players." + key;
                String date = yaml.getString(path + ".date", "");
                if (!today.equals(date)) continue; // yesterday's data, skip
                long earned = yaml.getLong(path + ".earned", 0);
                Map<String, Integer> kills = new HashMap<>();
                if (yaml.isConfigurationSection(path + ".kills")) {
                    for (String mob : yaml.getConfigurationSection(path + ".kills").getKeys(false)) {
                        kills.put(mob, yaml.getInt(path + ".kills." + mob, 0));
                    }
                }
                data.put(uuid, new PlayerKillData(date, earned, kills));
            } catch (IllegalArgumentException ignored) {}
        }
        plugin.getLogger().info("Loaded mob kill data for " + data.size() + " player(s).");
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, PlayerKillData> entry : data.entrySet()) {
            String path = "players." + entry.getKey();
            PlayerKillData d = entry.getValue();
            yaml.set(path + ".date", d.getDate());
            yaml.set(path + ".earned", d.getEarned());
            for (Map.Entry<String, Integer> kill : d.getKills().entrySet()) {
                yaml.set(path + ".kills." + kill.getKey(), kill.getValue());
            }
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save mob_kills.yml: " + e.getMessage());
        }
    }

    /**
     * Applies diminishing returns and daily cap, records the kill, and returns
     * the actual Bits to award. Returns 0 if the player is at their daily cap.
     */
    long calculateAndRecord(UUID player, String mobType, long baseReward, MobRewardConfig cfg) {
        String today = LocalDate.now().toString();
        PlayerKillData d = data.computeIfAbsent(player, k -> PlayerKillData.fresh(today));

        if (!today.equals(d.getDate())) d.resetForDate(today);

        long cap = cfg.getDailyCap();
        if (cap > 0 && d.getEarned() >= cap) return 0;

        int killCount = d.getKillCount(mobType);
        double multiplier = Math.max(cfg.getFloor(), 1.0 - cfg.getDecayPerKill() * killCount);
        long reward = Math.round(baseReward * multiplier);

        if (cap > 0 && d.getEarned() + reward > cap) {
            reward = cap - d.getEarned();
        }

        if (reward <= 0) return 0;

        d.addEarned(reward);
        d.incrementKills(mobType);
        return reward;
    }
}
