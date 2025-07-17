package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import dev.triumphteam.gui.paper.kotlin.builder.chestContainer
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Represents a module handling quest mechanics within the system. */
class QuestModule : ModuleInterface<QuestModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    override fun cmds(): CommandData {
        return CommandData(
            listOf(
                Commands.literal("quests")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx -> ctx.tryCatch { gui().open(it.sender as Player) } }
            ),
            "Allows players to open the quests gui.",
            listOf("q")
        )
    }

    override fun perms(): List<Permission> {
        return listOf(
            Permission(
                "${instance::class.simpleName}.quests.gui.use".lowercase(),
                "Allows use of the quests command",
                PermissionDefault.TRUE
            )
        )
    }

    /**
     * Constructs and configures a GUI for the associated module.
     * @return A fully configured `Gui` instance with the specified properties and components.
     */
    private fun gui(): Gui {
        return buildGui {
            spamPreventionDuration = config.spamPreventionDuration
            containerType = chestContainer { rows = 1 }
            title(config.guiTitle.mm())
            val completed = true
            val material = if (completed) Material.WRITABLE_BOOK else Material.ENCHANTED_BOOK
            val name = if (completed) "Completed Quest".mangoFmt() else "Quest".fireFmt()
            val lore = if (completed) listOf("Completed".mangoFmt()) else listOf("Click to open".fireFmt())
            statelessComponent {
                it[1, 5] = ItemBuilder.from(guiItemStack(material, name, lore)).asGuiItem()
            }
        }
    }

    /**
     * Creates an instance of an ItemStack with the specified material, name, and lore,
     * and applies the desired display properties to it.
     * @param material The material of the ItemStack.
     * @param itemName The display name of the ItemStack, parsed as a MiniMessage component.
     * @param itemLore The lore of the ItemStack, parsed as a list of MiniMessage components.
     * @return An ItemStack configured with the provided material, name, and lore.
     */
    private fun guiItemStack(material: Material, itemName: String, itemLore: List<String>): ItemStack {
        @Suppress("UnstableApiUsage")
        return ItemStack.of(material).apply {
            setData(DataComponentTypes.ITEM_NAME, itemName.mm())
            setData(DataComponentTypes.LORE, ItemLore.lore(itemLore.mm()))
        }
    }

    data class Config(
        override var enabled: Boolean = true,
        var spamPreventionDuration: Duration = 1.seconds,
        var guiTitle: String = "<b>Quests</b>".fireFmt(),
    ) : ModuleInterface.Config
}