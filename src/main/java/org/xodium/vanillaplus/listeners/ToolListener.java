package org.xodium.vanillaplus.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.xodium.vanillaplus.interfaces.ITEMS;
import org.xodium.vanillaplus.managers.ItemManager;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.minimessage.MiniMessage;

// TODO: fix chisel not in right mode when using it on stairs and switching to slabs.
public class ToolListener implements Listener {
    private static final int DAMAGE_AMOUNT = 1;
    private static final long COOLDOWN_TIME_MS = 500;
    private static final MiniMessage mm = MiniMessage.miniMessage();

    private enum Mode {
        FACE, SHAPE, HALF
    }

    private Mode currentMode = Mode.FACE; // TODO: should we even have a default mode, since it depends per block type
                                          // what they accept?
    private Map<Player, Long> lastBlockChangeTimes = new HashMap<>();

    @EventHandler
    public void onPlayerUseTool(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        Player player = e.getPlayer();
        Action action = e.getAction();

        if (item == null)
            return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(ITEMS.CHISEL_KEY, PersistentDataType.STRING))
            return;
        Block block = e.getClickedBlock();
        if (block == null)
            return;
        BlockData blockData = block.getBlockData();

        if (!(blockData instanceof Stairs || blockData instanceof Slab))
            return;
        if (player.isSneaking() && action == Action.RIGHT_CLICK_BLOCK)
            return;
        if (player.isSneaking() && action == Action.LEFT_CLICK_BLOCK) {
            switchMode(player, blockData instanceof Slab);
        } else if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
            long currentTime = System.currentTimeMillis();
            long lastChangeTime = lastBlockChangeTimes.getOrDefault(player, 0L);
            if (currentTime - lastChangeTime >= COOLDOWN_TIME_MS) {
                handleModeAction(block, action == Action.LEFT_CLICK_BLOCK, player);
                ItemManager.applyDamage(player, item, DAMAGE_AMOUNT);
                lastBlockChangeTimes.put(player, currentTime);
            }
        }
    }

    private void switchMode(Player player, boolean isSlab) {
        currentMode = isSlab ? Mode.HALF : Mode.values()[(currentMode.ordinal() + 1) % Mode.values().length];
        player.sendActionBar(mm
                .deserialize("<b><gradient:#CB2D3E:#EF473A>Mode:</gradient> " + currentMode + "</b>"));
    }

    private void handleModeAction(Block block, boolean clockwise, Player player) {
        BlockData blockData = block.getBlockData();
        if (blockData instanceof Stairs stairs) {
            switch (currentMode) {
                case FACE -> {
                    stairs.setFacing(getNextFace(stairs.getFacing(), clockwise));
                    sendModeChangeMessage(player, "Facing", stairs.getFacing().name());
                }
                case SHAPE -> {
                    stairs.setShape(getNextShape(stairs.getShape(), clockwise));
                    sendModeChangeMessage(player, "Shape", stairs.getShape().name());
                }
                case HALF -> {
                    stairs.setHalf(getNextHalf(stairs.getHalf(), clockwise));
                    sendModeChangeMessage(player, "Half", stairs.getHalf().name());
                }
            }
            block.setBlockData(stairs);
        } else if (blockData instanceof Slab slab && currentMode == Mode.HALF) {
            slab.setType(slab.getType() == Slab.Type.BOTTOM ? Slab.Type.TOP : Slab.Type.BOTTOM);
            sendModeChangeMessage(player, "Half", slab.getType().name());
            block.setBlockData(slab);
        }
    }

    private void sendModeChangeMessage(Player player, String property, String newValue) {
        player.sendActionBar(mm
                .deserialize("<b><gradient:#CB2D3E:#EF473A>" + property + " changed to:</gradient> " + newValue
                        + "</b>"));
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
        return Stairs.Shape.values()[(shape.ordinal() + (clockwise ? 1 : -1) + Stairs.Shape.values().length)
                % Stairs.Shape.values().length];
    }

    private Stairs.Half getNextHalf(Stairs.Half half, boolean clockwise) {
        return Stairs.Half.values()[(half.ordinal() + (clockwise ? 1 : -1) + Stairs.Half.values().length)
                % Stairs.Half.values().length];
    }
}