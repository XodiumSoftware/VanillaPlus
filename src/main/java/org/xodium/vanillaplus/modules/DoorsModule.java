package org.xodium.vanillaplus.modules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import org.bukkit.event.Event;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.xodium.vanillaplus.ConfigManager;
import org.xodium.vanillaplus.VanillaPlus;
import org.xodium.vanillaplus.interfaces.Modular;
import org.xodium.vanillaplus.interfaces.PERMS;
import org.xodium.vanillaplus.records.AdjacentBlockRecord;

import com.google.common.base.Enums;

// TODO: refactor.
public class DoorsModule implements Listener, Modular {
    public static final String ENABLE = "enable";

    // Sound settings
    public static final String SOUND_KNOCK_CATEGORY = "sound_knock_category";
    public static final String SOUND_KNOCK_PITCH = "sound_knock_pitch";
    public static final String SOUND_KNOCK_VOLUME = "sound_knock_volume";
    public static final String SOUND_KNOCK_WOOD = "sound_knock_wood";

    // Behavior settings
    public static final String ALLOW_AUTOCLOSE = "allow_autoclose";
    public static final String ALLOW_DOUBLEDOORS = "allow_doubledoors";
    public static final String ALLOW_KNOCKING = "allow_knocking";
    public static final String ALLOW_KNOCKING_GATES = "allow_knocking_gates";
    public static final String ALLOW_KNOCKING_TRAPDOORS = "allow_knocking_trapdoors";
    public static final String KNOCKING_REQUIRES_EMPTY_HAND = "knocking_requires_empty_hand";
    public static final String KNOCKING_REQUIRES_SHIFT = "knocking_requires_shift";

    // Auto-close settings
    public static final String AUTOCLOSE_DELAY = "autoclose_delay";

    private final HashMap<Block, Long> autoClose = new HashMap<>();
    private final VanillaPlus vp = VanillaPlus.getInstance();
    private final ConfigManager cm = new ConfigManager();
    private final static AdjacentBlockRecord[] POSSIBLE_NEIGHBOURS = {
            new AdjacentBlockRecord(0, -1, Door.Hinge.RIGHT, BlockFace.EAST),
            new AdjacentBlockRecord(0, 1, Door.Hinge.LEFT, BlockFace.EAST),

            new AdjacentBlockRecord(1, 0, Door.Hinge.RIGHT, BlockFace.SOUTH),
            new AdjacentBlockRecord(-1, 0, Door.Hinge.LEFT, BlockFace.SOUTH),

            new AdjacentBlockRecord(0, 1, Door.Hinge.RIGHT, BlockFace.WEST),
            new AdjacentBlockRecord(0, -1, Door.Hinge.LEFT, BlockFace.WEST),

            new AdjacentBlockRecord(-1, 0, Door.Hinge.RIGHT, BlockFace.NORTH),
            new AdjacentBlockRecord(1, 0, Door.Hinge.LEFT, BlockFace.NORTH)
    };

    {
        Bukkit.getScheduler().runTaskTimer(vp, () -> {
            Iterator<Map.Entry<Block, Long>> it = autoClose.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Block, Long> entry = it.next();
                Block b = entry.getKey();
                Long time = entry.getValue();
                if (System.currentTimeMillis() < time)
                    continue;
                if (b.getBlockData() instanceof Openable) {
                    Openable openable = (Openable) b.getBlockData();
                    if (openable.isOpen()) {
                        if (openable instanceof Door) {
                            Block otherDoor = this.getOtherPart((Door) openable, b);
                            if (otherDoor != null) {
                                this.toggleOtherDoor(b, otherDoor, false);
                            }
                        } else if (openable instanceof Gate) {
                            b.getWorld().playSound(b.getLocation(), Sound.BLOCK_FENCE_GATE_CLOSE, 1.0f, 1.0f);
                        }
                        openable.setOpen(false);
                        b.setBlockData(openable);
                        b.getWorld().playSound(b.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1.0f, 1.0f);
                    }
                }
                it.remove();
            }
        }, 1, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRightClick(PlayerInteractEvent e) {
        Block clickedBlock = e.getClickedBlock();
        if (clickedBlock == null)
            return;
        BlockData blockData = clickedBlock.getBlockData();

        if (e.getHand() != EquipmentSlot.HAND
                || e.getAction() != Action.RIGHT_CLICK_BLOCK
                || e.useInteractedBlock() == Event.Result.DENY
                || e.useItemInHand() == Event.Result.DENY
                || !e.getPlayer().hasPermission(PERMS.DOORSMODULE.USE)
                || !cm.getConfigValue(ALLOW_DOUBLEDOORS, Boolean.class)
                || !(blockData instanceof Door
                        || blockData instanceof Gate))
            return;

        if (blockData instanceof Door) {
            Door door = this.getBottomDoor((Door) blockData, clickedBlock);
            Block otherDoorBlock = this.getOtherPart(door, clickedBlock);
            if (otherDoorBlock != null && otherDoorBlock.getBlockData() instanceof Door) {
                Door otherDoor = (Door) otherDoorBlock.getBlockData();
                this.toggleOtherDoor(clickedBlock, otherDoorBlock, !otherDoor.isOpen());
                autoClose.put(otherDoorBlock,
                        System.currentTimeMillis()
                                + Long.valueOf(cm.getConfigValue(AUTOCLOSE_DELAY, Integer.class)) * 1000);
            }
        }
        autoClose.put(clickedBlock,
                System.currentTimeMillis() + Long.valueOf(cm.getConfigValue(AUTOCLOSE_DELAY, Integer.class)) * 1000);
    }

    @EventHandler
    public void onKnock(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        GameMode gm = p.getGameMode();

        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR)
            return;

        if (!p.hasPermission(PERMS.DOORSMODULE.KNOCK) || e.getAction() != Action.LEFT_CLICK_BLOCK
                || e.getHand() != EquipmentSlot.HAND)
            return;

        if (cm.getConfigValue(KNOCKING_REQUIRES_SHIFT, Boolean.class) && !p.isSneaking())
            return;

        if (cm.getConfigValue(KNOCKING_REQUIRES_EMPTY_HAND, Boolean.class)
                && p.getInventory().getItemInMainHand().getType() != Material.AIR)
            return;

        if (e.getClickedBlock() == null)
            return;

        Block block = e.getClickedBlock();
        BlockData blockData = block.getBlockData();

        if ((blockData instanceof Door && cm.getConfigValue(ALLOW_KNOCKING, Boolean.class))
                || (blockData instanceof TrapDoor && cm.getConfigValue(ALLOW_KNOCKING_TRAPDOORS, Boolean.class))
                || (blockData instanceof Gate && cm.getConfigValue(ALLOW_KNOCKING_GATES, Boolean.class))) {
            this.playKnockSound(block);
        }
    }

