package org.xodium.vanillaplus.listeners;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.xodium.vanillaplus.VanillaPlus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class ToolListener implements Listener {
    private final VanillaPlus plugin = VanillaPlus.getInstance();

    public ItemStack createStairTool() {
        ItemStack tool = new ItemStack(Material.DIAMOND_SWORD);
        NamespacedKey key = new NamespacedKey(plugin, "chisel");

        ItemMeta meta = tool.getItemMeta();
        if (meta != null) {
            PersistentDataContainer data = tool.getItemMeta().getPersistentDataContainer();
            data.set(key, PersistentDataType.STRING, "chisel_modifier");

            meta.displayName(Component.text("Chisel")
                    .color(TextColor.fromHexString("#00FF00")));
            meta.lore(List.of(
                    Component.text("Right click to modify stairs")
                            .color(TextColor.fromHexString("#FFFFFF"))));

            tool.setItemMeta(meta);
        }
        return tool;
    }

    @EventHandler
    public void onPlayerUseTool(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item != null && item.getType() == Material.DIAMOND_SWORD) {
            NamespacedKey key = new NamespacedKey(plugin, "chisel");
            PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();

            if (data.has(key, PersistentDataType.STRING)) {
                e.getPlayer().sendMessage(Component.text("You used the Chisel!")
                        .color(TextColor.fromHexString("#00FF00")));
            }
        }
    }
}
