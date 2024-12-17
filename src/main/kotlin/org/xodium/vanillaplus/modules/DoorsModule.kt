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
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.scheduler.BukkitRunnable
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.data.AdjacentBlockData
import java.util.*

// TODO: refactor.
class DoorsModule : ModuleInterface {
    private val cn: String = javaClass.getSimpleName()
    private val autoClose = HashMap<Block?, Long?>()

    init {
        Bukkit.getScheduler().runTaskTimer(instance, Runnable {
            val it = autoClose.entries.iterator()
            while (it.hasNext()) {
                val entry = it.next()
                val b = entry.key
                val time = entry.value ?: continue
                if (System.currentTimeMillis() < time) continue
                if (b?.blockData is Openable) {
                    val openable = b.blockData as Openable
                    if (openable.isOpen) {
                        if (openable is Door) {
                            val otherDoor = getOtherPart(openable, b)
                            if (otherDoor != null) {
                                toggleOtherDoor(b, otherDoor, false)
                            }
                        } else if (openable is Gate) {
                            b.world.playSound(b.location, Sound.BLOCK_FENCE_GATE_CLOSE, 1.0f, 1.0f)
                        }
                        openable.isOpen = false
                        b.blockData = openable
                        b.world.playSound(b.location, Sound.BLOCK_IRON_DOOR_CLOSE, 1.0f, 1.0f)
                    }
                }
                it.remove()
            }
        }, 1, 1)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onRightClick(e: PlayerInteractEvent) {
        val clickedBlock = e.clickedBlock ?: return
        val blockData = clickedBlock.blockData

        if (e.hand != EquipmentSlot.HAND || e.action != Action.RIGHT_CLICK_BLOCK || e.useInteractedBlock() == Event.Result.DENY || e.useItemInHand() == Event.Result.DENY || !e.player
                .hasPermission(
                    PERMS.USE
                ) || !(blockData is Door || blockData is Gate) || !FC.getBoolean(cn + CONFIG.ALLOW_DOUBLEDOORS)
        ) return

        if (blockData is Door) {
            val door = getBottomDoor(blockData, clickedBlock)
            val otherDoorBlock = getOtherPart(door, clickedBlock)
            if (otherDoorBlock != null && otherDoorBlock.blockData is Door) {
                val otherDoor = otherDoorBlock.blockData as Door
                toggleOtherDoor(clickedBlock, otherDoorBlock, !otherDoor.isOpen)
                if (e.player.hasPermission(PERMS.AUTOCLOSE)) {
                    autoClose[otherDoorBlock] = System.currentTimeMillis() +
                            FC.getLong(cn + CONFIG.AUTOCLOSE_DELAY) * 1000
                }
            }
        }
        if (e.player.hasPermission(PERMS.AUTOCLOSE)) {
            autoClose[clickedBlock] = System.currentTimeMillis() + FC.getLong(cn + CONFIG.AUTOCLOSE_DELAY) * 1000
        }
    }

    @EventHandler
    fun onKnock(e: PlayerInteractEvent) {
        val p = e.player

        if (p.gameMode == GameMode.CREATIVE || p.gameMode == GameMode.SPECTATOR || !p.hasPermission(PERMS.KNOCK) || e.action != Action.LEFT_CLICK_BLOCK || e.hand != EquipmentSlot.HAND || (FC.getBoolean(
                cn + CONFIG.KNOCKING_REQUIRES_SHIFT
            ) && !p.isSneaking)
            || (FC.getBoolean(cn + CONFIG.KNOCKING_REQUIRES_EMPTY_HAND)
                    && p.inventory.itemInMainHand.type != Material.AIR)
            || e.clickedBlock == null
        ) return

        val block = e.clickedBlock
        val blockData = block!!.blockData

        if ((blockData is Door && FC.getBoolean(cn + CONFIG.ALLOW_KNOCKING))
            || (blockData is TrapDoor && FC.getBoolean(cn + CONFIG.ALLOW_KNOCKING_TRAPDOORS))
            || (blockData is Gate && FC.getBoolean(cn + CONFIG.ALLOW_KNOCKING_GATES))
        ) {
            playKnockSound(block)
        }
    }

    private fun playKnockSound(block: Block) {
        val loc = block.location
        val world = block.world
        val sound = Optional
            .ofNullable<Sound>(
                Registry.SOUNDS
                    .get(
                        NamespacedKey.minecraft(
                            Objects.requireNonNull<String?>(FC.getString(cn + CONFIG.SOUND_KNOCK_WOOD))
                                .lowercase(Locale.getDefault())
                        )
                    )
            )
            .orElse(Sound.ITEM_SHIELD_BLOCK)
        val category = Enums
            .getIfPresent(
                SoundCategory::class.java,
                Objects.requireNonNull<String?>(FC.getString(cn + CONFIG.SOUND_KNOCK_CATEGORY))
                    .uppercase(Locale.getDefault())
            )
            .or(SoundCategory.BLOCKS)
        val volume = FC.getInt(cn + CONFIG.SOUND_KNOCK_VOLUME).toFloat()
        val pitch = FC.getInt(cn + CONFIG.SOUND_KNOCK_PITCH).toFloat()

        world.playSound(loc, sound, category, volume, pitch)
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
        }.runTaskLater(VP, 1L)
    }

