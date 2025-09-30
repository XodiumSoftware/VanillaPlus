@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import io.papermc.paper.connection.PlayerGameConnection
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import io.papermc.paper.datacomponent.item.ItemLore
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.dialog.DialogResponseView
import io.papermc.paper.event.player.PlayerCustomClickEvent
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
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

        // TODO: check if we can create a KingdomData.create() method in case kingdom == null
        val kingdom = KingdomData.get(sceptreUUID)
        if (kingdom == null) {
            val kingdom =
                KingdomData(
                    name = "${player.name}'s Kingdom",
                    ruler = player.uniqueId,
                )

            KingdomData.set(sceptreUUID, kingdom)
            player.showDialog(kingdomDialog(kingdom))
            instance.server.broadcast(
                config.l18n.kingdomCreatedMsg
                    .mm(Placeholder.component("kingdom_name", kingdom.name.mm())),
            )
        } else {
            player.showDialog(kingdomDialog(kingdom))
        }
    }

    @Suppress("unstableApiUsage")
    @EventHandler
    fun on(event: PlayerCustomClickEvent) {
        if (!enabled()) return
        when (event.identifier) {
            Key.key(instance, "kingdom/save") -> handleKingdomSave(event)
        }
    }

    /**
     * Handles the save action for the kingdom management dialog.
     * @param event the [PlayerCustomClickEvent] triggered when the player clicks the "Save" button.
     */
    @Suppress("unstableApiUsage")
    private fun handleKingdomSave(event: PlayerCustomClickEvent) {
        val view = event.dialogResponseView ?: return
        val player = (event.commonConnection as? PlayerGameConnection)?.player ?: return

        handleKingdomRuler(view, player)
        handleKingdomName(view, player)
        handleKingdomType(view, player)
    }

    @Suppress("unstableApiUsage")
    private fun handleKingdomRuler(
        view: DialogResponseView,
        player: Player,
    ) {
        val newRuler = UUID.fromString(view.getText("ruler") ?: return)
        val (sceptreUUID, kingdom) = getKingdomDataFromPlayer(player) ?: return
        if (kingdom.ruler == newRuler) return

        KingdomData.set(sceptreUUID, kingdom.copy(ruler = newRuler))
        instance.server.broadcast(
            config.l18n.kingdomRulerChangeMsg
                .mm(
                    Placeholder.component("kingdom_name", kingdom.name.mm()),
                    Placeholder.component(
                        "player",
                        instance.server.getPlayer(newRuler)?.displayName() ?: "NULL".mm(),
                    ),
                ),
        )
    }

    /**
     * Handles renaming a kingdom based on the player's dialog response.
     * @param view the [DialogResponseView] containing the player's submitted dialog inputs.
     * @param player the [Player] whose sceptre determines which kingdom to rename.
     */
    @Suppress("unstableApiUsage")
    private fun handleKingdomName(
        view: DialogResponseView,
        player: Player,
    ) {
        val newName = view.getText("name") ?: return
        val (sceptreUUID, kingdom) = getKingdomDataFromPlayer(player) ?: return
        if (kingdom.name == newName) return

        KingdomData.set(sceptreUUID, kingdom.copy(name = newName))
        instance.server.broadcast(
            config.l18n.kingdomRenameMsg
                .mm(
                    Placeholder.component("kingdom_name", newName.mm()),
                    Placeholder.component("kingdom_old_name", kingdom.name.mm()),
                ),
        )
    }

    /**
     * Handles updating the government type of kingdom based on the player's dialog response.
     * @param view the [DialogResponseView] containing the player's submitted dialog inputs.
     * @param player the [Player] whose sceptre determines which kingdom to update.
     */
    @Suppress("unstableApiUsage")
    private fun handleKingdomType(
        view: DialogResponseView,
        player: Player,
    ) {
        val newType = KingdomTypeEnum.valueOf(view.getText("type") ?: return)
        val (sceptreUUID, kingdom) = getKingdomDataFromPlayer(player) ?: return
        if (kingdom.type == newType) return

        KingdomData.set(sceptreUUID, kingdom.copy(type = newType))
    }

    /**
     * Retrieves the [KingdomData] associated with the sceptre held by the given [player].
     * @param player the [Player] whose main-hand sceptre should be checked.
     * @return a [Pair] containing the sceptre's [UUID] and its corresponding [KingdomData],
     *         or `null` if no valid data is found.
     */
    private fun getKingdomDataFromPlayer(player: Player): Pair<UUID, KingdomData>? {
        val item = player.inventory.itemInMainHand
        val sceptreUUIDString = item.persistentDataContainer.get(sceptreIdKey, PersistentDataType.STRING)
        val sceptreUUID = sceptreUUIDString?.let { UUID.fromString(it) } ?: return null
        val oldKingdom = KingdomData.get(sceptreUUID) ?: return null
        return sceptreUUID to oldKingdom
    }

    /**
     * Builds a kingdom management dialog for the specified [KingdomData].
     * @param data the [KingdomData] representing the current state of the kingdom (name, ruler, and type).
     * @return a fully built [Dialog] that allows viewing and modifying kingdom properties.
     */
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
                                    .singleOption(
                                        "ruler",
                                        "Ruler".fireFmt().mm(),
                                        instance.server.onlinePlayers.map { player ->
                                            SingleOptionDialogInput.OptionEntry.create(
                                                player.name,
                                                player.displayName(),
                                                (player.uniqueId == data.ruler),
                                            )
                                        },
                                    ).build(),
                                DialogInput
                                    .text("name", "Rename Kingdom".fireFmt().mm())
                                    .initial(data.name)
                                    .maxLength(100)
                                    .build(),
                                DialogInput
                                    .singleOption(
                                        "type",
                                        "Government Type".fireFmt().mm(),
                                        KingdomTypeEnum.entries.map { types ->
                                            SingleOptionDialogInput.OptionEntry.create(
                                                types.name,
                                                types.displayName(),
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

    /**
     * Creates a new instance of the custom sceptre item.
     * @return an [ItemStack] representing the fully configured sceptre.
     */
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

    /**
     * Creates the shaped crafting recipe for the custom sceptre item.
     * @return a [Recipe] representing the sceptre crafting recipe.
     */
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
        var l18n: L18n = L18n(),
    ) : ModuleInterface.Config {
        data class L18n(
            var kingdomCreatedMsg: String =
                "❗ ".fireFmt() +
                    "<i>The kingdom of <kingdom_name> has been created</i>".mangoFmt(true) +
                    " ❗".fireFmt(true),
            var kingdomRulerChangeMsg: String =
                "❗ ".fireFmt() +
                    "<i>The kingdom of <kingdom_name> has a new ruler named <player></i>".mangoFmt(true) +
                    " ❗".fireFmt(true),
            var kingdomRenameMsg: String = (
                "❗ ".fireFmt() +
                    "<i>The kingdom of <kingdom_old_name> is now known as <kingdom_name></i>".mangoFmt(true) +
                    " ❗".fireFmt(true)
            ),
        )
    }
}
