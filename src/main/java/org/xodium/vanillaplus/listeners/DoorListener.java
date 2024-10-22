package org.xodium.vanillaplus.listeners;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.event.Event;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
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
import org.xodium.vanillaplus.Database;
import org.xodium.vanillaplus.VanillaPlus;
import org.xodium.vanillaplus.handlers.DoorHandler;
import org.xodium.vanillaplus.interfaces.CONFIG;
import org.xodium.vanillaplus.interfaces.PERMS;

public class DoorListener implements Listener {
    // TODO: move hashmap to database
    private final HashMap<Block, Long> autoClose = new HashMap<>();
    private final VanillaPlus plugin = VanillaPlus.getInstance();
    private final Database db = new Database();

    {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Iterator<Map.Entry<Block, Long>> it = autoClose.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Block, Long> entry = it.next();
                Block block = entry.getKey();
                Long time = entry.getValue();
                if (System.currentTimeMillis() < time)
                    continue;
                if (block.getBlockData() instanceof Openable) {
                    Openable openable = (Openable) block.getBlockData();
                    if (openable.isOpen()) {
                        if (openable instanceof Door) {
                            Block otherDoor = DoorHandler.getOtherPart((Door) openable, block);
                            if (otherDoor != null) {
                                DoorHandler.toggleOtherDoor(block, otherDoor, false, false);
                            }
                        } else if (openable instanceof Gate) {
                            block.getWorld().playSound(block.getLocation(), Sound.BLOCK_FENCE_GATE_CLOSE, 1.0f, 1.0f);
                        }
                        openable.setOpen(false);
                        block.setBlockData(openable);
                        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1.0f, 1.0f);
                    }
                }
                it.remove();
            }
        }, 1, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIronDoor(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND
                || e.getAction() != Action.RIGHT_CLICK_BLOCK
                || e.useInteractedBlock() == Event.Result.DENY
                || e.useItemInHand() == Event.Result.DENY
                || !(e.getClickedBlock().getType() == Material.IRON_DOOR
                        || e.getClickedBlock().getType() == Material.IRON_TRAPDOOR)
                || !Boolean.parseBoolean((String) db.getData(CONFIG.ALLOW_IRONDOORS))
                || !e.getPlayer().hasPermission(PERMS.IRONDOORS))
            return;

        Block block = e.getClickedBlock();
        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1.0f, 1.0f);

        Openable door = (Openable) block.getBlockData();
        door.setOpen(!door.isOpen());
        onRightClick(e);
        block.setBlockData(door);

        autoClose.put(block,
                System.currentTimeMillis() + ((Long) db.getData(CONFIG.AUTOCLOSE_DELAY)) * 1000);
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
                || !Boolean.parseBoolean((String) db.getData(CONFIG.ALLOW_DOUBLEDOORS))
                || !(blockData instanceof Door
                        || blockData instanceof Gate))
            return;

        if (blockData instanceof Door) {
            Door door = DoorHandler.getBottomDoor((Door) blockData, clickedBlock);
            Block otherDoorBlock = DoorHandler.getOtherPart(door, clickedBlock);
            if (otherDoorBlock != null && otherDoorBlock.getBlockData() instanceof Door) {
                Door otherDoor = (Door) otherDoorBlock.getBlockData();
                DoorHandler.toggleOtherDoor(clickedBlock, otherDoorBlock, !otherDoor.isOpen(), false);
                autoClose.put(otherDoorBlock,
                        System.currentTimeMillis()
                                + (Long) db.getData(CONFIG.AUTOCLOSE_DELAY) * 1000);
            }
        }
        autoClose.put(clickedBlock,
                System.currentTimeMillis() + (Long) db.getData(CONFIG.AUTOCLOSE_DELAY) * 1000);
    }

    @EventHandler
    public void onKnock(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        GameMode gameMode = p.getGameMode();

        if (gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR
                || !p.hasPermission(PERMS.KNOCK)
                || e.getAction() != Action.LEFT_CLICK_BLOCK
                || e.getHand() != EquipmentSlot.HAND
                || (Boolean.parseBoolean((String) db.getData(CONFIG.KNOCKING_REQUIRES_SHIFT)) && !p.isSneaking())
                || (Boolean.parseBoolean((String) db.getData(CONFIG.KNOCKING_REQUIRES_EMPTY_HAND))
                        && p.getInventory().getItemInMainHand().getType() != Material.AIR)
                || e.getClickedBlock() == null)
            return;

        Block block = e.getClickedBlock();
        BlockData blockData = block.getBlockData();

        if ((blockData instanceof Door && Boolean.parseBoolean((String) db.getData(CONFIG.ALLOW_KNOCKING)))
                || (blockData instanceof TrapDoor
                        && Boolean.parseBoolean((String) db.getData(CONFIG.ALLOW_KNOCKING_TRAPDOORS)))
                || (blockData instanceof Gate
                        && Boolean.parseBoolean((String) db.getData(CONFIG.ALLOW_KNOCKING_GATES)))) {
            DoorHandler.playKnockSound(block);
        }
    }
}
