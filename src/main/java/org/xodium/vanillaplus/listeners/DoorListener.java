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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.xodium.vanillaplus.VanillaPlus;
import org.xodium.vanillaplus.interfaces.CONFIG;
import org.xodium.vanillaplus.interfaces.PERMS;
import org.xodium.vanillaplus.managers.DoorManager;

public class DoorListener implements Listener {
    private final HashMap<Block, Long> autoClose = new HashMap<>();
    private final VanillaPlus plugin = VanillaPlus.getInstance();
    private final FileConfiguration config = plugin.getConfig();

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
                            Block otherDoor = DoorManager.getOtherPart((Door) openable, block);
                            if (otherDoor != null) {
                                DoorManager.toggleOtherDoor(block, otherDoor, false);
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
                || !e.getPlayer().hasPermission(PERMS.DoorsPlus.USE)
                || !config.getBoolean(CONFIG.DoorsPlus.ALLOW_DOUBLEDOORS)
                || !(blockData instanceof Door
                        || blockData instanceof Gate))
            return;

        if (blockData instanceof Door) {
            Door door = DoorManager.getBottomDoor((Door) blockData, clickedBlock);
            Block otherDoorBlock = DoorManager.getOtherPart(door, clickedBlock);
            if (otherDoorBlock != null && otherDoorBlock.getBlockData() instanceof Door) {
                Door otherDoor = (Door) otherDoorBlock.getBlockData();
                DoorManager.toggleOtherDoor(clickedBlock, otherDoorBlock, !otherDoor.isOpen());
                autoClose.put(otherDoorBlock,
                        System.currentTimeMillis()
                                + Long.valueOf(config.getInt(CONFIG.DoorsPlus.AUTOCLOSE_DELAY)) * 1000);
            }
        }
        autoClose.put(clickedBlock,
                System.currentTimeMillis() + Long.valueOf(config.getInt(CONFIG.DoorsPlus.AUTOCLOSE_DELAY)) * 1000);
    }

    @EventHandler
    public void onKnock(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        GameMode gameMode = p.getGameMode();

        if (gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR)
            return;

        if (!p.hasPermission(PERMS.DoorsPlus.KNOCK) || e.getAction() != Action.LEFT_CLICK_BLOCK
                || e.getHand() != EquipmentSlot.HAND)
            return;

        if (config.getBoolean(CONFIG.DoorsPlus.KNOCKING_REQUIRES_SHIFT) && !p.isSneaking())
            return;

        if (config.getBoolean(CONFIG.DoorsPlus.KNOCKING_REQUIRES_EMPTY_HAND)
                && p.getInventory().getItemInMainHand().getType() != Material.AIR)
            return;

        if (e.getClickedBlock() == null)
            return;

        Block block = e.getClickedBlock();
        BlockData blockData = block.getBlockData();

        if ((blockData instanceof Door && config.getBoolean(CONFIG.DoorsPlus.ALLOW_KNOCKING))
                || (blockData instanceof TrapDoor && config.getBoolean(CONFIG.DoorsPlus.ALLOW_KNOCKING_TRAPDOORS))
                || (blockData instanceof Gate && config.getBoolean(CONFIG.DoorsPlus.ALLOW_KNOCKING_GATES))) {
            DoorManager.playKnockSound(block);
        }
    }

}
