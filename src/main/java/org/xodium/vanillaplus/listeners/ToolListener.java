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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.kyori.adventure.text.minimessage.MiniMessage;

// TODO: fix chisel not in right mode when using it on stairs and switching to slabs.
public class ToolListener implements Listener {
    private static final int DAMAGE_AMOUNT = 1;
    private static final long COOLDOWN_TIME_MS = 500;
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final EnumSet<BlockFace> FACES_BLACKLIST = EnumSet.of(
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.NORTH_EAST,
            BlockFace.NORTH_WEST,
            BlockFace.SOUTH_EAST,
            BlockFace.SOUTH_WEST,
            BlockFace.WEST_NORTH_WEST,
            BlockFace.NORTH_NORTH_WEST,
            BlockFace.NORTH_NORTH_EAST,
            BlockFace.EAST_NORTH_EAST,
            BlockFace.EAST_SOUTH_EAST,
            BlockFace.SOUTH_SOUTH_EAST,
            BlockFace.SOUTH_SOUTH_WEST,
            BlockFace.WEST_SOUTH_WEST,
            BlockFace.SELF);
    private static final EnumSet<Slab.Type> SLAB_BLACKLIST = EnumSet.of(Slab.Type.DOUBLE);

    private enum BlockMode {
        FACE, SHAPE, HALF
    }

    private volatile BlockMode currentBlockMode = BlockMode.FACE;
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
        currentBlockMode = isSlab ? BlockMode.HALF
                : BlockMode.values()[(currentBlockMode.ordinal() + 1) % BlockMode.values().length];
        player.sendActionBar(mm
                .deserialize("<b><gradient:#CB2D3E:#EF473A>Mode:</gradient> " + currentBlockMode + "</b>"));
    }

    private void handleModeAction(Block block, boolean clockwise, Player player) {
        BlockData blockData = block.getBlockData();
        if (blockData instanceof Stairs stairs) {
            switch (currentBlockMode) {
                case FACE -> {
                    stairs.setFacing(iterateFace(stairs.getFacing(), clockwise,
                            FACES_BLACKLIST));
                    sendModeChangeMessage(player, "Facing", stairs.getFacing().name());
                }
                case SHAPE -> {
                    stairs.setShape(iterateShape(stairs.getShape(), clockwise, null));
                    sendModeChangeMessage(player, "Shape", stairs.getShape().name());
                }
                case HALF -> {
                    stairs.setHalf(iterateHalf(stairs.getHalf(), clockwise, null));
                    sendModeChangeMessage(player, "Half", stairs.getHalf().name());
                }
            }
            block.setBlockData(stairs);
        } else if (blockData instanceof Slab slab && currentBlockMode == BlockMode.HALF) {
            slab.setType(iterateType(slab.getType(), clockwise, SLAB_BLACKLIST));
            sendModeChangeMessage(player, "Half", slab.getType().name());
            block.setBlockData(slab);
        }
    }

    private void sendModeChangeMessage(Player player, String property, String newValue) {
        player.sendActionBar(mm
                .deserialize("<b><gradient:#CB2D3E:#EF473A>" + property + " changed to:</gradient> " + newValue
                        + "</b>"));
    }

    private <T extends Enum<T>> T iterateEnum(T current, boolean clockwise, Set<T> blacklist) {
        T[] values = current.getDeclaringClass().getEnumConstants();
        int step = clockwise ? 1 : -1;
        int index = current.ordinal();
        int size = values.length;
        for (int i = 0; i < size; i++) {
            index = (index + step + size) % size;
            T next = values[index];
            if (blacklist == null || !blacklist.contains(next)) {
                return next;
            }
        }
        return current;
    }

    private BlockFace iterateFace(BlockFace face, boolean clockwise, Set<BlockFace> blacklist) {
        return iterateEnum(face, clockwise, blacklist);
    }

    private Stairs.Shape iterateShape(Stairs.Shape shape, boolean clockwise, Set<Stairs.Shape> blacklist) {
        return iterateEnum(shape, clockwise, blacklist);
    }

    private Stairs.Half iterateHalf(Stairs.Half half, boolean clockwise, Set<Stairs.Half> blacklist) {
        return iterateEnum(half, clockwise, blacklist);
    }

    private Slab.Type iterateType(Slab.Type type, boolean clockwise, Set<Slab.Type> blacklist) {
        return iterateEnum(type, clockwise, blacklist);
    }
}