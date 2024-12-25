package org.xodium.vanillaplus.modules

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.session.ClipboardHolder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.world.StructureGrowEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class SaplingModule : ModuleInterface {
    override val cn: String = javaClass.simpleName
    private val config = instance.config
    private val logger = instance.logger
    private val schematicsFolder = "schematics"
    private val schematicsPath: Path = instance.dataFolder.toPath().resolve(schematicsFolder)
    private val saplings = setOf(
        Material.OAK_SAPLING,
        Material.BIRCH_SAPLING,
        Material.CHERRY_SAPLING,
        Material.SPRUCE_SAPLING,
        Material.JUNGLE_SAPLING,
        Material.ACACIA_SAPLING,
        Material.DARK_OAK_SAPLING,
        Material.MANGROVE_PROPAGULE
    )
    private lateinit var saplingSchematicMap: Map<Material, List<Path>>

    // TODO: refactor the schematic handling mechanism till the fun on()
    override fun init() {
        Files.createDirectories(schematicsPath)
        copyResourcesFromJar(schematicsPath)
        val saplingConfig = config.getConfigurationSection("$cn.sapling_link")
        saplingSchematicMap = saplingConfig?.getKeys(false)?.mapNotNull {
            val m = Material.matchMaterial(it)
            if (m != null && saplings.contains(m)) {
                val files = parseSchematicFiles(saplingConfig[it] ?: emptyList<String>())
                if (files.isNotEmpty()) {
                    m to files
                } else {
                    logger.warning("No valid schematics found for $it.")
                    null
                }
            } else {
                logger.warning("Invalid sapling configuration entry: $it does not map to a valid sapling.")
                null
            }
        }?.toMap() ?: emptyMap()
    }

    private fun copyResourcesFromJar(targetDir: Path) {
        FileSystems.newFileSystem(
            URI.create("jar:file:${instance.javaClass.protectionDomain.codeSource.location.toURI().path}"),
            emptyMap<String, Any>()
        ).use { fileSystem ->
            val schematicsPath = fileSystem.getPath(schematicsFolder)
            Files.walk(schematicsPath).use { streamPath ->
                streamPath.filter { path ->
                    Files.isRegularFile(path) && path.toString().endsWith(".schem", ignoreCase = true)
                }.forEach { path ->
                    val targetPath = targetDir.resolve(schematicsPath.relativize(path).toString())
                    Files.createDirectories(targetPath.parent)
                    Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
    }

    private fun parseSchematicFiles(v: Any): List<Path> {
        val files = mutableListOf<Path>()
        when (v) {
            is List<*> -> v.mapNotNull {
                it?.toString()?.let { subDir ->
                    schematicsPath.resolve(subDir)
                }
            }.forEach { collectSchematicFiles(it, files) }

            is String -> collectSchematicFiles(schematicsPath.resolve(v), files)

            else -> logger.warning("Invalid schematic value type: $v")
        }
        return files
    }

    private fun collectSchematicFiles(path: Path, files: MutableList<Path>) {
        try {
            Files.walk(path).use { stream ->
                stream.filter { Files.isRegularFile(it) && it.toString().endsWith(".schem", ignoreCase = true) }
                    .forEach { files.add(it) }
            }
        } catch (ex: Exception) {
            logger.warning("Error processing path ${path.toAbsolutePath()}: ${ex.message}")
        }
    }

    // TODO: add check if there is enough space for the sapling to grow.
    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: StructureGrowEvent) {
        event.location.block.takeIf { saplings.contains(it.type) }?.let {
            event.isCancelled = replaceWithSchematicTree(it)
        }
    }

    private fun replaceWithSchematicTree(block: Block): Boolean {
        val schematicFile = saplingSchematicMap[block.type]?.randomOrNull()
        if (schematicFile == null || !Files.exists(schematicFile)) {
            logger.info("No custom schematic found for ${block.type}.")
            return false
        }
        val format = ClipboardFormats.findByFile(schematicFile.toFile())
        if (format == null) {
            logger.warning("Unsupported schematic format for file: ${schematicFile.fileName}")
            return false
        }
        Bukkit.getScheduler().runTask(instance, Runnable {
            Files.newInputStream(schematicFile).use {
                try {
                    val clipboard = format.getReader(it).read()
                    try {
                        WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(block.world)).use { editSession ->
                            Operations.complete(
                                ClipboardHolder(clipboard)
                                    .createPaste(editSession)
                                    .to(BlockVector3.at(block.x, block.y, block.z))
                                    .ignoreAirBlocks(config.getBoolean("$cn.ignore_air_blocks"))
                                    .ignoreStructureVoidBlocks(config.getBoolean("$cn.ignore_structure_void_blocks"))
                                    .copyEntities(config.getBoolean("$cn.copy_entities"))
                                    .copyBiomes(config.getBoolean("$cn.copy_biomes"))
                                    .build()
                            )
                        }
                    } catch (ex: Exception) {
                        logger.severe("Error while pasting schematic: ${ex.message}")
                    }
                } catch (ex: Exception) {
                    logger.severe("Error reading schematic file: ${ex.message}")
                }
            }
        })
        return true
    }

    override fun enabled() = config.getBoolean("$cn.enable")
}
