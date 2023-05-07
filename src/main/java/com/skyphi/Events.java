package com.skyphi;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Item;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.skyphi.runnables.HomingArrowRunnable;

public class Events implements Listener {

    private int taskPearl = -1, taskVoidLaunch = -1, taskPiglin = -1;
    private boolean fireFlying = false;
    private Vector enderEyeDirection;

    @EventHandler
    public void on(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        ItemStack item = event.getItem();
        if(item == null) return;
        Material itemType = item.getType();
        switch(itemType) {
            default:
                break;
            case WOLF_SPAWN_EGG:
                if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
                event.setCancelled(true);
                event.getItem().setAmount(event.getItem().getAmount()-1);
                Location wolfSpawn = event.getClickedBlock().getLocation().add(event.getBlockFace().getDirection());
                Wolf wolf = (Wolf)world.spawnEntity(wolfSpawn, EntityType.WOLF);
                wolf.setTamed(true);
                wolf.setOwner(player);
                wolf.setCollarColor(DyeColor.PINK);
                wolf.setCustomName(getRandomDogName());
                break;
            case ENDER_EYE:
                if(event.getAction() != Action.RIGHT_CLICK_AIR) return;
                Bukkit.getScheduler().runTaskLater(App.instance, new Runnable() {
                    @Override
                    public void run() {
                        if(enderEyeDirection == null) return;
                        player.setVelocity(enderEyeDirection.multiply(100));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20*10, 0));
                        player.getInventory().addItem(new ItemStack(Material.ENDER_EYE, 1));
                        enderEyeDirection = null;
                    }
                }, 20*2);
                break;
        }
    }

    @EventHandler
    public void on(BlockBreakEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        Block block = event.getBlock();
        Material mat = block.getType();
        switch(mat) {
            default:
                break;
            case NETHER_GOLD_ORE:
            case GOLD_BLOCK:
                if(world.getEnvironment() != Environment.NETHER) break;
                if(taskPiglin != -1) return;
                Vector inverseDirectionVec = player.getLocation().getDirection().normalize().multiply(-1);
                Location pigSpawn = getSurfaceLoc(player.getLocation().add(inverseDirectionVec.multiply(2)));
                Location look = player.getLocation();
                pigSpawn.setDirection(look.subtract(pigSpawn).toVector());
                world.playSound(pigSpawn, Sound.ENTITY_PIGLIN_ADMIRING_ITEM, SoundCategory.NEUTRAL, 1, 1);
                Piglin piglin = (Piglin)world.spawnEntity(pigSpawn, EntityType.PIGLIN);
                piglin.setAdult();
                piglin.setAI(false);
                pigSpawn.add(0, 1, 0);
                taskPiglin = Bukkit.getScheduler().scheduleSyncRepeatingTask(App.instance, new Runnable() {
                    int count = 0;
                    public void run() {
                        if(count++ >= 15) {
                            Bukkit.getScheduler().cancelTask(taskPiglin);
                            taskPiglin = -1;
                            world.spawnParticle(Particle.HEART, pigSpawn, 1);
                            piglin.remove();
                        }
                        ItemStack nextItem = getRandomPiglinItem();
                        Item item = world.dropItem(pigSpawn, nextItem);
                        item.setVelocity(look.toVector().multiply(0.1));
                    }
                  }, 20, 3);
                break;
        }
    }

    @EventHandler
    public void on(ProjectileHitEvent event) {
        ProjectileSource ps = event.getEntity().getShooter();
        if(ps instanceof Entity && ((Entity)ps).getType() == EntityType.BLAZE)  {
            Location l = event.getEntity().getLocation();
            l.getWorld().createExplosion(l, 1, false, false);
        }
    }

    @EventHandler
    public void on(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        if(entity instanceof SmallFireball) {
            Fireball f = (Fireball)entity;
            Location l = f.getLocation().add(f.getDirection().multiply(20));
            Snowball b = entity.getWorld().spawn(l, Snowball.class);
            b.setVelocity(f.getDirection().multiply(10));
            b.setShooter(f.getShooter());
            event.setCancelled(true);
        }else if(entity instanceof EnderSignal) {
            EnderSignal e = (EnderSignal)entity;
            e.setDropItem(false);
            Bukkit.getScheduler().runTaskLater(App.instance, new Runnable() {
                @Override
                public void run() {
                    enderEyeDirection = e.getVelocity().normalize();
                }
            }, 20);
        }else if(entity instanceof Silverfish) {
            Silverfish silverfish = (Silverfish)entity;
            silverfish.setGlowing(true);
            silverfish.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(100);
            silverfish.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(0);
        }
    }

    @EventHandler
    public void on(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        World world = entity.getWorld();
        if(damager instanceof Player) {
            Player player = (Player)damager;
            Location pl = player.getLocation();
            if(entity instanceof Enderman) {
                if(taskPearl != -1) return;
                Enderman enderman = (Enderman)entity;
                Location el = enderman.getLocation();
                Location look = pl.clone();
                enderman.setAI(false);
                el.setDirection(look.subtract(0, 1, 0).subtract(el).toVector());
                enderman.teleport(el);
                ItemStack pearl = new ItemStack(Material.ENDER_PEARL, 1);
                el.add(0, 2, 0);
                look.setDirection(look.getDirection().add(new Vector(0, 8, 0)));
                taskPearl = Bukkit.getScheduler().scheduleSyncRepeatingTask(App.instance, new Runnable() {
                    int count = 0;
                    public void run() {
                        if(count++ >= 15) {
                            Bukkit.getScheduler().cancelTask(taskPearl);
                            taskPearl = -1;
                        }
                        Item item = world.dropItem(el, pearl);
                        item.setVelocity(look.toVector().multiply(0.1));
                    }
                  }, 20, 3);
            }
        }else if(damager instanceof Arrow) {
            if(entity instanceof EnderCrystal) {
                Arrow arrow = (Arrow)world.spawnEntity(entity.getLocation(), EntityType.ARROW);
                new HomingArrowRunnable(arrow).runTaskTimer(App.instance, 5, 1);
            }
        }
    }

    @EventHandler
    public void on(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if(entity instanceof Creeper) {
            event.setCancelled(true);
            Location l = entity.getLocation().add(0, 1, 0);
            entity.remove();
            World w = l.getWorld();
            new BukkitRunnable(){
                double t = 0;
                double r = 2;
                public void run(){
                    t = t + Math.PI/16;
                    double x = r*Math.cos(t);
                    double y = t;
                    double z = r*Math.sin(t);
                    l.add(x, y, z);
                    w.spawnParticle(Particle.HEART, l, 1);
                    l.subtract(x, y, z);
                    if (t > Math.PI*2){
                        this.cancel();
                    }
                }
            }.runTaskTimer(App.instance, 0, 1);
            new BukkitRunnable(){
                double t = 0;
                double r = 1;
                public void run(){
                    t = t + Math.PI/16;
                    double x = -r*Math.cos(t);
                    double y = -t/8;
                    double z = -r*Math.sin(t);
                    l.add(x, y, z);
                    w.spawnParticle(Particle.HEART, l, 1);
                    l.subtract(x, y, z);
                    if (t > Math.PI*2){
                        this.cancel();
                    }
                }
            }.runTaskTimer(App.instance, 0, 1);
        }
    }

    @EventHandler
    public void on(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        World world = entity.getWorld();
        if(entity instanceof Player) {
            Player player = (Player)entity;
            // player can't go under half heart (can't die)
            if(player.getHealth() - event.getFinalDamage() <= 0) {
                double newDmg = player.getHealth()-1;
                if(player.getHealth()-newDmg <= 0) event.setDamage(0);
                else event.setDamage(newDmg);
            }

            switch(event.getCause()) {
                default:
                    break;
                case LAVA: // witch fire resistance event
                    if(player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) break;
                    Vector dir = player.getEyeLocation().getDirection();
                    Block b = player.getLocation().getBlock().getRelative((int)(Math.round(dir.getX())*4), 1, (int)(Math.round(dir.getZ())*4));
                    b.setType(Material.OBSIDIAN);
                    Witch e = (Witch)world.spawnEntity(b.getRelative(0, 1, 0).getLocation().add(0.5, 0, 0.5), EntityType.WITCH);
                    e.setCustomName("Your Saviour");
                    e.setTarget(player);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20*60*10, 1)); // apply fire res early
                    world.playSound(e.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, SoundCategory.HOSTILE, 1, 1);
                    e.playEffect(EntityEffect.TOTEM_RESURRECT);
                    break;
                case ENTITY_EXPLOSION:
                    event.setDamage(0);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 2, 0));
                    player.setHealth(player.getHealth() + event.getFinalDamage());
                    break;
                case VOID:
                    event.setDamage(0);
                    if(taskVoidLaunch != -1) break;
                    taskVoidLaunch = Bukkit.getScheduler().scheduleSyncRepeatingTask(App.instance, new Runnable() {
                        public void run() {
                            if(player.getLocation().getY() > 80) {
                                Bukkit.getScheduler().cancelTask(taskVoidLaunch);
                                taskVoidLaunch = -1;
                            }
                            player.setVelocity(new Vector(0, 100, 0));
                        }
                    }, 0, 2);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20*10, 0));
                    break;
                case FIRE:
                    if(fireFlying || world.getEnvironment() != Environment.NETHER) break;
                    fireFlying = true;
                    player.setAllowFlight(true);
                    player.setFlying(true);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20*60, 0));
                    Bukkit.getScheduler().runTaskLater(App.instance, new Runnable() {
                        @Override
                        public void run() {
                            fireFlying = false;
                            player.setAllowFlight(false);
                            player.setFlying(false);
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20*10, 0));
                        }
                    }, 20*60);
                    break;
            }
        }
    }

    @EventHandler
    public void on(PotionSplashEvent event) {
        ProjectileSource ps = event.getPotion().getShooter();
        if(ps instanceof Witch) {
            Witch w = (Witch)ps;
            if(!w.getCustomName().equals("Your Saviour")) return;
            event.setCancelled(true);
            event.getAffectedEntities().forEach(e -> {
                e.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20*60*10, 1));
            });
            Bukkit.getScheduler().runTaskLater(App.instance, new Runnable() {
                @Override
                public void run() {
                    Location l = w.getLocation();
                    w.remove();
                    l.getWorld().spawnParticle(Particle.HEART, l, 1);
                    Block under = l.getBlock().getRelative(0, -1, 0);
                    if(under.getType() == Material.OBSIDIAN) under.setType(Material.AIR);
                }
            }, 20);
            // ItemStack potion = new ItemStack(Material.SPLASH_POTION);
            // PotionMeta im = (PotionMeta)potion.getItemMeta();
            // im.setBasePotionData(new PotionData(PotionType.FIRE_RESISTANCE, true, false));
            // potion.setItemMeta(im);
            // event.getPotion().setItem(potion);
            // Bukkit.broadcastMessage(event.getPotion().getItem()+"");
        }
    }
    
    // auto portal when water bucket in lava pool
    @EventHandler
    public void on(PlayerBucketEmptyEvent event) {
        if(event.getBucket() == Material.WATER_BUCKET) {
            Block b = event.getBlock();
            if(b.getType() == Material.LAVA) {
                Block b2 = null;
                if(b.getRelative(-1, 0, 0).getType() == Material.LAVA) b2 = b.getRelative(-1, 0, 0);
                else if(b.getRelative(1, 0, 0).getType() == Material.LAVA) b2 = b.getRelative(1, 0, 0);
                else if(b.getRelative(0, 0, -1).getType() == Material.LAVA) b2 = b.getRelative(0, 0, -1);
                else if(b.getRelative(0, 0, 1).getType() == Material.LAVA) b2 = b.getRelative(0, 0, 1);
                if(b2 == null) return;

                World world = b.getWorld();
                Location diff = b2.getLocation().subtract(b.getLocation());
                int x = diff.getBlockX(), z = diff.getBlockZ();
                
                b.getRelative(0, -1, 0).setType(Material.OBSIDIAN);
                b2.getRelative(0, -1, 0).setType(Material.OBSIDIAN);
                b2.setType(Material.AIR);

                int ticks = 0;
                for(int y = -1; y < 4; y++) {
                    final Block block = b.getRelative(-x, y, -z), block2 = b2.getRelative(x, y, z);
                    Bukkit.getScheduler().runTaskLater(App.instance, new Runnable() {
                        @Override
                        public void run() {
                            block.setType(Material.OBSIDIAN);
                            world.spawnParticle(Particle.SMALL_FLAME, block.getLocation(), 50);
                            block2.setType(Material.OBSIDIAN);
                            world.spawnParticle(Particle.SMALL_FLAME, block2.getLocation(), 50);
                        }
                    }, ticks);
                    ticks += 10;
                }
                final Block block = b.getRelative(0, 3, 0), block2 = b2.getRelative(0, 3, 0);
                Bukkit.getScheduler().runTaskLater(App.instance, new Runnable() {
                    @Override
                    public void run() {
                        block.setType(Material.OBSIDIAN);
                        world.spawnParticle(Particle.SMALL_FLAME, block.getLocation(), 50);
                        block2.setType(Material.OBSIDIAN);
                        world.spawnParticle(Particle.SMALL_FLAME, block2.getLocation(), 50);
                    }
                }, ticks);
            }
        }
    }

    // -- helpers -- //

    private Material[] piglinItems = {
        Material.COOKED_PORKCHOP, Material.IRON_NUGGET, Material.GOLDEN_CARROT,
        Material.OBSIDIAN, Material.FIRE_CHARGE, Material.WOLF_SPAWN_EGG
    };
    private ItemStack getRandomPiglinItem() {
        Random rand = new Random();
        return new ItemStack(piglinItems[rand.nextInt(piglinItems.length)], rand.nextInt(10)+1);
    }

    private String[] dogNames = {"Buddy", "Bailey", "Max", "Charlie", "Lucy", "Daisy", "Molly", "Sadie", "Lola", "Zoe", 
        "Coco", "Rosie", "Penny", "Ruby", "Maggie", "Luna", "Chloe", "Lily", "Sophie", "Stella", "Finn", "Ollie", "Toby", 
        "Rocky", "Gus", "Teddy", "Winnie", "Eddie", "Leo", "Bentley", "Piper", "Harley", "Buster", "Milo", "Mia", "Abby", 
        "Bella", "Roxy", "Gracie", "Cali", "Hazel", "Mocha", "Cinnamon", "Cookie", "Oreo", "Peanut", "Poppy", "Honey", 
        "Sugar", "Pumpkin", "Maple", "Sunny", "Duke", "Bear", "Zeus", "Thor", "Apollo", "Jax", "Rocco", "Simba", "Rufus", 
        "Murphy", "Tucker", "Harvey", "Frankie", "Gatsby", "Elvis", "Prince", "Oliver", "Scooby", "Benji", "Lassie", 
        "Beethoven", "Baloo", "Bagheera", "Dumbo", "Felix", "Garfield", "Kermit", "Mickey", "Minnie", "Nemo", "Snoopy", 
        "Sully", "Tarzan", "Woody", "Yogi", "Bambi", "Bolt", "Buzz", "Cinderella", "Daisy Duke", "Donald", "Dopey", "Dory", 
        "Eeyore", "Flounder", "Gizmo", "Goofy", "Grumpy", "Happy", "Lilo", "Mater", "Meeko", "Mowgli", "Pocahontas", "Pumba", 
        "Remy", "Sebastian", "Smoochie", "Thumper", "Tigger", "Timon", "Winnie the Pooh", "Zazu", "Alfie", "Archie", "Barney", 
        "Brady", "Brody", "Bruno", "Cody", "Cooper", "Dexter", "Fergus", "George", "Gizmo", "Gunner", "Hank", "Hudson", "Hunter", 
        "Jack", "Jasper", "Koda", "Lenny", "Louie", "Lucky", "Marley", "Monty", "Nash", "Nelson", "Oscar", "Otis", "Pablo"};
    private String getRandomDogName() {
        Random rand = new Random();
        return dogNames[rand.nextInt(dogNames.length)];
    }
    
    private Location getSurfaceLoc(Location loc) {
        if(loc.getBlock().getType() != Material.AIR) {
            while(loc.getBlock().getType() != Material.AIR) {
                loc = loc.add(0, 1, 0);
            }
        }else {
            while(loc.getBlock().getType() == Material.AIR) {
                loc = loc.subtract(0, 1, 0);
            }
            loc = loc.add(0, 1, 0);
        }
        return loc.getBlock().getLocation();
    }

}
