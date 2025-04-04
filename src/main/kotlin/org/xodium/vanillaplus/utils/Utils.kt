/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

@file:Suppress("UnstableApiUsage")

package org.xodium.vanillaplus.utils


import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.VanillaPlus
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
    fun String.fireFmt(inverted: Boolean = false): String =
        "<gradient:${if (inverted) "#EF473A:#CB2D3E" else "#CB2D3E:#EF473A"}>$this<reset>"

    fun String.mangoFmt(inverted: Boolean = false): String =
        "<gradient:${if (inverted) "#FFA751:#FFE259" else "#FFE259:#FFA751"}>$this<reset>"

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
            VanillaPlus.Companion.instance.logger.severe("An Error has occured: ${e.message}")
            e.printStackTrace()
            (ctx.source.sender as Player).sendMessage("${VanillaPlus.Companion.PREFIX}<red>An Error has occured. Check server logs for details.".mm())
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
                Enchantment.BANE_OF_ARTHROPODS -> if (EntityRegistry.ARTHROPODS.contains(entityType)) 2.5 * level else 0.0
                Enchantment.SMITE -> if (EntityRegistry.UNDEAD.contains(entityType)) 2.5 * level else 0.0
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

    /**
     * A function to move bowls and bottles in an inventory.
     *
     * @param inv The inventory to move the bowls and bottles in.
     * @param slot The slot to move the bowls and bottles from.
     * @return True if the bowls and bottles were moved successfully, false otherwise.
     */
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

    /**
     * A function to check if a player has a hoe in their inventory.
     *
     * @param inventory The inventory to check.
     * @return True if the player has a hoe in their inventory, false otherwise.
     */
    fun hasShears(inventory: Array<ItemStack?>): Boolean {
        for (i in 0..<9) {
            if (inventory[i] == null) continue
            if (inventory[i]!!.type == Material.SHEARS) return true
        }
        return false
    }

    /**
     * A function to check if a player has a hoe in their inventory.
     *
     * @param inventory The inventory to check.
     * @return True if the player has a hoe in their inventory, false otherwise.
     */
    fun hasSword(inventory: Array<ItemStack?>): Boolean {
        for (i in 0..<9) {
            if (inventory[i] == null) continue
            if (inventory[i]!!.type.name.endsWith("_SWORD")) return true
        }
        return false
    }

    /**
     * A function to check if a player has a hoe in their hotbar.
     *
     * @param inventory The inventory of the player.
     * @return True if the player has a hoe in their hotbar, false otherwise.
     */
    fun hasHoe(inventory: Array<ItemStack?>): Boolean {
        for (i in 0..<9) {
            if (inventory[i] == null) continue
            if (inventory[i]!!.type.name.endsWith("_HOE")) return true
        }
        return false
    }

    /**
     * A function to get the multiplier of an item stack.
     *
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
     *
     * @param itemStack The item stack to get the base multiplier of.
     * @return The base multiplier of the item stack.
     */
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

    /**
     * A function to get the tps of the server.
     *
     * @return The tps of the server.
     */
    fun getTps(): String {
        val tps = VanillaPlus.Companion.instance.server.tps[0]
        val clampedTps = tps.coerceIn(0.0, 20.0)
        val ratio = clampedTps / 20.0
        val color = getColorForTps(ratio)
        val formattedTps = String.format("%.1f", tps)
        return "<color:$color>$formattedTps</color>"
    }

    /**
     * Calculate a hex color between red and green based on the provided ratio (0.0 to 1.0)
     *
     * @param ratio The ratio to calculate the color for.
     * @return The hex color for the ratio.
     */
    private fun getColorForTps(ratio: Double): String {
        val r = (255 * (1 - ratio)).toInt()
        val g = (255 * ratio).toInt()
        val b = 0
        return String.format("#%02X%02X%02X", r, g, b)
    }

    /**
     * Gets a formatted string representing the current weather in the main world.
     *
     * @return A formatted string representing the weather.
     */
    fun getWeather(): String {
        val world = VanillaPlus.Companion.instance.server.worlds[0]
        return when {
            world.isThundering -> "<red>\uD83C\uDF29<reset>"
            world.hasStorm() -> "<yellow>\uD83C\uDF26<reset>"
            else -> "<green>\uD83C\uDF24<reset>"
        }
    }

    /**
     * Converts a legacy formatted string (with § color codes) to an Adventure Component
     *
     * @param text The legacy formatted string
     * @param char The character to use as the color code character
     * @return An Adventure Component
     */
    fun legacyToComponent(text: String, char: Char = '§'): Component {
        return LegacyComponentSerializer.builder()
            .character(char)
            .hexColors()
            .build()
            .deserialize(text)
    }
}