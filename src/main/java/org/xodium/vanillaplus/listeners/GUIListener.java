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
import org.xodium.vanillaplus.interfaces.CONFIG;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

// TODO: add action to each setting (based on value) and save the value to the database.
public class GUIListener implements Listener {
    private final static Database db = new Database();
    private static final Map<Player, Inventory> playerInventories = new HashMap<>();
    private static final Map<Player, Integer> playerPageIndices = new HashMap<>();
    private static final List<GUISettings> settings = new ArrayList<>();
    static {
        settings.add(
                new GUISettings(Material.OAK_DOOR, "Knock on Wooden Door", List.of(CONFIG.SOUND_KNOCK_WOOD), (p) -> {
                    // This is where the action should be performed
                }));
        settings.add(new GUISettings(Material.BELL, "Set Knock Volume", List.of(CONFIG.SOUND_KNOCK_VOLUME), (p) -> {
            // This is where the action should be performed
        }));
        settings.add(new GUISettings(Material.NOTE_BLOCK, "Set Knock Pitch", List.of(CONFIG.SOUND_KNOCK_PITCH), (p) -> {
            // This is where the action should be performed
        }));
        settings.add(
                new GUISettings(Material.JUKEBOX, "Set Knock Category", List.of(CONFIG.SOUND_KNOCK_CATEGORY), (p) -> {
                    // This is where the action should be performed
                }));
        settings.add(new GUISettings(Material.LEVER, "Allow Knocking", List.of(CONFIG.ALLOW_KNOCKING), (p) -> {
            // This is where the action should be performed
        }));
        settings.add(new GUISettings(Material.SPRUCE_TRAPDOOR, "Allow Knocking on Trapdoors",
                List.of(CONFIG.ALLOW_KNOCKING_TRAPDOORS), (p) -> {
                    // This is where the action should be performed
                }));
        settings.add(new GUISettings(Material.OAK_FENCE_GATE, "Allow Knocking on Gates",
                List.of(CONFIG.ALLOW_KNOCKING_GATES), (p) -> {
                    // This is where the action should be performed
                }));
        settings.add(new GUISettings(Material.CLOCK, "Allow Auto-Close", List.of(CONFIG.ALLOW_AUTOCLOSE), (p) -> {
            // This is where the action should be performed
        }));
        settings.add(
                new GUISettings(Material.RED_CARPET, "Require Shift to Knock", List.of(CONFIG.KNOCKING_REQUIRES_SHIFT),
                        (p) -> {
                            // This is where the action should be performed
                        }));
        settings.add(new GUISettings(Material.BARRIER, "Require Empty Hand to Knock",
                List.of(CONFIG.KNOCKING_REQUIRES_EMPTY_HAND), (p) -> {
                    // This is where the action should be performed
                }));
        settings.add(
                new GUISettings(Material.SPRUCE_DOOR, "Allow Double Doors", List.of(CONFIG.ALLOW_DOUBLEDOORS), (p) -> {
                    // This is where the action should be performed
                }));
        settings.add(new GUISettings(Material.CLOCK, "Set Auto-Close Delay", List.of(CONFIG.AUTOCLOSE_DELAY), (p) -> {
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
            inventorySize = (int) Math.ceil(inventorySize / 9.0) * 9;

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
