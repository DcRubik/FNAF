package com.dcrubik.fnaf.commands;

import com.dcrubik.fnaf.FNAF;
import com.dcrubik.fnaf.managers.CameraManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetupCommand implements CommandExecutor {

    private final FNAF plugin;

    public SetupCommand(FNAF plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§e§lFNAF §7» §cOnly players can use this command.");
            return true;
        }

        if (!player.hasPermission("fnaf.setup")) {
            player.sendMessage("§e§lFNAF §7» §cYou don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setup" -> handleSetup(player, args);
            case "stop" -> handleStop(player);
            case "guard" -> handleGuard(player);
            case "animatronic" -> handleAnimatronic(player);
            case "reload" -> handleReload(player);
            default -> sendHelp(player);
        }

        return true;
    }

    private void handleSetup(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /fnaf setup [lobby|guardspawn|animatronicspawn|addcamera <name>]");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "lobby" -> {
                plugin.getCameraManager().setLobbySpawn(player.getLocation());
                player.sendMessage("§aLobby spawn saved!");
            }
            case "guardspawn" -> {
                plugin.getCameraManager().setGuardSpawn(player.getLocation());
                player.sendMessage("§aGuard spawn saved!");
            }
            case "animatronicspawn" -> {
                plugin.getCameraManager().setAnimatronicSpawn(player.getLocation());
                player.sendMessage("§aAnimatronic spawn saved!");
            }
            case "addcamera" -> {
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /fnaf setup addcamera <name>");
                    return;
                }
                String cameraName = args[2];
                plugin.getCameraManager().addCamera(cameraName, player.getLocation());
                player.sendMessage("§aCamera §b" + cameraName + " §asaved! Total: §f"
                    + plugin.getCameraManager().getCameraNames().size());
            }
            default -> player.sendMessage("§cUnknown option. Use: lobby, guardspawn, animatronicspawn, addcamera <name>");
        }
    }

    private void handleStop(Player player) {
        if (!plugin.getGameManager().isGameRunning()) {
            player.sendMessage("§cNo game is currently running!");
            return;
        }
        plugin.getGameManager().endGame();
        player.sendMessage("§cGame forcefully stopped.");
    }

    private void handleGuard(Player player) {
        plugin.getGameManager().addToGuardTeam(player);
    }

    private void handleAnimatronic(Player player) {
        plugin.getGameManager().addToAnimatronicTeam(player);
    }

    private void handleReload(Player player) {
        plugin.reloadConfig();
        plugin.getCameraManager().reloadLocations();
        player.sendMessage("§aConfiguration reloaded!");
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬ FNAF by DcRubik Commands ▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("§e/start §7- Start a new game");
        player.sendMessage("§e/fnaf stop §7- Force stop the current game");
        player.sendMessage("§e/fnaf guard §7- Assign yourself as Guard");
        player.sendMessage("§e/fnaf animatronic §7- Assign yourself as Animatronic");
        player.sendMessage("§e/fnaf setup lobby §7- Set lobby spawn here");
        player.sendMessage("§e/fnaf setup guardspawn §7- Set guard spawn here");
        player.sendMessage("§e/fnaf setup animatronicspawn §7- Set animatronic spawn here");
        player.sendMessage("§e/fnaf setup addcamera <name> §7- Add camera here");
        player.sendMessage("§e/fnaf reload §7- Reload configuration files");
        player.sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }
}
