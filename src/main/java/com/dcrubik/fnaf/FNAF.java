package com.dcrubik.fnaf;

import com.dcrubik.fnaf.commands.SetupCommand;
import com.dcrubik.fnaf.commands.StartCommand;
import com.dcrubik.fnaf.listeners.AnimatronicListener;
import com.dcrubik.fnaf.listeners.MenuListener;
import com.dcrubik.fnaf.listeners.SignListener;
import com.dcrubik.fnaf.managers.CameraManager;
import com.dcrubik.fnaf.managers.GameManager;
import com.dcrubik.fnaf.managers.MapManager;
import com.dcrubik.fnaf.scoreboard.ScoreboardHandler;
import org.bukkit.plugin.java.JavaPlugin;

public class FNAF extends JavaPlugin {

    private static FNAF instance;
    private GameManager gameManager;
    private CameraManager cameraManager;
    private MapManager mapManager;
    private ScoreboardHandler scoreboardHandler;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        mapManager = new MapManager(this);
        gameManager = new GameManager(this);
        cameraManager = new CameraManager(this);
        scoreboardHandler = new ScoreboardHandler(this);

        getCommand("setup").setExecutor(new SetupCommand(this));
        getCommand("start").setExecutor(new StartCommand(this));

        getServer().getPluginManager().registerEvents(new AnimatronicListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new SignListener(this), this);

        getLogger().info("FNAF plugin enabled! Maps loaded: " + mapManager.getMapNames().size());
    }

    @Override
    public void onDisable() {
        if (gameManager != null) gameManager.endGame();
        if (scoreboardHandler != null) scoreboardHandler.stopUpdateTask();
        getLogger().info("FNAF plugin disabled.");
    }

    public static FNAF getInstance() { return instance; }
    public GameManager getGameManager() { return gameManager; }
    public CameraManager getCameraManager() { return cameraManager; }
    public MapManager getMapManager() { return mapManager; }
    public ScoreboardHandler getScoreboardHandler() { return scoreboardHandler; }
}