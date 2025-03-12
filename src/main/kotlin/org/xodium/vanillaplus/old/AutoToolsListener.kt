/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package de.jeff_media.bestTools

import de.jeff_media.bestTools.AutoToolsHandler.Companion.getEmptyHotBarSlot
import de.jeff_media.bestTools.Messages.Companion.sendMessage
import de.jeff_media.bestTools.PlayerUtils.isAllowedGameMode
import org.bukkit.Bukkit
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
import java.util.*

class AutoToolsListener internal constructor(main: Main) : Listener {
    val handler: AutoToolsHandler =
        Objects.requireNonNull<AutoToolsHandler>(main.toolHandler, "ToolHandler must not be null")
    val main: Main = Objects.requireNonNull<Main>(main, "Main must not be null")
    val useAxeAsWeapon: Boolean = main.config.getBoolean("use-axe-as-sword")


    @EventHandler
    fun onPlayerAttackEntity(e: EntityDamageByEntityEvent) {
        val st = if (main.measurePerformance) System.nanoTime() else 0
        main.debug("EntityDamageByEntity 1")

        if (e.damager !is Player) return
        main.debug("EntityDamageByEntity 2")
        val p = e.damager as Player
        if (!p.hasPermission("besttools.use")) return
        main.debug("EntityDamageByEntity 3")
        val playerSetting = main.getPlayerSetting(p)
        if (!playerSetting.isBestToolsEnabled()) return
        main.debug("EntityDamageByEntity 4")
        val enemy = e.getEntity()

        if (isAllowedGameMode(p, main.config.getBoolean("allow-in-adventure-mode"))) {
            return
        }

        if (!(enemy is Monster && playerSetting.isSwordOnMobs())
        ) return

        main.debug("Getting the best roscoe for " + enemy.type.name)

        val inv = p.inventory
        val bestRoscoe = handler.getBestRoscoeFromInventory(
            enemy.type,
            p,
            playerSetting.isHotbarOnly(),
            inv.itemInMainHand,
            useAxeAsWeapon
        )

        if (bestRoscoe == null || bestRoscoe == inv.itemInMainHand) {
            main.meter.add(st, false)
            return
        }
        switchToBestRoscoe(p, bestRoscoe, playerSetting.isHotbarOnly(), playerSetting.getFavoriteSlot())
        main.meter.add(st, false)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onBreak(event: BlockBreakEvent) {
        Bukkit.getScheduler().runTaskLater(main, Runnable {
            Bukkit.getPluginManager().callEvent(BestToolsNotifyEvent(event.player, event.getBlock()))
        }, 1)
    }

    @EventHandler
    fun onNotify(event: BestToolsNotifyEvent) {
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

    @EventHandler
    fun onPlayerInteractWithBlock(event: PlayerInteractEvent) {
        val st = if (main.measurePerformance) System.nanoTime() else 0

        val playerSetting = main.getPlayerSetting(event.getPlayer())
        if (playerSetting.getBtcache().valid
            && event.clickedBlock != null && event.clickedBlock!!
                .type == playerSetting.getBtcache().lastMat
        ) {
            main.meter.add(st, true)
            return
        }
        val p = event.getPlayer()
        if (!p.hasPermission("besttools.use")) {
            return
        }
        if (!hasBestToolsEnabled(p, playerSetting)) {
            return
        }
        val block = event.clickedBlock
        if (block == null) {
            return
        }

        if (block.type == Material.AIR) {
            return
        }

        if (main.toolHandler.globalBlacklist.contains(block.type)) {
            return
        }

        if (isAllowedGameMode(p, main.config.getBoolean("allow-in-adventure-mode"))) {
            return
        }
        val inv = p.inventory

        if (main.config.getBoolean("dont-switch-during-battle") && handler.isWeapon(inv.itemInMainHand)) {
            main.debug("Return: It's a gun^^")
            return
        }

        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return
        if (event.hand != EquipmentSlot.HAND) return

        val bestTool =
            handler.getBestToolFromInventory(block.type, p, playerSetting.isHotbarOnly(), inv.itemInMainHand)

        if (bestTool == null || bestTool == inv.itemInMainHand) {
            main.meter.add(st, false)
            playerSetting.getBtcache().validate(block.type)
            return
        }
        switchToBestTool(
            p,
            bestTool,
            playerSetting.isHotbarOnly(),
            block.type /*,playerSetting.getFavoriteSlot()*/
        )
        playerSetting.getBtcache().validate(block.type)
        main.meter.add(st, false)
    }

    private fun getFavoriteSlot(player: Player): Int {
        return if (main.config.getInt("favorite-slot") == -1) {
            player.inventory.heldItemSlot
        } else {
            main.config.getInt("favorite-slot")
        }
    }

    private fun switchToBestTool(p: Player, bestTool: ItemStack?, hotbarOnly: Boolean, target: Material?) {
        var bestTool = bestTool
        val inv = p.inventory
        if (bestTool == null) {
            val currentItem = inv.itemInMainHand

            val emptyHotbarSlot = getEmptyHotBarSlot(inv)
            if (emptyHotbarSlot != -1) {
                inv.heldItemSlot = emptyHotbarSlot
                return
            }

            if (main.toolHandler.isDamageable(currentItem)) return
            bestTool = handler.getNonToolItemFromArray(handler.inventoryToArray(p, hotbarOnly), currentItem, target)
        }
        if (bestTool == null) {
            handler.freeSlot(getFavoriteSlot(p), inv)
            main.debug("Could not find any appropiate tool")
            return
        }
        val positionInInventory = handler.getPositionInInventory(bestTool, inv)
        if (positionInInventory != -1) {
            handler.moveToolToSlot(positionInInventory, getFavoriteSlot(p), inv)
            main.debug("Found tool")
        } else {
            handler.freeSlot(getFavoriteSlot(p), inv)
            main.debug("Use no tool")
        }
    }

    private fun switchToBestRoscoe(p: Player, bestRoscoe: ItemStack?, hotbarOnly: Boolean, favoriteSlot: Int) {
        var bestRoscoe = bestRoscoe
        val inv = p.inventory
        if (bestRoscoe == null) {
            val currentItem = inv.itemInMainHand

            val emptyHotbarSlot = getEmptyHotBarSlot(inv)
            if (emptyHotbarSlot != -1) {
                inv.heldItemSlot = emptyHotbarSlot
                return
            }

            if (main.toolHandler.isDamageable(currentItem)) return
            bestRoscoe =
                handler.getNonToolItemFromArray(handler.inventoryToArray(p, hotbarOnly), currentItem, Material.BEDROCK)
        }
        if (bestRoscoe == null) {
            handler.freeSlot(favoriteSlot, inv)
            main.debug("Could not find any appropiate tool")
            return
        }
        val positionInInventory = handler.getPositionInInventory(bestRoscoe, inv)
        if (positionInInventory != -1) {
            handler.moveToolToSlot(positionInInventory, favoriteSlot, inv)
            main.debug("Found tool")
        } else {
            handler.freeSlot(favoriteSlot, inv)
            main.debug("Use no tool")
        }
    }

    private fun hasBestToolsEnabled(p: Player, playerSetting: PlayerSetting): Boolean {
        if (!playerSetting.isBestToolsEnabled()) {
            if (!playerSetting.isHasSeenBestToolsMessage()) {
                sendMessage(p, main.messages.MSG_BESTTOOL_USAGE)
                playerSetting.setHasSeenBestToolsMessage()
            }
            return false
        }
        return true
    }
}
