package com.dcrubik.fnaf.managers;

import com.dcrubik.fnaf.FNAF;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class MapManager {

    private final FNAF plugin;

    public MapManager(FNAF plugin) {
        this.plugin = plugin;
    }

    public boolean createMap(String mapName) {
        String path = "maps." + mapName;
        if (plugin.getConfig().contains(path)) return false;
        plugin.getConfig().set(path + "._created", true);
        plugin.saveConfig();
        return true;
    }

    public boolean mapExists(String mapName) {
        return plugin.getConfig().contains("maps." + mapName);
    }

    public List<String> getMapNames() {
        List<String> names = new ArrayList<>();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("maps");
        if (section != null) names.addAll(section.getKeys(false));
        return names;
    }

    public void saveLocation(String mapName, String key, Location loc) {
        String path = "maps." + mapName + "." + key;
        plugin.getConfig().set(path + ".world", loc.getWorld().getName());
        plugin.getConfig().set(path + ".x", loc.getX());
        plugin.getConfig().set(path + ".y", loc.getY());
        plugin.getConfig().set(path + ".z", loc.getZ());
        plugin.getConfig().set(path + ".yaw", (double) loc.getYaw());
        plugin.getConfig().set(path + ".pitch", (double) loc.getPitch());
        plugin.saveConfig();
    }

    public Location loadLocation(String mapName, String key) {
        String path = "maps." + mapName + "." + key;
        if (!plugin.getConfig().contains(path + ".world")) return null;
        String worldName = plugin.getConfig().getString(path + ".world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        double x    = plugin.getConfig().getDouble(path + ".x");
        double y    = plugin.getConfig().getDouble(path + ".y");
        double z    = plugin.getConfig().getDouble(path + ".z");
        float yaw   = (float) plugin.getConfig().getDouble(path + ".yaw");
        float pitch = (float) plugin.getConfig().getDouble(path + ".pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    public void setLobbySpawn(String mapName, Location loc)    { saveLocation(mapName, "lobby", loc); }
    public void setGuardSpawn(String mapName, Location loc)    { saveLocation(mapName, "guardspawn", loc); }
    public Location getLobbySpawn(String mapName)              { return loadLocation(mapName, "lobby"); }
    public Location getGuardSpawn(String mapName)              { return loadLocation(mapName, "guardspawn"); }

    public void addAnimatronicSpawn(String mapName, Location loc) {
        int count = getAnimatronicSpawnCount(mapName);
        saveLocation(mapName, "animatronic-spawns." + (count + 1), loc);
    }

    public int getAnimatronicSpawnCount(String mapName) {
        ConfigurationSection section = plugin.getConfig()
                .getConfigurationSection("maps." + mapName + ".animatronic-spawns");
        return section == null ? 0 : section.getKeys(false).size();
    }

    public Location getAnimatronicSpawn(String mapName, int index) {
        return loadLocation(mapName, "animatronic-spawns." + index);
    }

    public List<Location> getAllAnimatronicSpawns(String mapName) {
        List<Location> spawns = new ArrayList<>();
        int count = getAnimatronicSpawnCount(mapName);
        for (int i = 1; i <= count; i++) {
            Location loc = getAnimatronicSpawn(mapName, i);
            if (loc != null) spawns.add(loc);
        }
        return spawns;
    }

    public void addCamera(String mapName, String cameraName, Location loc) {
        saveLocation(mapName, "cameras." + cameraName, loc);
    }

    public Location getCameraLocation(String mapName, String cameraName) {
        return loadLocation(mapName, "cameras." + cameraName);
    }

    public List<String> getCameraNames(String mapName) {
        List<String> names = new ArrayList<>();
        ConfigurationSection section = plugin.getConfig()
                .getConfigurationSection("maps." + mapName + ".cameras");
        if (section != null) names.addAll(section.getKeys(false));
        return names;
    }

    public boolean isMapReady(String mapName) {
        return mapExists(mapName)
                && getLobbySpawn(mapName) != null
                && getGuardSpawn(mapName) != null
                && getAnimatronicSpawnCount(mapName) > 0;
    }

    public String getMapStatus(String mapName) {
        if (!mapExists(mapName)) return "§cNo existe";
        StringBuilder sb = new StringBuilder();
        sb.append(getLobbySpawn(mapName) != null    ? "§a✔ §7Lobby  "     : "§c✘ §7Lobby  ");
        sb.append(getGuardSpawn(mapName) != null    ? "§a✔ §7GuardSpawn  " : "§c✘ §7GuardSpawn  ");
        sb.append(getAnimatronicSpawnCount(mapName) > 0
                ? "§a✔ §7AnimSpawns(" + getAnimatronicSpawnCount(mapName) + ")  "
                : "§c✘ §7AnimSpawns  ");
        sb.append(getCameraNames(mapName).size() > 0
                ? "§a✔ §7Cameras(" + getCameraNames(mapName).size() + ")"
                : "§c✘ §7Cameras");
        return sb.toString();
    }
}