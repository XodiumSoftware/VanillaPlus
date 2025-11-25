package org.xodium.vanillaplus.features

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.mask.BlockTypeMask
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.math.transform.AffineTransform
import com.sk89q.worldedit.session.ClipboardHolder
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.world.StructureGrowEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.hooks.WorldEditHook
import org.xodium.vanillaplus.interfaces.FeatureInterface
import org.xodium.vanillaplus.registries.MaterialRegistry
import java.io.IOException
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.stream.Collectors

/** Represents a feature handling tree mechanics within the system. */
internal object TreesFeature : FeatureInterface {
    private val config: Config = Config()

    private val schematicCache: Map<Material, List<Clipboard>> by lazy {
        MaterialRegistry.SAPLING_LINKS.mapValues { loadSchematics("/schematics/${it.value}") }
    }

    /**
     * Handle the StructureGrowEvent.
     * @param event The StructureGrowEvent.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: StructureGrowEvent) {
        if (!WorldEditHook.get()) {
            instance.logger.warning("WorldEdit not found, TreesFeature will not function properly.")
            return
        }
        event.location.block
            .takeIf {
                Tag.SAPLINGS.isTagged(it.type) ||
                    it.type == Material.WARPED_FUNGUS ||
                    it.type == Material.CRIMSON_FUNGUS
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
                Files
                    .walk(fs.getPath(resourceDir.removePrefix("/")), 1)
                    .filter { Files.isRegularFile(it) }
                    .collect(Collectors.toList())
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
    private fun readClipboard(
        path: Path,
        channel: ReadableByteChannel,
    ): Clipboard {
        val format = ClipboardFormats.findByAlias("schem") ?: error("Unsupported schematic format for resource: $path")
        return try {
            format.getReader(Channels.newInputStream(channel)).read()
        } catch (e: Exception) {
            throw IOException("Failed to read schematic $path: ${e.message}", e)
        }
    }

    /**
     * Paste a random schematic at the specified block.
     * @param block The block to paste the schematic at.
     * @return True if the schematic was pasted successfully.
     */
    private fun pasteSchematic(block: Block): Boolean {
        val clipboards = schematicCache[block.type] ?: return false
        return pasteSchematic(block, clipboards.random())
    }

    /**
     * Paste a schematic at the specified block.
     * @param block The block to paste the schematic at.
     * @param clipboard The specific clipboard to paste.
     * @return True if the schematic was pasted successfully.
     */
    private fun pasteSchematic(
        block: Block,
        clipboard: Clipboard,
    ): Boolean {
        instance.server.scheduler.runTask(
            instance,
            Runnable {
                try {
                    WorldEdit
                        .getInstance()
                        .newEditSession(BukkitAdapter.adapt(block.world))
                        .use { editSession ->
                            block.type = Material.AIR
                            editSession.mask =
                                BlockTypeMask(
                                    editSession,
                                    config.treeMask.map { BukkitAdapter.asBlockType(it) },
                                )
                            ClipboardHolder(clipboard).apply {
                                transform = transform.combine(AffineTransform().rotateY(getRandomRotation().toDouble()))
                                Operations.complete(
                                    createPaste(editSession)
                                        .to(BlockVector3.at(block.x, block.y, block.z))
                                        .copyBiomes(config.copyBiomes)
                                        .copyEntities(config.copyEntities)
                                        .ignoreAirBlocks(config.ignoreAirBlocks)
                                        .ignoreStructureVoidBlocks(config.ignoreStructureVoidBlocks)
                                        .build(),
                                )
                            }
                        }
                } catch (e: Exception) {
                    instance.logger.severe("Error while pasting schematic: ${e.message}")
                }
            },
        )
        return true
    }

    /**
     * Returns a random rotation angle from a given list of angles.
     * @param angle The list of angles to choose from. Defaults to [0, 90, 180, 270].
     * @return A random angle from the provided or default list.
     * @throws IllegalArgumentException if any angle is not a multiple of 90 or outside [0, 270)
     */
    private fun getRandomRotation(angle: List<Int> = listOf(0, 90, 180, 270)): Int {
        require(angle.all { it in setOf(0, 90, 180, 270) }) { "Angles must be one of: 0, 90, 180, 270" }
        return angle.random()
    }

    data class Config(
        var copyBiomes: Boolean = false,
        var copyEntities: Boolean = false,
        var ignoreAirBlocks: Boolean = true,
        var ignoreStructureVoidBlocks: Boolean = true,
        var treeMask: Set<Material> =
            setOf(
                Material.AZALEA,
                Material.WEEPING_VINES,
                Material.CORNFLOWER,
                Material.CLOSED_EYEBLOSSOM,
                Material.PINK_TULIP,
                Material.OPEN_EYEBLOSSOM,
                Material.WHITE_TULIP,
                Material.SNOW,
                Material.FERN,
                Material.AZALEA_LEAVES,
                Material.SUNFLOWER,
                Material.PEONY,
                Material.PINK_PETALS,
                Material.LILAC,
                Material.LARGE_FERN,
                Material.VINE,
                Material.CAVE_VINES_PLANT,
                Material.TORCHFLOWER,
                Material.RED_TULIP,
                Material.ORANGE_TULIP,
                Material.KELP,
                Material.AIR,
                Material.FLOWERING_AZALEA,
                Material.AZURE_BLUET,
                Material.MOSS_BLOCK,
                Material.PITCHER_PLANT,
                Material.WEEPING_VINES_PLANT,
                Material.TALL_SEAGRASS,
                Material.TWISTING_VINES,
                Material.BLUE_ORCHID,
                Material.CAVE_VINES,
                Material.ROSE_BUSH,
                Material.SPORE_BLOSSOM,
                Material.FLOWERING_AZALEA_LEAVES,
                Material.POPPY,
                Material.TWISTING_VINES_PLANT,
                Material.DANDELION,
                Material.DEAD_BUSH,
                Material.LILY_OF_THE_VALLEY,
                Material.KELP_PLANT,
                Material.SHORT_GRASS,
                Material.CHORUS_FLOWER,
                Material.ALLIUM,
                Material.MANGROVE_PROPAGULE,
                Material.CHERRY_LEAVES,
                Material.SUGAR_CANE,
                Material.SEAGRASS,
                Material.MOSS_CARPET,
                Material.WITHER_ROSE,
                Material.TALL_GRASS,
                Material.OXEYE_DAISY,
            ),
    )
}
