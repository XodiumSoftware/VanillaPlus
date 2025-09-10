package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling cauldron mechanics within the system. */
internal class CauldronModule : ModuleInterface<CauldronModule.Config> {
    override val config: Config = Config()

    init {
        if (enabled()) {
            instance.server.scheduler.runTaskTimer(
                instance,
                Runnable {
                    for (world in instance.server.worlds) {
                        for (chunk in world.loadedChunks) {
                            for (x in 0..15) {
                                for (y in 0 until world.maxHeight) {
                                    for (z in 0..15) {
                                        val block = chunk.getBlock(x, y, z)
                                        if (block.type != Material.WATER_CAULDRON) continue

                                        val nearbyItems = block.world
                                            .getNearbyEntities(block.location.add(0.5, 0.5, 0.5), 0.5, 1.0, 0.5)
                                            .filter { it is org.bukkit.entity.Item }
                                            .map { it as org.bukkit.entity.Item }

                                        for (item in nearbyItems) {
                                            val mat = when {
                                                config.convertDirt && item.itemStack.type == Material.DIRT -> Material.MUD
                                                config.convertConcretePowder && org.bukkit.Tag.CONCRETE_POWDER.isTagged(
                                                    item.itemStack.type
                                                ) ->
                                                    Material.entries.firstOrNull {
                                                        it.name == item.itemStack.type.name.removeSuffix("_POWDER")
                                                    }

                                                else -> null
                                            }

                                            mat?.let {
                                                println("Item ${item.itemStack.type} dropped in cauldron, converting to $it at ${block.location}")
                                                item.remove()
                                                block.type = it

                                                val data = block.blockData
                                                if (data is org.bukkit.block.data.Levelled) {
                                                    data.level = (data.level - 1).coerceAtLeast(0)
                                                    block.blockData = data
                                                    println("Cauldron water level decreased to ${data.level}")
                                                    if (data.level == 0) {
                                                        block.type = Material.CAULDRON
                                                        println("Cauldron is now empty")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                1L,
                2L,
            )
        }
    }

    data class Config(
        override var enabled: Boolean = true,
        var convertConcretePowder: Boolean = true,
        var convertDirt: Boolean = true,
    ) : ModuleInterface.Config
}
