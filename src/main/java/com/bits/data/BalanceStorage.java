package com.bits.data;

import com.bits.BitsPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BalanceStorage {

    private final BitsPlugin plugin;
    private final File file;
    private final Map<UUID, Long> balances = new HashMap<>();

    public BalanceStorage(BitsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "balances.yml");
    }

    public void load() {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try { file.createNewFile(); } catch (IOException e) {
                plugin.getLogger().severe("Could not create balances.yml: " + e.getMessage());
            }
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        balances.clear();
        if (yaml.isConfigurationSection("balances")) {
            for (String key : yaml.getConfigurationSection("balances").getKeys(false)) {
                try {
                    balances.put(UUID.fromString(key), yaml.getLong("balances." + key));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        plugin.getLogger().info("Loaded " + balances.size() + " balance(s).");
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, Long> entry : balances.entrySet()) {
            yaml.set("balances." + entry.getKey(), entry.getValue());
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save balances.yml: " + e.getMessage());
        }
    }

    public Map<UUID, Long> getBalances() {
        return balances;
    }
}
