package org.xodium.illyriaplus.items

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.IllyriaKingdoms.Companion.instance
import org.xodium.illyriaplus.guis.MainGui
import org.xodium.illyriaplus.interfaces.ItemInterface
import org.xodium.illyriaplus.pdcs.ItemPDC.isSceptre
import org.xodium.illyriaplus.pdcs.ItemPDC.kingdomId
import org.xodium.illyriaplus.utils.Utils.MM
import kotlin.uuid.ExperimentalUuidApi

/**
 * Kingdom Sceptre item.
 * Whoever holds the sceptre is the kingdom owner.
 * Each sceptre is bound to a specific kingdom via PDC.
 */
@OptIn(ExperimentalUuidApi::class)
internal object SceptreItem : ItemInterface {
    /** The default kingdom tool item stack (not bound to any kingdom). */
    @Suppress("UnstableApiUsage")
    override val item =
        ItemStack.of(Material.STICK).apply {
            setData(
                DataComponentTypes.ITEM_NAME,
                MM.deserialize("<gradient:#FFA751:#FFE259>Kingdom Sceptre</gradient>"),
            )
            setData(
                DataComponentTypes.LORE,
                ItemLore.lore(
                    listOf(
                        Component.empty(),
                        MM.deserialize("<gray>The holder of this sceptre rules the kingdom.</gray>"),
                        Component.empty(),
                        MM.deserialize("<red>Not bound to any kingdom.</red>"),
                    ),
                ),
            )
            setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            setData(DataComponentTypes.ITEM_MODEL, Key.key(instance, "sceptre"))
            isSceptre = true
        }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) {
        val item = event.item ?: return

        if (!item.isSceptre) return

        event.isCancelled = true

        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return

        val kingdom = item.kingdomId

        if (kingdom != null) MainGui.window(kingdom).open()
    }
}
