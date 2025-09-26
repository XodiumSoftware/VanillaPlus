package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.container.type.PaperContainerType.hopper
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.time.toKotlinDuration

/** Represents a module handling kingdom mechanics within the system. */
internal class KingdomModule : ModuleInterface<KingdomModule.Config> {
    override val config: Config = Config()

    private val sceptreKey = NamespacedKey(instance, "kingdom_sceptre")
    private val sceptreRecipeKey = NamespacedKey(instance, "kingdom_sceptre_recipe")

    init {
        if (enabled()) instance.server.addRecipe(sceptreRecipe())
    }

    override fun cmds(): List<CommandData> =
        listOf(
            CommandData(
                Commands
                    .literal("sceptre")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx ->
                        ctx.tryCatch {
                            if (it.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                            (it.sender as Player).give(sceptre())
                        }
                    },
                "This command gives you a sceptre",
                emptyList(),
            ),
        )

    override fun perms(): List<Permission> =
        listOf(
            Permission(
                "${instance::class.simpleName}.sceptre".lowercase(),
                "Allows use of the sceptre command",
                PermissionDefault.OP,
            ),
        )

    @EventHandler
    fun on(event: PlayerInteractEvent) {
        if (!enabled() && event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return

        val item = event.item ?: return
        if (item.persistentDataContainer.has(sceptreKey, PersistentDataType.BYTE)) {
            event.isCancelled = true
            gui().open(event.player)
        }
    }

    private fun gui(): Gui =
        buildGui {
            containerType = hopper()
            spamPreventionDuration = config.guiSpamPreventionDuration.toKotlinDuration()
            title("".mm()) // TODO: change to player kingdom name.
            statelessComponent { }
        }

    private fun sceptre(): ItemStack =
        @Suppress("unstableApiUsage")
        ItemStack.of(config.sceptreMaterial).apply {
            setData(DataComponentTypes.ITEM_NAME, config.sceptreItemName.mm())
            setData(DataComponentTypes.LORE, ItemLore.lore(config.sceptreLore.mm()))
            setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, config.sceptreGlint)
            setData(
                DataComponentTypes.CUSTOM_MODEL_DATA,
                CustomModelData.customModelData().addString(config.sceptreCustomModelData),
            )
            editPersistentDataContainer { it.set(sceptreKey, PersistentDataType.BYTE, 1) }
        }

    private fun sceptreRecipe(): Recipe =
        ShapedRecipe(sceptreRecipeKey, sceptre()).apply {
            shape(" A ", " B ", " C ")
            setIngredient('A', Material.BLAZE_ROD)
            setIngredient('B', Material.NETHER_STAR)
            setIngredient('C', Material.EMERALD)
        }

    data class Config(
        override var enabled: Boolean = true,
        var guiSpamPreventionDuration: Duration = Duration.of(1, ChronoUnit.SECONDS),
        var sceptreMaterial: Material = Material.BLAZE_ROD,
        var sceptreItemName: String = "Royal Sceptre".mangoFmt(),
        var sceptreLore: MutableList<String> = mutableListOf("<gray>Right-click to manage your Kingdom</gray>"),
        var sceptreGlint: Boolean = true,
        var sceptreCustomModelData: String = "sceptre",
    ) : ModuleInterface.Config
}
