/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.data.Directional
import org.bukkit.block.data.type.Slab
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.meta.Damageable
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.PlayerData
import org.xodium.vanillaplus.enums.ChiselMode
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

        val playerId = player.uniqueId.toString()
        val playerData = PlayerData.getData().find { it.id == playerId } ?: PlayerData(playerId)

        //TODO: switching doesn't work.
        if (event.action == Action.RIGHT_CLICK_AIR && player.isSneaking) {
            switchChiselMode(player, playerData)
            event.isCancelled = true
            return
        }

        val block = event.clickedBlock ?: return

        val iterateClockwise = when {
            event.action.isLeftClick -> true
            event.action.isRightClick -> false
            else -> return
        }

        handleChiselAction(block, playerData, iterateClockwise, event)
    }

    /**
     * Switches the [ChiselMode].
     * @param player The [Player] who is using the chisel.
     * @param playerData The [PlayerData] containing the current [ChiselMode].
     */
    private fun switchChiselMode(player: Player, playerData: PlayerData) {
        val newMode = when (playerData.chiselMode) {
            ChiselMode.ROTATE -> ChiselMode.FLIP
            else -> ChiselMode.ROTATE
        }
        PlayerData.setData(playerData.copy(chiselMode = newMode))
        player.sendActionBar("Chisel mode set to $newMode".skylineFmt().mm())
    }

    /**
     * Handles the chisel action based on the [ChiselMode].
     * @param block The [Block] being interacted with.
     * @param playerData The [PlayerData] containing the current [ChiselMode].
     * @param iterateClockwise True if iterating clockwise, false otherwise.
     * @param event The [PlayerInteractEvent] triggered by the [Player].
     */
    private fun handleChiselAction(
        block: Block,
        playerData: PlayerData,
        iterateClockwise: Boolean,
        event: PlayerInteractEvent
    ) {
        val player = event.player
        val item = player.inventory.itemInMainHand

        var used = false

        when (playerData.chiselMode) {
            ChiselMode.ROTATE -> {
                val data = block.blockData
                if (data is Directional) {
                    data.facing = data.facing.iterate(iterateClockwise)
                    block.blockData = data
                    event.isCancelled = true
                    used = true
                }
            }

            ChiselMode.FLIP -> {
                val data = block.blockData
                if (data is Slab && data.type != Slab.Type.DOUBLE) {
                    data.type = data.type.iterate(iterateClockwise)
                    block.blockData = data
                    event.isCancelled = true
                    used = true
                }
            }
        }

        if (used) {
            val meta = item.itemMeta
            if (meta is Damageable) {
                meta.damage = meta.damage + 1
                if (meta.damage >= item.type.maxDurability) {
                    player.inventory.setItemInMainHand(null)
                    player.playSound(Config.ChiselModule.CHISEL_DURABILITY_DECREASE_SOUND)
                } else {
                    item.itemMeta = meta
                }
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
     * Checks if the given [ItemStack] is a chisel.
     * @param item The [ItemStack] to check.
     * @return True if the [ItemStack] is a chisel, false otherwise.
     */
    private fun isChisel(item: ItemStack): Boolean {
        return item.itemMeta?.persistentDataContainer?.has(chiselKey, PersistentDataType.BYTE) == true
    }
}