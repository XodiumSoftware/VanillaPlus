/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.old

import org.bukkit.Material
import org.bukkit.Tag
import java.util.*

/**
 * Please don't cry because I use Strings instead of Material. It's for backward compatability and the map only gets built once on startup, so don't worry
 */
class AutoToolsUtils(main: Main) {
    val wood: Array<String> = arrayOf<String>(
        "BIRCH",
        "ACACIA",
        "OAK",
        "DARK_OAK",
        "SPRUCE",
        "JUNGLE"
    ) // Crimson and Warped stems are not needed, this is only for old versions
    val weapons: Array<String> = arrayOf<String>(
        "BOW",
        "CROSSBOW",
        "TRIDENT",
        "NETHERITE_SWORD",
        "DIAMOND_SWORD",
        "GOLDEN_SWORD",
        "IRON_SWORD",
        "STONE_SWORD",
        "WOODEN_SWORD",
        "MACE"
    )
    val instaBreakableByHand: Array<String> = arrayOf<String>(
        "COMPARATOR",
        "REPEATER",
        "REDSTONE_WIRE",
        "REDSTONE_TORCH",
        "REDSTONE_WALL_TORCH",
        "TORCH",
        "SOUL_TORCH",
        "WALL_TORCH",
        "SOUL_WALL_TORCH",
        "SCAFFOLDING",
        "SLIME_BLOCK",
        "HONEY_BLOCK",
        "TNT",
        "TRIPWIRE",
        "TRIPWIRE_HOOK",
        "GRASS",
        "SUGAR_CANE",
        "LILY_PAD",
        "OAK_SAPLING",
        "SPRUCE_SAPLING",
        "BIRCH_SAPLING",
        "JUNGLE_SAPLING",
        "ACACIA_SAPLING",
        "DARK_OAK_SAPLING",
        "BROWN_MUSHROOM",
        "RED_MUSHROOM",
        "CRIMSON_FUNGUS",
        "WARPED_FUNGUS",
        "CRIMSON_ROOTS",
        "WARPED_ROOTS",
        "WEEPING VINES",
        "TWISTING_VINES",
        "DEAD_BUSH",
        "WHEAT",
        "CARROTS",
        "POTATOES",
        "BEETROOTS",
        "PUMPKIN_STEM",
        "MELON_STEM",
        "NETHER_WART",
        "FLOWER_POT",
        "DANDELION",
        "POPPY",
        "BLUE_ORCHID",
        "ALLIUM",
        "AZURE_BLUET",
        "RED_TULIP",
        "ORANGE_TULIP",
        "WHITE_TULIP",
        "PINK_TULIP",
        "OXEYE_DAISY",
        "CORNFLOWER",
        "LILY_OF_THE_VALLEY",
        "WITHER_ROSE",
        "SUNFLOWER",
        "LILAC",
        "ROSE_BUSH",
        "PEONY",
        "POTTED_DANDELION",
        "POTTED_POPPY",
        "POTTED_BLUE_ORCHID",
        "POTTED_ALLIUM",
        "POTTED_AZURE_BLUET",
        "POTTED_RED_TULIP",
        "POTTED_ORANGE_TULIP",
        "POTTED_WHITE_TULIP",
        "POTTED_PINK_TULIP",
        "POTTED_OXEYE_DAISY",
        "POTTED_CORNFLOWER",
        "POTTED_LILY_OF_THE_VALLEY",
        "POTTED_WITHER_ROSE",
        "POTTED_SUNFLOWER",
        "POTTED_LILAC",
        "POTTED_ROSE_BUSH",
        "POTTED_PEONY",
        "TUBE_CORAL",
        "BRAIN_CORAL",
        "BUBBLE_CORAL",
        "FIRE_CORAL",
        "HORN_CORAL",
        "DEAD_TUBE_CORAL",
        "DEAD_BRAIN_CORAL",
        "DEAD_BUBBLE_CORAL",
        "DEAD_FIRE_CORAL",
        "DEAD_HORN_CORAL"
    )

    // TODO: Add grass only in 1.13+ instead of always
    val hoes: Array<String> =
        arrayOf<String>("NETHERITE_HOE", "DIAMOND_HOE", "GOLDEN_HOE", "IRON_HOE", "STONE_HOE", "WOODEN_HOE")
    val pickaxes: Array<String> = arrayOf<String>(
        "NETHERITE_PICKAXE",
        "DIAMOND_PICKAXE",
        "GOLDEN_PICKAXE",
        "IRON_PICKAXE",
        "STONE_PICKAXE",
        "WOODEN_PICKAXE"
    )
    val axes: Array<String> =
        arrayOf<String>("NETHERITE_AXE", "DIAMOND_AXE", "GOLDEN_AXE", "IRON_AXE", "STONE_AXE", "WOODEN_AXE")
    val shovels: Array<String> = arrayOf<String>(
        "NETHERITE_SHOVEL",
        "DIAMOND_SHOVEL",
        "GOLDEN_SHOVEL",
        "IRON_SHOVEL",
        "STONE_SHOVEL",
        "WOODEN_SHOVEL"
    )

