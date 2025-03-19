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
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.registries.BlockTypesRegistry
import java.io.IOException
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.stream.Collectors


class TreesModule : ModuleInterface {
    override fun enabled(): Boolean = Config.TreesModule.ENABLED

    private val schematicCache: Map<Material, List<Clipboard>> =
        Config.TreesModule.SAPLING_LINK.mapValues { (_, dirs) ->
            dirs.flatMap { dir -> loadSchematics("/schematics/$dir") }
        }

    private fun loadSchematics(resourceDir: String): List<Clipboard> {
        val url = javaClass.getResource(resourceDir) ?: error("Resource directory not found: $resourceDir")
        return try {
            FileSystems.newFileSystem(url.toURI(), mapOf("create" to false)).use { fs ->
                val dirPath = fs.getPath(resourceDir.removePrefix("/"))
                Files.walk(dirPath, 1)
                    .filter { Files.isRegularFile(it) }
                    .collect(Collectors.toList())
                    .also { if (it.isEmpty()) error("No schematics found in directory: $resourceDir") }
                    .map { path ->
                        Files.newByteChannel(path, StandardOpenOption.READ).use { channel ->
                            readClipboard(path, channel)
                        }
                    }
            }
        } catch (e: IOException) {
            error("Failed to load schematics from $resourceDir: ${e.message}")
        }
    }

    private fun readClipboard(path: Path, channel: ReadableByteChannel): Clipboard {
        val format = ClipboardFormats.findByAlias("schem") ?: error("Unsupported schematic format for resource: $path")
        return try {
            format.getReader(Channels.newInputStream(channel)).read()
        } catch (e: Exception) {
            throw IOException("Failed to read schematic $path: ${e.message}", e)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: StructureGrowEvent) {
        event.location.block.takeIf {
            Tag.SAPLINGS.isTagged(it.type)
                    || it.type == Material.WARPED_FUNGUS
                    || it.type == Material.CRIMSON_FUNGUS
        }?.let { event.isCancelled = pasteSchematic(it) }
    }

    private fun pasteSchematic(block: Block): Boolean {
        val clipboards = schematicCache[block.type] ?: return false
        val clipboard = clipboards.random()
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
                                .ignoreAirBlocks(Config.TreesModule.IGNORE_AIR_BLOCKS)
                                .ignoreStructureVoidBlocks(Config.TreesModule.IGNORE_STRUCTURE_VOID_BLOCKS)
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