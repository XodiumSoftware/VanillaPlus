package org.xodium.vanillaplus.managers;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.xodium.vanillaplus.VanillaPlus;
import org.xodium.vanillaplus.data.PossibleNeighbour;
import org.xodium.vanillaplus.interfaces.CONFIG;

import com.google.common.base.Enums;

public class DoorManager {
    private final static VanillaPlus plugin = VanillaPlus.getInstance();
    private final static FileConfiguration config = plugin.getConfig();
    private final static PossibleNeighbour[] POSSIBLE_NEIGHBOURS = {
            new PossibleNeighbour(0, -1, Door.Hinge.RIGHT, BlockFace.EAST),
            new PossibleNeighbour(0, 1, Door.Hinge.LEFT, BlockFace.EAST),

            new PossibleNeighbour(1, 0, Door.Hinge.RIGHT, BlockFace.SOUTH),
            new PossibleNeighbour(-1, 0, Door.Hinge.LEFT, BlockFace.SOUTH),

            new PossibleNeighbour(0, 1, Door.Hinge.RIGHT, BlockFace.WEST),
            new PossibleNeighbour(0, -1, Door.Hinge.LEFT, BlockFace.WEST),

            new PossibleNeighbour(-1, 0, Door.Hinge.RIGHT, BlockFace.NORTH),
            new PossibleNeighbour(1, 0, Door.Hinge.LEFT, BlockFace.NORTH)
    };

    public static void playKnockSound(Block block) {
        Location location = block.getLocation();
        World world = block.getWorld();

        Sound sound = Optional
                .ofNullable(
                        Registry.SOUNDS.get(NamespacedKey.minecraft(config.getString(CONFIG.SOUND_KNOCK_WOOD))))
                .orElse(Sound.ITEM_SHIELD_BLOCK);

        SoundCategory category = Enums
                .getIfPresent(SoundCategory.class,
                        config.getString(CONFIG.SOUND_KNOCK_CATEGORY))
                .or(SoundCategory.BLOCKS);

        float volume = (float) config.getDouble(CONFIG.SOUND_KNOCK_VOLUME);
        float pitch = (float) config.getDouble(CONFIG.SOUND_KNOCK_PITCH);

        world.playSound(location, sound, category, volume, pitch);
    }

    public static void toggleDoor(Block doorBlock, Openable openable, boolean open) {
        openable.setOpen(open);
        doorBlock.setBlockData(openable);
    }

    public static Door getBottomDoor(Door door, Block block) {
        Block below = (door.getHalf() == Bisected.Half.BOTTOM) ? block : block.getRelative(BlockFace.DOWN);
        if (below.getType() == block.getType() && below.getBlockData() instanceof Door) {
            return (Door) below.getBlockData();
        }
        return null;
    }

    public static Block getOtherPart(Door door, Block block) {
        if (door == null)
            return null;
        for (PossibleNeighbour neighbour : POSSIBLE_NEIGHBOURS) {
            Block relative = block.getRelative(neighbour.getOffsetX(), 0, neighbour.getOffsetZ());
            Door otherDoor = (relative.getBlockData() instanceof Door) ? (Door) relative.getBlockData() : null;
            if (otherDoor != null
                    && neighbour.getFacing() == door.getFacing()
                    && neighbour.getHinge() == door.getHinge()
                    && relative.getType() == block.getType()
                    && otherDoor.getHinge() != neighbour.getHinge()
                    && otherDoor.isOpen() == door.isOpen()
                    && otherDoor.getFacing() == neighbour.getFacing()) {
                return relative;
            }
        }
        return null;
    }

    public static void toggleOtherDoor(Block block, Block otherBlock, boolean open, boolean causedByRedstone) {
        toggleOtherDoor(block, otherBlock, open, causedByRedstone, false);
    }

    public static void toggleOtherDoor(Block block, Block otherBlock, boolean open, boolean causedByRedstone,
            boolean force) {

        if (!(block.getBlockData() instanceof Door) || !(otherBlock.getBlockData() instanceof Door))
            return;

        Door door = (Door) block.getBlockData();
        Door otherDoor = (Door) otherBlock.getBlockData();

        if (causedByRedstone) {
            DoorManager.toggleDoor(otherBlock, otherDoor, open);
            return;
        }

        boolean openNow = door.isOpen();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!(otherBlock.getBlockData() instanceof Door))
                    return;
                Door newDoor = (Door) block.getBlockData();
                if (!force && newDoor.isOpen() == openNow) {
                    return;
                }
                DoorManager.toggleDoor(otherBlock, otherDoor, open);
            }
        }.runTaskLater(plugin, 1L);
    }
}
