package org.xodium.vanillaplus.enchantments

import com.destroystokyo.paper.MaterialTags
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.meta.Damageable
import org.xodium.vanillaplus.enchantments.EarthrendEnchantment.LEVEL_TO_MAX_BLOCKS
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.Utils.displayName

/** Represents an object handling earthrend enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object EarthrendEnchantment : EnchantmentInterface {
    private val DIRECTIONS =
        arrayOf(
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.SOUTH,
            BlockFace.NORTH,
        )
    private val LEVEL_TO_MAX_BLOCKS = mapOf(1 to 16, 2 to 32, 3 to 48)

    override fun invoke(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder =
        builder
            .description(key.displayName())
            .anvilCost(2)
            .maxLevel(3)
            .weight(2)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(25, 5))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(75, 10))
            .activeSlots(EquipmentSlotGroup.MAINHAND)

    /**
     * Chain-mines all connected ore blocks of the same type when an ore is broken.
     * Only activates in non-creative mode with an Earthrend-enchanted tool in the main hand.
     * The number of additional blocks broken is capped by the enchantment level via [LEVEL_TO_MAX_BLOCKS].
     * If the tool also has [TetherEnchantment], drops are pulled directly into the player's inventory.
     * Durability is consumed for each extra block broken.
     * @param event The [BlockBreakEvent] to handle.
     */
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player

        if (player.gameMode == GameMode.CREATIVE) return

        val itemInHand = player.inventory.itemInMainHand

        if (!itemInHand.containsEnchantment(get())) return

        val block = event.block

        if (!MaterialTags.ORES.isTagged(block.type)) return

        val blockType = block.type
        val enchantLevel = itemInHand.getEnchantmentLevel(get())
        val connectedBlocks = findConnectedBlocks(block, blockType, LEVEL_TO_MAX_BLOCKS[enchantLevel] ?: return)

        if (connectedBlocks.size <= 1) return

        val hasTetherEnchant = itemInHand.containsEnchantment(TetherEnchantment.get())
        val damageableMeta = itemInHand.itemMeta as? Damageable ?: return
        var currentDamage = damageableMeta.damage

        for (connectedBlock in connectedBlocks) {
            if (connectedBlock == block) continue
            if (hasTetherEnchant) {
                val drops = connectedBlock.getDrops(itemInHand, player)

                connectedBlock.type = Material.AIR

                if (drops.isNotEmpty()) {
                    val inventory = player.inventory

                    for (drop in drops) {
                        val remaining = inventory.addItem(drop)

                        for (remainingItem in remaining.values) {
                            connectedBlock.world.dropItem(connectedBlock.location, remainingItem)
                        }
                    }
                }
            } else {
                connectedBlock.breakNaturally(itemInHand)
            }

            currentDamage++

            if (currentDamage >= itemInHand.type.maxDurability.toInt()) break
        }

        if (currentDamage > damageableMeta.damage) {
            damageableMeta.damage = currentDamage
            itemInHand.itemMeta = damageableMeta
        }
    }

    /**
     * Finds all connected [Block]s of the same type starting from the given [Block].
     * @param startBlock The starting [Block].
     * @param targetType The [Material] type of block to search for.
     * @param maxBlocks The maximum number of blocks to find.
     * @return A [List] of connected [Block]s of the same type.
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
