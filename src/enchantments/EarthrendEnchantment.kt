package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.EnchantmentStorageMeta
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
    private val ORE_TAGS by lazy {
        setOf(
            Tag.COAL_ORES,
            Tag.COPPER_ORES,
            Tag.IRON_ORES,
            Tag.GOLD_ORES,
            Tag.DIAMOND_ORES,
            Tag.EMERALD_ORES,
            Tag.REDSTONE_ORES,
            Tag.LAPIS_ORES,
        )
    }
    private val SPECIAL_ORES =
        setOf(
            Material.ANCIENT_DEBRIS,
            Material.NETHER_QUARTZ_ORE,
            Material.NETHER_GOLD_ORE,
        )

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
     * Handles the vein mining logic when a [Block] is broken.
     * @param event The [BlockBreakEvent].
     */
    fun earthrend(event: BlockBreakEvent) {
        val player = event.player

        if (player.gameMode == GameMode.CREATIVE) return

        val itemInHand = player.inventory.itemInMainHand

        if (!itemInHand.hasItemMeta() || !itemInHand.itemMeta.hasEnchant(get())) return

        val block = event.block

        if (!isOre(block.type)) return

        val blockType = block.type
        val enchantLevel = itemInHand.itemMeta.getEnchantLevel(get())
        val connectedBlocks = findConnectedBlocks(block, blockType, LEVEL_TO_MAX_BLOCKS[enchantLevel] ?: return)

        if (connectedBlocks.size <= 1) return

        val hasTetherEnchant = itemInHand.itemMeta.hasEnchant(TetherEnchantment.get())
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
     * Converts all items in the player's inventory, armor, offhand, and ender chest
     * from the legacy VeinMine enchantment key to Earthrend.
     * @param player The player whose items should be migrated.
     */
    fun migrate(player: Player) {
        val old = VeinMineEnchantment.get()
        val new = get()
        val inv = player.inventory

        migrateSlots(inv, old, new)

        val armor = inv.armorContents.clone()
        armor.forEach { it?.let { item -> migrateItem(item, old, new) } }
        inv.armorContents = armor

        val offhand = inv.itemInOffHand
        if (migrateItem(offhand, old, new)) inv.setItemInOffHand(offhand)

        migrateSlots(player.enderChest, old, new)
    }

    private fun migrateSlots(
        inventory: Inventory,
        old: Enchantment,
        new: Enchantment,
    ) {
        for (i in 0 until inventory.size) {
            val item = inventory.getItem(i) ?: continue
            if (migrateItem(item, old, new)) inventory.setItem(i, item)
        }
    }

    private fun migrateItem(
        item: ItemStack,
        old: Enchantment,
        new: Enchantment,
    ): Boolean {
        val meta = item.itemMeta ?: return false
        return when {
            meta is EnchantmentStorageMeta && meta.hasStoredEnchant(old) -> {
                val level = meta.getStoredEnchantLevel(old)
                meta.removeStoredEnchant(old)
                meta.addStoredEnchant(new, level, true)
                item.itemMeta = meta
                true
            }

            meta.hasEnchant(old) -> {
                val level = meta.getEnchantLevel(old)
                meta.removeEnchant(old)
                meta.addEnchant(new, level, true)
                item.itemMeta = meta
                true
            }

            else -> {
                false
            }
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

    /**
     * Checks if a [Material] is considered an ore for vein mining purposes.
     * @param material The [Material] to check.
     * @return `true` if the material is a valid ore, `false` otherwise.
     */
    private fun isOre(material: Material): Boolean = ORE_TAGS.any { it.isTagged(material) } || material in SPECIAL_ORES
}