    override fun enabled(): Boolean {
        return FC.getBoolean(cn + ModuleInterface.CONFIG.ENABLE)
    }

    private fun getBottomDoor(door: Door, block: Block): Door? {
        val below = if (door.half == Bisected.Half.BOTTOM) block else block.getRelative(BlockFace.DOWN)
        if (below.type == block.type && below.blockData is Door) {
            return below.blockData as Door
        }
        return null
    }

    private fun getOtherPart(door: Door?, block: Block): Block? {
        if (door != null) {
            for (neighbour in POSSIBLE_NEIGHBOURS) {
                val relative = block.getRelative(neighbour!!.offsetX, 0, neighbour.offsetZ)
                val otherDoor = if (relative.blockData is Door) relative.blockData as Door else null
                if (otherDoor != null && neighbour.facing == door.facing && neighbour.hinge == door.hinge && relative.type == block.type && otherDoor.hinge != neighbour.hinge && otherDoor.isOpen == door.isOpen && otherDoor.facing == neighbour.facing) {
                    return relative
                }
            }
        }
        return null
    }

    override fun config() {
        FC.addDefault(cn + ModuleInterface.CONFIG.ENABLE, true)
        FC.addDefault(cn + CONFIG.SOUND_KNOCK_CATEGORY, "BLOCKS")
        FC.addDefault(cn + CONFIG.SOUND_KNOCK_PITCH, 1.0)
        FC.addDefault(cn + CONFIG.SOUND_KNOCK_VOLUME, 1.0)
        FC.addDefault(cn + CONFIG.SOUND_KNOCK_WOOD, "entity_zombie_attack_wooden_door")
        FC.addDefault(cn + CONFIG.ALLOW_AUTOCLOSE, true)
        FC.addDefault(cn + CONFIG.ALLOW_DOUBLEDOORS, true)
        FC.addDefault(cn + CONFIG.ALLOW_KNOCKING, true)
        FC.addDefault(cn + CONFIG.ALLOW_KNOCKING_GATES, true)
        FC.addDefault(cn + CONFIG.ALLOW_KNOCKING_TRAPDOORS, true)
        FC.addDefault(cn + CONFIG.KNOCKING_REQUIRES_EMPTY_HAND, true)
        FC.addDefault(cn + CONFIG.KNOCKING_REQUIRES_SHIFT, false)
        FC.addDefault(cn + CONFIG.AUTOCLOSE_DELAY, 6)
        VP.saveConfig()
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
            val USE: String = VP.javaClass.getSimpleName() + ".doubledoors"
            val KNOCK: String = VP.javaClass.getSimpleName() + ".knock"
            val AUTOCLOSE: String = VP.javaClass.getSimpleName() + ".autoclose"
        }
    }

    companion object {
        private val VP = instance
        private val FC: FileConfiguration = instance.config
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
