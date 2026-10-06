package com.aermini.kitbattleaddon;

import com.aermini.kitbattleaddon.ActionbarSender;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InvincibilityTracker implements Listener {
    private final KitBattleAddon plugin;
    private final ConcurrentHashMap<UUID, Long> invincibleUntil = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, BukkitTask> actionbarTasks = new ConcurrentHashMap<>();
    public InvincibilityTracker(KitBattleAddon plugin) {
        this.plugin = plugin;
    }

    public void setInvincible(Player player, long durationMs) {
        long expireAt = System.currentTimeMillis() + durationMs;
        invincibleUntil.put(player.getUniqueId(), expireAt);
        startActionbar(player);
    }

    public boolean isInvincible(Player player) {
        Long expireAt = invincibleUntil.get(player.getUniqueId());
        if (expireAt == null) return false;
        if (System.currentTimeMillis() > expireAt) {
            invincibleUntil.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player victim = (Player) e.getEntity();
        if (!isInvincible(victim)) return;
        e.setCancelled(true);
        Entity damager = e.getDamager();
        if (damager instanceof Player) {
            Player attacker = (Player) damager;
            String msg = ChatColor.translateAlternateColorCodes(
                    '&', plugin.getSpawnConfig().getHitwdmsg());
            attacker.sendMessage(msg);
        }
    }

    private void startActionbar(Player player) {
        BukkitTask existing = actionbarTasks.remove(player.getUniqueId());
        if (existing != null) existing.cancel();
        final BukkitTask[] holder = new BukkitTask[1];
        holder[0] = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                cancelActionbar(player, holder[0]);
                return;
            }
            long remainingMs = invincibleUntil.getOrDefault(player.getUniqueId(), 0L) - System.currentTimeMillis();
            if (remainingMs <= 0) {
                cancelActionbar(player, holder[0]);
                return;
            }
            int remainingSec = (int) Math.ceil(remainingMs / 1000.0);
            String msg = ChatColor.translateAlternateColorCodes(
                    '&', plugin.getSpawnConfig().getWdmsg().replace("{wdtime}", String.valueOf(remainingSec)));
            ActionbarSender.send(player, msg);
        }, 0L, 20L);
        actionbarTasks.put(player.getUniqueId(), holder[0]);
    }
    private void cancelActionbar(Player player, BukkitTask task) {
        if (task != null) task.cancel();
        actionbarTasks.remove(player.getUniqueId());
        invincibleUntil.remove(player.getUniqueId());
    }
}