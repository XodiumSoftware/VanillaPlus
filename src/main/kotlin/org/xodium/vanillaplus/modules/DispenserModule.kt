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

        val block = event.block
        val item = event.item
        val dispenser = block.state as? Dispenser ?: return

        when {
            isPlantableCrop(item.type) -> handleCropPlanting(block, item, event)
            isMusicDisc(item.type) -> handleJukeboxInsertion(block, item, event)
            isEmptyBucket(item.type) -> handleCauldronLiquidCollection(block, item, dispenser, event)
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
     * @param block The dispenser block that is attempting to plant the crop.
     * @param item The seed item stack being dispensed.
     * @param event The block dispense event that triggered this handler.
     * @see getTargetBlock For determining the farmland block in front of the dispenser.
     * @see isFarmland For checking if the target block is suitable farmland.
     * @see consumeItem For removing the item from the dispenser's inventory.
     */
    private fun handleCropPlanting(
        block: Block,
        item: ItemStack,
        event: BlockDispenseEvent,
    ) {
        val targetBlock = getTargetBlock(block) ?: return

        if (!isFarmland(targetBlock.type)) return

        val blockAbove = targetBlock.getRelative(BlockFace.UP)
        if (!blockAbove.type.isAir) return

        val cropType =
            when (item.type) {
                Material.WHEAT_SEEDS -> Material.WHEAT
                Material.POTATO -> Material.POTATOES
                Material.CARROT -> Material.CARROTS
                Material.BEETROOT_SEEDS -> Material.BEETROOTS
                Material.MELON_SEEDS -> Material.MELON_STEM
                Material.PUMPKIN_SEEDS -> Material.PUMPKIN_STEM
                Material.COCOA_BEANS -> Material.COCOA
                Material.NETHER_WART -> Material.NETHER_WART
                Material.TORCHFLOWER_SEEDS -> Material.TORCHFLOWER_CROP
                Material.PITCHER_POD -> Material.PITCHER_CROP
                else -> return
            }

        blockAbove.type = cropType
        consumeItem(block, item)
        event.isCancelled = true

        blockAbove.world.playSound(blockAbove.location, BukkitSound.ITEM_CROP_PLANT, 1.0f, 1.0f)
    }

    /**
     * Handles the insertion of a music disc into a jukebox via a dispenser.
     * @param block The dispenser block that is attempting to insert the item.
     * @param item The item stack being dispensed (expected to be a music disc).
     * @param event The block dispense event that triggered this handler.
     * @see getTargetBlock For determining the block in front of the dispenser.
     * @see consumeItem For removing the item from the dispenser's inventory.
     */
    private fun handleJukeboxInsertion(
        block: Block,
        item: ItemStack,
        event: BlockDispenseEvent,
    ) {
        val targetBlock = getTargetBlock(block) ?: return

        if (targetBlock.type != Material.JUKEBOX) return

        val jukebox = targetBlock.state as? Jukebox ?: return

        if (jukebox.record.type != Material.AIR) return

        jukebox.setRecord(item)
        jukebox.update()

        event.isCancelled = true

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable { consumeItem(block, item) },
            1L,
        )
    }

    /**
     * Handles the collection of liquids from cauldrons using empty buckets via dispensers.
     * @param block The dispenser block that is attempting to collect the liquid.
     * @param item The item stack being dispensed (must be an empty bucket).
     * @param dispenser The dispenser block state from which the item is being dispensed.
     * @param event The block dispense event that triggered this handler.
     * @see getTargetBlock For determining the cauldron block in front of the dispenser.
     * @see replaceItem For swapping the empty bucket with a filled bucket in the dispenser.
     * @see isEmptyBucket For checking if the dispensed item is an empty bucket.
     */
    private fun handleCauldronLiquidCollection(
        block: Block,
        item: ItemStack,
        dispenser: Dispenser,
        event: BlockDispenseEvent,
    ) {
        val targetBlock = getTargetBlock(block) ?: return

        if (targetBlock.type != Material.CAULDRON && targetBlock.type != Material.WATER_CAULDRON &&
            targetBlock.type != Material.LAVA_CAULDRON
        ) {
            return
        }

        val filledBucketType =
            when (targetBlock.type) {
                Material.WATER_CAULDRON -> Material.WATER_BUCKET
                Material.LAVA_CAULDRON -> Material.LAVA_BUCKET
                else -> return
            }

        if (targetBlock.type == Material.CAULDRON) return

        replaceItem(dispenser, item, ItemStack(filledBucketType))

        targetBlock.type = Material.CAULDRON
        targetBlock.state.update()

        event.isCancelled = true

        targetBlock.world.playSound(targetBlock.location, BukkitSound.ITEM_BUCKET_FILL, 1.0f, 1.0f)
    }

    /**
     * Gets the block directly in front of a dispenser based on its facing direction.
     * @param dispenserBlock The dispenser block whose target block to find.
     * @return The block directly in front of the dispenser, or `null` if the dispenser
     *         has no facing direction or is not directional.
     * @see Directional For the interface that provides facing direction.
     * @see Block.getRelative For getting adjacent blocks based on direction.
     */
    private fun getTargetBlock(dispenserBlock: Block): Block? {
        val direction = (dispenserBlock.blockData as? Directional)?.facing ?: return null
        return dispenserBlock.getRelative(direction)
    }

    /**
     * Checks if the given material is suitable farmland for planting crops.
     * @param material The material to check.
     * @return `true` if the material is farmland or soul sand, `false` otherwise.
     */
    private fun isFarmland(material: Material): Boolean = material == Material.FARMLAND || material == Material.SOUL_SAND

    /**
     * Consumes one item from the dispenser's inventory.
     * @param block The block to consume the item from.
     * @param item The item stack to consume one unit from.
     * @see Dispenser.inventory For accessing the dispenser's item storage.
     */
    private fun consumeItem(
        block: Block,
        item: ItemStack,
    ) {
        val dispenserState = block.state as? Dispenser ?: return
        val inventory = dispenserState.inventory

        for (slot in 0 until inventory.size) {
            val stack = inventory.getItem(slot)
            if (stack != null && stack.isSimilar(item)) {
                if (stack.amount > 1) {
                    stack.amount -= 1
                } else {
                    inventory.clear(slot)
                }
                return
            }
        }
    }

    /**
     * Replaces an item in the dispenser's inventory with a new item type.
     * @param dispenser The dispenser block state containing the inventory.
     * @param oldItem The item stack to be replaced.
     * @param newItem The new item stack to replace with (amount will be set to match old item).
     * @see Dispenser.inventory For accessing the dispenser's item storage.
     */
    private fun replaceItem(
        dispenser: Dispenser,
        oldItem: ItemStack,
        newItem: ItemStack,
    ) {
        val inventory = dispenser.inventory
        val contents = inventory.contents

        for ((index, item) in contents.withIndex()) {
            if (item != null && item.type == oldItem.type) {
                val newStack = newItem.clone()
                newStack.amount = item.amount
                inventory.setItem(index, newStack)
                break
            }
        }
        dispenser.update()
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
