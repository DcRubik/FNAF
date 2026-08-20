package com.dcrubik.fnaf.menu;

import com.dcrubik.fnaf.FNAF;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MapMenu {

    private static final String TITLE = "§e§lFNAF §8- §7Selecciona un mapa";

    public static void open(Player player, FNAF plugin) {
        List<String> maps = plugin.getMapManager().getMapNames();
        int size = Math.max(9, ((maps.size() / 9) + 1) * 9);
        Inventory inv = Bukkit.createInventory(null, Math.min(size, 54), TITLE);

        for (int i = 0; i < maps.size() && i < 54; i++) {
            String mapName = maps.get(i);
            boolean ready = plugin.getMapManager().isMapReady(mapName);

            ItemStack item = new ItemStack(ready ? Material.LIME_WOOL : Material.RED_WOOL);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName((ready ? "§a" : "§c") + mapName);
                List<String> lore = new ArrayList<>();
                lore.add("§8────────────────");
                lore.add(plugin.getMapManager().getMapStatus(mapName));
                lore.add("§8────────────────");
                if (ready) {
                    lore.add("§eClick para unirte");
                } else {
                    lore.add("§cMapa incompleto");
                    lore.add("§7Usa §e/fnaf setup <mapa> §7para completarlo");
                }
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(i, item);
        }

        if (maps.isEmpty()) {
            ItemStack empty = new ItemStack(Material.BARRIER);
            ItemMeta meta = empty.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§cNo hay mapas creados");
                List<String> lore = new ArrayList<>();
                lore.add("§7Usa §e/fnaf create <nombre> §7para crear uno");
                meta.setLore(lore);
                empty.setItemMeta(meta);
            }
            inv.setItem(4, empty);
        }

        player.openInventory(inv);
    }

    public static boolean isMapMenu(Inventory inv) {
        return inv.getType().name().equals("CHEST") &&
                TITLE.equals(inv.getViewers().stream()
                        .findFirst()
                        .map(e -> e.getOpenInventory().getTitle())
                        .orElse(""));
    }

    public static String getTitle() { return TITLE; }
}