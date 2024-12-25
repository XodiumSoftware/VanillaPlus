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
import java.io.File

class SaplingModule : ModuleInterface {
    override val cn: String = javaClass.simpleName
    private val config = instance.config
    private val logger = instance.logger
    private val schematicsFolder = "schematics"
    private val schematicsPath = File(instance.dataFolder, schematicsFolder)
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
    private lateinit var saplingSchematicMap: Map<Material, List<File>>

    override fun init() {
        setupDefaultSchematics()
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

    private fun setupDefaultSchematics() {
        schematicsPath.mkdirs()
        when (instance.getResource(schematicsFolder)) {
            null -> logger.warning("Default schematics directory not found in resources.")
            else -> copyResourcesFromJar(schematicsPath)
        }
    }

    private fun copyResourcesFromJar(targetDir: File) {
        val jar = File(instance.javaClass.protectionDomain.codeSource.location.toURI())
        if (!jar.exists()) {
            logger.warning("Jar file does not exist: ${jar.absolutePath}")
            return
        }
        java.util.jar.JarFile(jar).use { jarFile ->
            val entries = jarFile.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.name.startsWith(schematicsFolder) && !entry.isDirectory) {
                    val entryTarget = File(targetDir, entry.name.removePrefix("$schematicsFolder/"))
                    entryTarget.parentFile.mkdirs()
                    instance.javaClass.classLoader.getResourceAsStream(entry.name)?.use { input ->
                        entryTarget.outputStream().use { output -> input.copyTo(output) }
                    } ?: logger.warning("Failed to get input stream for ${entry.name}")
                }
            }
        }
    }

    private fun parseSchematicFiles(v: Any): List<File> {
        val files = mutableListOf<File>()
        when (v) {
            is List<*> -> v.mapNotNull {
                it?.toString()?.let { subDir ->
                    File(schematicsPath, subDir)
                }
            }.forEach { collectSchematicFiles(it, files) }

            is String -> collectSchematicFiles(File(schematicsPath, v), files)

            else -> logger.warning("Invalid schematic value type: $v")
        }
        return files
    }

    private fun collectSchematicFiles(file: File, files: MutableList<File>) {
        when {
            file.isDirectory -> files.addAll(file.listFiles { _, name -> name.endsWith(".schem", ignoreCase = true) }
                ?: emptyArray())

            file.isFile && file.extension.equals("schem", ignoreCase = true) -> files.add(file)
            else -> logger.warning("Invalid file or directory: ${file.absolutePath}")
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: StructureGrowEvent) {
        event.location.block.takeIf { saplings.contains(it.type) }?.let {
            event.isCancelled = replaceWithSchematicTree(it)
        }
    }

    private fun replaceWithSchematicTree(block: Block): Boolean {
        val schematicFile = saplingSchematicMap[block.type]?.randomOrNull()
        if (schematicFile == null || !schematicFile.exists()) {
            logger.info("No custom schematic found for ${block.type}.")
            return false
        }
        val format = ClipboardFormats.findByFile(schematicFile)
        if (format == null) {
            logger.warning("Unsupported schematic format for file: ${schematicFile.name}")
            return false
        }
        Bukkit.getScheduler().runTask(instance, Runnable {
            schematicFile.inputStream().use {
                try {
                    val clipboard = format.getReader(it).read()
                    try {
                        WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(block.world)).use {
                            Operations.complete(
                                ClipboardHolder(clipboard)
                                    .createPaste(it)
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
