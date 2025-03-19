/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.Damageable
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Database
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.enums.ToolEnum
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.registries.MaterialRegistry
import java.util.*
import java.util.function.ToIntFunction
import java.util.stream.Collectors


class AutoToolModule : ModuleInterface {
    /**
     * @return true if the module is enabled
     */
    override fun enabled(): Boolean = Config.AutoToolModule.ENABLED

    /**
     * @return the command for the module
     */
    @Suppress("UnstableApiUsage")
    override fun cmd(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("autotool")
            .requires { it.sender.hasPermission(Perms.AutoTool.USE) }
            .executes(Command { Utils.tryCatch(it) { toggle(it.sender as Player) } })
    }

    val toolMap: MutableMap<Material, ToolEnum> = HashMap()

    companion object {
        const val HOTBAR_SIZE = 9
        const val INVENTORY_SIZE = 36
    }

    init {
        if (enabled()) {
            Database.createTable(this::class)
        }
    }

    @EventHandler
    fun on(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) return
        val player = event.damager as Player
        if (!player.hasPermission(Perms.AutoTool.USE)) return
        TODO("check if autotool is enabled in db")
        val entity = event.getEntity()
        if (!(entity is Monster && Config.AutoToolModule.USE_SWORD_ON_HOSTILE_MOBS)) return
        val bestRoscoe = getBestRoscoeFromInventory(entity.type, player, Config.AutoToolModule.USE_AXE_AS_SWORD)
        if (bestRoscoe == null || bestRoscoe == player.inventory.itemInMainHand) return
        switchToBestRoscoe(player, bestRoscoe)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: BlockBreakEvent) {
        val player = event.player
        onPlayerInteractWithBlock(
            PlayerInteractEvent(
                player,
                Action.LEFT_CLICK_BLOCK,
                player.inventory.itemInMainHand,
                event.block,
                BlockFace.SELF,
                EquipmentSlot.HAND
            )
        )
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerInteractWithBlock(event: PlayerInteractEvent) {
//        val playerSetting = main.getPlayerSetting(event.getPlayer()) // FIX
//        if (playerSetting.getBtcache().valid
//            && event.clickedBlock != null && event.clickedBlock!!
//                .type == playerSetting.getBtcache().lastMat
//        ) return
        val player = event.player
        if (!player.hasPermission(Perms.AutoTool.USE)) return
        if (!hasBestToolsEnabled(player)) return
        val block = event.clickedBlock
        if (block == null) return
        if (block.type == Material.AIR) return
        val playerInventory = player.inventory
        if (Config.AutoToolModule.DONT_SWITCH_DURING_BATTLE && isWeapon(playerInventory.itemInMainHand))
            return
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return
        if (event.hand != EquipmentSlot.HAND) return
        val bestTool = getBestToolFromInventory(block.type, player, playerInventory.itemInMainHand)
        if (bestTool == null || bestTool == playerInventory.itemInMainHand) {
//            playerSetting.getBtcache().validate(block.type)
            return
        }
        switchToBestTool(player, bestTool, block.type)
//        playerSetting.getBtcache().validate(block.type)
    }

    private fun hasBestToolsEnabled(player: Player): Boolean =
        TODO(
            "check if autotool is enabled in db, " +
                    "maybe do it directly in the method inline instead of creating an extra method"
        )

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: PlayerQuitEvent): Unit = TODO("check database if enabled")

    init {
        tagToMap(Tag.MINEABLE_AXE, ToolEnum.AXE)
        tagToMap(Tag.MINEABLE_HOE, ToolEnum.HOE)
        tagToMap(Tag.MINEABLE_PICKAXE, ToolEnum.PICKAXE)
        tagToMap(Tag.MINEABLE_SHOVEL, ToolEnum.SHOVEL)

        // NONE SPECIFIC
        tagToMap(Tag.FLOWERS, ToolEnum.NONE)

        // CUSTOM
//        addToMap(Material.GLOWSTONE, ToolEnum.PICKAXE) // TODO: Prefer SilkTouch
    }

    private fun equipBestItem(playerInventory: PlayerInventory, bestItem: ItemStack?) {
        if (bestItem == null) {
            freeSlot(playerInventory.heldItemSlot, playerInventory)
            return
        }
        val positionInInventory = getPositionInInventory(bestItem, playerInventory)
        if (positionInInventory != -1) {
            moveToolToSlot(positionInInventory, playerInventory.heldItemSlot, playerInventory)
        } else {
            freeSlot(playerInventory.heldItemSlot, playerInventory)
        }
    }

    private fun switchToBestRoscoe(player: Player, bestRoscoe: ItemStack?) {
        var bestRoscoe = bestRoscoe
        val playerInventory = player.inventory
        if (bestRoscoe == null) {
            val currentItem = playerInventory.itemInMainHand
            if (isDamageable(currentItem)) return
            bestRoscoe =
                getNonToolItemFromArray(
                    inventoryToArray(player),
                    currentItem,
                    Material.BEDROCK
                )
        }
        equipBestItem(playerInventory, bestRoscoe)
    }

    private fun switchToBestTool(player: Player, itemStack: ItemStack?, target: Material?) {
        var bestTool = itemStack
        val playerInventory = player.inventory
        if (bestTool == null) {
            val currentItem = playerInventory.itemInMainHand
            if (isDamageable(currentItem)) return
            bestTool = getNonToolItemFromArray(inventoryToArray(player), currentItem, target)
        }
        equipBestItem(playerInventory, bestTool)
    }

    /**
     * Adds materials from the given tag to the tool map.
     * @param tag The material tag.
     * @param tool The tool type.
     */
    private fun tagToMap(tag: Tag<Material>, tool: ToolEnum) = tagToMap(tag, tool, null)

    /**
     * Adds materials from the given tag to the tool map.
     * @param tag The material tag.
     * @param tool The tool type.
     * @param filter If not null, only materials whose name contains this filter will be added.
     */
    private fun tagToMap(tag: Tag<Material>, tool: ToolEnum, filter: String?) {
        tag.values.forEach { material ->
            if (filter == null || material.name.contains(filter)) {
                addToMap(material, tool)
            }
        }
    }

    /**
     * Adds materials from the given material to the tool map.
     * @param material The material tag.
     * @param tool The tool type.
     */
    private fun addToMap(material: Material, tool: ToolEnum) {
        toolMap[material] = tool
    }

    fun isWeapon(itemStack: ItemStack): Boolean = MaterialRegistry.WEAPONS.contains(itemStack.type)

    fun isToolOrRoscoe(itemStack: ItemStack): Boolean =
        MaterialRegistry.DEFAULT_MATERIALS.contains(itemStack.type) || MaterialRegistry.WEAPONS.contains(itemStack.type)

    fun getBestToolType(mat: Material): ToolEnum = toolMap[mat] ?: ToolEnum.NONE

    fun hasSilkTouch(itemStack: ItemStack?): Boolean = itemStack?.itemMeta?.hasEnchant(Enchantment.SILK_TOUCH) == true

    fun inventoryToArray(player: Player): Array<ItemStack?> =
        Array(INVENTORY_SIZE) { player.inventory.getItem(it) }

    fun profitsFromSilkTouch(material: Material): Boolean = MaterialRegistry.PROFITS_FROM_SILK_TOUCH.contains(material)

    fun profitsFromFortune(material: Material): Boolean = MaterialRegistry.PROFITS_FROM_FORTUNE.contains(material)

    fun isTool(tool: ToolEnum, itemStack: ItemStack): Boolean {
        val material = itemStack.type
        return when (tool) {
            ToolEnum.PICKAXE -> Tag.ITEMS_PICKAXES.isTagged(material)
            ToolEnum.AXE -> Tag.ITEMS_AXES.isTagged(material)
            ToolEnum.SHOVEL -> Tag.ITEMS_SHOVELS.isTagged(material)
            ToolEnum.HOE -> Tag.ITEMS_HOES.isTagged(material)
            ToolEnum.SHEARS -> material == Material.SHEARS
            ToolEnum.NONE -> isDamageable(itemStack)
            ToolEnum.SWORD -> Tag.ITEMS_SWORDS.isTagged(material)
        }
    }

    fun getNonToolItemFromArray(items: Array<ItemStack?>, currentItem: ItemStack, target: Material?): ItemStack? {
        if (MaterialRegistry.INSTA_BREAKABLE_BY_HAND.contains(target) && !Tag.ITEMS_HOES.isTagged(currentItem.type) ||
            !isToolOrRoscoe(currentItem)
        ) return currentItem
        for (item in items) if (isDamageable(item)) return item
        return null
    }

    fun getBestItemStackFromArray(
        tool: ToolEnum,
        itemStacks: Array<ItemStack?>,
        trySilkTouch: Boolean,
        itemStack: ItemStack,
        material: Material
    ): ItemStack? {
        if (tool == ToolEnum.NONE) {
            return getNonToolItemFromArray(itemStacks, itemStack, material)
        }
        var list: MutableList<ItemStack?> = ArrayList<ItemStack?>()
        for (itemStack in itemStacks) {
            if (itemStack != null && isTool(tool, itemStack) && itemStack.itemMeta is Damageable) {
                val damageable = itemStack.itemMeta as Damageable
                if (damageable.damage < itemStack.type.maxDurability - 1) {
                    if (!trySilkTouch) list.add(itemStack) else if (hasSilkTouch(itemStack)) list.add(itemStack)
                }
            } else if (itemStack != null && isTool(tool, itemStack)) {
                if (!trySilkTouch) list.add(itemStack) else if (hasSilkTouch(itemStack)) list.add(itemStack)
            }
        }
        if (list.isEmpty()) {
            return if (trySilkTouch) getBestItemStackFromArray(tool, itemStacks, false, itemStack, material) else null
        }
        list.sortWith(Comparator.comparingInt<ItemStack?>(ToIntFunction { itemStack: ItemStack ->
            Utils.getMultiplier(itemStack)
        }).reversed())
        if (material == Material.DIAMOND_ORE || material == Material.DEEPSLATE_DIAMOND_ORE) {
            list = putIronPlusBeforeGoldPickaxes(list)
        }
        return list[0]
    }

    private fun putIronPlusBeforeGoldPickaxes(list: MutableList<ItemStack?>?): MutableList<ItemStack?> {
        if (list == null || list.isEmpty()) return list!!
        let {
            if (it.isTool(ToolEnum.PICKAXE, list[0]!!)) {
                val newList = list.stream().filter { itemStack: ItemStack? ->
                    when (itemStack!!.type) {
                        Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.GOLDEN_PICKAXE -> return@filter false
                        else -> return@filter true
                    }
                }.collect(Collectors.toList())
                if (!newList.isEmpty()) return newList
            }
        }
        return list
    }

    fun getBestRoscoeFromArray(
        itemStacks: Array<ItemStack?>,
        entityType: EntityType,
        useAxe: Boolean
    ): ItemStack? {
        val itemStackArrayList = ArrayList<ItemStack?>()
        for (itemStack in itemStacks) {
            if (itemStack != null && isRoscoe(itemStack, useAxe) && itemStack.itemMeta is Damageable) {
                val damageable = itemStack.itemMeta as Damageable
                if (damageable.damage < itemStack.type.maxDurability - 1) {
                    itemStackArrayList.add(itemStack)
                }
            } else if (itemStack != null && isRoscoe(itemStack, useAxe)) {
                itemStackArrayList.add(itemStack)
            }
        }
        if (itemStackArrayList.isEmpty()) return null
        itemStackArrayList.sortWith(Comparator { o1: ItemStack?, o2: ItemStack? ->
            if (Utils.getDamage(o1, entityType) < Utils.getDamage(o2, entityType)) 1 else -1
        })
        return itemStackArrayList[0]
    }

    private fun isRoscoe(itemStack: ItemStack, useAxe: Boolean): Boolean = when {
        useAxe -> Tag.ITEMS_SWORDS.isTagged(itemStack.type) || Tag.ITEMS_AXES.isTagged(itemStack.type)
        else -> Tag.ITEMS_SWORDS.isTagged(itemStack.type)
    }

    fun getBestToolFromInventory(
        material: Material,
        player: Player,
        itemStack: ItemStack
    ): ItemStack? {
        val items = inventoryToArray(player)
        val tool: ToolEnum?
        if (!Tag.LEAVES.isTagged(material) && material != Material.COBWEB) {
            tool = getBestToolType(material)
        } else {
            tool = if (Utils.hasShears(player.inventory.storageContents)) {
                ToolEnum.SHEARS
            } else if (Utils.hasHoe(player.inventory.storageContents)
                && material != Material.COBWEB
            ) {
                ToolEnum.HOE
            } else if (((Config.AutoToolModule.CONSIDER_SWORDS_FOR_COBWEBS
                        && material == Material.COBWEB) || (material != Material.COBWEB
                        && Config.AutoToolModule.CONSIDER_SWORDS_FOR_LEAVES))
                && Utils.hasSword(
                    player.inventory.storageContents
                )
            ) {
                ToolEnum.SWORD
            } else {
                ToolEnum.NONE
            }
        }
        val bestStack = getBestItemStackFromArray(tool, items, profitsFromSilkTouch(material), itemStack, material)
        if (bestStack == null) return getNonToolItemFromArray(items, itemStack, material)
        return bestStack
    }

    fun getBestRoscoeFromInventory(
        entityType: EntityType,
        player: Player,
        useAxe: Boolean
    ): ItemStack? = getBestRoscoeFromArray(inventoryToArray(player), entityType, useAxe)

    fun getPositionInInventory(itemStack: ItemStack, playerInventory: PlayerInventory): Int {
        for (i in 0..<Objects.requireNonNull<PlayerInventory>(playerInventory, "Inventory must not be null").size) {
            val currentItem = playerInventory.getItem(i)
            if (currentItem == null) continue
            if (currentItem == Objects.requireNonNull<ItemStack?>(itemStack, "Item must not be null")) return i
        }
        return -1
    }

    fun moveToolToSlot(source: Int, dest: Int, playerInventory: PlayerInventory) {
        playerInventory.heldItemSlot = dest
        if (source == dest) return
        val sourceItem = playerInventory.getItem(source)
        val destItem = playerInventory.getItem(dest)
        if (source < HOTBAR_SIZE) {
            playerInventory.heldItemSlot = source
            return
        }
        if (destItem == null) {
            playerInventory.setItem(dest, sourceItem)
            playerInventory.setItem(source, null)
        } else {
            playerInventory.setItem(source, destItem)
            playerInventory.setItem(dest, sourceItem)
        }
    }

    fun isDamageable(itemStack: ItemStack?): Boolean =
        itemStack == null || !itemStack.hasItemMeta() || itemStack.itemMeta !is Damageable

    fun freeSlot(source: Int, playerInventory: PlayerInventory) {
        playerInventory.itemInMainHand

        if (isDamageable(playerInventory.itemInMainHand)) return
        val item = playerInventory.getItem(source)
        if (item == null) return
        if (isDamageable(item)) return

        playerInventory.setItem(source, null)
        playerInventory.addItem(item)

        if (playerInventory.getItem(source) == null) {
            playerInventory.heldItemSlot = source
            return
        }
        for (i in source..<INVENTORY_SIZE) {
            if (playerInventory.getItem(i) == null) {
                playerInventory.setItem(i, item)
                playerInventory.setItem(source, null)
                playerInventory.heldItemSlot = source
                return
            }
        }
        for (i in 0..<HOTBAR_SIZE) {
            if (playerInventory.getItem(i) == null || isDamageable(playerInventory.getItem(i))) {
                playerInventory.heldItemSlot = i
            }
        }
    }

    fun toggle(player: Player) {
        TODO("toggle in database")
    }
}