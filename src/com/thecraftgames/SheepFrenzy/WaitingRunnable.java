package com.thecraftgames.SheepFrenzy;

import com.thecraftgames.core.Util;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class WaitingRunnable implements Runnable {
    private int id;
    private int i = 120;
    private SheepFrenzy main;

    public WaitingRunnable(SheepFrenzy instance) {
        this.main = instance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void cancel() {
        for (Player p : main.getServer().getOnlinePlayers()) {
            p.setLevel(0);
        }
        main.wr = null;
        main.getServer().getScheduler().cancelTask(id);
    }

    public void setTime(int i) {
        this.i = i;
    }

    public int getTime() {
        return i;
    }

    @Override
    public void run() {
        for (Player p : main.getServer().getOnlinePlayers()) {
            p.setLevel(i);
        }
        if (i == 0) {
            Util.log("[Waiting Finished]");
            main.startGame();
            cancel();
        } else if (i == 10) {
            main.getServer().broadcastMessage(ChatColor.GOLD + "» 10 seconds left!");
        } else if (i == 30) {
            main.getServer().broadcastMessage(ChatColor.GOLD + "» 30 seconds left!");
        } else if (i == 60) {
            main.getServer().broadcastMessage(ChatColor.GOLD + "» 1 minute left!");
        }
        i--;
    }


}
