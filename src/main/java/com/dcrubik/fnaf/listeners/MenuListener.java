package com.dcrubik.fnaf.listeners;

import com.dcrubik.fnaf.FNAF;
import com.dcrubik.fnaf.menu.MapMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MenuListener implements Listener {

    private final FNAF plugin;

    public MenuListener(FNAF plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(MapMenu.getTitle())) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String displayName = meta.getDisplayName();
        // Strip color codes to get the map name
        String mapName = displayName.replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim();

        if (!plugin.getMapManager().mapExists(mapName)) return;
        if (!plugin.getMapManager().isMapReady(mapName)) {
            player.sendMessage("§cEste mapa no está listo todavía.");
            return;
        }

        player.closeInventory();
        joinMap(player, mapName);
    }

    private void joinMap(Player player, String mapName) {
        if (plugin.getGameManager().isGameRunning()) {
            player.sendMessage("§cYa hay una partida en curso en §e" + plugin.getGameManager().getActiveMap() + "§c.");
            return;
        }

        // Teleport to lobby of that map
        var lobby = plugin.getMapManager().getLobbySpawn(mapName);
        if (lobby != null) player.teleport(lobby);

        player.sendMessage("§aHas entrado al mapa §e" + mapName + "§a. Espera a que empiece la partida.");
        player.sendMessage("§7Usa §e/fnaf guard §7o §e/fnaf animatronic §7para elegir equipo.");
    }
}