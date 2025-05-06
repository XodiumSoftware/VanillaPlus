/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.utils

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.PREFIX
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.registries.EntityRegistry
import org.xodium.vanillaplus.registries.MaterialRegistry
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import java.util.*

/** General utilities. */
object Utils {
    /**
     * A helper function to wrap command execution with standardised error handling.
     * @param ctx The CommandContext used to get the CommandSourceStack.
     * @param action The action to execute, receiving a CommandSourceStack as a parameter.
     * @return Command.SINGLE_SUCCESS after execution.
     */
    @Suppress("UnstableApiUsage")
    fun tryCatch(ctx: CommandContext<CommandSourceStack>, action: (CommandSourceStack) -> Unit): Int {
        try {
            action(ctx.source)
        } catch (e: Exception) {
            instance.logger.severe("An Error has occurred: ${e.message}")
            e.printStackTrace()
            (ctx.source.sender as Player).sendMessage("${PREFIX}<red>An Error has occurred. Check server logs for details.".mm())
        }
        return Command.SINGLE_SUCCESS
    }

    /**
     * A function to get the damage to an item stack against an entity type.
     * @param itemStack The item stack to get the damage to.
     * @param entityType The entity type to get the damage against.
     * @return The damage to the item stack against the entity type.
     */
    fun getDamage(itemStack: ItemStack?, entityType: EntityType): Double {
        val base = MaterialRegistry.BASE_DAMAGE_MAP[itemStack?.type ?: Material.AIR] ?: 0.0
        return if (base == 0.0) 0.0 else base + getBonus(itemStack, entityType)
    }

    /**
     * A function to get the bonus damage to an item stack against an entity type.
     * @param itemStack The item stack to get the bonus damage to.
     * @param entityType The entity type to get the bonus damage against.
     * @return The bonus damage to the item stack against the entity type.
     */
    private fun getBonus(itemStack: ItemStack?, entityType: EntityType): Double =
        itemStack?.itemMeta?.enchants?.entries?.sumOf { (enchantment, level) ->
            when (enchantment) {
                Enchantment.SHARPNESS -> 0.5 * level + 0.5
                Enchantment.BANE_OF_ARTHROPODS -> if (EntityRegistry.ARTHROPODS.contains(entityType)) 2.5 * level else 0.0
                Enchantment.SMITE -> if (EntityRegistry.UNDEAD.contains(entityType)) 2.5 * level else 0.0
                else -> 0.0
            }
        } ?: 0.0

    /**
     * A function to move bowls and bottles in an inventory.
     * @param inv The inventory to move the bowls and bottles in.
     * @param slot The slot to move the bowls and bottles from.
     * @return True if the bowls and bottles were moved successfully, false otherwise.
     */
    fun moveBowlsAndBottles(inv: Inventory, slot: Int): Boolean {
        val itemStack = inv.getItem(slot) ?: return false
        if (!MaterialRegistry.BOWL_OR_BOTTLE.contains(itemStack.type)) return false

        inv.clear(slot)

        val leftovers = inv.addItem(itemStack)
        if (inv.getItem(slot)?.amount == null ||
            inv.getItem(slot)?.amount == 0 ||
            inv.getItem(slot)?.type == Material.AIR
        ) return true

        if (leftovers.isNotEmpty()) {
            val holder = inv.holder
            if (holder !is Player) return false
            for (leftover in leftovers.values) {
                holder.world.dropItem(holder.location, leftover)
            }
            return false
        }

        for (i in 35 downTo 0) {
            if (inv.getItem(i)?.amount == null ||
                inv.getItem(i)?.amount == 0 ||
                inv.getItem(i)?.type == Material.AIR
            ) {
                inv.setItem(i, itemStack)
                return true
            }
        }
        return false
    }

    /**
     * A function to check if the inventory contains a specific item.
     * @param inventory The inventory to check.
     * @param predicate The predicate to check against the item type.
     * @return True if the inventory contains the item, false otherwise.
     */
    private fun inventoryContains(
        inventory: Array<ItemStack?>,
        predicate: (Material) -> Boolean
    ): Boolean {
        for (i in 0..<9) {
            val item = inventory[i] ?: continue
            if (predicate(item.type)) return true
        }
        return false
    }

    /**
     * A function to check if the inventory contains shears.
     * @param inventory The inventory to check.
     * @return True if the inventory contains shears, false otherwise.
     */
    fun hasShears(inventory: Array<ItemStack?>): Boolean =
        inventoryContains(inventory) { it == Material.SHEARS }

    /**
     * A function to check if the inventory contains a sword.
     * @param inventory The inventory to check.
     * @return True if the inventory contains a sword, false otherwise.
     */
    fun hasSword(inventory: Array<ItemStack?>): Boolean =
        inventoryContains(inventory) { it.name.endsWith("_SWORD") }

