package com.bops.data;

import com.bops.BopsPlugin;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class BalanceManager {

    private final BopsPlugin plugin;
    private final BalanceStorage storage;

    public BalanceManager(BopsPlugin plugin, BalanceStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public double getBalance(UUID uuid) {
        double starting = plugin.getConfig().getDouble("settings.starting-balance", 0);
        return storage.getBalances().getOrDefault(uuid, starting);
    }

    public void setBalance(UUID uuid, double amount) {
        double rounded = Math.round(Math.max(0.0, amount) * 100.0) / 100.0;
        storage.getBalances().put(uuid, rounded);
    }

    public boolean has(UUID uuid, double amount) {
        return amount >= 0 && getBalance(uuid) >= amount;
    }

    public void deposit(UUID uuid, double amount) {
        if (amount <= 0) return;
        setBalance(uuid, getBalance(uuid) + amount);
    }

    public boolean withdraw(UUID uuid, double amount) {
        if (amount <= 0) return false;
        double current = getBalance(uuid);
        if (current < amount) return false;
        setBalance(uuid, current - amount);
        return true;
    }

    public boolean pay(UUID from, UUID to, double amount) {
        double fromBalance = getBalance(from);
        if (fromBalance < amount) return false;
        setBalance(from, fromBalance - amount);
        deposit(to, amount);
        return true;
    }

    public Map<UUID, Double> getAllBalances() {
        return Collections.unmodifiableMap(storage.getBalances());
    }

    public void save() {
        storage.save();
    }
}
