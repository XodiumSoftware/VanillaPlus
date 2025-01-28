/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.mask.BlockTypeMask
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.world.block.BlockTypes
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.world.StructureGrowEvent
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class SaplingModule : ModuleInterface {
    private val logger = instance.logger
    private val schematicsPath = instance.dataFolder.toPath().resolve(SCHEMATICS_FOLDER)
    private lateinit var saplingSchematicMap: Map<Material, List<Path>>

    companion object {
        private val SCHEMATICS_FOLDER = Paths.get("schematics")

        private val SAPLINGS = setOf(
            Material.ACACIA_SAPLING,
            Material.BIRCH_SAPLING,
            Material.CHERRY_SAPLING,
            Material.DARK_OAK_SAPLING,
            Material.JUNGLE_SAPLING,
            Material.MANGROVE_PROPAGULE,
            Material.OAK_SAPLING,
            Material.PALE_OAK_SAPLING,
            Material.SPRUCE_SAPLING,
        )
        private val OVERRIDEABLE_BLOCKS = setOf(
            // General
            BlockTypes.AIR,
            // Greenery
            BlockTypes.SHORT_GRASS,
            BlockTypes.TALL_GRASS,
            BlockTypes.SHORT_GRASS,
            BlockTypes.FERN,
            BlockTypes.LARGE_FERN,
            BlockTypes.DEAD_BUSH,
            BlockTypes.VINE,
            BlockTypes.SEAGRASS,
            BlockTypes.TALL_SEAGRASS,
            BlockTypes.SUGAR_CANE,
            BlockTypes.KELP,
            BlockTypes.KELP_PLANT,
            BlockTypes.CAVE_VINES,
            BlockTypes.CAVE_VINES_PLANT,
            BlockTypes.WEEPING_VINES,
            BlockTypes.WEEPING_VINES_PLANT,
            BlockTypes.TWISTING_VINES,
            BlockTypes.TWISTING_VINES_PLANT,
            BlockTypes.FLOWERING_AZALEA_LEAVES,
            BlockTypes.AZALEA_LEAVES,
            BlockTypes.AZALEA,
            BlockTypes.FLOWERING_AZALEA,
            BlockTypes.SNOW,
            BlockTypes.MOSS_CARPET,
            BlockTypes.MOSS_BLOCK,
            // Small Flowers
            BlockTypes.ALLIUM,
            BlockTypes.AZURE_BLUET,
            BlockTypes.BLUE_ORCHID,
            BlockTypes.CORNFLOWER,
            BlockTypes.DANDELION,
            BlockTypes.CLOSED_EYEBLOSSOM,
            BlockTypes.OPEN_EYEBLOSSOM,
            BlockTypes.LILY_OF_THE_VALLEY,
            BlockTypes.OXEYE_DAISY,
            BlockTypes.POPPY,
            BlockTypes.TORCHFLOWER,
            BlockTypes.ORANGE_TULIP,
            BlockTypes.PINK_TULIP,
            BlockTypes.RED_TULIP,
            BlockTypes.WHITE_TULIP,
            BlockTypes.WITHER_ROSE,
            // Tall Flowers
            BlockTypes.LILAC,
            BlockTypes.PEONY,
            BlockTypes.PITCHER_PLANT,
            BlockTypes.ROSE_BUSH,
            BlockTypes.SUNFLOWER,
            // Other Flowers
            BlockTypes.CHERRY_LEAVES,
            BlockTypes.CHORUS_FLOWER,
            BlockTypes.FLOWERING_AZALEA,
            BlockTypes.FLOWERING_AZALEA_LEAVES,
            BlockTypes.MANGROVE_PROPAGULE,
            BlockTypes.PINK_PETALS,
//            BlockTypes.WILDFLOWERS, // TODO: Add in 1.21.5
            BlockTypes.SPORE_BLOSSOM,
        )
    }

    override fun init() {
        Utils.copyResourcesFromJar(SCHEMATICS_FOLDER, schematicsPath)
        saplingSchematicMap = loadSaplingSchematicMap()
    }

    private fun loadSaplingSchematicMap(): Map<Material, List<Path>> {
        return Config.SaplingModule.saplingLink
            .mapNotNull { (k, v) -> validateAndMapSapling(k, v) }
            .toMap()
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
            Files.newInputStream(schematicFile).use { inputStream ->
                try {
                    val clipboard = format.getReader(inputStream).read()
                    try {
                        WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(block.world))
                            .use { editSession ->
                                block.type = Material.AIR
                                editSession.mask = BlockTypeMask(editSession, OVERRIDEABLE_BLOCKS)
                                Operations.complete(
                                    ClipboardHolder(clipboard)
                                        .createPaste(editSession)
                                        .to(BlockVector3.at(block.x, block.y, block.z))
                                        .ignoreAirBlocks(Config.SaplingModule.IGNORE_AIR_BLOCKS)
                                        .ignoreStructureVoidBlocks(Config.SaplingModule.IGNORE_STRUCTURE_VOID_BLOCKS)
                                        .copyEntities(Config.SaplingModule.COPY_ENTITIES)
                                        .copyBiomes(Config.SaplingModule.COPY_BIOMES)
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

    override fun enabled(): Boolean = Config.SaplingModule.ENABLE
}
