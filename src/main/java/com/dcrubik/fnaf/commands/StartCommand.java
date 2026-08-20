package com.dcrubik.fnaf.commands;

import com.dcrubik.fnaf.FNAF;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommand implements CommandExecutor {

    private final FNAF plugin;

    public StartCommand(FNAF plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!player.hasPermission("fnaf.start")) {
            player.sendMessage("§cYou don't have permission to start the game.");
            return true;
        }

        if (plugin.getGameManager().isGameRunning()) {
            player.sendMessage("§cA game is already running! Use §4/fnaf stop §cto end it first.");
            return true;
        }

        if (plugin.getCameraManager().getGuardSpawn() == null) {
            player.sendMessage("§cGuard spawn is not set! Use §e/fnaf setup guardspawn§c.");
            return true;
        }

        if (plugin.getCameraManager().getAnimatronicSpawn() == null) {
            player.sendMessage("§cAnimatronic spawn is not set! Use §e/fnaf setup animatronicspawn§c.");
            return true;
        }

        plugin.getGameManager().startGame();
        player.sendMessage("§aGame started! Night 1 begins now.");
        return true;
    }
}
