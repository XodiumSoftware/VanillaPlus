/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.interfaces

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.NamespacedKey
import org.bukkit.event.Listener
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe

/**
 * Represents a contract for a module within the system.
 * Every implementing module must define initialization logic and state management.
 * This interface extends the Listener interface, allowing modules to handle events.
 */
interface ModuleInterface : Listener {
    /**
     * Determines if this module is currently enabled.
     * @return `true` if the module is enabled, `false` otherwise.
     */
    fun enabled(): Boolean

    /**
     * Constructs and returns a literal argument builder for the current command source stack.
     * @return A LiteralArgumentBuilder instance representing the command structure, or `null` if not applicable.
     */
    @Suppress("UnstableApiUsage")
    fun cmd(): LiteralArgumentBuilder<CommandSourceStack>? = null

    /**
     * Retrieves a recipe associated with the given key and item.
     * @param key The unique key identifying the recipe.
     * @param item The item associated with the recipe.
     * @return The recipe if found, or `null` if no recipe exists for the given key and item.
     */
    fun recipe(key: NamespacedKey, item: ItemStack): Recipe? = null


    /**
     * Retrieves the graphical user interface (GUI) inventory associated with the module.
     * @return The Inventory representing the module's GUI, or `null` if no GUI is available.
     */
    fun gui(): Inventory? = null
}
