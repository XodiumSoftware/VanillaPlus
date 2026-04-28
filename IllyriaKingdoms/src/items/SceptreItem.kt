package org.xodium.illyriaplus.items

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.IllyriaKingdoms.Companion.instance
import org.xodium.illyriaplus.guis.MainGui
import org.xodium.illyriaplus.interfaces.ItemInterface
import org.xodium.illyriaplus.pdcs.ItemPDC.isSceptre
import org.xodium.illyriaplus.pdcs.ItemPDC.sceptreMode

/** Kingdom Tool item (Sceptre) used for accessing kingdom management GUI. */
internal object SceptreItem : ItemInterface {
    /** The operating modes for the Kingdom Sceptre. */
    enum class SceptreMode {
        GUI,
        TRIGGERS,
    }

    /** The kingdom tool item stack with custom name, lore, and PDC flag. */
    @Suppress("UnstableApiUsage")
    override val item =
        ItemStack.of(Material.STICK).apply {
            setData(
                DataComponentTypes.ITEM_NAME,
                mm.deserialize("<gradient:#FFA751:#FFE259>Kingdom Sceptre</gradient>"),
            )
            setData(
                DataComponentTypes.LORE,
                ItemLore.lore(
                    listOf(
                        Component.empty(),
                        mm.deserialize("<gray>Right-click to open your kingdom menu.</gray>"),
                    ),
                ),
            )
            setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            setData(
                DataComponentTypes.ITEM_MODEL,
                Key.key(instance, TODO("set key to link with custom texture in resourcepack")),
            )
            isSceptre = true
            sceptreMode = SceptreMode.GUI
        }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) {
        val item = event.item ?: return

        if (!item.isSceptre) return

        event.isCancelled = true

        when (event.action) {
            Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> handleLeftClick(event, item)
            Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> handleRightClick(event, item)
            else -> return
        }
    }

    private fun handleLeftClick(
        event: PlayerInteractEvent,
        item: ItemStack,
    ) {
        when (item.sceptreMode) {
            SceptreMode.GUI -> {
                MainGui.window.open(event.player)
            }

            SceptreMode.TRIGGERS -> {
                // TODO: Triggers mode - boundary wand logic here
            }
        }
    }

    private fun handleRightClick(
        event: PlayerInteractEvent,
        item: ItemStack,
    ) {
        item.sceptreMode =
            when (item.sceptreMode) {
                SceptreMode.GUI -> SceptreMode.TRIGGERS
                SceptreMode.TRIGGERS -> SceptreMode.GUI
            }

        event.player.sendActionBar(mm.deserialize("<gray>Mode:</gray> <yellow>${item.sceptreMode}</yellow>"))
    }
}
