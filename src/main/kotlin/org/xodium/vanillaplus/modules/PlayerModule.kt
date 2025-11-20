@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import io.papermc.paper.datacomponent.item.ResolvableProfile
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent
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
import org.xodium.vanillaplus.managers.ModuleManager
import org.xodium.vanillaplus.pdcs.PlayerPDC.nickname
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch

/** Represents a module handling player mechanics within the system. */
internal class PlayerModule : ModuleInterface<PlayerModule.Config> {
    override val config: Config = Config()

    private val tabListModule by lazy { ModuleManager.tabListModule }

    override fun cmds(): List<CommandData> =
        listOf(
            CommandData(
                Commands
                    .literal("nickname")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx ->
                        ctx.tryCatch {
                            if (it.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                            nickname(it.sender as Player, "")
                        }
                    }.then(
                        Commands
                            .argument("name", StringArgumentType.greedyString())
                            .executes { ctx ->
                                ctx.tryCatch {
                                    if (it.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                                    nickname(it.sender as Player, StringArgumentType.getString(ctx, "name"))
                                }
                            },
                    ),
                "Allows players to set or remove their nickname",
                listOf("nick"),
            ),
        )

    override fun perms(): List<Permission> =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.nickname".lowercase(),
                "Allows use of the nickname command",
                PermissionDefault.TRUE,
            ),
        )

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        if (!enabled()) return

        val player = event.player

        player.displayName(player.nickname?.mm())

        if (config.i18n.playerJoinMsg.isEmpty()) return

        event.joinMessage(null)

        instance.server.onlinePlayers
            .filter { it.uniqueId != player.uniqueId }
            .forEach {
                it.sendMessage(
                    config.i18n.playerJoinMsg.mm(
                        Placeholder.component("player", player.displayName()),
                    ),
                )
            }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerQuitEvent) {
        if (!enabled() || config.i18n.playerQuitMsg.isEmpty()) return

        event.quitMessage(config.i18n.playerQuitMsg.mm(Placeholder.component("player", event.player.displayName())))
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerDeathEvent) {
        if (!enabled()) return

        val killer = event.entity.killer ?: return

        if (Math.random() < config.skullDropChance) {
            event.entity.world.dropItemNaturally(
                event.entity.location,
                playerSkull(event.entity, killer),
            )
        }
        // TODO
//        if (config.i18n.playerDeathMsg.isNotEmpty()) event.deathMessage()
//        if (config.i18n.playerDeathScreenMsg.isNotEmpty()) event.deathScreenMessageOverride()
    }

    @EventHandler
    fun on(event: PlayerAdvancementDoneEvent) {
        if (!enabled() || config.i18n.playerAdvancementDoneMsg.isEmpty()) return

        event.message(
            config.i18n.playerAdvancementDoneMsg.mm(
                Placeholder.component("player", event.player.displayName()),
                Placeholder.component("advancement", event.advancement.displayName()),
            ),
        )
    }

    @EventHandler
    fun on(event: InventoryClickEvent) {
        if (!enabled()) return

        enderchest(event)
    }

    @EventHandler(ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) {
        if (!enabled()) return

        xpToBottle(event)
        FeatherFallingEnchantment.featherFalling(event)
    }

    @EventHandler
    fun on(event: BlockBreakEvent) {
        if (!enabled()) return

        ReplantEnchantment.replant(event)
        SilkTouchEnchantment.silkTouch(event)
        VeinMineEnchantment.veinMine(event)
    }

    @EventHandler
    fun on(event: BlockDropItemEvent) {
        if (!enabled()) return

        PickupEnchantment.pickup(event)
    }

    @EventHandler
    fun on(event: EntityEquipmentChangedEvent) {
        if (!enabled()) return

        NightVisionEnchantment.nightVision(event)
    }

    /**
     * Handles the inventory click event where a player can open their ender chest by clicking on an ender chest item
     * in their inventory.
     * @param event The InventoryClickEvent triggered when a player clicks in an inventory.
     */
    private fun enderchest(event: InventoryClickEvent) {
        if (event.click != config.enderChestClickType ||
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

        if (player.calculateTotalExperiencePoints() < config.xpCostToBottle) return

        player.giveExp(-config.xpCostToBottle)
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
        if (tabListModule.enabled()) tabListModule.updatePlayerDisplayName(player)
        player.sendActionBar(config.i18n.nicknameUpdated.mm(Placeholder.component("nickname", player.displayName())))
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
                config.i18n.playerHeadName.mm(Placeholder.component("player", entity.name.mm())),
            )
            setData(
                DataComponentTypes.LORE,
                ItemLore
                    .lore(
                        config.i18n.playerHeadLore
                            .mm(
                                Placeholder.component("player", entity.name.mm()),
                                Placeholder.component("killer", killer.name.mm()),
                            ),
                    ),
            )
        }

    data class Config(
        var enderChestClickType: ClickType = ClickType.SHIFT_RIGHT,
        var skullDropChance: Double = 0.1,
        var xpCostToBottle: Int = 11,
        var silkTouchConfig: SilkTouchEnchantment.Config = SilkTouchEnchantment.Config(),
        var i18n: I18n = I18n(),
    ) : ModuleInterface.Config {
        data class I18n(
            var playerHeadName: String = "<player>’s Skull",
            var playerHeadLore: List<String> = listOf("<player> killed by <killer>"),
//            var playerDeathMsg: String = "<killer> ${"⚔".mangoFmt(true)} <player>",
            var playerJoinMsg: String = "<green>➕<reset> <mango_inverted>›</mango_inverted> <player>",
            var playerQuitMsg: String = "<red>➖<reset> <mango_inverted>›</mango_inverted> <player>",
            var playerDeathMsg: String = "☠ <mango_inverted>›</mango_inverted>",
            var playerDeathScreenMsg: String = "☠",
            var playerAdvancementDoneMsg: String =
                "\uD83C\uDF89 <mango_inverted>›</mango_inverted> <player> " +
                    "<mango>has made the advancement:</mango> <advancement>",
            var nicknameUpdated: String = "<fire>Nickname has been updated to: <nickname></fire>",
        )
    }
}
