package org.xodium.vanillaplus.modules

import com.google.common.base.Enums
import org.bukkit.*
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
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class DoorsModule : ModuleInterface {
    override val cn: String = javaClass.simpleName
    private val pcn = instance.javaClass.simpleName
    private val autoCloseDelay = instance.config.getLong("$cn.autoclose_delay") * 1000
    private val autoClose = ConcurrentHashMap<Block, Long>()

    companion object {
        private val POSSIBLE_NEIGHBOURS = arrayOf<AdjacentBlockData?>(
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
            autoClose.forEach { (block, t) ->
                if (System.currentTimeMillis() >= t) {
                    handleAutoClose(block)
                    autoClose.remove(block)
                }
            }
        }, 1L, 1L)
    }

    private fun handleAutoClose(block: Block) {
        val data = block.blockData
        if (data !is Openable || !data.isOpen) return
        when (data) {
            is Door -> handleDoorClose(block, data)
            is Gate -> handleGateClose(block)
            else -> return
        }
        data.isOpen = false
        block.blockData = data
    }

    private fun handleDoorClose(block: Block, door: Door) {
        getOtherPart(door, block)?.let { otherDoor ->
            toggleOtherDoor(block, otherDoor, false)
        }
        block.world.playSound(block.location, Sound.BLOCK_IRON_DOOR_CLOSE, 1.0f, 1.0f)
    }

    private fun handleGateClose(block: Block) {
        block.world.playSound(block.location, Sound.BLOCK_FENCE_GATE_CLOSE, 1.0f, 1.0f)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: PlayerInteractEvent) {
        val clickedBlock = event.clickedBlock ?: return
        val data = clickedBlock.blockData
        if (!isValidInteraction(event)) return
        when (event.action) {
            Action.LEFT_CLICK_BLOCK -> handleLeftClick(event, data, clickedBlock)
            Action.RIGHT_CLICK_BLOCK -> handleRightClick(event, data, clickedBlock)
            else -> return
        }
    }

    private fun isValidInteraction(event: PlayerInteractEvent): Boolean {
        return event.hand == EquipmentSlot.HAND &&
                event.useInteractedBlock() != Event.Result.DENY &&
                event.useItemInHand() != Event.Result.DENY
    }

    private fun handleLeftClick(event: PlayerInteractEvent, data: BlockData, block: Block) {
        if (canKnock(event, event.player) && isKnockableBlock(data)) playKnockSound(block)
    }

    private fun handleRightClick(event: PlayerInteractEvent, data: BlockData, block: Block) {
        val player = event.player
        if (player.hasPermission("$pcn.doubledoors") &&
            instance.config.getBoolean("$cn.allow_doubledoors") &&
            (data is Door || data is Gate)
        ) processDoorOrGateInteraction(player, data, block)
        if (player.hasPermission("$pcn.autoclose")) autoClose[block] = System.currentTimeMillis() + autoCloseDelay
    }

    private fun processDoorOrGateInteraction(player: Player, data: BlockData, block: Block) {
        if (data is Door) {
            val door = getDoorBottom(data, block)
            val otherDoorBlock = getOtherPart(door, block)
            if (otherDoorBlock != null && otherDoorBlock.blockData is Door) {
                val otherDoor = otherDoorBlock.blockData as Door
                toggleOtherDoor(block, otherDoorBlock, !otherDoor.isOpen)
                if (player.hasPermission("$pcn.autoclose")) {
                    autoClose[otherDoorBlock] = System.currentTimeMillis() + autoCloseDelay
                }
            }
        }
    }

    private fun canKnock(event: PlayerInteractEvent, player: Player): Boolean {
        return when {
            player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR -> false
            !player.hasPermission("$pcn.knock") -> false
            event.action != Action.LEFT_CLICK_BLOCK || event.hand != EquipmentSlot.HAND -> false
            isKnockingConditionViolated(player) -> false
            else -> true
        }
    }

    private fun isKnockingConditionViolated(player: Player): Boolean {
        return (instance.config.getBoolean("$cn.knocking_requires_shift") && !player.isSneaking) ||
                (instance.config.getBoolean("$cn.knocking_requires_empty_hand") &&
                        player.inventory.itemInMainHand.type != Material.AIR)
    }

    private fun isKnockableBlock(data: BlockData): Boolean {
        return when (data) {
            is Door -> instance.config.getBoolean("$cn.allow_knocking")
            is TrapDoor -> instance.config.getBoolean("$cn.allow_knocking_trapdoors")
            is Gate -> instance.config.getBoolean("$cn.allow_knocking_gates")
            else -> false
        }
    }

    private fun playKnockSound(block: Block) {
        block.world.playSound(
            block.location,
            instance.config.getString("$cn.sound_knock_effect")
                ?.lowercase(Locale.getDefault())
                ?.let { NamespacedKey.minecraft(it) }
                ?.let(Registry.SOUNDS::get)
                ?: Sound.ITEM_SHIELD_BLOCK,
            instance.config.getString("$cn.sound_knock_category")
                ?.uppercase(Locale.getDefault())
                ?.let { Enums.getIfPresent(SoundCategory::class.java, it).orNull() }
                ?: SoundCategory.BLOCKS,
            instance.config.getInt("$cn.sound_knock_volume").toFloat(),
            instance.config.getInt("$cn.sound_knock_pitch").toFloat())
    }

    private fun toggleOtherDoor(block: Block, block2: Block, open: Boolean) {
        if (block.blockData !is Door || block2.blockData !is Door) return
        Bukkit.getScheduler().runTaskLater(instance, Runnable {
            val door = block.blockData as Door
            val door2 = block2.blockData as Door
            if (door.isOpen != door2.isOpen) {
                toggleDoor(block2, door2, open)
            }
        }, 1L)
    }

    private fun getDoorBottom(door: Door, block: Block): Door? {
        val bottomHalf = if (door.half == Bisected.Half.BOTTOM) block else block.getRelative(BlockFace.DOWN)
        return (bottomHalf.blockData as? Door)?.takeIf { bottomHalf.type == block.type }
    }

    private fun getOtherPart(door: Door?, block: Block): Block? {
        if (door == null) return null
        return POSSIBLE_NEIGHBOURS.firstOrNull { neighbour ->
            val relative = block.getRelative(neighbour!!.offsetX, 0, neighbour.offsetZ)
            (relative.blockData as? Door)?.takeIf {
                it.facing == door.facing &&
                        it.hinge != door.hinge &&
                        it.isOpen == door.isOpen &&
                        relative.type == block.type
            } != null
        }?.let { block.getRelative(it.offsetX, 0, it.offsetZ) }
    }

    override fun enabled() = instance.config.getBoolean("$cn.enable")
}
