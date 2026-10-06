package com.aermini.kitbattleaddon;

import me.wazup.kitbattle.Kitbattle;
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
        pm.registerEvents(new KillListener(), this);
        pm.registerEvents(new LobbyListener(this), this);
        pm.registerEvents(new SoupListener(this), this);

        kitListener = new KitSelectListener(this);
        boolean found = kitListener.init();
        getCommand("kitbattleaddon").setExecutor(new AddonCommand(this));
        if (found) {
            getLogger().info("KitBattle hook active! " + spawnConfig.getSpawnCount() + " spawn points loaded.");
            if (Kitbattle.getInstance().config.SoupAutoDisappear) {
                getLogger().warning("KitBattle config Soup-Auto-Disappear must be false! Bowl auto remove is handled by KitBattleAddon.");
            }
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