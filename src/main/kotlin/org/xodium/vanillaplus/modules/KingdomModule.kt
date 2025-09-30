@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import dev.triumphteam.gui.paper.container.type.PaperContainerType.hopper
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import io.papermc.paper.datacomponent.item.ItemLore
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.KingdomData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import java.util.*
import kotlin.time.Duration.Companion.seconds

/** Represents a module handling kingdom mechanics within the system. */
internal class KingdomModule : ModuleInterface<KingdomModule.Config> {
    override val config: Config = Config()

    private val sceptreIdKey = NamespacedKey(instance, "kingdom_sceptre_id")
    private val sceptreRecipeKey = NamespacedKey(instance, "kingdom_sceptre_recipe")

    init {
        if (enabled()) instance.server.addRecipe(sceptreRecipe())
    }

    @EventHandler
    fun on(event: PlayerInteractEvent) {
        if (!enabled() && event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return

        val item = event.item ?: return
        val player = event.player
        val sceptreUUIDString = item.persistentDataContainer.get(sceptreIdKey, PersistentDataType.STRING)
        val sceptreUUID = sceptreUUIDString?.let { UUID.fromString(it) } ?: return

        event.isCancelled = true

        val kingdom = KingdomData.get(sceptreUUID)
        if (kingdom == null) {
            val kingdom =
                KingdomData(
                    name = "${player.name}'s Kingdom",
                    sceptre = sceptreUUID,
                    ruler = player.uniqueId,
                )

            KingdomData.set(sceptreUUID, kingdom)

            player.sendMessage("New kingdom '${kingdom.name}' has been created!".mm())
            player.sendMessage("You are now the ruler. Right-click the sceptre to manage your kingdom.".mm())

            gui(kingdom).open(player)
        } else {
            if (kingdom.ruler == player.uniqueId) {
                gui(kingdom).open(player)
            } else {
                KingdomData.set(sceptreUUID, kingdom.copy(ruler = kingdom.ruler))
                gui(kingdom).open(player)
            }
        }
    }

    private fun gui(kingdom: KingdomData): Gui =
        buildGui {
            containerType = hopper()
            spamPreventionDuration = config.guiSpamPreventionDuration.seconds
            title(kingdom.name.mm())
            statelessComponent {
                it[0] =
                    ItemBuilder
                        .from(Material.NAME_TAG)
                        .name("Rename your Kingdom".fireFmt().mm())
                        .lore("<gray>Left-click to rename your Kingdom</gray>".mm())
                        .asGuiItem { player, _ ->
                            @Suppress("unstableApiUsage")
                            player.showDialog(
                                Dialog.create { builder ->
                                    builder
                                        .empty()
                                        .base(DialogBase.builder("Rename your Kingdom".fireFmt().mm()).build())
                                        .type(
                                            DialogType.confirmation(
                                                ActionButton
                                                    .builder("Save".fireFmt().mm())
                                                    .action(
                                                        DialogAction.customClick(
                                                            Key.key(instance, "kingdom/rename/agree"),
                                                            null,
                                                        ),
                                                    ).build(),
                                                ActionButton
                                                    .builder("Discard".fireFmt().mm())
                                                    .action(
                                                        DialogAction.customClick(
                                                            Key.key(instance, "kingdom/rename/disagree"),
                                                            null,
                                                        ),
                                                    ).build(),
                                            ),
                                        )
                                },
                            )
                        }
            }
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
            editPersistentDataContainer {
                it.set(sceptreIdKey, PersistentDataType.STRING, UUID.randomUUID().toString())
            }
        }

    private fun sceptreRecipe(): Recipe =
        ShapedRecipe(sceptreRecipeKey, sceptre()).apply {
            shape(" A ", " B ", " C ")
            setIngredient('A', Material.EMERALD)
            setIngredient('B', Material.NETHER_STAR)
            setIngredient('C', Material.BLAZE_ROD)
        }

    data class Config(
        override var enabled: Boolean = true,
        var guiSpamPreventionDuration: Int = 1,
        var sceptreMaterial: Material = Material.BLAZE_ROD,
        var sceptreItemName: String = "Royal Sceptre".mangoFmt(),
        var sceptreLore: MutableList<String> = mutableListOf("<gray>Right-click to manage your Kingdom</gray>"),
        var sceptreGlint: Boolean = true,
        var sceptreCustomModelData: String = "sceptre",
    ) : ModuleInterface.Config
}
