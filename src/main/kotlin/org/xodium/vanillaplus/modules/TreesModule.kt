/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.world.StructureGrowEvent
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.registries.MaterialRegistry


class TreesModule : ModuleInterface {
    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: StructureGrowEvent) {
        event.location.block.takeIf { MaterialRegistry.SAPLINGS.contains(it.type) }?.let {
            event.isCancelled = createCustomTree(it)
        }
    }

    private fun createCustomTree(saplingBlock: Block): Boolean {
        val trunkHeight = 5
        val leavesRadius = 2

        saplingBlock.type = Material.AIR

        for (i in 0..<trunkHeight) {
            val trunkBlock: Block = saplingBlock.world.getBlockAt(
                saplingBlock.x,
                saplingBlock.y + i,
                saplingBlock.z
            )
            trunkBlock.type = Material.OAK_LOG
        }

        for (x in -leavesRadius..leavesRadius) {
            for (y in -leavesRadius..leavesRadius) {
                for (z in -leavesRadius..leavesRadius) {
                    if (x * x + y * y + z * z <= leavesRadius * leavesRadius) {
                        val leavesBlock: Block = saplingBlock.world.getBlockAt(
                            saplingBlock.x + x,
                            saplingBlock.y + trunkHeight - 1 + y,
                            saplingBlock.z + z
                        )
                        if (MaterialRegistry.TREE_MASK.contains(leavesBlock.type)) {
                            leavesBlock.type = Material.OAK_LEAVES
                        }
                    }
                }
            }
        }
        return true
    }

    override fun enabled(): Boolean = Config.TreesModule.ENABLED
}
