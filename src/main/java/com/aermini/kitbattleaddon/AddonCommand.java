package com.aermini.kitbattleaddon;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;

public class AddonCommand implements CommandExecutor {
    private final KitBattleAddon plugin;
    public AddonCommand(KitBattleAddon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("kitbattleaddon.admin")) {
            sender.sendMessage("§c[KitBattleAddon] §7No permission.");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "addspawn":
                return handleAddSpawn(sender);
            case "list":
                return handleList(sender);
            case "remove":
                return handleRemove(sender, args);
            case "setlobby":
                return handleSetLobby(sender, args);
            case "reload":
                return handleReload(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleAddSpawn(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c[KitBattleAddon] §7Players only.");
            return true;
        }
        Player player = (Player) sender;
        Location loc = player.getLocation();
        plugin.getSpawnConfig().addSpawn(loc);

        int count = plugin.getSpawnConfig().getSpawnCount();
        player.sendMessage("§a[KitBattleAddon] §7Spawn §f#" + count + " §7added!");
        player.sendMessage("§8  " + loc.getWorld().getName()
                + " x=" + String.format("%.1f", loc.getX())
                + " y=" + String.format("%.1f", loc.getY())
                + " z=" + String.format("%.1f", loc.getZ())
                + " yaw=" + String.format("%.1f", loc.getYaw()));
        return true;
    }

    private boolean handleList(CommandSender sender) {
        List<Location> spawns = plugin.getSpawnConfig().getSpawns();
        if (spawns.isEmpty()) {
            sender.sendMessage("§e[KitBattleAddon] §7No spawn points configured.");
            sender.sendMessage("§7  Stand somewhere and run §f/kitbattleaddon addspawn");
            return true;
        }
        sender.sendMessage("§e[KitBattleAddon] §7Spawn points (" + spawns.size() + "):");
        for (int i = 0; i < spawns.size(); i++) {
            Location loc = spawns.get(i);
            sender.sendMessage("§7  #" + (i + 1) + " §f"
                    + loc.getWorld().getName()
                    + " " + String.format("%.1f", loc.getX())
                    + ", " + String.format("%.1f", loc.getY())
                    + ", " + String.format("%.1f", loc.getZ()));
        }
        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§c[KitBattleAddon] §7Usage: /kitbattleaddon remove <number>");
            return true;
        }
        int index;
        try {
            index = Integer.parseInt(args[1]) - 1;
        } catch (NumberFormatException e) {
            sender.sendMessage("§c[KitBattleAddon] §7Invalid number.");
            return true;
        }
        if (plugin.getSpawnConfig().removeSpawn(index)) {
            sender.sendMessage("§a[KitBattleAddon] §7Spawn #" + (index + 1) + " removed.");
        } else {
            sender.sendMessage("§c[KitBattleAddon] §7Invalid spawn number.");
        }
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        plugin.getSpawnConfig().load();
        sender.sendMessage("§a[KitBattleAddon] §7Reloaded! " + plugin.getSpawnConfig().getSpawnCount() + " spawn points.");
        return true;
    }

    private boolean handleSetLobby(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c[KitBattleAddon] §7Players only.");
            return true;
        }
        if (args.length < 2 || !(args[1].equalsIgnoreCase("pos1") || args[1].equalsIgnoreCase("pos2"))) {
            sender.sendMessage("§c[KitBattleAddon] §7Usage: /kitbattleaddon setlobby <pos1|pos2>");
            return true;
        }
        int index = args[1].equalsIgnoreCase("pos1") ? 1 : 2;
        Player player = (Player) sender;
        Location loc = player.getLocation();
        plugin.getSpawnConfig().setLobbyPos(index, loc);
        player.sendMessage("§a[KitBattleAddon] §7Lobby pos" + index + " set!");
        player.sendMessage("§8  " + loc.getWorld().getName()
                + " x=" + String.format("%.1f", loc.getX())
                + " y=" + String.format("%.1f", loc.getY())
                + " z=" + String.format("%.1f", loc.getZ()));
        if (plugin.getSpawnConfig().isLobbyReady()) {
            player.sendMessage("§a[KitBattleAddon] §7Lobby region is active!");
        } else {
            player.sendMessage("§7[KitBattleAddon] §7Set both pos1 and pos2 in the same world to activate the lobby.");
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§e§lKitBattleAddon §7v1.0.0");
        sender.sendMessage("§7  /kitbattleaddon addspawn §8- §fAdd current position as spawn");
        sender.sendMessage("§7  /kitbattleaddon list §8- §fList all spawns");
        sender.sendMessage("§7  /kitbattleaddon remove <#> §8- §fRemove a spawn");
        sender.sendMessage("§7  /kitbattleaddon setlobby <pos1|pos2> §8- §fSet lobby region corner");
        sender.sendMessage("§7  /kitbattleaddon reload §8- §fReload config");
    }
}