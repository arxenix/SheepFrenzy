package com.thecraftgames.SheepFrenzy;

import com.thecraftgames.SheepFrenzy.SheepFrenzy.GamePhase;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpawnerRunnable implements Runnable {
    SheepFrenzy main;

    public SpawnerRunnable(SheepFrenzy instance) {
        this.main = instance;
    }

    @Override
    public void run() {
        if (main.phase == GamePhase.COUNTINGDOWN || main.phase == GamePhase.STARTED || main.phase == GamePhase.FIGHT) {
            Location l1 = main.bounds[0];
            Location l2 = main.bounds[1];
            /*
             * 				Entity sheepEntity = e.getLocation().getWorld().spawnEntity(e.getLocation(), EntityType.SHEEP);
				Sheep sheep = (Sheep) sheepEntity;
				sheep.setColor(DyeColor.getByWoolData(colors[r.nextInt(colors.length)]));
				sheep.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
				sheep.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 255));
			 */

            for (int i = 0; i < 10; i++) {
                int spawnx = main.r.nextInt(l2.getBlockX());
                int spawnz = main.r.nextInt(l2.getBlockZ());
                int spawny = main.r.nextInt(main.r.nextInt(l2.getBlockY() - l1.getBlockY() + 1) + l1.getBlockY());

                Location toSpawn = new Location(l1.getWorld(), spawnx, spawny, spawnz);
                if (toSpawn.getBlock().getTypeId() == 0) {
                    if (toSpawn.getBlock().getRelative(0, -1, 0).getTypeId() != 0) {
                        //spawn!
                        Entity sheepEntity = toSpawn.getWorld().spawnEntity(toSpawn, EntityType.SHEEP);
                        Sheep sheep = (Sheep) sheepEntity;
                        sheep.setColor(DyeColor.getByWoolData(main.colors[main.r.nextInt(main.colors.length)]));
                        sheep.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
                        sheep.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 255));
                    }
                }
            }
        }
    }
}
