package com.bops.placeholders;

import com.bops.api.BopsAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.Locale;

public class BopsPlaceholderExpansion extends PlaceholderExpansion {

    @Override
    public String getIdentifier() { return "bops"; }

    @Override
    public String getAuthor() { return "Dboxual"; }

    @Override
    public String getVersion() { return "1.1.5"; }

    @Override
    public boolean persist() { return true; }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) return "";
        double balance = BopsAPI.getBalance(player.getUniqueId());
        return switch (params) {
            case "balance"                       -> fmtStrip(balance);
            case "balance_formatted",
                 "balance_commas"                -> fmtCommas(balance);
            case "balance_raw"                   -> String.format("%.2f", balance);
            case "balance_whole"                 -> fmtWhole(balance);
            default                              -> null;
        };
    }

    private String fmtStrip(double v) {
        return String.format("%.2f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private String fmtCommas(double v) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(v);
    }

    private String fmtWhole(double v) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(0);
        nf.setGroupingUsed(true);
        return nf.format((long) Math.floor(v));
    }
}
