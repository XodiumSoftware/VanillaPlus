package org.xodium.doorsplus.handlers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.scheduler.BukkitRunnable;
import org.xodium.doorsplus.DoorsPlus;
import org.xodium.doorsplus.config.Config;
import org.xodium.doorsplus.data.PossibleNeighbour;
import org.xodium.doorsplus.interfaces.CONST;

import com.google.common.base.Enums;

public class DoorHandler {
    private final static DoorsPlus main = DoorsPlus.getInstance();

    public static void playKnockSound(Block block) {
        DoorsPlus dp = DoorsPlus.getInstance();
        Location location = block.getLocation();
        World world = block.getWorld();
        Sound sound = block.getType() == Material.IRON_DOOR
                ? Enums.getIfPresent(Sound.class, dp.getConfig().getString(Config.SOUND_KNOCK_IRON))
                        .or(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR)
                : Enums.getIfPresent(Sound.class, dp.getConfig().getString(Config.SOUND_KNOCK_WOOD))
                        .or(Sound.ITEM_SHIELD_BLOCK);

        SoundCategory category = Enums
                .getIfPresent(SoundCategory.class, dp.getConfig().getString(Config.SOUND_KNOCK_CATEGORY))
                .or(SoundCategory.BLOCKS);

        float volume = (float) dp.getConfig().getDouble(Config.SOUND_KNOCK_VOLUME, 1.0);
        float pitch = (float) dp.getConfig().getDouble(Config.SOUND_KNOCK_PITCH, 1.0);

        world.playSound(location, sound, category, volume, pitch);
    }

    public static void toggleDoor(Block otherDoorBlock, Openable otherDoor, boolean open) {
        otherDoor.setOpen(open);
        otherDoorBlock.setBlockData(otherDoor);
    }

    public static Door getBottomDoor(Door door, Block block) {
        Block below = (door.getHalf() == Bisected.Half.BOTTOM) ? block : block.getRelative(BlockFace.DOWN);
        if (below.getType() == block.getType() && below.getBlockData() instanceof Door) {
            return (Door) below.getBlockData();
        }
        return null;
    }

    public static Block getOtherPart(Door door, Block block) {
        if (door == null) {
            return null;
        }
        for (PossibleNeighbour neighbour : CONST.POSSIBLE_NEIGHBOURS) {
            Block relative = block.getRelative(neighbour.getOffsetX(), 0, neighbour.getOffsetZ());
            Door otherDoor = (relative.getBlockData() instanceof Door) ? (Door) relative.getBlockData() : null;
            if (neighbour.getFacing() == door.getFacing()
                    && neighbour.getHinge() == door.getHinge()
                    && relative.getType() == block.getType()
                    && otherDoor != null
                    && otherDoor.getHinge() != neighbour.getHinge()
                    && door.isOpen() == otherDoor.isOpen()
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

        if (!(block.getBlockData() instanceof Door) || !(otherBlock.getBlockData() instanceof Door)) {
            return;
        }

        Door door = (Door) block.getBlockData();
        Door otherDoor = (Door) otherBlock.getBlockData();

        if (causedByRedstone) {
            DoorHandler.toggleDoor(otherBlock, otherDoor, open);
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
                DoorHandler.toggleDoor(otherBlock, otherDoor, open);
            }
        }.runTaskLater(main, 1L);
    }
}
