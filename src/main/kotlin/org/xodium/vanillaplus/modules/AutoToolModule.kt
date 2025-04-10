/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

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
import org.xodium.vanillaplus.data.BlockTypeData
import org.xodium.vanillaplus.enums.ToolEnum
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.registries.MaterialRegistry
import org.xodium.vanillaplus.utils.Utils
import org.xodium.vanillaplus.utils.Utils.fireFmt
import org.xodium.vanillaplus.utils.Utils.mm
import java.util.*
import java.util.function.ToIntFunction
import java.util.stream.Collectors


/**
 * Handles the automatic switching of tools and weapons.
 */
class AutoToolModule : ModuleInterface {
    override fun enabled(): Boolean = Config.AutoToolModule.ENABLED

    @Suppress("UnstableApiUsage")
    override fun cmd(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("autotool")
            .requires { it.sender.hasPermission(Perms.AutoTool.USE) }
            .executes { it -> Utils.tryCatch(it) { toggle(it.sender as Player) } }
    }

    private val toolMap: MutableMap<Material, ToolEnum> = HashMap()
    private val blockTypeCaches = mutableMapOf<UUID, BlockTypeData>()

    companion object {
        const val HOTBAR_SIZE: Int = 9
        const val INVENTORY_SIZE: Int = 36
    }

    init {
        if (enabled()) {
            Database.createTable(this::class)
        }
        tagToMap(Tag.MINEABLE_AXE, ToolEnum.AXE)
        tagToMap(Tag.MINEABLE_HOE, ToolEnum.HOE)
        tagToMap(Tag.MINEABLE_PICKAXE, ToolEnum.PICKAXE)
        tagToMap(Tag.MINEABLE_SHOVEL, ToolEnum.SHOVEL)

        // NONE SPECIFIC
        tagToMap(Tag.FLOWERS, ToolEnum.NONE)

        // CUSTOM
        addToMap(Material.GLOWSTONE, ToolEnum.PICKAXE)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: EntityDamageByEntityEvent) {
        val damager = event.damager
        if (damager !is Player) return
        val player = damager
        if (!player.hasPermission(Perms.AutoTool.USE)) return
        if (!isEnabledForPlayer(player)) return
        val entity = event.getEntity()
        if (!(entity is Monster && Config.AutoToolModule.USE_SWORD_ON_HOSTILE_MOBS)) return
        val bestRoscoe = getBestRoscoeFromInventory(entity.type, player, Config.AutoToolModule.USE_AXE_AS_SWORD)
        if (bestRoscoe == null || bestRoscoe == player.inventory.itemInMainHand) return
        switchToBestRoscoe(player, bestRoscoe)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerInteractWithBlock(event: PlayerInteractEvent) {
        val player = event.player
        val playerCache = getPlayerCache(player)
        if (playerCache.valid && event.clickedBlock != null && event.clickedBlock!!.type == playerCache.lastMat) return
        if (!player.hasPermission(Perms.AutoTool.USE)) return
        if (!isEnabledForPlayer(player)) return
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
            playerCache.validate(block.type)
            return
        }
        switchToBestTool(player, bestTool, block.type)
        playerCache.validate(block.type)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerQuitEvent) {
        blockTypeCaches.remove(event.player.uniqueId)
    }

    /**
     * Gets the player cache for the given player.
     * @param player The player to get the cache for.
     * @return The BlockTypeData for the player.
     */
    private fun getPlayerCache(player: Player): BlockTypeData {
        return blockTypeCaches.getOrPut(player.uniqueId) { BlockTypeData() }
    }

    /**
     * Equips the best item for the given player and item stack.
     * @param playerInventory The player's inventory to modify.
     * @param bestItem The best item stack to equip.
     */
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

    /**
     * Switches to the best Roscoe for the given player and item stack.
     * @param player The player to switch the Roscoe for.
     * @param bestRoscoe The item stack to switch to.
     */
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

    /**
     * Switches to the best tool for the given player and item stack.
     * @param player The player to switch the tool for.
     * @param itemStack The item stack to switch to.
     * @param target The target material.
     */
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

    /**
     * Checks if the given item stack is a weapon.
     * @param itemStack The item stack to check.
     * @return true if the item stack is a weapon, false otherwise.
     */
    private fun isWeapon(itemStack: ItemStack): Boolean = MaterialRegistry.WEAPONS.contains(itemStack.type)

    /**
     * Checks if the given item stack is a tool or a Roscoe.
     * @param itemStack The item stack to check.
     * @return true if the item stack is a tool or a Roscoe, false otherwise.
     */
    private fun isToolOrRoscoe(itemStack: ItemStack): Boolean =
        MaterialRegistry.DEFAULT_MATERIALS.contains(itemStack.type) || MaterialRegistry.WEAPONS.contains(itemStack.type)

    /**
     * Gets the best tool type for the given material.
     * @param material The material to check.
     * @return The best tool type for the material.
     */
    private fun getBestToolType(material: Material): ToolEnum = toolMap[material] ?: ToolEnum.NONE

