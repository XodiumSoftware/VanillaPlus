/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Fence
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs
import org.bukkit.entity.Player
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

/** Represents a module handling chisel mechanics within the system. */
class ChiselModule : ModuleInterface {
    override fun enabled(): Boolean = Config.ChiselModule.ENABLED

    private val chiselKey = NamespacedKey(instance, "chisel")

    init {
        if (enabled()) instance.server.addRecipe(recipe())
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) {
        if (!enabled()) return

        val player = event.player
        val item = player.inventory.itemInMainHand

        if (!isChisel(item)) return

        val block = event.clickedBlock ?: return

        val iterateClockwise = when {
            event.action.isLeftClick -> true
            event.action.isRightClick -> false
            else -> return
        }

        handleChiselAction(block, iterateClockwise, event)
    }

    /**
     * Handles the chisel action based on the block type and the player's interaction.
     * @param block The [Block] being interacted with.
     * @param iterateClockwise True if iterating clockwise, false otherwise.
     * @param event The [PlayerInteractEvent] triggered by the [Player].
     */
    private fun handleChiselAction(block: Block, iterateClockwise: Boolean, event: PlayerInteractEvent) {
        val data = block.blockData
        val isSneaking = event.player.isSneaking
        val used = when {
            data is Fence -> {
                block.blockData = data.apply { iterate(allowedFaces.toList(), iterateClockwise) }
                event.isCancelled = true
                true
            }

            data is Stairs -> {
                block.blockData = data.apply {
                    if (isSneaking) facing = facing.iterate(faces.toList(), iterateClockwise)
                    shape = shape.iterate(Stairs.Shape.entries, iterateClockwise)
                    half = half.iterate(Bisected.Half.entries, iterateClockwise)
                }
                event.isCancelled = true
                true
            }

            data is Slab && data.type != Slab.Type.DOUBLE -> {
                block.blockData = data.apply {
                    type = type.iterate(Slab.Type.entries.filter { it != Slab.Type.DOUBLE }, iterateClockwise)
                }
                event.isCancelled = true
                true
            }

            else -> false
        }

        @Suppress("UnstableApiUsage")
        if (used) {
            val player = event.player
            val item = player.inventory.itemInMainHand
            val currentDamage = item.getData(DataComponentTypes.DAMAGE) ?: 0
            val newDamage = currentDamage + 1
            if (newDamage >= item.type.maxDurability) {
                player.inventory.setItemInMainHand(null)
                player.playSound(Config.ChiselModule.CHISEL_DURABILITY_DECREASE_SOUND)
            } else {
                item.setData(DataComponentTypes.DAMAGE, newDamage)
            }
        }
    }

    /**
     * Creates a [ShapedRecipe] for the chisel item.
     * @return The chisel [ShapedRecipe].
     */
    private fun recipe(): ShapedRecipe {
        return ShapedRecipe(chiselKey, chisel()).apply {
            shape("   ", " A ", " B ")
            setIngredient('A', Material.IRON_INGOT)
            setIngredient('B', Material.STICK)
        }
    }

    /**
     * Creates a chisel [ItemStack].
     * @return The chisel [ItemStack].
     */
    private fun chisel(): ItemStack {
        @Suppress("UnstableApiUsage")
        return ItemStack.of(Material.BRUSH).apply {
            editPersistentDataContainer { it.set(chiselKey, PersistentDataType.BYTE, 1) }
            setData(DataComponentTypes.ITEM_MODEL, Key.key("chisel"))
            setData(DataComponentTypes.CUSTOM_NAME, "Chisel".mm())
            setData(
                DataComponentTypes.LORE, ItemLore.lore(
                    listOf(
                        "Usage:".fireFmt(),
                        "${"[shift]".skylineFmt()} <white>Switch Chisel Mode",
                        "${"[Right-click]".skylineFmt()} <white>Iterate Block State Clockwise",
                        "${"[Left-click]".skylineFmt()} <white>Iterate Block State Anti-Clockwise",
                    ).mm()
                )
            )
        }
    }

    /**
     * Checks if the given [ItemStack] is a chisel.
     * @param item The [ItemStack] to check.
     * @return True if the [ItemStack] is a chisel, false otherwise.
     */
    private fun isChisel(item: ItemStack): Boolean {
        return item.persistentDataContainer.has(chiselKey, PersistentDataType.BYTE)
    }
}