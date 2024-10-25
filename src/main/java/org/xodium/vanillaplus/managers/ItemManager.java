package org.xodium.vanillaplus.managers;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
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
    private static final String CHISEL_NAME = "Chisel";
    private static final String CHISEL_MODIFIER = "chisel_modifier";

    public ItemStack createChisel() {
        ItemStack item = new ItemStack(Material.BRUSH);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (meta != null) {
            container.set(ITEMS.CHISEL_KEY, PersistentDataType.STRING, CHISEL_MODIFIER);
            meta.setCustomModelData(1);
            meta.displayName(MiniMessage.miniMessage().deserialize(CHISEL_NAME));
            item.setItemMeta(meta);
            createChiselRecipe(ITEMS.CHISEL_KEY, item);
        }
        plugin.getLogger().info("Keys after setting CHISEL_KEY: " + container.getKeys());
        plugin.getLogger().info("Chisel item created with CHISEL_KEY.");
        return item;
    }

    private static void createChiselRecipe(NamespacedKey key, ItemStack item) {
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape(" A ", " B ");
        recipe.setIngredient('A', Material.IRON_INGOT);
        recipe.setIngredient('B', Material.STICK);
        plugin.getServer().addRecipe(recipe);
    }

    public static void applyDamage(Player p, ItemStack item, int damage) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable damageable) {
            int currentDamage = damageable.getDamage();
            damageable.setDamage(currentDamage + damage);
            if (currentDamage >= item.getType().getMaxDurability()) {
                item.setAmount(0);
                p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            } else {
                item.setItemMeta(meta);
            }
        }
    }
}
