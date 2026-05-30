package com.bits.data;

import com.bits.BitsPlugin;

import java.util.UUID;

public class BalanceManager {

    private final BitsPlugin plugin;
    private final BalanceStorage storage;

    public BalanceManager(BitsPlugin plugin, BalanceStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public long getBalance(UUID uuid) {
        long starting = plugin.getConfig().getLong("settings.starting-balance", 0);
        return storage.getBalances().getOrDefault(uuid, starting);
    }

    public void setBalance(UUID uuid, long amount) {
        storage.getBalances().put(uuid, Math.max(0, amount));
    }

    public void give(UUID uuid, long amount) {
        setBalance(uuid, getBalance(uuid) + amount);
    }

    /** Returns false if the player has insufficient funds. */
    public boolean take(UUID uuid, long amount) {
        long current = getBalance(uuid);
        if (current < amount) return false;
        setBalance(uuid, current - amount);
        return true;
    }

    /** Returns false if the payer has insufficient funds. */
    public boolean pay(UUID from, UUID to, long amount) {
        if (getBalance(from) < amount) return false;
        setBalance(from, getBalance(from) - amount);
        give(to, amount);
        return true;
    }

    public void save() {
        storage.save();
    }
}