    /**
     * A function to check if the inventory contains a hoe.
     * @param inventory The inventory to check.
     * @return True if the inventory contains a hoe, false otherwise.
     */
    fun hasHoe(inventory: Array<ItemStack?>): Boolean =
        inventoryContains(inventory) { it.name.endsWith("_HOE") }

    /**
     * A function to get the multiplier of an item stack.
     * @param itemStack The item stack to get the multiplier of.
     * @return The multiplier of the item stack.
     */
    fun getMultiplier(itemStack: ItemStack): Int {
        val base = getBaseMultiplier(itemStack)
        val itemMeta = itemStack.itemMeta ?: return base
        val efficiency = Enchantment.EFFICIENCY ?: return base
        if (!itemMeta.hasEnchant(efficiency)) return base
        val efficiencyLevel = itemMeta.getEnchantLevel(efficiency)
        return base + (efficiencyLevel * efficiencyLevel) + 1
    }

    /**
     * A function to get the base multiplier of an item stack.
     * @param itemStack The item stack to get the base multiplier of.
     * @return The base multiplier of the item stack.
     */
    private fun getBaseMultiplier(itemStack: ItemStack): Int {
        val itemName = itemStack.type.name
        return when {
            itemName.startsWith("DIAMOND") -> 8
            itemName.startsWith("IRON") -> 6
            itemName.startsWith("NETHERITE") -> 9
            itemName.startsWith("STONE") -> 4
            itemName.startsWith("WOOD") -> 2
            itemName.startsWith("GOLD") -> 12
            else -> 1
        }
    }

    /**
     * A function to get the tps of the server.
     * @return The tps of the server.
     */
    fun getTps(): String {
        val tps = instance.server.tps[0]
        val clampedTps = tps.coerceIn(0.0, 20.0)
        val ratio = clampedTps / 20.0
        val color = getColorForTps(ratio)
        val formattedTps = String.format("%.1f", tps)
        return "<color:$color>$formattedTps</color>"
    }

    /**
     * Calculate a hex colour between red and green based on the provided ratio (0.0 to 1.0).
     * @param ratio The ratio to calculate the colour for.
     * @return The hex colour for the ratio.
     */
    private fun getColorForTps(ratio: Double): String {
        val r = (255 * (1 - ratio)).toInt()
        val g = (255 * ratio).toInt()
        val b = 0
        return String.format("#%02X%02X%02X", r, g, b)
    }

    /**
     * Gets a formatted string representing the current weather in the main world.
     * @return A formatted string representing the weather.
     */
    fun getWeather(): String {
        val world = instance.server.worlds[0]
        return when {
            world.isThundering -> "<red>\uD83C\uDF29<reset>"
            world.hasStorm() -> "<yellow>\uD83C\uDF26<reset>"
            else -> "<green>\uD83C\uDF24<reset>"
        }
    }

    /**
     * Charges the player the specified amount of XP.
     * @param player The player to charge.
     * @param amount The amount of XP to charge.
     */
    fun chargePlayerXp(player: Player, amount: Int): Player {
        return player.apply {
            val remainingXp = maxOf(0, totalExperience - amount)
            totalExperience = 0
            level = 0
            exp = 0f
            if (remainingXp > 0) giveExp(remainingXp)
        }
    }

    /**
     * Checks if the player is on cooldown.
     * @param player The player to check.
     * @param cooldownDuration The cooldown duration in milliseconds.
     * @param key The NamespacedKey to use for the cooldown.
     * @return true if the player is not on cooldown, false otherwise.
     */
    fun cooldown(player: Player, cooldownDuration: Long, key: NamespacedKey): Boolean {
        val now = System.currentTimeMillis()
        val container = player.persistentDataContainer
        val last = container.get(key, PersistentDataType.LONG) ?: 0L
        return if (now >= last + cooldownDuration) {
            container.set(key, PersistentDataType.LONG, now)
            true
        } else {
            player.sendActionBar(("You must wait before using this mechanic again".fireFmt()).mm())
            false
        }
    }

    /**
     * Returns an EnumSet of the enum constants that match the provided regex list.
     * @param enumClass The class of the enum to search.
     * @param regexList A list of regex patterns to match against the enum constant names.
     * @return An EnumSet containing the matching enum constants.
     */
    fun <E : Enum<E>> getEnumsFromRegexList(enumClass: Class<E>, regexList: List<Regex>): EnumSet<E> {
        return enumClass.enumConstants
            ?.filter { constant -> regexList.any { it.matches(constant.name) } }
            ?.toCollection(EnumSet.noneOf(enumClass))
            ?: EnumSet.noneOf(enumClass)
    }
}