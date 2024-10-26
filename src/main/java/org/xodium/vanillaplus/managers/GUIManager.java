package org.xodium.vanillaplus.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.xodium.vanillaplus.Database;
import org.xodium.vanillaplus.data.GUISettings;
import org.xodium.vanillaplus.interfaces.CONFIG;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

// TODO: implement saving of settings in db.
public class GUIManager {
    private static final Database db = new Database();
    public final Map<Player, Inventory> playerInventories = new HashMap<>();
    public final Map<Player, Integer> playerPageIndices = new HashMap<>();
    public final List<GUISettings> settings = new ArrayList<>();
    private static final MiniMessage mm = MiniMessage.miniMessage();

    {
        settings.add(
                new GUISettings(Material.OAK_DOOR, "Knock on Wooden Door", List.of(CONFIG.SOUND_KNOCK_WOOD), (p) -> {
                    // Action for knocking sound on wooden door
                }));
        settings.add(new GUISettings(Material.BELL, "Set Knock Volume", List.of(CONFIG.SOUND_KNOCK_VOLUME), (p) -> {
            // Action for setting knock volume
        }));
        settings.add(new GUISettings(Material.NOTE_BLOCK, "Set Knock Pitch", List.of(CONFIG.SOUND_KNOCK_PITCH), (p) -> {
            // Action for setting knock pitch
        }));
        settings.add(
                new GUISettings(Material.JUKEBOX, "Set Knock Category", List.of(CONFIG.SOUND_KNOCK_CATEGORY), (p) -> {
                    // Action for setting knock category
                }));
        settings.add(new GUISettings(Material.LEVER, "Allow Knocking", List.of(CONFIG.ALLOW_KNOCKING), (p) -> {
            // Action for allowing knocking
        }));
        settings.add(new GUISettings(Material.SPRUCE_TRAPDOOR, "Allow Knocking on Trapdoors",
                List.of(CONFIG.ALLOW_KNOCKING_TRAPDOORS), (p) -> {
                    // Action for knocking on trapdoors
                }));
        settings.add(new GUISettings(Material.OAK_FENCE_GATE, "Allow Knocking on Gates",
                List.of(CONFIG.ALLOW_KNOCKING_GATES), (p) -> {
                    // Action for knocking on gates
                }));
        settings.add(new GUISettings(Material.CLOCK, "Allow Auto-Close", List.of(CONFIG.ALLOW_AUTOCLOSE), (p) -> {
            // Action for auto-close
        }));
        settings.add(
                new GUISettings(Material.RED_CARPET, "Require Shift to Knock", List.of(CONFIG.KNOCKING_REQUIRES_SHIFT),
                        (p) -> {
                            // Action for requiring shift to knock
                        }));
        settings.add(new GUISettings(Material.BARRIER, "Require Empty Hand to Knock",
                List.of(CONFIG.KNOCKING_REQUIRES_EMPTY_HAND), (p) -> {
                    // Action for requiring empty hand to knock
                }));
        settings.add(
                new GUISettings(Material.SPRUCE_DOOR, "Allow Double Doors", List.of(CONFIG.ALLOW_DOUBLEDOORS), (p) -> {
                    // Action for allowing double doors
                }));
        settings.add(new GUISettings(Material.CLOCK, "Set Auto-Close Delay", List.of(CONFIG.AUTOCLOSE_DELAY), (p) -> {
            // Action for setting auto-close delay
        }));
    }

    public void openGUI(Player player) {
        if (playerInventories.containsKey(player)) {
            player.closeInventory();
        }

        int currentPageIndex = playerPageIndices.getOrDefault(player, 0);
        Inventory gui = createGUI(player, currentPageIndex);

        playerInventories.put(player, gui);
        player.openInventory(gui);
    }

    private Inventory createGUI(Player player, int pageIndex) {
        int maxSlotsPerPage = 54;
        Inventory gui = Bukkit.createInventory(null, maxSlotsPerPage,
                mm.deserialize("<b><gradient:#CB2D3E:#EF473A>VanillaPlus Settings</gradient></b>"));

        int startIdx = pageIndex * maxSlotsPerPage;
        for (int i = 0; i < maxSlotsPerPage && (startIdx + i) < settings.size(); i++) {
            GUISettings setting = settings.get(startIdx + i);
            ItemStack item = new ItemStack(setting.getMaterial());
            ItemMeta meta = item.getItemMeta();
            meta.displayName(
                    mm.deserialize("<b><gradient:#CB2D3E:#EF473A>" + setting.getDisplayName() + "</gradient></b>"));

            List<Component> loreComponents = new ArrayList<>();
            for (String key : setting.getLore()) {
                Object value = db.getData(key);
                loreComponents.add(mm.deserialize("<b>" + convertToDisplayString(value) + "</b>"));
            }
            meta.lore(loreComponents);
            item.setItemMeta(meta);
            gui.setItem(i, item);
        }

        addPaginationButtons(gui, pageIndex);
        return gui;
    }

    private void addPaginationButtons(Inventory gui, int pageIndex) {
        int maxSlotsPerPage = 54;
        int totalPageCount = (int) Math.ceil(settings.size() / (double) maxSlotsPerPage);

        if (pageIndex > 0) {
            ItemStack previousButton = createButton(Material.ARROW, "Previous Page");
            gui.setItem(maxSlotsPerPage - 9, previousButton);
        }

        if (pageIndex < totalPageCount - 1) {
            ItemStack nextButton = createButton(Material.ARROW, "Next Page");
            gui.setItem(maxSlotsPerPage - 1, nextButton);
        }
    }

    private ItemStack createButton(Material material, String displayName) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        meta.displayName(mm.deserialize(displayName));
        button.setItemMeta(meta);
        return button;
    }

    private String convertToDisplayString(Object value) {
        if (value instanceof Boolean) {
            return ((Boolean) value) ? "<green>True" : "<red>False";
        }
        return value != null ? "<gold>" + value.toString() : "N/A";
    }
}
