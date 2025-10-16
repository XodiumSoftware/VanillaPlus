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
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.bukkit.Sound as BukkitSound

/** Represents a module handling dispenser mechanics within the system. */
internal class DispenserModule : ModuleInterface<DispenserModule.Config> {
    override val config: Config = Config()

    private val plantableCrops: Set<Material> = Tag.CROPS.values
    private val musicDiscs: Set<Material> = Material.entries.filter { it.name.startsWith("MUSIC_DISC") }.toSet()

    @EventHandler
    fun on(event: BlockDispenseEvent) {
        if (!enabled()) return

        val block = event.block
        val item = event.item
        val dispenser = block.state as? Dispenser ?: return

        when {
            isPlantableCrop(item.type) -> handleCropPlanting(block, item, dispenser, event)
            isMusicDisc(item.type) -> handleJukeboxInsertion(block, item, dispenser, event)
            isEmptyBucket(item.type) -> handleCauldronLiquidCollection(block, item, dispenser, event)
        }
    }

    private fun isPlantableCrop(material: Material): Boolean = material in plantableCrops

    private fun isMusicDisc(material: Material): Boolean = material in musicDiscs

    private fun isEmptyBucket(material: Material): Boolean = material == Material.BUCKET

    private fun handleCropPlanting(
        block: Block,
        item: ItemStack,
        dispenser: Dispenser,
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
        consumeItem(dispenser, item)
        event.isCancelled = true

        blockAbove.world.playSound(blockAbove.location, BukkitSound.ITEM_CROP_PLANT, 1.0f, 1.0f)
    }

    private fun handleJukeboxInsertion(
        block: Block,
        item: ItemStack,
        dispenser: Dispenser,
        event: BlockDispenseEvent,
    ) {
        val targetBlock = getTargetBlock(block) ?: return

        if (targetBlock.type != Material.JUKEBOX) return

        val jukebox = targetBlock.state as? Jukebox ?: return

        if (jukebox.record.type != Material.AIR) return

        jukebox.setRecord(item.clone().apply { amount = 1 })
        jukebox.update()

        consumeItem(dispenser, item)
        event.isCancelled = true
    }

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

    private fun getTargetBlock(dispenserBlock: Block): Block? {
        val direction = (dispenserBlock.blockData as? Directional)?.facing ?: return null
        return dispenserBlock.getRelative(direction)
    }

    private fun isFarmland(material: Material): Boolean = material == Material.FARMLAND || material == Material.SOUL_SAND

    private fun consumeItem(
        dispenser: Dispenser,
        item: ItemStack,
    ) {
        dispenser.inventory.removeItem(item.clone().apply { amount = 1 })
        dispenser.update()
    }

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
