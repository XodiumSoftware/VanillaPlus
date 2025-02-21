/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.world.StructureGrowEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.registries.MaterialRegistry


class TreesModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigData.TreesModule().enabled

    private val schematicCache: Map<Material, List<String>> =
        ConfigData.TreesModule().saplingLink.mapValues { (_, dirs) ->
            dirs.flatMap { dir -> loadSchematics("/schematics/$dir") }
        }

    private fun loadSchematics(resourceDir: String): List<String> {
        //TODO: replace with own.
        return TODO("Provide the return value")
    }


    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: StructureGrowEvent) {
        event.location.block.takeIf { MaterialRegistry.SAPLINGS.contains(it.type) }?.let {
            event.isCancelled = pasteSchematic(it)
        }
    }

    private fun pasteSchematic(block: Block): Boolean {
        val clipboard = schematicCache[block.type]?.randomOrNull()
        if (clipboard == null) {
            instance.logger.info("No custom schematic found for ${block.type}.")
            return false
        }
        //TODO: replace with own.
        return true
    }
}
