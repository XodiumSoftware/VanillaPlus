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
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.xodium.vanillaplus.Database;
import org.xodium.vanillaplus.VanillaPlus;
import org.xodium.vanillaplus.interfaces.ModuleInterface;
import org.xodium.vanillaplus.records.AdjacentBlockRecord;

import com.google.common.base.Enums;

// TODO: refactor.
public class DoorsModule implements ModuleInterface {
    private final String cn = getClass().getSimpleName();
    private static final VanillaPlus vp = VanillaPlus.getInstance();
    private static final Database db = new Database();

    private interface CONFIG extends ModuleInterface.CONFIG {
        // Sound settings
        String SOUND_KNOCK_CATEGORY = ".sound_knock_category";
        String SOUND_KNOCK_PITCH = ".sound_knock_pitch";
        String SOUND_KNOCK_VOLUME = ".sound_knock_volume";
        String SOUND_KNOCK_WOOD = ".sound_knock_wood";

        // Behavior settings
        String ALLOW_AUTOCLOSE = ".allow_autoclose";
        String ALLOW_DOUBLEDOORS = ".allow_doubledoors";
        String ALLOW_KNOCKING = ".allow_knocking";
        String ALLOW_KNOCKING_GATES = ".allow_knocking_gates";
        String ALLOW_KNOCKING_TRAPDOORS = ".allow_knocking_trapdoors";
        String KNOCKING_REQUIRES_EMPTY_HAND = ".knocking_requires_empty_hand";
        String KNOCKING_REQUIRES_SHIFT = ".knocking_requires_shift";

        // Auto-close settings
        String AUTOCLOSE_DELAY = ".autoclose_delay";
    }

    private interface PERMS {
        String USE = vp.getClass().getSimpleName() + ".doubledoors";
        String KNOCK = vp.getClass().getSimpleName() + ".knock";
        String AUTOCLOSE = vp.getClass().getSimpleName() + ".autoclose";
    }

    private final HashMap<Block, Long> autoClose = new HashMap<>();
    private static final AdjacentBlockRecord[] POSSIBLE_NEIGHBOURS = {
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
                || !e.getPlayer().hasPermission(PERMS.USE)
                || !(blockData instanceof Door || blockData instanceof Gate)
                || !db.getData(cn + CONFIG.ALLOW_DOUBLEDOORS, Boolean.class))
            return;

        if (blockData instanceof Door) {
            Door door = this.getBottomDoor((Door) blockData, clickedBlock);
            Block otherDoorBlock = this.getOtherPart(door, clickedBlock);
            if (otherDoorBlock != null && otherDoorBlock.getBlockData() instanceof Door) {
                Door otherDoor = (Door) otherDoorBlock.getBlockData();
                this.toggleOtherDoor(clickedBlock, otherDoorBlock, !otherDoor.isOpen());
                if (e.getPlayer().hasPermission(PERMS.AUTOCLOSE)) {
                    autoClose.put(otherDoorBlock,
                            System.currentTimeMillis()
                                    + db.getData(cn + CONFIG.AUTOCLOSE_DELAY, Long.class) * 1000);
                }
            }
        }
        if (e.getPlayer().hasPermission(PERMS.AUTOCLOSE)) {
            autoClose.put(clickedBlock,
                    System.currentTimeMillis() + db.getData(cn + CONFIG.AUTOCLOSE_DELAY, Long.class) * 1000);
        }
    }

    @EventHandler
    public void onKnock(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        GameMode gm = p.getGameMode();

        if ((gm == GameMode.CREATIVE
                || gm == GameMode.SPECTATOR)
                || (!p.hasPermission(PERMS.KNOCK)
                        || e.getAction() != Action.LEFT_CLICK_BLOCK
                        || e.getHand() != EquipmentSlot.HAND)
                || (db.getData(cn + CONFIG.KNOCKING_REQUIRES_SHIFT, Boolean.class) && !p.isSneaking())
                || (db.getData(cn + CONFIG.KNOCKING_REQUIRES_EMPTY_HAND, Boolean.class)
                        && p.getInventory().getItemInMainHand().getType() != Material.AIR)
                || (e.getClickedBlock() == null)) {
            return;
        }

        Block block = e.getClickedBlock();
        BlockData blockData = block.getBlockData();

        if ((blockData instanceof Door && db.getData(cn + CONFIG.ALLOW_KNOCKING, Boolean.class))
                || (blockData instanceof TrapDoor && db.getData(cn + CONFIG.ALLOW_KNOCKING_TRAPDOORS, Boolean.class))
                || (blockData instanceof Gate && db.getData(cn + CONFIG.ALLOW_KNOCKING_GATES, Boolean.class))) {
            this.playKnockSound(block);
        }
    }

    public void playKnockSound(Block block) {
        Location loc = block.getLocation();
        World world = block.getWorld();
        Sound sound = Optional
                .ofNullable(
                        Registry.SOUNDS
                                .get(NamespacedKey.minecraft(
                                        db.getData(cn + CONFIG.SOUND_KNOCK_WOOD, String.class).toLowerCase())))
                .orElse(Sound.ITEM_SHIELD_BLOCK);
        SoundCategory category = Enums
                .getIfPresent(SoundCategory.class,
                        db.getData(cn + CONFIG.SOUND_KNOCK_CATEGORY, String.class).toUpperCase())
                .or(SoundCategory.BLOCKS);
        float volume = db.getData(cn + CONFIG.SOUND_KNOCK_VOLUME, Float.class);
        float pitch = db.getData(cn + CONFIG.SOUND_KNOCK_PITCH, Float.class);

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
    public boolean enabled() {
        return db.getData(cn + CONFIG.ENABLE, Boolean.class);
    }

    @Override
    public void config() {
        db.setData(cn + CONFIG.ENABLE, true);
        db.setData(cn + CONFIG.SOUND_KNOCK_CATEGORY, "BLOCKS");
        db.setData(cn + CONFIG.SOUND_KNOCK_PITCH, 1.0);
        db.setData(cn + CONFIG.SOUND_KNOCK_VOLUME, 1.0);
        db.setData(cn + CONFIG.SOUND_KNOCK_WOOD, "entity_zombie_attack_wooden_door");
        db.setData(cn + CONFIG.ALLOW_AUTOCLOSE, true);
        db.setData(cn + CONFIG.ALLOW_DOUBLEDOORS, true);
        db.setData(cn + CONFIG.ALLOW_KNOCKING, true);
        db.setData(cn + CONFIG.ALLOW_KNOCKING_GATES, true);
        db.setData(cn + CONFIG.ALLOW_KNOCKING_TRAPDOORS, true);
        db.setData(cn + CONFIG.KNOCKING_REQUIRES_EMPTY_HAND, true);
        db.setData(cn + CONFIG.KNOCKING_REQUIRES_SHIFT, false);
        db.setData(cn + CONFIG.AUTOCLOSE_DELAY, 6);
    }
}
