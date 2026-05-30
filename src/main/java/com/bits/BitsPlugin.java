package com.bits;

import com.bits.commands.BitsCommand;
import com.bits.data.BalanceManager;
import com.bits.data.BalanceStorage;
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
