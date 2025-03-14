/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Openable
import org.bukkit.block.data.type.Door
import org.bukkit.block.data.type.Gate
import org.bukkit.block.data.type.TrapDoor
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.AdjacentBlockData
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.util.concurrent.ConcurrentHashMap


class DoorsModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigData.DoorsModule().enabled

    private val autoCloseDelay = ConfigData.DoorsModule().autoCloseDelay * 1000L
    private val autoClose = ConcurrentHashMap<Block, Long>()

    companion object {
        private const val SCHEDULER_PERIOD_TICKS = 1L
        private val POSSIBLE_NEIGHBOURS = listOf(
            AdjacentBlockData(0, -1, Door.Hinge.RIGHT, BlockFace.EAST),
            AdjacentBlockData(0, 1, Door.Hinge.LEFT, BlockFace.EAST),

            AdjacentBlockData(1, 0, Door.Hinge.RIGHT, BlockFace.SOUTH),
            AdjacentBlockData(-1, 0, Door.Hinge.LEFT, BlockFace.SOUTH),

            AdjacentBlockData(0, 1, Door.Hinge.RIGHT, BlockFace.WEST),
            AdjacentBlockData(0, -1, Door.Hinge.LEFT, BlockFace.WEST),

            AdjacentBlockData(-1, 0, Door.Hinge.RIGHT, BlockFace.NORTH),
            AdjacentBlockData(1, 0, Door.Hinge.LEFT, BlockFace.NORTH)
        )

        private fun toggleDoor(block: Block, openable: Openable, open: Boolean) {
            openable.isOpen = open
            block.blockData = openable
        }
    }

    init {
        instance.server.scheduler.runTaskTimer(instance, Runnable {
            autoClose.entries.removeIf { (block, time) ->
                if (System.currentTimeMillis() >= time) {
                    handleAutoClose(block)
                    true
                } else false
            }
        }, SCHEDULER_PERIOD_TICKS, SCHEDULER_PERIOD_TICKS)
    }

    private fun handleAutoClose(block: Block) {
        val data = block.blockData as? Openable ?: return
        if (!data.isOpen) return
        when (data) {
            is Door -> handleDoorClose(block, data)
            is Gate -> handleGateClose(block)
            else -> return
        }
        data.isOpen = false
        block.blockData = data
    }

    private fun handleDoorClose(block: Block, door: Door) {
        getOtherPart(door, block)?.let { toggleOtherDoor(block, it, false) }
        block.world.playSound(ConfigData.DoorsModule().soundDoorClose)
    }

    private fun handleGateClose(block: Block) = block.world.playSound(ConfigData.DoorsModule().soundGateClose)

    private fun isValidInteraction(event: PlayerInteractEvent): Boolean {
        return event.hand == EquipmentSlot.HAND &&
                event.useInteractedBlock() != Event.Result.DENY &&
                event.useItemInHand() != Event.Result.DENY
    }

    private fun handleLeftClick(event: PlayerInteractEvent, block: Block) {
        if (canKnock(event, event.player) && isKnockableBlock(block.blockData)) {
            block.world.playSound(ConfigData.DoorsModule().soundKnock)
        }
    }

    private fun handleRightClick(block: Block) {
        if (ConfigData.DoorsModule().allowDoubleDoors && (block.blockData is Door || block.blockData is Gate)) {
            processDoorOrGateInteraction(block)
        }
        if (ConfigData.DoorsModule().allowAutoClose) autoClose[block] = System.currentTimeMillis() + autoCloseDelay
    }

    private fun processDoorOrGateInteraction(block: Block) {
        if (block.blockData is Door) {
            val otherDoorBlock = getOtherPart(getDoorBottom(block.blockData as Door, block), block)
            if (otherDoorBlock != null && otherDoorBlock.blockData is Door) {
                val otherDoor = otherDoorBlock.blockData as Door
                toggleOtherDoor(block, otherDoorBlock, !otherDoor.isOpen)
                if (ConfigData.DoorsModule().allowAutoClose) {
                    autoClose[otherDoorBlock] = System.currentTimeMillis() + autoCloseDelay
                }
            }
        }
    }

    private fun canKnock(event: PlayerInteractEvent, player: Player): Boolean {
        return when {
            player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR -> false
            event.action != Action.LEFT_CLICK_BLOCK || event.hand != EquipmentSlot.HAND -> false
            isKnockingConditionViolated(player) -> false
            else -> true
        }
    }

    private fun isKnockingConditionViolated(player: Player): Boolean {
        return (ConfigData.DoorsModule().knockingRequiresShift && !player.isSneaking) ||
                (ConfigData.DoorsModule().knockingRequiresEmptyHand &&
                        player.inventory.itemInMainHand.type != Material.AIR)
    }

    private fun isKnockableBlock(data: BlockData): Boolean = when (data) {
        is Door -> ConfigData.DoorsModule().allowKnockingDoors
        is Gate -> ConfigData.DoorsModule().allowKnockingGates
        is TrapDoor -> ConfigData.DoorsModule().allowKnockingTrapdoors
        else -> false
    }

    private fun toggleOtherDoor(block: Block, block2: Block, open: Boolean) {
        if (block.blockData !is Door || block2.blockData !is Door) return
        instance.server.scheduler.runTaskLater(instance, Runnable {
            val door = block.blockData as Door
            val door2 = block2.blockData as Door
            if (door.isOpen != door2.isOpen) toggleDoor(block2, door2, open)
        }, 1L)
    }

    private fun getDoorBottom(door: Door, block: Block): Door? {
        val bottomHalf = if (door.half == Bisected.Half.BOTTOM) block else block.getRelative(BlockFace.DOWN)
        return (bottomHalf.blockData as? Door)?.takeIf { bottomHalf.type == block.type }
    }

    private fun getOtherPart(door: Door?, block: Block): Block? {
        if (door == null) return null
        return POSSIBLE_NEIGHBOURS.firstOrNull { neighbour ->
            val relative = block.getRelative(neighbour.offsetX, 0, neighbour.offsetZ)
            (relative.blockData as? Door)?.takeIf {
                it.facing == door.facing &&
                        it.hinge != door.hinge &&
                        it.isOpen == door.isOpen &&
                        relative.type == block.type
            } != null
        }?.let { block.getRelative(it.offsetX, 0, it.offsetZ) }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: PlayerInteractEvent) {
        val clickedBlock = event.clickedBlock ?: return
        if (!isValidInteraction(event)) return
        when (event.action) {
            Action.LEFT_CLICK_BLOCK -> handleLeftClick(event, clickedBlock)
            Action.RIGHT_CLICK_BLOCK -> handleRightClick(clickedBlock)
            else -> return
        }
    }
}
