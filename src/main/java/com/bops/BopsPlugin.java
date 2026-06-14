package com.bops;

import com.bops.api.BopsAPI;
import com.bops.commands.BopsCommand;
import com.bops.data.BalanceManager;
import com.bops.data.BalanceStorage;
import com.bops.mobs.MobRewardListener;
import com.bops.placeholders.BitsLegacyExpansion;
import com.bops.placeholders.BopsPlaceholderExpansion;
import org.bukkit.plugin.java.JavaPlugin;

public class BopsPlugin extends JavaPlugin {

    private BalanceStorage storage;
    private BalanceManager balanceManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        storage = new BalanceStorage(this);
        storage.load();
        balanceManager = new BalanceManager(this, storage);

        BopsAPI.init(balanceManager);

        BopsCommand handler = new BopsCommand(this, balanceManager);
        getCommand("bops").setExecutor(handler);
        getCommand("bops").setTabCompleter(handler);

        getServer().getPluginManager().registerEvents(new MobRewardListener(this, balanceManager), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            boolean bopsOk = new BopsPlaceholderExpansion().register();
            boolean bitsOk = new BitsLegacyExpansion().register();
            if (bopsOk && bitsOk) {
                getLogger().info("PlaceholderAPI support enabled — %bops_balance%, %bops_balance_raw%, %bops_balance_formatted%, %bops_balance_commas%, %bops_balance_whole% active.");
            } else {
                getLogger().warning("PlaceholderAPI found but expansion registration failed (bops=" + bopsOk + ", bits=" + bitsOk + "). Check PAPI logs.");
            }
        } else {
            getLogger().info("PlaceholderAPI not found — placeholders skipped.");
        }

        getLogger().info("Bops v" + getPluginMeta().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (storage != null) storage.save();
        getLogger().info("Bops disabled.");
    }

    public void reload() {
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    public BalanceManager getBalanceManager() {
        return balanceManager;
    }
}
