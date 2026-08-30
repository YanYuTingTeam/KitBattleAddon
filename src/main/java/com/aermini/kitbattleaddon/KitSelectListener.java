package com.aermini.kitbattleaddon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class KitSelectListener implements Listener {
    private final KitBattleAddon plugin;
    private static final String KIT_EVENT_CLASS = "me.wazup.kitbattle.events.PlayerSelectKitEvent";
    private Class<? extends Event> kitEventClass;
    private Method getPlayerMethod;
    private RegisteredListener registeredListener;
    private HandlerList handlerList;

    public KitSelectListener(KitBattleAddon plugin) {
        this.plugin = plugin;
    }

    public boolean init() {
        try {
            kitEventClass = (Class<? extends Event>) Class.forName(KIT_EVENT_CLASS);
            try {
                getPlayerMethod = kitEventClass.getMethod("getPlayer");
                if (!Player.class.isAssignableFrom(getPlayerMethod.getReturnType())) {
                    getPlayerMethod = null;
                }
            } catch (NoSuchMethodException ignored) {}

            if (getPlayerMethod == null) {
                plugin.getLogger().warning("Could not find getPlayer() on PlayerSelectKitEvent!");
                return false;
            }
            Method getHandlerList = kitEventClass.getMethod("getHandlerList");
            handlerList = (HandlerList) getHandlerList.invoke(null);
            EventExecutor executor = (listener, event) -> {
                if (!kitEventClass.isInstance(event)) return;
                onKitSelect(event);
            };
            registeredListener = new RegisteredListener(
                    new Listener() {},
                    executor,
                    EventPriority.MONITOR,
                    plugin,
                    false
            );
            handlerList.register(registeredListener);
            plugin.getLogger().info("PlayerSelectKitEvent listener registered.");
            return true;
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("KitBattle event class not found: " + KIT_EVENT_CLASS);
            return false;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to hook KitBattle", e);
            return false;
        }
    }

    private void onKitSelect(Event event) {
        try {
            Player player = (Player) getPlayerMethod.invoke(event);
            if (player == null) return;

            Location spawn = plugin.getSpawnConfig().getRandomSpawn();
            if (spawn == null) {
                plugin.getLogger().warning("No spawn points configured! Use /kitbattleaddon addspawn");
                return;
            }
            final Player p = player;
            final Location dest = spawn.clone();
            if (p.isOnline()) p.teleport(dest);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error handling kit selection", e);
        }
    }

    public void unregister() {
        if (handlerList != null && registeredListener != null) {
            handlerList.unregister(registeredListener);
        }
    }
}