package org.xodium.vanillaplus.modules

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.math.BlockVector3
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.type.Sapling
import org.bukkit.event.EventHandler
import org.bukkit.event.world.StructureGrowEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.io.File

class SaplingModule : ModuleInterface {
    override val cn: String = javaClass.simpleName
    private val schematicsPath = File(instance.dataFolder, "schematics")
    private val saplings: Set<Material> = Material.entries.filter { it.createBlockData() is Sapling }.toSet()
    private val saplingSchematicMap: Map<Material, List<File>>

    init {
        schematicsPath.mkdirs()
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

    @EventHandler
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

    private fun parseSchematicFiles(v: Any): List<File> =
        (v as? List<*>)?.mapNotNull { it?.toString()?.let(::File) } ?: emptyList()

    override fun enabled(): Boolean {
        return instance.config.getBoolean("$cn.enable")
    }
}
