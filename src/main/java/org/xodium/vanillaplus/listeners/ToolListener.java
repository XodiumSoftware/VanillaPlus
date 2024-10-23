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
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.xodium.vanillaplus.VanillaPlus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ToolListener implements Listener {
    private static final VanillaPlus plugin = VanillaPlus.getInstance();
    private static final BlockFace[] BLOCK_FACES = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
    private static final NamespacedKey CHISEL_KEY = new NamespacedKey(plugin, "chisel");

    {
        createChisel();
    }

    public ItemStack createChisel() {
        ItemStack item = new ItemStack(Material.BRUSH);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
        data.set(CHISEL_KEY, PersistentDataType.STRING, "chisel_modifier");
        meta.setCustomModelData(1);
        meta.displayName(Component.text("Chisel"));
        meta.lore(List.of(
                MiniMessage.miniMessage()
                        .deserialize("<dark_gray>▶ <gray>L+click to modify stairs clockwise <dark_gray>◀"),
                MiniMessage.miniMessage()
                        .deserialize("<dark_gray>▶ <gray>R+click to modify stairs anti-clockwise <dark_gray>◀")));
        item.setItemMeta(meta);
        createChiselRecipe(CHISEL_KEY, item);
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
            PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
            if (data.has(CHISEL_KEY, PersistentDataType.STRING)) {
                Block block = e.getClickedBlock();
                if (block != null && Tag.STAIRS.isTagged(block.getType())) {
                    e.setCancelled(true); // TODO: Didnt work.
                    Stairs stairs = (Stairs) block.getBlockData();
                    BlockFace currentFacing = stairs.getFacing();
                    if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        for (int i = 0; i < BLOCK_FACES.length; i++) {
                            if (BLOCK_FACES[i] == currentFacing) {
                                stairs.setFacing(BLOCK_FACES[(i + 1) % BLOCK_FACES.length]);
                                break;
                            }
                        }
                    } else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                        for (int i = 0; i < BLOCK_FACES.length; i++) {
                            if (BLOCK_FACES[i] == currentFacing) {
                                stairs.setFacing(BLOCK_FACES[(i - 1 + BLOCK_FACES.length) % BLOCK_FACES.length]);
                                break;
                            }
                        }
                    }
                    block.setBlockData(stairs);
                    ItemMeta meta = item.getItemMeta();
                    if (meta instanceof Damageable) {
                        Damageable damageable = (Damageable) meta;
                        damageable.setDamage(damageable.getDamage() + 1);
                        item.setItemMeta(meta);
                    } else {
                        plugin.getLogger().warning("Failed to cast item meta to damageable");
                    }
                }
            }
        }
    }
}
