package com.thecraftgames.SheepFrenzy;

import com.thecraftgames.SheepFrenzy.SheepFrenzy.GamePhase;
import com.thecraftgames.core.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class StartingRunnable implements Runnable {
    private int id;
    private int i = 10;
    private SheepFrenzy main;

    public StartingRunnable(SheepFrenzy instance) {
        this.main = instance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void cancel() {
        main.sr = null;
        main.getServer().getScheduler().cancelTask(id);
    }

    @Override
    public void run() {
        Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "[" + ChatColor.DARK_AQUA + "Sheep" + ChatColor.WHITE + "Frenzy" + ChatColor.GOLD + "] " + ChatColor.GREEN + "Game is starting in " + ChatColor.GRAY + Integer.toString(i) + " seconds");
        if (i == 0) {
            Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "Game Started!");
            Bukkit.getServer().broadcastMessage(ChatColor.BLUE + "Shear sheep to gain items, then when PVP starts, slaughter your oppenents! Beware though, some sheep are dangerous!");
            main.phase = GamePhase.STARTED;
            Util.log("[Countdown Finished]");
            cancel();
        }
        i--;
    }


}
