@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.Utils.MM
import java.util.*

/** Represents a module handling armor stand mechanics within the system. */
internal object ArmorStandModule : ModuleInterface {
    private val armorStandViews = WeakHashMap<InventoryView, ArmorStand>()

    @EventHandler
    fun on(event: PlayerInteractAtEntityEvent) = handleArmorStandInventory(event)

    @EventHandler
    fun on(event: InventoryClickEvent) {
        armorStandViews[event.view] ?: return
        event.isCancelled = true
    }

    @EventHandler
    fun on(event: InventoryCloseEvent) {
        armorStandViews.remove(event.view)
    }

    /**
     * Handles the interaction with an ArmorStand's inventory.
     * @param event EntityInteractEvent The event triggered by the interaction.
     */
    private fun handleArmorStandInventory(event: PlayerInteractAtEntityEvent) {
        val armorStand = event.rightClicked as? ArmorStand ?: return
        val player = event.player

        if (!player.isSneaking) return

        event.isCancelled = true

        val view = armorStand.menu(player)

        armorStandViews[view] = armorStand
        view.open()
    }

    /**
     * Creates a menu for the given ArmorStand and Player.
     * @receiver ArmorStand The ArmorStand for which the menu is created.
     * @param player Player The player for whom the menu is created.
     * @return InventoryView The created menu view.
     */
    @Suppress("UnstableApiUsage")
    private fun ArmorStand.menu(player: Player): InventoryView =
        MenuType
            .GENERIC_9X6
            .builder()
            .title(customName() ?: MM.deserialize(name))
            .build(player)
            .apply {
                topInventory
                    .apply {
                        fill()
                    }
            }

    /**
     * Fills the inventory with the configured fill item.
     * @receiver Inventory The inventory to be filled.
     */
    private fun Inventory.fill() {
        for (i in 0 until size) {
            setItem(
                i,
                @Suppress("UnstableApiUsage")
                ItemStack.of(config.armorStandModule.menuFillItemMaterial).apply {
                    setData(
                        DataComponentTypes.TOOLTIP_DISPLAY,
                        TooltipDisplay.tooltipDisplay().hideTooltip(config.armorStandModule.menuFIllItemTooltip),
                    )
                },
            )
        }
    }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
        var menuFillItemMaterial: Material = Material.BLACK_STAINED_GLASS_PANE,
        var menuFIllItemTooltip: Boolean = true,
    )
}
