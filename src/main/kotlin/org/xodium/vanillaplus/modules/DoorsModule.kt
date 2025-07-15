package org.xodium.vanillaplus.modules

import net.kyori.adventure.sound.Sound
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
import org.xodium.vanillaplus.data.SoundData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.bukkit.Sound as BukkitSound

/** Represents a module handling door mechanics within the system. */
class DoorsModule : ModuleInterface<DoorsModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    private val autoClose = mutableMapOf<Block, Long>()
    private val possibleNeighbours = listOf(
        AdjacentBlockData(0, -1, Door.Hinge.RIGHT, BlockFace.EAST),
        AdjacentBlockData(0, 1, Door.Hinge.LEFT, BlockFace.EAST),

        AdjacentBlockData(1, 0, Door.Hinge.RIGHT, BlockFace.SOUTH),
        AdjacentBlockData(-1, 0, Door.Hinge.LEFT, BlockFace.SOUTH),

        AdjacentBlockData(0, 1, Door.Hinge.RIGHT, BlockFace.WEST),
        AdjacentBlockData(0, -1, Door.Hinge.LEFT, BlockFace.WEST),

        AdjacentBlockData(-1, 0, Door.Hinge.RIGHT, BlockFace.NORTH),
        AdjacentBlockData(1, 0, Door.Hinge.LEFT, BlockFace.NORTH)
    )

    init {
        if (enabled()) {
            instance.server.scheduler.runTaskTimer(
                instance,
                Runnable {
                    autoClose.entries.removeIf { (block, time) ->
                        System.currentTimeMillis() >= time && handleAutoClose(block)
                    }
                },
                config.initDelay,
                config.interval
            )
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) {
        if (!enabled()) return

        val clickedBlock = event.clickedBlock ?: return
        if (!isValidInteraction(event)) return
        when (event.action) {
            Action.LEFT_CLICK_BLOCK -> handleLeftClick(event, clickedBlock)
            Action.RIGHT_CLICK_BLOCK -> handleRightClick(clickedBlock)
            else -> return
        }
    }

    /**
     * Toggles the state of the door or gate based on the provided block and openable data.
     * @param block The block representing the door or gate to be toggled.
     * @param openable The Openable data representing the door or gate.
     * @param open The desired state (open or closed) for the door or gate.
     */
    private fun toggleDoor(block: Block, openable: Openable, open: Boolean) {
        openable.isOpen = open
        block.blockData = openable
    }

    /**
     * Handles the auto-close functionality for doors and gates.
     * @param block The block representing the door or gate to be closed.
     * @return True if the block was successfully closed, false otherwise.
     */
    private fun handleAutoClose(block: Block): Boolean {
        val data = block.blockData as? Openable ?: return false
        if (!data.isOpen) return false
        when (data) {
            is Door -> handleDoorClose(block, data)
            is Gate -> handleGateClose(block)
            else -> return false
        }
        data.isOpen = false
        block.blockData = data
        return true
    }

    /**
     * Handles the sound effect for closing a door.
     * @param block The block representing the door being closed.
     * @param door The door block data.
     */
    private fun handleDoorClose(block: Block, door: Door) {
        getOtherPart(door, block)?.let { toggleOtherDoor(block, it, false) }
        block.world.playSound(config.soundDoorClose.toSound())
    }

    /**
     * Handles the sound effect for closing a gate.
     * @param block The block representing the gate being closed.
     */
    private fun handleGateClose(block: Block) =
        block.world.playSound(config.soundGateClose.toSound())

    /**
     * Checks if the interaction event is valid for processing.
     * @param event The `player interact event`.
     * @return True if the interaction is valid, false otherwise.
     */
    private fun isValidInteraction(event: PlayerInteractEvent): Boolean {
        return event.hand == EquipmentSlot.HAND &&
                event.useInteractedBlock() != Event.Result.DENY &&
                event.useItemInHand() != Event.Result.DENY
    }

    /**
     * Handles the left-click interaction with doors and gates, playing a knock sound if applicable.
     * @param event The `player interact event`.
     * @param block The block representing the door or gate being interacted with.
     */
    private fun handleLeftClick(event: PlayerInteractEvent, block: Block) {
        if (canKnock(event, event.player) && isKnockableBlock(block.blockData)) {
            block.world.playSound(config.soundKnock.toSound())
        }
    }

    /**
     * Handles the right-click interaction with doors and gates, toggling their state and handling auto-close functionality.
     * @param block The block representing the door or gate being interacted with.
     */
    private fun handleRightClick(block: Block) {
        if (config.allowDoubleDoors && (block.blockData is Door || block.blockData is Gate)) {
            processDoorOrGateInteraction(block)
        }
        if (config.allowAutoClose) autoClose[block] =
            System.currentTimeMillis() + config.autoCloseDelay
    }

    /**
     * Processes the interaction with doors or gates, toggling their state and handling auto-close functionality.
     * @param block The block representing the door or gate being interacted with.
     */
    private fun processDoorOrGateInteraction(block: Block) {
        val door = (block.blockData as? Door) ?: return
        val door2Block = getOtherPart(getDoorBottom(door, block), block) ?: return
        val secondDoor = door2Block.blockData as? Door ?: return
        toggleOtherDoor(block, door2Block, !secondDoor.isOpen)
        if (config.allowAutoClose) {
            autoClose[door2Block] = System.currentTimeMillis() + config.autoCloseDelay
        }
    }

    /**
     * Checks if the player can knock on the block based on their game mode and interaction conditions.
     * @param event The `player interact event`.
     * @param player The player attempting to knock.
     * @return True if the player can knock, false otherwise.
     */
    private fun canKnock(event: PlayerInteractEvent, player: Player): Boolean {
        return when {
            player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR -> false
            event.action != Action.LEFT_CLICK_BLOCK || event.hand != EquipmentSlot.HAND -> false
            isKnockingConditionViolated(player) -> false
            else -> true
        }
    }

    /**
     * Checks if the knocking conditions are violated based on the player's state and configuration.
     * @param player The player attempting to knock.
     * @return True if the knocking conditions are violated, false otherwise.
     */
    private fun isKnockingConditionViolated(player: Player): Boolean {
        return (config.knockingRequiresShifting && !player.isSneaking) ||
                (config.knockingRequiresEmptyHand &&
                        player.inventory.itemInMainHand.type != Material.AIR)
    }

    /**
     * Checks if the block data is of a type that can be knocked on.
     * @param data The block data to check.
     * @return True if the block can be knocked on, false otherwise.
     */
    private fun isKnockableBlock(data: BlockData): Boolean {
        return when (data) {
            is Door -> config.allowKnockingDoors
            is Gate -> config.allowKnockingGates
            is TrapDoor -> config.allowKnockingTrapdoors
            else -> false
        }
    }

    /**
     * Toggles the state of the other door when one door is opened or closed.
     * @param block The block representing the first door.
     * @param block2 The block representing the second door.
     * @param open The desired state (open or closed) for the second door.
     */
    private fun toggleOtherDoor(block: Block, block2: Block, open: Boolean) {
        if (block.blockData !is Door || block2.blockData !is Door) return
        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                val door = block.blockData as Door
                val door2 = block2.blockData as Door
                if (door.isOpen != door2.isOpen) toggleDoor(block2, door2, open)
            },
            config.initDelay
        )
    }

    /**
     * Retrieves the bottom half of a door if the provided block is the top half.
     * @param door The door block data.
     * @param block The block to check.
     * @return The bottom half of the door if it exists, null otherwise.
     */
    private fun getDoorBottom(door: Door, block: Block): Door? {
        return (if (door.half == Bisected.Half.BOTTOM) block else block.getRelative(BlockFace.DOWN))
            .blockData as? Door
    }

    /**
     * Retrieves the other part of a door if it exists.
     * @param door The door block data.
     * @param block The block to check.
     * @return The other part of the door if it exists, null otherwise.
     */
    private fun getOtherPart(door: Door?, block: Block): Block? {
        if (door == null) return null
        return possibleNeighbours.firstOrNull { neighbour ->
            val relative = block.getRelative(neighbour.offsetX, 0, neighbour.offsetZ)
            (relative.blockData as? Door)?.takeIf {
                it.facing == door.facing &&
                        it.hinge != door.hinge &&
                        it.isOpen == door.isOpen &&
                        relative.type == block.type
            } != null
        }?.let { block.getRelative(it.offsetX, 0, it.offsetZ) }
    }

    data class Config(
        override var enabled: Boolean = true,
        var initDelay: Long = 1L,
        var interval: Long = 1L,
        var allowAutoClose: Boolean = true,
        var allowDoubleDoors: Boolean = true,
        var allowKnockingDoors: Boolean = true,
        var allowKnockingGates: Boolean = true,
        var allowKnockingTrapdoors: Boolean = true,
        var knockingRequiresEmptyHand: Boolean = true,
        var knockingRequiresShifting: Boolean = true,
        var autoCloseDelay: Long = 6L * 1000L,
        var soundDoorClose: SoundData = SoundData(
            BukkitSound.BLOCK_IRON_DOOR_CLOSE,
            Sound.Source.BLOCK
        ),
        var soundGateClose: SoundData = SoundData(
            BukkitSound.BLOCK_FENCE_GATE_CLOSE,
            Sound.Source.BLOCK
        ),
        var soundKnock: SoundData = SoundData(
            BukkitSound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR,
            Sound.Source.HOSTILE
        ),
    ) : ModuleInterface.Config
}
