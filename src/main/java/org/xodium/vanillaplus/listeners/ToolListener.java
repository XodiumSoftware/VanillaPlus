package org.xodium.vanillaplus.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.xodium.vanillaplus.interfaces.ITEMS;
import org.xodium.vanillaplus.managers.ItemManager;

public class ToolListener implements Listener {
    private static final int DAMAGE_AMOUNT = 1;

    // TODO: MAKE USE OF THE DEBUG STICK WITH LIMITATIONS!!!
    // TODO: change the way we identify the item.
    // TODO: damage is not being applied anymore.
    @EventHandler
    public void onPlayerUseTool(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item == null) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(ITEMS.CHISEL_KEY, PersistentDataType.STRING)) {
            return;
        }
        Block block = e.getClickedBlock();
        if (block == null) {
            return;
        }
        BlockData blockData = block.getBlockData();
        if (!(blockData instanceof Stairs || blockData instanceof Slab)) {
            return;
        }
        ItemManager.applyDamage(e.getPlayer(), item, DAMAGE_AMOUNT);
    }
}