    val defaultMats: Array<Material?> = arrayOf<Material?>(
        Material.DIAMOND_PICKAXE,
        Material.DIAMOND_AXE,
        Material.DIAMOND_HOE,
        Material.DIAMOND_SHOVEL,

        Material.GOLDEN_PICKAXE,
        Material.GOLDEN_AXE,
        Material.GOLDEN_HOE,
        Material.GOLDEN_SHOVEL,

        Material.IRON_PICKAXE,
        Material.IRON_AXE,
        Material.IRON_HOE,
        Material.IRON_SHOVEL,

        Material.STONE_PICKAXE,
        Material.STONE_AXE,
        Material.STONE_HOE,
        Material.STONE_SHOVEL,

        Material.WOODEN_PICKAXE,
        Material.WOODEN_AXE,
        Material.WOODEN_HOE,
        Material.WOODEN_SHOVEL,

        Material.SHEARS
    )
    val netheriteTools: Array<String> = arrayOf<String>(
        "NETHERITE_PICKAXE",
        "NETHERITE_AXE",
        "NETHERITE_HOE",
        "NETHERITE_SHOVEL"
    )

    val main: Main = Objects.requireNonNull<Main>(main, "Main must not be null")

    // This is called AFTER BestToolsHandler, so the Utils can affect the Handler
    init {
        Objects.requireNonNull<AutoToolsHandler?>(
            main.toolHandler,
            "BestToolsHandler must be instantiated before BestToolUtils!"
        )

        // Register valid weapons
        for (weapon in weapons) {
            if (Material.getMaterial(weapon) != null) {
                main.toolHandler!!.weapons.add(Material.getMaterial(weapon))
            }
        }

        // Register all InstaBreaksByHand
        for (s in instaBreakableByHand) {
            addToMap(s, main.toolHandler!!.instaBreakableByHand)
        }

        // Hoes
        for (s in hoes) {
            addToMap(s, main.toolHandler!!.hoes)
        }

        // Pickaxes
        for (s in pickaxes) {
            addToMap(s, main.toolHandler!!.pickaxes)
        }

        // Axes
        for (s in axes) {
            addToMap(s, main.toolHandler!!.axes)
        }

        // Shovels
        for (s in shovels) {
            addToMap(s, main.toolHandler!!.shovels)
        }

        // Swords
        for (s in swords) {
            addToMap(s, main.toolHandler!!.swords)
        }

        main.toolHandler!!.allTools.addAll(listOf<Material?>(*defaultMats))
        for (s in netheriteTools) {
            if (Material.getMaterial(s) != null) {
                main.toolHandler!!.allTools.add(Material.getMaterial(s))
            }
        }

        this.initMap()

        //uToolMap = Map.copyOf(main.toolHandler.toolMap); // Java 10+ only
    }

    private fun addToMap(name: String, list: ArrayList<Material?>) {
        val mat: Material? = Material.getMaterial(name)
        if (mat != null) {
            list.add(mat)
        } else {
            main.debug("Skipping unknown Material $name")
        }
    }

    private fun tagToMap(tag: Tag<Material>, tool: AutoToolsHandler.Tool) {
        tagToMap(
            Objects.requireNonNull<Tag<Material?>?>(tag, "Tag must not be null"),
            Objects.requireNonNull<BestToolsHandler.Tool?>(tool, "Tool must not be null"),
            null
        )
    }

    private fun tagToMap(tag: Tag<Material>, tool: AutoToolsHandler.Tool, match: String?) {
        Objects.requireNonNull<Tag<Material?>>(tag, "Tag must not be null")
        Objects.requireNonNull<BestToolsHandler.Tool>(tool, "Tool must not be null")
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
        if (mat == null) {
            main.debug("Skipping unknown fallback Material $matName")
            return
        }
        addToMap(mat, tool)
    }

    private fun addToMap(mat: Material, tool: AutoToolsHandler.Tool) {
        Objects.requireNonNull<HashMap<Material?, AutoToolsHandler.Tool?>?>(
            Objects.requireNonNull<AutoToolsHandler?>(
                main.toolHandler,
                "ToolHandler must not be null"
            ).toolMap, "ToolMap must not be null"
        )
            .put(
                Objects.requireNonNull<Material?>(mat, "Material must not be null"),
                Objects.requireNonNull<BestToolsHandler.Tool?>(tool, "Tool must not be null")
            )
    }

    fun initMap() {
        val startTime = System.nanoTime()
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
            val bamboo_plantable_on = arrayOf<String?>(
                "GRASS_BLOCK",
                "DIRT",
                "COARSE_DIRT",
                "GRAVEL",
                "MYCELIUM",
                "PODZOL",
                "SAND",
                "RED_SAND"
            )
            for (s in bamboo_plantable_on) {
                addToMap(s.toString(), AutoToolsHandler.Tool.SHOVEL)
            }
        } catch (_: NoClassDefFoundError) {
            val bamboo_plantable_on = arrayOf<String?>(
                "GRASS_BLOCK",
                "DIRT",
                "COARSE_DIRT",
                "GRAVEL",
                "MYCELIUM",
                "PODZOL",
                "SAND",
                "RED_SAND"
            )
            for (s in bamboo_plantable_on) {
                addToMap(s.toString(), AutoToolsHandler.Tool.SHOVEL)
            }
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
            for (mat in Material.entries) {
                if (mat.name.contains("AMETHYST")) {
                    addToMap(mat.name, AutoToolsHandler.Tool.PICKAXE)
                }
                if (mat.name.endsWith("_ORE")) {
                    addToMap(mat.name, AutoToolsHandler.Tool.PICKAXE)
                }
                if (mat.name.contains("BASALT")) {
                    addToMap(mat.name, AutoToolsHandler.Tool.PICKAXE)
                }
                if (mat.name.contains("DEEPSLATE")) {
                    addToMap(mat.name, AutoToolsHandler.Tool.PICKAXE)
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


        val endTime = System.nanoTime()
        //printMap();
        if (main.verbose) {
            main.logger
                .info(String.format("Building the <Block,Tool> map took %d ms", (endTime - startTime) / 1000000))
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
