/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.old

import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.ConfigData
import java.util.*

class AutoToolsListener : Listener {
    val handler: AutoToolsHandler =
        Objects.requireNonNull<AutoToolsHandler>(AutoToolsHandler(), "ToolHandler must not be null")
    val useAxeAsWeapon: Boolean = ConfigData.AutoToolModule().useAxeAsSword

    @EventHandler
    fun on(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) return
        val player = event.damager as Player
        if (!player.hasPermission(Perms.AutoTool.USE)) return
        TODO("check if autotool is enabled in db")
        val entity = event.getEntity()
        if (!(entity is Monster && ConfigData.AutoToolModule().useSwordOnHostileMobs)) return
        val playerInventory = player.inventory
        val bestRoscoe = handler.getBestRoscoeFromInventory(entity.type, player, useAxeAsWeapon)
        if (bestRoscoe == null || bestRoscoe == playerInventory.itemInMainHand) return
        switchToBestRoscoe(player, bestRoscoe, ConfigData.AutoToolModule().favoriteSlot)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: BlockBreakEvent) {
        instance.server.scheduler.runTaskLater(instance, Runnable {
            instance.server.pluginManager.callEvent(AutoToolNotifyEvent(event.player, event.getBlock()))
        }, 1)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: AutoToolNotifyEvent) {
        onPlayerInteractWithBlock(
            PlayerInteractEvent(
                event.getPlayer(),
                Action.LEFT_CLICK_BLOCK,
                event.getPlayer().inventory.itemInMainHand,
                event.block,
                BlockFace.SELF,
                EquipmentSlot.HAND
            )
        )
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerInteractWithBlock(event: PlayerInteractEvent) {
        val playerSetting = main.getPlayerSetting(event.getPlayer()) // FIX
        if (playerSetting.getBtcache().valid
            && event.clickedBlock != null && event.clickedBlock!!
                .type == playerSetting.getBtcache().lastMat
        ) return
        val player = event.player
        if (!player.hasPermission(Perms.AutoTool.USE)) return
        if (!hasBestToolsEnabled(player)) return
        val block = event.clickedBlock
        if (block == null) return
        if (block.type == Material.AIR) return
        val playerInventory = player.inventory
        if (ConfigData.AutoToolModule().dontSwitchDuringBattle && handler.isWeapon(playerInventory.itemInMainHand))
            return
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return
        if (event.hand != EquipmentSlot.HAND) return
        val bestTool = handler.getBestToolFromInventory(block.type, player, playerInventory.itemInMainHand)
        if (bestTool == null || bestTool == playerInventory.itemInMainHand) {
            playerSetting.getBtcache().validate(block.type)
            return
        }
        switchToBestTool(player, bestTool, block.type)
        playerSetting.getBtcache().validate(block.type)
    }

    private fun getFavoriteSlot(player: Player): Int {
        return if (ConfigData.AutoToolModule().favoriteSlot == -1) {
            player.inventory.heldItemSlot
        } else {
            ConfigData.AutoToolModule().favoriteSlot
        }
    }

    private fun switchToBestTool(player: Player, itemStack: ItemStack?, target: Material?) {
        var bestTool = itemStack
        val playerInventory = player.inventory
        if (bestTool == null) {
            val currentItem = playerInventory.itemInMainHand
            if (handler.isDamageable(currentItem)) return
            bestTool = handler.getNonToolItemFromArray(handler.inventoryToArray(player), currentItem, target)
        }
        if (bestTool == null) {
            handler.freeSlot(getFavoriteSlot(player), playerInventory)
            return
        }
        val positionInInventory = handler.getPositionInInventory(bestTool, playerInventory)
        if (positionInInventory != -1) {
            handler.moveToolToSlot(positionInInventory, getFavoriteSlot(player), playerInventory)
        } else {
            handler.freeSlot(getFavoriteSlot(player), playerInventory)
        }
    }

    private fun switchToBestRoscoe(player: Player, bestRoscoe: ItemStack?, favoriteSlot: Int) {
        var bestRoscoe = bestRoscoe
        val playerInventory = player.inventory
        if (bestRoscoe == null) {
            val currentItem = playerInventory.itemInMainHand
            if (handler.isDamageable(currentItem)) return
            bestRoscoe =
                handler.getNonToolItemFromArray(
                    handler.inventoryToArray(player),
                    currentItem,
                    Material.BEDROCK
                )
        }
        if (bestRoscoe == null) {
            handler.freeSlot(favoriteSlot, playerInventory)
            return
        }
        val positionInInventory = handler.getPositionInInventory(bestRoscoe, playerInventory)
        if (positionInInventory != -1) {
            handler.moveToolToSlot(positionInInventory, favoriteSlot, playerInventory)
        } else {
            handler.freeSlot(favoriteSlot, playerInventory)
        }
    }

    private fun hasBestToolsEnabled(player: Player): Boolean {
        TODO("change into toggle()?")
    }
}
