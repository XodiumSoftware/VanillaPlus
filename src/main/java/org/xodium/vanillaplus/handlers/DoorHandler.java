package org.xodium.vanillaplus.handlers;

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
import org.xodium.vanillaplus.Database;
import org.xodium.vanillaplus.VanillaPlus;
import org.xodium.vanillaplus.data.PossibleNeighbour;
import org.xodium.vanillaplus.interfaces.CONFIG;
import org.xodium.vanillaplus.interfaces.CONST;

import com.google.common.base.Enums;

public class DoorHandler {
    private final static VanillaPlus plugin = VanillaPlus.getInstance();
    private final static Database db = new Database();

    public static void playKnockSound(Block block) {
        VanillaPlus plugin = VanillaPlus.getInstance();
        Location location = block.getLocation();
        World world = block.getWorld();
        Sound sound = block.getType() == Material.IRON_DOOR
                ? Enums.getIfPresent(Sound.class, plugin.getConfig().getString(db.getData(CONFIG.SOUND_KNOCK_IRON)))
                        .or(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR)
                : Enums.getIfPresent(Sound.class, plugin.getConfig().getString(db.getData(CONFIG.SOUND_KNOCK_WOOD)))
                        .or(Sound.ITEM_SHIELD_BLOCK);

        SoundCategory category = Enums
                .getIfPresent(SoundCategory.class,
                        plugin.getConfig().getString(db.getData(CONFIG.SOUND_KNOCK_CATEGORY)))
                .or(SoundCategory.BLOCKS);

        float volume = (float) plugin.getConfig().getDouble(db.getData(CONFIG.SOUND_KNOCK_VOLUME), 1.0);
        float pitch = (float) plugin.getConfig().getDouble(db.getData(CONFIG.SOUND_KNOCK_PITCH), 1.0);

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
        }.runTaskLater(plugin, 1L);
    }
}
