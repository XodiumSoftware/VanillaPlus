package org.xodium.vanillaplus.modules

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.session.ClipboardHolder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.world.StructureGrowEvent
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class SaplingModule : ModuleInterface {
    private val config = instance.config
    private val logger = instance.logger
    private val schematicsPath = instance.dataFolder.toPath().resolve(SCHEMATICS_FOLDER)
    private lateinit var saplingSchematicMap: Map<Material, List<Path>>

    companion object {
        private val SCHEMATICS_FOLDER = Paths.get("schematics")
        private val SAPLINGS = setOf(
            Material.OAK_SAPLING,
            Material.BIRCH_SAPLING,
            Material.CHERRY_SAPLING,
            Material.SPRUCE_SAPLING,
            Material.JUNGLE_SAPLING,
            Material.ACACIA_SAPLING,
            Material.DARK_OAK_SAPLING,
            Material.MANGROVE_PROPAGULE
        )
    }

    override fun init() {
        Utils.copyResourcesFromJar(SCHEMATICS_FOLDER, schematicsPath)
        saplingSchematicMap = loadSaplingSchematicMap()
    }

    private fun loadSaplingSchematicMap(): Map<Material, List<Path>> {
        return config.getConfigurationSection("$cn.sapling_link")
            ?.let { saplingConfig ->
                saplingConfig.getKeys(false)
                    .mapNotNull { validateAndMapSapling(it, saplingConfig[it]) }
                    .toMap()
            } ?: emptyMap()
    }

    private fun validateAndMapSapling(key: String, value: Any?): Pair<Material, List<Path>>? {
        val material = Material.matchMaterial(key)
        if (material == null || !SAPLINGS.contains(material)) {
            logger.warning("Invalid sapling configuration entry: $key does not map to a valid sapling.")
            return null
        }
        val files = Utils.parseFiles(value ?: emptyList<String>(), schematicsPath, ".schem")
        if (files.isEmpty()) {
            logger.warning("No valid schematics found for $key.")
            return null
        }
        return material to files
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: StructureGrowEvent) {
        event.location.block.takeIf { SAPLINGS.contains(it.type) }?.let {
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
                    if (!canPlaceTree(
                            block.world,
                            block.x,
                            block.y,
                            block.z,
                            clipboard.dimensions.x(),
                            clipboard.dimensions.y(),
                            clipboard.dimensions.z()
                        )
                    ) return@Runnable
                    try {
                        WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(block.world))
                            .use {
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

    private fun canPlaceTree(
        world: World,
        startX: Int,
        startY: Int,
        startZ: Int,
        schematicX: Int,
        schematicY: Int,
        schematicZ: Int
    ): Boolean {
        if (startY < 0 || (startY + schematicY) > world.maxHeight) return false
        return sequence {
            for (xOffset in 0 until schematicX) {
                for (yOffset in 0 until schematicY) {
                    for (zOffset in 0 until schematicZ) {
                        yield(world.getBlockAt(startX + xOffset, startY + yOffset, startZ + zOffset))
                    }
                }
            }
        }.all { it.isReplaceable }
    }

    override fun enabled() = config.getBoolean("$cn.enable")
}
