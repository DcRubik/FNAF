package com.dcrubik.fnaf.managers;

import com.dcrubik.fnaf.FNAF;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameManager {

    private final FNAF plugin;
    private String activeMap;
    private int currentNight;
    private boolean gameRunning;
    private BukkitTask nightTimer;
    private long nightStartTime;
    private final List<UUID> guardTeam = new ArrayList<>();
    private final List<UUID> animatronicTeam = new ArrayList<>();
    private static final int MAX_NIGHTS = 5;

    private static final int ANIMATRONIC_SLOWNESS = 1;

    public GameManager(FNAF plugin) {
        this.plugin = plugin;
    }

    public void startGame(String mapName) {
        if (gameRunning) return;
        if (!plugin.getMapManager().isMapReady(mapName)) {
            Bukkit.broadcastMessage("§cEl mapa §e" + mapName + " §cno está listo para jugar.");
            return;
        }
        activeMap = mapName;
        gameRunning = true;
        currentNight = 0;
        startNextNight();
    }

    public void startNextNight() {
        if (currentNight >= MAX_NIGHTS) { endGame(); return; }
        currentNight++;
        nightStartTime = System.currentTimeMillis();
        broadcastNightTitle();
        spawnPlayers();
        applyRoleEffects();
        plugin.getScoreboardHandler().updateAllScoreboards();

        int nightDuration = plugin.getConfig().getInt("settings.night-duration", 300) * 20;
        nightTimer = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (currentNight < MAX_NIGHTS) {
                Bukkit.broadcastMessage("§aNoche " + currentNight + " completada! ¡Los guardias sobreviven!");
                startNextNight();
            } else {
                Bukkit.broadcastMessage("§a¡5 noches superadas! §6¡Ganan los guardias!");
                endGame();
            }
        }, nightDuration);
    }

    private void broadcastNightTitle() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle("§c§lNoche " + currentNight, "§7¡Buena suerte!", 10, 70, 20);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.5f);
        }
        Bukkit.broadcastMessage("§cNoche §6" + currentNight + " §7ha comenzado!");
    }

    private void spawnPlayers() {
        Location guardSpawn = plugin.getMapManager().getGuardSpawn(activeMap);
        List<Location> animSpawns = plugin.getMapManager().getAllAnimatronicSpawns(activeMap);

        for (UUID uuid : guardTeam) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && guardSpawn != null) {
                p.teleport(guardSpawn);
                giveGuardItems(p);
            }
        }

        for (int i = 0; i < animatronicTeam.size(); i++) {
            Player p = Bukkit.getPlayer(animatronicTeam.get(i));
            if (p == null || animSpawns.isEmpty()) continue;
            Location spawn = animSpawns.get(i % animSpawns.size());
            p.teleport(spawn);
        }
    }

    private void giveGuardItems(Player player) {
        player.getInventory().clear();
        ItemStack cameraItem = new ItemStack(Material.ENDER_EYE);
        ItemMeta meta = cameraItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b§lTablet de Cámaras");
            List<String> lore = new ArrayList<>();
            lore.add("§7Click derecho para ciclar cámaras");
            lore.add("§7Shift para salir de la cámara");
            lore.add("§8Cámaras: §f" + plugin.getMapManager().getCameraNames(activeMap).size());
            meta.setLore(lore);
            cameraItem.setItemMeta(meta);
        }
        player.getInventory().setItem(0, cameraItem);
    }

    private void applyRoleEffects() {
        for (UUID uuid : animatronicTeam) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) applySlowness(p);
        }
    }

    public void endGame() {
        if (!gameRunning && currentNight == 0) return;
        gameRunning = false;
        if (nightTimer != null) { nightTimer.cancel(); nightTimer = null; }

        clearEffects();
        plugin.getScoreboardHandler().updateAllScoreboards();
        Bukkit.broadcastMessage("§cJuego terminado tras §6" + currentNight + " §7noche(s).");

        Location lobby = activeMap != null ? plugin.getMapManager().getLobbySpawn(activeMap) : null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getCameraManager().resetPlayer(player.getUniqueId());
            player.getInventory().clear();
            if (lobby != null) player.teleport(lobby);
        }

        currentNight = 0;
        nightStartTime = 0;
        guardTeam.clear();
        animatronicTeam.clear();
        activeMap = null;
    }

    private void clearEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.removePotionEffect(PotionEffectType.SLOWNESS);
        }
    }


    public void addToGuardTeam(Player player) {
        UUID uuid = player.getUniqueId();
        animatronicTeam.remove(uuid);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        if (!guardTeam.contains(uuid)) guardTeam.add(uuid);
        player.sendMessage("§aEres un §bGuardia de Seguridad§a!");
        plugin.getScoreboardHandler().updateScoreboard(player);
    }

    public void addToAnimatronicTeam(Player player) {
        UUID uuid = player.getUniqueId();
        guardTeam.remove(uuid);
        if (!animatronicTeam.contains(uuid)) animatronicTeam.add(uuid);
        applySlowness(player);
        player.sendMessage("§aEres un §cAnimatrónico§a! No puedes correr.");
        plugin.getScoreboardHandler().updateScoreboard(player);
    }

    public void applySlowness(Player player) {
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS,
                Integer.MAX_VALUE,
                ANIMATRONIC_SLOWNESS,
                false, false, true
        ));
    }

    public boolean isGuard(Player player)       { return guardTeam.contains(player.getUniqueId()); }
    public boolean isAnimatronic(Player player) { return animatronicTeam.contains(player.getUniqueId()); }
    public int getCurrentNight()                { return currentNight; }
    public boolean isGameRunning()              { return gameRunning; }
    public String getActiveMap()                { return activeMap; }
    public List<UUID> getGuardTeam()            { return guardTeam; }
    public List<UUID> getAnimatronicTeam()      { return animatronicTeam; }

    public int getNightElapsedSeconds() {
        if (!gameRunning || nightStartTime == 0) return 0;
        return (int) ((System.currentTimeMillis() - nightStartTime) / 1000L);
    }

    public int getNightDurationSeconds() {
        return plugin.getConfig().getInt("settings.night-duration", 300);
    }
}