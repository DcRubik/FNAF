package com.dcrubik.fnaf.listeners;

import com.dcrubik.fnaf.FNAF;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Signs format:
 * Line 0: [FNAF]
 * Line 1: <mapName>
 * Line 2: (optional text)
 * Line 3: (optional text)
 *
 * Right-click to join that map's lobby.
 */
public class SignListener implements Listener {

    private final FNAF plugin;

    public SignListener(FNAF plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignCreate(SignChangeEvent event) {
        if (!event.getLine(0).equalsIgnoreCase("[FNAF]")) return;
        if (!event.getPlayer().hasPermission("fnaf.setup")) {
            event.getPlayer().sendMessage("§cNo tienes permiso para crear carteles FNAF.");
            return;
        }

        String mapName = event.getLine(1);
        if (mapName == null || mapName.isEmpty()) {
            event.getPlayer().sendMessage("§cEscribe el nombre del mapa en la línea 2.");
            return;
        }

        if (!plugin.getMapManager().mapExists(mapName)) {
            event.getPlayer().sendMessage("§cEl mapa §e" + mapName + " §cno existe. Créalo con §e/fnaf create " + mapName);
            return;
        }

        event.setLine(0, "§e§l[FNAF]");
        event.setLine(1, "§f" + mapName);
        event.setLine(2, plugin.getMapManager().isMapReady(mapName) ? "§aClick para unirte" : "§cIncompleto");
        event.getPlayer().sendMessage("§aCartel FNAF creado para el mapa §e" + mapName + "§a!");
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof Sign sign)) return;

        String line0 = sign.getLine(0).replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim();
        if (!line0.equalsIgnoreCase("[FNAF]")) return;

        event.setCancelled(true);

        String mapName = sign.getLine(1).replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim();
        Player player = event.getPlayer();

        if (!plugin.getMapManager().mapExists(mapName)) {
            player.sendMessage("§cEste cartel apunta a un mapa que no existe: §e" + mapName);
            return;
        }

        if (!plugin.getMapManager().isMapReady(mapName)) {
            player.sendMessage("§cEste mapa no está listo todavía.");
            return;
        }

        if (plugin.getGameManager().isGameRunning()) {
            player.sendMessage("§cYa hay una partida en curso en §e" + plugin.getGameManager().getActiveMap() + "§c.");
            return;
        }

        var lobby = plugin.getMapManager().getLobbySpawn(mapName);
        if (lobby != null) player.teleport(lobby);

        player.sendMessage("§aHas entrado al mapa §e" + mapName + "§a.");
        player.sendMessage("§7Usa §e/fnaf guard §7o §e/fnaf animatronic §7para elegir equipo.");
    }
}