package org.xodium.vanillaplus.managers;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.xodium.vanillaplus.VanillaPlus;
import org.xodium.vanillaplus.interfaces.ITEMS;

import net.kyori.adventure.text.minimessage.MiniMessage;

public class ItemManager {
    private static final VanillaPlus plugin = VanillaPlus.getInstance();
    private static final NamespacedKey CHISEL_KEY = new NamespacedKey(plugin, "chisel");
    private static final String CHISEL_MODIFIER = "chisel_modifier";

    public ItemStack createChisel() {
        ItemStack item = new ItemStack(Material.BRUSH);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
        data.set(CHISEL_KEY, PersistentDataType.STRING, CHISEL_MODIFIER);
        meta.setCustomModelData(1);
        meta.displayName(MiniMessage.miniMessage().deserialize(ITEMS.CHISEL_NAME));
        meta.lore(List.of(
                MiniMessage.miniMessage()
                        .deserialize("<dark_gray>▶ <gray>L_click to loop through the X axis block states <dark_gray>◀"),
                MiniMessage.miniMessage()
                        .deserialize(
                                "<dark_gray>▶ <gray>R_click to counter-loop through the X axis block states <dark_gray>◀"),
                MiniMessage.miniMessage()
                        .deserialize(
                                "<dark_gray>▶ <gray>Shift+click to toggle the block Y axis state <dark_gray>◀")));
        item.setItemMeta(meta);
        createChiselRecipe(CHISEL_KEY, item);
        return item;
    }

    private static void createChiselRecipe(NamespacedKey key, ItemStack item) {
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape(" A ", " B ");
        recipe.setIngredient('A', Material.IRON_INGOT);
        recipe.setIngredient('B', Material.STICK);
        plugin.getServer().addRecipe(recipe);
    }

    public static void applyDamage(ItemStack item, int damage) {
        ItemMeta meta = item.getItemMeta();
        Damageable damageable = (Damageable) meta;
        int currentDamage = damageable.getDamage();
        damageable.setDamage(currentDamage + damage);
        item.setItemMeta(meta);
    }
}
