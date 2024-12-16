package org.xodium.vanillaplus.modules;

import com.google.common.base.Enums;
import org.bukkit.*;
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
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.xodium.vanillaplus.VanillaPlus;
import org.xodium.vanillaplus.interfaces.ModuleInterface;
import org.xodium.vanillaplus.records.AdjacentBlockRecord;

import java.util.*;

// TODO: refactor.
public class DoorsModule implements ModuleInterface {
    private final String cn = getClass().getSimpleName();
    private static final VanillaPlus VP = VanillaPlus.getInstance();
    private static final FileConfiguration FC = VP.getConfig();
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
    private final HashMap<Block, Long> autoClose = new HashMap<>();

    {
        Bukkit.getScheduler().runTaskTimer(VP, () -> {
            Iterator<Map.Entry<Block, Long>> it = autoClose.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Block, Long> entry = it.next();
                Block b = entry.getKey();
                Long time = entry.getValue();
                if (System.currentTimeMillis() < time)
                    continue;
                if (b.getBlockData() instanceof Openable openable) {
                    if (openable.isOpen()) {
                        if (openable instanceof Door) {
                            Block otherDoor = getOtherPart((Door) openable, b);
                            if (otherDoor != null) {
                                toggleOtherDoor(b, otherDoor, false);
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
                || !FC.getBoolean(cn + CONFIG.ALLOW_DOUBLEDOORS))
            return;

        if (blockData instanceof Door) {
            Door door = getBottomDoor((Door) blockData, clickedBlock);
            Block otherDoorBlock = getOtherPart(door, clickedBlock);
            if (otherDoorBlock != null && otherDoorBlock.getBlockData() instanceof Door otherDoor) {
                toggleOtherDoor(clickedBlock, otherDoorBlock, !otherDoor.isOpen());
                if (e.getPlayer().hasPermission(PERMS.AUTOCLOSE)) {
                    autoClose.put(otherDoorBlock,
                            System.currentTimeMillis()
                                    + FC.getLong(cn + CONFIG.AUTOCLOSE_DELAY) * 1000);
                }
            }
        }
        if (e.getPlayer().hasPermission(PERMS.AUTOCLOSE)) {
            autoClose.put(clickedBlock,
                    System.currentTimeMillis() + FC.getLong(cn + CONFIG.AUTOCLOSE_DELAY) * 1000);
        }
    }

    @EventHandler
    public void onKnock(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR
                || !p.hasPermission(PERMS.KNOCK) || e.getAction() != Action.LEFT_CLICK_BLOCK
                || e.getHand() != EquipmentSlot.HAND
                || (FC.getBoolean(cn + CONFIG.KNOCKING_REQUIRES_SHIFT) && !p.isSneaking())
                || (FC.getBoolean(cn + CONFIG.KNOCKING_REQUIRES_EMPTY_HAND)
                        && p.getInventory().getItemInMainHand().getType() != Material.AIR)
                || e.getClickedBlock() == null)
            return;

        Block block = e.getClickedBlock();
        BlockData blockData = block.getBlockData();

        if ((blockData instanceof Door && FC.getBoolean(cn + CONFIG.ALLOW_KNOCKING))
                || (blockData instanceof TrapDoor && FC.getBoolean(cn + CONFIG.ALLOW_KNOCKING_TRAPDOORS))
                || (blockData instanceof Gate && FC.getBoolean(cn + CONFIG.ALLOW_KNOCKING_GATES))) {
            playKnockSound(block);
        }
    }

    public void playKnockSound(Block block) {
        Location loc = block.getLocation();
        World world = block.getWorld();
        Sound sound = Optional
                .ofNullable(
                        Registry.SOUNDS
                                .get(NamespacedKey.minecraft(
                                        Objects.requireNonNull(FC.getString(cn + CONFIG.SOUND_KNOCK_WOOD)).toLowerCase())))
                .orElse(Sound.ITEM_SHIELD_BLOCK);
        SoundCategory category = Enums
                .getIfPresent(SoundCategory.class,
                        Objects.requireNonNull(FC.getString(cn + CONFIG.SOUND_KNOCK_CATEGORY)).toUpperCase())
                .or(SoundCategory.BLOCKS);
        float volume = (float) FC.getInt(cn + CONFIG.SOUND_KNOCK_VOLUME);
        float pitch = (float) FC.getInt(cn + CONFIG.SOUND_KNOCK_PITCH);

        world.playSound(loc, sound, category, volume, pitch);
    }

    public void toggleOtherDoor(Block block, Block otherBlock, boolean open) {
        if (!(block.getBlockData() instanceof Door door) || !(otherBlock.getBlockData() instanceof Door otherDoor))
            return;

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
        }.runTaskLater(VP, 1L);
    }

    @Override
    public boolean enabled() {
        return FC.getBoolean(cn + CONFIG.ENABLE);
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

    @Override
    public void config() {
        FC.addDefault(cn + CONFIG.ENABLE, true);
        FC.addDefault(cn + CONFIG.SOUND_KNOCK_CATEGORY, "BLOCKS");
        FC.addDefault(cn + CONFIG.SOUND_KNOCK_PITCH, 1.0);
        FC.addDefault(cn + CONFIG.SOUND_KNOCK_VOLUME, 1.0);
        FC.addDefault(cn + CONFIG.SOUND_KNOCK_WOOD, "entity_zombie_attack_wooden_door");
        FC.addDefault(cn + CONFIG.ALLOW_AUTOCLOSE, true);
        FC.addDefault(cn + CONFIG.ALLOW_DOUBLEDOORS, true);
        FC.addDefault(cn + CONFIG.ALLOW_KNOCKING, true);
        FC.addDefault(cn + CONFIG.ALLOW_KNOCKING_GATES, true);
        FC.addDefault(cn + CONFIG.ALLOW_KNOCKING_TRAPDOORS, true);
        FC.addDefault(cn + CONFIG.KNOCKING_REQUIRES_EMPTY_HAND, true);
        FC.addDefault(cn + CONFIG.KNOCKING_REQUIRES_SHIFT, false);
        FC.addDefault(cn + CONFIG.AUTOCLOSE_DELAY, 6);
        VP.saveConfig();
    }

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
        String USE = VP.getClass().getSimpleName() + ".doubledoors";
        String KNOCK = VP.getClass().getSimpleName() + ".knock";
        String AUTOCLOSE = VP.getClass().getSimpleName() + ".autoclose";
    }
}
