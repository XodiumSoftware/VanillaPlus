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
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.registries.MaterialRegistry
import java.io.IOException
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.stream.Collectors

/** Represents a module handling tree mechanics within the system. */
class TreesModule : ModuleInterface<TreesModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean {
        if (!config.enabled) return false

        val worldEdit = instance.server.pluginManager.getPlugin("WorldEdit") != null
        if (!worldEdit) instance.logger.warning("WorldEdit not found, disabling TreesModule")

        return worldEdit
    }

    /** A map of sapling materials to a list of schematics. */
    private val schematicCache: Map<Material, List<Clipboard>> by lazy {
        config.saplingLink.mapValues { (_, dirs) ->
            dirs.flatMap { dir -> loadSchematics("/schematics/$dir") }
        }
    }

    /**
     * Handle the StructureGrowEvent.
     * @param event The StructureGrowEvent.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
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

    /**
     * Read a schematic from the specified path.
     * @param path The path to the schematic file.
     * @param channel The channel to read the schematic from.
     * @return The loaded schematic.
     */
    private fun readClipboard(path: Path, channel: ReadableByteChannel): Clipboard {
        val format = ClipboardFormats.findByAlias("schem") ?: error("Unsupported schematic format for resource: $path")
        return try {
            format.getReader(Channels.newInputStream(channel)).read()
        } catch (e: Exception) {
            throw IOException("Failed to read schematic $path: ${e.message}", e)
        }
    }

    /**
     * Paste a schematic at the specified block.
     * @param block The block to paste the schematic at.
     * @return True if the schematic was pasted successfully.
     */
    private fun pasteSchematic(block: Block): Boolean {
        val clipboards = schematicCache[block.type] ?: return false
        val clipboard = clipboards.random()
        instance.server.scheduler.runTask(
            instance,
            Runnable {
                try {
                    WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(block.world))
                        .use { editSession ->
                            block.type = Material.AIR
                            editSession.mask = BlockTypeMask(
                                editSession,
                                MaterialRegistry.TREE_MASK.map { BukkitAdapter.asBlockType(it) })
                            Operations.complete(
                                ClipboardHolder(clipboard)
                                    .createPaste(editSession)
                                    .to(BlockVector3.at(block.x, block.y, block.z))
                                    .copyBiomes(config.copyBiomes)
                                    .copyEntities(config.copyEntities)
                                    .ignoreAirBlocks(config.ignoreAirBlocks)
                                    .ignoreStructureVoidBlocks(config.ignoreStructureVoidBlocks)
                                    .build()
                            )
                        }
                } catch (ex: Exception) {
                    instance.logger.severe("Error while pasting schematic: ${ex.message}")
                }
            })
        return true
    }

    data class Config(
        override var enabled: Boolean = true,
        var copyBiomes: Boolean = false,
        var copyEntities: Boolean = false,
        var ignoreAirBlocks: Boolean = true,
        var ignoreStructureVoidBlocks: Boolean = true,
        var saplingLink: Map<Material, List<String>> = mapOf(
            Material.ACACIA_SAPLING to listOf("trees/acacia"),
            Material.BIRCH_SAPLING to listOf("trees/birch"),
            Material.CHERRY_SAPLING to listOf("trees/cherry"),
            Material.CRIMSON_FUNGUS to listOf("trees/crimson"),
            Material.DARK_OAK_SAPLING to listOf("trees/dark_oak"),
            Material.JUNGLE_SAPLING to listOf("trees/jungle"),
            Material.MANGROVE_PROPAGULE to listOf("trees/mangrove"),
            Material.OAK_SAPLING to listOf("trees/oak"),
            Material.PALE_OAK_SAPLING to listOf("trees/pale_oak"),
            Material.SPRUCE_SAPLING to listOf("trees/spruce"),
            Material.WARPED_FUNGUS to listOf("trees/warped"),
        ),
    ) : ModuleInterface.Config
}