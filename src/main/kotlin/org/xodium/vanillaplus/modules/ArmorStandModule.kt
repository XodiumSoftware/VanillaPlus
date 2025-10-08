package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.click.ClickContext
import dev.triumphteam.gui.click.MoveResult
import dev.triumphteam.gui.click.action.GuiClickAction
import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import dev.triumphteam.gui.paper.kotlin.builder.chestContainer
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.EquipmentSlot
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
            statelessComponent {
                // Filler slots
                for (slot in 0 until 54) {
                    it[slot] =
                        ItemBuilder
                            .from(config.guiFillerMaterial)
                            .name(config.i18n.guiFillerItemName.mm())
                            .asGuiItem()
                }
                // Helmet slot
                it[13] =
                    ItemBuilder
                        .from(armorStand.equipment.helmet)
                        .asGuiItem(
                            GuiClickAction.movable { player, ctx ->
                                // handleEquipmentSwap(ctx, player, armorStand, EquipmentSlot.HEAD)
                                MoveResult.ALLOW
                            },
                        )
                // Main Hand slot
                it[21] =
                    ItemBuilder
                        .from(armorStand.equipment.itemInMainHand)
                        .asGuiItem(
                            GuiClickAction.movable { player, ctx ->
                                // handleEquipmentSwap(ctx, player, armorStand, EquipmentSlot.HAND)
                                MoveResult.ALLOW
                            },
                        )
                // Chestplate slot
                it[22] =
                    ItemBuilder
                        .from(armorStand.equipment.chestplate)
                        .asGuiItem(
                            GuiClickAction.movable { player, ctx ->
                                // handleEquipmentSwap(ctx, player, armorStand, EquipmentSlot.CHEST)
                                MoveResult.ALLOW
                            },
                        )
                // Offhand slot
                it[23] =
                    ItemBuilder
                        .from(armorStand.equipment.itemInOffHand)
                        .asGuiItem(
                            GuiClickAction.movable { player, ctx ->
                                // handleEquipmentSwap(ctx, player, armorStand, EquipmentSlot.OFF_HAND)
                                MoveResult.ALLOW
                            },
                        )
                // Leggings slot
                it[31] =
                    ItemBuilder
                        .from(armorStand.equipment.leggings)
                        .asGuiItem(
                            GuiClickAction.movable { player, ctx ->
                                // handleEquipmentSwap(ctx, player, armorStand, EquipmentSlot.LEGS)
                                MoveResult.ALLOW
                            },
                        )
                // Boots slot
                it[40] =
                    ItemBuilder
                        .from(armorStand.equipment.boots)
                        .asGuiItem(
                            GuiClickAction.movable { player, ctx ->
                                // handleEquipmentSwap(ctx, player, armorStand, EquipmentSlot.FEET)
                                MoveResult.ALLOW
                            },
                        )
                // Arms toggling slot0
                it[43] =
                    ItemBuilder
                        .from(if (armorStand.hasArms()) Material.GREEN_WOOL else Material.RED_WOOL)
                        .name(config.i18n.toggleArmsItemName.mm())
                        .asGuiItem { _, ctx ->
                            armorStand.setArms(!armorStand.hasArms())
                            ctx.guiView.open()
                        }
            }
        }

    /**
     * TODO
     */
    private fun handleEquipmentSwap(
        ctx: ClickContext,
        player: Player,
        armorStand: ArmorStand,
        slot: EquipmentSlot,
    ) {
        val equipment = armorStand.equipment
        val playerInventory = player.inventory
        val cursorItem = playerInventory.itemInMainHand

        val standItem =
            when (slot) {
                EquipmentSlot.HEAD -> equipment.helmet
                EquipmentSlot.CHEST -> equipment.chestplate
                EquipmentSlot.LEGS -> equipment.leggings
                EquipmentSlot.FEET -> equipment.boots
                EquipmentSlot.HAND -> equipment.itemInMainHand
                EquipmentSlot.OFF_HAND -> equipment.itemInOffHand
                else -> return
            }

        when (slot) {
            EquipmentSlot.HEAD -> equipment.setHelmet(cursorItem)
            EquipmentSlot.CHEST -> equipment.setChestplate(cursorItem)
            EquipmentSlot.LEGS -> equipment.setLeggings(cursorItem)
            EquipmentSlot.FEET -> equipment.setBoots(cursorItem)
            EquipmentSlot.HAND -> equipment.setItemInMainHand(cursorItem)
            EquipmentSlot.OFF_HAND -> equipment.setItemInOffHand(cursorItem)
            else -> return
        }

        playerInventory.setItemInMainHand(standItem)
        ctx.guiView.open()
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