    /**
     * Checks if the given item stack has Silk Touch enchantment.
     * @param itemStack The item stack to check.
     * @return true if the item stack has Silk Touch enchantment, false otherwise.
     */
    private fun hasSilkTouch(itemStack: ItemStack?): Boolean =
        itemStack?.itemMeta?.hasEnchant(Enchantment.SILK_TOUCH) == true

    /**
     * Converts the player's inventory to an array of item stacks.
     * @param player The player whose inventory to convert.
     * @return An array of item stacks representing the player's inventory.
     */
    private fun inventoryToArray(player: Player): Array<ItemStack?> =
        Array(INVENTORY_SIZE) { player.inventory.getItem(it) }

    /**
     * Checks if the given material profits from Silk Touch.
     * @param material The material to check.
     * @return true if the material profits from Silk Touch, false otherwise.
     */
    private fun profitsFromSilkTouch(material: Material): Boolean =
        MaterialRegistry.PROFITS_FROM_SILK_TOUCH.contains(material)

    /**
     * Checks if the given material profits from Fortune.
     * @param material The material to check.
     * @return true if the material profits from Fortune, false otherwise.
     */
    @Suppress("unused")
    private fun profitsFromFortune(material: Material): Boolean =
        MaterialRegistry.PROFITS_FROM_FORTUNE.contains(material)

    /**
     * Checks if the given item stack is a tool of the specified type.
     * @param toolEnum The tool type to check.
     * @param itemStack The item stack to check.
     * @return true if the item stack is a tool of the specified type, false otherwise.
     */
    private fun isTool(toolEnum: ToolEnum, itemStack: ItemStack): Boolean {
        val material = itemStack.type
        return when (toolEnum) {
            ToolEnum.PICKAXE -> Tag.ITEMS_PICKAXES.isTagged(material)
            ToolEnum.AXE -> Tag.ITEMS_AXES.isTagged(material)
            ToolEnum.SHOVEL -> Tag.ITEMS_SHOVELS.isTagged(material)
            ToolEnum.HOE -> Tag.ITEMS_HOES.isTagged(material)
            ToolEnum.SHEARS -> material == Material.SHEARS
            ToolEnum.NONE -> isDamageable(itemStack)
            ToolEnum.SWORD -> Tag.ITEMS_SWORDS.isTagged(material)
        }
    }

    /**
     * Checks if the given item stack is a tool of the specified type.
     * @param itemStacks The array of item stacks to check.
     * @param itemStack The item stack to check.
     * @param material The material to check.
     * @return true if the item stack is a tool of the specified type, false otherwise.
     */
    private fun getNonToolItemFromArray(
        itemStacks: Array<ItemStack?>,
        itemStack: ItemStack,
        material: Material?
    ): ItemStack? {
        if (MaterialRegistry.INSTA_BREAKABLE_BY_HAND.contains(material) && !Tag.ITEMS_HOES.isTagged(itemStack.type) ||
            !isToolOrRoscoe(itemStack)
        ) return itemStack
        for (item in itemStacks) if (isDamageable(item)) return item
        return null
    }

    /**
     * Gets the best item stack from the array of item stacks.
     * @param toolEnum The tool type to check.
     * @param itemStacks The array of item stacks to check.
     * @param trySilkTouch Whether to try Silk Touch enchantment.
     * @param itemStack The item stack to check.
     * @param material The material to check.
     * @return The best item stack from the array, or null if none found.
     */
    private fun getBestItemStackFromArray(
        toolEnum: ToolEnum,
        itemStacks: Array<ItemStack?>,
        trySilkTouch: Boolean,
        itemStack: ItemStack,
        material: Material
    ): ItemStack? {
        if (toolEnum == ToolEnum.NONE) {
            return getNonToolItemFromArray(itemStacks, itemStack, material)
        }
        var list: MutableList<ItemStack?> = ArrayList<ItemStack?>()
        for (itemStack in itemStacks) {
            if (itemStack != null && isTool(toolEnum, itemStack) && itemStack.itemMeta is Damageable) {
                val damageable = itemStack.itemMeta as Damageable
                if (damageable.damage < itemStack.type.maxDurability - 1) {
                    if (!trySilkTouch) list.add(itemStack) else if (hasSilkTouch(itemStack)) list.add(itemStack)
                }
            } else if (itemStack != null && isTool(toolEnum, itemStack)) {
                if (!trySilkTouch) list.add(itemStack) else if (hasSilkTouch(itemStack)) list.add(itemStack)
            }
        }
        if (list.isEmpty()) {
            return if (trySilkTouch) getBestItemStackFromArray(
                toolEnum,
                itemStacks,
                false,
                itemStack,
                material
            ) else null
        }
        list.sortWith(Comparator.comparingInt(ToIntFunction { itemStack: ItemStack ->
            Utils.getMultiplier(itemStack)
        }).reversed())
        if (material == Material.DIAMOND_ORE || material == Material.DEEPSLATE_DIAMOND_ORE) {
            list = putIronPlusBeforeGoldPickaxes(list)
        }
        return list[0]
    }

