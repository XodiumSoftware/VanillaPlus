package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.ItemStack
import org.incendo.interfaces.core.click.ClickHandler
import org.incendo.interfaces.kotlin.paper.asElement
import org.incendo.interfaces.kotlin.paper.buildChestInterface
import org.incendo.interfaces.paper.PaperInterfaceListeners
import org.incendo.interfaces.paper.PlayerViewer
import org.incendo.interfaces.paper.type.ChestInterface
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.lore
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.name
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt

/** Represents a module handling armor stand mechanics within the system. */
internal class ArmorStandModule : ModuleInterface<ArmorStandModule.Config> {
    override val config: Config = Config()

    init {
        PaperInterfaceListeners.install(instance)
    }

    @EventHandler
    fun on(event: PlayerInteractAtEntityEvent) {
        if (!enabled() ||
            event.rightClicked !is ArmorStand ||
            event.player.isSneaking ||
            event.player.inventory.itemInMainHand.type == Material.NAME_TAG
        ) {
            return
        }
        val armorStand = event.rightClicked as ArmorStand
        armorStand.gui().open(PlayerViewer.of(event.player))
        event.isCancelled = true
    }

    /**
     * Builds a GUI for interacting with an armor stand entity.
     * @return A configured Gui instance ready to be displayed to players.
     */
    private fun ArmorStand.gui(): ChestInterface =
        buildChestInterface {
            rows = 6
            title = customName() ?: name.mm()
            // Filler slots
            withTransform { view ->
                (0 until 9).forEach { x ->
                    (0 until 6).forEach { y ->
                        view[x, y] =
                            ItemStack
                                .of(config.guiFillerMaterial)
                                .name(config.i18n.guiFillerItemName.mm())
                                .asElement(ClickHandler.cancel())
                    }
                }
            }
            // Helmet slot
            withTransform { view ->
                view[4, 1] =
                    equipment.helmet.asElement(
                        ClickHandler.canceling { ctx ->
                            ctx.cancel(false)
                            val cursor = ctx.cause().cursor
                            if (cursor.type != Material.AIR) {
                                ctx.viewer().player().setItemOnCursor(null)
                                equipment.setHelmet(cursor)
                                ctx.view().update()
                            }
                        },
                    )
            }
            // Main Hand slot
            withTransform { view ->
                view[3, 2] =
                    equipment.itemInMainHand.asElement(
                        ClickHandler.canceling { ctx ->
                            ctx.cancel(false)
                            val cursor = ctx.cause().cursor
                            if (cursor.type != Material.AIR) {
                                ctx.viewer().player().setItemOnCursor(null)
                                equipment.setItemInMainHand(cursor)
                                ctx.view().update()
                            }
                        },
                    )
            }
            // Chestplate slot
            withTransform { view ->
                view[4, 2] =
                    equipment.chestplate.asElement(
                        ClickHandler.canceling { ctx ->
                            ctx.cancel(false)
                            val cursor = ctx.cause().cursor
                            if (cursor.type != Material.AIR) {
                                ctx.viewer().player().setItemOnCursor(null)
                                equipment.setChestplate(cursor)
                                ctx.view().update()
                            }
                        },
                    )
            }
            // Offhand slot
            withTransform { view ->
                view[5, 2] =
                    equipment.itemInOffHand.asElement(
                        ClickHandler.canceling { ctx ->
                            ctx.cancel(false)
                            val cursor = ctx.cause().cursor
                            if (cursor.type != Material.AIR) {
                                ctx.viewer().player().setItemOnCursor(null)
                                equipment.setItemInOffHand(cursor)
                                ctx.view().update()
                            }
                        },
                    )
            }
            // Leggings slot
            withTransform { view ->
                view[4, 3] =
                    equipment.leggings.asElement(
                        ClickHandler.canceling { ctx ->
                            if (ctx.cause().isLeftClick) {
                                ctx.cancel(false)
                                val cursor = ctx.cause().cursor
                                if (cursor.type != Material.AIR) {
                                    ctx.viewer().player().setItemOnCursor(null)
                                    equipment.setLeggings(cursor)
                                    ctx.view().update()
                                }
                            }
                        },
                    )
            }
            // Boots slot
            withTransform { view ->
                view[4, 4] =
                    equipment.boots.asElement(
                        ClickHandler.canceling { ctx ->
                            ctx.cancel(false)
                            val cursor = ctx.cause().cursor
                            if (cursor.type != Material.AIR) {
                                ctx.viewer().player().setItemOnCursor(null)
                                equipment.setBoots(cursor)
                                ctx.view().update()
                            }
                        },
                    )
            }
            // Nametag toggling slot
            withTransform { view ->
                view[7, 1] =
                    ItemStack
                        .of(if (isCustomNameVisible) Material.GREEN_WOOL else Material.RED_WOOL)
                        .name(config.i18n.toggleNameTagItemName.mm())
                        .asElement(
                            ClickHandler.canceling {
                                isCustomNameVisible = !isCustomNameVisible
                                it.view().update()
                            },
                        )
            }
            // Arms toggling slot
            withTransform { view ->
                view[7, 2] =
                    ItemStack
                        .of(if (hasArms()) Material.GREEN_WOOL else Material.RED_WOOL)
                        .name(config.i18n.toggleArmsItemName.mm())
                        .asElement(
                            ClickHandler.canceling {
                                setArms(!hasArms())
                                it.view().update()
                            },
                        )
            }
            // BasePlate toggling slot
            withTransform { view ->
                view[7, 4] =
                    ItemStack
                        .of(if (hasBasePlate()) Material.GREEN_WOOL else Material.RED_WOOL)
                        .name(config.i18n.toggleBasePlateItemName.mm())
                        .asElement(
                            ClickHandler.canceling {
                                setBasePlate(!hasBasePlate())
                                it.view().update()
                            },
                        )
            }
            // Size toggling slot
            withTransform { view ->
                view[7, 3] =
                    ItemStack
                        .of(if (!isSmall) Material.GREEN_WOOL else Material.RED_WOOL)
                        .name(config.i18n.toggleSizeItemName.mm())
                        .asElement(
                            ClickHandler.canceling {
                                isSmall = !isSmall
                                it.view().update()
                            },
                        )
            }
            // More options slot
            withTransform { view ->
                view[1, 4] =
                    ItemStack
                        .of(Material.ARMOR_STAND)
                        .name(config.i18n.moreOptionsItemName.mm())
                        .lore("COMING SOON".fireFmt().mm())
                        .asElement(ClickHandler.cancel())
            }
        }

    data class Config(
        override var enabled: Boolean = true,
        var guiFillerMaterial: Material = Material.BLACK_STAINED_GLASS_PANE,
        var i18n: I18n = I18n(),
    ) : ModuleInterface.Config {
        data class I18n(
            var guiFillerItemName: String = "",
            var toggleArmsItemName: String = "Toggle Arms".mangoFmt(),
            var toggleNameTagItemName: String = "Toggle Name Tag".mangoFmt(),
            var toggleBasePlateItemName: String = "Toggle Base Plate".mangoFmt(),
            var toggleSizeItemName: String = "Toggle Size".mangoFmt(),
            var moreOptionsItemName: String = "More Options".mangoFmt(),
        )
    }
}
