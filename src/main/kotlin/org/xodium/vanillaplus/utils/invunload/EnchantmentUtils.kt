/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.utils.invunload

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

object EnchantmentUtils {
    fun hasMatchingEnchantments(first: ItemStack, second: ItemStack): Boolean {
        val config = Config.InvUnloadModule

        if (!config.MATCH_ENCHANTMENTS && (!config.MATCH_ENCHANTMENTS_ON_BOOKS || first.type != Material.ENCHANTED_BOOK)) return true

        val firstMeta = first.itemMeta ?: instance.server.itemFactory.getItemMeta(first.type)
        val secondMeta = second.itemMeta ?: instance.server.itemFactory.getItemMeta(second.type)

        if (firstMeta == null && secondMeta == null) return true
        if (firstMeta is EnchantmentStorageMeta && secondMeta is EnchantmentStorageMeta) {
            val firstEnchants = firstMeta.storedEnchants
            val secondEnchants = secondMeta.storedEnchants

            if (firstEnchants.size != secondEnchants.size) return false

            return firstEnchants.all { (enchant, level) -> secondEnchants[enchant] == level }
        }

        val firstHasEnchants = firstMeta?.hasEnchants() == true
        val secondHasEnchants = secondMeta?.hasEnchants() == true

        if (!firstHasEnchants && !secondHasEnchants) return true
        if (firstHasEnchants != secondHasEnchants) return false

        return firstMeta?.enchants == secondMeta?.enchants
    }
}