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
import org.bukkit.block.data.Directional
import org.bukkit.block.data.type.Slab
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
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
        println("PlayerInteractEvent fired: action=${event.action}, sneaking=${event.player.isSneaking}")
        if (!enabled()) return

        val player = event.player
        val item = player.inventory.itemInMainHand

        if (!isChisel(item)) {
            println("Not a chisel: ${item.type}")
            return
        }

        val playerId = player.uniqueId.toString()
        val playerData = PlayerData.getData().find { it.id == playerId } ?: PlayerData(playerId)

        println("Action: ${event.action}, Sneaking: ${player.isSneaking}, Mode: ${playerData.chiselMode}")

        //TODO: switching doesn't work.
        if (event.action.isLeftClick && player.isSneaking) {
            println("Attempting to switch chisel mode for player $playerId")
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
        println("Switching mode from ${playerData.chiselMode} to $newMode for player ${playerData.id}")
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
        @Suppress("UnstableApiUsage")
        return ItemStack.of(Material.BRUSH).apply {
            editPersistentDataContainer { it.set(chiselKey, PersistentDataType.BYTE, 1) }
            setData(DataComponentTypes.ITEM_MODEL, Key.key("chisel"))
            setData(DataComponentTypes.CUSTOM_NAME, "Chisel".mm())
            setData(
                DataComponentTypes.LORE, ItemLore.lore(
                    //TODO: dont forget to adjust lore based on what solution you have for the mode switching issue.
                    listOf(
                        "Usage:".fireFmt(),
                        "${"[Sneak + Right-click]".skylineFmt()} <white>Switch Mode",
                        "${"[Right-click]".skylineFmt()} <white>Iterate Block Faces Clockwise",
                        "${"[Left-click]".skylineFmt()} <white>Iterate Block Faces Anti-Clockwise",
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