@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import io.papermc.paper.datacomponent.item.ResolvableProfile
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.enchantments.*
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.pdcs.PlayerPDC.nickname
import org.xodium.vanillaplus.utils.ExtUtils.executesCatching
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents a module handling player mechanics within the system. */
internal object PlayerModule : ModuleInterface {
    private val tabListModule by lazy { TabListModule }

    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("nickname")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .executesCatching {
                        if (it.source.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                        nickname(it.source.sender as Player, "")
                    }.then(
                        Commands
                            .argument("name", StringArgumentType.greedyString())
                            .executesCatching {
                                if (it.source.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                                nickname(it.source.sender as Player, StringArgumentType.getString(it, "name"))
                            },
                    ),
                "Allows players to set or remove their nickname",
                listOf("nick"),
            ),
        )

    override val perms =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.nickname".lowercase(),
                "Allows use of the nickname command",
                PermissionDefault.TRUE,
            ),
        )

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        val player = event.player

        player.displayName(player.nickname?.mm())

        if (config.playerModule.i18n.playerJoinMsg
                .isEmpty()
        ) {
            return
        }

        event.joinMessage(null)

        instance.server.onlinePlayers
            .filter { it.uniqueId != player.uniqueId }
            .forEach {
                it.sendMessage(
                    config.playerModule.i18n.playerJoinMsg.mm(
                        Placeholder.component("player", player.displayName()),
                    ),
                )
            }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerQuitEvent) {
        if (config.playerModule.i18n.playerQuitMsg
                .isEmpty()
        ) {
            return
        }

        event.quitMessage(
            config.playerModule.i18n.playerQuitMsg.mm(
                Placeholder.component(
                    "player",
                    event.player.displayName(),
                ),
            ),
        )
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerDeathEvent) {
        val killer = event.entity.killer ?: return

        if (Math.random() < config.playerModule.skullDropChance) {
            event.entity.world.dropItemNaturally(
                event.entity.location,
                playerSkull(event.entity, killer),
            )
        }
        // TODO
//        if (config.playerFeature.i18n.playerDeathMsg.isNotEmpty()) event.deathMessage()
//        if (config.playerFeature.i18n.playerDeathScreenMsg.isNotEmpty()) event.deathScreenMessageOverride()
    }

    @EventHandler
    fun on(event: PlayerAdvancementDoneEvent) {
        if (config.playerModule.i18n.playerAdvancementDoneMsg
                .isEmpty()
        ) {
            return
        }

        event.message(
            config.playerModule.i18n.playerAdvancementDoneMsg.mm(
                Placeholder.component("player", event.player.displayName()),
                Placeholder.component("advancement", event.advancement.displayName()),
            ),
        )
    }

    @EventHandler
    fun on(event: InventoryClickEvent) {
        enderchest(event)
    }

    @EventHandler(ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) {
        xpToBottle(event)
        FeatherFallingEnchantment.featherFalling(event)
    }

    @EventHandler
    fun on(event: BlockBreakEvent) {
        ReplantEnchantment.replant(event)
        SilkTouchEnchantment.silkTouch(event)
        VeinMineEnchantment.veinMine(event)
    }

    @EventHandler
    fun on(event: BlockDropItemEvent) {
        PickupEnchantment.pickup(event)
    }

    @EventHandler
    fun on(event: EntityEquipmentChangedEvent) {
        NightVisionEnchantment.nightVision(event)
    }

    /**
     * Handles the inventory click event where a player can open their ender chest by clicking on an ender chest item
     * in their inventory.
     * @param event The InventoryClickEvent triggered when a player clicks in an inventory.
     */
    private fun enderchest(event: InventoryClickEvent) {
        if (event.click != config.playerModule.enderChestClickType ||
            event.currentItem?.type != Material.ENDER_CHEST ||
            event.clickedInventory?.type != InventoryType.PLAYER
        ) {
            return
        }

        event.isCancelled = true

        instance.server.scheduler.runTask(
            instance,
            Runnable { event.whoClicked.openInventory(event.whoClicked.enderChest) },
        )
    }

    /**
     * Handles the interaction event where a player can convert their experience points into an experience bottle
     * if specific conditions are met.
     * @param event The PlayerInteractEvent triggered when a player interacts with the world or an object.
     */
    private fun xpToBottle(event: PlayerInteractEvent) {
        if (event.clickedBlock?.type != Material.ENCHANTING_TABLE ||
            event.item?.type != Material.GLASS_BOTTLE ||
            !event.player.isSneaking
        ) {
            return
        }

        val player = event.player

        if (player.calculateTotalExperiencePoints() < config.playerModule.xpCostToBottle) return

        player.giveExp(-config.playerModule.xpCostToBottle)
        event.item?.subtract(1)
        player.inventory
            .addItem(ItemStack.of(Material.EXPERIENCE_BOTTLE, 1))
            .values
            .forEach { player.world.dropItemNaturally(player.location, it) }
    }

    /**
     * Sets the nickname of the player to the given name.
     * @param player The player whose nickname is to be set.
     * @param name The new nickname for the player.
     */
    private fun nickname(
        player: Player,
        name: String,
    ) {
        player.nickname = name
        player.displayName(player.nickname?.mm())
        // TODO: add enabled check.
        tabListModule.updatePlayerDisplayName(player)
        player.sendActionBar(
            config.playerModule.i18n.nicknameUpdated.mm(
                Placeholder.component(
                    "nickname",
                    player.displayName(),
                ),
            ),
        )
    }

    /**
     * Creates a custom player skull item when a player is killed.
     * @param entity The player whose head is being created.
     * @param killer The player who killed the entity.
     * @return An [ItemStack] representing the customized player head.
     */
    @Suppress("UnstableApiUsage")
    private fun playerSkull(
        entity: Player,
        killer: Player,
    ): ItemStack =
        ItemStack.of(Material.PLAYER_HEAD).apply {
            setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(entity.playerProfile))
            setData(
                DataComponentTypes.CUSTOM_NAME,
                config.playerModule.i18n.playerHeadName
                    .mm(Placeholder.component("player", entity.name.mm())),
            )
            setData(
                DataComponentTypes.LORE,
                ItemLore
                    .lore(
                        config.playerModule.i18n.playerHeadLore
                            .mm(
                                Placeholder.component("player", entity.name.mm()),
                                Placeholder.component("killer", killer.name.mm()),
                            ),
                    ),
            )
        }

    @Serializable
    data class Config(
        var enabled: Boolean = true,
        var enderChestClickType: ClickType = ClickType.SHIFT_RIGHT,
        var skullDropChance: Double = 0.1,
        var xpCostToBottle: Int = 11,
        var silkTouch: SilkTouchEnchantment = SilkTouchEnchantment(),
        var i18n: I18n = I18n(),
    ) {
        @Serializable
        data class SilkTouchEnchantment(
            var allowSpawnerSilk: Boolean = true,
            var allowBuddingAmethystSilk: Boolean = true,
        )

        @Serializable
        data class I18n(
            var playerHeadName: String = "<player>’s Skull",
            var playerHeadLore: List<String> = listOf("<player> killed by <killer>"),
//          var playerDeathMsg: String = "<killer> <gradient:#FFE259:#FFA751>⚔</gradient> <player>",
            var playerJoinMsg: String = "<green>➕<reset> <gradient:#FFE259:#FFA751>›</gradient> <player>",
            var playerQuitMsg: String = "<red>➖<reset> <gradient:#FFE259:#FFA751>›</gradient> <player>",
            var playerDeathMsg: String = "☠ <gradient:#FFE259:#FFA751>›</gradient>",
            var playerDeathScreenMsg: String = "☠",
            var playerAdvancementDoneMsg: String =
                "\uD83C\uDF89 <gradient:#FFE259:#FFA751>›</gradient> <player> " +
                    "<gradient:#FFE259:#FFA751>has made the advancement:</gradient> <advancement>",
            var nicknameUpdated: String = "<gradient:#CB2D3E:#EF473A>Nickname has been updated to: <nickname></gradient>",
        )
    }
}
