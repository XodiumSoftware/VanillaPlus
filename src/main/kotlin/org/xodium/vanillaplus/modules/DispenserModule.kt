package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Dispenser
import org.bukkit.block.Jukebox
import org.bukkit.block.data.Directional
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.bukkit.Sound as BukkitSound

/** Represents a module handling dispenser mechanics within the system. */
internal class DispenserModule : ModuleInterface<DispenserModule.Config> {
    override val config: Config = Config()

    @EventHandler
    fun on(event: BlockDispenseEvent) {
        if (!enabled()) return

        when {
            isPlantableCrop(event.item.type) -> handleCropPlanting(event)
            isMusicDisc(event.item.type) -> handleJukeboxInsertion(event)
            isEmptyBucket(event.item.type) -> handleCauldronLiquidCollection(event)
        }
    }

    /**
     * Checks if the given material is a plantable crop.
     * @param material The material to check.
     * @return `true` if the material is a plantable crop, `false` otherwise.
     * @see Tag.CROPS For the complete list of crop materials.
     */
    private fun isPlantableCrop(material: Material): Boolean = material in Tag.CROPS.values

    /**
     * Checks if the given material is a music disc.
     * @param material The material to check.
     * @return `true` if the material is a music disc, `false` otherwise.
     */
    private fun isMusicDisc(material: Material): Boolean = material in Material.entries.filter { it.name.startsWith("MUSIC_DISC") }

    /**
     * Checks if the given material is an empty bucket.
     * @param material The material to check.
     * @return `true` if the material is an empty bucket, `false` otherwise.
     */
    private fun isEmptyBucket(material: Material): Boolean = material == Material.BUCKET

    /**
     * Handles the automatic planting of crops from a dispenser.
     * @param event The block dispense event that triggered this handler.
     * @see targetBlock For determining the farmland block in front of the dispenser.
     * @see isFarmable For checking if the target block is suitable farmland.
     */
    private fun handleCropPlanting(event: BlockDispenseEvent) {
        val dispenser = event.block.state as? Dispenser ?: return
        val targetBlock = dispenser.targetBlock() ?: return

        if (!targetBlock.type.isFarmable()) return

        val blockAbove = targetBlock.getRelative(BlockFace.UP)

        if (blockAbove.type.isAir) return

        Tag.CROPS.isTagged(event.item.type) // NOTE: use this as a check.

        val cropType =
            when (event.item.type) {
                Material.WHEAT_SEEDS -> Material.WHEAT
                Material.POTATO -> Material.POTATOES
                Material.CARROT -> Material.CARROTS
                Material.BEETROOT_SEEDS -> Material.BEETROOTS
                Material.MELON_SEEDS -> Material.MELON_STEM
                Material.PUMPKIN_SEEDS -> Material.PUMPKIN_STEM
                Material.TORCHFLOWER_SEEDS -> Material.TORCHFLOWER_CROP
                Material.PITCHER_POD -> Material.PITCHER_CROP
                else -> return
            }

        blockAbove.type = cropType
        event.isCancelled = true

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable { dispenser.inventory.removeItem(event.item) },
            1L,
        )

        blockAbove.world.playSound(blockAbove.location, BukkitSound.ITEM_CROP_PLANT, 1.0f, 1.0f)
    }

    /**
     * Handles the insertion of a music disc into a jukebox via a dispenser.
     * @param event The block dispense event that triggered this handler.
     * @see targetBlock For determining the block in front of the dispenser.
     */
    private fun handleJukeboxInsertion(event: BlockDispenseEvent) {
        val dispenser = event.block.state as? Dispenser ?: return
        val targetBlock = dispenser.targetBlock() ?: return

        if (targetBlock.type != Material.JUKEBOX) return

        val jukebox = targetBlock.state as? Jukebox ?: return

        if (jukebox.record.type != Material.AIR) return

        jukebox.setRecord(event.item)
        jukebox.update()

        event.isCancelled = true

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable { dispenser.inventory.removeItem(event.item) },
            1L,
        )
    }

    /**
     * Handles the collection of liquids from cauldrons using empty buckets via dispensers.
     * @param event The block dispense event that triggered this handler.
     * @see targetBlock For determining the cauldron block in front of the dispenser.
     * @see replaceItem For swapping the empty bucket with a filled bucket in the dispenser.
     * @see isEmptyBucket For checking if the dispensed item is an empty bucket.
     */
    private fun handleCauldronLiquidCollection(event: BlockDispenseEvent) {
        val dispenser = event.block.state as? Dispenser ?: return
        val targetBlock = dispenser.targetBlock() ?: return

        if (!Tag.CAULDRONS.isTagged(targetBlock.type)) return

        val filledBucketType =
            when (targetBlock.type) {
                Material.WATER_CAULDRON -> Material.WATER_BUCKET
                Material.LAVA_CAULDRON -> Material.LAVA_BUCKET
                Material.POWDER_SNOW_CAULDRON -> Material.POWDER_SNOW_BUCKET
                else -> return
            }

        targetBlock.setType(Material.CAULDRON, true)

        event.isCancelled = true

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable { replaceItem(dispenser, Material.BUCKET, ItemStack.of(filledBucketType)) },
            1L,
        )

        targetBlock.world.playSound(targetBlock.location, BukkitSound.ITEM_BUCKET_FILL, 1.0f, 1.0f)
    }

    /**
     * Gets the block directly in front of this dispenser based on its facing direction.
     * @return The block directly in front of the dispenser, or `null` if it has no facing direction.
     * @see Directional For accessing the dispenser’s facing direction.
     * @see Block.getRelative For retrieving adjacent blocks based on direction.
     */
    private fun Dispenser.targetBlock(): Block? = (blockData as? Directional)?.facing?.let { block.getRelative(it) }

    /**
     * Checks if the given material is suitable farmland for planting crops.
     * @return `true` if the material is farmland or soul sand, `false` otherwise.
     */
    private fun Material.isFarmable(): Boolean = this == Material.FARMLAND || this == Material.SOUL_SAND

    /**
     * Replaces an item in the dispenser's inventory with a new item type.
     * @param dispenser The dispenser block state containing the inventory.
     * @param oldType The item type to be replaced.
     * @param newItem The new item stack to replace with (amount will be set to match old item).
     * @see Dispenser.inventory For accessing the dispenser's item storage.
     */
    private fun replaceItem(
        dispenser: Dispenser,
        oldType: Material,
        newItem: ItemStack,
    ) {
        for ((index, item) in dispenser.inventory.contents.withIndex()) {
            if (item != null && item.type == oldType && item.amount > 0) {
                item.amount -= 1

                val targetIndex = if (item.amount == 0) index else dispenser.inventory.firstEmpty()
                if (targetIndex != -1) dispenser.inventory.setItem(targetIndex, newItem.clone().apply { amount = 1 })

                break
            }
        }
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
