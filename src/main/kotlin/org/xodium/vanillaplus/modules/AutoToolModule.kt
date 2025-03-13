/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerQuitEvent
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.old.AutoToolsHandler
import org.xodium.vanillaplus.old.AutoToolsHandler.Tool
import org.xodium.vanillaplus.registries.MaterialRegistry

class AutoToolModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigData.AutoToolModule().enabled

    val handler: AutoToolsHandler = AutoToolsHandler()

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: PlayerQuitEvent): Unit = TODO("check database if enabled")

    init {
        for ((c, t) in listOf(
            MaterialRegistry.WEAPONS to handler.weapons,
            MaterialRegistry.INSTA_BREAKABLE_BY_HAND to handler.instaBreakableByHand,
            Tag.ITEMS_HOES.values.toSet() to handler.hoes,
            Tag.ITEMS_PICKAXES.values.toSet() to handler.pickaxes,
            Tag.ITEMS_AXES.values.toSet() to handler.axes,
            Tag.ITEMS_SHOVELS.values.toSet() to handler.shovels,
            Tag.ITEMS_SWORDS.values.toSet() to handler.swords,
            MaterialRegistry.NETHERITE_TOOLS to handler.allTools,
            MaterialRegistry.DEFAULT_MATERIALS to handler.allTools
        )) t.addAll(c)

        tagToMap(Tag.MINEABLE_AXE, Tool.AXE)
        tagToMap(Tag.MINEABLE_HOE, Tool.HOE)
        tagToMap(Tag.MINEABLE_PICKAXE, Tool.PICKAXE)
        tagToMap(Tag.MINEABLE_SHOVEL, Tool.SHOVEL)

        // NONE SPECIFIC
        tagToMap(Tag.FLOWERS, Tool.NONE)

        // CUSTOM
//        addToMap(Material.GLOWSTONE, Tool.PICKAXE) // TODO: Prefer SilkTouch
    }

    /**
     * Adds materials from the given tag to the tool map.
     * @param tag The material tag.
     * @param tool The tool type.
     */
    private fun tagToMap(tag: Tag<Material>, tool: Tool) = tagToMap(tag, tool, null)

    /**
     * Adds materials from the given tag to the tool map.
     * @param tag The material tag.
     * @param tool The tool type.
     * @param filter If not null, only materials whose name contains this filter will be added.
     */
    private fun tagToMap(tag: Tag<Material>, tool: Tool, filter: String?) {
        tag.values.forEach { material ->
            if (filter == null || material.name.contains(filter)) {
                addToMap(material, tool)
            }
        }
    }

    /**
     * Adds materials from the given material to the tool map.
     * @param material The material tag.
     * @param tool The tool type.
     */
    private fun addToMap(material: Material, tool: Tool) {
        handler.toolMap[material] = tool
    }

    val guiItem = ItemBuilder.from(Material.MILK_BUCKET)
        .name(Utils.mangoFormat("AutoTool").mm())
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click to toggle <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Auto switch to best tool",
                "   <gray>Shortcut: <gold>/autotool | /at"
            ).mm()
        )
        .asGuiItem { player, _ -> toggle(player) }

    fun toggle(player: Player) {
        TODO("toggle in database")
    }
}