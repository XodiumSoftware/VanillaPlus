package org.xodium.vanillaplus.utils

import io.papermc.paper.event.block.BlockFailedDispenseEvent
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Dispenser
import org.bukkit.block.Jukebox
import org.bukkit.block.data.Directional
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/** Jukebox utilities. */
internal object JukeboxUtils {
    private val config = Config()

    /**
     * Inserts a record into a jukebox using a dispenser.
     * @param event The [BlockDispenseEvent].
     */
    fun insert(event: BlockDispenseEvent) {
        if (!config.enableJukeboxInsertion) return

        val dispenser = event.block.state as? Dispenser ?: return
        val targetBlock = dispenser.targetBlock() ?: return

        if (targetBlock.type != Material.JUKEBOX) return

        val jukebox = targetBlock.state as? Jukebox ?: return

        if (jukebox.record.type != Material.AIR) return

        jukebox.setRecord(event.item)
        jukebox.update()

        event.isCancelled = true

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable { dispenser.inventory.removeItem(event.item) },
            1L,
        )
    }

    // NOTE: there is no event that detects if it can extract since a dispenser only inserts?

    /**
     * Extracts a record from a jukebox using a dispenser.
     * @param event The [BlockFailedDispenseEvent].
     */
    fun extract(event: BlockFailedDispenseEvent) {
        if (!config.enableJukeboxExtraction) return

        val dispenser = event.block.state as? Dispenser ?: return
        val targetBlock = dispenser.targetBlock() ?: return

        if (targetBlock.type != Material.JUKEBOX) return

        val jukebox = targetBlock.state as? Jukebox ?: return

        if (jukebox.record.type == Material.AIR) return

        val record = jukebox.record

        jukebox.setRecord(ItemStack.of(Material.AIR))
        jukebox.update()
        // jukebox.stopPlaying()

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                val remaining = dispenser.inventory.addItem(record)

                if (!remaining.isEmpty()) targetBlock.world.dropItem(targetBlock.location, record)
            },
            1L,
        )
    }

    /**
     * Gets the block in front of the dispenser.
     * @receiver The [Dispenser].
     * @return The target [Block], or null if it cannot be determined.
     */
    private fun Dispenser.targetBlock(): Block? = (blockData as? Directional)?.facing?.let { block.getRelative(it) }

    data class Config(
        var enableJukeboxInsertion: Boolean = true,
        var enableJukeboxExtraction: Boolean = true,
    )
}
