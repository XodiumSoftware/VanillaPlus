/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
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
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.AdjacentBlockData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.util.concurrent.ConcurrentHashMap


class DoorsModule : ModuleInterface {
    private val autoCloseDelay = Config.DoorsModule.AUTO_CLOSE_DELAY * 1000
    private val autoClose = ConcurrentHashMap<Block, Long>()

    companion object {
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

        fun toggleDoor(block: Block, openable: Openable, open: Boolean) {
            openable.isOpen = open
            block.blockData = openable
        }
    }

    override fun init() {
        Bukkit.getScheduler().runTaskTimer(instance, Runnable {
            autoClose.entries.removeIf { (block, time) ->
                if (System.currentTimeMillis() >= time) {
                    handleAutoClose(block)
                    true
                } else false
            }
        }, 1L, 1L)
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
        getOtherPart(door, block)?.let {
            toggleOtherDoor(block, it, false)
        }
        Utils.playSound(
            block,
            Config.DoorsModule.SOUND_CLOSE_DOOR_EFFECT,
            Sound.BLOCK_IRON_DOOR_CLOSE,
            Config.DoorsModule.SOUND_CLOSE_DOOR_VOLUME,
            Config.DoorsModule.SOUND_CLOSE_DOOR_PITCH,
        )
    }

    private fun handleGateClose(block: Block) {
        Utils.playSound(
            block,
            Config.DoorsModule.SOUND_CLOSE_GATE_EFFECT,
            Sound.BLOCK_FENCE_GATE_CLOSE,
            Config.DoorsModule.SOUND_CLOSE_GATE_VOLUME,
            Config.DoorsModule.SOUND_CLOSE_GATE_PITCH,
        )
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: PlayerInteractEvent) {
        val clickedBlock = event.clickedBlock ?: return
        val data = clickedBlock.blockData
        if (!isValidInteraction(event)) return
        when (event.action) {
            Action.LEFT_CLICK_BLOCK -> handleLeftClick(event, data, clickedBlock)
            Action.RIGHT_CLICK_BLOCK -> handleRightClick(data, clickedBlock)
            else -> return
        }
    }

    private fun isValidInteraction(event: PlayerInteractEvent): Boolean {
        return event.hand == EquipmentSlot.HAND &&
                event.useInteractedBlock() != Event.Result.DENY &&
                event.useItemInHand() != Event.Result.DENY
    }

    private fun handleLeftClick(event: PlayerInteractEvent, data: BlockData, block: Block) {
        if (canKnock(event, event.player) && isKnockableBlock(data)) {
            Utils.playSound(
                block,
                Config.DoorsModule.SOUND_KNOCK_EFFECT,
                Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR,
                Config.DoorsModule.SOUND_KNOCK_VOLUME,
                Config.DoorsModule.SOUND_KNOCK_PITCH
            )
        }
    }

    private fun handleRightClick(data: BlockData, block: Block) {
        if (Config.DoorsModule.ALLOW_DOUBLE_DOORS && (data is Door || data is Gate))
            processDoorOrGateInteraction(data, block)
        if (Config.DoorsModule.ALLOW_AUTO_CLOSE) autoClose[block] = System.currentTimeMillis() + autoCloseDelay
    }

    private fun processDoorOrGateInteraction(data: BlockData, block: Block) {
        if (data is Door) {
            val door = getDoorBottom(data, block)
            val otherDoorBlock = getOtherPart(door, block)
            if (otherDoorBlock != null && otherDoorBlock.blockData is Door) {
                val otherDoor = otherDoorBlock.blockData as Door
                toggleOtherDoor(block, otherDoorBlock, !otherDoor.isOpen)
                if (Config.DoorsModule.ALLOW_AUTO_CLOSE) {
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
        return (Config.DoorsModule.KNOCKING_REQUIRES_SHIFT && !player.isSneaking) ||
                (Config.DoorsModule.KNOCKING_REQUIRES_EMPTY_HAND &&
                        player.inventory.itemInMainHand.type != Material.AIR)
    }

    private fun isKnockableBlock(data: BlockData): Boolean {
        return when (data) {
            is Door -> Config.DoorsModule.ALLOW_KNOCKING_DOORS
            is TrapDoor -> Config.DoorsModule.ALLOW_KNOCKING_TRAPDOORS
            is Gate -> Config.DoorsModule.ALLOW_KNOCKING_GATES
            else -> false
        }
    }

    private fun toggleOtherDoor(block: Block, block2: Block, open: Boolean) {
        if (block.blockData !is Door || block2.blockData !is Door) return
        Bukkit.getScheduler().runTaskLater(instance, Runnable {
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

    override fun enabled(): Boolean = Config.DoorsModule.ENABLED
}
