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
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.xodium.vanillaplus.VanillaPlus;
import org.xodium.vanillaplus.interfaces.CONFIG;
import org.xodium.vanillaplus.interfaces.PERMS;
import org.xodium.vanillaplus.records.AdjacentBlockRecord;

import com.google.common.base.Enums;

// TODO: refactor.
public class DoorsModule implements Listener {
    public static final String ENABLE = "enable";

    private final HashMap<Block, Long> autoClose = new HashMap<>();
    private final VanillaPlus vp = VanillaPlus.getInstance();
    private final FileConfiguration config = vp.getConfig();
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
                || !config.getBoolean(CONFIG.DOORSMODULE.ALLOW_DOUBLEDOORS)
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
                                + Long.valueOf(config.getInt(CONFIG.DOORSMODULE.AUTOCLOSE_DELAY)) * 1000);
            }
        }
        autoClose.put(clickedBlock,
                System.currentTimeMillis() + Long.valueOf(config.getInt(CONFIG.DOORSMODULE.AUTOCLOSE_DELAY)) * 1000);
    }

    @EventHandler
    public void onKnock(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        GameMode gameMode = p.getGameMode();

        if (gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR)
            return;

        if (!p.hasPermission(PERMS.DOORSMODULE.KNOCK) || e.getAction() != Action.LEFT_CLICK_BLOCK
                || e.getHand() != EquipmentSlot.HAND)
            return;

        if (config.getBoolean(CONFIG.DOORSMODULE.KNOCKING_REQUIRES_SHIFT) && !p.isSneaking())
            return;

        if (config.getBoolean(CONFIG.DOORSMODULE.KNOCKING_REQUIRES_EMPTY_HAND)
                && p.getInventory().getItemInMainHand().getType() != Material.AIR)
            return;

        if (e.getClickedBlock() == null)
            return;

        Block block = e.getClickedBlock();
        BlockData blockData = block.getBlockData();

        if ((blockData instanceof Door && config.getBoolean(CONFIG.DOORSMODULE.ALLOW_KNOCKING))
                || (blockData instanceof TrapDoor && config.getBoolean(CONFIG.DOORSMODULE.ALLOW_KNOCKING_TRAPDOORS))
                || (blockData instanceof Gate && config.getBoolean(CONFIG.DOORSMODULE.ALLOW_KNOCKING_GATES))) {
            this.playKnockSound(block);
        }
    }

    public void playKnockSound(Block block) {
        Location location = block.getLocation();
        World world = block.getWorld();

        Sound sound = Optional
                .ofNullable(
                        Registry.SOUNDS
                                .get(NamespacedKey.minecraft(config.getString(CONFIG.DOORSMODULE.SOUND_KNOCK_WOOD))))
                .orElse(Sound.ITEM_SHIELD_BLOCK);

        SoundCategory category = Enums
                .getIfPresent(SoundCategory.class,
                        config.getString(CONFIG.DOORSMODULE.SOUND_KNOCK_CATEGORY))
                .or(SoundCategory.BLOCKS);

        float volume = (float) config.getDouble(CONFIG.DOORSMODULE.SOUND_KNOCK_VOLUME);
        float pitch = (float) config.getDouble(CONFIG.DOORSMODULE.SOUND_KNOCK_PITCH);

        world.playSound(location, sound, category, volume, pitch);
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

}
