package com.thecraftgames.SheepFrenzy;

import com.thecraftgames.core.RequestJoinServerEvent;
import com.thecraftgames.core.Util;
import com.thecraftgames.craftcredits.CraftCredits;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SheepFrenzy extends JavaPlugin implements Listener {
    public List<String> players = new ArrayList<String>();
    public GamePhase phase = GamePhase.WAITING;

    public WaitingRunnable wr = null;
    public StartingRunnable sr = null;
    public PvPRunnable pr = null;

    Location[] bounds;
    byte[] colors = {0, 4, 5, 11, 14};
    ItemStack[] weaponDrops = new ItemStack[7];
    ItemStack[] itemDrops = new ItemStack[14];
    ItemStack[] armorDrops = new ItemStack[12];
    ItemStack[] allDrops;
    World w;
    Random r = new Random();

    String sizeString;
    public SheepConfig cfg;

    @Override
    public void onEnable() {
        cfg = new SheepConfig(this);
        cfg.load();
        this.sizeString = Integer.toString(cfg.size);
        w = Bukkit.getWorld("world");
        //w.setTicksPerAnimalSpawns(1);
        bounds = Util.pasteSchematic(new File(this.getDataFolder().getAbsolutePath() + File.separator + "arena.schematic"), w, 64);

        weaponDrops[0] = new ItemStack(Material.WOOD_SWORD, 1);
        weaponDrops[1] = new ItemStack(Material.WOOD_SWORD, 1);
        weaponDrops[2] = new ItemStack(Material.WOOD_SWORD, 1);
        weaponDrops[3] = new ItemStack(Material.STONE_SWORD, 1);
        weaponDrops[4] = new ItemStack(Material.ARROW, 3);
        weaponDrops[5] = new ItemStack(Material.BOW, 1);
        weaponDrops[6] = new ItemStack(Material.ARROW, 4);


        itemDrops[0] = new ItemStack(Material.APPLE, 3);
        itemDrops[1] = new ItemStack(Material.APPLE, 2);
        itemDrops[2] = new ItemStack(Material.BREAD, 2);
        itemDrops[3] = new ItemStack(Material.GOLD_INGOT, 1);
        itemDrops[4] = new ItemStack(Material.DIAMOND, 1);
        itemDrops[5] = new ItemStack(Material.IRON_INGOT, 1);
        itemDrops[6] = new ItemStack(Material.STICK, 3);
        itemDrops[7] = new ItemStack(Material.FLINT_AND_STEEL, 1);
        itemDrops[8] = new ItemStack(Material.BONE, 5);
        itemDrops[9] = new ItemStack(Material.BONE, 2);
        itemDrops[10] = new ItemStack(Material.COOKED_BEEF, 2);
        itemDrops[11] = new ItemStack(Material.COOKED_BEEF, 2);
        itemDrops[12] = new ItemStack(Material.STICK, 1);
        itemDrops[13] = new ItemStack(Material.GOLDEN_APPLE, 1);

        armorDrops[0] = new ItemStack(Material.LEATHER_BOOTS, 1);
        armorDrops[1] = colorize(Material.LEATHER_CHESTPLATE, Color.WHITE);
        armorDrops[2] = colorize(Material.LEATHER_LEGGINGS, Color.RED);
        armorDrops[3] = new ItemStack(Material.LEATHER_HELMET, 1);
        armorDrops[4] = colorize(Material.LEATHER_BOOTS, Color.AQUA);
        armorDrops[5] = colorize(Material.LEATHER_CHESTPLATE, Color.PURPLE);
        armorDrops[6] = colorize(Material.LEATHER_LEGGINGS, Color.GREEN);
        armorDrops[7] = colorize(Material.LEATHER_HELMET, Color.BLACK);
        armorDrops[8] = new ItemStack(Material.CHAINMAIL_BOOTS, 1);
        armorDrops[9] = new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1);
        armorDrops[10] = new ItemStack(Material.CHAINMAIL_LEGGINGS, 1);
        armorDrops[11] = new ItemStack(Material.CHAINMAIL_HELMET, 1);
        allDrops = ArrayUtils.addAll(ArrayUtils.addAll(itemDrops, weaponDrops), armorDrops);
        getServer().getPluginManager().registerEvents(this, this);

        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                String amount = Integer.toString(players.size());
                Util.setGameSigns(new String[]{"&a&l[Join]", "&a" + Util.getName() + "&e- " + amount + "/" + sizeString, "&bWaiting for", "&bplayers..."});
            }
        }, 100L);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, new SpawnerRunnable(this), 20L, 1L);


        for (Entity e : getServer().getWorld("world").getEntities()) {
            if ((e instanceof Item) || e instanceof Sheep) {
                e.remove();
            }
        }
    }

    public void onDisable() {
        Util.setGameSigns(new String[]{"&4[Server Down]", "✦✦" + Util.getName() + "✦✦", "&8Under", "&8maintenance!"});
    }

    @EventHandler
    public void onShear(PlayerShearEntityEvent e) {
        if (Util.isSpectating(e.getPlayer())) {
            e.setCancelled(true);
            return;
        }
        if (phase == GamePhase.STARTED || phase == GamePhase.FIGHT || phase == GamePhase.COUNTINGDOWN) {
            Sheep sheep = (Sheep) e.getEntity();
            if (r.nextInt(8) == 1) {
                sheep.remove();
                TNTPrimed tnt = (TNTPrimed) sheep.getWorld().spawnEntity(sheep.getLocation().add(0, 1, 0), EntityType.PRIMED_TNT);
                tnt.setFuseTicks(30);
            } else {
                ItemStack itemToDrop = new ItemStack(Material.GRASS, 1);
                switch (sheep.getColor().getWoolData()) {
                    case 0:
                        itemToDrop = allDrops[r.nextInt(allDrops.length - 1)];
                        break;
                    case 4:
                        sheep.getWorld().spawnEntity(sheep.getLocation(), EntityType.THROWN_EXP_BOTTLE);
                        return;
                    case 5:
                        itemToDrop = itemDrops[r.nextInt(itemDrops.length - 1)];
                        break;
                    case 11:
                        itemToDrop = armorDrops[r.nextInt(armorDrops.length - 1)];
                        break;
                    case 14:
                        itemToDrop = weaponDrops[r.nextInt(weaponDrops.length - 1)];
                        break;
                }
                sheep.getWorld().dropItemNaturally(sheep.getLocation(), itemToDrop);
            }
        } else {
            e.getPlayer().sendMessage(ChatColor.RED + "Please wait until the game starts!");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamageEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            if (e.getDamager() instanceof Player) {
                if (Util.isSpectating((Player) e.getDamager())) {
                    e.setCancelled(true);
                    return;
                }
            }
            if (phase != GamePhase.FIGHT) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (Util.isSpectating(p)) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        Util.unspectatePlayer(p);
        p.setLevel(0);
        p.getInventory().clear();
        p.getInventory().setArmorContents(new ItemStack[4]);
        players.remove(p.getName());
        String amount = Integer.toString(players.size());
        if (phase == GamePhase.WAITING) {
            getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + e.getPlayer().getName() + ChatColor.GOLD + " has left the game!" + ChatColor.GREEN + " (" + amount + "/" + sizeString + ")");
            Util.setGameSigns(new String[]{"&a&l[Join]", "&a" + Util.getName() + "&e- " + amount + "/" + sizeString, "&bWaiting for", "&bplayers..."});
            if (players.size() == 1) {
                if (wr != null) {
                    wr.cancel();
                }
            }
        } else {
            p.teleport(Util.stringToLoc(cfg.respawnLocation));
            getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + e.getPlayer().getName() + ChatColor.GOLD + " has forfeit!" + ChatColor.GREEN + " (" + amount + "/" + sizeString + " remain)");
            checkWin();
        }
    }

    public boolean checkWin() {
        if (phase == GamePhase.FIGHT || phase == GamePhase.COUNTINGDOWN || phase == GamePhase.STARTED) {
            if (players.size() == 1) {
                String name = players.get(0);
                Player winner = Bukkit.getPlayer(name);
                winner.playEffect(winner.getLocation(), Effect.POTION_BREAK, 0);
                getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + name + ChatColor.GREEN + " has won the SheepFrenzy!");
                CraftCredits.addCredits(name, cfg.credits);
                //CraftCredits.setCredits(name, CraftCredits.getCredits(name)+10);

                Util.setGameSigns(new String[]{"&f&l[Restarting]", "▇▇" + Util.getName() + "▇▇", "▓▓▓▓▓▓▓", "▒▒▒▒▒▒▒"});

                if (sr != null) {
                    Util.log("[Countdown Cancelled]");
                    sr.cancel();
                }
                if (pr != null) {
                    Util.log("[PvP Cancelled]");
                    pr.cancel();
                }
                Util.setGameSigns(new String[]{"&f&l[Restarting]", "▇▇" + Util.getName() + "▇▇", "▓▓▓▓▓▓▓", "▒▒▒▒▒▒▒"});


                phase = GamePhase.RESTARTING;
                Util.log("[Game Restarting]");
                getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                    @Override
                    public void run() {
                        Util.log("[Sending Players to Hub]");

                        for (Player on : getServer().getOnlinePlayers()) {
                            Util.redirectRequest("hub", on);
                        }

                        //clear drops
                        for (Entity e : w.getEntities()) {
                            if (e instanceof Item) {
                                e.remove();
                            } else if (e instanceof Arrow) {
                                e.remove();
                            } else if (e instanceof Sheep) {
                                e.remove();
                            }
                        }
                        bounds = Util.pasteSchematic(new File(getDataFolder().getAbsolutePath() + File.separator + "arena.schematic"), w, 64);


                    }
                }, 200L);
                getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                    @Override
                    public void run() {
                        String amount = Integer.toString(players.size());
                        Util.setGameSigns(new String[]{"&a&l[Join]", "&a" + Util.getName() + "&e- " + amount + "/" + sizeString, "&bWaiting for", "&bplayers..."});

                        Util.log("[New Game Started]");
                        phase = GamePhase.WAITING;
                    }
                }, 800L);
                return true;
            } else {
                String amount = Integer.toString(players.size());
                Util.setGameSigns(new String[]{"&4[In Progress]", "&a" + Util.getName() + "&6- " + amount + "/" + sizeString, "&9Wait until", "&9the next game"});
                return false;
            }
        }
        return false;
    }

    @EventHandler
    public void onKill(EntityDeathEvent e) {
        if (phase == GamePhase.WAITING) return;
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            players.remove(p.getName());
            String amount = Integer.toString(players.size());
            getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + p.getName() + ChatColor.GOLD + " was slain! " + ChatColor.GREEN + "(" + amount + "/" + sizeString + " remain)");
            if (!checkWin()) {
                Util.spectatePlayer(p);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent e) {
        e.setRespawnLocation(Util.stringToLoc(cfg.respawnLocation));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.setAllowFlight(false);
        p.setFlying(false);

        p.getInventory().clear();
        p.getInventory().setArmorContents(new ItemStack[4]);


        ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta bm = (BookMeta) book.getItemMeta();
        bm.setAuthor(cfg.bookAuthor);
        bm.setTitle(cfg.bookName);
        bm.setLore(cfg.bookPages);
        book.setItemMeta(bm);
        p.getInventory().addItem(book);


        for (PotionEffectType type : PotionEffectType.values()) {
            if (type != null) {
                if (p.hasPotionEffect(type)) {
                    p.removePotionEffect(type);
                }
            }
        }
        p.setHealth(20.0);
        p.setFoodLevel(20);


        //if the game is not started
        if (phase == GamePhase.WAITING) {
            //teleport to spawn and add them to player list
            p.teleport(Util.stringToLoc(cfg.respawnLocation));
            players.add(p.getName());
            String amount = Integer.toString(players.size());
            getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + p.getName() + ChatColor.GOLD + " has joined!" + ChatColor.GREEN + " (" + amount + "/" + sizeString + ")");

            if (players.size() == 2) {
                //start countdown
                wr = new WaitingRunnable(this);
                wr.setId(getServer().getScheduler().scheduleSyncRepeatingTask(this, wr, 20L, 20L));
            }
            if (players.size() >= cfg.size) {
                //shorten countdown
                if (wr.getTime() > 30) {
                    wr.setTime(30);
                }
                Util.setGameSigns(new String[]{"&4&l[Full]", "&a" + Util.getName() + "&e- " + amount + "/" + sizeString, "&3Starting", "&3soon..."});
            } else {
                Util.setGameSigns(new String[]{"&a&l[Join]", "&a" + Util.getName() + "&e- " + amount + "/" + sizeString, "&bWaiting for", "&bplayers..."});
            }
        } else {
            //put them in spectate mode
            Util.spectatePlayer(p);
        }
    }

    public void startGame() {
        if (wr != null) {
            wr.cancel();
        }
        //start game!
        getServer().broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Game Starting!");
        phase = GamePhase.COUNTINGDOWN;
        String amount = Integer.toString(players.size());
        Util.setGameSigns(new String[]{"&4[In Progress]", "&a" + Util.getName() + "&6- " + amount + "/" + sizeString, "&9Wait until", "&9the next game"});

        ItemStack shears = new ItemStack(Material.SHEARS, 1);
        shears.addUnsafeEnchantment(Enchantment.DURABILITY, 10);

        Collections.shuffle(cfg.spawnLocations);
        cfg.save();
        for (int i = 0; i < players.size(); i++) {
            String pn = players.get(i);
            Player p = Bukkit.getPlayer(pn);
            p.teleport(Util.stringToLoc(cfg.spawnLocations.get(i)));
            p.getInventory().clear();
            p.getInventory().addItem(shears);
        }

        sr = new StartingRunnable(this);
        sr.setId(getServer().getScheduler().scheduleSyncRepeatingTask(this, sr, 20L, 20L));

        pr = new PvPRunnable(this);
        pr.setId(getServer().getScheduler().scheduleSyncDelayedTask(this, pr, 2400L));
    }


    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (Util.isSpectating(e.getPlayer())) {
            return;
        }
        if (phase == GamePhase.COUNTINGDOWN) {
            if (((e.getTo().getX() != e.getFrom().getX()) || (e.getTo().getZ() != e.getFrom().getZ()) || (e.getTo().getY() != e.getFrom().getY()))) {
                e.setTo(e.getFrom());
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if ((phase != GamePhase.STARTED) && (phase != GamePhase.FIGHT)) {
            e.setCancelled(true);
        } else {
            if (!players.contains(e.getPlayer().getName())) {
                e.setCancelled(true);
            } else {
                if (e.getBlock().getType() != Material.WOOL) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        if (e.getEntity() instanceof TNTPrimed) {
            e.blockList().clear();
        }
    }


    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (Util.isSpectating(e.getPlayer())) {
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onRequestJoin(RequestJoinServerEvent e) {
        Util.log("> Recieved join Request");
        if (phase == GamePhase.WAITING) {
            if (players.size() < cfg.size) {
                e.setAllowed(true);
                e.setReason("&aConnecting to SheepFrenzy...");
            } else {
                if (e.isVIP()) {
                    e.setAllowed(true);
                    e.setReason("&aConnecting to SheepFrenzy...");
                    String toKick = players.get(r.nextInt(players.size()));
                    players.remove(toKick);
                    Bukkit.getPlayer(toKick).kickPlayer(ChatColor.RED + "You have been kicked out by a donor!");
                } else {
                    e.setAllowed(false);
                    e.setReason("&4Game is full!");
                }
            }
        } else if (phase != GamePhase.RESTARTING) {
            //spectate??
            if (getServer().getOnlinePlayers().length >= getServer().getMaxPlayers()) {
                e.setAllowed(false);
                e.setReason("&4This server is full!");
            } else {
                e.setAllowed(true);
                e.setReason("&eSpectating game...");
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
                             String[] args) {
        if (cmd.getName().equalsIgnoreCase("setgamespawn")) {
            if (!sender.isOp()) return true;
            if (sender instanceof Player) {
                Player p = (Player) sender;
                cfg.spawnLocations.add(Util.locToString(p.getLocation()));
                cfg.save();
                p.sendMessage(ChatColor.GREEN + "Set a spawn Location");
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("setrespawn")) {
            if (!sender.isOp()) return true;
            if (sender instanceof Player) {
                Player p = (Player) sender;
                cfg.respawnLocation = Util.locToString(p.getLocation());
                cfg.save();
                p.sendMessage(ChatColor.GREEN + "Set Respawn Location");
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("clearspawns")) {
            if (!sender.isOp()) return true;
            if (sender instanceof Player) {
                Player p = (Player) sender;
                cfg.spawnLocations = new ArrayList<String>();
                cfg.save();
                p.sendMessage(ChatColor.GREEN + "Cleared Spawn locations");
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("forcestart")) {
            if (!sender.isOp()) return true;
            startGame();
            sender.sendMessage(ChatColor.GREEN + "Force Started game!");
            return true;
        } else if (cmd.getName().equalsIgnoreCase("setbook")) {
            if (!sender.isOp()) return true;
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (p.getItemInHand().getType() == Material.WRITTEN_BOOK) {
                    BookMeta bm = (BookMeta) p.getItemInHand().getItemMeta();
                    cfg.bookAuthor = bm.getAuthor();
                    cfg.bookPages = bm.getPages();
                    cfg.bookName = bm.getTitle();
                    cfg.save();
                }
            }
        }
        return false;
    }

    public enum GamePhase {
        COUNTINGDOWN,
        STARTED,
        FIGHT,
        RESTARTING,
        WAITING;
    }

    public ItemStack colorize(Material m, Color c) {
        ItemStack i = new ItemStack(m, 1);
        LeatherArmorMeta im = (LeatherArmorMeta) i.getItemMeta();
        im.setColor(c);
        i.setItemMeta(im);
        return i;
    }
}
