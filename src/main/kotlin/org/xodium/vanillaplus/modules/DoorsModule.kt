package org.xodium.vanillaplus.modules

import com.google.common.base.Enums
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.Openable
import org.bukkit.block.data.type.Door
import org.bukkit.block.data.type.Gate
import org.bukkit.block.data.type.TrapDoor
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.scheduler.BukkitRunnable
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.AdjacentBlockData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.util.*
import java.util.concurrent.ConcurrentHashMap

// TODO: refactor.
class DoorsModule : ModuleInterface {
    private val cn: String = javaClass.getSimpleName()
    private val autoCloseDelay: Long = instance.config.getLong("$cn${CONFIG.AUTOCLOSE_DELAY}") * 1000
    private val autoClose = ConcurrentHashMap<Block, Long>()

    init {
        Bukkit.getScheduler().runTaskTimer(instance, Runnable {
            val currentTime = System.currentTimeMillis()
            val iterator = autoClose.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val block = entry.key
                val expiryTime = entry.value
                if (currentTime >= expiryTime) {
                    if (block.blockData is Openable) {
                        val openable = block.blockData as Openable
                        if (openable.isOpen) {
                            if (openable is Door) {
                                getOtherPart(openable, block)?.let { otherDoor ->
                                    toggleOtherDoor(block, otherDoor, false)
                                }
                            } else if (openable is Gate) {
                                block.world.playSound(block.location, Sound.BLOCK_FENCE_GATE_CLOSE, 1.0f, 1.0f)
                            }
                            openable.isOpen = false
                            block.blockData = openable
                            block.world.playSound(block.location, Sound.BLOCK_IRON_DOOR_CLOSE, 1.0f, 1.0f)
                        }
                    }
                    iterator.remove()
                }
            }
        }, 1L, 1L)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onRightClick(e: PlayerInteractEvent) {
        val clickedBlock = e.clickedBlock ?: return
        val blockData = clickedBlock.blockData
        if (e.hand != EquipmentSlot.HAND || e.action != Action.RIGHT_CLICK_BLOCK || e.useInteractedBlock() == Event.Result.DENY || e.useItemInHand() == Event.Result.DENY || !e.player.hasPermission(
                PERMS.USE
            )
            || !(blockData is Door || blockData is Gate) || !instance.config.getBoolean(cn + CONFIG.ALLOW_DOUBLEDOORS)
        ) return
        if (blockData is Door) {
            val door = getBottomDoor(blockData, clickedBlock)
            val otherDoorBlock = getOtherPart(door, clickedBlock)
            if (otherDoorBlock != null && otherDoorBlock.blockData is Door) {
                val otherDoor = otherDoorBlock.blockData as Door
                toggleOtherDoor(clickedBlock, otherDoorBlock, !otherDoor.isOpen)
                if (e.player.hasPermission(PERMS.AUTOCLOSE)) {
                    autoClose[otherDoorBlock] = System.currentTimeMillis() + autoCloseDelay
                }
            }
        }
        if (e.player.hasPermission(PERMS.AUTOCLOSE)) {
            autoClose[clickedBlock] = System.currentTimeMillis() + autoCloseDelay
        }
    }

    @EventHandler
    fun onKnock(e: PlayerInteractEvent) {
        val p = e.player
        if (p.gameMode == GameMode.CREATIVE || p.gameMode == GameMode.SPECTATOR || !p.hasPermission(PERMS.KNOCK) || e.action != Action.LEFT_CLICK_BLOCK || e.hand != EquipmentSlot.HAND || (instance.config.getBoolean(
                cn + CONFIG.KNOCKING_REQUIRES_SHIFT
            ) && !p.isSneaking)
            || (instance.config.getBoolean(cn + CONFIG.KNOCKING_REQUIRES_EMPTY_HAND)
                    && p.inventory.itemInMainHand.type != Material.AIR)
            || e.clickedBlock == null
        ) return
        val block = e.clickedBlock
        val blockData = block!!.blockData
        if ((blockData is Door && instance.config.getBoolean(cn + CONFIG.ALLOW_KNOCKING))
            || (blockData is TrapDoor && instance.config.getBoolean(cn + CONFIG.ALLOW_KNOCKING_TRAPDOORS))
            || (blockData is Gate && instance.config.getBoolean(cn + CONFIG.ALLOW_KNOCKING_GATES))
        ) {
            playKnockSound(block)
        }
    }

