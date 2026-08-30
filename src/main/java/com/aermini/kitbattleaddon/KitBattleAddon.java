package com.aermini.kitbattleaddon;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class KitBattleAddon extends JavaPlugin {
    private SpawnConfig spawnConfig;
    private KitSelectListener kitListener;
    private InvincibilityTracker invincibleTracker;

    @Override
    public void onEnable() {
        spawnConfig = new SpawnConfig(this);
        spawnConfig.load();

        invincibleTracker = new InvincibilityTracker(this);
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(invincibleTracker, this);

        kitListener = new KitSelectListener(this);
        boolean found = kitListener.init();
        getCommand("kitbattleaddon").setExecutor(new AddonCommand(this));
        if (found) {
            getLogger().info("KitBattle hook active! " + spawnConfig.getSpawnCount() + " spawn points loaded.");
        } else {
            getLogger().warning("KitBattle not found. Plugin will not function.");
        }
    }

    @Override
    public void onDisable() {
        if (kitListener != null) {
            kitListener.unregister();
        }
    }

    public SpawnConfig getSpawnConfig() {
        return spawnConfig;
    }

    public InvincibilityTracker getInvincibilityTracker() {
        return invincibleTracker;
    }
}