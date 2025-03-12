/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

@file:Suppress("UnstableApiUsage")

package org.xodium.vanillaplus

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.registries.EntityRegistry
import org.xodium.vanillaplus.registries.MaterialRegistry
import java.util.*


/**
 * Provides utility functions for directory creation and file copying within the plugin.
 */
object Utils {
    val MM = MiniMessage.miniMessage()
    val fillerItem = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).name("".mm()).asGuiItem()
    val backItem = ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
        .name(firewatchFormat("Back").mm())
        .lore(listOf("<dark_gray>✖ <gray>Return to the previous menu").mm())
        .asGuiItem { player, _ -> Gui.faqGUI().open(player) }

    fun firewatchFormat(text: String) = "<b><gradient:#CB2D3E:#EF473A>$text</gradient></b>"
    fun mangoFormat(text: String) = "<b><gradient:#FFE259:#FFA751>$text</gradient></b>"
    fun worldSizeFormat(size: Int) = if (size >= 1000) "${size / 1000}k" else size.toString()

    fun String.mm() = MM.deserialize(this)
    fun List<String>.mm() = map { it.mm() }

    fun EntityType.format(locale: Locale = Locale.ENGLISH, delimiters: String = "_", separator: String = " ") =
        name.lowercase(locale).split(delimiters).joinToString(separator)
        { it.replaceFirstChar { char -> char.uppercaseChar() } }

    fun List<EntityType>.format(separator: String) = this.joinToString(separator) { it.format() }

    fun <K, V : Comparable<V>> sortByValue(map: MutableMap<K?, V?>): MutableMap<K?, V?> {
        val list: MutableList<MutableMap.MutableEntry<K?, V?>> = ArrayList(map.entries)
        list.sortWith(compareBy<MutableMap.MutableEntry<K?, V?>> { it.value })
        val result: MutableMap<K?, V?> = LinkedHashMap()
        for (entry in list) result[entry.key] = entry.value
        return result
    }

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
    fun getDamage(itemStack: ItemStack, entityType: EntityType): Double {
        val base = getBaseDamage(itemStack.type)
        return if (base == 0.0) 0.0 else base + getBonus(itemStack, entityType)
    }

    /**
     * A function to get the bonus damage of an item stack against an entity type.
     *
     * @param itemStack The item stack to get the bonus damage of.
     * @param entityType The entity type to get the bonus damage against.
     * @return The bonus damage of the item stack against the entity type.
     */
    fun getBonus(itemStack: ItemStack, entityType: EntityType): Double =
        itemStack.itemMeta?.enchants?.entries?.sumOf { (enchantment, level) ->
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

    fun refillStack(inventory: Inventory, source: Int, dest: Int, itemStack: ItemStack?) {
        instance.server.scheduler.runTask(instance, Runnable {
            when {
                inventory.getItem(source) == null -> return@Runnable
                inventory.getItem(source) != itemStack -> return@Runnable
                inventory.getItem(dest) != null && !moveBowlsAndBottles(inventory, dest) -> return@Runnable
                else -> {
                    inventory.setItem(source, null)
                    inventory.setItem(dest, itemStack)
                }
            }
        })
    }

    fun getMatchingStackPosition(inventory: PlayerInventory, mat: Material, currentSlot: Int): Int {
        val slots = HashMap<Int?, Int?>()
        for (i in 0..<36) {
            if (i == currentSlot) continue
            val item = inventory.getItem(i)
            if (item == null) continue
            if (item.type != mat) continue
            if (item.amount == 64) return i
            slots[i] = item.amount
        }
        if (slots.isEmpty()) return -1

        val sortedSlots = sortByValue<Int?, Int?>(slots)
        if (sortedSlots.isEmpty()) return -1

        return sortedSlots.entries.toTypedArray().last().key!!
    }

    fun hasShears(hotbarOnly: Boolean, inventory: Array<ItemStack?>): Boolean {
        val maxSlot = if (hotbarOnly) 9 else inventory.size
        for (i in 0..<maxSlot) {
            if (inventory[i] == null) continue
            if (inventory[i]!!.type == Material.SHEARS) return true
        }
        return false
    }

    fun hasSword(hotbarOnly: Boolean, inventory: Array<ItemStack?>): Boolean {
        val maxSlot = if (hotbarOnly) 9 else inventory.size
        for (i in 0..<maxSlot) {
            if (inventory[i] == null) continue
            if (inventory[i]!!.type.name.endsWith("_SWORD")) return true
        }
        return false
    }

    fun hasHoe(hotbarOnly: Boolean, inventory: Array<ItemStack?>): Boolean {
        val maxSlot = if (hotbarOnly) 9 else inventory.size
        for (i in 0..<maxSlot) {
            if (inventory[i] == null) continue
            if (inventory[i]!!.type.name.endsWith("_HOE")) return true
        }
        return false
    }

    fun isLeaves(material: Material): Boolean = material.name.endsWith("_LEAVES")

    fun getMultiplier(itemStack: ItemStack): Int {
        val base = getBaseMultiplier(itemStack)
        val itemMeta = itemStack.itemMeta ?: return base
        val efficiency = Enchantment.EFFICIENCY ?: return base
        if (!itemMeta.hasEnchant(efficiency)) return base
        val efficiencyLevel = itemMeta.getEnchantLevel(efficiency)
        return base + (efficiencyLevel * efficiencyLevel) + 1
    }

    fun getBaseMultiplier(itemStack: ItemStack): Int {
        return when {
            itemStack.type.name.startsWith("DIAMOND") -> 8
            itemStack.type.name.startsWith("IRON") -> 6
            itemStack.type.name.startsWith("NETHERITE") -> 9
            itemStack.type.name.startsWith("STONE") -> 4
            itemStack.type.name.startsWith("WOOD") -> 2
            itemStack.type.name.startsWith("GOLD") -> 12
            else -> 1
        }
    }
}