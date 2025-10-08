package org.xodium.vanillaplus.modules

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
import kotlin.time.Duration.Companion.seconds

/** Represents a module handling armor stand mechanics within the system. */
internal class ArmorStandModule : ModuleInterface<ArmorStandModule.Config> {
    override val config: Config = Config()

    @EventHandler
    fun on(event: PlayerInteractAtEntityEvent) {
        if (!enabled() || event.rightClicked !is ArmorStand) return
        gui(event.rightClicked as ArmorStand)
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
                it[1] = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).asGuiItem()
            }
        }

    /**
     * Checks if the given material represents a tool item.
     * @param material the [Material] to check.
     * @return `true` if the material is a tool, `false` otherwise.
     */
    private fun isTool(material: Material): Boolean =
        Tag.ITEMS_AXES.isTagged(material) ||
            Tag.ITEMS_PICKAXES.isTagged(material) ||
            Tag.ITEMS_SHOVELS.isTagged(material) ||
            Tag.ITEMS_HOES.isTagged(material) ||
            Tag.ITEMS_SWORDS.isTagged(material) ||
            material == Material.FISHING_ROD ||
            material == Material.FLINT_AND_STEEL ||
            material == Material.SHEARS ||
            material == Material.BOW ||
            material == Material.CROSSBOW ||
            material == Material.TRIDENT ||
            material == Material.SHIELD ||
            material == Material.CARROT_ON_A_STICK ||
            material == Material.WARPED_FUNGUS_ON_A_STICK

    data class Config(
        override var enabled: Boolean = true,
        var guiSpamPreventionDuration: Int = 1,
    ) : ModuleInterface.Config
}
