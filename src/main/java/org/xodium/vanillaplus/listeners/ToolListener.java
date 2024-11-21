package org.xodium.vanillaplus.listeners;

import org.bukkit.GameMode;
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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.kyori.adventure.text.minimessage.MiniMessage;

public class ToolListener implements Listener {
    private static final int DAMAGE_AMOUNT = 1;
    private static final long COOLDOWN_TIME_MS = 500;
    private static final MiniMessage MM = MiniMessage.miniMessage();
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
    private Map<Player, Long> lastBlockChangeTimes = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerUseTool(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (item == null || !isChisel(item) || event.getClickedBlock() == null)
            return;

        Block block = event.getClickedBlock();
        BlockData blockData = block.getBlockData();

        if (!(blockData instanceof Stairs || blockData instanceof Slab))
            return;

        if (player.getGameMode() == GameMode.CREATIVE && action == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
        }

        if (player.isSneaking() && action == Action.RIGHT_CLICK_BLOCK)
            return;

        if (player.isSneaking() && action == Action.LEFT_CLICK_BLOCK) {
            toggleMode(player, blockData instanceof Slab);
        } else if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
            processBlockChange(block, action, player, item);
        }
    }

    private boolean isChisel(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(ITEMS.CHISEL_KEY, PersistentDataType.STRING);
    }

    private void toggleMode(Player player, boolean isSlab) {
        currentBlockMode = isSlab ? BlockMode.HALF
                : BlockMode.values()[(currentBlockMode.ordinal() + 1) % BlockMode.values().length];
        sendActionBarMessage(player, "Mode", currentBlockMode.name());
    }

    private void processBlockChange(Block block, Action action, Player player, ItemStack item) {
        long currentTime = System.currentTimeMillis();
        long lastChangeTime = lastBlockChangeTimes.getOrDefault(player, 0L);

        if (currentTime - lastChangeTime >= COOLDOWN_TIME_MS) {
            handleModeAction(block, action == Action.LEFT_CLICK_BLOCK, player);
            ItemManager.applyDamage(player, item, DAMAGE_AMOUNT);
            lastBlockChangeTimes.put(player, currentTime);
        }
    }

    private void handleModeAction(Block block, boolean clockwise, Player player) {
        BlockData blockData = block.getBlockData();

        if (blockData instanceof Stairs stairs) {
            switch (currentBlockMode) {
                case FACE -> modifyAndNotify(stairs, player, "Facing",
                        () -> stairs.setFacing(iterateEnum(stairs.getFacing(), clockwise, FACES_BLACKLIST)));
                case SHAPE -> modifyAndNotify(stairs, player, "Shape",
                        () -> stairs.setShape(iterateEnum(stairs.getShape(), clockwise, Collections.emptySet())));
                case HALF -> modifyAndNotify(stairs, player, "Half",
                        () -> stairs.setHalf(iterateEnum(stairs.getHalf(), clockwise, Collections.emptySet())));
            }
            block.setBlockData(stairs);
        } else if (blockData instanceof Slab slab && currentBlockMode == BlockMode.HALF) {
            modifyAndNotify(slab, player, "Half",
                    () -> slab.setType(iterateEnum(slab.getType(), clockwise, SLAB_BLACKLIST)));
            block.setBlockData(slab);
        }
    }

    private <T> void modifyAndNotify(T blockData, Player player, String property, Runnable modification) {
        modification.run();
        sendActionBarMessage(player, property, blockData.toString());
    }

    private void sendActionBarMessage(Player player, String property, String newValue) {
        player.sendActionBar(MM.deserialize(
                String.format("<b><gradient:#CB2D3E:#EF473A>%s changed to:</gradient> %s</b>", property, newValue)));
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
}