package com.bops.api;

import com.bops.data.BalanceManager;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class BopsAPI {

    private static BalanceManager manager;

    public static void init(BalanceManager m) {
        manager = m;
    }

    private static BalanceManager mgr() {
        if (manager == null) throw new IllegalStateException("BopsAPI not ready — is Bops enabled?");
        return manager;
    }

    public static double getBalance(UUID uuid) {
        return mgr().getBalance(uuid);
    }

    public static double getBalance(Player player) {
        return mgr().getBalance(player.getUniqueId());
    }

    public static void deposit(UUID uuid, double amount) {
        mgr().deposit(uuid, amount);
    }

    public static boolean withdraw(UUID uuid, double amount) {
        return mgr().withdraw(uuid, amount);
    }

    public static void setBalance(UUID uuid, double amount) {
        mgr().setBalance(uuid, amount);
    }

    public static boolean has(UUID uuid, double amount) {
        return mgr().has(uuid, amount);
    }
}
