package com.aermini.kitbattleaddon;

import me.wazup.kitbattle.Kitbattle;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SoupListener implements Listener {
    private final KitBattleAddon plugin;

    public SoupListener(KitBattleAddon plugin) {
        this.plugin = plugin;
    }

    // 必须 LOWEST: KitBattle 的汤分支在 NORMAL 且会把手上物品直接 setType 成碗, 晚于它就看不到这是汤了
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.MUSHROOM_SOUP) return;
        Player player = e.getPlayer();
        Kitbattle kb = Kitbattle.getInstance();
        if (kb == null || !kb.players.contains(player.getUniqueId())) return;
        int slot = player.getInventory().getHeldItemSlot();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            ItemStack now = player.getInventory().getItem(slot);
            if (now != null && now.getType() == Material.BOWL) {
                player.getInventory().setItem(slot, null);
            }
        }, 1L);
    }
}
