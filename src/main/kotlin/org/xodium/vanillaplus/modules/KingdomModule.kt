package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.container.type.PaperContainerType.hopper
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Represents a module handling kingdom mechanics within the system. */
internal class KingdomModule : ModuleInterface<KingdomModule.Config> {
    override val config: Config = Config()

    private val sceptreKey = NamespacedKey(instance, "kingdom_sceptre")

    @EventHandler
    fun on(event: PlayerInteractEvent) {
        if (!enabled() && event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return

        val item = event.item ?: return
        if (item.type != config.sceptreMaterial) return
    }

    private fun gui(): Gui =
        buildGui {
            containerType = hopper()
            spamPreventionDuration = config.guiSpamPreventionDuration
            title("Kingdom".mm()) // TODO: change to player kingdom name.
        }

    private fun sceptre(): ItemStack =
        @Suppress("unstableApiUsage")
        ItemStack.of(config.sceptreMaterial).apply {
            setData(DataComponentTypes.ITEM_NAME, config.sceptreItemName.mm())
            setData(DataComponentTypes.LORE, ItemLore.lore(config.sceptreLore.mm()))
            setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, config.sceptreGlint)
            editPersistentDataContainer { it.set(sceptreKey, PersistentDataType.BYTE, 1) }
        }

    data class Config(
        override var enabled: Boolean = true,
        var guiSpamPreventionDuration: Duration = 1.seconds,
        var sceptreMaterial: Material = Material.BLAZE_ROD,
        var sceptreItemName: String = "Royal Sceptre".mangoFmt(),
        var sceptreLore: MutableList<String> = mutableListOf("<gray>Right-click to manage your Kingdom</gray>"),
        var sceptreGlint: Boolean = true,
    ) : ModuleInterface.Config
}
