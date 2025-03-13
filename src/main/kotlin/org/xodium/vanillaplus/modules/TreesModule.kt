/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.mask.BlockTypeMask
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.session.ClipboardHolder
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.world.StructureGrowEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.registries.BlockTypesRegistry
import java.net.JarURLConnection


class TreesModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigData.TreesModule().enabled

    private val schematicCache: Map<Material, List<Clipboard>> =
        ConfigData.TreesModule().saplingLink.mapValues { (_, dirs) ->
            dirs.flatMap { dir -> loadSchematics("/schematics/$dir") }
        }

    private fun loadSchematics(resourceDir: String): List<Clipboard> {
        return ((javaClass.getResource(resourceDir)
            ?: error("Resource directory not found: $resourceDir")).openConnection() as JarURLConnection).jarFile.use { jarFile ->
            jarFile.entries().asSequence()
                .filter { !it.isDirectory && it.name.endsWith(".schem") }
                .map { entry ->
                    jarFile.getInputStream(entry).use { inputStream ->
                        (ClipboardFormats.findByAlias("schem")?.getReader(inputStream)
                            ?: error("Unsupported schematic format for resource entry: ${entry.name}")).read()
                    }
                }.toList()
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: StructureGrowEvent) {
        event.location.block.takeIf { Tag.SAPLINGS.isTagged(it.type) }?.let {
            event.isCancelled = pasteSchematic(it)
        }
    }

    private fun pasteSchematic(block: Block): Boolean {
        val clipboard = schematicCache[block.type]?.randomOrNull()
            ?: error("No custom schematic found for ${block.type}.")
        instance.server.scheduler.runTask(instance, Runnable {
            try {
                WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(block.world))
                    .use { editSession ->
                        block.type = Material.AIR
                        editSession.mask = BlockTypeMask(editSession, BlockTypesRegistry.TREE_MASK)
                        Operations.complete(
                            ClipboardHolder(clipboard)
                                .createPaste(editSession)
                                .to(BlockVector3.at(block.x, block.y, block.z))
                                .ignoreAirBlocks(ConfigData.TreesModule().ignoreAirBlocks)
                                .build()
                        )
                    }
            } catch (ex: Exception) {
                instance.logger.severe("Error while pasting schematic: ${ex.message}")
            }
        })
        return true
    }
}
