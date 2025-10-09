package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.click.MoveResult
import dev.triumphteam.gui.click.action.GuiClickAction
import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import dev.triumphteam.gui.paper.kotlin.builder.chestContainer
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import kotlin.time.Duration.Companion.seconds

/** Represents a module handling armor stand mechanics within the system. */
internal class ArmorStandModule : ModuleInterface<ArmorStandModule.Config> {
    override val config: Config = Config()

    @EventHandler
    fun on(event: PlayerInteractAtEntityEvent) {
        if (!enabled() ||
            event.rightClicked !is ArmorStand ||
            event.player.isSneaking ||
            event.player.inventory.itemInMainHand.type == Material.NAME_TAG
        ) {
            return
        }
        gui(event.rightClicked as ArmorStand).open(event.player)
        event.isCancelled = true
    }

    /**
     * Builds a GUI for interacting with an armor stand entity.
     * @param armorStand The armor stand entity to create the GUI for.
     * @return A configured Gui instance ready to be displayed to players.
     */
    private fun gui(armorStand: ArmorStand): Gui =
        buildGui {
            containerType = chestContainer { rows = 6 }
            spamPreventionDuration = config.guiSpamPreventionDuration.seconds
            title(armorStand.customName() ?: armorStand.name.mm())
            statelessComponent { component ->
                // Filler slots
                repeat(53) { slot ->
                    component[slot] =
                        ItemBuilder
                            .from(config.guiFillerMaterial)
                            .name(config.i18n.guiFillerItemName.mm())
                            .asGuiItem()
                }
                // Helmet slot
                component[13] =
                    ItemBuilder
                        .from(armorStand.equipment.helmet)
                        .asGuiItem(
                            GuiClickAction.movable { player, ctx ->
                                val cursor = player.inventory.itemInMainHand
                                val equipment = armorStand.equipment
                                if (Tag.ITEMS_HEAD_ARMOR.isTagged(cursor.type)) equipment.setHelmet(cursor)
                                ctx.guiView.open()
                                MoveResult.ALLOW
                            },
                        )
                // Main Hand slot
                component[21] =
                    ItemBuilder
                        .from(armorStand.equipment.itemInMainHand)
                        .asGuiItem(
                            GuiClickAction.movable { player, ctx ->
                                val cursor = player.inventory.itemInMainHand
                                val equipment = armorStand.equipment
                                if (cursor.type != Material.AIR) equipment.setItemInMainHand(cursor)
                                ctx.guiView.open()
                                MoveResult.ALLOW
                            },
                        )
                // Chestplate slot
                component[22] =
                    ItemBuilder
                        .from(armorStand.equipment.chestplate)
                        .asGuiItem(
                            GuiClickAction.movable { player, ctx ->
                                val cursor = player.inventory.itemInMainHand
                                val equipment = armorStand.equipment
                                if (Tag.ITEMS_CHEST_ARMOR.isTagged(cursor.type)) equipment.setChestplate(cursor)
                                ctx.guiView.open()
                                MoveResult.ALLOW
                            },
                        )
                // Offhand slot
                component[23] =
                    ItemBuilder
                        .from(armorStand.equipment.itemInOffHand)
                        .asGuiItem(
                            GuiClickAction.movable { player, ctx ->
                                val cursor = player.inventory.itemInMainHand
                                val equipment = armorStand.equipment
                                if (cursor.type != Material.AIR) equipment.setItemInOffHand(cursor)
                                ctx.guiView.open()
                                MoveResult.ALLOW
                            },
                        )
                // Leggings slot
                component[31] =
                    ItemBuilder
                        .from(armorStand.equipment.leggings)
                        .asGuiItem(
                            GuiClickAction.movable { player, ctx ->
                                // TODO: dont take from main hand instead take from cursor dragging. or dropped item into slot.
                                val cursor = player.inventory.itemInMainHand
                                val equipment = armorStand.equipment
                                if (Tag.ITEMS_LEG_ARMOR.isTagged(cursor.type)) equipment.setLeggings(cursor)
                                ctx.guiView.open()
                                MoveResult.ALLOW
                            },
                        )
                // Boots slot
                component[40] =
                    ItemBuilder
                        .from(armorStand.equipment.boots)
                        .asGuiItem(
                            GuiClickAction.movable { player, ctx ->
                                val cursor = player.inventory.itemInMainHand
                                val equipment = armorStand.equipment
                                if (Tag.ITEMS_FOOT_ARMOR.isTagged(cursor.type)) equipment.setBoots(cursor)
                                ctx.guiView.open()
                                MoveResult.ALLOW
                            },
                        )
                // Arms toggling slot0
                component[43] =
                    ItemBuilder
                        .from(if (armorStand.hasArms()) Material.GREEN_WOOL else Material.RED_WOOL)
                        .name(config.i18n.toggleArmsItemName.mm())
                        .asGuiItem { _, ctx ->
                            armorStand.setArms(!armorStand.hasArms())
                            ctx.guiView.open()
                        }
            }
        }

    data class Config(
        override var enabled: Boolean = true,
        var guiSpamPreventionDuration: Int = 0,
        var guiFillerMaterial: Material = Material.BLACK_STAINED_GLASS_PANE,
        var i18n: I18n = I18n(),
    ) : ModuleInterface.Config {
        data class I18n(
            var guiFillerItemName: String = "",
            var toggleArmsItemName: String = "Toggle Arms".mangoFmt(),
        )
    }
}
