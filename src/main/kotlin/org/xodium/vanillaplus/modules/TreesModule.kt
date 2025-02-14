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
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths


class TreesModule : ModuleInterface {
    override fun enabled(): Boolean = Config.TreesModule.ENABLED

    private val schematicCache: Map<Material, List<Clipboard>> =
        Config.TreesModule.SAPLING_LINK.mapValues { (_, paths) -> paths.map { loadSchematic("/schematics/$it") } }

    private fun loadSchematic(resourcePath: String): Clipboard =
        javaClass.getResource(resourcePath)?.toURI()?.let { uri ->
            Files.newInputStream(
                if (uri.scheme == "jar")
                    FileSystems.newFileSystem(uri, emptyMap<String, Any>()).getPath(resourcePath)
                else
                    Paths.get(uri)
            ).use { inputStream ->
                ClipboardFormats.findByAlias("schem")
                    ?.getReader(inputStream)
                    ?.read() ?: error("Unsupported schematic format for resource: $resourcePath")
            }
        } ?: error("Resource not found: $resourcePath")

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: StructureGrowEvent) {
        event.location.block.takeIf { MaterialRegistry.SAPLINGS.contains(it.type) }?.let {
            event.isCancelled = replaceWithSchematicTree(it)
        }
    }

    private fun replaceWithSchematicTree(block: Block): Boolean {
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
