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
        when (instance.javaClass.classLoader.getResource(schematicsFolder)) {
            null -> instance.logger.warning("Default schematics directory not found in resources.")
            else -> copyResourcesFromJar(schematicsPath)
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

    private fun parseSchematicFiles(v: Any): List<File> {
        val files = mutableListOf<File>()
        when (v) {
            is List<*> -> v.mapNotNull {
                it?.toString()?.let { subDir ->
                    File(schematicsPath, subDir)
                }
            }.forEach { collectSchematicFiles(it, files) }

            is String -> collectSchematicFiles(File(schematicsPath, v), files)

            else -> instance.logger.warning("Invalid schematic value type: $v")
        }
        return files
    }

    private fun collectSchematicFiles(file: File, files: MutableList<File>) {
        if (file.isDirectory) {
            files.addAll(file.listFiles { _, name -> name.endsWith(".schem", ignoreCase = true) } ?: emptyArray())
        } else if (file.isFile && file.extension.equals("schem", ignoreCase = true)) {
            files.add(file)
        } else {
            instance.logger.warning("Invalid file or directory: ${file.absolutePath}")
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: StructureGrowEvent) {
        val block = event.location.block
        if (saplings.contains(block.type)) {
            event.isCancelled = true
            replaceWithSchematicTree(block)
        }
    }

    private fun replaceWithSchematicTree(block: Block) {
        val schematicFile = saplingSchematicMap[block.type]?.randomOrNull()
        if (schematicFile == null || !schematicFile.exists()) {
            instance.logger.info("No custom schematic found for ${block.type}.")
            return
        }
        val format = ClipboardFormats.findByFile(schematicFile)
        if (format == null) {
            instance.logger.warning("Unsupported schematic format for file: ${schematicFile.name}")
            return
        }
        Bukkit.getScheduler().runTask(instance, Runnable {
            schematicFile.inputStream().use {
                try {
                    Operations.complete(
                        ClipboardHolder(
                            format.getReader(it).read()
                        ).createPaste(WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(block.world)))
                            .to(BlockVector3.at(block.x, block.y, block.z))
                            .ignoreAirBlocks(true)
                            .build()
                    )
                } catch (ex: Exception) {
                    instance.logger.severe("Error while pasting schematic: ${ex.message}")
                }
            }
        })
    }

    override fun enabled() = instance.config.getBoolean("$cn.enable")
}
