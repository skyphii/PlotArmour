package com.skyphi.runnables;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;

public class HomingArrowRunnable extends BukkitRunnable {

    private Arrow arrow;
    private Entity target;

    public HomingArrowRunnable(Arrow arrow) {
        this.arrow = arrow;
    }
    public HomingArrowRunnable(Arrow arrow, Entity target) {
        this.arrow = arrow;
        this.target = target;
    }

    @Override
    public void run() {
        if(target == null) setTarget();

        // Block b = arrow.getAttachedBlock();
        // if(b != null) System.out.println(b.getType());
        // if(b != null && b.getType() == Material.IRON_BARS) b.breakNaturally();

        Block b = arrow.getLocation().getBlock();
        for(int y = -1; y <= 1; y++) {
            for(int x = -1; x <= 1; x++) {
                for(int z = -1; z <= 1; z++) {
                    Block b2 = b.getRelative(x, y, z);
                    if(b2.getType() == Material.IRON_BARS || b2.getType() == Material.OBSIDIAN || b2.getType() == Material.BEDROCK) {
                        b2.breakNaturally();
                    }
                }
            }
        }

        Vector newVector = target.getBoundingBox().getCenter().subtract(arrow.getLocation().toVector()).normalize();
        arrow.setVelocity(newVector);
    }

    private void setTarget() {
        List<Entity> crystals = arrow.getNearbyEntities(1000, 100, 1000);
        crystals.forEach(e -> {
            if(e.getType() == EntityType.ENDER_CRYSTAL) {
                target = e;
                arrow.setGlowing(true);
                return;
            }
        });

        if(target == null) cancel();
    }
    
}
