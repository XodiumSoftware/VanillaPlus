/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.old

import org.bukkit.Material
import org.bukkit.Tag
import org.xodium.vanillaplus.registries.MaterialRegistry
import java.util.*

/**
 * Please don't cry because I use Strings instead of Material. It's for backward compatability and the map only gets built once on startup, so don't worry
 */
class AutoToolsUtils {
    val handler: AutoToolsHandler =
        Objects.requireNonNull<AutoToolsHandler>(AutoToolsHandler(), "ToolHandler must not be null")

    val wood: Array<String> = arrayOf<String>(
        "BIRCH",
        "ACACIA",
        "OAK",
        "DARK_OAK",
        "SPRUCE",
        "JUNGLE"
    ) // Crimson and Warped stems are not needed, this is only for old versions

    init {
        for ((c, t) in listOf(
            MaterialRegistry.WEAPONS to handler.weapons,
            MaterialRegistry.INSTA_BREAKABLE_BY_HAND to handler.instaBreakableByHand,
            MaterialRegistry.HOES to handler.hoes,
            MaterialRegistry.PICKAXES to handler.pickaxes,
            MaterialRegistry.AXES to handler.axes,
            MaterialRegistry.SHOVELS to handler.shovels,
            MaterialRegistry.SWORDS to handler.swords,
            MaterialRegistry.NETHERITE_TOOLS to handler.allTools
        )) t.addAll(c)

        handler.allTools.addAll(MaterialRegistry.DEFAULT_MATERIALS)
        this.initMap()
    }

    private fun tagToMap(tag: Tag<Material>, tool: AutoToolsHandler.Tool) {
        tagToMap(
            tag,
            Objects.requireNonNull<AutoToolsHandler.Tool?>(tool, "Tool must not be null"),
            null
        )
    }

    private fun tagToMap(tag: Tag<Material>, tool: AutoToolsHandler.Tool, match: String?) {
        tag
        Objects.requireNonNull<AutoToolsHandler.Tool>(tool, "Tool must not be null")
        for (mat in tag.getValues()) {
            if (match == null) {
                addToMap(mat, tool)
            } else {
                if (mat.name.contains(match)) {
                    addToMap(mat, tool)
                }
            }
        }
    }

    private fun addToMap(matName: String, tool: AutoToolsHandler.Tool) {
        val mat: Material? = Material.getMaterial(matName)
        if (mat == null) return
        addToMap(mat, tool)
    }

    private fun addToMap(mat: Material, tool: AutoToolsHandler.Tool) {
        Objects.requireNonNull<HashMap<Material?, AutoToolsHandler.Tool?>?>(
            Objects.requireNonNull<AutoToolsHandler?>(
                handler,
                "ToolHandler must not be null"
            ).toolMap, "ToolMap must not be null"
        )
            .put(
                Objects.requireNonNull<Material?>(mat, "Material must not be null"),
                Objects.requireNonNull<AutoToolsHandler.Tool?>(tool, "Tool must not be null")
            )
    }

    private fun handleBambooPlantableOnFallback() {
        for (s in arrayOf<String?>(
            "GRASS_BLOCK",
            "DIRT",
            "COARSE_DIRT",
            "GRAVEL",
            "MYCELIUM",
            "PODZOL",
            "SAND",
            "RED_SAND"
        )) {
            addToMap(s.toString(), AutoToolsHandler.Tool.SHOVEL)
        }
    }

