package org.xodium.vanillaplus.listeners;

import org.bukkit.Material;
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
import org.xodium.vanillaplus.interfaces.ITEMS;

import net.kyori.adventure.text.minimessage.MiniMessage;

public class ToolListener implements Listener {

    // TODO: make use of applyDamage method to reduce durability of the chisel on
    // use.
    @EventHandler
    public void onPlayerUseTool(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item == null || !(item.getType() == Material.BRUSH
                && MiniMessage.miniMessage().serialize(item.getItemMeta().displayName())
                        .contains(ITEMS.CHISEL_NAME))) {
            return;
        }
        Block block = e.getClickedBlock();
        if (block == null) {
            return;
        }

        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof Stairs || blockData instanceof Slab)) {
            return;
        }
        if (e.getPlayer().isSneaking()) {
            toggleHalfState(block);
        } else {
            toggleBlockState(block, e.getAction() == Action.LEFT_CLICK_BLOCK);
        }
    }

    private void toggleHalfState(Block block) {
        BlockData blockData = block.getBlockData();
        if (blockData instanceof Stairs stairs) {
            stairs.setHalf(stairs.getHalf() == Stairs.Half.BOTTOM ? Stairs.Half.TOP : Stairs.Half.BOTTOM);
            block.setBlockData(stairs);
        } else if (blockData instanceof Slab slab) {
            slab.setType(slab.getType() == Slab.Type.BOTTOM ? Slab.Type.TOP : Slab.Type.BOTTOM);
            block.setBlockData(slab);
        }
    }

    // TODO: its doesnt loop logically.
    private void toggleBlockState(Block block, boolean clockwise) {
        BlockData blockData = block.getBlockData();
        if (blockData instanceof Stairs stairs) {
            stairs.setFacing(getNextFace(stairs.getFacing(), clockwise));
            if (stairs.getShape() != Stairs.Shape.STRAIGHT) {
                stairs.setShape(getNextShape(stairs.getShape(), clockwise));
            }
            block.setBlockData(stairs);
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
            default -> shape;
        };
    }
}