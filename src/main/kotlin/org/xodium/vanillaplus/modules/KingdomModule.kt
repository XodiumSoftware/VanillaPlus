@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import io.papermc.paper.datacomponent.item.ItemLore
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.event.player.PlayerCustomClickEvent
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
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
import org.xodium.vanillaplus.enums.KingdomTypeEnum
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import java.util.*

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
            player.showDialog(kingdomDialog(kingdom))
            instance.server.broadcast(
                "❗ "
                    .fireFmt()
                    .mm()
                    .append(
                        "<i>The kingdom of ${kingdom.name} has been created</i>"
                            .mangoFmt(
                                true,
                            ).mm(),
                    ).append(" ❗".fireFmt(true).mm()),
            )
        } else {
            if (kingdom.ruler == player.uniqueId) {
                player.showDialog(kingdomDialog(kingdom))
            } else {
                KingdomData.set(sceptreUUID, kingdom.copy(ruler = kingdom.ruler))
                player.showDialog(kingdomDialog(kingdom))
                instance.server.broadcast(
                    "❗ "
                        .fireFmt()
                        .mm()
                        .append(
                            "<i>The kingdom of ${kingdom.name} has a new ruler named ${kingdom.ruler}</i>"
                                .mangoFmt(
                                    true,
                                ).mm(),
                        ).append(" ❗".fireFmt(true).mm()),
                )
            }
        }
    }

    @Suppress("unstableApiUsage")
    @EventHandler
    fun on(event: PlayerCustomClickEvent) {
        if (!enabled() || event.identifier != Key.key(instance, "kingdom/save")) return

        val view = event.dialogResponseView ?: return
        val player = (event.commonConnection as? PlayerGameConnection)?.player ?: return
        val name = view.getText("name") ?: return
        val item = player.inventory.itemInMainHand
        val sceptreUUIDString = item.persistentDataContainer.get(sceptreIdKey, PersistentDataType.STRING)
        val sceptreUUID = sceptreUUIDString?.let { UUID.fromString(it) } ?: return
        val oldKingdom = KingdomData.get(sceptreUUID) ?: return
        val oldName = oldKingdom.name
        if (oldName == name) return
        KingdomData.set(sceptreUUID, oldKingdom.copy(name = name))
        instance.server.broadcast(
            "❗ "
                .fireFmt()
                .mm()
                .append("<i>The kingdom of $oldName is now known as $name</i>".mangoFmt(true).mm())
                .append(" ❗".fireFmt(true).mm()),
        )
    }

    @Suppress("unstableApiUsage")
    private fun kingdomDialog(data: KingdomData): Dialog =
        Dialog.create {
            it
                .empty()
                .base(
                    DialogBase
                        .builder(data.name.mm())
                        .inputs(
                            listOf(
                                DialogInput
                                    .text("name", "Rename Kingdom".fireFmt().mm())
                                    .initial(data.name)
                                    .maxLength(100)
                                    .build(),
                                DialogInput
                                    .singleOption(
                                        "type",
                                        "Set Government Type".fireFmt().mm(),
                                        KingdomTypeEnum.entries.map { types ->
                                            SingleOptionDialogInput.OptionEntry.create(
                                                types.name,
                                                types.name.mm(),
                                                (types == data.type),
                                            )
                                        },
                                    ).build(),
                            ),
                        ).build(),
                ).type(
                    DialogType.confirmation(
                        ActionButton
                            .builder("Save".fireFmt().mm())
                            .action(
                                DialogAction.customClick(
                                    Key.key(instance, "kingdom/save"),
                                    null,
                                ),
                            ).build(),
                        ActionButton
                            .builder("Discard".fireFmt().mm())
                            .action(
                                DialogAction.customClick(
                                    Key.key(instance, "kingdom/discard"),
                                    null,
                                ),
                            ).build(),
                    ),
                )
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
