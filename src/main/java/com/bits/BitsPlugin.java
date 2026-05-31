package com.bits;

import com.bits.api.BitsEconomy;
import com.bits.commands.BitsCommand;
import com.bits.data.BalanceManager;
import com.bits.data.BalanceStorage;
import com.bits.vault.VaultEconomyBridge;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class BitsPlugin extends JavaPlugin {

    private BalanceStorage storage;
    private BalanceManager balanceManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        storage = new BalanceStorage(this);
        storage.load();
        balanceManager = new BalanceManager(this, storage);

        // Register BitsEconomy as a Bukkit service so other plugins can look it
        // up without casting to BitsPlugin.
        getServer().getServicesManager().register(
                BitsEconomy.class, balanceManager, this, ServicePriority.Normal);

        // Register Vault economy bridge if Vault is present.
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            new VaultEconomyBridge(this, balanceManager).register();
            getLogger().info("Vault economy bridge registered.");
        }

        BitsCommand handler = new BitsCommand(this, balanceManager);
        getCommand("bits").setExecutor(handler);
        getCommand("bits").setTabCompleter(handler);

        getLogger().info("Bits v" + getPluginMeta().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (storage != null) storage.save();
        getLogger().info("Bits disabled.");
    }

    public BalanceManager getBalanceManager() {
        return balanceManager;
    }
}
