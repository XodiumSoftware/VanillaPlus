/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.data.Directional
import org.bukkit.block.data.type.Slab
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.BlockUtils.iterate
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.skylineFmt
import java.util.*

enum class ChiselMode { ROTATE, FLIP }

/** Represents a module handling chisel mechanics within the system. */
class ChiselModule : ModuleInterface {
    override fun enabled(): Boolean = Config.ChiselModule.ENABLED

    private val chiselKey = NamespacedKey(instance, "chisel")
    private val playerModes = mutableMapOf<UUID, ChiselMode>()

    init {
        if (enabled()) instance.server.addRecipe(recipe())
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) {
        if (!enabled()) return

        val player = event.player
        val item = player.inventory.itemInMainHand
        val block = event.clickedBlock ?: return

        if (!isChisel(item)) return

        if (event.action.isRightClick && player.isSneaking) {
            val newMode = when (playerModes[player.uniqueId]) {
                ChiselMode.ROTATE -> ChiselMode.FLIP
                else -> ChiselMode.ROTATE
            }
            playerModes[player.uniqueId] = newMode
            player.sendActionBar("Chisel mode set to $newMode".skylineFmt().mm())
            event.isCancelled = true
            return
        }

        val iterateClockwise = when {
            event.action.isLeftClick -> true
            event.action.isRightClick -> false
            else -> return
        }

        when (playerModes[player.uniqueId]) {
            ChiselMode.ROTATE -> {
                val data = block.blockData
                if (data is Directional) {
                    data.facing = data.facing.iterate(iterateClockwise)
                    block.blockData = data
                    event.isCancelled = true
                }
            }

            ChiselMode.FLIP -> {
                val data = block.blockData
                if (data is Slab && data.type != Slab.Type.DOUBLE) {
                    data.type = data.type.iterate(iterateClockwise)
                    block.blockData = data
                    event.isCancelled = true
                }
            }

            else -> return
        }
    }

    /**
     * Creates a recipe for the chisel item.
     * @return The chisel recipe.
     */
    private fun recipe(): ShapedRecipe {
        return ShapedRecipe(chiselKey, chisel()).apply {
            shape("   ", " A ", " B ")
            setIngredient('A', Material.IRON_INGOT)
            setIngredient('B', Material.STICK)
        }
    }

    /**
     * Creates a chisel item stack.
     * @return The chisel item stack.
     */
    private fun chisel(): ItemStack {
        return ItemStack(Material.BRUSH).apply {
            itemMeta = itemMeta.apply {
                displayName("Chisel".mm())
                lore(
                    listOf(
                        "Usage:".fireFmt(),
                        "${"[Sneak + Right-click]".skylineFmt()} <white>Switch Mode",
                        "${"[Right-click]".skylineFmt()} <white>Iterate Block Faces Clockwise",
                        "${"[Left-click]".skylineFmt()} <white>Iterate Block Faces Anti-Clockwise",
                    ).mm()
                )
                persistentDataContainer.set(chiselKey, PersistentDataType.BYTE, 1)
            }
        }
    }

    /**
     * Checks if the given item stack is a chisel.
     * @param item The item stack to check.
     * @return True if the item stack is a chisel, false otherwise.
     */
    private fun isChisel(item: ItemStack): Boolean {
        return item.itemMeta?.persistentDataContainer?.has(chiselKey, PersistentDataType.BYTE) == true
    }
}