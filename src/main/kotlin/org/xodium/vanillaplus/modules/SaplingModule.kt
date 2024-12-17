package org.xodium.vanillaplus.modules

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.math.BlockVector3
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.world.StructureGrowEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.io.File

class SaplingModule : ModuleInterface {
    private val cn: String = javaClass.simpleName
    private val schematicsPath = File(instance.dataFolder, "schematics")
    private val saplings: Set<Material> = Material.entries.filter { it.name.endsWith("_SAPLING") }.toSet()

    init {
        schematicsPath.mkdirs()
    }

    @EventHandler
    fun on(e: StructureGrowEvent) {
        if (saplings.contains(e.location.block.type)) e.isCancelled = true
        replaceWithSchematicTree(e.location.block)
    }

    private fun replaceWithSchematicTree(b: Block) {
        val schematicFile = getSchematicForSapling(b.type) ?: return
        val format = ClipboardFormats.findByFile(schematicFile) ?: return
        schematicFile.inputStream().use { inputStream ->
            WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(b.world)).use { editSession ->
                format.getReader(inputStream).read().paste(editSession, BlockVector3.at(b.x, b.y, b.z), true)
            }
        }
    }

    private fun getSchematicForSapling(m: Material): File? {
        return if (saplings.contains(m)) {
            File(schematicsPath, m.name.lowercase().replace("_sapling", "_tree.schematic"))
        } else {
            instance.logger.warning("No schematic found for Material: ${m.name}")
            null
        }
    }

    override fun enabled(): Boolean {
        return instance.config.getBoolean("$cn.enable")
    }
}
