/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

@file:Suppress("UnstableApiUsage")

package org.xodium.vanillaplus

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.registries.EntityRegistry
import org.xodium.vanillaplus.registries.MaterialRegistry
import java.util.*


/**
 * Provides utility functions for directory creation and file copying within the plugin.
 */
object Utils {
    val MM = MiniMessage.miniMessage()

    fun String.mm() = MM.deserialize(this)
    fun List<String>.mm() = this.map { it.mm() }
    fun fireWatchFormat(text: String) = "<b><gradient:#CB2D3E:#EF473A>$text</b>".mm()

    fun EntityType.format(locale: Locale = Locale.ENGLISH, delimiters: String = "_", separator: String = " ") =
        name.lowercase(locale).split(delimiters).joinToString(separator)
        { it.replaceFirstChar { char -> char.uppercaseChar() } }

    fun List<EntityType>.format(separator: String) = this.joinToString(separator) { it.format() }

    /**
     * A helper function to wrap command execution with standardized error handling.
     *
     * @param ctx The CommandContext used to obtain the CommandSourceStack.
     * @param action The action to execute, receiving a CommandSourceStack as a parameter.
     * @return Command.SINGLE_SUCCESS after execution.
     */
    fun tryCatch(ctx: CommandContext<CommandSourceStack>, action: (CommandSourceStack) -> Unit): Int {
        try {
            action(ctx.source)
        } catch (e: Exception) {
            instance.logger.severe("An Error has occured: ${e.message}")
            e.printStackTrace()
            (ctx.source.sender as Player).sendMessage("${VanillaPlus.PREFIX}<red>An Error has occured. Check server logs for details.".mm())
        }
        return Command.SINGLE_SUCCESS
    }

    /**
     * A function to get the base damage of a material.
     *
     * @param material The material to get the base damage of.
     * @return The base damage of the material.
     */
    fun getBaseDamage(material: Material): Double = MaterialRegistry.BASE_DAMAGE_MAP[material] ?: 0.0

    /**
     * A function to check if an entity type is an arthropod.
     *
     * @param entityType The entity type to check.
     * @return True if the entity type is an arthropod, false otherwise.
     */
    fun isArthropod(entityType: EntityType): Boolean = EntityRegistry.ARTHROPODS.contains(entityType)

    /**
     * A function to check if an entity type is an undead.
     *
     * @param entityType The entity type to check.
     * @return True if the entity type is an undead, false otherwise.
     */
    fun isUndead(entityType: EntityType): Boolean = EntityRegistry.UNDEAD.contains(entityType)

    /**
     * A function to get the damage of an item stack against an entity type.
     *
     * @param itemStack The item stack to get the damage of.
     * @param entityType The entity type to get the damage against.
     * @return The damage of the item stack against the entity type.
     */
    fun getDamage(itemStack: ItemStack?, entityType: EntityType): Double {
        val base = getBaseDamage(itemStack?.type ?: Material.AIR)
        return if (base == 0.0) 0.0 else base + getBonus(itemStack, entityType)
    }

    /**
     * A function to get the bonus damage of an item stack against an entity type.
     *
     * @param itemStack The item stack to get the bonus damage of.
     * @param entityType The entity type to get the bonus damage against.
     * @return The bonus damage of the item stack against the entity type.
     */
    fun getBonus(itemStack: ItemStack?, entityType: EntityType): Double =
        itemStack?.itemMeta?.enchants?.entries?.sumOf { (enchantment, level) ->
            when (enchantment) {
                Enchantment.SHARPNESS -> 0.5 * level + 0.5
                Enchantment.BANE_OF_ARTHROPODS -> if (isArthropod(entityType)) 2.5 * level else 0.0
                Enchantment.SMITE -> if (isUndead(entityType)) 2.5 * level else 0.0
                else -> 0.0
            }
        } ?: 0.0

    /**
     * A function to check if a material is a bowl or bottle.
     *
     * @param material The material to check.
     * @return True if the material is a bowl or bottle, false otherwise.
     */
    fun isBowlOrBottle(material: Material): Boolean = material in setOf(Material.GLASS_BOTTLE, Material.BOWL)

    fun moveBowlsAndBottles(inv: Inventory, slot: Int): Boolean {
        if (!isBowlOrBottle(Objects.requireNonNull<ItemStack?>(inv.getItem(slot)).type)) return false
        val toBeMoved = inv.getItem(slot)
        inv.clear(slot)
        val leftovers = inv.addItem(toBeMoved!!)
        if (inv.getItem(slot) == null || Objects.requireNonNull<ItemStack?>(inv.getItem(slot))
                .amount == 0 || Objects.requireNonNull<ItemStack?>(inv.getItem(slot)).type == Material.AIR
        ) return true
        if (!leftovers.isEmpty()) {
            for (leftover in leftovers.values) {
                if (inv.holder !is Player) return false
                val p = inv.holder as Player?
                p!!.world.dropItem(p.location, leftover)
            }
            return false
        }
        for (i in 35 downTo 0) {
            inv.clear(slot)
            if (inv.getItem(i) == null || Objects.requireNonNull<ItemStack?>(inv.getItem(i))
                    .amount == 0 || Objects.requireNonNull<ItemStack?>(inv.getItem(i)).type == Material.AIR
            ) {
                inv.setItem(i, toBeMoved)
                return true
            }
        }
        return false
    }

    fun hasShears(inventory: Array<ItemStack?>): Boolean {
        for (i in 0..<9) {
            if (inventory[i] == null) continue
            if (inventory[i]!!.type == Material.SHEARS) return true
        }
        return false
    }

    fun hasSword(inventory: Array<ItemStack?>): Boolean {
        for (i in 0..<9) {
            if (inventory[i] == null) continue
            if (inventory[i]!!.type.name.endsWith("_SWORD")) return true
        }
        return false
    }

    fun hasHoe(inventory: Array<ItemStack?>): Boolean {
        for (i in 0..<9) {
            if (inventory[i] == null) continue
            if (inventory[i]!!.type.name.endsWith("_HOE")) return true
        }
        return false
    }

    fun getMultiplier(itemStack: ItemStack): Int {
        val base = getBaseMultiplier(itemStack)
        val itemMeta = itemStack.itemMeta ?: return base
        val efficiency = Enchantment.EFFICIENCY ?: return base
        if (!itemMeta.hasEnchant(efficiency)) return base
        val efficiencyLevel = itemMeta.getEnchantLevel(efficiency)
        return base + (efficiencyLevel * efficiencyLevel) + 1
    }

    fun getBaseMultiplier(itemStack: ItemStack): Int {
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
}