package org.xodium.vanillaplus.modules

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.math.BlockVector3
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.type.Sapling
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
// TODO: fix not recognizing the schematics.
class SaplingModule : ModuleInterface {
    override val cn: String = javaClass.simpleName
    private val schematicsPath = File(instance.dataFolder, "schematics")
    private val saplings =
        (Material.entries.filter { runCatching { it.isBlock && it.createBlockData() is Sapling }.getOrDefault(false) }
            .toSet() + Material.MANGROVE_PROPAGULE)
    private val saplingSchematicMap: Map<Material, List<File>>

    init {
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
        if (instance.javaClass.classLoader.getResource("schematics") != null) {
            copyResourcesFromJar(schematicsPath)
        } else {
            instance.logger.warning("Default schematics directory not found in resources.")
        }
    }

    private fun copyResourcesFromJar(targetDir: File) {
        val resourcePath = "schematics"
        val jar = File(instance.javaClass.protectionDomain.codeSource.location.toURI())
        if (!jar.exists()) return
        java.util.jar.JarFile(jar).use { jarFile ->
            val entries = jarFile.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.name.startsWith(resourcePath) && !entry.isDirectory) {
                    val entryTarget = File(targetDir, entry.name.removePrefix("$resourcePath/"))
                    entryTarget.parentFile.mkdirs()
                    instance.javaClass.classLoader.getResourceAsStream(entry.name)?.use { input ->
                        entryTarget.outputStream().use { output -> input.copyTo(output) }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(e: StructureGrowEvent) {
        if (saplings.contains(e.location.block.type)) e.isCancelled = true
        replaceWithSchematicTree(e.location.block)
    }

    private fun replaceWithSchematicTree(b: Block) {
        val schematicFile = saplingSchematicMap[b.type]?.randomOrNull()
        if (schematicFile != null && schematicFile.exists()) {
            val format = ClipboardFormats.findByFile(schematicFile)
            if (format != null) {
                schematicFile.inputStream().use { inputStream ->
                    WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(b.world)).use { editSession ->
                        format.getReader(inputStream).read().paste(editSession, BlockVector3.at(b.x, b.y, b.z), true)
                    }
                }
            } else {
                instance.logger.warning("Unsupported schematic format for file: ${schematicFile.name}")
            }
        } else {
            instance.logger.info("No custom schematic found for ${b.type}. Using default Minecraft behavior.")
        }
    }

    private fun parseSchematicFiles(v: Any): List<File> {
        val files = mutableListOf<File>()
        when (v) {
            is List<*> -> v.mapNotNull { it?.toString()?.let(::File) }.forEach { collectSchematicFiles(it, files) }
            is String -> collectSchematicFiles(File(v), files)
        }
        return files
    }

    private fun collectSchematicFiles(file: File, files: MutableList<File>) {
        if (file.isDirectory) {
            files.addAll(file.listFiles { _, name -> name.endsWith(".schem", ignoreCase = true) }
                ?: emptyArray())

        } else if (file.isFile && file.extension.equals("schem", ignoreCase = true)) {
            files.add(file)
        }
    }

    override fun enabled(): Boolean {
        return instance.config.getBoolean("$cn.enable")
    }
}