    private fun playKnockSound(block: Block) {
        block.world.playSound(
            block.location,
            instance.config.getString("$cn${CONFIG.SOUND_KNOCK_WOOD}")
                ?.lowercase(Locale.getDefault())
                ?.let { NamespacedKey.minecraft(it) }
                ?.let(Registry.SOUNDS::get)
                ?: Sound.ITEM_SHIELD_BLOCK,
            instance.config.getString("$cn${CONFIG.SOUND_KNOCK_CATEGORY}")
                ?.uppercase(Locale.getDefault())
                ?.let { Enums.getIfPresent(SoundCategory::class.java, it).orNull() }
                ?: SoundCategory.BLOCKS,
            instance.config.getInt("$cn${CONFIG.SOUND_KNOCK_VOLUME}").toFloat(),
            instance.config.getInt("$cn${CONFIG.SOUND_KNOCK_PITCH}").toFloat())
    }

    private fun toggleOtherDoor(block: Block, otherBlock: Block, open: Boolean) {
        if (block.blockData !is Door || otherBlock.blockData !is Door) return
        object : BukkitRunnable() {
            override fun run() {
                val currentDoor = block.blockData as Door
                val otherDoor = otherBlock.blockData as Door
                if (currentDoor.isOpen == otherDoor.isOpen) {
                    return
                }
                toggleDoor(otherBlock, otherDoor, open)
            }
        }.runTaskLater(instance, 1L)
    }

    private fun getBottomDoor(door: Door, block: Block): Door? {
        val belowBlock = if (door.half == Bisected.Half.BOTTOM) block else block.getRelative(BlockFace.DOWN)
        return (belowBlock.blockData as? Door)?.takeIf { belowBlock.type == block.type }
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

    override fun enabled(): Boolean {
        return instance.config.getBoolean(cn + ModuleInterface.CONFIG.ENABLE)
    }

    override fun config() {
        instance.config.addDefault(cn + ModuleInterface.CONFIG.ENABLE, true)
        instance.config.addDefault(cn + CONFIG.SOUND_KNOCK_CATEGORY, "BLOCKS")
        instance.config.addDefault(cn + CONFIG.SOUND_KNOCK_PITCH, 1.0)
        instance.config.addDefault(cn + CONFIG.SOUND_KNOCK_VOLUME, 1.0)
        instance.config.addDefault(cn + CONFIG.SOUND_KNOCK_WOOD, "entity_zombie_attack_wooden_door")
        instance.config.addDefault(cn + CONFIG.ALLOW_AUTOCLOSE, true)
        instance.config.addDefault(cn + CONFIG.ALLOW_DOUBLEDOORS, true)
        instance.config.addDefault(cn + CONFIG.ALLOW_KNOCKING, true)
        instance.config.addDefault(cn + CONFIG.ALLOW_KNOCKING_GATES, true)
        instance.config.addDefault(cn + CONFIG.ALLOW_KNOCKING_TRAPDOORS, true)
        instance.config.addDefault(cn + CONFIG.KNOCKING_REQUIRES_EMPTY_HAND, true)
        instance.config.addDefault(cn + CONFIG.KNOCKING_REQUIRES_SHIFT, false)
        instance.config.addDefault(cn + CONFIG.AUTOCLOSE_DELAY, 6)
        instance.saveConfig()
    }

    private interface CONFIG : ModuleInterface.CONFIG {
        companion object {
            // Sound settings
            const val SOUND_KNOCK_CATEGORY: String = ".sound_knock_category"
            const val SOUND_KNOCK_PITCH: String = ".sound_knock_pitch"
            const val SOUND_KNOCK_VOLUME: String = ".sound_knock_volume"
            const val SOUND_KNOCK_WOOD: String = ".sound_knock_wood"

            // Behavior settings
            const val ALLOW_AUTOCLOSE: String = ".allow_autoclose"
            const val ALLOW_DOUBLEDOORS: String = ".allow_doubledoors"
            const val ALLOW_KNOCKING: String = ".allow_knocking"
            const val ALLOW_KNOCKING_GATES: String = ".allow_knocking_gates"
            const val ALLOW_KNOCKING_TRAPDOORS: String = ".allow_knocking_trapdoors"
            const val KNOCKING_REQUIRES_EMPTY_HAND: String = ".knocking_requires_empty_hand"
            const val KNOCKING_REQUIRES_SHIFT: String = ".knocking_requires_shift"

            // Auto-close settings
            const val AUTOCLOSE_DELAY: String = ".autoclose_delay"
        }
    }

    private interface PERMS {
        companion object {
            val USE: String = instance.javaClass.getSimpleName() + ".doubledoors"
            val KNOCK: String = instance.javaClass.getSimpleName() + ".knock"
            val AUTOCLOSE: String = instance.javaClass.getSimpleName() + ".autoclose"
        }
    }

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

        fun toggleDoor(doorBlock: Block, openable: Openable, open: Boolean) {
            openable.isOpen = open
            doorBlock.blockData = openable
        }
    }
}
