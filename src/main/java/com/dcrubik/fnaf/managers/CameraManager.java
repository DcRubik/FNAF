package com.dcrubik.fnaf.managers;

import com.dcrubik.fnaf.FNAF;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

public class CameraManager {

    private final FNAF plugin;

    // Which camera index each player is on
    private final Map<UUID, Integer> playerCameraIndex = new HashMap<>();
    // Where the player was before entering camera mode
    private final Map<UUID, Location> originalLocations = new HashMap<>();
    // Players currently frozen on a camera
    private final Set<UUID> frozenPlayers = new HashSet<>();
    // Which map each player is viewing cameras for
    private final Map<UUID, String> playerMap = new HashMap<>();

    public CameraManager(FNAF plugin) {
        this.plugin = plugin;
        startFreezeTask();
    }

    private void startFreezeTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID uuid : Set.copyOf(frozenPlayers)) {
                Player p = Bukkit.getPlayer(uuid);
                if (p == null) { frozenPlayers.remove(uuid); continue; }

                String mapName = playerMap.get(uuid);
                if (mapName == null) continue;

                int idx = playerCameraIndex.getOrDefault(uuid, 0);
                List<String> cameras = plugin.getMapManager().getCameraNames(mapName);
                if (cameras.isEmpty() || idx >= cameras.size()) continue;

                Location camLoc = plugin.getMapManager().getCameraLocation(mapName, cameras.get(idx));
                if (camLoc == null) continue;

                if (p.getLocation().distanceSquared(camLoc) > 0.05) {
                    p.teleport(camLoc);
                }
            }
        }, 1L, 1L);
    }


    public void cycleCamera(Player player, String mapName) {
        List<String> cameras = plugin.getMapManager().getCameraNames(mapName);
        if (cameras.isEmpty()) {
            player.sendMessage("§cEste mapa no tiene cámaras configuradas.");
            return;
        }

        UUID uuid = player.getUniqueId();

        // First time entering camera mode — save original position
        if (!frozenPlayers.contains(uuid)) {
            originalLocations.put(uuid, player.getLocation().clone());
            frozenPlayers.add(uuid);
            playerMap.put(uuid, mapName);
        }

        int current = playerCameraIndex.getOrDefault(uuid, -1);
        int next = (current + 1) % cameras.size();
        playerCameraIndex.put(uuid, next);

        String cameraName = cameras.get(next);
        Location cameraLoc = plugin.getMapManager().getCameraLocation(mapName, cameraName);

        if (cameraLoc == null) {
            player.sendMessage("§cCámara '" + cameraName + "' no tiene ubicación guardada.");
            return;
        }

        player.teleport(cameraLoc);
        player.sendMessage("§b📹 Cámara: §f" + cameraName + " §7[" + (next + 1) + "/" + cameras.size() + "]");
    }

    public boolean exitCamera(Player player) {
        UUID uuid = player.getUniqueId();
        if (!frozenPlayers.contains(uuid)) return false;

        frozenPlayers.remove(uuid);
        playerCameraIndex.remove(uuid);
        playerMap.remove(uuid);

        Location origin = originalLocations.remove(uuid);
        if (origin != null) {
            player.teleport(origin);
            player.sendMessage("§7Has salido del modo cámara.");
        }
        return true;
    }

    public boolean isInCamera(UUID uuid) {
        return frozenPlayers.contains(uuid);
    }

    public void resetPlayer(UUID uuid) {
        frozenPlayers.remove(uuid);
        playerCameraIndex.remove(uuid);
        originalLocations.remove(uuid);
        playerMap.remove(uuid);
    }

    public Location getGuardSpawn() {
        String active = plugin.getGameManager().getActiveMap();
        if (active == null) return null;
        return plugin.getMapManager().getGuardSpawn(active);
    }

    public Location getLobbySpawn() {
        String active = plugin.getGameManager().getActiveMap();
        if (active == null) return null;
        return plugin.getMapManager().getLobbySpawn(active);
    }

    public List<String> getCameraNames() {
        String active = plugin.getGameManager().getActiveMap();
        if (active == null) return List.of();
        return plugin.getMapManager().getCameraNames(active);
    }
}