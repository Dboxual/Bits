package com.bops.data;

import com.bops.BopsPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BalanceStorage {

    private final BopsPlugin plugin;
    private final File file;
    private final Map<UUID, Double> balances = new HashMap<>();

    public BalanceStorage(BopsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "balances.yml");
    }

    public void load() {
        file.getParentFile().mkdirs();

        if (!file.exists()) {
            // Migrate from old Bits plugin data if present
            File bitsBalances = new File(plugin.getDataFolder().getParentFile(), "Bits/balances.yml");
            if (bitsBalances.exists()) {
                plugin.getLogger().info("Migrating balances from Bits plugin data...");
                loadFrom(bitsBalances);
                save(); // write immediately to bops balances.yml
                plugin.getLogger().info("Migration complete: " + balances.size() + " balance(s) carried over from Bits.");
                return;
            }

            try { file.createNewFile(); } catch (IOException e) {
                plugin.getLogger().severe("Could not create balances.yml: " + e.getMessage());
            }
        }

        loadFrom(file);
        plugin.getLogger().info("Loaded " + balances.size() + " balance(s).");
    }

    private void loadFrom(File source) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(source);
        balances.clear();
        if (yaml.isConfigurationSection("balances")) {
            for (String key : yaml.getConfigurationSection("balances").getKeys(false)) {
                try {
                    balances.put(UUID.fromString(key), yaml.getDouble("balances." + key));
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            yaml.set("balances." + entry.getKey(), entry.getValue());
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save balances.yml: " + e.getMessage());
        }
    }

    public Map<UUID, Double> getBalances() {
        return balances;
    }
}