    public void playKnockSound(Block block) {
        Location loc = block.getLocation();
        World world = block.getWorld();
        Sound sound = Optional
                .ofNullable(
                        Registry.SOUNDS
                                .get(NamespacedKey.minecraft(cm.getConfigValue(SOUND_KNOCK_WOOD, String.class))))
                .orElse(Sound.ITEM_SHIELD_BLOCK);
        SoundCategory category = Enums
                .getIfPresent(SoundCategory.class,
                        cm.getConfigValue(SOUND_KNOCK_CATEGORY, String.class))
                .or(SoundCategory.BLOCKS);
        float volume = cm.getConfigValue(SOUND_KNOCK_VOLUME, Double.class).floatValue();
        float pitch = cm.getConfigValue(SOUND_KNOCK_PITCH, Double.class).floatValue();

        world.playSound(loc, sound, category, volume, pitch);
    }

    public static void toggleDoor(Block doorBlock, Openable openable, boolean open) {
        openable.setOpen(open);
        doorBlock.setBlockData(openable);
    }

    public Door getBottomDoor(Door door, Block block) {
        Block below = (door.getHalf() == Bisected.Half.BOTTOM) ? block : block.getRelative(BlockFace.DOWN);
        if (below.getType() == block.getType() && below.getBlockData() instanceof Door) {
            return (Door) below.getBlockData();
        }
        return null;
    }

    public Block getOtherPart(Door door, Block block) {
        if (door != null) {
            for (AdjacentBlockRecord neighbour : POSSIBLE_NEIGHBOURS) {
                Block relative = block.getRelative(neighbour.offsetX(), 0, neighbour.offsetZ());
                Door otherDoor = (relative.getBlockData() instanceof Door) ? (Door) relative.getBlockData() : null;
                if (otherDoor != null
                        && neighbour.facing() == door.getFacing()
                        && neighbour.hinge() == door.getHinge()
                        && relative.getType() == block.getType()
                        && otherDoor.getHinge() != neighbour.hinge()
                        && otherDoor.isOpen() == door.isOpen()
                        && otherDoor.getFacing() == neighbour.facing()) {
                    return relative;
                }
            }
        }
        return null;
    }

    public void toggleOtherDoor(Block block, Block otherBlock, boolean open) {
        if (!(block.getBlockData() instanceof Door) || !(otherBlock.getBlockData() instanceof Door))
            return;

        Door door = (Door) block.getBlockData();
        Door otherDoor = (Door) otherBlock.getBlockData();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!(otherBlock.getBlockData() instanceof Door))
                    return;
                Door newDoor = (Door) block.getBlockData();
                if (newDoor.isOpen() == door.isOpen()) {
                    return;
                }
                DoorsModule.toggleDoor(otherBlock, otherDoor, open);
            }
        }.runTaskLater(vp, 1L);
    }

    public Map<String, Object> config() {
        return new HashMap<String, Object>() {
            {
                put(ENABLE, true);
                put(SOUND_KNOCK_CATEGORY, "BLOCKS");
                put(SOUND_KNOCK_PITCH, 1.0);
                put(SOUND_KNOCK_VOLUME, 1.0);
                put(SOUND_KNOCK_WOOD, "entity_zombie_attack_wooden_door");
                put(ALLOW_AUTOCLOSE, true);
                put(ALLOW_DOUBLEDOORS, true);
                put(ALLOW_KNOCKING, true);
                put(ALLOW_KNOCKING_GATES, true);
                put(ALLOW_KNOCKING_TRAPDOORS, true);
                put(KNOCKING_REQUIRES_EMPTY_HAND, true);
                put(KNOCKING_REQUIRES_SHIFT, false);
                put(AUTOCLOSE_DELAY, 6);
            }
        };
    }

}
