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

    public boolean has(UUID uuid, long amount) {
        return amount >= 0 && getBalance(uuid) >= amount;
    }

    public void deposit(UUID uuid, long amount) {
        if (amount <= 0) return;
        long current = getBalance(uuid);
        long next = current + amount;
        if (next < current) next = Long.MAX_VALUE;
        setBalance(uuid, next);
    }

    public boolean withdraw(UUID uuid, long amount) {
        if (amount <= 0) return false;
        long current = getBalance(uuid);
        if (current < amount) return false;
        setBalance(uuid, current - amount);
        return true;
    }

    public boolean pay(UUID from, UUID to, long amount) {
        long fromBalance = getBalance(from);
        if (fromBalance < amount) return false;
        setBalance(from, fromBalance - amount);
        deposit(to, amount);
        return true;
    }

    public void save() {
        storage.save();
    }
}
