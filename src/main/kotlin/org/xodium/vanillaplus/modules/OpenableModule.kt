@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import net.kyori.adventure.sound.Sound
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Openable
import org.bukkit.block.data.type.Door
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
// TODO: replace.
import java.util.*

/** Represents a module handling openable blocks mechanics within the system. */
internal object OpenableModule : ModuleInterface {
    private val disallowedKnockGameModes = EnumSet.of(GameMode.CREATIVE, GameMode.SPECTATOR)
    private val possibleNeighbours: Set<AdjacentBlockData> =
        setOf(
            AdjacentBlockData(0, -1, Door.Hinge.RIGHT, BlockFace.EAST),
            AdjacentBlockData(0, 1, Door.Hinge.LEFT, BlockFace.EAST),
            AdjacentBlockData(1, 0, Door.Hinge.RIGHT, BlockFace.SOUTH),
            AdjacentBlockData(-1, 0, Door.Hinge.LEFT, BlockFace.SOUTH),
            AdjacentBlockData(0, 1, Door.Hinge.RIGHT, BlockFace.WEST),
            AdjacentBlockData(0, -1, Door.Hinge.LEFT, BlockFace.WEST),
            AdjacentBlockData(-1, 0, Door.Hinge.RIGHT, BlockFace.NORTH),
            AdjacentBlockData(1, 0, Door.Hinge.LEFT, BlockFace.NORTH),
        )

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) = playerInteract(event)

    /**
     * Handles block interactions and delegates to the correct click handler.
     * @param event The [PlayerInteractEvent] triggered by the player.
     */
    private fun playerInteract(event: PlayerInteractEvent) {
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
    private fun toggleDoor(
        block: Block,
        openable: Openable,
        open: Boolean,
    ) {
        openable.isOpen = open
        block.blockData = openable
    }

    /**
     * Checks if the interaction event is valid for processing.
     * @param event The `player interact event`.
     * @return True if the interaction is valid, false otherwise.
     */
    private fun isValidInteraction(event: PlayerInteractEvent): Boolean =
        event.hand == EquipmentSlot.HAND &&
            event.useInteractedBlock() != Event.Result.DENY &&
            event.useItemInHand() != Event.Result.DENY

    /**
     * Handles the left-click interaction with doors and gates, playing a knock sound if applicable.
     * @param event The `player interact event`.
     * @param block The block representing the door or gate being interacted with.
     */
    private fun handleLeftClick(
        event: PlayerInteractEvent,
        block: Block,
    ) {
        if (canKnock(event, event.player) && isKnockableBlock(block.blockData)) playKnockSound(block)
    }

    /**
     * Handles the right-click interaction with doors and gates, toggling their state.
     * @param block The block representing the door or gate being interacted with.
     */
    private fun handleRightClick(block: Block) {
        if (config.openableModule.allowDoubleDoors && block.blockData is Openable) processDoorOrGateInteraction(block)
    }

    /**
     * Plays the knocking sound to all nearby players around the specified block.
     * @param block The block where the knock sound originates. The sound will be played
     *              at this block's location and propagated to nearby players.
     */
    private fun playKnockSound(block: Block) {
        block.world
            .getNearbyPlayers(block.location, config.openableModule.soundProximityRadius)
            .forEach { it.playSound(config.openableModule.soundKnock.toSound()) }
    }

    /**
     * Processes the interaction with doors or gates, toggling their state.
     * @param block The block representing the door or gate being interacted with.
     */
    private fun processDoorOrGateInteraction(block: Block) {
        val door2Block = getOtherPart(getDoorBottom(block), block) ?: return
        val secondDoor = door2Block.blockData as? Door ?: return

        toggleOtherDoor(block, door2Block, !secondDoor.isOpen)
    }

    /**
     * Checks if the player can knock on the block based on their game mode and interaction conditions.
     * @param event The `player interact event`.
     * @param player The player attempting to knock.
     * @return True if the player can knock, false otherwise.
     */
    private fun canKnock(
        event: PlayerInteractEvent,
        player: Player,
    ): Boolean =
        event.action == Action.LEFT_CLICK_BLOCK &&
            event.hand == EquipmentSlot.HAND &&
            player.gameMode !in disallowedKnockGameModes &&
            !isKnockingConditionViolated(player)

    /**
     * Checks if the knocking conditions are violated based on the player's state and config.
     * @param player The player attempting to knock.
     * @return True if any knocking condition is violated, false otherwise.
     */
    private fun isKnockingConditionViolated(player: Player): Boolean =
        isViolatingSneakingRequirement(player) || isViolatingEmptyHandRequirement(player)

    /**
     * Checks if the player is violating the sneaking requirement for knocking.
     * @param player The player attempting to knock.
     * @return True if sneaking is required, but the player isn't sneaking, false otherwise.
     */
    private fun isViolatingSneakingRequirement(player: Player): Boolean =
        config.openableModule.knockingRequiresShifting && !player.isSneaking

    /**
     * Checks if the player is violating the empty hand requirement for knocking.
     * @param player The player attempting to knock.
     * @return True if an empty hand is required, but the player is holding something, false otherwise.
     */
    private fun isViolatingEmptyHandRequirement(player: Player): Boolean =
        config.openableModule.knockingRequiresEmptyHand &&
            player.inventory.itemInMainHand.type != Material.AIR

    /**
     * Checks if the block data is of a type that can be knocked on.
     * @param data The block data to check.
     * @return True if the block can be knocked on, false otherwise.
     */
    private fun isKnockableBlock(data: BlockData): Boolean = config.openableModule.allowKnocking && data is Openable

    /**
     * Toggles the state of the other door when one door is opened or closed.
     * @param block The block representing the first door.
     * @param block2 The block representing the second door.
     * @param open The desired state (open or closed) for the second door.
     * @param delay The delay in ticks before toggling the secondary door (defaults to config.openableFeature.initDelayInTicks).
     *              This delay helps prevent race conditions with block updates.
     */
    private fun toggleOtherDoor(
        block: Block,
        block2: Block,
        open: Boolean,
        delay: Long = config.openableModule.initDelayInTicks,
    ) {
        if (block.blockData !is Door || block2.blockData !is Door) return

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                val door = block.blockData as Door
                val door2 = block2.blockData as Door
                if (door.isOpen != door2.isOpen) toggleDoor(block2, door2, open)
            },
            delay,
        )
    }

    /**
     * Retrieves the bottom half of a door if the provided block is the top half.
     * @param block The block to check.
     * @return The bottom half of the door if it exists, null otherwise.
     */
    private fun getDoorBottom(block: Block): Door? {
        val door = block.blockData as? Door ?: return null

        return if (door.half == Bisected.Half.BOTTOM) door else block.getRelative(BlockFace.DOWN).blockData as? Door
    }

    /**
     * Retrieves the other part of a door if it exists.
     * @param door The door block data.
     * @param block The block to check.
     * @return The other part of the door if it exists, null otherwise.
     */
    private fun getOtherPart(
        door: Door?,
        block: Block,
    ): Block? {
        if (door == null) return null

        return possibleNeighbours
            .map { it to block.getRelative(it.offsetX, 0, it.offsetZ).blockData as? Door }
            .firstOrNull { (neighbour, otherDoor) ->
                otherDoor?.let { neighbour.matchesDoorPair(it, door, block.type) } == true
            }?.first
            ?.getRelativeBlock(block)
    }

    @Serializable
    data class Config(
        var enabled: Boolean = true,
        var initDelayInTicks: Long = 1,
        var allowDoubleDoors: Boolean = true,
        var allowKnocking: Boolean = true,
        var knockingRequiresEmptyHand: Boolean = true,
        var knockingRequiresShifting: Boolean = true,
        var soundKnock: SoundData = SoundData("entity.zombie.attack_wooden_door", Sound.Source.HOSTILE),
        var soundProximityRadius: Double = 10.0,
    )
}
