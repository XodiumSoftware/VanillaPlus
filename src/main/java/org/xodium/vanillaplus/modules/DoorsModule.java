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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.xodium.vanillaplus.VanillaPlus;
import org.xodium.vanillaplus.interfaces.Modular;
import org.xodium.vanillaplus.interfaces.PERMS;
import org.xodium.vanillaplus.records.AdjacentBlockRecord;

import com.google.common.base.Enums;

// TODO: refactor.
public class DoorsModule implements Modular {
    // Sound settings
    public static final String SOUND_KNOCK_CATEGORY = ".sound_knock_category";
    public static final String SOUND_KNOCK_PITCH = ".sound_knock_pitch";
    public static final String SOUND_KNOCK_VOLUME = ".sound_knock_volume";
    public static final String SOUND_KNOCK_WOOD = ".sound_knock_wood";

    // Behavior settings
    public static final String ALLOW_AUTOCLOSE = ".allow_autoclose";
    public static final String ALLOW_DOUBLEDOORS = ".allow_doubledoors";
    public static final String ALLOW_KNOCKING = ".allow_knocking";
    public static final String ALLOW_KNOCKING_GATES = ".allow_knocking_gates";
    public static final String ALLOW_KNOCKING_TRAPDOORS = ".allow_knocking_trapdoors";
    public static final String KNOCKING_REQUIRES_EMPTY_HAND = ".knocking_requires_empty_hand";
    public static final String KNOCKING_REQUIRES_SHIFT = ".knocking_requires_shift";

    // Auto-close settings
    public static final String AUTOCLOSE_DELAY = ".autoclose_delay";

    private final HashMap<Block, Long> autoClose = new HashMap<>();
    private final VanillaPlus vp = VanillaPlus.getInstance();
    private final FileConfiguration fc = vp.getConfig();
    private final String className = getClass().getSimpleName();
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
                || !fc.getBoolean(ALLOW_DOUBLEDOORS)
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
                                + Long.valueOf(fc.getLong(className + AUTOCLOSE_DELAY)) * 1000);
            }
        }
        autoClose.put(clickedBlock,
                System.currentTimeMillis() + Long.valueOf(fc.getLong(className + AUTOCLOSE_DELAY)) * 1000);
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

        if (fc.getBoolean(className + KNOCKING_REQUIRES_SHIFT) && !p.isSneaking())
            return;

        if (fc.getBoolean(className + KNOCKING_REQUIRES_EMPTY_HAND)
                && p.getInventory().getItemInMainHand().getType() != Material.AIR)
            return;

        if (e.getClickedBlock() == null)
            return;

        Block block = e.getClickedBlock();
        BlockData blockData = block.getBlockData();

        if ((blockData instanceof Door && fc.getBoolean(className + ALLOW_KNOCKING))
                || (blockData instanceof TrapDoor && fc.getBoolean(className + ALLOW_KNOCKING_TRAPDOORS))
                || (blockData instanceof Gate && fc.getBoolean(className + ALLOW_KNOCKING_GATES))) {
            this.playKnockSound(block);
        }
    }

    public void playKnockSound(Block block) {
        Location loc = block.getLocation();
        World world = block.getWorld();
        Sound sound = Optional
                .ofNullable(
                        Registry.SOUNDS
                                .get(NamespacedKey.minecraft(fc.getString(className + SOUND_KNOCK_WOOD))))
                .orElse(Sound.ITEM_SHIELD_BLOCK);
        SoundCategory category = Enums
                .getIfPresent(SoundCategory.class,
                        fc.getString(className + SOUND_KNOCK_CATEGORY))
                .or(SoundCategory.BLOCKS);
        float volume = fc.getInt(className + SOUND_KNOCK_VOLUME);
        float pitch = fc.getInt(className + SOUND_KNOCK_PITCH);

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

    @Override
    public boolean isEnabled() {
        return fc.getBoolean(className + ENABLE);
    }

    @Override
    public void config() {
        fc.addDefaults(Map.ofEntries(
                Map.entry(className + ENABLE, true),
                Map.entry(className + SOUND_KNOCK_CATEGORY, "BLOCKS"),
                Map.entry(className + SOUND_KNOCK_PITCH, 1.0),
                Map.entry(className + SOUND_KNOCK_VOLUME, 1.0),
                Map.entry(className + SOUND_KNOCK_WOOD, "entity_zombie_attack_wooden_door"),
                Map.entry(className + ALLOW_AUTOCLOSE, true),
                Map.entry(className + ALLOW_DOUBLEDOORS, true),
                Map.entry(className + ALLOW_KNOCKING, true),
                Map.entry(className + ALLOW_KNOCKING_GATES, true),
                Map.entry(className + ALLOW_KNOCKING_TRAPDOORS, true),
                Map.entry(className + KNOCKING_REQUIRES_EMPTY_HAND, true),
                Map.entry(className + KNOCKING_REQUIRES_SHIFT, false),
                Map.entry(className + AUTOCLOSE_DELAY, 6)));
        vp.saveConfig();
    }
}
