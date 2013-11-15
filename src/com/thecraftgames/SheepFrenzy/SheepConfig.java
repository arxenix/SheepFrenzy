package com.thecraftgames.SheepFrenzy;

import com.thecraftgames.craftcredits.Config;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class SheepConfig extends Config {
    public SheepConfig(SheepFrenzy main) {
        this.setFile(main);
    }

    public List<String> spawnLocations = new ArrayList<String>();
    public String respawnLocation = "";
    public int size = 8;
    public int credits = 10;
    public String bookName = ChatColor.RED + "" + ChatColor.BOLD + "SheepFrenzy Info";
    public String bookAuthor = ChatColor.DARK_AQUA + "TheCraftGames";
    public List<String> bookPages = new ArrayList<String>();
}
