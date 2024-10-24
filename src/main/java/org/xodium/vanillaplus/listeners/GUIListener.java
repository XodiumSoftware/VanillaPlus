package org.xodium.vanillaplus.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.xodium.vanillaplus.Database;
import org.xodium.vanillaplus.data.GUISettings;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class GUIListener implements Listener {
    private final static Database db = new Database();
    private static final Map<Player, Inventory> playerInventories = new HashMap<>();
    private static final Map<Player, Integer> playerPageIndices = new HashMap<>();
    private static final List<GUISettings> settings = new ArrayList<>();
    static {
        settings.add(
                new GUISettings(Material.OAK_DOOR, "Knock on Wooden Door", List.of("knock_on_wooden_door"), (p) -> {
                    // This is where the action should be performed
                }));
        settings.add(new GUISettings(Material.BELL, "Set Knock Volume", List.of("set_knock_volume"), (p) -> {
            // This is where the action should be performed
        }));
        settings.add(new GUISettings(Material.NOTE_BLOCK, "Set Knock Pitch", List.of("sound-knock-pitch"), (p) -> {
            // This is where the action should be performed
        }));
        settings.add(new GUISettings(Material.JUKEBOX, "Set Knock Category", List.of("sound-knock-category"), (p) -> {
            // This is where the action should be performed
        }));
        settings.add(new GUISettings(Material.LEVER, "Allow Knocking", List.of("allow-knocking"), (p) -> {
            // This is where the action should be performed
        }));
        settings.add(new GUISettings(Material.SPRUCE_TRAPDOOR, "Allow Knocking on Trapdoors",
                List.of("allow-knocking-trapdoors"), (p) -> {
                    // This is where the action should be performed
                }));
        settings.add(new GUISettings(Material.OAK_FENCE_GATE, "Allow Knocking on Gates",
                List.of("allow-knocking-gates"), (p) -> {
                    // This is where the action should be performed
                }));
        settings.add(new GUISettings(Material.CLOCK, "Allow Auto-Close", List.of("allow-autoclose"), (p) -> {
            // This is where the action should be performed
        }));
        settings.add(new GUISettings(Material.RED_CARPET, "Require Shift to Knock", List.of("knocking-requires-shift"),
                (p) -> {
                    // This is where the action should be performed
                }));
        settings.add(new GUISettings(Material.BARRIER, "Require Empty Hand to Knock",
                List.of("knocking-requires-empty-hand"), (p) -> {
                    // This is where the action should be performed
                }));
        settings.add(new GUISettings(Material.SPRUCE_DOOR, "Allow Double Doors", List.of("allow-doubledoors"), (p) -> {
            // This is where the action should be performed
        }));
        settings.add(new GUISettings(Material.CLOCK, "Set Auto-Close Delay", List.of("autoclose-delay"), (p) -> {
            // This is where the action should be performed
        }));
    }

    public static void openGUI(Player p) {
        int settingsCount = settings.size();
        int maxSlotsPerInventory = 54;
        int inventoryCount = (int) Math.ceil(settingsCount / (double) maxSlotsPerInventory);

        if (playerInventories.containsKey(p)) {
            p.closeInventory();
        }

        for (int invIndex = 0; invIndex < inventoryCount; invIndex++) {
            int inventorySize = Math.min(maxSlotsPerInventory, settingsCount - (invIndex * maxSlotsPerInventory));

            Inventory gui = Bukkit.createInventory(null, inventorySize, MiniMessage.miniMessage()
                    .deserialize("<b><gradient:#CB2D3E:#EF473A>VanillaPlus Settings</gradient></b>"));

            for (int i = 0; i < inventorySize - 9; i++) {
                int settingIndex = (invIndex * maxSlotsPerInventory) + i;
                if (settingIndex >= settingsCount)
                    break;

                GUISettings setting = settings.get(settingIndex);
                ItemStack item = new ItemStack(setting.getMaterial());
                ItemMeta meta = item.getItemMeta();
                meta.displayName(MiniMessage.miniMessage().deserialize(setting.getDisplayName()));

                List<Component> loreComponents = new ArrayList<>();
                for (String key : setting.getLore()) {
                    Object value = db.getData(key);
                    loreComponents.add(Component.text(value != null ? value.toString() : "N/A"));
                }

                meta.lore(loreComponents);
                item.setItemMeta(meta);
                gui.setItem(i, item);
            }

            if (invIndex > 0) {
                ItemStack previousButton = new ItemStack(Material.ARROW);
                ItemMeta previousMeta = previousButton.getItemMeta();
                previousMeta.displayName(MiniMessage.miniMessage()
                        .deserialize("Previous Page"));
                previousButton.setItemMeta(previousMeta);
                gui.setItem(gui.getSize() - 9, previousButton);
            }

            if (invIndex < inventoryCount - 1) {
                ItemStack nextButton = new ItemStack(Material.ARROW);
                ItemMeta nextMeta = nextButton.getItemMeta();
                nextMeta.displayName(MiniMessage.miniMessage()
                        .deserialize("Next Page"));
                nextButton.setItemMeta(nextMeta);
                gui.setItem(gui.getSize() - 1, nextButton);
            }

            playerInventories.put(p, gui);
            p.openInventory(gui);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (MiniMessage.miniMessage().serialize(e.getView().title()).contains("VanillaPlus Settings")) {
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            ItemStack clickedItem = e.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR)
                return;

            if (clickedItem.getType() == Material.ARROW) {
                String clickedItemName = MiniMessage.miniMessage().serialize(clickedItem.getItemMeta().displayName());

                if (clickedItemName.contains("Previous Page")) {
                    if (playerPageIndices.getOrDefault(p, 0) > 0) {
                        playerPageIndices.put(p, playerPageIndices.get(p) - 1);
                        openGUI(p);
                    }
                    return;
                } else if (clickedItemName.contains("Next Page")) {
                    int settingsCount = settings.size();
                    int maxSlotsPerInventory = 54;
                    if (playerPageIndices.getOrDefault(p,
                            0) < (int) Math.ceil(settingsCount / (double) maxSlotsPerInventory) - 1) {
                        playerPageIndices.put(p, playerPageIndices.get(p) + 1);
                        openGUI(p);
                    }
                    return;
                }
            }

            settings.stream()
                    .filter(setting -> setting.getMaterial() == clickedItem.getType())
                    .findFirst()
                    .ifPresent(setting -> setting.getAction().accept(p));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        playerInventories.remove(e.getPlayer());
    }
}
