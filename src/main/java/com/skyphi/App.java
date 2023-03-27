package com.skyphi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class App extends JavaPlugin {

    public static App instance;

    @Override
    public void onEnable() {
        App.instance = this;

        AddRecipes();

        // events
        getServer().getPluginManager().registerEvents(new Events(), this);

        // commands
        // this.getCommand("start").setExecutor(new StartCommand());
    }

    @Override
    public void onDisable() {

    }

    private void AddRecipes() {
        {
            NamespacedKey key = new NamespacedKey(this, "fire_sword");
            ItemStack i = new ItemStack(Material.WOODEN_SWORD);
            i.addEnchantment(Enchantment.FIRE_ASPECT, 2);
            ItemMeta meta = i.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "FIRE SWORD >:D");
            i.setItemMeta(meta);
            ShapedRecipe sword = new ShapedRecipe(key, i);
            sword.shape("O", "O", "I");
            sword.setIngredient('O', Material.CRIMSON_PLANKS);
            sword.setIngredient('I', Material.STICK);
            Bukkit.getServer().addRecipe(sword);
        }
        {
            NamespacedKey key = new NamespacedKey(this, "fire_sword2");
            ItemStack i = new ItemStack(Material.WOODEN_SWORD);
            i.addEnchantment(Enchantment.FIRE_ASPECT, 2);
            ItemMeta meta = i.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "FIRE SWORD >:D");
            i.setItemMeta(meta);
            ShapedRecipe sword = new ShapedRecipe(key, i);
            sword.shape("O", "O", "I");
            sword.setIngredient('O', Material.WARPED_PLANKS);
            sword.setIngredient('I', Material.STICK);
            Bukkit.getServer().addRecipe(sword);
        }
    }

}
