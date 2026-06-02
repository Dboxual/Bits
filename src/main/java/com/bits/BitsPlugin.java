package com.bits;

import com.bits.api.BitsEconomy;
import com.bits.commands.BitsCommand;
import com.bits.data.BalanceManager;
import com.bits.data.BalanceStorage;
import com.bits.mobs.MobKillListener;
import com.bits.mobs.MobRewardConfig;
import com.bits.mobs.MobKillTracker;
import com.bits.mobs.KillDataStorage;
import com.bits.vault.VaultEconomyBridge;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class BitsPlugin extends JavaPlugin {

    private BalanceStorage storage;
    private BalanceManager balanceManager;
    private KillDataStorage killDataStorage;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        storage = new BalanceStorage(this);
        storage.load();
        balanceManager = new BalanceManager(this, storage);

        killDataStorage = new KillDataStorage(this);
        killDataStorage.load();

        getServer().getServicesManager().register(
                BitsEconomy.class, balanceManager, this, ServicePriority.Normal);

        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            new VaultEconomyBridge(this, balanceManager).register();
            getLogger().info("Vault economy bridge registered.");
        }

        BitsCommand handler = new BitsCommand(this, balanceManager);
        getCommand("bits").setExecutor(handler);
        getCommand("bits").setTabCompleter(handler);

        MobRewardConfig mobCfg = new MobRewardConfig(this);
        if (mobCfg.isEnabled()) {
            MobKillTracker tracker = new MobKillTracker();
            getServer().getPluginManager().registerEvents(
                    new MobKillListener(mobCfg, tracker, killDataStorage, balanceManager), this);
            getLogger().info("Mob kill rewards enabled.");
        }

        getLogger().info("Bits v" + getPluginMeta().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (storage != null) storage.save();
        if (killDataStorage != null) killDataStorage.save();
        getLogger().info("Bits disabled.");
    }

    public BalanceManager getBalanceManager() {
        return balanceManager;
    }
}
