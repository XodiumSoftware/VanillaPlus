/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.Utils.tryCatch

/** Represents a module handling quests mechanics within the system. */
class QuestModule : ModuleInterface<QuestModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    override fun cmds(): CommandData? {
        return CommandData(
            listOf(
                Commands.literal("quests")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx -> ctx.tryCatch { quests() } }
            ),
            "Lists all quests",
            listOf("q")
        )
    }

    override fun perms(): List<Permission> {
        return listOf(
            Permission(
                "${instance::class.simpleName}.quests.use".lowercase(),
                "Allows use of the quests command",
                PermissionDefault.TRUE
            )
        )
    }

    /**
     * Creates an inventory for quests.
     * @return an [Inventory] with 5 slots, each containing a quest item.
     */
    private fun quests(): Inventory {
        return instance.server.createInventory(null, InventoryType.HOPPER, "Quests".fireFmt().mm()).apply {
            setItem(0, questItem())
            setItem(1, questItem())
            setItem(2, questItem())
            setItem(3, questItem())
            setItem(4, questItem())
        }
    }

    /**
     * Creates a quest item with a specific material.
     * @param material the [Material] for the quest item.
     * @param itemName the name of the quest item as a [Component].
     * @param lore the lore of the quest item as [ItemLore].
     * @return an [ItemStack] representing the quest item.
     */
    @Suppress("UnstableApiUsage")
    private fun questItem(material: Material, itemName: Component, lore: ItemLore): ItemStack {
        return ItemStack.of(material).apply {
            setData(DataComponentTypes.ITEM_NAME, itemName)
            setData(DataComponentTypes.LORE, lore)
        }
    }

    data class Config(
        override var enabled: Boolean = true
    ) : ModuleInterface.Config
}