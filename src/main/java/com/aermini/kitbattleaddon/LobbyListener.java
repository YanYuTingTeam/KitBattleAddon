package com.aermini.kitbattleaddon;

import me.wazup.kitbattle.Kit;
import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.abilities.Ability;
import me.wazup.kitbattle.managers.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

public class LobbyListener implements Listener {
    private final KitBattleAddon plugin;

    public LobbyListener(KitBattleAddon plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player
                && plugin.getSpawnConfig().isInLobby(e.getEntity().getLocation())) {
            e.setCancelled(true);
            return;
        }
        if (!(e instanceof EntityDamageByEntityEvent)) return;
        Player attacker = getAttacker(((EntityDamageByEntityEvent) e).getDamager());
        if (attacker != null && plugin.getSpawnConfig().isInLobby(attacker.getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL) return;
        Player player = e.getPlayer();
        if (!plugin.getSpawnConfig().isInLobby(player.getLocation())) return;
        PlayerData data = PlayerDataManager.get(player);
        if (data == null || data.getKit() == null) return;
        ItemStack item = player.getItemInHand();
        if (item == null) return;
        for (Ability ability : data.getKit().getInteractionAbilities()) {
            if (ability.getActivationMaterial() == item.getType()) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof Player)) return;
        Player player = e.getPlayer();
        Player target = (Player) e.getRightClicked();
        if (!plugin.getSpawnConfig().isInLobby(player.getLocation())
                && !plugin.getSpawnConfig().isInLobby(target.getLocation())) return;
        PlayerData data = PlayerDataManager.get(player);
        if (data == null || data.getKit() == null) return;
        if (!data.getKit().getEntityInteractionAbilities().isEmpty()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerFish(PlayerFishEvent e) {
        Player player = e.getPlayer();
        if (plugin.getSpawnConfig().isInLobby(player.getLocation())) {
            e.setCancelled(true);
            return;
        }
        if (e.getState() == PlayerFishEvent.State.CAUGHT_ENTITY
                && e.getCaught() instanceof Player
                && plugin.getSpawnConfig().isInLobby(e.getCaught().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        healInLobby(e.getPlayer(), e.getTo());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        healInLobby(e.getPlayer(), e.getTo());
    }

    private void healInLobby(Player player, Location to) {
        if (!plugin.getSpawnConfig().isInLobby(to)) return;
        if (player.getHealth() < player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private Player getAttacker(Entity entity) {
        if (entity instanceof Player) return (Player) entity;
        if (entity instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) entity).getShooter();
            if (shooter instanceof Player) return (Player) shooter;
        }
        // KitBattle 召唤物命名是 "主人's xxx", 用 "'s" 前面那段反查主人
        if (entity.hasMetadata("toRemove") && entity.getCustomName() != null) {
            return Bukkit.getPlayer(entity.getCustomName().split("'s")[0]);
        }
        return null;
    }
}
