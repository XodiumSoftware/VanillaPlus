/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.world.StructureGrowEvent
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.registries.MaterialRegistry
import kotlin.math.ceil
import kotlin.random.Random


class TreesModule : ModuleInterface {
    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: StructureGrowEvent) {
        event.location.block.takeIf { MaterialRegistry.SAPLINGS.contains(it.type) }?.let {
            event.isCancelled = createCustomTree(it)
        }
    }

    private fun getTreeComponents(sapling: Material): Pair<Material, Material>? {
        return when (sapling) {
            Material.ACACIA_SAPLING -> Pair(Material.ACACIA_WOOD, Material.ACACIA_LEAVES)
            Material.BIRCH_SAPLING -> Pair(Material.BIRCH_WOOD, Material.BIRCH_LEAVES)
            Material.CHERRY_SAPLING -> Pair(Material.CHERRY_WOOD, Material.CHERRY_LEAVES)
            Material.DARK_OAK_SAPLING -> Pair(Material.DARK_OAK_WOOD, Material.DARK_OAK_LEAVES)
            Material.JUNGLE_SAPLING -> Pair(Material.JUNGLE_WOOD, Material.JUNGLE_LEAVES)
            Material.OAK_SAPLING -> Pair(Material.OAK_WOOD, Material.OAK_LEAVES)
            Material.PALE_OAK_SAPLING -> Pair(Material.PALE_OAK_WOOD, Material.PALE_OAK_LEAVES)
            Material.SPRUCE_SAPLING -> Pair(Material.SPRUCE_WOOD, Material.SPRUCE_LEAVES)
            else -> null
        }
    }


    private fun createCustomTree(block: Block): Boolean {
        val (trunkMaterial, leavesMaterial) = getTreeComponents(block.type) ?: return false
        val world = block.world
        val (x, y, z) = Triple(block.x, block.y, block.z)
        val trunkHeight = Random.nextInt(4, 12)
        val trunkRadius1 = Random.nextInt(2, 5)
        val trunkRadius2 = if (trunkRadius1 > 1) Random.nextInt(1, trunkRadius1) else 1
        val leavesRadius = Random.nextInt(1, 6)
        block.type = Material.AIR
        generateTrunk(world, x, y, z, trunkHeight, trunkMaterial, trunkRadius1, trunkRadius2)
        generateLeaves(world, x, y, z, trunkHeight, leavesMaterial, leavesRadius)
        return true
    }

    private fun generateTrunk(world: World, x: Int, y: Int, z: Int, height: Int, mat: Material, r1: Int, r2: Int) {
        for (layer in 0 until height) {
            val fraction = layer.toDouble() / (if (height > 1) height - 1 else 1)
            val radiusDouble = r1 - (r1 - r2) * fraction
            val intRadius = ceil(radiusDouble).toInt()
            for (xOffset in -intRadius..intRadius) {
                for (zOffset in -intRadius..intRadius) {
                    if (xOffset * xOffset + zOffset * zOffset <= radiusDouble * radiusDouble) {
                        world.getBlockAt(x + xOffset, y + layer, z + zOffset).apply {
                            if (MaterialRegistry.TREE_MASK.contains(type)) type = mat
                        }
                    }
                }
            }
        }
    }

    private fun generateLeaves(world: World, x: Int, y: Int, z: Int, height: Int, mat: Material, r: Int) {
        for (xOffset in -r..r) {
            for (yOffset in -r..r) {
                for (zOffset in -r..r) {
                    if (xOffset * xOffset + yOffset * yOffset + zOffset * zOffset <= r * r) {
                        world.getBlockAt(x + xOffset, y + height - 1 + yOffset, z + zOffset).apply {
                            if (MaterialRegistry.TREE_MASK.contains(type)) type = mat
                        }
                    }
                }
            }
        }
    }

    override fun enabled(): Boolean = Config.TreesModule.ENABLED
}
