package org.xodium.vanillaplus.listeners;

import java.util.List;

import org.bukkit.Tag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.xodium.vanillaplus.VanillaPlus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class ToolListener implements Listener {
    private final VanillaPlus plugin = VanillaPlus.getInstance();

    public ToolListener() {
        createChisel();
    }

    public ItemStack createChisel() {
        ItemStack item = new ItemStack(Material.BRUSH);
        NamespacedKey key = new NamespacedKey(plugin, "chisel");

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
            data.set(key, PersistentDataType.STRING, "chisel_modifier");
            meta.setCustomModelData(1);
            meta.displayName(Component.text("Chisel"));
            meta.lore(List.of(
                    Component.text("R+click to modify stairs")
                            .color(TextColor.fromHexString("#FFFFFF"))));
            item.setItemMeta(meta);
            createChiselRecipe(key, item);
        }
        return item;
    }

    private void createChiselRecipe(NamespacedKey key, ItemStack item) {
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape(" A ", " B ");
        recipe.setIngredient('A', Material.IRON_INGOT);
        recipe.setIngredient('B', Material.STICK);
        plugin.getServer().addRecipe(recipe);
    }

    @EventHandler
    public void onPlayerUseTool(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item != null && item.getType() == Material.BRUSH) {
            NamespacedKey key = new NamespacedKey(plugin, "chisel");
            PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
            if (data.has(key, PersistentDataType.STRING)) {
                Block block = e.getClickedBlock();
                if (block != null && Tag.STAIRS.isTagged(block.getType())) {
                    Stairs stairs = (Stairs) block.getBlockData();
                    BlockFace currentFacing = stairs.getFacing();
                    BlockFace[] faces = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
                    for (int i = 0; i < faces.length; i++) {
                        if (faces[i] == currentFacing) {
                            stairs.setFacing(faces[(i + 1) % faces.length]);
                            break;
                        }
                    }
                    block.setBlockData(stairs);
                    ItemMeta meta = item.getItemMeta();
                    if (meta instanceof Damageable) {
                        Damageable damageable = (Damageable) meta;
                        damageable.setDamage(damageable.getDamage() + 1);
                        item.setItemMeta(meta);
                    }
                }
            }
        }
    }
}
