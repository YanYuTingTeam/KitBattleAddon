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
        plugin.saveConfig();
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