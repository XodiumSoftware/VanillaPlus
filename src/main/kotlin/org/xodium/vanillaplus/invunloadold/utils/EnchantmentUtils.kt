/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold.utils

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.xodium.vanillaplus.invunloadold.Main

class EnchantmentUtils(private val main: Main) {
    fun hasMatchingEnchantments(first: ItemStack, second: ItemStack): Boolean {
        if (!main.getConfig().getBoolean("match-enchantments") && !main.getConfig()
                .getBoolean("match-enchantments-on-books")
        ) {
            //System.out.println(1);
            return true
        }

        if (!main.getConfig().getBoolean("match-enchantments") && main.getConfig()
                .getBoolean("match-enchantments-on-books")
        ) {
            if (first.type != Material.ENCHANTED_BOOK) {
                //System.out.println(2);
                return true
            }
        }


        //System.out.println(4);
        if (!first.hasItemMeta() && !second.hasItemMeta()) return true
        //System.out.println(5);
        val firstMeta =
            if (first.hasItemMeta()) first.itemMeta else Bukkit.getItemFactory().getItemMeta(first.type)
        val secondMeta =
            if (second.hasItemMeta()) second.itemMeta else Bukkit.getItemFactory().getItemMeta(second.type)

        if (firstMeta is EnchantmentStorageMeta && secondMeta is EnchantmentStorageMeta) {
            val firstStorage = firstMeta
            val secondStorage = secondMeta
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


        //System.out.println(6);
        if (!firstMeta.hasEnchants() && !secondMeta.hasEnchants()) return true
        //System.out.println(7);
        if (firstMeta.hasEnchants() && !secondMeta.hasEnchants()) return false
        //System.out.println(8);
        if (!firstMeta.hasEnchants() && secondMeta.hasEnchants()) return false

        //System.out.println(9);
        return firstMeta.enchants == secondMeta.enchants
    }
}
