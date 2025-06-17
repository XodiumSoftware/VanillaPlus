/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.world.StructureGrowEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.ConfigManager
import org.xodium.vanillaplus.utils.SchematicUtils
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files

/** Represents a module handling tree mechanics within the system. */
class TreesModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigManager.data.treesModule.enabled

    /** A map of sapling materials to a list of schematics. */
    private val schematicCache: Map<Material, List<List<Triple<Int, Int, Int>>>> by lazy {
        ConfigManager.data.treesModule.saplingLink.mapValues { (_, dirs) ->
            dirs.flatMap { dir -> loadSchematics("/schematics/$dir") }
        }
    }

    /**
     * Handle the StructureGrowEvent.
     * @param event The StructureGrowEvent.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: StructureGrowEvent) {
        if (!enabled()) return
        event.location.block.takeIf {
            Tag.SAPLINGS.isTagged(it.type)
                    || it.type == Material.WARPED_FUNGUS
                    || it.type == Material.CRIMSON_FUNGUS
        }?.let { event.isCancelled = pasteSchematic(it) }
    }

    /**
     * Load schematics from the specified resource directory.
     * @param resourceDir The directory containing the schematics.
     * @return A list of loaded schematics.
     */
    private fun loadSchematics(resourceDir: String): List<List<Triple<Int, Int, Int>>> {
        val url = javaClass.getResource(resourceDir) ?: error("Resource directory not found: $resourceDir")
        return try {
            FileSystems.newFileSystem(url.toURI(), mapOf("create" to false)).use { fs ->
                val dirPath = fs.getPath(resourceDir.removePrefix("/"))
                Files.walk(dirPath, 1)
                    .filter { Files.isRegularFile(it) }
                    .map { path -> Files.newInputStream(path).use { SchematicUtils.loadSimpleSchematic(it) } }
                    .toList()
            }
        } catch (e: IOException) {
            error("Failed to load schematics from $resourceDir: ${e.message}")
        }
    }

    /**
     * Paste a schematic at the specified block.
     * @param block The block to paste the schematic at.
     * @return True if the schematic was pasted successfully.
     */
    private fun pasteSchematic(block: Block): Boolean {
        val schematics = schematicCache[block.type] ?: return false
        val schematic = schematics.random()

        instance.server.scheduler.runTask(
            instance,
            Runnable {
                block.type = Material.AIR
                SchematicUtils.pasteSimpleSchematic(block, schematic)
            })
        return true
    }
}