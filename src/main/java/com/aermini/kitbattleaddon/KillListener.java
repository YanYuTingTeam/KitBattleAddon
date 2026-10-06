package com.aermini.kitbattleaddon;

import me.wazup.kitbattle.Kitbattle;
import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.managers.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;

public class KillListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Kitbattle kb = Kitbattle.getInstance();
        if (kb == null) return;
        Player victim = e.getEntity();
        if (!kb.players.contains(victim.getUniqueId())) return;
        Player killer = victim.getKiller();
        if (killer == null || PlayerDataManager.get(killer) == null) return;
        if (killer.getName().equals(victim.getName())) return;
        giveSoup(killer);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Kitbattle kb = Kitbattle.getInstance();
        if (kb == null) return;
        Player victim = e.getPlayer();
        if (!kb.players.contains(victim.getUniqueId())) return;
        PlayerData victimData = PlayerDataManager.get(victim);
        if (victimData == null || victimData.damagers.isEmpty()) return;
        if ((System.currentTimeMillis() - victimData.lastHitTime) / 1000L > (long) kb.config.combatLogDuration) return;
        Player killer = Bukkit.getPlayer(victimData.lastHit);
        if (killer == null || killer.getName().equals(victim.getName())) return;
        if (!kb.players.contains(killer.getUniqueId())) return;
        giveSoup(killer);
    }

    private void giveSoup(Player killer) {
        HashMap<Integer, ItemStack> left = killer.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP));
        for (ItemStack item : left.values()) {
            killer.getWorld().dropItemNaturally(killer.getLocation(), item);
        }
    }
}
