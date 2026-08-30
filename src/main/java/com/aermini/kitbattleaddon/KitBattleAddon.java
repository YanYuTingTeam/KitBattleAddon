package com.aermini.kitbattleaddon;

import org.bukkit.plugin.java.JavaPlugin;
public class KitBattleAddon extends JavaPlugin {
    private SpawnConfig spawnConfig;
    private KitSelectListener kitListener;

    @Override
    public void onEnable() {
        spawnConfig = new SpawnConfig(this);
        spawnConfig.load();
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
}