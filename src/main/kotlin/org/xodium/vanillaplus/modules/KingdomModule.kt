package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.container.type.PaperContainerType.hopper
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import io.papermc.paper.datacomponent.item.ItemLore
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
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import java.util.*
import kotlin.time.Duration.Companion.seconds

/** Represents a module handling kingdom mechanics within the system. */
internal class KingdomModule : ModuleInterface<KingdomModule.Config> {
    override val config: Config = Config()

    private val sceptreKey = NamespacedKey(instance, "kingdom_sceptre")
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
        if (item.persistentDataContainer.has(sceptreKey, PersistentDataType.BYTE)) {
            event.isCancelled = true

            // TODO: we should assign UUID on sceptre itemstack, so it assigns it on using the recipe. so we can remove this method completely and just fetch it directly.
            val sceptreUUID = getSceptreUUID(item)
            val kingdom = KingdomData.get(sceptreUUID)
            if (kingdom == null) {
                val kingdom =
                    KingdomData(
                        name = "${player.name}'s Kingdom",
                        sceptre = sceptreUUID,
                        ruler = player.uniqueId,
                    )

                player.sendMessage("New kingdom '${kingdom.name}' has been created!".mm())
                player.sendMessage("You are now the ruler. Right-click the sceptre to manage your kingdom.".mm())

                gui(kingdom).open(player)
            } else {
                if (kingdom.ruler == player.uniqueId) {
                    gui(kingdom).open(player)
                } else {
                    // FIX: val cannot be reassigned.
                    kingdom.ruler = player.uniqueId
                    KingdomData.save() // TODO: use set() instead since that saves to cache and file. save() is private.
                    gui(kingdom).open(player)
                }
            }
        }
    }

    // TODO: maybe move to its own SceptreData?
    private fun getSceptreUUID(item: ItemStack): UUID {
        val pdc = item.persistentDataContainer
        val sceptreUUIDString = pdc.get(sceptreIdKey, PersistentDataType.STRING)

        return if (sceptreUUIDString != null) {
            UUID.fromString(sceptreUUIDString)
        } else {
            val newUUID = UUID.randomUUID()
            // FIX: set() doesn't exist.
            pdc.set(sceptreIdKey, PersistentDataType.STRING, newUUID.toString())
            newUUID
        }
    }

    private fun gui(kingdom: KingdomData): Gui =
        buildGui {
            containerType = hopper()
            spamPreventionDuration = config.guiSpamPreventionDuration.seconds
            title(kingdom.name.mm())
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
        var guiSpamPreventionDuration: Int = 1,
        var sceptreMaterial: Material = Material.BLAZE_ROD,
        var sceptreItemName: String = "Royal Sceptre".mangoFmt(),
        var sceptreLore: MutableList<String> = mutableListOf("<gray>Right-click to manage your Kingdom</gray>"),
        var sceptreGlint: Boolean = true,
        var sceptreCustomModelData: String = "sceptre",
    ) : ModuleInterface.Config
}
