package org.xodium.vanillaplus.modules

import com.fastasyncworldedit.core.Fawe
import com.fastasyncworldedit.core.extent.clipboard.io.FastSchematicReaderV3
import com.fastasyncworldedit.core.math.MutableBlockVector3
import com.sk89q.worldedit.bukkit.BukkitAdapter
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.world.StructureGrowEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.io.File

/**
 * The `SaplingModule` class is responsible for handling custom behavior when saplings grow into trees
 * within a Minecraft Bukkit server. It implements the `ModuleInterface` and listens for `StructureGrowEvent`
 * to replace the default tree generation process with a schematic-defined structure.
 *
 * The module links specific sapling types to schematic files stored in a predefined directory
 * and can paste these custom structures at the location of sapling growth.
 *
 * This class supports configuration and operates based on the Bukkit's plugin data folder and configuration settings.
 *
 * Primary Features:
 * - Identifies saplings and their corresponding schematics.
 * - Intercepts natural tree growth events.
 * - Cancels default growth behavior and replaces it with schematic-based generation.
 *
 * Key Concepts:
 * - **Saplings**: A set of sapling materials defined from the Minecraft Block type system.
 * - **Schematics**: Files representing custom tree structures.
 * - **Configuration**: Specifies mappings between sapling types and their schematic file lists.
 */
class SaplingModule : ModuleInterface {
    override val cn: String = javaClass.simpleName
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
        val saplingConfig = instance.config.getConfigurationSection("$cn.sapling_link")
        saplingSchematicMap = saplingConfig?.getKeys(false)?.mapNotNull { k ->
            val m = Material.matchMaterial(k)
            if (m != null && saplings.contains(m)) {
                val files = parseSchematicFiles(saplingConfig[k] ?: emptyList<String>())
                if (files.isNotEmpty()) {
                    m to files
                } else {
                    instance.logger.warning("No valid schematics found for $k.")
                    null
                }
            } else {
                instance.logger.warning("Invalid sapling configuration entry: $k does not map to a valid sapling.")
                null
            }
        }?.toMap() ?: emptyMap()
    }

    private fun setupDefaultSchematics() {
        schematicsPath.mkdirs()
        if (instance.javaClass.classLoader.getResource(schematicsFolder) != null) {
            copyResourcesFromJar(schematicsPath)
        } else {
            instance.logger.warning("Default schematics directory not found in resources.")
        }
    }

    private fun copyResourcesFromJar(targetDir: File) {
        val jar = File(instance.javaClass.protectionDomain.codeSource.location.toURI())
        if (!jar.exists()) {
            instance.logger.warning("Jar file does not exist: ${jar.absolutePath}")
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
                    } ?: instance.logger.warning("Failed to get input stream for ${entry.name}")
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(e: StructureGrowEvent) {
        if (saplings.contains(e.location.block.type)) e.isCancelled = true
        replaceWithSchematicTree(e.location.block)
    }

    private fun replaceWithSchematicTree(block: Block) {
        val faweInstance = Fawe.instance()
        if (faweInstance == null) {
            instance.logger.warning("Fawe instance is null. Unable to replace tree with schematic.")
            return
        }
        val schematicFile = saplingSchematicMap[block.type]?.randomOrNull()
        if (schematicFile != null && schematicFile.exists()) {
            Bukkit.getScheduler().runTaskAsynchronously(instance, Runnable {
                schematicFile.inputStream().use { inputStream ->
                    // TODO: in the future replace BukkitAdapter with FaweAdapter.
                    faweInstance.worldEdit.newEditSession(BukkitAdapter.adapt(block.world)).use { editSession ->
                        try {
                            FastSchematicReaderV3(inputStream).read(block.world.uid)
                                .paste(editSession, MutableBlockVector3.at(block.x, block.y, block.z), true)
                        } catch (ex: Exception) {
                            instance.logger.warning("Error pasting schematic: ${schematicFile.name}: ${ex.message}")
                        }
                    }
                }
            })
        } else {
            instance.logger.info("No custom schematic found for ${block.type}. Using default Minecraft behavior.")
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

            is String -> {
                val resolvedDir = File(schematicsPath, v)
                collectSchematicFiles(resolvedDir, files)
            }

            else -> {
                instance.logger.warning("Invalid schematic value type: $v")
            }
        }
        return files
    }

    private fun collectSchematicFiles(file: File, files: MutableList<File>) {
        if (file.isDirectory) {
            val schematics = file.listFiles { _, name -> name.endsWith(".schem", ignoreCase = true) } ?: emptyArray()
            files.addAll(schematics)
        } else if (file.isFile && file.extension.equals("schem", ignoreCase = true)) {
            files.add(file)
        } else {
            instance.logger.warning("Invalid file or directory: ${file.absolutePath}")
        }
    }

    override fun enabled(): Boolean {
        return instance.config.getBoolean("$cn.enable")
    }
}
