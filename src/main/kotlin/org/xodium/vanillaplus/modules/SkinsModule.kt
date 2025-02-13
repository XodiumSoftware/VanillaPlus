/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import io.papermc.paper.command.brigadier.argument.ArgumentTypes.player
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.VanillaPlus
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.registries.MaterialRegistry
import java.util.*

class SkinsModule : ModuleInterface {
    override fun enabled(): Boolean = Config.SkinsModule.ENABLED

    private val witherWinners: MutableSet<UUID> = mutableSetOf()
    private val elderGuardianWinners: MutableSet<UUID> = mutableSetOf()
    private val enderDragonWinners: MutableSet<UUID> = mutableSetOf()
    private val wardenWinners: MutableSet<UUID> = mutableSetOf()

    private val witherSkinData = 100
    private val elderGuardianSkinData = 101
    private val enderDragonSkinData = 102
    private val wardenSkinData = 103

    private val baseLore = listOf("<dark_gray>▶ <gray>Click the item to toggle custom skin <dark_gray>◀")

    private val bossMap: Map<EntityType, Pair<MutableSet<UUID>, String>> = mapOf(
        EntityType.WITHER to (witherWinners to "Wither"),
        EntityType.ELDER_GUARDIAN to (elderGuardianWinners to "Elder Guardian"),
        EntityType.ENDER_DRAGON to (enderDragonWinners to "Ender Dragon"),
        EntityType.WARDEN to (wardenWinners to "Warden")
    )

    private fun getSkinLore(player: Player, bossName: String, skinData: Int): List<String> = when {
        isUnlocked(player, skinData) -> baseLore
        else -> baseLore + "<red>Locked! Defeat the $bossName to unlock this skin."
    }

    private fun buildSkinItem(
        material: Material,
        skinData: Int,
        displayName: String,
        bossName: String
    ) = ItemBuilder.from(material)
        .name(Utils.mangoFormat(displayName))
        .lore(getSkinLore(player, bossName, skinData).mm())
        .asGuiItem { player, _ -> toggleSkin(player, skinData) }

    private fun isUnlocked(player: Player, skinData: Int): Boolean =
        when (skinData) {
            witherSkinData -> witherWinners.contains(player.uniqueId)
            elderGuardianSkinData -> elderGuardianWinners.contains(player.uniqueId)
            enderDragonSkinData -> enderDragonWinners.contains(player.uniqueId)
            wardenSkinData -> wardenWinners.contains(player.uniqueId)
            else -> false
        }

    private fun validateItemIsNotNullOrAir(heldItem: ItemStack?, player: Player): Boolean {
        if (heldItem == null || heldItem.type == Material.AIR) {
            player.sendMessage("${VanillaPlus.PREFIX}<red>You must hold an item in your hotbar!".mm())
            return false
        }
        return true
    }

    private fun validateItemIsSword(heldItem: ItemStack, player: Player): Boolean {
        if (heldItem.type !in MaterialRegistry.SWORDS) {
            player.sendMessage("${VanillaPlus.PREFIX}<red>This skin can only be applied to swords!".mm())
            return false
        }
        return true
    }

    private fun validateItemMeta(meta: ItemMeta?, player: Player): Boolean {
        if (meta == null) {
            player.sendMessage("${VanillaPlus.PREFIX}<red>This item cannot hold custom model data!".mm())
            return false
        }
        return true
    }

    private fun getValidHeldItemMeta(player: Player): ItemMeta? {
        val heldItem = player.inventory.getItem(player.inventory.heldItemSlot)
        if (!validateItemIsNotNullOrAir(heldItem, player)) return null
        if (!validateItemIsSword(heldItem!!, player)) return null
        val meta = heldItem.itemMeta
        if (!validateItemMeta(meta, player)) return null
        return meta
    }

    private fun toggleSkin(player: Player, skinData: Int) {
        if (!isUnlocked(player, skinData)) return
        val heldItem = player.inventory.getItem(player.inventory.heldItemSlot) ?: return
        val meta = getValidHeldItemMeta(player) ?: return
        if (meta.hasCustomModelData() && meta.customModelData == skinData) {
            meta.setCustomModelData(null)
            heldItem.itemMeta = meta
            player.sendMessage("${VanillaPlus.PREFIX}<green>Custom skin removed from your item!".mm())
        } else {
            meta.setCustomModelData(skinData)
            heldItem.itemMeta = meta
            player.sendMessage("${VanillaPlus.PREFIX}<green>Custom skin applied to your item!".mm())
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        val bossData = bossMap[event.entityType] ?: return
        bossData.first.add(killer.uniqueId)
        killer.sendMessage(
            "${VanillaPlus.PREFIX}<dark_aqua>Congratulations! You have defeated the ${bossData.second} and unlocked custom skins!".mm()
        )
    }

    override fun gui(): Gui {
        return buildGui {
            title(Utils.birdflopFormat("Skins"))
            component {
                render {
                    it.setItem(
                        0,
                        buildSkinItem(
                            Material.WITHER_SKELETON_SKULL,
                            witherSkinData,
                            "Wither Boss Skin",
                            "Wither"
                        )
                    )
                    it.setItem(
                        1,
                        buildSkinItem(
                            Material.ELDER_GUARDIAN_SPAWN_EGG,
                            elderGuardianSkinData,
                            "Elder Guardian Skin",
                            "Elder Guardian"
                        )
                    )
                    it.setItem(
                        2,
                        buildSkinItem(
                            Material.DRAGON_HEAD,
                            enderDragonSkinData,
                            "Ender Dragon Skin",
                            "Ender Dragon"
                        )
                    )
                    it.setItem(
                        3,
                        buildSkinItem(
                            Material.WARDEN_SPAWN_EGG,
                            wardenSkinData,
                            "Warden Skin",
                            "Warden"
                        )
                    )
                    (4..7).forEach { index -> it.setItem(index, Utils.fillerItem) }
                    it.setItem(8, Utils.backItem)
                }
            }
        }
    }
}