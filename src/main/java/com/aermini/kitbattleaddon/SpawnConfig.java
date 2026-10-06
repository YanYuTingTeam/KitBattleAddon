package com.aermini.kitbattleaddon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import java.util.*;

public class SpawnConfig {
    private final KitBattleAddon plugin;
    private final List<Location> spawns = new ArrayList<>();
    private final Random random = new Random();
    private int wdtime;
    private String wdmsg;
    private String hitwdmsg;
    private Location lobbyPos1;
    private Location lobbyPos2;
    public SpawnConfig(KitBattleAddon plugin) {
        this.plugin = plugin;
    }

    public void load() {
        spawns.clear();
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        wdtime = plugin.getConfig().getInt("wdtime", 3000);
        wdmsg = plugin.getConfig().getString("wdmsg", "&a&l无敌状态将于&a&l{wdtime}&a&l秒后结束！");
        hitwdmsg = plugin.getConfig().getString("hitwdmsg", "&b&l烟雨庭 &r&7>>&c&l该玩家处于无敌状态！");
        lobbyPos1 = loadLobbyPos("lobby.pos1");
        lobbyPos2 = loadLobbyPos("lobby.pos2");
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("spawns");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            ConfigurationSection s = section.getConfigurationSection(key);
            if (s == null) continue;
            World world = Bukkit.getWorld(s.getString("world", "world"));
            if (world == null) {
                plugin.getLogger().warning("Spawn '" + key + "': world not found, skipping.");
                continue;
            }
            Location loc = new Location(
                    world,
                    s.getDouble("x"),
                    s.getDouble("y"),
                    s.getDouble("z"),
                    (float) s.getDouble("yaw", 0),
                    (float) s.getDouble("pitch", 0)
            );
            spawns.add(loc);
        }
        plugin.getLogger().info("Loaded " + spawns.size() + " spawn points.");
    }

    private Location loadLobbyPos(String path) {
        String worldName = plugin.getConfig().getString(path + ".world");
        if (worldName == null) return null;
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("Lobby pos: world not found, skipping.");
            return null;
        }
        return new Location(
                world,
                plugin.getConfig().getDouble(path + ".x"),
                plugin.getConfig().getDouble(path + ".y"),
                plugin.getConfig().getDouble(path + ".z")
        );
    }

    public void save() {
        plugin.getConfig().set("spawns", null);
        for (int i = 0; i < spawns.size(); i++) {
            Location loc = spawns.get(i);
            String path = "spawns." + i;
            plugin.getConfig().set(path + ".world", loc.getWorld().getName());
            plugin.getConfig().set(path + ".x", loc.getX());
            plugin.getConfig().set(path + ".y", loc.getY());
            plugin.getConfig().set(path + ".z", loc.getZ());
            plugin.getConfig().set(path + ".yaw", (double) loc.getYaw());
            plugin.getConfig().set(path + ".pitch", (double) loc.getPitch());
        }
        saveLobbyPos("lobby.pos1", lobbyPos1);
        saveLobbyPos("lobby.pos2", lobbyPos2);
        plugin.saveConfig();
    }

    private void saveLobbyPos(String path, Location loc) {
        if (loc == null) {
            plugin.getConfig().set(path, null);
            return;
        }
        plugin.getConfig().set(path + ".world", loc.getWorld().getName());
        plugin.getConfig().set(path + ".x", loc.getX());
        plugin.getConfig().set(path + ".y", loc.getY());
        plugin.getConfig().set(path + ".z", loc.getZ());
    }

    public void addSpawn(Location loc) {
        spawns.add(loc.clone());
        save();
    }

    public boolean removeSpawn(int index) {
        if (index < 0 || index >= spawns.size()) return false;
        spawns.remove(index);
        save();
        return true;
    }

    public Location getRandomSpawn() {
        if (spawns.isEmpty()) return null;
        return spawns.get(random.nextInt(spawns.size()));
    }

    public void setLobbyPos(int index, Location loc) {
        if (index == 1) {
            lobbyPos1 = loc.clone();
        } else {
            lobbyPos2 = loc.clone();
        }
        save();
    }

    public boolean isLobbyReady() {
        return lobbyPos1 != null && lobbyPos2 != null
                && lobbyPos1.getWorld().equals(lobbyPos2.getWorld());
    }

    public boolean isInLobby(Location loc) {
        if (!isLobbyReady()) return false;
        if (!lobbyPos1.getWorld().equals(loc.getWorld())) return false;
        double minX = Math.min(lobbyPos1.getX(), lobbyPos2.getX());
        double maxX = Math.max(lobbyPos1.getX(), lobbyPos2.getX());
        double minY = Math.min(lobbyPos1.getY(), lobbyPos2.getY());
        double maxY = Math.max(lobbyPos1.getY(), lobbyPos2.getY());
        double minZ = Math.min(lobbyPos1.getZ(), lobbyPos2.getZ());
        double maxZ = Math.max(lobbyPos1.getZ(), lobbyPos2.getZ());
        return loc.getX() >= minX && loc.getX() <= maxX
                && loc.getY() >= minY && loc.getY() <= maxY
                && loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }

    public int getSpawnCount() {
        return spawns.size();
    }

    public List<Location> getSpawns() {
        return Collections.unmodifiableList(spawns);
    }
    public int getWdtime() { return wdtime; }
    public String getWdmsg() { return wdmsg; }
    public String getHitwdmsg() { return hitwdmsg; }
}