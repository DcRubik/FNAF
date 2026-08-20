package com.dcrubik.fnaf.listeners;

import com.dcrubik.fnaf.FNAF;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AnimatronicListener implements Listener {

    private final FNAF plugin;

    public AnimatronicListener(FNAF plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.getGameManager().isAnimatronic(player)) {
            applySlowness(player);
        }
        plugin.getScoreboardHandler().updateScoreboard(player);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getScoreboardHandler().removeScoreboard(player);
        plugin.getCameraManager().resetPlayer(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (plugin.getGameManager().isAnimatronic(player)) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> applySlowness(player), 1L);
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        if (!e.isSneaking()) return; // solo al agacharse, no al levantarse
        boolean wasInCamera = plugin.getCameraManager().exitCamera(e.getPlayer());
        if (wasInCamera) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.ENDER_EYE) {
            return;
        }

        if (!plugin.getGameManager().isGuard(player)) {
            return;
        }

        if (!plugin.getGameManager().isGameRunning()) {
            player.sendMessage("§cNo game is running. Use §e/start §cto begin!");
            return;
        }

        event.setCancelled(true);
        plugin.getCameraManager().cycleCamera(player, player.getName());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPotionSplash(PotionSplashEvent event) {
        for (var entry : event.getAffectedEntities()) {
            if (entry instanceof Player player && plugin.getGameManager().isAnimatronic(player)) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> applySlowness(player), 1L);
            }
        }
    }

    private void applySlowness(Player player) {
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.SLOWNESS,
            Integer.MAX_VALUE,
            1,
            false,
            false,
            true
        ));
    }
}