    fun initMap() {
        System.nanoTime()
        initFallbackMaterials()
        try {
            tagToMap(Tag.ANVIL, AutoToolsHandler.Tool.PICKAXE)

            tagToMap(Tag.ICE, AutoToolsHandler.Tool.PICKAXE)
            tagToMap(Tag.LEAVES, AutoToolsHandler.Tool.SHEARS)
            tagToMap(Tag.LOGS, AutoToolsHandler.Tool.AXE)
            tagToMap(Tag.PLANKS, AutoToolsHandler.Tool.AXE)
            tagToMap(Tag.RAILS, AutoToolsHandler.Tool.PICKAXE)
            tagToMap(Tag.WOOL, AutoToolsHandler.Tool.SHEARS)

            // WATCH OUT FOR ORDER - START //
            tagToMap(Tag.BUTTONS, AutoToolsHandler.Tool.AXE)
            tagToMap(Tag.BUTTONS, AutoToolsHandler.Tool.PICKAXE, "STONE")

            tagToMap(Tag.DOORS, AutoToolsHandler.Tool.AXE)
            tagToMap(Tag.DOORS, AutoToolsHandler.Tool.PICKAXE, "IRON")

            tagToMap(Tag.TRAPDOORS, AutoToolsHandler.Tool.AXE)
            tagToMap(Tag.TRAPDOORS, AutoToolsHandler.Tool.PICKAXE, "IRON")

            tagToMap(Tag.SLABS, AutoToolsHandler.Tool.PICKAXE)
            tagToMap(Tag.WOODEN_SLABS, AutoToolsHandler.Tool.AXE)

            tagToMap(Tag.STAIRS, AutoToolsHandler.Tool.PICKAXE)
            tagToMap(Tag.WOODEN_STAIRS, AutoToolsHandler.Tool.PICKAXE)

            // WATCH OUT FOR ORDER - END //
            tagToMap(Tag.SAND, AutoToolsHandler.Tool.SHOVEL)
            tagToMap(Tag.STONE_BRICKS, AutoToolsHandler.Tool.PICKAXE)

            addToMap("SEAGRASS", AutoToolsHandler.Tool.SHEARS)
            addToMap("TALL_SEAGRASS", AutoToolsHandler.Tool.SHEARS)
        } catch (_: NoClassDefFoundError) {
            // GRASS_BLOCK prior to 1.13 is called GRASS
            addToMap("GRASS", AutoToolsHandler.Tool.SHOVEL)
        }

        // Tags for 1.14+
        try {
            tagToMap(Tag.BAMBOO_PLANTABLE_ON, AutoToolsHandler.Tool.SHOVEL)
            tagToMap(Tag.SIGNS, AutoToolsHandler.Tool.AXE)
            tagToMap(Tag.WALLS, AutoToolsHandler.Tool.PICKAXE)

            // Order important START
            tagToMap(Tag.FENCES, AutoToolsHandler.Tool.AXE)
            tagToMap(Tag.FENCES, AutoToolsHandler.Tool.PICKAXE, "NETHER")
            tagToMap(Tag.FENCES, AutoToolsHandler.Tool.PICKAXE, "BRICK")
            // Order important END
        } catch (_: NoSuchFieldError) {
            handleBambooPlantableOnFallback()
        } catch (_: NoClassDefFoundError) {
            handleBambooPlantableOnFallback()
        }

        // Tags for 1.15+
        try {
            tagToMap(Tag.BEEHIVES, AutoToolsHandler.Tool.AXE)
            tagToMap(Tag.SHULKER_BOXES, AutoToolsHandler.Tool.PICKAXE)
            tagToMap(Tag.CROPS, AutoToolsHandler.Tool.HOE)
            tagToMap(Tag.FLOWERS, AutoToolsHandler.Tool.NONE)
        } catch (_: NoSuchFieldError) {
        } catch (_: NoClassDefFoundError) {
        }

        // Tags for 1.16+
        try {
            tagToMap(Tag.CRIMSON_STEMS, AutoToolsHandler.Tool.AXE)
            tagToMap(Tag.FENCE_GATES, AutoToolsHandler.Tool.AXE)
            tagToMap(Tag.NYLIUM, AutoToolsHandler.Tool.PICKAXE)
            // Important order START //
            tagToMap(Tag.PRESSURE_PLATES, AutoToolsHandler.Tool.PICKAXE)
            tagToMap(Tag.WOODEN_PRESSURE_PLATES, AutoToolsHandler.Tool.AXE)

            // Important order STOP //
        } catch (_: NoSuchFieldError) {
        } catch (_: NoClassDefFoundError) {
        }

        // Stairs in 1.16+
        addToMap("WARPED_STAIRS", AutoToolsHandler.Tool.AXE)
        addToMap("CRIMSON_STAIRS", AutoToolsHandler.Tool.AXE)

        // Some of the following definitions are redundant because of the tags above
        // However I don't want to miss something, so they are still defined here
        // Shouldn't harm because building the map takes only take 2 ms when the
        // plugin is enabled
        addToMap("BONE_BLOCK", AutoToolsHandler.Tool.PICKAXE)

        // Issue #1
        addToMap("BASALT", AutoToolsHandler.Tool.PICKAXE)
        addToMap("POLISHED_BASALT", AutoToolsHandler.Tool.PICKAXE)
        addToMap("GLOWSTONE", AutoToolsHandler.Tool.PICKAXE) // TODO: Prefer SilkTouch
        addToMap("NETHER_GOLD_ORE", AutoToolsHandler.Tool.PICKAXE)

        // Issue #1 End

        // Issue #2
        addToMap("SPONGE", AutoToolsHandler.Tool.HOE)
        addToMap("WET_SPONGE", AutoToolsHandler.Tool.HOE)
        addToMap("PISTON", AutoToolsHandler.Tool.PICKAXE)
        addToMap("STICKY_PISTON", AutoToolsHandler.Tool.PICKAXE)
        addToMap("PISTON_HEAD", AutoToolsHandler.Tool.PICKAXE)
        addToMap("MOVING_PISTON", AutoToolsHandler.Tool.PICKAXE)
        addToMap("CHORUS_PLANT", AutoToolsHandler.Tool.AXE)
        addToMap("CHORUS_FLOWER", AutoToolsHandler.Tool.AXE)
        addToMap("CARVED_PUMPKIN", AutoToolsHandler.Tool.AXE)
        addToMap("HAY_BLOCK", AutoToolsHandler.Tool.HOE)
        addToMap("OBSERVER", AutoToolsHandler.Tool.PICKAXE)
        addToMap("NETHER_WART_BLOCK", AutoToolsHandler.Tool.HOE)
        addToMap("WARPED_WART_BLOCK", AutoToolsHandler.Tool.HOE)
        addToMap("MAGMA_BLOCK", AutoToolsHandler.Tool.PICKAXE)

        // Issue #2 End

        // Issue #3
        addToMap("TARGET", AutoToolsHandler.Tool.HOE)
        addToMap("SHROOMLIGHT", AutoToolsHandler.Tool.HOE)
        addToMap("BELL", AutoToolsHandler.Tool.PICKAXE)
        addToMap("STONECUTTER", AutoToolsHandler.Tool.PICKAXE)
        addToMap("SMITHING_TABLE", AutoToolsHandler.Tool.AXE)
        addToMap("LECTERN", AutoToolsHandler.Tool.AXE)
        addToMap("GRINDSTONE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("FLETCHING_TABLE", AutoToolsHandler.Tool.AXE)
        addToMap("CARTOGRAPHY_TABLE", AutoToolsHandler.Tool.AXE)
        addToMap("BLAST_FURNACE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("SMOKER", AutoToolsHandler.Tool.PICKAXE)
        addToMap("BARREL", AutoToolsHandler.Tool.AXE)
        addToMap("COMPOSTER", AutoToolsHandler.Tool.AXE)
        addToMap("LOOM", AutoToolsHandler.Tool.AXE)
        addToMap("DRIED_KELP_BLOCK", AutoToolsHandler.Tool.HOE)

        // Issue #3 End
        addToMap("ACACIA_BUTTON", AutoToolsHandler.Tool.AXE)
        addToMap("ACACIA_FENCE", AutoToolsHandler.Tool.AXE)
        addToMap("ACACIA_FENCE_GATE", AutoToolsHandler.Tool.AXE)
        addToMap("ACACIA_LEAVES", AutoToolsHandler.Tool.SHEARS)
        addToMap("ACACIA_PRESSURE_PLATE", AutoToolsHandler.Tool.AXE)
        addToMap("ACACIA_SLAB", AutoToolsHandler.Tool.AXE)
        addToMap("ACACIA_STAIRS", AutoToolsHandler.Tool.AXE)
        addToMap("ANCIENT_DEBRIS", AutoToolsHandler.Tool.PICKAXE)
        addToMap("ANDESITE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("BAMBOO", AutoToolsHandler.Tool.AXE)
        addToMap("BAMBOO_SAPLING", AutoToolsHandler.Tool.AXE)
        addToMap("BASALT", AutoToolsHandler.Tool.PICKAXE)
        addToMap("BIRCH_BUTTON", AutoToolsHandler.Tool.AXE)
        addToMap("BIRCH_FENCE", AutoToolsHandler.Tool.AXE)
        addToMap("BIRCH_FENCE_GATE", AutoToolsHandler.Tool.AXE)
        addToMap("BIRCH_LEAVES", AutoToolsHandler.Tool.SHEARS)
        addToMap("BIRCH_PRESSURE_PLATE", AutoToolsHandler.Tool.AXE)
        addToMap("BIRCH_SLAB", AutoToolsHandler.Tool.AXE)
        addToMap("BIRCH_STAIRS", AutoToolsHandler.Tool.AXE)
        addToMap("BLACKSTONE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("BLACKSTONE_SLAB", AutoToolsHandler.Tool.PICKAXE)
        addToMap("BLACKSTONE_STAIRS", AutoToolsHandler.Tool.PICKAXE)
        addToMap("BLACK_CONCRETE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("BLACK_CONCRETE_POWDER", AutoToolsHandler.Tool.SHOVEL)
        addToMap("BLUE_CONCRETE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("BLUE_CONCRETE_POWDER", AutoToolsHandler.Tool.SHOVEL)
        addToMap("BOOKSHELF", AutoToolsHandler.Tool.AXE)
        addToMap("BREWING_STAND", AutoToolsHandler.Tool.PICKAXE)
        addToMap("BRICKS", AutoToolsHandler.Tool.PICKAXE)
        addToMap("BRICK_SLAB", AutoToolsHandler.Tool.PICKAXE)
        addToMap("BRICK_STAIRS", AutoToolsHandler.Tool.PICKAXE)
        addToMap("BROWN_CONCRETE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("BROWN_CONCRETE_POWDER", AutoToolsHandler.Tool.SHOVEL)
        addToMap("BROWN_MUSHROOM_BLOCK", AutoToolsHandler.Tool.AXE)
        addToMap("CAMPFIRE", AutoToolsHandler.Tool.AXE)
        addToMap("CAULDRON", AutoToolsHandler.Tool.PICKAXE)
        addToMap("CHAIN", AutoToolsHandler.Tool.PICKAXE)
        addToMap("CHEST", AutoToolsHandler.Tool.AXE)
        addToMap("CHISELED_RED_SANDSTONE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("CHISELED_SANDSTONE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("CHISELED_STONE_BRICKS", AutoToolsHandler.Tool.PICKAXE)
        addToMap("CLAY", AutoToolsHandler.Tool.SHOVEL)
        addToMap("COAL_BLOCK", AutoToolsHandler.Tool.PICKAXE)
        addToMap("COAL_ORE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("COARSE_DIRT", AutoToolsHandler.Tool.SHOVEL)
        addToMap("COBBLESTONE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("COBBLESTONE_SLAB", AutoToolsHandler.Tool.PICKAXE)
        addToMap("COBBLESTONE_STAIRS", AutoToolsHandler.Tool.PICKAXE)
        addToMap("COBWEB", AutoToolsHandler.Tool.SHEARS)
        addToMap("COCOA", AutoToolsHandler.Tool.AXE)
        addToMap("CRACKED_STONE_BRICKS", AutoToolsHandler.Tool.PICKAXE)
        addToMap("CRAFTING_TABLE", AutoToolsHandler.Tool.AXE)
        addToMap("CRYING_OBSIDIAN", AutoToolsHandler.Tool.PICKAXE)
        addToMap("CUT_RED_SANDSTONE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("CUT_SANDSTONE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("CYAN_CONCRETE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("CYAN_CONCRETE_POWDER", AutoToolsHandler.Tool.SHOVEL)
        addToMap("DARK_OAK_BUTTON", AutoToolsHandler.Tool.AXE)
        addToMap("DARK_OAK_FENCE", AutoToolsHandler.Tool.AXE)
        addToMap("DARK_OAK_FENCE_GATE", AutoToolsHandler.Tool.AXE)
        addToMap("DARK_OAK_LEAVES", AutoToolsHandler.Tool.SHEARS)
        addToMap("DARK_OAK_PRESSURE_PLATE", AutoToolsHandler.Tool.AXE)
        addToMap("DARK_OAK_SLAB", AutoToolsHandler.Tool.AXE)
        addToMap("DARK_OAK_STAIRS", AutoToolsHandler.Tool.AXE)
        addToMap("DARK_PRISMARINE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("DARK_PRISMARINE_SLAB", AutoToolsHandler.Tool.PICKAXE)
        addToMap("DARK_PRISMARINE_STAIRS", AutoToolsHandler.Tool.PICKAXE)
        addToMap("DAYLIGHT_DETECTOR", AutoToolsHandler.Tool.AXE)
        addToMap("DIAMOND_BLOCK", AutoToolsHandler.Tool.PICKAXE)
        addToMap("DIAMOND_ORE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("DIORITE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("DIRT", AutoToolsHandler.Tool.SHOVEL)
        addToMap("DISPENSER", AutoToolsHandler.Tool.PICKAXE)
        addToMap("DROPPER", AutoToolsHandler.Tool.PICKAXE)
        addToMap("EMERALD_BLOCK", AutoToolsHandler.Tool.PICKAXE)
        addToMap("EMERALD_ORE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("ENCHANTING_TABLE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("ENDER_CHEST", AutoToolsHandler.Tool.PICKAXE)
        addToMap("END_STONE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("FARMLAND", AutoToolsHandler.Tool.SHOVEL)
        addToMap("FURNACE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("GILDED_BLACKSTONE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("GOLD_BLOCK", AutoToolsHandler.Tool.PICKAXE)
        addToMap("GOLD_ORE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("GRANITE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("GRASS_BLOCK", AutoToolsHandler.Tool.SHOVEL)
        addToMap("GRASS_PATH", AutoToolsHandler.Tool.SHOVEL)
        addToMap("DIRT_PATH", AutoToolsHandler.Tool.SHOVEL)
        addToMap("GRAVEL", AutoToolsHandler.Tool.SHOVEL)
        addToMap("GRAY_CONCRETE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("GRAY_CONCRETE_POWDER", AutoToolsHandler.Tool.SHOVEL)
        addToMap("GREEN_CONCRETE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("GREEN_CONCRETE_POWDER", AutoToolsHandler.Tool.SHOVEL)
        addToMap("HEAVY_WEIGHTED_PRESSURE_PLATE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("HOPPER", AutoToolsHandler.Tool.PICKAXE)
        addToMap("IRON_BARS", AutoToolsHandler.Tool.PICKAXE)
        addToMap("IRON_BLOCK", AutoToolsHandler.Tool.PICKAXE)
        addToMap("IRON_DOOR", AutoToolsHandler.Tool.PICKAXE)
        addToMap("IRON_ORE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("IRON_TRAPDOOR", AutoToolsHandler.Tool.PICKAXE)
        addToMap("JACK_O_LANTERN", AutoToolsHandler.Tool.AXE)
        addToMap("JUKEBOX", AutoToolsHandler.Tool.AXE)
        addToMap("JUNGLE_BUTTON", AutoToolsHandler.Tool.AXE)
        addToMap("JUNGLE_FENCE", AutoToolsHandler.Tool.AXE)
        addToMap("JUNGLE_FENCE_GATE", AutoToolsHandler.Tool.AXE)
        addToMap("JUNGLE_LEAVES", AutoToolsHandler.Tool.SHEARS)
        addToMap("JUNGLE_PRESSURE_PLATE", AutoToolsHandler.Tool.AXE)
        addToMap("JUNGLE_SLAB", AutoToolsHandler.Tool.AXE)
        addToMap("JUNGLE_STAIRS", AutoToolsHandler.Tool.AXE)
        addToMap("LADDER", AutoToolsHandler.Tool.AXE)
        addToMap("LANTERN", AutoToolsHandler.Tool.PICKAXE)
        addToMap("LAPIS_BLOCK", AutoToolsHandler.Tool.PICKAXE)
        addToMap("LAPIS_ORE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("LIGHT_BLUE_CONCRETE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("LIGHT_BLUE_CONCRETE_POWDER", AutoToolsHandler.Tool.SHOVEL)
        addToMap("LIGHT_GRAY_CONCRETE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("LIGHT_GRAY_CONCRETE_POWDER", AutoToolsHandler.Tool.SHOVEL)
        addToMap("LIGHT_WEIGHTED_PRESSURE_PLATE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("LIME_CONCRETE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("LIME_CONCRETE_POWDER", AutoToolsHandler.Tool.SHOVEL)
        addToMap("LODESTONE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("MAGENTA_CONCRETE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("MAGENTA_CONCRETE_POWDER", AutoToolsHandler.Tool.SHOVEL)
        addToMap("MELON", AutoToolsHandler.Tool.AXE)
        addToMap("MOSSY_COBBLESTONE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("MOSSY_STONE_BRICKS", AutoToolsHandler.Tool.PICKAXE)
        addToMap("MUSHROOM_STEM", AutoToolsHandler.Tool.AXE)
        addToMap("MYCELIUM", AutoToolsHandler.Tool.SHOVEL)
        addToMap("NETHERITE_BLOCK", AutoToolsHandler.Tool.PICKAXE)
        addToMap("NETHERRACK", AutoToolsHandler.Tool.PICKAXE)
        addToMap("NETHER_BRICK", AutoToolsHandler.Tool.PICKAXE)
        addToMap("NETHER_BRICK_FENCE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("NETHER_BRICK_SLAB", AutoToolsHandler.Tool.PICKAXE)
        addToMap("NETHER_BRICK_STAIRS", AutoToolsHandler.Tool.PICKAXE)
        addToMap("NETHER_QUARTZ_ORE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("NOTE_BLOCK", AutoToolsHandler.Tool.AXE)
        addToMap("OAK_BUTTON", AutoToolsHandler.Tool.AXE)
        addToMap("OAK_FENCE", AutoToolsHandler.Tool.AXE)
        addToMap("OAK_FENCE_GATE", AutoToolsHandler.Tool.AXE)
        addToMap("OAK_LEAVES", AutoToolsHandler.Tool.SHEARS)
        addToMap("OAK_PRESSURE_PLATE", AutoToolsHandler.Tool.AXE)
        addToMap("OAK_SLAB", AutoToolsHandler.Tool.AXE)
        addToMap("OAK_STAIRS", AutoToolsHandler.Tool.AXE)
        addToMap("OBSIDIAN", AutoToolsHandler.Tool.PICKAXE)
        addToMap("ORANGE_CONCRETE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("ORANGE_CONCRETE_POWDER", AutoToolsHandler.Tool.SHOVEL)
        addToMap("PINK_CONCRETE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("PINK_CONCRETE_POWDER", AutoToolsHandler.Tool.SHOVEL)
        addToMap("PODZOL", AutoToolsHandler.Tool.SHOVEL)
        addToMap("POLISHED_ANDESITE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("POLISHED_DIORITE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("POLISHED_GRANITE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("PUMPKIN", AutoToolsHandler.Tool.AXE)
        addToMap("PURPLE_CONCRETE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("PURPLE_CONCRETE_POWDER", AutoToolsHandler.Tool.SHOVEL)
        addToMap("QUARTZ_BLOCK", AutoToolsHandler.Tool.PICKAXE)
        addToMap("QUARTZ_BRICKS", AutoToolsHandler.Tool.PICKAXE)
        addToMap("REDSTONE_BLOCK", AutoToolsHandler.Tool.PICKAXE)
        addToMap("REDSTONE_ORE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("RED_CONCRETE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("RED_CONCRETE_POWDER", AutoToolsHandler.Tool.SHOVEL)
        addToMap("RED_MUSHROOM_BLOCK", AutoToolsHandler.Tool.AXE)
        addToMap("RED_NETHER_BRICKS", AutoToolsHandler.Tool.PICKAXE)
        addToMap("RED_NETHER_BRICK_SLAB", AutoToolsHandler.Tool.PICKAXE)
        addToMap("RED_NETHER_BRICK_STAIRS", AutoToolsHandler.Tool.PICKAXE)
        addToMap("RED_SAND", AutoToolsHandler.Tool.SHOVEL)
        addToMap("RED_SANDSTONE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("RED_SANDSTONE_SLAB", AutoToolsHandler.Tool.PICKAXE)
        addToMap("RED_SANDSTONE_STAIRS", AutoToolsHandler.Tool.PICKAXE)
        addToMap("RESPAWN_ANCHOR", AutoToolsHandler.Tool.PICKAXE)
        addToMap("SAND", AutoToolsHandler.Tool.SHOVEL)
        addToMap("SANDSTONE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("SANDSTONE_SLAB", AutoToolsHandler.Tool.PICKAXE)
        addToMap("SANDSTONE_STAIRS", AutoToolsHandler.Tool.PICKAXE)
        addToMap("SEA_LANTERN", AutoToolsHandler.Tool.PICKAXE)
        addToMap("SMOOTH_QUARTZ", AutoToolsHandler.Tool.PICKAXE)
        addToMap("SMOOTH_RED_SANDSTONE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("SMOOTH_SANDSTONE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("SMOOTH_STONE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("SNOW", AutoToolsHandler.Tool.SHOVEL)
        addToMap("SNOW_BLOCK", AutoToolsHandler.Tool.SHOVEL)
        addToMap("SOUL_CAMPFIRE", AutoToolsHandler.Tool.AXE)
        addToMap("SOUL_LANTERN", AutoToolsHandler.Tool.PICKAXE)
        addToMap("SOUL_SAND", AutoToolsHandler.Tool.SHOVEL)
        addToMap("SOUL_SOIL", AutoToolsHandler.Tool.SHOVEL)
        addToMap("SPAWNER", AutoToolsHandler.Tool.PICKAXE)
        addToMap("SPRUCE_BUTTON", AutoToolsHandler.Tool.AXE)
        addToMap("SPRUCE_FENCE", AutoToolsHandler.Tool.AXE)
        addToMap("SPRUCE_FENCE_GATE", AutoToolsHandler.Tool.AXE)
        addToMap("SPRUCE_LEAVES", AutoToolsHandler.Tool.SHEARS)
        addToMap("SPRUCE_PRESSURE_PLATE", AutoToolsHandler.Tool.AXE)
        addToMap("SPRUCE_STAIRS", AutoToolsHandler.Tool.AXE)
        addToMap("STONE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("STONE_BRICKS", AutoToolsHandler.Tool.PICKAXE)
        addToMap("STONE_BRICK_SLAB", AutoToolsHandler.Tool.PICKAXE)
        addToMap("STONE_BRICK_STAIRS", AutoToolsHandler.Tool.PICKAXE)
        addToMap("STONE_BUTTON", AutoToolsHandler.Tool.PICKAXE)
        addToMap("STONE_PRESSURE_PLATE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("STONE_SLAB", AutoToolsHandler.Tool.PICKAXE)
        addToMap("TERRACOTTA", AutoToolsHandler.Tool.PICKAXE)
        addToMap("TRAPPED_CHEST", AutoToolsHandler.Tool.AXE)
        addToMap("VINE", AutoToolsHandler.Tool.SHEARS)
        addToMap("WHITE_CONCRETE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("WHITE_CONCRETE_POWDER", AutoToolsHandler.Tool.SHOVEL)
        addToMap("YELLOW_CONCRETE", AutoToolsHandler.Tool.PICKAXE)
        addToMap("YELLOW_CONCRETE_POWDER", AutoToolsHandler.Tool.SHOVEL)

        // 1.17
        try {
            for (material in Material.entries) {
                if (material.name.contains("AMETHYST")) {
                    addToMap(material.name, AutoToolsHandler.Tool.PICKAXE)
                }
                if (material.name.endsWith("_ORE")) {
                    addToMap(material.name, AutoToolsHandler.Tool.PICKAXE)
                }
                if (material.name.contains("BASALT")) {
                    addToMap(material.name, AutoToolsHandler.Tool.PICKAXE)
                }
                if (material.name.contains("DEEPSLATE")) {
                    addToMap(material.name, AutoToolsHandler.Tool.PICKAXE)
                }
            }

            addToMap(Material.GLOW_LICHEN, AutoToolsHandler.Tool.SHEARS)
            addToMap(Material.CALCITE, AutoToolsHandler.Tool.PICKAXE)
        } catch (_: Throwable) {
        }

        // This is fairly expensive, but 2ms vs 8ms isn't a meaningful difference.
        try {
            tagToMap(Tag.MINEABLE_AXE, AutoToolsHandler.Tool.AXE)
            tagToMap(Tag.MINEABLE_HOE, AutoToolsHandler.Tool.HOE)
            tagToMap(Tag.MINEABLE_PICKAXE, AutoToolsHandler.Tool.PICKAXE)
            tagToMap(Tag.MINEABLE_SHOVEL, AutoToolsHandler.Tool.SHOVEL)
        } catch (_: NoSuchFieldError) {
        } catch (_: NoClassDefFoundError) {
        }
    }

    // F****** Spigot API is not "forward compatible" with new Material enums
    // TODO: I believe we can avoid compatibility issues by using Registry<Material>
    private fun initFallbackMaterials() {
        for (mat in Material.entries) {
            if (!mat.isBlock) {
                continue
            }

            val n: String = mat.name

            if (n.contains("GLASS")) {
                addToMap(mat, AutoToolsHandler.Tool.PICKAXE)
                continue
            }

            // Fallback for all wooden things
            for (woodType in wood) {
                if (n.contains(woodType)) {
                    if (n.contains("STAIRS") || n.contains("LOG") || n.contains("PLANK")) {
                        addToMap(mat, AutoToolsHandler.Tool.AXE)
                    }
                }
            }

            // Fallback for Tag.WALLS
            if (n.contains("STONE") || n.contains("BRICK")) {
                addToMap(mat, AutoToolsHandler.Tool.PICKAXE)
                continue
            }

            // End Tag.WALLS

            // Issue #1
            if (n.contains("BLACKSTONE")) {
                addToMap(mat, AutoToolsHandler.Tool.PICKAXE)
                continue
            }
            if (n.contains("NETHER_BRICK")) {
                addToMap(mat, AutoToolsHandler.Tool.PICKAXE)
                continue
            }

            // Issue #1 End

            // Issue #2
            if (n.contains("TERRACOTTA")) {
                addToMap(mat, AutoToolsHandler.Tool.PICKAXE)
                continue
            }
            if (n.contains("PURPUR")) {
                addToMap(mat, AutoToolsHandler.Tool.PICKAXE)
                continue
            }
            if (n.contains("INFESTED")) {
                addToMap(mat, AutoToolsHandler.Tool.PICKAXE)
                continue
            }
            if (n.contains("ENDSTONE_BRICK")) {
                addToMap(mat, AutoToolsHandler.Tool.PICKAXE)
                continue
            }
            if (n.contains("QUARTZ")) {
                addToMap(mat, AutoToolsHandler.Tool.PICKAXE)
                continue
            }
            if (n.contains("CORAL_BLOCK")) {
                addToMap(mat, AutoToolsHandler.Tool.PICKAXE)
                continue
            }
            if (n.contains("PRISMARINE")) {
                addToMap(mat, AutoToolsHandler.Tool.PICKAXE)
                continue
            }

            // Issue #2 End

            // Tags only in 1.16+ START
            if (n.contains("FENCE_GATE")) {
                addToMap(mat, AutoToolsHandler.Tool.AXE)
                continue
            }
            if (n.contains("PRESSURE_PLATE")) {
                if (n.contains("STONE") || n.contains("IRON") || n.contains("GOLD")) {
                    addToMap(mat, AutoToolsHandler.Tool.PICKAXE)
                    continue
                }
                addToMap(mat, AutoToolsHandler.Tool.AXE)
                continue
            }

            // Tags only in 1.16+ END

            // Tags only in 1.15+ START
            if (n.contains("SHULKER_BOX")) {
                addToMap(mat, AutoToolsHandler.Tool.PICKAXE)
                continue
            }

            // Tags only in 1.15+ END

            // Tags only in 1.14+ START
            if (n.contains("FENCE")) {
                if (n.contains("NETHER") || n.contains("BRICK")) {
                    addToMap(mat, AutoToolsHandler.Tool.PICKAXE)
                    continue
                }
                addToMap(mat, AutoToolsHandler.Tool.AXE)
                continue
            }
            if (n.contains("SIGN")) {
                addToMap(mat, AutoToolsHandler.Tool.AXE)
                continue
            }

            // Tags only in 1.14+ END

            // Different item names < 1.13
            if (n == "LEAVES" || n == "WOOL") {
                addToMap(mat, AutoToolsHandler.Tool.SHEARS)
                continue
            }
            if (n == "WORKBENCH") {
                addToMap(mat, AutoToolsHandler.Tool.AXE)
            }
        }
    }
}
