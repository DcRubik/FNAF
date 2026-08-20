package com.dcrubik.fnaf.scoreboard;

import com.dcrubik.fnaf.FNAF;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardHandler {

    private final FNAF plugin;
    private final Map<UUID, Scoreboard> playerScoreboards;
    private BukkitTask updateTask;

    // Unique blank entries using invisible color codes
    private static final String[] BLANKS = { "§0", "§0§0", "§0§0§0" };

    private static final String[] NIGHT_LABELS = {
            "Early Dusk 1st",
            "Midnight 2nd",
            "Late Night 3rd",
            "Dead Hours 4th",
            "Final Hour 5th"
    };

    private static final String[] CLOCK_HOURS = {
            "12:00am", "1:00am", "2:00am", "3:00am", "4:00am", "5:00am", "6:00am"
    };

    public ScoreboardHandler(FNAF plugin) {
        this.plugin = plugin;
        this.playerScoreboards = new HashMap<>();
        startUpdateTask();
    }

    private void startUpdateTask() {
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAllScoreboards, 0L, 20L);
    }

    public void updateAllScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateScoreboard(player);
        }
    }

    public void updateScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("fnaf", Criteria.DUMMY, "§e§lFNAF");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        boolean gameRunning = plugin.getGameManager().isGameRunning();
        int currentNight = plugin.getGameManager().getCurrentNight();
        int elapsed = plugin.getGameManager().getNightElapsedSeconds();
        int duration = plugin.getGameManager().getNightDurationSeconds();

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yy"));

        String nightLabel, clockTime, locationLine, purseLine;

        if (gameRunning && currentNight >= 1 && currentNight <= 5) {
            nightLabel = NIGHT_LABELS[currentNight - 1];
            int tickIndex = (duration > 0)
                    ? Math.min((int) ((elapsed / (double) duration) * (CLOCK_HOURS.length - 1)), CLOCK_HOURS.length - 1)
                    : 0;
            clockTime = CLOCK_HOURS[tickIndex];
            locationLine = "§aFreddy's Office";
            int remaining = Math.max(duration - elapsed, 0);
            int mins = remaining / 60;
            int secs = remaining % 60;
            purseLine = "§6" + String.format("%d:%02d", mins, secs) + " §7remaining";
        } else {
            nightLabel = "§7Waiting...";
            clockTime = "12:00am";
            locationLine = "§aLobby";
            purseLine = "§70";
        }

        String role;
        if (plugin.getGameManager().isGuard(player)) {
            role = "§bSecurity Guard";
        } else if (plugin.getGameManager().isAnimatronic(player)) {
            role = "§cAnimatronic";
        } else {
            role = "§7None";
        }

        line (scoreboard, objective, "§9", 10, "§7" + date + " §8" + player.getName());
        blank(scoreboard, objective, BLANKS[0], 9);
        line (scoreboard, objective, "§f", 8,  "§f " + nightLabel);
        line (scoreboard, objective, "§e", 7,  "§7 " + clockTime);
        line (scoreboard, objective, "§d", 6,  "§8\u25cf §r" + locationLine);
        blank(scoreboard, objective, BLANKS[1], 5);
        line (scoreboard, objective, "§c", 4,  "§fRole: " + role);
        line (scoreboard, objective, "§b", 3,  "§fPurse: " + (gameRunning ? purseLine : "§60"));
        blank(scoreboard, objective, BLANKS[2], 2);
        line (scoreboard, objective, "§a", 1,  "§ewww.dcrubik.net");

        player.setScoreboard(scoreboard);
        playerScoreboards.put(player.getUniqueId(), scoreboard);
    }

    private void line(Scoreboard sb, Objective obj, String entry, int score, String prefix) {
        Team team = sb.registerNewTeam("ln" + score);
        team.addEntry(entry);
        team.setPrefix(prefix);
        obj.getScore(entry).setScore(score);
    }

    private void blank(Scoreboard sb, Objective obj, String entry, int score) {
        Team team = sb.registerNewTeam("ln" + score);
        team.addEntry(entry);
        team.setPrefix("");
        obj.getScore(entry).setScore(score);
    }

    public void removeScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        player.setScoreboard(manager.getMainScoreboard());
        playerScoreboards.remove(player.getUniqueId());
    }

    public void removeAllScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeScoreboard(player);
        }
        playerScoreboards.clear();
    }

    public void stopUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }
}