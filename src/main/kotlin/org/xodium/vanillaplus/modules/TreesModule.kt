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
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.world.StructureGrowEvent
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.registries.BlockTypesRegistry
import org.xodium.vanillaplus.registries.MaterialRegistry
import java.net.JarURLConnection
import java.nio.file.Files
import java.nio.file.Paths


class TreesModule : ModuleInterface {
    override fun enabled(): Boolean = Config.TreesModule.ENABLED

    private val schematicCache: Map<Material, List<Clipboard>> =
        Config.TreesModule.SAPLING_LINK.mapValues { (_, dirs) ->
            dirs.flatMap { dir -> loadSchematics("/schematics/$dir") }
        }

    private fun loadSchematics(resourceDir: String): List<Clipboard> {
        val url = javaClass.getResource(resourceDir)
            ?: error("Resource directory not found: $resourceDir")
        val schematics = mutableListOf<Clipboard>()
        if (url.protocol == "jar") {
            (url.openConnection() as JarURLConnection).jarFile.use { jarFile ->
                jarFile.entries().asSequence().filter { entry ->
                    !entry.isDirectory && entry.name.startsWith(resourceDir.removePrefix("/"))
                }.forEach { entry ->
                    jarFile.getInputStream(entry).use { inputStream ->
                        schematics.add(
                            (ClipboardFormats.findByAlias("schem")?.getReader(inputStream)
                                ?: error("Unsupported schematic format for resource entry: ${entry.name}")).read()
                        )
                    }
                }
            }
        } else {
            Files.list(Paths.get(url.toURI())).use { paths ->
                paths.filter { Files.isRegularFile(it) }.forEach { path ->
                    Files.newInputStream(path).use { inputStream ->
                        schematics.add(
                            (ClipboardFormats.findByAlias("schem")?.getReader(inputStream)
                                ?: error("Unsupported schematic format for file: $path")).read()
                        )
                    }
                }
            }
        }
        if (schematics.isEmpty())
            error("No schematics found in directory: $resourceDir")
        return schematics
    }


    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: StructureGrowEvent) {
        event.location.block.takeIf { MaterialRegistry.SAPLINGS.contains(it.type) }?.let {
            event.isCancelled = pasteSchematic(it)
        }
    }

    private fun pasteSchematic(block: Block): Boolean {
        val clipboard = schematicCache[block.type]?.randomOrNull()
        if (clipboard == null) {
            instance.logger.info("No custom schematic found for ${block.type}.")
            return false
        }
        Bukkit.getScheduler().runTask(instance, Runnable {
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
