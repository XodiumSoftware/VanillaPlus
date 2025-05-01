/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold.utils

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.ItemMeta
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

object EnchantmentUtils {
    fun hasMatchingEnchantments(first: ItemStack, second: ItemStack): Boolean {
        if (!instance.config.getBoolean("match-enchantments") && !instance.config
                .getBoolean("match-enchantments-on-books")
        ) {
            return true
        }

        if (!instance.config.getBoolean("match-enchantments") && instance.config
                .getBoolean("match-enchantments-on-books")
        ) {
            if (first.type != Material.ENCHANTED_BOOK) {
                return true
            }
        }

        if (!first.hasItemMeta() && !second.hasItemMeta()) return true
        val firstMeta: ItemMeta =
            if (first.hasItemMeta()) first.itemMeta else instance.server.itemFactory.getItemMeta(first.type)
        val secondMeta: ItemMeta =
            if (second.hasItemMeta()) second.itemMeta else instance.server.itemFactory.getItemMeta(second.type)

        if (firstMeta is EnchantmentStorageMeta && secondMeta is EnchantmentStorageMeta) {
            val firstStorage: EnchantmentStorageMeta = firstMeta
            val secondStorage: EnchantmentStorageMeta = secondMeta
            if (firstStorage.storedEnchants.size != secondStorage.storedEnchants.size) {
                return false
            }
            for (enchantment in firstStorage.storedEnchants.entries) {
                if (!secondStorage.storedEnchants.containsKey(enchantment.key)) {
                    return false
                }
                if (secondStorage.getStoredEnchantLevel(enchantment.key) != enchantment.value) {
                    return false
                }
            }
            return true
        }
        if (!firstMeta.hasEnchants() && !secondMeta.hasEnchants()) return true
        if (firstMeta.hasEnchants() && !secondMeta.hasEnchants()) return false
        if (!firstMeta.hasEnchants() && secondMeta.hasEnchants()) return false
        return firstMeta.enchants == secondMeta.enchants
    }
}
