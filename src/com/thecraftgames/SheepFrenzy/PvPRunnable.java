package com.thecraftgames.SheepFrenzy;

import com.thecraftgames.SheepFrenzy.SheepFrenzy.GamePhase;
import com.thecraftgames.core.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class PvPRunnable implements Runnable {
    private int id;

    private SheepFrenzy main;

    public PvPRunnable(SheepFrenzy instance) {
        this.main = instance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void cancel() {
        main.pr = null;
        main.getServer().getScheduler().cancelTask(id);
    }

    @Override
    public void run() {
        Bukkit.getServer().broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "-- PvP Started!! --");
        main.phase = GamePhase.FIGHT;
        Util.log("[PvP Started]");
        cancel();
    }
}
