package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.meta.Damageable
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents an object handling vein mine enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object VeinMineEnchantment : EnchantmentInterface {
    private val DIRECTIONS =
        arrayOf(
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.SOUTH,
            BlockFace.NORTH,
        )

    const val MAX_BLOCKS = 32

    override fun invoke(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder =
        builder
            .description(key.value().replaceFirstChar { it.uppercase() }.mm())
            .anvilCost(2)
            .maxLevel(1)
            .weight(2)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(25, 0))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(75, 0))
            .activeSlots(EquipmentSlotGroup.MAINHAND)

    /**
     * Handles the vein mining logic when a block is broken.
     * @param event The block break event.
     */
    fun veinMine(event: BlockBreakEvent) {
        val player = event.player

        if (player.gameMode == GameMode.CREATIVE) return

        val itemInHand = player.inventory.itemInMainHand

        if (!itemInHand.hasItemMeta() || !itemInHand.itemMeta.hasEnchant(get())) return

        val block = event.block
        val blockType = block.type
        val connectedBlocks = findConnectedBlocks(block, blockType, MAX_BLOCKS)

        if (connectedBlocks.size <= 1) return

        val damageableMeta = itemInHand.itemMeta as? Damageable ?: return
        val maxDurability = itemInHand.type.maxDurability.toInt()
        var currentDamage = damageableMeta.damage

        for (connectedBlock in connectedBlocks) {
            if (connectedBlock == block) continue

            connectedBlock.breakNaturally(itemInHand)
            currentDamage++

            if (currentDamage >= maxDurability) {
                itemInHand.amount = 0
                player.playSound(player.location, Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f)
                break
            }
        }

        if (currentDamage > damageableMeta.damage) {
            damageableMeta.damage = currentDamage
            itemInHand.itemMeta = damageableMeta
        }
    }

    /**
     * Finds all connected blocks of the same type starting from the given block.
     * @param startBlock The starting block.
     * @param targetType The type of block to search for.
     * @param maxBlocks The maximum number of blocks to find.
     * @return A list of connected blocks of the same type.
     */
    private fun findConnectedBlocks(
        startBlock: Block,
        targetType: Material,
        maxBlocks: Int,
    ): List<Block> {
        val visited = LinkedHashSet<Block>(maxBlocks)
        val queue = ArrayDeque<Block>()

        queue.add(startBlock)
        visited.add(startBlock)

        while (queue.isNotEmpty() && visited.size < maxBlocks) {
            val current = queue.removeFirst()

            for (direction in DIRECTIONS) {
                val neighbor = current.getRelative(direction)

                if (visited.size >= maxBlocks) break
                if (neighbor.type == targetType && visited.add(neighbor)) queue.add(neighbor)
            }
        }

        return visited.toList()
    }
}