    /**
     * Puts Iron and Gold pickaxes before Diamond pickaxes in the list.
     * @param itemStacks The list of item stacks to modify.
     * @return The modified list of item stacks.
     */
    private fun putIronPlusBeforeGoldPickaxes(itemStacks: MutableList<ItemStack?>?): MutableList<ItemStack?> {
        if (itemStacks == null || itemStacks.isEmpty()) return itemStacks!!
        let {
            if (it.isTool(ToolEnum.PICKAXE, itemStacks[0]!!)) {
                val newList = itemStacks.stream().filter { itemStack: ItemStack? ->
                    when (itemStack!!.type) {
                        Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.GOLDEN_PICKAXE -> return@filter false
                        else -> return@filter true
                    }
                }.collect(Collectors.toList())
                if (!newList.isEmpty()) return newList
            }
        }
        return itemStacks
    }

    /**
     * Gets the best Roscoe from the array of item stacks.
     * @param itemStacks The array of item stacks to check.
     * @param entityType The entity type to check against.
     * @param useAxe Whether to use an axe as a sword.
     * @return The best Roscoe from the array, or null if none found.
     */
    private fun getBestRoscoeFromArray(
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

    /**
     * Checks if the given item stack is a Roscoe.
     * @param itemStack The item stack to check.
     * @param useAxe Whether to use an axe as a sword.
     * @return true if the item stack is a Roscoe, false otherwise.
     */
    private fun isRoscoe(itemStack: ItemStack, useAxe: Boolean): Boolean = when {
        useAxe -> Tag.ITEMS_SWORDS.isTagged(itemStack.type) || Tag.ITEMS_AXES.isTagged(itemStack.type)
        else -> Tag.ITEMS_SWORDS.isTagged(itemStack.type)
    }

    /**
     * Gets the best tool from the player's inventory for the given material.
     * @param material The material to check.
     * @param player The player to check.
     * @param itemStack The item stack to check.
     * @return The best tool from the player's inventory, or null if none found.
     */
    private fun getBestToolFromInventory(
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

    /**
     * Gets the best Roscoe from the player's inventory for the given entity type.
     * @param entityType The entity type to check.
     * @param player The player to check.
     * @param useAxe Whether to use an axe as a sword.
     * @return The best Roscoe from the player's inventory, or null if none found.
     */
    private fun getBestRoscoeFromInventory(
        entityType: EntityType,
        player: Player,
        useAxe: Boolean
    ): ItemStack? = getBestRoscoeFromArray(inventoryToArray(player), entityType, useAxe)

    /**
     * Gets the position of the given item stack in the player's inventory.
     * @param itemStack The item stack to check.
     * @param playerInventory The player's inventory to check.
     * @return The position of the item stack in the inventory, or -1 if not found.
     */
    private fun getPositionInInventory(itemStack: ItemStack, playerInventory: PlayerInventory): Int {
        for (i in 0..<Objects.requireNonNull(playerInventory, "Inventory must not be null").size) {
            val currentItem = playerInventory.getItem(i)
            if (currentItem == null) continue
            if (currentItem == Objects.requireNonNull(itemStack, "Item must not be null")) return i
        }
        return -1
    }

    /**
     * Moves the tool from the source slot to the destination slot in the player's inventory.
     * @param source The source slot.
     * @param dest The destination slot.
     * @param playerInventory The player's inventory to modify.
     */
    private fun moveToolToSlot(source: Int, dest: Int, playerInventory: PlayerInventory) {
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

    /**
     * Checks if the given item stack is damageable.
     * @param itemStack The item stack to check.
     * @return true if the item stack is damageable, false otherwise.
     */
    private fun isDamageable(itemStack: ItemStack?): Boolean =
        itemStack == null || !itemStack.hasItemMeta() || itemStack.itemMeta !is Damageable

    /**
     * Frees the slot in the player's inventory.
     * @param source The source slot.
     * @param playerInventory The player's inventory to modify.
     */
    private fun freeSlot(source: Int, playerInventory: PlayerInventory) {
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

    /**
     * Checks if the AutoTool is enabled for the player.
     * @param player The player to check.
     * @return true if enabled (default), false if explicitly disabled
     */
    private fun isEnabledForPlayer(player: Player): Boolean =
        Database.getData(this::class, player.uniqueId.toString())?.lowercase() != "false"

    /**
     * Toggles the AutoTool setting for the player.
     *
     * @param player The player to toggle.
     */
    private fun toggle(player: Player) {
        val currentValue = isEnabledForPlayer(player)
        val newValue = (!currentValue).toString()
        Database.setData(this::class, player.uniqueId.toString(), newValue)
        player.sendActionBar(("${"AutoTool:".fireFmt()} ${if (!currentValue) "<green>ON" else "<red>OFF"}").mm())
    }
}