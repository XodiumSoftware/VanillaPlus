package org.xodium.vanillaplus.listeners;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.xodium.vanillaplus.VanillaPlus;

import net.kyori.adventure.text.minimessage.MiniMessage;

public class ToolListener implements Listener {
    private static final VanillaPlus plugin = VanillaPlus.getInstance();
    private static final String CHISEL_NAME = "Chisel";
    private static final NamespacedKey CHISEL_KEY = new NamespacedKey(plugin, "chisel");
    private static final String CHISEL_MODIFIER = "chisel_modifier";

    {
        createChisel();
    }

    public ItemStack createChisel() {
        ItemStack item = new ItemStack(Material.BRUSH);
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
        data.set(CHISEL_KEY, PersistentDataType.STRING, CHISEL_MODIFIER);
        meta.setCustomModelData(1);
        meta.displayName(MiniMessage.miniMessage().deserialize(CHISEL_NAME));
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
        if (item == null || !isTool(item, Material.BRUSH, CHISEL_NAME)) {
            return;
        }

        Block block = e.getClickedBlock();
        if (block == null || (!isBlockType(block, "STAIRS") && !isBlockType(block, "SLAB"))) {
            return;
        }

        boolean clockwise = e.getAction() == Action.LEFT_CLICK_BLOCK;
        boolean shiftClick = e.getPlayer().isSneaking();

        toggleBlockState(block, clockwise, shiftClick);
    }

    private boolean isTool(ItemStack item, Material material, String toolName) {
        return item.getType() == material
                && MiniMessage.miniMessage().serialize(item.getItemMeta().displayName()).contains(toolName);
    }

    private boolean isBlockType(Block block, String blockType) {
        return block.getType().name().contains(blockType);
    }

    private void toggleBlockState(Block block, boolean clockwise, boolean shiftClick) {
        BlockData data = block.getBlockData();

        if (data instanceof Stairs stairs) {
            if (shiftClick) {
                Stairs.Shape currentShape = stairs.getShape();
                Stairs.Shape nextShape = getNextShape(currentShape, clockwise);
                stairs.setShape(nextShape);
            } else {
                BlockFace currentFacing = stairs.getFacing();
                BlockFace nextFacing = getNextFace(currentFacing, clockwise);
                stairs.setFacing(nextFacing);

                Stairs.Half currentHalf = stairs.getHalf();
                stairs.setHalf(currentHalf == Stairs.Half.BOTTOM ? Stairs.Half.TOP : Stairs.Half.BOTTOM);
            }
            block.setBlockData(stairs);

        } else if (data instanceof Slab slab) {
            slab.setType(slab.getType() == Slab.Type.BOTTOM ? Slab.Type.TOP : Slab.Type.BOTTOM);
            block.setBlockData(slab);
        }
    }

    private BlockFace getNextFace(BlockFace face, boolean clockwise) {
        return switch (face) {
            case NORTH -> clockwise ? BlockFace.EAST : BlockFace.WEST;
            case EAST -> clockwise ? BlockFace.SOUTH : BlockFace.NORTH;
            case SOUTH -> clockwise ? BlockFace.WEST : BlockFace.EAST;
            case WEST -> clockwise ? BlockFace.NORTH : BlockFace.SOUTH;
            default -> face;
        };
    }

    private Stairs.Shape getNextShape(Stairs.Shape shape, boolean clockwise) {
        return switch (shape) {
            case STRAIGHT -> clockwise ? Stairs.Shape.INNER_LEFT : Stairs.Shape.INNER_RIGHT;
            case INNER_LEFT -> clockwise ? Stairs.Shape.OUTER_LEFT : Stairs.Shape.STRAIGHT;
            case OUTER_LEFT -> clockwise ? Stairs.Shape.OUTER_RIGHT : Stairs.Shape.INNER_LEFT;
            case OUTER_RIGHT -> clockwise ? Stairs.Shape.INNER_RIGHT : Stairs.Shape.OUTER_LEFT;
            case INNER_RIGHT -> clockwise ? Stairs.Shape.STRAIGHT : Stairs.Shape.OUTER_RIGHT;
        };
    }
}